package com.sms.tagger.util

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 开源版：不做任何限制，全部返回无限制
 */
object UsageLimitManager {

    private const val PREF_NAME = "sms_usage_limit_prefs"
    private const val KEY_IDENTIFY_COUNT_PREFIX = "identify_count_"

    // 仅作为展示用途，不再限制
    const val FULL_FREE_DAILY_IDENTIFY_LIMIT = Int.MAX_VALUE
    const val TRIAL_DAILY_IDENTIFY_LIMIT = Int.MAX_VALUE
    const val FREE_HISTORY_LIMIT = Int.MAX_VALUE
    const val FREE_IDENTIFY_DELAY_MS = 0L

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private fun getTodayKey(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return KEY_IDENTIFY_COUNT_PREFIX + dateFormat.format(Date())
    }

    fun getTodayIdentifyCount(context: Context): Int = 0

    fun getDailyIdentifyLimit(context: Context): Int = Int.MAX_VALUE

    fun isDailyLimitReached(context: Context): Boolean = false

    fun incrementIdentifyCount(context: Context) {
        // no-op
    }

    fun getRemainingIdentifyCount(context: Context): Int = Int.MAX_VALUE

    fun shouldShowHistoryLimitHint(context: Context, currentCount: Int): Boolean = false

    fun getIdentifyDelayMs(context: Context): Long = 0L

    fun <T> limitHistoryList(context: Context, list: List<T>): List<T> = list

    fun cleanupOldData(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
