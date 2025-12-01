package com.sms.tagger.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.Telephony

/**
 * 短信写入辅助工具类（Stub实现）
 * 
 * 目的：满足Android系统对默认短信应用的要求
 * 说明：这是最小化实现，仅满足系统检查，不提供UI入口
 */
object SmsWriteHelper {
    
    private const val TAG = "SmsWriteHelper"
    
    /**
     * 写入短信到系统短信数据库（Stub实现）
     * 
     * @param context Context
     * @param address 发件人地址
     * @param body 短信内容
     * @param date 时间戳（毫秒）
     * @param type 短信类型（1=收件箱，2=已发送等）
     * @return 是否成功
     */
    fun writeSms(
        context: Context,
        address: String,
        body: String,
        date: Long = System.currentTimeMillis(),
        type: Int = Telephony.Sms.MESSAGE_TYPE_INBOX
    ): Boolean {
        return try {
            val values = ContentValues().apply {
                put(Telephony.Sms.ADDRESS, address)
                put(Telephony.Sms.BODY, body)
                put(Telephony.Sms.DATE, date)
                put(Telephony.Sms.TYPE, type)
                put(Telephony.Sms.READ, 0) // 未读
                put(Telephony.Sms.SEEN, 0) // 未查看
            }
            
            val uri = context.contentResolver.insert(
                Telephony.Sms.CONTENT_URI,
                values
            )
            
            val success = uri != null
            if (success) {
                AppLogger.d(TAG, "短信写入成功: 发件人=$address")
            } else {
                AppLogger.w(TAG, "短信写入失败: URI为null")
            }
            success
        } catch (e: Exception) {
            AppLogger.e(TAG, "写入短信异常: ${e.message}", e)
            false
        }
    }
    
    /**
     * 删除短信（Stub实现）
     * 
     * @param context Context
     * @param smsId 短信ID
     * @return 是否成功
     */
    fun deleteSms(context: Context, smsId: Long): Boolean {
        return try {
            val uri = Uri.parse("content://sms/$smsId")
            val deleted = context.contentResolver.delete(uri, null, null)
            val success = deleted > 0
            if (success) {
                AppLogger.d(TAG, "短信删除成功: ID=$smsId")
            } else {
                AppLogger.w(TAG, "短信删除失败: ID=$smsId")
            }
            success
        } catch (e: Exception) {
            AppLogger.e(TAG, "删除短信异常: ${e.message}", e)
            false
        }
    }
    
    /**
     * 标记短信为已读（Stub实现）
     * 
     * @param context Context
     * @param smsId 短信ID
     * @return 是否成功
     */
    fun markAsRead(context: Context, smsId: Long): Boolean {
        return try {
            val values = ContentValues().apply {
                put(Telephony.Sms.READ, 1)
                put(Telephony.Sms.SEEN, 1)
            }
            
            val uri = Uri.parse("content://sms/$smsId")
            val updated = context.contentResolver.update(uri, values, null, null)
            val success = updated > 0
            if (success) {
                AppLogger.d(TAG, "短信标记为已读成功: ID=$smsId")
            } else {
                AppLogger.w(TAG, "短信标记为已读失败: ID=$smsId")
            }
            success
        } catch (e: Exception) {
            AppLogger.e(TAG, "标记短信已读异常: ${e.message}", e)
            false
        }
    }
}

