package com.panjx.clouddrive.core.network.datasource

import android.util.Log
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.panjx.clouddrive.core.config.Config
import com.panjx.clouddrive.core.modle.File
import com.panjx.clouddrive.core.modle.FileDetail
import com.panjx.clouddrive.core.modle.request.CopyFilesRequest
import com.panjx.clouddrive.core.modle.request.CreateFolderRequest
import com.panjx.clouddrive.core.modle.request.DownloadRequest
import com.panjx.clouddrive.core.modle.request.MoveFilesRequest
import com.panjx.clouddrive.core.modle.request.RefreshTokenRequest
import com.panjx.clouddrive.core.modle.request.RenameFileRequest
import com.panjx.clouddrive.core.modle.request.User
import com.panjx.clouddrive.core.modle.request.UserFileIdsRequest
import com.panjx.clouddrive.core.modle.response.DownloadResponse
import com.panjx.clouddrive.core.modle.response.LoginData
import com.panjx.clouddrive.core.modle.response.NetworkPageData
import com.panjx.clouddrive.core.modle.response.NetworkResponse
import com.panjx.clouddrive.core.modle.response.UploadResponse
import com.panjx.clouddrive.core.network.di.NetworkModule
import com.panjx.clouddrive.core.network.retrofit.MyNetworkApiService
import com.panjx.clouddrive.data.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.Query
import javax.inject.Inject

class MyRetrofitDatasource @Inject constructor(
    private val userPreferences: UserPreferences,
    private val client: OkHttpClient? = null
) {
    // 不再缓存service实例，而是每次获取时创建新的实例，以便使用最新的服务器地址
    private fun getService(): MyNetworkApiService {
        // 对于token刷新相关的功能，使用基础客户端，避免循环依赖
        val clientToUse = if (isTokenRefreshEndpoint()) {
            NetworkModule.providesBaseOkHttpClient(userPreferences)
        } else {
            // 对于其他请求，优先使用传入的client，
            // 如果未指定，则使用带有token刷新机制的client
            client ?: NetworkModule.providesOkHttpClient(userPreferences)
        }
        
        return Retrofit.Builder()
            .baseUrl(Config.getEndpoint(userPreferences))
            .callFactory(clientToUse)
            .addConverterFactory(NetworkModule.provideNetworkJson().asConverterFactory("application/json".toMediaType())) //添加json转换器
            .build()
            .create(MyNetworkApiService::class.java)
    }
    
    // 检查当前请求是否是刷新token的请求
    private fun isTokenRefreshEndpoint(): Boolean {
        // 通过堆栈跟踪查找调用方法
        val stackTrace = Thread.currentThread().stackTrace
        for (element in stackTrace) {
            if (element.methodName == "refreshToken") {
                return true
            }
        }
        return false
    }

    // 根据文件夹ID获取文件列表
    suspend fun getFilesByFolderId(folderId: String): NetworkResponse<NetworkPageData<File>> {
        Log.d("MyRetrofitDatasource", "获取文件列表: folderId=$folderId")
        return getService().getFilesByFolderId(folderId)
    }

    // 获取文件详情
    suspend fun fileDetails(
        @Query(value="id") id:String
    ): NetworkResponse<File>{
        Log.d("MyRetrofitDatasource", "获取文件详情: id=$id")
        return getService().fileDetails(id)
    }

    suspend fun login(
        email: String, password: String
    ): NetworkResponse<LoginData> {
        Log.d("MyRetrofitDatasource", "登录: email=$email")
        return getService().login(User(email, password))
    }
    
    suspend fun register(
        username: String, password: String
    ): NetworkResponse<LoginData> {
        Log.d("MyRetrofitDatasource", "注册: username=$username")
        return getService().register(User(username, password))
    }
    
    // 刷新token
    suspend fun refreshToken(): NetworkResponse<LoginData> {
        val refreshToken = runBlocking { userPreferences.refreshToken.first() }
        Log.d("MyRetrofitDatasource", "刷新token（使用基础客户端）")
        return getService().refreshToken(RefreshTokenRequest(refreshToken))
    }
    
    suspend fun sendEmailVerifyCode(email: String): NetworkResponse<Nothing> {
        Log.d("MyRetrofitDatasource", "发送验证码: email=$email")
        return getService().sendEmailVerifyCode(email)
    }

    suspend fun uploadFile(file: File): NetworkResponse<UploadResponse> {
        Log.d("MyRetrofitDatasource", "========== 开始上传文件API调用 ==========")
        Log.d("MyRetrofitDatasource", "文件详情:")
        Log.d("MyRetrofitDatasource", "- 文件名: ${file.fileName}")
        Log.d("MyRetrofitDatasource", "- 扩展名: ${file.fileExtension}")
        Log.d("MyRetrofitDatasource", "- SHA256: ${file.fileSHA256}")
        Log.d("MyRetrofitDatasource", "- 父目录ID: ${file.filePid}")
        
        try {
            val response = getService().uploadFile(file)
            Log.d("MyRetrofitDatasource", "API调用完成，响应码: ${response.code}")
            
            if (response.code == 0) {
                Log.d("MyRetrofitDatasource", "上传API调用成功:")
                Log.d("MyRetrofitDatasource", "- 文件是否存在: ${response.data?.fileExists}")
                Log.d("MyRetrofitDatasource", "- 域名列表: ${response.data?.domain}")
                Log.d("MyRetrofitDatasource", "- 上传令牌: ${response.data?.uploadToken}")
            } else {
                Log.e("MyRetrofitDatasource", "上传API调用失败:")
                Log.e("MyRetrofitDatasource", "- 错误码: ${response.code}")
                Log.e("MyRetrofitDatasource", "- 错误消息: ${response.message}")
            }
            
            Log.d("MyRetrofitDatasource", "========== 上传文件API调用结束 ==========")
            return response
        } catch (e: Exception) {
            Log.e("MyRetrofitDatasource", "上传API调用异常:", e)
            Log.e("MyRetrofitDatasource", "- 异常类型: ${e.javaClass.simpleName}")
            Log.e("MyRetrofitDatasource", "- 异常消息: ${e.message}")
            Log.d("MyRetrofitDatasource", "========== 上传文件API调用结束(异常) ==========")
            throw e
        }
    }

    suspend fun uploadComplete(file: File): NetworkResponse<Unit> {
        Log.d("MyRetrofitDatasource", "上传完成: file=$file")
        return getService().uploadComplete(file)
    }
    
    // 获取文件下载链接
    suspend fun getDownloadUrl(fileId: Long): NetworkResponse<DownloadResponse> {
        Log.d("MyRetrofitDatasource", "========== 开始获取下载链接API调用 ==========")
        Log.d("MyRetrofitDatasource", "文件ID: $fileId")
        
        try {
            val request = DownloadRequest(fileId)
            Log.d("MyRetrofitDatasource", "发送下载请求: ${request.id}")
            
            // 获取当前服务器地址
            val endpoint = Config.getEndpoint(userPreferences)
            Log.d("MyRetrofitDatasource", "当前服务器地址: $endpoint")
            
            val response = getService().getDownloadUrl(request)
            
            Log.d("MyRetrofitDatasource", "API调用完成，响应码: ${response.code}")
            Log.d("MyRetrofitDatasource", "响应消息: ${response.message}")
            
            if (response.code == 1) {
                Log.d("MyRetrofitDatasource", "获取下载链接成功:")
                Log.d("MyRetrofitDatasource", "- 是否文件夹: ${response.data?.folderType}")
                Log.d("MyRetrofitDatasource", "- 总大小: ${response.data?.totalSize}")
                Log.d("MyRetrofitDatasource", "- 文件数量: ${response.data?.downloadFiles?.size}")
                
                // 记录每个文件的信息
                response.data?.downloadFiles?.forEachIndexed { index, fileInfo ->
                    Log.d("MyRetrofitDatasource", "文件[$index]: ${fileInfo.fileName}, URL长度: ${fileInfo.url?.length ?: 0}")
                }
            } else {
                Log.e("MyRetrofitDatasource", "获取下载链接失败:")
                Log.e("MyRetrofitDatasource", "- 错误码: ${response.code}")
                Log.e("MyRetrofitDatasource", "- 错误消息: ${response.message}")
            }
            
            Log.d("MyRetrofitDatasource", "========== 获取下载链接API调用结束 ==========")
            return response
        } catch (e: Exception) {
            Log.e("MyRetrofitDatasource", "获取下载链接异常:", e)
            Log.e("MyRetrofitDatasource", "- 异常类型: ${e.javaClass.simpleName}")
            Log.e("MyRetrofitDatasource", "- 异常消息: ${e.message}")
            Log.e("MyRetrofitDatasource", "- 异常堆栈: ", e)
            Log.d("MyRetrofitDatasource", "========== 获取下载链接API调用结束(异常) ==========")
            throw e
        }
    }
    
    // 复制文件
    suspend fun copyFiles(fileIds: List<Long>, targetFolderId: Long): NetworkResponse<Unit> {
        Log.d("MyRetrofitDatasource", "复制文件: fileIds=$fileIds, targetFolderId=$targetFolderId")
        return getService().copyFiles(CopyFilesRequest(fileIds, targetFolderId))
    }
    
    // 移动文件
    suspend fun moveFiles(fileIds: List<Long>, targetFolderId: Long): NetworkResponse<Unit> {
        Log.d("MyRetrofitDatasource", "移动文件: fileIds=$fileIds, targetFolderId=$targetFolderId")
        return getService().moveFiles(MoveFilesRequest(fileIds, targetFolderId))
    }
    
    // 创建文件夹
    suspend fun createFolder(name: String, parentId: Long): NetworkResponse<Unit> {
        Log.d("MyRetrofitDatasource", "创建文件夹: name=$name, parentId=$parentId")
        return getService().createFolder(CreateFolderRequest(name, parentId))
    }
    
    // 删除文件
    suspend fun deleteFiles(fileIds: List<Long>): NetworkResponse<Unit> {
        Log.d("MyRetrofitDatasource", "删除文件: fileIds=$fileIds")
        return getService().deleteFiles(UserFileIdsRequest(fileIds))
    }

    // 重命名文件
    suspend fun renameFile(fileId: Long, newName: String): NetworkResponse<Unit> {
        Log.d("MyRetrofitDatasource", "重命名文件: fileId=$fileId, newName=$newName")
        return getService().renameFile(RenameFileRequest(fileId, newName))
    }

    // 收藏文件
    suspend fun favorites(fileIds: List<Long>): NetworkResponse<Unit> {
        Log.d("MyRetrofitDatasource", "收藏文件: fileId=$fileIds")
        return getService().favorites(UserFileIdsRequest(fileIds))
    }

    // 取消收藏文件
    suspend fun unfavorites(fileIds: List<Long>): NetworkResponse<Unit> {
        Log.d("MyRetrofitDatasource", "取消收藏文件: fileId=$fileIds")
        return getService().unFavorites(UserFileIdsRequest(fileIds))
    }

    // 获取文件详情
    suspend fun getFileDetails(fileIds: List<Long>): NetworkResponse<FileDetail> {
        Log.d("MyRetrofitDatasource", "获取文件详情: fileId=$fileIds")
        return getService().getFileDetails(UserFileIdsRequest(fileIds))
    }
}