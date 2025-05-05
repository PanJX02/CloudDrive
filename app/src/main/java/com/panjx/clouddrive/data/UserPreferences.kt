package com.panjx.clouddrive.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.panjx.clouddrive.core.modle.response.UserInfoResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {
    companion object {
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val USERNAME = stringPreferencesKey("username")
        private val TOKEN = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val ENDPOINT_KEY = stringPreferencesKey("endpoint")
        
        // 用户详细信息
        private val USER_INFO = stringPreferencesKey("user_info")
        private val USER_ID = longPreferencesKey("user_id")
        private val NICKNAME = stringPreferencesKey("nickname")
        private val EMAIL = stringPreferencesKey("email")
        private val USED_SPACE = longPreferencesKey("used_space")
        private val TOTAL_SPACE = longPreferencesKey("total_space")
        private val LAST_UPDATE_TIME = longPreferencesKey("last_update_time")
        
        // 默认后端地址
        const val DEFAULT_ENDPOINT = "http://api.1216.ink/"
        
        // 可选后端地址列表，使用友好名称作为键，实际URL作为值
        val ENDPOINT_OPTIONS = mapOf(
            "默认服务器" to "http://api.1216.ink/",
            "备用服务器1" to "http://8.140.30.175:8080/",
        )
        
        // 根据URL获取友好名称
        fun getEndpointName(url: String): String {
            return ENDPOINT_OPTIONS.entries.find { it.value == url }?.key ?: "未知服务器"
        }
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_LOGGED_IN] ?: false
        }

    val username: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[USERNAME] ?: ""
        }

    val token: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[TOKEN] ?: ""
        }
        
    val refreshToken: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[REFRESH_TOKEN] ?: ""
        }
        
    val endpoint: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[ENDPOINT_KEY] ?: DEFAULT_ENDPOINT
        }
        
    // 获取存储的用户信息
    val cachedUserInfo: Flow<UserInfoResponse?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_INFO]?.let {
                try {
                    Json.decodeFromString<UserInfoResponse>(it)
                } catch (e: Exception) {
                    null
                }
            }
        }
        
    // 获取用户ID
    val userId: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[USER_ID]?.toLong() ?: 0L
        }
        
    // 获取昵称
    val nickname: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[NICKNAME] ?: ""
        }
        
    // 获取邮箱
    val email: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[EMAIL] ?: ""
        }
        
    // 获取已使用空间
    val usedSpace: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[USED_SPACE] ?: 0L
        }
        
    // 获取总空间
    val totalSpace: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[TOTAL_SPACE] ?: 0L
        }
        
    // 获取最后更新时间
    val lastUpdateTime: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[LAST_UPDATE_TIME] ?: 0L
        }

    suspend fun setLoggedIn(isLoggedIn: Boolean, username: String = "", accessToken: String = "", refreshToken: String? = null) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = isLoggedIn
            preferences[USERNAME] = username
            preferences[TOKEN] = accessToken
            preferences[REFRESH_TOKEN] = refreshToken ?: ""
        }
    }

    suspend fun clearLoginState() {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = false
            preferences[USERNAME] = ""
            preferences[TOKEN] = ""
            preferences[REFRESH_TOKEN] = ""
        }
    }
    
    suspend fun setEndpoint(endpoint: String) {
        context.dataStore.edit { preferences ->
            preferences[ENDPOINT_KEY] = endpoint
        }
    }
    
    // 保存用户信息
    suspend fun saveUserInfo(userInfo: UserInfoResponse) {
        context.dataStore.edit { preferences ->
            // 保存完整的用户信息对象
            preferences[USER_INFO] = Json.encodeToString(userInfo)
            
            // 同时保存常用字段，方便单独访问
            preferences[USER_ID] = userInfo.userId.toLong()
            preferences[NICKNAME] = userInfo.nickname
            preferences[EMAIL] = userInfo.email ?: ""
            preferences[USED_SPACE] = userInfo.usedSpace
            preferences[TOTAL_SPACE] = userInfo.totalSpace
            preferences[LAST_UPDATE_TIME] = System.currentTimeMillis()
        }
    }
} 