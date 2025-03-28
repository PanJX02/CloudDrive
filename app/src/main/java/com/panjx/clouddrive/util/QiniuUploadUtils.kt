package com.panjx.clouddrive.util

import com.qiniu.android.common.FixedZone
import com.qiniu.android.http.ResponseInfo
import com.qiniu.android.storage.Configuration
import com.qiniu.android.storage.FileRecorder
import com.qiniu.android.storage.UpCancellationSignal
import com.qiniu.android.storage.UpCompletionHandler
import com.qiniu.android.storage.UpProgressHandler
import com.qiniu.android.storage.UploadManager
import com.qiniu.android.storage.UploadOptions
import com.qiniu.android.utils.Utils
import org.json.JSONObject
import java.io.File
import java.io.IOException

/**
 * 七牛云文件上传工具类
 */
object QiniuUploadUtils {

    /**
     * 获取默认的上传管理器
     * 
     * @param hosts 上传域名列表
     * @param threshold 分片上传阈值，默认4MB，大于该值采用分片上传，小于该值采用表单上传
     * @param concurrent 是否开启分片并发上传
     * @return UploadManager 上传管理器实例
     */
    fun getUploadManager(
        hosts: Array<String> = emptyArray(),
        threshold: Int = 4 * 1024 * 1024,
        concurrent: Boolean = true
    ): UploadManager {
        // 创建断点续传记录器
        val recorder = try {
            FileRecorder("${Utils.sdkDirectory()}/recorder")
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }

        // 配置上传区域，如果hosts为空数组则使用默认配置
        val zone = if (hosts.isNotEmpty()) FixedZone(hosts) else null
        
        // 构建上传配置
        val configuration = Configuration.Builder()
            .apply {
                if (zone != null) {
                    zone(zone) // 配置上传区域
                }
            }
            .putThreshold(threshold) // 分片上传阈值
            .useConcurrentResumeUpload(concurrent) // 是否开启分片上传
            .recorder(recorder) // 断点续传记录器
            .resumeUploadVersion(Configuration.RESUME_UPLOAD_VERSION_V2) // 使用分片上传V2
            .build()
            
        return UploadManager(configuration)
    }

    /**
     * 上传文件到七牛云
     *
     * @param filePath 文件路径
     * @param key 文件在七牛云上的唯一标识
     * @param token 上传凭证
     * @param onProgress 上传进度回调
     * @param onComplete 上传完成回调
     * @param onCancelled 取消上传判断回调
     */
    fun uploadFile(
        filePath: String,
        key: String,
        token: String,
        onProgress: ((key: String, percent: Double) -> Unit)? = null,
        onComplete: ((key: String, info: ResponseInfo, response: JSONObject?) -> Unit)? = null,
        onCancelled: (() -> Boolean)? = null
    ) {
        // 验证文件是否存在
        val file = File(filePath)
        if (!file.exists() || !file.isFile) {
            // 不能直接创建ResponseInfo，改为返回错误信息
            onComplete?.invoke(key, ResponseInfo.invalidArgument("文件不存在或不是有效文件"), null)
            return
        }

        // 创建上传选项
        val options = UploadOptions(
            null, // 扩展参数，默认为null
            null, // 指定MIME类型，默认为null
            true, // 是否开启上传后检查CRC32校验，默认为false
            object : UpProgressHandler {
                override fun progress(key: String, percent: Double) {
                    onProgress?.invoke(key, percent)
                }
            },
            object : UpCancellationSignal {
                override fun isCancelled(): Boolean {
                    return onCancelled?.invoke() ?: false
                }
            }
        )

        // 获取上传管理器并开始上传
        val uploadManager = getUploadManager()
        
        uploadManager.put(
            filePath, 
            key, 
            token,
            object : UpCompletionHandler {
                override fun complete(key: String, info: ResponseInfo, response: JSONObject?) {
                    onComplete?.invoke(key, info, response)
                }
            },
            options
        )
    }

    /**
     * 上传字节数组到七牛云
     *
     * @param data 要上传的字节数组
     * @param key 文件在七牛云上的唯一标识
     * @param token 上传凭证
     * @param onProgress 上传进度回调
     * @param onComplete 上传完成回调
     * @param onCancelled 取消上传判断回调
     */
    fun uploadData(
        data: ByteArray,
        key: String,
        token: String,
        onProgress: ((key: String, percent: Double) -> Unit)? = null,
        onComplete: ((key: String, info: ResponseInfo, response: JSONObject?) -> Unit)? = null,
        onCancelled: (() -> Boolean)? = null
    ) {
        // 创建上传选项
        val options = UploadOptions(
            null, 
            null, 
            true,
            object : UpProgressHandler {
                override fun progress(key: String, percent: Double) {
                    onProgress?.invoke(key, percent)
                }
            },
            object : UpCancellationSignal {
                override fun isCancelled(): Boolean {
                    return onCancelled?.invoke() ?: false
                }
            }
        )

        // 获取上传管理器并开始上传
        val uploadManager = getUploadManager()
        
        uploadManager.put(
            data, 
            key, 
            token,
            object : UpCompletionHandler {
                override fun complete(key: String, info: ResponseInfo, response: JSONObject?) {
                    onComplete?.invoke(key, info, response)
                }
            },
            options
        )
    }
} 