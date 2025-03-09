package com.panjx.clouddrive.core.network.di

import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

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

    fun providesOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)//连接超时
            .writeTimeout(10, TimeUnit.SECONDS)//写超时
            .readTimeout(10, TimeUnit.SECONDS)//读超时
            .build()
    }

}