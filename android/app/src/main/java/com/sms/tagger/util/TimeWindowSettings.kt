package com.sms.tagger.util

import android.content.Context
import androidx.core.content.edit

/**
 * 统一管理快递/短信的时间窗口配置
 */
object TimeWindowSettings {
    private const val PREF_NAME = "time_window_settings"
    private const val KEY_EXPRESS_DAYS = "express_recent_days"
    private const val KEY_SMS_DAYS = "sms_recent_days"

    private const val DEFAULT_EXPRESS_DAYS = 30L
    private const val DEFAULT_SMS_DAYS = 90L

    private val EXPRESS_OPTIONS = listOf(30L, 60L, 90L)
    private val SMS_OPTIONS = listOf(90L, 120L, 180L)

    fun getExpressDays(context: Context): Long {
        val stored = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_EXPRESS_DAYS, DEFAULT_EXPRESS_DAYS)
        return EXPRESS_OPTIONS.firstOrNull { it == stored } ?: DEFAULT_EXPRESS_DAYS
    }

    fun getSmsDays(context: Context): Long {
        val stored = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_SMS_DAYS, DEFAULT_SMS_DAYS)
        return SMS_OPTIONS.firstOrNull { it == stored } ?: DEFAULT_SMS_DAYS
    }

    fun setExpressDays(context: Context, days: Long) {
        val target = EXPRESS_OPTIONS.firstOrNull { it == days } ?: DEFAULT_EXPRESS_DAYS
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit {
            putLong(KEY_EXPRESS_DAYS, target)
        }
    }

    fun setSmsDays(context: Context, days: Long) {
        val target = SMS_OPTIONS.firstOrNull { it == days } ?: DEFAULT_SMS_DAYS
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit {
            putLong(KEY_SMS_DAYS, target)
        }
    }

    fun expressOptions(): List<Long> = EXPRESS_OPTIONS

    fun smsOptions(): List<Long> = SMS_OPTIONS
}


