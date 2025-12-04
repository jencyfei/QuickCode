package com.sms.tagger.util

import android.content.Context
import android.content.SharedPreferences
import com.sms.tagger.BuildConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 使用量限制管理器
 * 
 * 管理免费版的各种限制：
 * - 每日识别次数限制（Trial 10次/天，免费版 5 次/天）
 * - 历史记录条数限制（3条）
 * - 识别延迟（1秒）
 */
object UsageLimitManager {

    private const val PREF_NAME = "sms_usage_limit_prefs"
    private const val KEY_IDENTIFY_COUNT_PREFIX = "identify_count_"
    private const val KEY_HISTORY_LIMIT_SHOWN = "history_limit_shown"
    private fun isTrialLocked(context: Context): Boolean =
        BuildConfig.IS_TRIAL && TrialManager.isTrialExpired(context)


    // 免费版限制常量
    const val FULL_FREE_DAILY_IDENTIFY_LIMIT = 5  // Full 版（未激活）每日识别限制
    const val TRIAL_DAILY_IDENTIFY_LIMIT = 10     // Trial 版每日识别限制
    const val FREE_HISTORY_LIMIT = 3              // 历史记录条数限制
    const val FREE_IDENTIFY_DELAY_MS = 1000L      // 识别延迟（毫秒）

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /**
     * 获取今日日期键
     */
    private fun getTodayKey(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return KEY_IDENTIFY_COUNT_PREFIX + dateFormat.format(Date())
    }

    /**
     * 获取今日已识别次数
     */
    fun getTodayIdentifyCount(context: Context): Int {
        return prefs(context).getInt(getTodayKey(), 0)
    }

    /**
     * Trial 或免费版的每日识别次数限制
     */
    fun getDailyIdentifyLimit(context: Context): Int =
        if (BuildConfig.IS_TRIAL) TRIAL_DAILY_IDENTIFY_LIMIT else FULL_FREE_DAILY_IDENTIFY_LIMIT

    /**
     * 检查是否达到每日识别限制
     * @return true 表示已达到限制，不能继续识别
     */
    fun isDailyLimitReached(context: Context): Boolean {
        if (isTrialLocked(context)) {
            return true
        }
        // 已激活用户无限制
        if (ActivationManager.isActivated(context)) {
            return false
        }
        return getTodayIdentifyCount(context) >= getDailyIdentifyLimit(context)
    }

    /**
     * 增加今日识别次数
     */
    fun incrementIdentifyCount(context: Context) {
        if (isTrialLocked(context)) {
            return
        }
        // 已激活用户不计数
        if (ActivationManager.isActivated(context)) {
            return
        }
        val key = getTodayKey()
        val current = prefs(context).getInt(key, 0)
        prefs(context).edit().putInt(key, current + 1).apply()
    }

    /**
     * 获取剩余识别次数
     */
    fun getRemainingIdentifyCount(context: Context): Int {
        if (isTrialLocked(context)) {
            return 0
        }
        if (ActivationManager.isActivated(context)) {
            return Int.MAX_VALUE
        }
        val used = getTodayIdentifyCount(context)
        return (getDailyIdentifyLimit(context) - used).coerceAtLeast(0)
    }

    /**
     * 检查是否需要显示历史记录限制提示
     * 只在首次达到限制时显示
     */
    fun shouldShowHistoryLimitHint(context: Context, currentCount: Int): Boolean {
        if (isTrialLocked(context)) {
            return false
        }
        // 已激活用户无限制
        if (ActivationManager.isActivated(context)) {
            return false
        }
        // 未达到限制
        if (currentCount < FREE_HISTORY_LIMIT) {
            return false
        }
        // 检查是否已经显示过
        val shown = prefs(context).getBoolean(KEY_HISTORY_LIMIT_SHOWN, false)
        return !shown
    }

    /**
     * 标记历史记录限制提示已显示
     */
    fun markHistoryLimitHintShown(context: Context) {
        prefs(context).edit().putBoolean(KEY_HISTORY_LIMIT_SHOWN, true).apply()
    }

    /**
     * 重置历史记录限制提示状态（用于测试）
     */
    fun resetHistoryLimitHintShown(context: Context) {
        prefs(context).edit().putBoolean(KEY_HISTORY_LIMIT_SHOWN, false).apply()
    }

    /**
     * 获取识别延迟时间
     * @return 免费版返回延迟毫秒数，专业版返回0
     */
    fun getIdentifyDelayMs(context: Context): Long {
        if (isTrialLocked(context)) {
            return 0L
        }
        return if (ActivationManager.isActivated(context)) {
            0L
        } else {
            FREE_IDENTIFY_DELAY_MS
        }
    }

    /**
     * 限制历史记录列表
     * 免费版只返回最近的 FREE_HISTORY_LIMIT 条
     */
    fun <T> limitHistoryList(context: Context, list: List<T>): List<T> {
        if (isTrialLocked(context)) {
            return emptyList()
        }
        return if (ActivationManager.isActivated(context)) {
            list
        } else {
            list.take(FREE_HISTORY_LIMIT)
        }
    }

    /**
     * 清理过期的计数数据（可选，用于节省存储空间）
     */
    fun cleanupOldData(context: Context) {
        val prefs = prefs(context)
        val today = getTodayKey()
        val allKeys = prefs.all.keys.filter { it.startsWith(KEY_IDENTIFY_COUNT_PREFIX) }
        val editor = prefs.edit()
        allKeys.filter { it != today }.forEach { key ->
            editor.remove(key)
        }
        editor.apply()
    }
}

