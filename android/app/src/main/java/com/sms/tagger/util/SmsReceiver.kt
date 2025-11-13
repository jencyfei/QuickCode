package com.sms.tagger.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.sms.tagger.data.model.SmsCreate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }
        
        context ?: return
        
        // 解析短信内容
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isEmpty()) {
            return
        }
        
        // 提取短信信息
        val message = messages[0]
        val sender = message.displayOriginatingAddress ?: "未知"
        val content = messages.joinToString("") { it.messageBody ?: "" }
        val timestamp = message.timestampMillis
        val receivedAt = dateFormat.format(Date(timestamp))
        
        Log.d(TAG, "收到新短信 - 发件人: $sender, 内容: $content")
        
        // 创建短信对象
        val smsCreate = SmsCreate(
            sender = sender,
            content = content,
            receivedAt = receivedAt,
            phoneNumber = sender
        )
        
        // TODO: 在这里调用Repository上传到服务器
        // 示例：
        // CoroutineScope(Dispatchers.IO).launch {
        //     try {
        //         val repository = SmsRepository(context)
        //         repository.uploadSms(smsCreate)
        //         Log.d(TAG, "短信上传成功")
        //     } catch (e: Exception) {
        //         Log.e(TAG, "短信上传失败: ${e.message}")
        //     }
        // }
        
        // 发送本地广播通知UI更新
        val localIntent = Intent("com.sms.tagger.NEW_SMS")
        localIntent.putExtra("sender", sender)
        localIntent.putExtra("content", content)
        context.sendBroadcast(localIntent)
    }
}
