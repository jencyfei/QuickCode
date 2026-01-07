package com.sms.tagger.util

import android.content.Context

/**
 * 开源版本：移除激活限制，永远视为已激活。
 */
object ActivationManager {

    data class ActivationInfo(
        val deviceId: String,
        val remaining: Int,
        val activatedAt: Long
    )

    fun getDeviceIdForUser(context: Context): String = DeviceIdManager.getDeviceId(context)

    // 永远已激活
    fun isActivated(context: Context): Boolean = true

    // 无限次数
    fun getRemainingActivations(context: Context): Int = Int.MAX_VALUE

    // 可返回 null，UI 可忽略
    fun getActivationInfo(context: Context): ActivationInfo? = null

    // 激活码验证：直接成功，不存储任何状态
    fun validateActivationCode(context: Context, rawCode: String): Result<String> {
        return Result.success("开源版本，激活已开放")
    }
}
