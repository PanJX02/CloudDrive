package com.panjx.clouddrive.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {
    companion object {
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val USERNAME = stringPreferencesKey("username")
        private val TOKEN = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val ENDPOINT_KEY = stringPreferencesKey("endpoint")
        
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
} 