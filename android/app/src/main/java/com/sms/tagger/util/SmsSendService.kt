package com.sms.tagger.util

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.provider.Telephony
import android.telephony.SmsManager
import android.util.Log

/**
 * 短信发送服务（Stub实现）
 * 
 * 目的：满足Android系统对默认短信应用的要求
 * 说明：这是最小化实现，不提供UI入口，仅满足系统检查
 */
class SmsSendService : Service() {
    
    companion object {
        private const val TAG = "SmsSendService"
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            return START_NOT_STICKY
        }
        
        when (intent.action) {
            Intent.ACTION_SENDTO -> {
                handleSendSms(intent)
            }
        }
        
        return START_NOT_STICKY
    }
    
    /**
     * 处理发送短信请求（Stub实现）
     * 
     * 注意：这是最小化实现，仅满足系统检查
     * 实际应用中，如果需要真正发送短信，需要在此处实现完整逻辑
     */
    private fun handleSendSms(intent: Intent) {
        val uri = intent.data
        if (uri == null) {
            AppLogger.w(TAG, "发送短信Intent缺少URI")
            return
        }
        
        val phoneNumber = uri.schemeSpecificPart
        val message = intent.getStringExtra("sms_body") ?: ""
        
        AppLogger.d(TAG, "收到发送短信请求 - 号码: $phoneNumber, 内容长度: ${message.length}")
        
        // Stub实现：记录日志，不实际发送
        // 如果需要实际发送，取消下面的注释
        /*
        try {
            val smsManager = SmsManager.getDefault()
            val parts = smsManager.divideMessage(message)
            if (parts.size == 1) {
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            } else {
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
            }
            AppLogger.d(TAG, "短信发送成功")
        } catch (e: Exception) {
            AppLogger.e(TAG, "短信发送失败: ${e.message}", e)
        }
        */
    }
}

