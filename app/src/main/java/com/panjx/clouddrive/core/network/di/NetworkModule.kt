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
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Qualifier
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

class HeadersInterceptor(private val userPreferences: UserPreferences) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        val request = originalRequest.newBuilder()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .build()
        
        return chain.proceed(request)
    }
}

// 添加自定义限定符
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TokenRefreshOkHttpClient

/*
* 网络注入依赖
* */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    // 单例模式存储OkHttpClient实例，避免重复创建
    private val clientWithAuthenticator = AtomicReference<OkHttpClient>()
    private val baseClient = AtomicReference<OkHttpClient>()
    
    const val TIMEOUT_SECONDS = 30L
    
    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    @Singleton
    fun provideNetworkJson(): Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
    }
    
    @Provides
    @Singleton
    fun okHttpCallFactory(
        @TokenRefreshOkHttpClient okHttpClient: OkHttpClient
    ):Call.Factory = okHttpClient

    // 基础OkHttpClient，不包含token刷新机制
    @Provides
    @Singleton
    @BaseOkHttpClient
    fun providesBaseOkHttpClientForHilt(userPreferences: UserPreferences): OkHttpClient {
        return providesBaseOkHttpClient(userPreferences)
    }

    // 供外部直接调用的方法，不含Hilt注解
    fun providesBaseOkHttpClient(userPreferences: UserPreferences): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }

        val headersInterceptor = HeadersInterceptor(userPreferences)

        return OkHttpClient.Builder()
            .addInterceptor(headersInterceptor)
            .addInterceptor(logging)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    // 带有自动token刷新功能的OkHttpClient
    @Provides
    @Singleton
    @TokenRefreshOkHttpClient
    fun providesOkHttpClientForHilt(userPreferences: UserPreferences): OkHttpClient {
        return providesOkHttpClient(userPreferences)
    }

    // 供外部直接调用的方法，不含Hilt注解
    fun providesOkHttpClient(userPreferences: UserPreferences): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }

        // 创建多个拦截器实例，避免并发问题
        val headersInterceptor = HeadersInterceptor(userPreferences)
        val authInterceptor = AuthInterceptor(userPreferences)

        return OkHttpClient.Builder()
            .addInterceptor(headersInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }
    
    // 提供MyRetrofitDatasource的方法
    @Provides
    @Singleton
    fun provideMyRetrofitDatasource(
        userPreferences: UserPreferences,
        @TokenRefreshOkHttpClient okHttpClient: OkHttpClient
    ): MyRetrofitDatasource {
        return MyRetrofitDatasource(userPreferences, okHttpClient)
    }
}