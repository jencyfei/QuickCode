package com.sms.tagger.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicReference

/**
 * Handles offline activation storage/validation.
 * Note: activation code decoding currently assumes Base64(deviceId:remaining:salt)
 * and can be strengthened once the Python generator is in place.
 */
object ActivationManager {

    private const val PREF_NAME = "sms_activation_prefs"
    private const val KEY_ACTIVATION = "activation_payload"
    private const val HASH_ALGORITHM = "SHA-256"

    private val gson = Gson()
    private val cache = AtomicReference<ActivationData?>()

    data class ActivationData(
        val code: String,
        val deviceId: String,
        val remaining: Int,
        val activatedAt: Long,
        val checksum: String
    )

    data class ActivationInfo(
        val deviceId: String,
        val remaining: Int,
        val activatedAt: Long
    )

    fun getDeviceIdForUser(context: Context): String = DeviceIdManager.getDeviceId(context)

    fun isActivated(context: Context): Boolean {
        val data = readActivationData(context) ?: return false
        val deviceId = getDeviceIdForUser(context)
        if (data.deviceId != deviceId) return false
        if (data.remaining < 0) return false
        val expectedChecksum = checksum(data.code, data.deviceId, data.remaining, data.activatedAt)
        return expectedChecksum == data.checksum
    }

    fun getRemainingActivations(context: Context): Int {
        val data = readActivationData(context) ?: return 0
        return if (data.deviceId == getDeviceIdForUser(context)) data.remaining else 0
    }

    fun getActivationInfo(context: Context): ActivationInfo? {
        val data = readActivationData(context) ?: return null
        val deviceId = getDeviceIdForUser(context)
        if (data.deviceId != deviceId) return null
        val expectedChecksum = checksum(data.code, data.deviceId, data.remaining, data.activatedAt)
        if (expectedChecksum != data.checksum) return null
        return ActivationInfo(
            deviceId = data.deviceId,
            remaining = data.remaining,
            activatedAt = data.activatedAt
        )
    }

    fun validateActivationCode(context: Context, rawCode: String): Result<String> {
        val deviceId = getDeviceIdForUser(context)
        val payloadResult = decodeActivationCode(rawCode)
        val payload = payloadResult.getOrElse { return Result.failure(it) }

        if (!payload.deviceId.equals(deviceId, ignoreCase = true)) {
            return Result.failure(IllegalStateException("设备ID不匹配，请确认输入的激活码。"))
        }

        if (payload.allowedActivations <= 0) {
            return Result.failure(IllegalStateException("激活次数已用尽，请联系开发者购买新的激活码。"))
        }

        val activatedAt = System.currentTimeMillis()
        val updated = ActivationData(
            code = rawCode.trim(),
            deviceId = deviceId,
            remaining = payload.allowedActivations - 1,
            activatedAt = activatedAt,
            checksum = checksum(
                rawCode.trim(),
                deviceId,
                payload.allowedActivations - 1,
                activatedAt
            )
        )

        saveActivationData(context, updated)
        return Result.success("激活成功，剩余可激活次数：${updated.remaining}")
    }

    private fun checksum(code: String, deviceId: String, remaining: Int, activatedAt: Long): String {
        val digest = MessageDigest.getInstance(HASH_ALGORITHM)
        val payload = "$code|$deviceId|$remaining|$activatedAt"
        return digest.digest(payload.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    private data class ActivationPayload(
        val deviceId: String,
        val allowedActivations: Int,
        val salt: String
    )

    private fun decodeActivationCode(rawCode: String): Result<ActivationPayload> = runCatching {
        val normalized = rawCode.trim()
        val decoded = String(Base64.decode(normalized, Base64.DEFAULT))
        val parts = decoded.split(":")
        require(parts.size >= 3) { "激活码格式错误，请重新确认。" }
        val allowed = parts[1].toIntOrNull()
            ?: throw IllegalArgumentException("激活码中激活次数无效。")
        ActivationPayload(
            deviceId = parts[0],
            allowedActivations = allowed,
            salt = parts[2]
        )
    }

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private fun readActivationData(context: Context): ActivationData? {
        cache.get()?.let { return it }
        val json = prefs(context).getString(KEY_ACTIVATION, null) ?: return null
        return try {
            gson.fromJson(json, ActivationData::class.java).also { cache.set(it) }
        } catch (e: JsonSyntaxException) {
            null
        }
    }

    private fun saveActivationData(context: Context, data: ActivationData) {
        cache.set(data)
        prefs(context).edit().putString(KEY_ACTIVATION, gson.toJson(data)).apply()
    }
}

