package com.sms.tagger.util

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
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
        
        // 分页大小
        private const val PAGE_SIZE = 100
    }
    
    /**
     * 读取所有短信（包括收件箱和已发送）
     * 使用分页机制避免一次性加载过多数据
     * @param limit 限制数量，默认5000条
     * @return 短信列表
     */
    fun readAllSms(limit: Int = 5000): List<SmsCreate> {
        val smsList = mutableListOf<SmsCreate>()
        
        android.util.Log.d(TAG, "========== 开始读取短信 ==========")
        android.util.Log.d(TAG, "限制数量: $limit")
        
        // 检查权限
        if (!hasPermission()) {
            android.util.Log.e(TAG, "❌ 权限检查失败: 没有短信读取权限")
            return smsList
        }
        
        android.util.Log.d(TAG, "✅ 权限检查通过")
        
        try {
            // 计算需要的页数
            val pageCount = (limit + PAGE_SIZE - 1) / PAGE_SIZE
            android.util.Log.d(TAG, "分页信息: 总页数=$pageCount, 每页大小=$PAGE_SIZE")
            
            for (page in 0 until pageCount) {
                val offset = page * PAGE_SIZE
                val pageLimit = minOf(PAGE_SIZE, limit - offset)
                
                android.util.Log.d(TAG, "读取第 ${page + 1}/$pageCount 页 (offset=$offset, limit=$pageLimit)")
                
                val pageSms = readSmsPage(offset, pageLimit)
                android.util.Log.d(TAG, "第 ${page + 1} 页读取到 ${pageSms.size} 条短信")
                
                smsList.addAll(pageSms)
                
                // 如果返回的数据少于 pageLimit，说明已经到底了
                if (pageSms.size < pageLimit) {
                    android.util.Log.d(TAG, "已到达短信列表底部")
                    break
                }
            }
            
            android.util.Log.d(TAG, "✅ 成功读取 ${smsList.size} 条短信")
            
            // 打印前5条短信的详细信息
            smsList.take(5).forEachIndexed { index, sms ->
                android.util.Log.d(TAG, "短信 ${index + 1}: 发件人=${sms.sender}, 内容=${sms.content.take(50)}, 时间=${sms.receivedAt}")
            }
            
        } catch (e: Exception) {
            android.util.Log.e(TAG, "❌ 读取短信异常: ${e.message}", e)
            e.printStackTrace()
        }
        
        android.util.Log.d(TAG, "========== 短信读取完成 ==========")
        return smsList
    }
    
    /**
     * 读取单页短信
     * @param offset 偏移量
     * @param limit 每页数量
     */
    private fun readSmsPage(offset: Int, limit: Int): List<SmsCreate> {
        val smsList = mutableListOf<SmsCreate>()
        val pageNum = offset / PAGE_SIZE
        
        try {
            val allSmsUri = Uri.parse("content://sms")
            android.util.Log.d(TAG, "查询URI: $allSmsUri")
            
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
                "${Telephony.Sms.DATE} DESC LIMIT $limit OFFSET $offset"
            )
            
            if (cursor == null) {
                android.util.Log.e(TAG, "❌ 第 $pageNum 页: Cursor为null，数据库访问失败")
                return smsList
            }
            
            android.util.Log.d(TAG, "✅ 第 $pageNum 页: 成功获取Cursor")
            
            cursor.use {
                val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)
                
                android.util.Log.d(TAG, "第 $pageNum 页: 列索引 - ADDRESS=$addressIndex, BODY=$bodyIndex, DATE=$dateIndex")
                
                var rowCount = 0
                while (it.moveToNext()) {
                    rowCount++
                    val address = it.getString(addressIndex) ?: "未知号码"
                    val body = it.getString(bodyIndex) ?: ""
                    val date = it.getLong(dateIndex)
                    
                    // 转换为ISO 8601格式
                    val receivedAt = try {
                        dateFormat.format(Date(date))
                    } catch (e: Exception) {
                        android.util.Log.w(TAG, "❌ 时间戳转换失败: date=$date, 错误=${e.message}")
                        "1970-01-01T00:00:00"
                    }
                    
                    // 只打印前3条短信的详细信息
                    if (rowCount <= 3) {
                        android.util.Log.d(TAG, "第 $pageNum 页第 $rowCount 行: 发件人=$address, 内容=${body.take(40)}, 时间戳=$date, 转换后=$receivedAt")
                    }
                    
                    smsList.add(
                        SmsCreate(
                            sender = address,
                            content = body,
                            receivedAt = receivedAt,
                            phoneNumber = address
                        )
                    )
                }
                
                android.util.Log.d(TAG, "第 $pageNum 页: 共读取 $rowCount 行数据")
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "❌ 第 $pageNum 页读取错误: ${e.message}", e)
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
        android.util.Log.d(TAG, "========== 开始权限检查 ==========")
        
        // 1. 检查运行时权限
        val runtimePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
        
        android.util.Log.d(TAG, "运行时权限(READ_SMS): ${if (runtimePermission) "✅ 已授予" else "❌ 未授予"}")
        
        if (!runtimePermission) {
            android.util.Log.e(TAG, "❌ 运行时权限检查失败")
            return false
        }
        
        // 2. 检查是否能访问 SMS 提供者
        return try {
            val allSmsUri = Uri.parse("content://sms")
            android.util.Log.d(TAG, "尝试访问SMS提供者: $allSmsUri")
            
            val cursor = context.contentResolver.query(
                allSmsUri,
                arrayOf(Telephony.Sms._ID),
                null,
                null,
                "${Telephony.Sms.DATE} DESC LIMIT 1"
            )
            
            if (cursor == null) {
                android.util.Log.e(TAG, "❌ SMS提供者访问失败: Cursor为null")
                return false
            }
            
            val hasData = cursor.moveToFirst() == true
            cursor.close()
            
            if (hasData) {
                android.util.Log.d(TAG, "✅ SMS提供者访问成功，存在短信数据")
            } else {
                android.util.Log.w(TAG, "⚠️ SMS提供者访问成功，但无短信数据")
            }
            
            android.util.Log.d(TAG, "========== 权限检查完成 ==========")
            hasData
        } catch (e: Exception) {
            android.util.Log.e(TAG, "❌ SMS提供者访问异常: ${e.message}", e)
            e.printStackTrace()
            false
        }
    }
}
