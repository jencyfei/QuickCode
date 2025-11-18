package com.sms.tagger.util

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 日志文件写入工具
 * 将应用日志写入到文件中，便于诊断问题
 */
class LogFileWriter(private val context: Context) {
    
    companion object {
        private const val TAG = "LogFileWriter"
        private const val LOG_DIR = "sms_logs"
        private const val MAX_LOG_FILES = 10  // 最多保留10个日志文件
        private const val MAX_LOG_SIZE = 5 * 1024 * 1024  // 单个日志文件最大5MB
        
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        private val fileNameFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
    }
    
    private val logDir: File = File(context.getExternalFilesDir(null), LOG_DIR)
    private var currentLogFile: File? = null
    
    init {
        // 创建日志目录
        if (!logDir.exists()) {
            logDir.mkdirs()
        }
        
        // 初始化当前日志文件
        currentLogFile = getOrCreateLogFile()
    }
    
    /**
     * 获取或创建日志文件
     */
    private fun getOrCreateLogFile(): File {
        val timestamp = fileNameFormat.format(Date())
        val logFile = File(logDir, "sms_agent_$timestamp.log")
        
        if (!logFile.exists()) {
            logFile.createNewFile()
            Log.d(TAG, "创建新日志文件: ${logFile.absolutePath}")
        }
        
        return logFile
    }
    
    /**
     * 写入日志
     */
    fun writeLog(tag: String, level: String, message: String, throwable: Throwable? = null) {
        try {
            val logFile = currentLogFile ?: return
            
            // 检查文件大小，如果超过限制则创建新文件
            if (logFile.length() > MAX_LOG_SIZE) {
                currentLogFile = getOrCreateLogFile()
            }
            
            val timestamp = dateFormat.format(Date())
            val logMessage = "[$timestamp] [$level] [$tag] $message\n"
            
            logFile.appendText(logMessage)
            
            // 如果有异常，也写入异常信息
            if (throwable != null) {
                val stackTrace = throwable.stackTraceToString()
                logFile.appendText("$stackTrace\n")
            }
            
            // 清理旧日志文件
            cleanOldLogFiles()
        } catch (e: Exception) {
            Log.e(TAG, "写入日志文件失败", e)
        }
    }
    
    /**
     * 清理旧日志文件
     */
    private fun cleanOldLogFiles() {
        try {
            val files = logDir.listFiles() ?: return
            
            if (files.size > MAX_LOG_FILES) {
                // 按修改时间排序，删除最旧的文件
                files.sortBy { it.lastModified() }
                    .take(files.size - MAX_LOG_FILES)
                    .forEach { file ->
                        if (file.delete()) {
                            Log.d(TAG, "删除旧日志文件: ${file.name}")
                        }
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "清理旧日志文件失败", e)
        }
    }
    
    /**
     * 获取所有日志文件
     */
    fun getLogFiles(): List<File> {
        return logDir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
    
    /**
     * 获取最新的日志文件内容
     */
    fun getLatestLogContent(): String {
        return try {
            val logFile = currentLogFile ?: return ""
            logFile.readText()
        } catch (e: Exception) {
            "读取日志失败: ${e.message}"
        }
    }
    
    /**
     * 获取指定日志文件的内容
     */
    fun getLogContent(fileName: String): String {
        return try {
            val logFile = File(logDir, fileName)
            if (logFile.exists()) {
                logFile.readText()
            } else {
                "日志文件不存在: $fileName"
            }
        } catch (e: Exception) {
            "读取日志失败: ${e.message}"
        }
    }
    
    /**
     * 清空所有日志
     */
    fun clearAllLogs() {
        try {
            logDir.listFiles()?.forEach { file ->
                if (file.delete()) {
                    Log.d(TAG, "删除日志文件: ${file.name}")
                }
            }
            currentLogFile = getOrCreateLogFile()
        } catch (e: Exception) {
            Log.e(TAG, "清空日志失败", e)
        }
    }
    
    /**
     * 获取日志目录路径
     */
    fun getLogDirPath(): String {
        return logDir.absolutePath
    }
}
