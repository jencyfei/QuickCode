package com.sms.tagger.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sms.tagger.MainActivity
import com.sms.tagger.R
import java.security.MessageDigest

/**
 * 通知与去重辅助
 */
object NotificationHelper {

    private const val CHANNEL_ID = "express_channel"
    private const val CHANNEL_NAME = "快递提醒"
    private const val PREF_NAME = "express_notification_prefs"
    private const val KEY_RECENT_HASHES = "recent_hashes"
    private const val MAX_RECENT = 20
    private const val NOTIFICATION_ID_OFFSET = 200000

    fun showExpressNotification(context: Context, info: ExpressInfo) {
        ensureChannelCreated(context)

        // 仅显示标题，格式如 “菜鸟驿站：TEST-1234”
        val company = if (info.company.isNullOrBlank()) "快递" else info.company
        val title = "$company：${info.pickupCode}"

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            // 不展示正文，保持通知简洁
            .setContentText("")
            .setStyle(null)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pending)

        NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), builder.build())
    }

    /**
     * 跳转到通知设置页（应用级）
     */
    fun openNotificationSettings(context: Context) {
        try {
            val intent = Intent().apply {
                action = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
                } else {
                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                } else {
                    data = android.net.Uri.parse("package:${context.packageName}")
                }
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // 兜底：打开应用详情页
            val fallback = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallback)
        }
    }

    fun isDuplicate(context: Context, sender: String, content: String, timestamp: Long): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getStringSet(KEY_RECENT_HASHES, emptySet())?.toMutableSet() ?: mutableSetOf()
        val hash = hash("$sender|$content|$timestamp")
        return if (saved.contains(hash)) {
            true
        } else {
            if (saved.size >= MAX_RECENT) {
                // 移除最早的一条（简单地移除首个）
                saved.remove(saved.first())
            }
            saved.add(hash)
            prefs.edit().putStringSet(KEY_RECENT_HASHES, saved).apply()
            false
        }
    }

    /**
     * 确保通知渠道已创建（可安全多次调用）
     */
    fun ensureChannelCreated(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val existing = manager.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "快递取件码提醒"
            enableLights(true)
            lightColor = Color.BLUE
            enableVibration(true)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
        }
        manager.createNotificationChannel(channel)
    }

    private fun hash(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(value.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

