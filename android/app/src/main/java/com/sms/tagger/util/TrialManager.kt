package com.sms.tagger.util

import android.content.Context

/**
 * 开源版：不再限制试用期，恒为未过期。
 */
object TrialManager {
    fun ensureTrialStartTime(context: Context) { /* no-op */ }
    fun getRemainingDays(context: Context): Int = Int.MAX_VALUE
    fun isTrialExpired(context: Context): Boolean = false
}

