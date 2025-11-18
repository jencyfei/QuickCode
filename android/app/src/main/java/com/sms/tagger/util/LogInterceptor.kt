package com.sms.tagger.util

import android.util.Log
import java.util.logging.Handler
import java.util.logging.LogRecord

/**
 * 日志拦截器
 * 将应用的所有日志同时输出到Logcat和文件
 */
class LogInterceptor(private val logFileWriter: LogFileWriter) : Handler() {
    
    override fun publish(record: LogRecord?) {
        if (record == null) return
        
        val tag = record.loggerName ?: "App"
        val message = record.message ?: ""
        val level = record.level.name
        val throwable = record.thrown
        
        logFileWriter.writeLog(tag, level, message, throwable)
    }
    
    override fun flush() {}
    
    override fun close() {}
}

/**
 * 日志工具类
 * 提供便捷的日志输出方法
 */
object AppLogger {
    private var logFileWriter: LogFileWriter? = null
    
    fun init(logFileWriter: LogFileWriter) {
        this.logFileWriter = logFileWriter
    }
    
    fun d(tag: String, message: String) {
        Log.d(tag, message)
        logFileWriter?.writeLog(tag, "DEBUG", message)
    }
    
    fun i(tag: String, message: String) {
        Log.i(tag, message)
        logFileWriter?.writeLog(tag, "INFO", message)
    }
    
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        Log.w(tag, message, throwable)
        logFileWriter?.writeLog(tag, "WARN", message, throwable)
    }
    
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
        logFileWriter?.writeLog(tag, "ERROR", message, throwable)
    }
}
