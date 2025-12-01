package com.sms.tagger.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.sms.tagger.data.model.SmsCreate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * 短信接收器
 * 监听系统新短信并自动上传到服务器
 * 支持两种模式：
 * 1. SMS_RECEIVED - 普通短信接收（非默认短信应用）
 * 2. SMS_DELIVER - 默认短信应用专用接收
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
                // 普通短信接收（非默认短信应用）
                handleSmsReceived(context, intent)
            }
            Telephony.Sms.Intents.SMS_DELIVER_ACTION -> {
                // 默认短信应用专用接收
                handleSmsDeliver(context, intent)
            }
            else -> {
                AppLogger.d(TAG, "未知的Intent Action: $action")
            }
        }
    }
    
    /**
     * 处理普通短信接收（SMS_RECEIVED）
     */
    private fun handleSmsReceived(context: Context, intent: Intent) {
        AppLogger.d(TAG, "收到普通短信广播（SMS_RECEIVED）")
        processSms(context, intent, isDefaultSmsApp = false)
    }
    
    /**
     * 处理默认短信应用专用接收（SMS_DELIVER）
     */
    private fun handleSmsDeliver(context: Context, intent: Intent) {
        AppLogger.d(TAG, "收到默认短信应用广播（SMS_DELIVER）")
        processSms(context, intent, isDefaultSmsApp = true)
    }
    
    /**
     * 处理短信的公共逻辑
     */
    private fun processSms(context: Context, intent: Intent, isDefaultSmsApp: Boolean) {
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
            
            AppLogger.d(TAG, "收到新短信 - 模式=${if (isDefaultSmsApp) "默认应用" else "普通接收"}, 发件人=$sender, 内容长度=${content.length}")
            AppLogger.d(TAG, "短信内容: ${content.take(200)}")
            
            // 创建短信对象
            val smsCreate = SmsCreate(
                sender = sender,
                content = content,
                receivedAt = receivedAt,
                phoneNumber = sender
            )
            
            // 如果是默认短信应用，执行取件码识别
            if (isDefaultSmsApp) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // 尝试提取快递信息
                        val expressInfo = ExpressExtractor.extractExpressInfo(smsCreate)
                        if (expressInfo != null) {
                            AppLogger.w(TAG, "✅ 识别到快递信息: 公司=${expressInfo.company}, 取件码=${expressInfo.pickupCode}")
                        } else {
                            AppLogger.d(TAG, "未识别到快递信息")
                        }
                    } catch (e: Exception) {
                        AppLogger.e(TAG, "提取快递信息失败: ${e.message}", e)
                    }
                }
            }
            
            // 发送本地广播通知UI更新
            val localIntent = Intent("com.sms.tagger.NEW_SMS")
            localIntent.putExtra("sender", sender)
            localIntent.putExtra("content", content)
            localIntent.putExtra("isDefaultSmsApp", isDefaultSmsApp)
            context.sendBroadcast(localIntent)
            
        } catch (e: Exception) {
            AppLogger.e(TAG, "处理短信异常: ${e.message}", e)
        }
    }
}
