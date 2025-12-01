package com.sms.tagger.util

import android.content.Context
import android.provider.Telephony

/**
 * 默认短信应用检查工具类
 * 
 * 用于检查当前应用是否为系统默认短信应用
 */
object SmsDefaultAppChecker {
    
    private const val TAG = "SmsDefaultAppChecker"
    
    /**
     * 检查当前应用是否为默认短信应用
     * 
     * @param context Context
     * @return true表示是默认短信应用，false表示不是
     */
    fun isDefaultSmsApp(context: Context): Boolean {
        val defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(context)
        val isDefault = defaultSmsPackage == context.packageName
        
        AppLogger.d(TAG, "当前默认短信应用: $defaultSmsPackage, 本应用包名: ${context.packageName}, 是否匹配: $isDefault")
        
        return isDefault
    }
    
    /**
     * 获取当前默认短信应用的包名
     * 
     * @param context Context
     * @return 默认短信应用的包名，如果没有则返回null
     */
    fun getDefaultSmsPackage(context: Context): String? {
        return Telephony.Sms.getDefaultSmsPackage(context)
    }
}

