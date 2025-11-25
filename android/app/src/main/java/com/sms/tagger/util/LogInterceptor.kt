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

