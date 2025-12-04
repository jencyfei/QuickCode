package com.sms.tagger.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import java.text.SimpleDateFormat
import java.util.*

/**
 * 短信接收器
 * 监听系统新短信并自动上传到服务器
 */
class SmsReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "SmsReceiver"
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    }
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            return
        }
        
        val action = intent.action
        when (action) {
            Telephony.Sms.Intents.SMS_RECEIVED_ACTION -> {
                handleSmsReceived(context, intent)
            }
            else -> {
                AppLogger.d(TAG, "未知的Intent Action: $action")
            }
        }
    }
    
    /**
     * 处理短信接收
     */
    private fun handleSmsReceived(context: Context, intent: Intent) {
        AppLogger.d(TAG, "收到短信广播（SMS_RECEIVED）")
        processSms(context, intent)
    }
    
    /**
     * 处理短信的公共逻辑
     */
    private fun processSms(context: Context, intent: Intent) {
        try {
            // 解析短信内容
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (messages.isEmpty()) {
                AppLogger.w(TAG, "短信消息为空")
                return
            }
            
            // 提取短信信息
            val message = messages[0]
            val sender = message.displayOriginatingAddress ?: "未知"
            val content = messages.joinToString("") { it.messageBody ?: "" }
            val timestamp = message.timestampMillis
            val receivedAt = dateFormat.format(Date(timestamp))
            
            AppLogger.d(TAG, "收到新短信 - 发件人=$sender, 内容长度=${content.length}")
            AppLogger.d(TAG, "短信内容: ${content.take(200)}")
            
            // 发送本地广播通知UI更新
            val localIntent = Intent("com.sms.tagger.NEW_SMS")
            localIntent.putExtra("sender", sender)
            localIntent.putExtra("content", content)
            context.sendBroadcast(localIntent)
            
        } catch (e: Exception) {
            AppLogger.e(TAG, "处理短信异常: ${e.message}", e)
        }
    }
}
