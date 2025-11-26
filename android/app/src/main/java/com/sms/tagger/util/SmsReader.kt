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
        
        // 分页大小 - 增加到 200 以提高读取效率
        private const val PAGE_SIZE = 200
    }
    
    /**
     * 读取所有短信（包括收件箱和已发送）
     * 使用基于时间戳的迭代查询，避免OFFSET导致的数据丢失
     * @param limit 限制数量，默认50000条
     * @return 短信列表
     */
    fun readAllSms(limit: Int = 50000): List<SmsCreate> {
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
            var lastDate: Long? = null
            var pageNum = 0
            var hasMore = true
            
            // 使用基于时间戳的迭代查询，确保读取所有短信
            // 使用Set记录已读取的短信_ID，避免重复
            val seenIds = mutableSetOf<Long>()
            
            while (hasMore && smsList.size < limit) {
                pageNum++
                val pageLimit = minOf(PAGE_SIZE, limit - smsList.size)
                
                AppLogger.d(TAG, "读取第 $pageNum 页 (limit=$pageLimit, lastDate=${if (lastDate != null) java.util.Date(lastDate) else "null"}, lastId=${lastReadMinId ?: "null"})")
                
                val pageSmsWithId = readSmsPageByDate(lastDate, lastReadMinId, pageLimit)
                AppLogger.d(TAG, "第 $pageNum 页读取到 ${pageSmsWithId.size} 条短信（原始）")
                
                if (pageSmsWithId.isEmpty()) {
                    AppLogger.d(TAG, "已到达短信列表底部（无更多数据）")
                    hasMore = false
                    break
                }
                
                // 去重：使用_ID作为唯一标识，避免重复添加
                val newSmsWithId = pageSmsWithId.filterNot { smsWithId ->
                    seenIds.contains(smsWithId.id)
                }
                
                // 记录新短信的_ID
                newSmsWithId.forEach { seenIds.add(it.id) }
                
                if (newSmsWithId.size < pageSmsWithId.size) {
                    val duplicates = pageSmsWithId.size - newSmsWithId.size
                    AppLogger.w(TAG, "⚠️ 第 $pageNum 页检测到 $duplicates 条重复短信（基于_ID），已过滤")
                }
                
                // 提取SmsCreate并添加到列表
                val newSms = newSmsWithId.map { it.sms }
                smsList.addAll(newSms)
                AppLogger.d(TAG, "第 $pageNum 页去重后添加 ${newSms.size} 条短信，累计 ${smsList.size} 条")
                
                // 更新最后一条短信的时间戳和_ID，用于下一页查询
                // 使用 readSmsPageByDate 保存的 lastReadMinDate 和 lastReadMinId
                lastDate = if (lastReadMinDate != null && lastReadMinDate!! > 0) {
                    lastReadMinDate!!  // 直接使用，不再减1毫秒
                } else {
                    // 如果无法获取，尝试从返回的短信列表中解析
                    newSms.minOfOrNull { sms ->
                        try {
                            dateFormat.parse(sms.receivedAt)?.time ?: Long.MAX_VALUE
                        } catch (e: Exception) {
                            Long.MAX_VALUE
                        }
                    } ?: run {
                        // 最后的备用方案：使用递减策略
                        System.currentTimeMillis() - (pageNum * 86400000L)
                    }
                }
                
                // 如果返回的数据少于 pageLimit，说明已经到底了
                if (pageSmsWithId.size < pageLimit) {
                    AppLogger.d(TAG, "已到达短信列表底部（返回数量少于限制）")
                    hasMore = false
                    break
                }
                
                // 安全限制：最多读取1000页，防止无限循环
                if (pageNum >= 1000) {
                    AppLogger.w(TAG, "⚠️ 已达到最大页数限制（1000页），停止读取")
                    break
                }
            }
            
            AppLogger.d(TAG, "✅ 成功读取 ${smsList.size} 条短信（共 $pageNum 页）")
            
            // 打印前5条短信的详细信息
            smsList.take(5).forEachIndexed { index, sms ->
                AppLogger.d(TAG, "短信 ${index + 1}: 发件人=${sms.sender}, 内容=${sms.content.take(50)}, 时间=${sms.receivedAt}")
            }
            
            // 统计不同发件人的短信数量（用于调试）
            val senderStats = smsList.groupingBy { it.sender }.eachCount()
            val topSenders = senderStats.toList().sortedByDescending { it.second }.take(10)
            AppLogger.d(TAG, "发件人统计（前10名）:")
            topSenders.forEach { (sender, count) ->
                AppLogger.d(TAG, "  - $sender: $count 条")
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
                
                // 搜索运营商短信（用于调试）
                val operatorSms = smsList.filter { 
                    it.content.contains("中国移动", ignoreCase = true) || 
                    it.content.contains("中国联通", ignoreCase = true) ||
                    it.content.contains("中国电信", ignoreCase = true) ||
                    it.sender.contains("10086", ignoreCase = true) ||
                    it.sender.contains("10010", ignoreCase = true) ||
                    it.sender.contains("10000", ignoreCase = true) ||
                    it.sender == "101906" ||
                    it.sender.contains("106875", ignoreCase = true) ||
                    it.content.contains("郑好停", ignoreCase = true)
                }
                AppLogger.d(TAG, "包含运营商/服务相关的短信共 ${operatorSms.size} 条")
                operatorSms.take(10).forEachIndexed { index, sms ->
                    AppLogger.d(TAG, "  运营商/服务短信 ${index + 1}: 发件人=${sms.sender}, 内容=${sms.content.take(80)}, 时间=${sms.receivedAt}")
                }
                
                // 专门检查101906和10687542007747193的短信
                val targetSenderSms = smsList.filter { 
                    it.sender == "101906" || 
                    it.sender.contains("10687542007747193", ignoreCase = true) ||
                    (it.sender.contains("106875", ignoreCase = true) && it.content.contains("郑好停", ignoreCase = true))
                }
                if (targetSenderSms.isNotEmpty()) {
                    AppLogger.w(TAG, "🔍 找到目标发件人的短信 ${targetSenderSms.size} 条:")
                    targetSenderSms.forEachIndexed { index, sms ->
                        AppLogger.w(TAG, "  目标发件人短信 ${index + 1}: 发件人=${sms.sender}, 内容=${sms.content}, 时间=${sms.receivedAt}")
                    }
                } else {
                    AppLogger.w(TAG, "⚠️ 未找到目标发件人的短信（101906 或 10687542007747193）")
                }
            }
            
        } catch (e: Exception) {
            AppLogger.e(TAG, "❌ 读取短信异常: ${e.message}", e)
            e.printStackTrace()
        }
        
        AppLogger.d(TAG, "========== 短信读取完成 ==========")
        return smsList
    }
    
    // 用于保存最后一次读取的最后一条短信信息（用于分页）
    private var lastReadMinDate: Long? = null
    private var lastReadMinId: Long? = null  // 保存最后一条短信的_ID
    
    // 临时数据类，用于在分页时携带_ID信息
    private data class SmsWithId(
        val id: Long,
        val sms: SmsCreate
    )
    
    /**
     * 基于时间戳读取单页短信（避免使用OFFSET导致的查询问题）
     * @param beforeDate 在此时间之前的短信（毫秒时间戳，null表示从最新开始）
     * @param beforeId 当beforeDate相同时，使用此_ID作为辅助条件（null表示不使用）
     * @param limit 每页数量
     * @return 短信列表（包含_ID信息）
     */
    private fun readSmsPageByDate(beforeDate: Long?, beforeId: Long?, limit: Int): List<SmsWithId> {
        val smsList = mutableListOf<SmsWithId>()
        lastReadMinDate = null  // 重置
        lastReadMinId = null    // 重置
        
        try {
            val allSmsUri = Uri.parse("content://sms")
            
            // 构建查询条件：使用组合条件确保分页准确性
            // 如果同时有 beforeDate 和 beforeId，使用组合条件处理相同时间戳的情况
            val selection = when {
                beforeDate != null && beforeId != null -> {
                    // 使用组合条件：(DATE < lastDate) OR (DATE = lastDate AND _ID < lastId)
                    // 这样可以正确处理相同时间戳的短信
                    "(${Telephony.Sms.DATE} < ?) OR (${Telephony.Sms.DATE} = ? AND ${Telephony.Sms._ID} < ?)"
                }
                beforeDate != null -> {
                    // 兼容旧逻辑：如果没有_ID，只使用时间戳
                    "${Telephony.Sms.DATE} < ?"
                }
                else -> null
            }
            
            val selectionArgs = when {
                beforeDate != null && beforeId != null -> {
                    arrayOf(beforeDate.toString(), beforeDate.toString(), beforeId.toString())
                }
                beforeDate != null -> {
                    arrayOf(beforeDate.toString())
                }
                else -> null
            }
            
            // 使用基于时间戳和_ID的查询，避免OFFSET问题
            // 双重排序：先按 DATE DESC，相同时间戳时按 _ID DESC
            val cursor = context.contentResolver.query(
                allSmsUri,
                arrayOf(
                    Telephony.Sms._ID,
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE,
                    Telephony.Sms.TYPE
                ),
                selection,  // 基于时间戳和_ID的筛选条件
                selectionArgs,  // 时间戳和_ID参数
                "${Telephony.Sms.DATE} DESC, ${Telephony.Sms._ID} DESC LIMIT $limit"  // 双重排序
            )
            
            if (cursor == null) {
                AppLogger.e(TAG, "❌ Cursor为null，数据库访问失败 (beforeDate=${if (beforeDate != null) java.util.Date(beforeDate) else "null"})")
                return smsList
            }
            
            cursor.use {
                val idIndex = it.getColumnIndex(Telephony.Sms._ID)  // 获取_ID列索引
                val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)
                val typeIndex = it.getColumnIndex(Telephony.Sms.TYPE)
                
                // 检查列索引是否有效（包括_ID）
                if (idIndex < 0 || addressIndex < 0 || bodyIndex < 0 || dateIndex < 0) {
                    AppLogger.e(TAG, "❌ 列索引无效 - _ID=$idIndex, ADDRESS=$addressIndex, BODY=$bodyIndex, DATE=$dateIndex")
                    return smsList
                }
                
                var rowCount = 0
                var errorCount = 0
                var skippedCount = 0
                var minDate: Long? = null  // 保存最小时间戳（最后一条，因为按DESC排序）
                var minId: Long? = null    // 保存最小_ID（最后一条的_ID）
                
                // 用于记录需要输出的短信详情（前20条和最后20条）
                // 使用滑动窗口记录最后20条，前20条立即输出
                val last20Entries = mutableListOf<String>()  // 记录最后20条的日志内容
                val LOG_COUNT = 20  // 记录前20条和最后20条
                
                while (it.moveToNext()) {
                    rowCount++
                    try {
                        val id = it.getLong(idIndex)
                        val address = it.getString(addressIndex) ?: "未知号码"
                        val body = it.getString(bodyIndex) ?: ""
                        val date = it.getLong(dateIndex)
                        val type = if (typeIndex >= 0) it.getInt(typeIndex) else -1
                        
                        // 保存最后一条短信的时间戳和_ID（用于下一页查询）
                        // 由于是按 DATE DESC, _ID DESC 排序，最后一条是最小的 DATE 和最小的 _ID
                        if (minDate == null || date < minDate || (date == minDate && (minId == null || id < minId))) {
                            minDate = date
                            minId = id
                        }
                        
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
                            AppLogger.w(TAG, "⚠️ 短信内容为空，发件人=$address，时间戳=$date")
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
                        
                        // 构建日志信息
                        val logMsg = "读取到短信[第${rowCount}行]: 类型=$typeName, _ID=$id, 发件人=$address, 内容=${body.take(60)}, 时间=$receivedAt"
                        
                        // 前20条立即输出
                        if (rowCount <= LOG_COUNT) {
                            AppLogger.d(TAG, logMsg)
                        } else {
                            // 后面的记录使用滑动窗口保存最后20条
                            last20Entries.add(logMsg)
                            // 保持窗口大小为LOG_COUNT
                            if (last20Entries.size > LOG_COUNT) {
                                last20Entries.removeAt(0)
                            }
                        }
                        
                        // 检查是否是运营商短信或目标短信（用于调试）
                        val isOperatorSms = body.contains("中国移动", ignoreCase = true) || 
                                           body.contains("中国联通", ignoreCase = true) ||
                                           body.contains("中国电信", ignoreCase = true) ||
                                           address.contains("10086", ignoreCase = true) ||
                                           address.contains("10010", ignoreCase = true) ||
                                           address.contains("10000", ignoreCase = true) ||
                                           address == "101906" ||  // 中国联通短信服务号码
                                           body.contains("郑好停", ignoreCase = true) ||
                                           address.contains("10687542007747193", ignoreCase = true) ||
                                           address.contains("106875", ignoreCase = true)  // 106开头的服务号码
                        
                        if (isOperatorSms) {
                            val operatorLogMsg = "🔍 运营商/服务短信[第${rowCount}行]: _ID=$id, 发件人=$address, 内容=${body.take(100)}, 时间=$receivedAt"
                            if (rowCount <= LOG_COUNT) {
                                AppLogger.d(TAG, operatorLogMsg)
                            } else {
                                // 运营商短信也记录到滑动窗口，但优先级较高
                                // 替换掉最早的一条非运营商短信
                                if (last20Entries.size >= LOG_COUNT) {
                                    last20Entries.removeAt(0)
                                }
                                last20Entries.add(operatorLogMsg)
                            }
                        }
                        
                        // 检查是否是目标短信（用于调试）
                        val isTargetSms = body.contains("1-4-4011") || 
                                         body.contains("凭1-4-4011", ignoreCase = true) ||
                                         (body.contains("菜鸟驿站", ignoreCase = true) && body.contains("4011"))
                        
                        if (isTargetSms) {
                            AppLogger.w(TAG, "🔍 找到目标短信！类型=$typeName, 发件人=$address, 完整内容=$body, 时间=$receivedAt")
                        }
                        
                        // 添加所有短信（包括空内容的短信），携带_ID信息
                        smsList.add(
                            SmsWithId(
                                id = id,
                                sms = SmsCreate(
                                sender = address,
                                content = body,
                                receivedAt = receivedAt,
                                phoneNumber = address
                                )
                            )
                        )
                    } catch (e: Exception) {
                        errorCount++
                        AppLogger.e(TAG, "❌ 读取短信字段失败 (第${rowCount}行) - ${e.message}", e)
                        // 异常情况下跳过这条短信，但继续处理其他短信
                        skippedCount++
                    }
                }
                
                // 输出最后20条短信的详细信息
                if (rowCount > LOG_COUNT && last20Entries.isNotEmpty()) {
                    val last20Start = rowCount - last20Entries.size + 1
                    AppLogger.d(TAG, "========== 最后${last20Entries.size}条短信详情 (第${last20Start}行至第${rowCount}行) ==========")
                    last20Entries.forEach { logMsg ->
                        AppLogger.d(TAG, logMsg)
                    }
                }
                
                // 保存最后一条短信的时间戳和_ID到类成员变量，用于下一页查询
                lastReadMinDate = minDate
                lastReadMinId = minId
                
                if (skippedCount > 0) {
                    AppLogger.w(TAG, "⚠️ 跳过了 $skippedCount 条异常短信")
                }
                
                AppLogger.d(TAG, "共读取 $rowCount 行数据，其中错误 $errorCount 行，最小时间戳=${if (minDate != null) java.util.Date(minDate) else "null"}，最小_ID=${minId ?: "null"}")
                if (errorCount > 0) {
                    AppLogger.w(TAG, "⚠️ 有 $errorCount 条短信读取失败，请检查日志")
                }
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "❌ 读取错误: ${e.message}", e)
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
