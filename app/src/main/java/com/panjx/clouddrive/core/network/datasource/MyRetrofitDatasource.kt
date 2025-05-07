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
import com.panjx.clouddrive.core.modle.request.PasswordRequest
import com.panjx.clouddrive.core.modle.request.RefreshTokenRequest
import com.panjx.clouddrive.core.modle.request.RenameFileRequest
import com.panjx.clouddrive.core.modle.request.SaveShareFilesRequest
import com.panjx.clouddrive.core.modle.request.ShareRequest
import com.panjx.clouddrive.core.modle.request.User
import com.panjx.clouddrive.core.modle.request.UserFileIdsRequest
import com.panjx.clouddrive.core.modle.request.UserInfoRequest
import com.panjx.clouddrive.core.modle.response.Announcement
import com.panjx.clouddrive.core.modle.response.DownloadResponse
import com.panjx.clouddrive.core.modle.response.LoginData
import com.panjx.clouddrive.core.modle.response.NetworkPageData
import com.panjx.clouddrive.core.modle.response.NetworkResponse
import com.panjx.clouddrive.core.modle.response.ShareListResponse
import com.panjx.clouddrive.core.modle.response.ShareResponse
import com.panjx.clouddrive.core.modle.response.UploadResponse
import com.panjx.clouddrive.core.modle.response.UserInfoResponse
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

    
    suspend fun register(
        username: String, password: String, inviteCode: String? = null
    ): NetworkResponse<LoginData> {
        Log.d("MyRetrofitDatasource", "注册: username=$username, inviteCode=$inviteCode")
        return getService().register(User(username, password, inviteCode))
    }
    
    // 刷新token
    suspend fun refreshToken(): NetworkResponse<LoginData> {
        val refreshToken = runBlocking { userPreferences.refreshToken.first() }
        Log.d("MyRetrofitDatasource", "刷新token（使用基础客户端）")
        return getService().refreshToken(RefreshTokenRequest(refreshToken))
    }

    suspend fun uploadFile(file: File): NetworkResponse<UploadResponse> {
        Log.d("MyRetrofitDatasource", "上传文件: name=${file.fileName}, pid=${file.filePid}")
        try {
            val response = getService().uploadFile(file)
            if (response.code != 1) {
                Log.e("MyRetrofitDatasource", "上传失败: ${response.message}")
            }
            return response
        } catch (e: Exception) {
            Log.e("MyRetrofitDatasource", "上传异常: ${e.message}")
            throw e
        }
    }

    suspend fun uploadComplete(file: File): NetworkResponse<Unit> {
        Log.d("MyRetrofitDatasource", "上传完成: file=$file")
        return getService().uploadComplete(file)
    }
    
    // 获取文件下载链接
    suspend fun getDownloadUrl(fileId: Long): NetworkResponse<DownloadResponse> {
        Log.d("MyRetrofitDatasource", "获取下载链接: fileId=$fileId")
        try {
            val response = getService().getDownloadUrl(DownloadRequest(fileId))
            if (response.code != 1) {
                Log.e("MyRetrofitDatasource", "获取下载链接失败: ${response.message}")
            }
            return response
        } catch (e: Exception) {
            Log.e("MyRetrofitDatasource", "获取下载链接异常: ${e.message}")
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
    suspend fun putInRecycleBin(fileIds: List<Long>): NetworkResponse<Unit> {
        Log.d("MyRetrofitDatasource", "删除文件: fileIds=$fileIds")
        return getService().putInRecycleBin(UserFileIdsRequest(fileIds))
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

    // 分享文件
    suspend fun shareFile(fileIds: List<Long>,validType:Int): NetworkResponse<ShareResponse> {
        Log.d("MyRetrofitDatasource", "分享文件: fileId=$fileIds")
        return getService().shareFile(ShareRequest(fileIds,validType))
    }


    // 根据文件夹ID获取文件列表
    suspend fun getShareFileList(shareKey: String, code: String, folderId: Long? = null): NetworkResponse<NetworkPageData<File>> {
        Log.d("MyRetrofitDatasource", "根据文件夹ID获取文件列表: shareKey=$shareKey, code=$code, folderId=$folderId")
        return getService().shareFileList(shareKey, code, folderId)
    }

    // 转存文件
    suspend fun saveShareFiles(fileIds: List<Long>, targetFolderId: Long, shareKey: String, code: String): NetworkResponse<Unit> {
        Log.d("MyRetrofitDatasource", "转存文件: fileIds=$fileIds, targetFolderId=$targetFolderId, shareKey=$shareKey")
        return getService().saveShareFiles(SaveShareFilesRequest(fileIds, targetFolderId, shareKey, code))
    }

    // 获取分享文件列表
    suspend fun getShareList(): NetworkResponse<List<ShareListResponse>> {
        Log.d("MyRetrofitDatasource", "获取分享列表")
        try {
            val response = getService().getShareList()
            if (response.code != 1) {
                Log.e("MyRetrofitDatasource", "获取分享列表失败: ${response.message}")
            }
            return response
        } catch (e: Exception) {
            Log.e("MyRetrofitDatasource", "获取分享列表异常: ${e.message}")
            throw e
        }
    }
    
    // 取消分享
    suspend fun cancelShare(shareKeyWithCode: String): NetworkResponse<Unit> {
        Log.d("MyRetrofitDatasource", "取消分享: shareKeyWithCode=$shareKeyWithCode")
        try {
            val response = getService().cancelShare(shareKeyWithCode)
            if (response.code != 1) {
                Log.e("MyRetrofitDatasource", "取消分享失败: ${response.message}")
            }
            return response
        } catch (e: Exception) {
            Log.e("MyRetrofitDatasource", "取消分享异常: ${e.message}")
            throw e
        }
    }

    // 获取收藏文件列表
    suspend fun getFavoriteFiles(): NetworkResponse<NetworkPageData<File>> {
        Log.d("MyRetrofitDatasource", "获取收藏文件列表")
        return getService().getFavoriteFiles()
    }

    // 获取公告
    suspend fun getAnnouncement(): NetworkResponse<List<Announcement>> {
        Log.d("MyRetrofitDatasource", "获取公告")
        try {
            val response = getService().getAnnouncements()
            if (response.code != 1) {
                Log.e("MyRetrofitDatasource", "获取公告失败: ${response.message}")
            }
            return response
        } catch (e: Exception) {
            Log.e("MyRetrofitDatasource", "获取公告异常: ${e.message}")
            throw e
        }
    }

    // 回收站文件列表
    suspend fun getRecycleBinFiles(): NetworkResponse<NetworkPageData<File>> {
        Log.d("MyRetrofitDatasource", "获取回收站文件列表")
        return getService().getRecycleBinFiles()
    }

    // 恢复文件
    suspend fun restoreFiles(fileIds: List<Long>): NetworkResponse<Unit> {
        Log.d("MyRetrofitDatasource", "恢复文件: fileIds=$fileIds")
        return getService().restoreFiles(UserFileIdsRequest(fileIds))
    }

    // 彻底删除文件
    suspend fun deleteFilesFromRecycleBin(fileIds: List<Long>): NetworkResponse<Unit> {
        Log.d("MyRetrofitDatasource", "彻底删除文件: fileIds=$fileIds")
        return getService().deleteFilesFromRecycleBin(UserFileIdsRequest(fileIds))
    }

    // 清空回收站
    suspend fun emptyRecycleBin(): NetworkResponse<Unit> {
        Log.d("MyRetrofitDatasource", "清空回收站")
        return getService().clearRecycleBin()
    }

    // 获取用户信息
    suspend fun getUserInfo(): NetworkResponse<UserInfoResponse> {
        Log.d("MyRetrofitDatasource", "获取用户信息")
        return getService().getUserInfo()
    }

    // 搜索文件
    suspend fun searchFiles(keyword: String, folderId: Long? = null): NetworkResponse<NetworkPageData<File>> {
        Log.d("MyRetrofitDatasource", "搜索文件: keyword=$keyword, folderId=$folderId")
        return getService().searchFiles(keyword, folderId)
    }

    // 修改用户信息
    suspend fun updateUserInfo(nickname: String, email: String, avatar: String): NetworkResponse<Unit> {
        Log.d("MyRetrofitDatasource", "修改用户信息: nickname=$nickname, email=$email, avatar=$avatar")
        return getService().updateUserInfo(UserInfoRequest(nickname, email, avatar))
    }

    // 修改密码
    suspend fun updatePassword(oldPassword: String, newPassword: String): NetworkResponse<Unit> {
        Log.d("MyRetrofitDatasource", "修改密码: oldPassword=$oldPassword, newPassword=$newPassword")
        return getService().updatePassword(PasswordRequest(oldPassword, newPassword))
    }
}