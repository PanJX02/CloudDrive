package com.panjx.clouddrive.feature.recycleBin

import com.panjx.clouddrive.core.modle.File
import com.panjx.clouddrive.core.network.datasource.MyRetrofitDatasource
import javax.inject.Inject

class RecycleBinRepository @Inject constructor(
    private val networkDatasource: MyRetrofitDatasource
) {
    // 获取回收站文件列表
    suspend fun getRecycleBinFiles(): List<File> {
        val response = networkDatasource.getRecycleBinFiles()
        if (response.code != 1) {
            throw Exception(response.message)
        }
        return response.data?.list ?: emptyList()
    }
    
    // 恢复文件
    suspend fun restoreFiles(fileIds: List<Long>) {
        val response = networkDatasource.restoreFiles(fileIds)
        if (response.code != 1) {
            throw Exception(response.message)
        }
    }
    
    // 彻底删除文件
    suspend fun deleteFilesFromRecycleBin(fileIds: List<Long>) {
        val response = networkDatasource.deleteFilesFromRecycleBin(fileIds)
        if (response.code != 1) {
            throw Exception(response.message)
        }
    }
    
    // 清空回收站
    suspend fun emptyRecycleBin() {
        val response = networkDatasource.emptyRecycleBin()
        if (response.code != 1) {
            throw Exception(response.message)
        }
    }
} 