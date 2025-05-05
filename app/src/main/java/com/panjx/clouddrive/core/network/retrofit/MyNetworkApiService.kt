package com.panjx.clouddrive.core.network.retrofit

import com.panjx.clouddrive.core.modle.File
import com.panjx.clouddrive.core.modle.FileDetail
import com.panjx.clouddrive.core.modle.request.CopyFilesRequest
import com.panjx.clouddrive.core.modle.request.CreateFolderRequest
import com.panjx.clouddrive.core.modle.request.DownloadRequest
import com.panjx.clouddrive.core.modle.request.MoveFilesRequest
import com.panjx.clouddrive.core.modle.request.RefreshTokenRequest
import com.panjx.clouddrive.core.modle.request.RenameFileRequest
import com.panjx.clouddrive.core.modle.request.SaveShareFilesRequest
import com.panjx.clouddrive.core.modle.request.ShareRequest
import com.panjx.clouddrive.core.modle.request.User
import com.panjx.clouddrive.core.modle.request.UserFileIdsRequest
import com.panjx.clouddrive.core.modle.response.Announcement
import com.panjx.clouddrive.core.modle.response.DownloadResponse
import com.panjx.clouddrive.core.modle.response.LoginData
import com.panjx.clouddrive.core.modle.response.NetworkPageData
import com.panjx.clouddrive.core.modle.response.NetworkResponse
import com.panjx.clouddrive.core.modle.response.ShareListResponse
import com.panjx.clouddrive.core.modle.response.ShareResponse
import com.panjx.clouddrive.core.modle.response.UploadResponse
import com.panjx.clouddrive.core.modle.response.UserInfoResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
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
    
    // 获取文件下载链接
    @POST("files/download")
    suspend fun getDownloadUrl(
        @Body request: DownloadRequest
    ): NetworkResponse<DownloadResponse>
    
    // 复制文件
    @POST("files/copy")
    suspend fun copyFiles(
        @Body request: CopyFilesRequest
    ): NetworkResponse<Unit>
    
    // 移动文件
    @POST("files/move")
    suspend fun moveFiles(
        @Body request: MoveFilesRequest
    ): NetworkResponse<Unit>
    
    // 创建文件夹
    @POST("folders/create")
    suspend fun createFolder(
        @Body request: CreateFolderRequest
    ): NetworkResponse<Unit>

    // 删除文件
    @POST("files/delete")
    suspend fun deleteFiles(
        @Body request: UserFileIdsRequest
    ): NetworkResponse<Unit>

    // 放入回收站
    @POST("files/recycle")
    suspend fun putInRecycleBin(
        @Body request: UserFileIdsRequest
    ): NetworkResponse<Unit>

    // 文件详情
    @POST("files/detail")
    suspend fun getFileDetails(
        @Body request: UserFileIdsRequest
    ): NetworkResponse<FileDetail>

    // 重命名文件
    @POST("files/rename")
    suspend fun renameFile(
        @Body request: RenameFileRequest
    ): NetworkResponse<Unit>

    // 收藏文件
    @POST("files/favorite")
    suspend fun favorites(
        @Body request: UserFileIdsRequest
    ): NetworkResponse<Unit>

    // 取消收藏文件
    @POST("files/unfavorite")
    suspend fun unFavorites(
        @Body request: UserFileIdsRequest
    ): NetworkResponse<Unit>

    // 获取收藏文件列表
    @GET("files/favorites")
    suspend fun getFavoriteFiles(
    ): NetworkResponse<NetworkPageData<File>>

    // 分享文件
    @POST("share")
    suspend fun shareFile(
        @Body request: ShareRequest
    ): NetworkResponse<ShareResponse>

    // 分享文件列表
    @GET("share/files")
    suspend fun shareFileList(
        @Query("shareKey") shareKey: String,
        @Query("code") code: String,
        @Query("folderId") folderId: Long?
    ): NetworkResponse<NetworkPageData<File>>

    // 转存文件
    @POST("share/save")
    suspend fun saveShareFiles(
        @Body request: SaveShareFilesRequest
    ): NetworkResponse<Unit>

    // 获取当前用户的分享列表
    @GET("share/list")
    suspend fun getShareList(
    ): NetworkResponse<List<ShareListResponse>>
    
    // 取消分享
    @DELETE("share")
    suspend fun cancelShare(
        @Query("shareKey") shareKey: String,
        @Query("code") code: String? = null
    ): NetworkResponse<Unit>

    // 获取公告
    @GET("announcements")
    suspend fun getAnnouncements(
    ): NetworkResponse<List<Announcement>>

    // 回收站文件列表
    @GET("recycle-bin")
    suspend fun getRecycleBinFiles(
    ): NetworkResponse<NetworkPageData<File>>

    // 恢复文件
    @POST("recycle-bin/restore")
    suspend fun restoreFiles(
        @Body request: UserFileIdsRequest
    ): NetworkResponse<Unit>

    // 彻底删除文件
    @POST("recycle-bin/delete")
    suspend fun deleteFilesFromRecycleBin(
        @Body request: UserFileIdsRequest
    ): NetworkResponse<Unit>

    // 清空回收站
    @POST("recycle-bin/clear")
    suspend fun clearRecycleBin(
    ): NetworkResponse<Unit>

    // 获取用户信息
    @GET("users/current")
    suspend fun getUserInfo(
    ): NetworkResponse<UserInfoResponse>

    // 文件搜索
    @GET("files/search")
    suspend fun searchFiles(
        @Query("keyword") keyword: String,
        @Query("folderId") folderId: Long? = null
    ): NetworkResponse<NetworkPageData<File>>
}