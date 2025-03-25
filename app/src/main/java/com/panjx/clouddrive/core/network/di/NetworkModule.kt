package com.panjx.clouddrive.core.network.di

import android.util.Log
import com.panjx.clouddrive.data.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.util.concurrent.TimeUnit

class AuthInterceptor(private val userPreferences: UserPreferences) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        Log.d("AuthInterceptor", "请求URL: ${originalRequest.url}, 方法: ${originalRequest.method}")
        
        val token = runBlocking { userPreferences.token.first() }
        val request = originalRequest.newBuilder()
            .apply {
                if (token.isNotEmpty()) {
                    addHeader("Authorization", "Bearer $token")
                    Log.d("AuthInterceptor", "添加token: Bearer $token")
                } else {
                    Log.d("AuthInterceptor", "token为空，不添加认证头")
                }
            }
            .build()
        return chain.proceed(request)
    }
}

/*
* 网络注入依赖
* */
object NetworkModule {
    fun provideNetworkJson(): Json = Json{
        ignoreUnknownKeys = true  // 忽略未知字段
    }
    fun okHttpCallFactory(
        okHttpClient: OkHttpClient
    ):Call.Factory = okHttpClient

    fun providesOkHttpClient(userPreferences: UserPreferences): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(userPreferences))
            .addInterceptor(ServerSwitchInterceptor()) // 添加服务器切换拦截器
            .connectTimeout(10, TimeUnit.SECONDS)//连接超时
            .writeTimeout(10, TimeUnit.SECONDS)//写超时
            .readTimeout(10, TimeUnit.SECONDS)//读超时
            .build()
    }

}