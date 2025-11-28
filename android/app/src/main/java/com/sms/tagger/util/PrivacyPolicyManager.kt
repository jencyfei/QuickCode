package com.sms.tagger.util

import android.content.Context
import android.content.SharedPreferences

/**
 * 隐私政策管理器
 * 
 * 管理用户隐私政策同意状态
 */
object PrivacyPolicyManager {

    private const val PREF_NAME = "privacy_policy_prefs"
    private const val KEY_PRIVACY_ACCEPTED = "privacy_accepted"
    private const val KEY_PRIVACY_VERSION = "privacy_version" // 可用于后续政策更新检查

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /**
     * 检查用户是否已同意隐私政策
     */
    fun isPrivacyAccepted(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_PRIVACY_ACCEPTED, false)
    }

    /**
     * 标记用户已同意隐私政策
     */
    fun acceptPrivacy(context: Context) {
        prefs(context).edit()
            .putBoolean(KEY_PRIVACY_ACCEPTED, true)
            .apply()
    }

    /**
     * 重置隐私政策同意状态（用于测试）
     */
    fun resetPrivacyAcceptance(context: Context) {
        prefs(context).edit()
            .putBoolean(KEY_PRIVACY_ACCEPTED, false)
            .apply()
    }
}

