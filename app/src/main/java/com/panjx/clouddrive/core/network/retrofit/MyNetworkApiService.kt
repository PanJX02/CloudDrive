package com.panjx.clouddrive.core.network.retrofit

import com.panjx.clouddrive.core.modle.File
import com.panjx.clouddrive.core.modle.request.User
import com.panjx.clouddrive.core.modle.response.LoginData
import com.panjx.clouddrive.core.modle.response.NetworkPageData
import com.panjx.clouddrive.core.modle.response.NetworkResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MyNetworkApiService {
    // 获取文件列表
    @GET("files/page")
    suspend fun files(
        @Query("file_pid") filePid: String? = null
    ):NetworkResponse<NetworkPageData<File>>

    // 获取文件详情
    @GET("files/info")
    suspend fun fileDetails(
        @Query(value="id") id:String
    ):NetworkResponse<File>

//    //登录
//    @POST("/login")
//    suspend fun login(
//        @Body data: User
//    ):NetworkResponse<Session>
    @POST("login")
    suspend fun login(
        @Body data: User
    ): NetworkResponse<LoginData>
    
    // 注册
    @POST("register")
    suspend fun register(
        @Body data: User
    ): NetworkResponse<LoginData>
    
    // 发送邮箱验证码
    @POST("sendEmailVerifyCode")
    suspend fun sendEmailVerifyCode(
        @Query("email") email: String
    ): NetworkResponse<Nothing>
    
    // 验证邮箱验证码
    @POST("verifyEmailCode")
    suspend fun verifyEmailCode(
        @Query("email") email: String,
        @Query("code") code: String
    ): NetworkResponse<Boolean>
}