package com.panjx.clouddrive.core.network.di

import android.util.Log
import com.panjx.clouddrive.core.network.datasource.MyRetrofitDatasource
import com.panjx.clouddrive.data.UserPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Singleton

class AuthInterceptor(private val userPreferences: UserPreferences) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        Log.d("AuthInterceptor", "请求URL: ${originalRequest.url}, 方法: ${originalRequest.method}")
        
        val accessToken = runBlocking { userPreferences.token.first() }
        val request = originalRequest.newBuilder()
            .apply {
                if (accessToken.isNotEmpty()) {
                    addHeader("Authorization", "Bearer $accessToken")
                    Log.d("AuthInterceptor", "添加accessToken: Bearer $accessToken")
                } else {
                    Log.d("AuthInterceptor", "accessToken为空，不添加认证头")
                }
            }
            .build()
        return chain.proceed(request)
    }
}

/*
* 网络注入依赖
* */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    // 单例模式存储OkHttpClient实例，避免重复创建
    private val clientWithAuthenticator = AtomicReference<OkHttpClient>()
    private val baseClient = AtomicReference<OkHttpClient>()
    
    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    @Singleton
    fun provideNetworkJsonForHilt(): Json = Json{
        ignoreUnknownKeys = true  // 忽略未知字段
        coerceInputValues = true  // 允许null值被映射到具有默认值的非空类型
        isLenient = true          // 增加宽松模式，更好地处理不规范的JSON
        explicitNulls = false     // 允许缺失字段默认为null
    }
    
    // 为了兼容现有代码，保留静态方法
    @OptIn(ExperimentalSerializationApi::class)
    fun provideNetworkJson(): Json = provideNetworkJsonForHilt()
    
    @Provides
    @Singleton
    fun okHttpCallFactory(
        okHttpClient: OkHttpClient
    ):Call.Factory = okHttpClient

    // 创建基础OkHttpClient（无token刷新功能，用于创建MyRetrofitDatasource）
    @Provides
    @Singleton
    fun providesBaseOkHttpClientForHilt(userPreferences: UserPreferences): OkHttpClient {
        // 如果已经创建过，直接返回
        baseClient.get()?.let { return it }
        
        // 创建新的客户端
        val newClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(userPreferences))
            .addInterceptor(ServerSwitchInterceptor()) // 添加服务器切换拦截器
            .connectTimeout(10, TimeUnit.SECONDS)//连接超时
            .writeTimeout(10, TimeUnit.SECONDS)//写超时
            .readTimeout(10, TimeUnit.SECONDS)//读超时
            .build()
        
        // 存储并返回
        baseClient.set(newClient)
        return newClient
    }
    
    // 为了兼容现有代码，保留静态方法
    fun providesBaseOkHttpClient(userPreferences: UserPreferences): OkHttpClient = 
        providesBaseOkHttpClientForHilt(userPreferences)

    // 创建带有token刷新功能的OkHttpClient
    @Provides
    @Singleton
    fun providesOkHttpClientForHilt(userPreferences: UserPreferences): OkHttpClient {
        // 如果已经创建过，直接返回
        clientWithAuthenticator.get()?.let { return it }
        
        // 先创建基础客户端，用于实例化MyRetrofitDatasource
        val client = providesBaseOkHttpClientForHilt(userPreferences)
        val baseDataSource = MyRetrofitDatasource(userPreferences, client)
        
        // 然后创建包含刷新机制的客户端
        val newClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(userPreferences))
            .addInterceptor(ServerSwitchInterceptor()) // 添加服务器切换拦截器
            .authenticator(TokenRefreshAuthenticator(userPreferences, baseDataSource))
            .connectTimeout(10, TimeUnit.SECONDS)//连接超时
            .writeTimeout(10, TimeUnit.SECONDS)//写超时
            .readTimeout(10, TimeUnit.SECONDS)//读超时
            .build()
        
        // 存储并返回
        clientWithAuthenticator.set(newClient)
        return newClient
    }
    
    // 为了兼容现有代码，保留静态方法
    fun providesOkHttpClient(userPreferences: UserPreferences): OkHttpClient = 
        providesOkHttpClientForHilt(userPreferences)
    
    // 提供MyRetrofitDatasource的方法
    @Provides
    @Singleton
    fun provideMyRetrofitDatasource(
        userPreferences: UserPreferences
    ): MyRetrofitDatasource {
        val client = providesOkHttpClientForHilt(userPreferences)
        return MyRetrofitDatasource(userPreferences, client)
    }
}