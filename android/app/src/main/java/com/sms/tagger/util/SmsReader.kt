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
        
            AppLogger.d(TAG, "========== 开始读取短信 ==========")
            AppLogger.d(TAG, "限制数量: $limit")
            AppLogger.d(TAG, "当前时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}")
            
            // 检查权限
            if (!hasPermission()) {
                AppLogger.e(TAG, "❌ 权限检查失败: 没有短信读取权限")
                return smsList
            }
            
            AppLogger.d(TAG, "✅ 权限检查通过")
        
        try {
            // 计算需要的页数
            val pageCount = (limit + PAGE_SIZE - 1) / PAGE_SIZE
            AppLogger.d(TAG, "分页信息: 总页数=$pageCount, 每页大小=$PAGE_SIZE")
            
            for (page in 0 until pageCount) {
                val offset = page * PAGE_SIZE
                val pageLimit = minOf(PAGE_SIZE, limit - offset)
                
                AppLogger.d(TAG, "读取第 ${page + 1}/$pageCount 页 (offset=$offset, limit=$pageLimit)")
                
                val pageSms = readSmsPage(offset, pageLimit)
                AppLogger.d(TAG, "第 ${page + 1} 页读取到 ${pageSms.size} 条短信")
                
                smsList.addAll(pageSms)
                
                // 如果返回的数据少于 pageLimit，说明已经到底了
                if (pageSms.size < pageLimit) {
                    AppLogger.d(TAG, "已到达短信列表底部")
                    break
                }
            }
            
            AppLogger.d(TAG, "✅ 成功读取 ${smsList.size} 条短信")
            
            // 打印前5条短信的详细信息
            smsList.take(5).forEachIndexed { index, sms ->
                AppLogger.d(TAG, "短信 ${index + 1}: 发件人=${sms.sender}, 内容=${sms.content.take(50)}, 时间=${sms.receivedAt}")
            }
            
            // 搜索包含"菜鸟驿站"和"1-4-4011"的短信（用于调试）
            val targetSms = smsList.filter { 
                it.content.contains("菜鸟驿站", ignoreCase = true) && 
                (it.content.contains("1-4-4011") || it.content.contains("凭1-4-4011"))
            }
            if (targetSms.isNotEmpty()) {
                AppLogger.w(TAG, "🔍 在读取的短信中找到 ${targetSms.size} 条目标短信:")
                targetSms.forEachIndexed { index, sms ->
                    AppLogger.w(TAG, "  目标短信 ${index + 1}: 发件人=${sms.sender}, 内容=${sms.content.take(80)}, 时间=${sms.receivedAt}")
                }
            } else {
                AppLogger.w(TAG, "⚠️ 在读取的 ${smsList.size} 条短信中未找到目标短信（包含'菜鸟驿站'和'1-4-4011'）")
                // 只统计数量，不列出所有短信（减少日志量）
                val cainiaoSms = smsList.filter { it.content.contains("菜鸟驿站", ignoreCase = true) }
                AppLogger.d(TAG, "包含'菜鸟驿站'的短信共 ${cainiaoSms.size} 条")
                // 只列出前3条作为示例
                cainiaoSms.take(3).forEachIndexed { index, sms ->
                    AppLogger.d(TAG, "  示例 ${index + 1}: 发件人=${sms.sender}, 内容=${sms.content.take(50)}...")
                }
            }
            
        } catch (e: Exception) {
            AppLogger.e(TAG, "❌ 读取短信异常: ${e.message}", e)
            e.printStackTrace()
        }
        
        AppLogger.d(TAG, "========== 短信读取完成 ==========")
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
            AppLogger.d(TAG, "查询URI: $allSmsUri")
            
            // 使用 content://sms URI 读取所有短信（包括收件箱、已发送等）
            // 注意：不添加 TYPE 过滤，以包含所有类型的短信
            // 读取所有类型的短信（收件箱、已发送、草稿等）
            // 不添加 TYPE 过滤，确保读取所有短信
            val cursor = context.contentResolver.query(
                allSmsUri,
                arrayOf(
                    Telephony.Sms._ID,
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE,
                    Telephony.Sms.TYPE
                ),
                null,  // 不使用 selection，读取所有短信（包括所有类型）
                null,  // 不使用 selectionArgs
                "${Telephony.Sms.DATE} DESC LIMIT $limit OFFSET $offset"
            )
            
            if (cursor == null) {
                AppLogger.e(TAG, "❌ 第 $pageNum 页: Cursor为null，数据库访问失败")
                return smsList
            }
            
            AppLogger.d(TAG, "✅ 第 $pageNum 页: 成功获取Cursor")
            
            cursor.use {
                val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)
                val typeIndex = it.getColumnIndex(Telephony.Sms.TYPE)
                
                AppLogger.d(TAG, "第 $pageNum 页: 列索引 - ADDRESS=$addressIndex, BODY=$bodyIndex, DATE=$dateIndex, TYPE=$typeIndex")
                
                // 检查列索引是否有效
                if (addressIndex < 0 || bodyIndex < 0 || dateIndex < 0) {
                    AppLogger.e(TAG, "❌ 第 $pageNum 页: 列索引无效 - ADDRESS=$addressIndex, BODY=$bodyIndex, DATE=$dateIndex")
                    return smsList
                }
                
                var rowCount = 0
                var errorCount = 0
                var skippedCount = 0
                
                while (it.moveToNext()) {
                    rowCount++
                    try {
                        val address = it.getString(addressIndex) ?: "未知号码"
                        val body = it.getString(bodyIndex) ?: ""
                        val date = it.getLong(dateIndex)
                        val type = if (typeIndex >= 0) it.getInt(typeIndex) else -1
                        
                        // 记录短信类型（1=收件箱，2=已发送，3=草稿等）
                        val typeName = when(type) {
                            1 -> "收件箱"
                            2 -> "已发送"
                            3 -> "草稿"
                            4 -> "发件箱"
                            5 -> "失败"
                            6 -> "待发送"
                            else -> "未知($type)"
                        }
                        
                        // 检查短信内容是否为空 - 空内容的短信仍然添加，但记录警告
                        if (body.isEmpty()) {
                            AppLogger.w(TAG, "⚠️ 第 $pageNum 页第 $rowCount 行: 短信内容为空，发件人=$address，时间戳=$date")
                            errorCount++
                            // 即使内容为空也继续处理（可能是多媒体短信或其他类型）
                        }
                    
                        // 转换为ISO 8601格式
                        val receivedAt = try {
                            dateFormat.format(Date(date))
                        } catch (e: Exception) {
                            AppLogger.w(TAG, "❌ 时间戳转换失败: date=$date, 错误=${e.message}")
                            "1970-01-01T00:00:00"
                        }
                        
                        // 只记录前3条短信的详细信息（减少日志量）
                        if (rowCount <= 3) {
                            AppLogger.d(TAG, "第 $pageNum 页第 $rowCount 行: 类型=$typeName, 发件人=$address, 内容=${body.take(60)}, 时间=$receivedAt")
                        }
                        
                        // 检查是否是目标短信（用于调试）- 更宽松的匹配条件
                        // 只在找到目标短信时记录详细日志
                        val isTargetSms = body.contains("1-4-4011") || 
                                         body.contains("凭1-4-4011", ignoreCase = true) ||
                                         (body.contains("菜鸟驿站", ignoreCase = true) && body.contains("4011")) ||
                                         (body.contains("菜鸟驿站", ignoreCase = true) && body.contains("1-4") && body.contains("4011"))
                        
                        if (isTargetSms) {
                            AppLogger.w(TAG, "🔍 找到目标短信！第 $pageNum 页第 $rowCount 行: 类型=$typeName, 发件人=$address, 完整内容=$body, 时间=$receivedAt")
                        }
                        
                        // 添加所有短信（包括空内容的短信）
                        smsList.add(
                            SmsCreate(
                                sender = address,
                                content = body,
                                receivedAt = receivedAt,
                                phoneNumber = address
                            )
                        )
                    } catch (e: Exception) {
                        errorCount++
                        AppLogger.e(TAG, "❌ 第 $pageNum 页第 $rowCount 行: 读取短信字段失败 - ${e.message}", e)
                        // 异常情况下跳过这条短信，但继续处理其他短信
                        skippedCount++
                    }
                }
                
                if (skippedCount > 0) {
                    AppLogger.w(TAG, "⚠️ 第 $pageNum 页: 跳过了 $skippedCount 条异常短信")
                }
                
                AppLogger.d(TAG, "第 $pageNum 页: 共读取 $rowCount 行数据，其中错误 $errorCount 行")
                if (errorCount > 0) {
                    AppLogger.w(TAG, "⚠️ 第 $pageNum 页: 有 $errorCount 条短信读取失败，请检查日志")
                }
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "❌ 第 $pageNum 页读取错误: ${e.message}", e)
            AppLogger.e(TAG, "❌ 错误堆栈: ${e.stackTraceToString()}", e)
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
        AppLogger.d(TAG, "========== 开始权限检查 ==========")
        
        // 1. 检查运行时权限
        val runtimePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
        
        AppLogger.d(TAG, "运行时权限(READ_SMS): ${if (runtimePermission) "✅ 已授予" else "❌ 未授予"}")
        
        if (!runtimePermission) {
            AppLogger.e(TAG, "❌ 运行时权限检查失败")
            return false
        }
        
        // 2. 检查是否能访问 SMS 提供者
        return try {
            val allSmsUri = Uri.parse("content://sms")
            AppLogger.d(TAG, "尝试访问SMS提供者: $allSmsUri")
            
            val cursor = context.contentResolver.query(
                allSmsUri,
                arrayOf(Telephony.Sms._ID),
                null,
                null,
                "${Telephony.Sms.DATE} DESC LIMIT 1"
            )
            
            if (cursor == null) {
                AppLogger.e(TAG, "❌ SMS提供者访问失败: Cursor为null")
                return false
            }
            
            val hasData = cursor.moveToFirst() == true
            cursor.close()
            
            if (hasData) {
                AppLogger.d(TAG, "✅ SMS提供者访问成功，存在短信数据")
            } else {
                AppLogger.w(TAG, "⚠️ SMS提供者访问成功，但无短信数据")
            }
            
            AppLogger.d(TAG, "========== 权限检查完成 ==========")
            hasData
        } catch (e: Exception) {
            AppLogger.e(TAG, "❌ SMS提供者访问异常: ${e.message}", e)
            e.printStackTrace()
            false
        }
    }
}
