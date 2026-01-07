package com.sms.tagger.util

import android.content.Context
import androidx.core.content.edit

/**
 * 短信列表显示设置管理
 */
object SmsListSettings {
    private const val PREF_NAME = "sms_list_settings"
    private const val KEY_SMS_LIST_ENABLED = "sms_list_enabled"
    
    /**
     * 获取短信列表是否启用
     * 默认关闭
     */
    fun isSmsListEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_SMS_LIST_ENABLED, false)
    }
    
    /**
     * 设置短信列表是否启用
     */
    fun setSmsListEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_SMS_LIST_ENABLED, enabled)
        }
    }
}

