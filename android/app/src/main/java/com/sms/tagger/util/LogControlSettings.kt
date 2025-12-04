package com.sms.tagger.util

import android.content.Context
import androidx.core.content.edit

/**
 * 诊断日志开关配置
 */
object LogControlSettings {
    private const val PREF_NAME = "log_control_settings"
    private const val KEY_VERBOSE = "verbose_logging_enabled"

    fun isVerboseLoggingEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_VERBOSE, false)
    }

    fun setVerboseLoggingEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_VERBOSE, enabled)
        }
        AppLogger.setVerboseOverride(enabled)
    }
}


