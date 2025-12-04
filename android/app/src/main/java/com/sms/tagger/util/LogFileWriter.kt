package com.sms.tagger.util

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.*
import kotlinx.coroutines.*

/**
 * 日志文件写入工具
 * 将应用日志写入到文件中，便于诊断问题
 * 日志文件存储在 /sdcard/Download/sms_agent_logs/ 目录，方便用户直接访问
 * 使用异步写入和批量缓冲，提高性能
 */
class LogFileWriter(private val context: Context) {
    
    companion object {
        private const val TAG = "LogFileWriter"
        private const val LOG_DIR = "sms_agent_logs"
        private const val MAX_LOG_FILES = 10  // 最多保留10个日志文件
        private const val MAX_LOG_SIZE = 5 * 1024 * 1024  // 单个日志文件最大5MB
        private const val BUFFER_SIZE = 50  // 缓冲区大小，达到此数量时批量写入
        private const val FLUSH_INTERVAL_MS = 2000L  // 每2秒自动刷新一次
        
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        private val fileNameFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
    }
    
    // 优先使用下载目录，失败时使用应用私有目录
    private val logDir: File by lazy { 
        getLogDirectory()
    }
    private var currentLogFile: File? = null
    
    // 日志缓冲区（线程安全）
    private val logBuffer = ConcurrentLinkedQueue<String>()
    
    // 后台写入协程
    private val writeScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var flushJob: Job? = null
    
    // 是否初始化成功
    private var isInitialized = false
    
    /**
     * 获取日志目录（优先外部存储，失败时使用私有目录）
     */
    private fun getLogDirectory(): File {
        // 方法1: 尝试使用外部存储的下载目录
        try {
            val externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (externalDir != null) {
                val dir = File(externalDir, LOG_DIR)
                if (dir.exists() || dir.mkdirs()) {
                    Log.d(TAG, "使用外部存储目录: ${dir.absolutePath}")
                    return dir
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "无法使用外部存储目录: ${e.message}")
        }
        
        // 方法2: 使用应用私有目录（更可靠，不需要权限）
        try {
            val privateDir = File(context.filesDir, LOG_DIR)
            if (!privateDir.exists()) {
                privateDir.mkdirs()
            }
            Log.d(TAG, "使用应用私有目录: ${privateDir.absolutePath}")
            return privateDir
        } catch (e: Exception) {
            Log.e(TAG, "无法创建日志目录: ${e.message}", e)
        }
        
        // 最后的备选方案：使用缓存目录
        return try {
            val cacheDir = File(context.cacheDir, LOG_DIR)
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            Log.d(TAG, "使用缓存目录: ${cacheDir.absolutePath}")
            cacheDir
        } catch (e: Exception) {
            Log.e(TAG, "无法使用缓存目录，使用临时目录: ${e.message}", e)
            // 最后的最后：使用临时目录
            File(context.cacheDir, "logs").also { 
                if (!it.exists()) it.mkdirs() 
            }
        }
    }
    
    init {
        try {
            // 创建日志目录（lazy初始化，在这里触发）
            logDir.exists()
            
            // 初始化当前日志文件
            currentLogFile = getOrCreateLogFile()
            
            // 启动后台刷新任务
            startFlushTask()
            
            isInitialized = true
            Log.d(TAG, "日志系统初始化成功")
        } catch (e: Exception) {
            // 初始化失败不应该导致应用崩溃，只是禁用文件日志
            Log.e(TAG, "日志系统初始化失败，将只使用Logcat: ${e.message}", e)
            isInitialized = false
            // 继续运行，只是不写文件
        }
    }
    
    /**
     * 启动后台刷新任务
     */
    private fun startFlushTask() {
        flushJob = writeScope.launch {
            while (isActive) {
                delay(FLUSH_INTERVAL_MS)
                flushBuffer()
            }
        }
    }
    
    /**
     * 刷新缓冲区（将缓冲的日志写入文件）
     */
    private fun flushBuffer() {
        if (!isInitialized || logBuffer.isEmpty()) return
        
        val logsToWrite = mutableListOf<String>()
        // 批量取出缓冲区中的日志
        repeat(BUFFER_SIZE) {
            logBuffer.poll()?.let { logsToWrite.add(it) }
        }
        
        if (logsToWrite.isEmpty()) return
        
        try {
            val logFile = currentLogFile ?: return
            
            // 检查文件大小，如果超过限制则创建新文件
            if (logFile.length() > MAX_LOG_SIZE) {
                currentLogFile = getOrCreateLogFile()
            }
            
            // 批量写入
            logFile.appendText(logsToWrite.joinToString(""))
            
            // 清理旧日志文件（降低频率，只在写入时偶尔清理）
            if (kotlin.random.Random.nextInt(100) < 5) {  // 5%的概率清理
                cleanOldLogFiles()
            }
        } catch (e: Exception) {
            Log.e(TAG, "批量写入日志失败", e)
        }
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
     * 写入日志（异步，非阻塞）
     */
    fun writeLog(tag: String, level: String, message: String, throwable: Throwable? = null) {
        // 如果未初始化成功，直接返回，避免崩溃
        if (!isInitialized) {
            return
        }
        
        try {
            val timestamp = dateFormat.format(Date())
            val logMessage = "[$timestamp] [$level] [$tag] $message\n"
            
            // 添加到缓冲区（非阻塞）
            logBuffer.offer(logMessage)
            
            // 如果有异常，也添加到缓冲区
            if (throwable != null) {
                val stackTrace = throwable.stackTraceToString()
                logBuffer.offer("$stackTrace\n")
            }
            
            // 如果缓冲区满了，立即刷新
            if (logBuffer.size >= BUFFER_SIZE) {
                writeScope.launch {
                    flushBuffer()
                }
            }
        } catch (e: Exception) {
            // 日志写入失败不应该影响应用运行
            Log.e(TAG, "添加日志到缓冲区失败", e)
        }
    }
    
    /**
     * 立即刷新缓冲区（同步）
     */
    fun flush() {
        flushBuffer()
    }
    
    /**
     * 清理旧日志文件
     */
    private fun cleanOldLogFiles() {
        try {
            val files = logDir.listFiles() ?: return
            
            if (files.size > MAX_LOG_FILES) {
                // 按修改时间排序，删除最旧的文件
                val sortedFiles = files.sortedBy { it.lastModified() }
                val filesToDelete = sortedFiles.take(files.size - MAX_LOG_FILES)
                for (file in filesToDelete) {
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
