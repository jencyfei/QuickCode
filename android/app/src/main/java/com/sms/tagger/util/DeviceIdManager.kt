package com.sms.tagger.util

import android.content.Context
import android.provider.Settings
import java.security.MessageDigest

/**
 * Generates a stable device identifier derived from ANDROID_ID and hashed via SHA-256.
 * Keeps everything local to avoid leaking raw identifiers.
 */
object DeviceIdManager {
    private const val HASH_ALGORITHM = "SHA-256"
    private const val FALLBACK_PREFIX = "fallback_device_id_"

    @Volatile
    private var cachedId: String? = null

    fun getDeviceId(context: Context): String {
        cachedId?.let { return it }

        val rawId = runCatching {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }.getOrNull()?.takeUnless { it.isNullOrBlank() }
            ?: "$FALLBACK_PREFIX${System.currentTimeMillis()}"

        val hashedId = hash(rawId)
        cachedId = hashedId
        return hashedId
    }

    private fun hash(value: String): String {
        val digest = MessageDigest.getInstance(HASH_ALGORITHM)
        val bytes = digest.digest(value.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * 生成设备ID的短码显示格式，用于UI展示
     * 格式：XXXX-XXXX-XXXX（取前12个字符，每4个一组用"-"分隔，转换为大写）
     * 例如：F096-72FF-8CC9
     */
    fun getDeviceIdShortCode(context: Context): String {
        val fullId = getDeviceId(context)
        if (fullId.length < 12) return fullId.uppercase()
        
        val shortPart = fullId.take(12).uppercase()
        return "${shortPart.take(4)}-${shortPart.drop(4).take(4)}-${shortPart.drop(8).take(4)}"
    }
}

