package com.sms.tagger.util

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import com.sms.tagger.data.model.SmsCreate
import java.text.SimpleDateFormat
import java.util.*

/**
 * 短信读取工具类
 * 负责从系统读取短信
 */
class SmsReader(private val context: Context) {
    
    companion object {
        private const val TAG = "SmsReader"
        
        // 短信URI
        private val SMS_INBOX_URI: Uri = Uri.parse("content://sms/inbox")
        private val SMS_SENT_URI: Uri = Uri.parse("content://sms/sent")
        
        // 日期格式
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    }
    
    /**
     * 读取所有短信（包括收件箱和已发送）
     * @param limit 限制数量，默认1000条
     * @return 短信列表
     */
    fun readAllSms(limit: Int = 1000): List<SmsCreate> {
        val smsList = mutableListOf<SmsCreate>()
        
        try {
            // 读取所有短信（不限制类型）- 使用通用 SMS URI
            val allSmsUri = Uri.parse("content://sms")
            val cursor = context.contentResolver.query(
                allSmsUri,
                arrayOf(
                    Telephony.Sms._ID,
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE,
                    Telephony.Sms.TYPE
                ),
                null,
                null,
                "${Telephony.Sms.DATE} DESC LIMIT $limit"
            )
            
            cursor?.use {
                val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)
                
                while (it.moveToNext()) {
                    val address = it.getString(addressIndex) ?: "未知"
                    val body = it.getString(bodyIndex) ?: ""
                    val date = it.getLong(dateIndex)
                    
                    // 转换为ISO 8601格式
                    val receivedAt = dateFormat.format(Date(date))
                    
                    smsList.add(
                        SmsCreate(
                            sender = address,
                            content = body,
                            receivedAt = receivedAt,
                            phoneNumber = address
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return smsList
    }
    
    /**
     * 读取指定时间范围的短信
     * @param startTime 开始时间（毫秒）
     * @param endTime 结束时间（毫秒）
     */
    fun readSmsInRange(startTime: Long, endTime: Long): List<SmsCreate> {
        val smsList = mutableListOf<SmsCreate>()
        
        try {
            val selection = "${Telephony.Sms.DATE} >= ? AND ${Telephony.Sms.DATE} <= ?"
            val selectionArgs = arrayOf(startTime.toString(), endTime.toString())
            
            // 读取所有短信（不仅仅是收件箱）
            val allSmsUri = Uri.parse("content://sms")
            val cursor = context.contentResolver.query(
                allSmsUri,
                arrayOf(
                    Telephony.Sms._ID,
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE
                ),
                selection,
                selectionArgs,
                "${Telephony.Sms.DATE} DESC"
            )
            
            cursor?.use {
                val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)
                
                while (it.moveToNext()) {
                    val address = it.getString(addressIndex) ?: "未知"
                    val body = it.getString(bodyIndex) ?: ""
                    val date = it.getLong(dateIndex)
                    
                    val receivedAt = dateFormat.format(Date(date))
                    
                    smsList.add(
                        SmsCreate(
                            sender = address,
                            content = body,
                            receivedAt = receivedAt,
                            phoneNumber = address
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return smsList
    }
    
    /**
     * 读取最新的N条短信
     * @param count 数量
     */
    fun readLatestSms(count: Int = 50): List<SmsCreate> {
        return readAllSms(count)
    }
    
    /**
     * 检查是否有短信权限
     */
    fun hasPermission(): Boolean {
        return try {
            val allSmsUri = Uri.parse("content://sms")
            val cursor = context.contentResolver.query(
                allSmsUri,
                arrayOf(Telephony.Sms._ID),
                null,
                null,
                "${Telephony.Sms.DATE} DESC LIMIT 1"
            )
            cursor?.close()
            true
        } catch (e: Exception) {
            false
        }
    }
}
