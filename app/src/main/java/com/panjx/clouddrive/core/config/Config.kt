package com.panjx.clouddrive.core.config

import com.panjx.clouddrive.data.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

// 配置文件
object Config {
    // 后端地址
    private var _endpoint: String? = null
    
    fun getEndpoint(userPreferences: UserPreferences? = null): String {
        if (_endpoint != null) {
            return _endpoint!!
        }
        
        return if (userPreferences != null) {
            runBlocking {
                _endpoint = userPreferences.endpoint.first()
                _endpoint!!
            }
        } else {
            // 默认后端地址，当无法获取用户偏好设置时使用
            UserPreferences.DEFAULT_ENDPOINT
        }
    }
    
    fun updateEndpoint(endpoint: String) {
        _endpoint = endpoint
    }
}