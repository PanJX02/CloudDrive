package com.panjx.clouddrive.core.network.retrofit

import com.panjx.clouddrive.core.modle.File
import com.panjx.clouddrive.core.modle.request.RefreshTokenRequest
import com.panjx.clouddrive.core.modle.request.User
import com.panjx.clouddrive.core.modle.response.LoginData
import com.panjx.clouddrive.core.modle.response.NetworkPageData
import com.panjx.clouddrive.core.modle.response.NetworkResponse
import com.panjx.clouddrive.core.modle.response.UploadResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MyNetworkApiService {
    
    // 根据文件夹ID获取文件列表
    @GET("folders/{folderId}/files")
    suspend fun getFilesByFolderId(
        @Path("folderId") folderId: String
    ):NetworkResponse<NetworkPageData<File>>

    // 获取文件详情
    @GET("files/info")
    suspend fun fileDetails(
        @Query(value="id") id:String
    ):NetworkResponse<File>

    // 登录
    @POST("auth/tokens")
    suspend fun login(
        @Body data: User
    ): NetworkResponse<LoginData>
    
    // 注册
    @POST("users")
    suspend fun register(
        @Body data: User
    ): NetworkResponse<LoginData>
    
    // 刷新token
    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
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

    // 上传文件
    @POST("files/upload")
    suspend fun uploadFile(
       @Body file: File
    ): NetworkResponse<UploadResponse>

    // 完成上传
    @POST("files/upload/complete")
    suspend fun uploadComplete(
        @Body file: File
    ): NetworkResponse<Unit>
}