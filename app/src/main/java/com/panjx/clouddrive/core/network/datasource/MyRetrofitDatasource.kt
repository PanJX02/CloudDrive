package com.panjx.clouddrive.core.network.datasource

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.panjx.clouddrive.core.config.Config
import com.panjx.clouddrive.core.modle.File
import com.panjx.clouddrive.core.modle.request.User
import com.panjx.clouddrive.core.modle.response.LoginData
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

    // 获取文件列表
    suspend fun files(): NetworkResponse<NetworkPageData<File>>{
        return service.files()
    }

    // 获取文件详情
    suspend fun fileDetails(
        @Query(value="id") id:String
    ): NetworkResponse<File>{
        return service.fileDetails(id)
    }

    suspend fun login(
        email: String, password: String
    ): NetworkResponse<LoginData> {
        return service.login(User(email, password))
    }
    
    suspend fun register(
        email: String, password: String, nickname: String, verifyCode: String = ""
    ): NetworkResponse<LoginData> {
        // 在实际实现中，应该将验证码作为参数传递给后端API
        // 如果验证码参数没有集成在 User 类中，可以使用单独的 API 调用或添加请求头
        
        // 例如：
        // val headers = HashMap<String, String>()
        // headers["verify-code"] = verifyCode
        // return service.registerWithHeaders(User(email, password, nickname), headers)
        
        // 或者创建新的请求模型包含验证码
        // return service.register(RegisterRequest(email, password, nickname, verifyCode))
        
        // 这里先使用简单实现
        return service.register(User(email, password, nickname))
    }
    
    suspend fun sendEmailVerifyCode(email: String): NetworkResponse<Nothing> {
        return service.sendEmailVerifyCode(email)
    }
}