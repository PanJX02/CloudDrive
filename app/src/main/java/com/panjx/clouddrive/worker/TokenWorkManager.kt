package com.panjx.clouddrive.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.panjx.clouddrive.TokenManager
import com.panjx.clouddrive.core.modle.response.TokenInfo
import com.panjx.clouddrive.core.network.datasource.MyRetrofitDatasource
import com.panjx.clouddrive.core.network.di.NetworkModule
import com.panjx.clouddrive.data.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * 管理Token刷新相关的WorkManager任务
 */
object TokenWorkManager {
    private const val TAG = "TokenWorkManager"
    private const val TOKEN_REFRESH_WORK_NAME = "token_refresh_work"
    private const val IMMEDIATE_TOKEN_CHECK_WORK_NAME = "immediate_token_check_work"
    
    // 最小刷新间隔（30秒）
    private const val MIN_REFRESH_INTERVAL_SECONDS = 5L
    
    // 最长刷新间隔（12小时）
    private const val MAX_REFRESH_INTERVAL_MINUTES = 12 * 60L
    
    /**
     * 立即执行一次token检查和刷新（应用启动时调用）
     */
    fun scheduleImmediateTokenCheck(context: Context) {
        Log.d(TAG, "调度立即执行的token检查任务")
        
        // 创建一次性工作请求
        val checkWorkRequest = OneTimeWorkRequestBuilder<TokenRefreshWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        
        // 提交工作请求
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                IMMEDIATE_TOKEN_CHECK_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                checkWorkRequest
            )
    }
    
    /**
     * 根据token过期时间设置定期刷新任务
     * @param context 上下文
     * @param forceReschedule 是否强制重新调度任务
     */
    fun scheduleTokenRefresh(context: Context, forceReschedule: Boolean = false) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userPreferences = UserPreferences(context)
                
                // 检查用户是否已登录
                val isLoggedIn = userPreferences.isLoggedIn.first()
                if (!isLoggedIn) {
                    Log.d(TAG, "用户未登录，取消所有token刷新任务")
                    cancelTokenRefreshWork(context)
                    return@launch
                }
                
                // 获取当前token
                val accessToken = userPreferences.token.first()
                if (accessToken.isEmpty()) {
                    Log.d(TAG, "accessToken为空，取消所有token刷新任务")
                    cancelTokenRefreshWork(context)
                    return@launch
                }
                
                // 检查token是否已过期，如果是立即执行刷新
                // 初始化TokenManager检查过期
                val baseClient = NetworkModule.providesBaseOkHttpClient(userPreferences)
                val baseDataSource = MyRetrofitDatasource(userPreferences, baseClient)
                val tokenManager = TokenManager(userPreferences, baseDataSource)
                
                if (tokenManager.isAccessTokenExpired()) {
                    Log.d(TAG, "token已过期，调度立即检查任务")
                    scheduleImmediateTokenCheck(context)
                }
                
                // 解析token获取过期时间
                val tokenInfo = TokenInfo.parseJwt(accessToken)
                if (tokenInfo == null) {
                    Log.e(TAG, "无法解析token，使用默认刷新间隔")
                    scheduleDefaultRefreshWork(context, forceReschedule, true)
                    return@launch
                }
                
                // 获取过期时间
                val expirationTime = tokenInfo.getExpirationTimeMillis()
                val currentTime = System.currentTimeMillis()
                
                // 计算过期时间差（分钟）
                val timeUntilExpiryMinutes = (expirationTime - currentTime) / (60 * 1000)
                
                // 计算过期时间差（秒），用于最小间隔比较
                val timeUntilExpirySeconds = (expirationTime - currentTime) / 1000
                
                // 设置刷新间隔
                val refreshIntervalMinutes = (timeUntilExpiryMinutes / 4)
                    .coerceAtMost(MAX_REFRESH_INTERVAL_MINUTES)
                
                // 确保最小间隔为30秒
                val finalRefreshInterval: Pair<Long, TimeUnit> = if ((timeUntilExpirySeconds / 4) < MIN_REFRESH_INTERVAL_SECONDS) {
                    Pair(MIN_REFRESH_INTERVAL_SECONDS, TimeUnit.SECONDS)
                } else {
                    Pair(refreshIntervalMinutes, TimeUnit.MINUTES)
                }
                
                Log.d(TAG, "设置token刷新间隔: ${if (finalRefreshInterval.second == TimeUnit.SECONDS) "${finalRefreshInterval.first} 秒" else "${finalRefreshInterval.first} 分钟"}，token将在 ${timeUntilExpiryMinutes} 分钟后过期")
                
                // 创建工作约束
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED) // 需要网络连接
                    .build()
                
                // 创建定期工作请求
                val refreshWorkRequest = PeriodicWorkRequestBuilder<TokenRefreshWorker>(
                    finalRefreshInterval.first, finalRefreshInterval.second
                )
                    .setConstraints(constraints)
                    .build()
                
                // 替换已有的工作
                val workPolicy = if (forceReschedule) {
                    ExistingPeriodicWorkPolicy.REPLACE
                } else {
                    ExistingPeriodicWorkPolicy.KEEP
                }
                
                // 提交工作请求
                WorkManager.getInstance(context)
                    .enqueueUniquePeriodicWork(
                        TOKEN_REFRESH_WORK_NAME,
                        workPolicy,
                        refreshWorkRequest
                    )
                
                Log.d(TAG, "Token刷新任务已调度")
                
            } catch (e: Exception) {
                Log.e(TAG, "调度token刷新任务失败", e)
                // 发生错误时，使用默认间隔
                scheduleDefaultRefreshWork(context, forceReschedule, true)
            }
        }
    }
    
    /**
     * 使用默认间隔调度token刷新任务（每6小时，除非设置快速刷新）
     */
    private fun scheduleDefaultRefreshWork(context: Context, forceReschedule: Boolean, useFastRefresh: Boolean = false) {
        // 创建工作约束
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // 需要网络连接
            .build()
        
        // 创建定期工作请求
        val refreshWorkRequest = if (useFastRefresh) {
            // 使用最小刷新间隔（30秒）
            PeriodicWorkRequestBuilder<TokenRefreshWorker>(
                MIN_REFRESH_INTERVAL_SECONDS, TimeUnit.SECONDS
            )
                .setConstraints(constraints)
                .build()
        } else {
            // 使用默认刷新间隔（6小时）
            PeriodicWorkRequestBuilder<TokenRefreshWorker>(
                6, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()
        }
        
        // 替换已有的工作
        val workPolicy = if (forceReschedule) {
            ExistingPeriodicWorkPolicy.REPLACE
        } else {
            ExistingPeriodicWorkPolicy.KEEP
        }
        
        // 提交工作请求
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                TOKEN_REFRESH_WORK_NAME,
                workPolicy,
                refreshWorkRequest
            )
        
        if (useFastRefresh) {
            Log.d(TAG, "已设置快速token刷新任务（${MIN_REFRESH_INTERVAL_SECONDS}秒）")
        } else {
            Log.d(TAG, "已设置默认token刷新任务（6小时）")
        }
    }
    
    /**
     * 取消所有token刷新相关的工作
     */
    fun cancelTokenRefreshWork(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(TOKEN_REFRESH_WORK_NAME)
        WorkManager.getInstance(context).cancelUniqueWork(IMMEDIATE_TOKEN_CHECK_WORK_NAME)
        Log.d(TAG, "已取消所有token刷新任务")
    }
} 