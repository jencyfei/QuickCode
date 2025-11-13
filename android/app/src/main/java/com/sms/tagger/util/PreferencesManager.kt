package com.sms.tagger.util

import android.content.Context
import android.content.SharedPreferences

/**
 * SharedPreferences管理类
 * 用于存储用户Token、设置等
 */
class PreferencesManager(context: Context) {
    
    companion object {
        private const val PREF_NAME = "sms_tagger_prefs"
        
        // Keys
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_TOKEN_TYPE = "token_type"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_LAST_SYNC_TIME = "last_sync_time"
        private const val KEY_AUTO_SYNC_ENABLED = "auto_sync_enabled"
        private const val KEY_THEME_COLOR = "theme_color"
    }
    
    private val prefs: SharedPreferences = 
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    // ==================== Token管理 ====================
    
    /**
     * 保存访问Token
     */
    fun saveAccessToken(token: String, tokenType: String = "Bearer") {
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, token)
            putString(KEY_TOKEN_TYPE, tokenType)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }
    
    /**
     * 获取访问Token
     */
    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }
    
    /**
     * 获取完整的Authorization头
     */
    fun getAuthorizationHeader(): String? {
        val token = getAccessToken() ?: return null
        val tokenType = prefs.getString(KEY_TOKEN_TYPE, "Bearer")
        return "$tokenType $token"
    }
    
    /**
     * 清除Token
     */
    fun clearToken() {
        prefs.edit().apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_TOKEN_TYPE)
            putBoolean(KEY_IS_LOGGED_IN, false)
            apply()
        }
    }
    
    // ==================== 用户信息 ====================
    
    /**
     * 保存用户信息
     */
    fun saveUserInfo(userId: Int, email: String?) {
        prefs.edit().apply {
            putInt(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, email)
            apply()
        }
    }
    
    /**
     * 获取用户ID
     */
    fun getUserId(): Int {
        return prefs.getInt(KEY_USER_ID, -1)
    }
    
    /**
     * 获取用户邮箱
     */
    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }
    
    /**
     * 检查是否已登录
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && 
               getAccessToken() != null
    }
    
    /**
     * 登出（清除所有用户数据）
     */
    fun logout() {
        prefs.edit().apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_TOKEN_TYPE)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_ID)
            putBoolean(KEY_IS_LOGGED_IN, false)
            apply()
        }
    }
    
    // ==================== 同步设置 ====================
    
    /**
     * 保存最后同步时间
     */
    fun saveLastSyncTime(timestamp: Long = System.currentTimeMillis()) {
        prefs.edit().putLong(KEY_LAST_SYNC_TIME, timestamp).apply()
    }
    
    /**
     * 获取最后同步时间
     */
    fun getLastSyncTime(): Long {
        return prefs.getLong(KEY_LAST_SYNC_TIME, 0)
    }
    
    /**
     * 设置自动同步开关
     */
    fun setAutoSyncEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SYNC_ENABLED, enabled).apply()
    }
    
    /**
     * 获取自动同步开关状态
     */
    fun isAutoSyncEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_SYNC_ENABLED, true)
    }
    
    // ==================== 主题设置 ====================
    
    /**
     * 保存主题颜色
     */
    fun saveThemeColor(color: String) {
        prefs.edit().putString(KEY_THEME_COLOR, color).apply()
    }
    
    /**
     * 获取主题颜色
     */
    fun getThemeColor(): String {
        return prefs.getString(KEY_THEME_COLOR, "#FF6B9D") ?: "#FF6B9D"
    }
    
    /**
     * 清除所有数据
     */
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
