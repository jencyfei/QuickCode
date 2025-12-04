package com.sms.tagger.util

import android.content.Context
import java.util.concurrent.TimeUnit

/**
 * Manages 15-day trial period for the Trial flavor.
 *
 * For Full flavor this manager is effectively unused (guarded by BuildConfig.IS_TRIAL elsewhere).
 */
object TrialManager {

    private const val PREF_NAME = "trial_prefs"
    private const val KEY_START_TIME = "trial_start_time"
    private const val TRIAL_DURATION_DAYS = 15

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /**
     * Records the trial start time if it has not been set yet.
     */
    fun ensureTrialStartTime(context: Context) {
        if (getTrialStartTime(context) == null) {
            val now = System.currentTimeMillis()
            prefs(context).edit().putLong(KEY_START_TIME, now).apply()
        }
    }

    /**
     * Returns the stored trial start timestamp (milliseconds) or null if not set.
     */
    fun getTrialStartTime(context: Context): Long? {
        val value = prefs(context).getLong(KEY_START_TIME, -1L)
        return if (value > 0) value else null
    }

    /**
     * Returns remaining days (0 when expired).
     */
    fun getRemainingDays(context: Context): Int {
        val start = getTrialStartTime(context) ?: return TRIAL_DURATION_DAYS
        val now = System.currentTimeMillis()
        val elapsedMillis = (now - start).coerceAtLeast(0L)
        val elapsedDays = TimeUnit.MILLISECONDS.toDays(elapsedMillis).toInt()
        return (TRIAL_DURATION_DAYS - elapsedDays).coerceAtLeast(0)
    }

    /**
     * Whether the 15-day trial period has expired.
     */
    fun isTrialExpired(context: Context): Boolean {
        return getRemainingDays(context) <= 0
    }

    /**
     * Utility for tests to reset stored start time.
     */
    fun reset(context: Context) {
        prefs(context).edit().remove(KEY_START_TIME).apply()
    }
}


