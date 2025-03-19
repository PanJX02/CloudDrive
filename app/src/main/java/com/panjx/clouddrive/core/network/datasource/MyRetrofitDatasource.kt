package com.panjx.clouddrive.core.network.datasource

import android.util.Log
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.panjx.clouddrive.core.config.Config
import com.panjx.clouddrive.core.modle.File
import com.panjx.clouddrive.core.modle.request.User
import com.panjx.clouddrive.core.modle.response.NetworkPageData
import com.panjx.clouddrive.core.modle.response.NetworkResponse
import com.panjx.clouddrive.core.network.di.NetworkModule
import com.panjx.clouddrive.core.network.retrofit.MyNetworkApiService
import com.panjx.clouddrive.data.UserPreferences
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.Query

class MyRetrofitDatasource(private val userPreferences: UserPreferences) {
    //网络请求接口
    private val service = Retrofit.Builder()
        .baseUrl(Config.ENDPOINT)
        .callFactory(NetworkModule.providesOkHttpClient(userPreferences))
        .addConverterFactory(NetworkModule.provideNetworkJson().asConverterFactory("application/json".toMediaType())) //添加json转换器
        .build()
        .create(MyNetworkApiService::class.java)


    // 根据文件夹ID获取文件列表
    suspend fun getFilesByFolderId(folderId: String): NetworkResponse<NetworkPageData<File>> {
        return service.getFilesByFolderId(folderId)
    }

    // 获取文件详情
    suspend fun fileDetails(
        @Query(value="id") id:String
    ): NetworkResponse<File>{
        return service.fileDetails(id)
    }

    suspend fun login(
        email: String, password: String
    ): NetworkResponse<String> {
        return service.login(User(email, password))
    }
    
    suspend fun register(
        username: String, password: String
    ): NetworkResponse<String> {
        Log.d("MyRetrofitDatasource", "register: $username, $password")
        return service.register(User(username, password))
    }
    
    suspend fun sendEmailVerifyCode(email: String): NetworkResponse<Nothing> {
        return service.sendEmailVerifyCode(email)
    }
}