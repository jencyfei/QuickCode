package com.sms.tagger.util

import android.util.Log

/**
 * 应用日志工具类
 * 统一管理日志输出，同时输出到 Logcat、日志文件和内存日志管理器
 */
object AppLogger {
    private var logFileWriter: LogFileWriter? = null
    
    /**
     * 初始化日志系统
     */
    fun init(writer: LogFileWriter) {
        logFileWriter = writer
    }
    
    /**
     * 写入 Debug 级别日志
     */
    fun d(tag: String, message: String, throwable: Throwable? = null) {
        Log.d(tag, message, throwable)
        logFileWriter?.writeLog(tag, "DEBUG", message, throwable)
        InMemoryLogger.addLog("DEBUG", tag, message, throwable)
    }
    
    /**
     * 写入 Info 级别日志
     */
    fun i(tag: String, message: String, throwable: Throwable? = null) {
        Log.i(tag, message, throwable)
        logFileWriter?.writeLog(tag, "INFO", message, throwable)
        InMemoryLogger.addLog("INFO", tag, message, throwable)
    }
    
    /**
     * 写入 Warning 级别日志
     */
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        Log.w(tag, message, throwable)
        logFileWriter?.writeLog(tag, "WARN", message, throwable)
        InMemoryLogger.addLog("WARN", tag, message, throwable)
    }
    
    /**
     * 写入 Error 级别日志
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
        logFileWriter?.writeLog(tag, "ERROR", message, throwable)
        InMemoryLogger.addLog("ERROR", tag, message, throwable)
    }
}

