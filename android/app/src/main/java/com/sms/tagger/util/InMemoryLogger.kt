package com.sms.tagger.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * 内存日志管理器
 * 用于在应用内显示调试日志
 */
object InMemoryLogger {
    private const val MAX_LOG_ENTRIES = 2000 // 最多保存2000条日志
    
    data class LogEntry(
        val timestamp: Long,
        val timeString: String,
        val level: String,
        val tag: String,
        val message: String,
        val throwable: Throwable? = null
    )
    
    private val logEntries = mutableListOf<LogEntry>()
    private val listeners = mutableSetOf<(List<LogEntry>) -> Unit>()
    
    private val timeFormatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    
    /**
     * 添加日志条目
     */
    fun addLog(level: String, tag: String, message: String, throwable: Throwable? = null) {
        val now = System.currentTimeMillis()
        val timeString = timeFormatter.format(Date(now))
        
        val entry = LogEntry(
            timestamp = now,
            timeString = timeString,
            level = level,
            tag = tag,
            message = message,
            throwable = throwable
        )
        
        synchronized(logEntries) {
            logEntries.add(entry)
            // 限制日志数量
            if (logEntries.size > MAX_LOG_ENTRIES) {
                logEntries.removeAt(0)
            }
        }
        
        // 通知监听者
        synchronized(listeners) {
            listeners.forEach { it(getAllLogs()) }
        }
    }
    
    /**
     * 获取所有日志
     */
    fun getAllLogs(): List<LogEntry> {
        synchronized(logEntries) {
            return logEntries.toList()
        }
    }
    
    /**
     * 获取过滤后的日志
     */
    fun getFilteredLogs(tagFilter: String? = null, levelFilter: String? = null): List<LogEntry> {
        synchronized(logEntries) {
            return logEntries.filter { entry ->
                (tagFilter == null || entry.tag.contains(tagFilter, ignoreCase = true)) &&
                (levelFilter == null || entry.level.equals(levelFilter, ignoreCase = true))
            }
        }
    }
    
    /**
     * 搜索包含关键词的日志
     */
    fun searchLogs(keyword: String): List<LogEntry> {
        synchronized(logEntries) {
            return logEntries.filter { entry ->
                entry.message.contains(keyword, ignoreCase = true) ||
                entry.tag.contains(keyword, ignoreCase = true)
            }
        }
    }
    
    /**
     * 清空日志
     */
    fun clear() {
        synchronized(logEntries) {
            logEntries.clear()
        }
        synchronized(listeners) {
            listeners.forEach { it(getAllLogs()) }
        }
    }
    
    /**
     * 添加日志监听者（用于实时更新UI）
     */
    fun addListener(listener: (List<LogEntry>) -> Unit) {
        synchronized(listeners) {
            listeners.add(listener)
        }
    }
    
    /**
     * 移除日志监听者
     */
    fun removeListener(listener: (List<LogEntry>) -> Unit) {
        synchronized(listeners) {
            listeners.remove(listener)
        }
    }
    
    /**
     * 获取日志统计信息
     */
    fun getStats(): Map<String, Int> {
        synchronized(logEntries) {
            val stats = mutableMapOf<String, Int>()
            logEntries.forEach { entry ->
                stats[entry.level] = (stats[entry.level] ?: 0) + 1
            }
            return stats
        }
    }
    
    /**
     * 导出日志为文本
     */
    fun exportLogs(): String {
        synchronized(logEntries) {
            val sb = StringBuilder()
            val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
            
            logEntries.forEach { entry ->
                val dateStr = dateFormatter.format(Date(entry.timestamp))
                sb.append("[$dateStr] [${entry.level}] [${entry.tag}] ${entry.message}\n")
                entry.throwable?.let {
                    sb.append(it.stackTraceToString())
                    sb.append("\n")
                }
            }
            
            return sb.toString()
        }
    }
}

