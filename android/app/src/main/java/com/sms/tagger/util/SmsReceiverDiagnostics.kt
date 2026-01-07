package com.sms.tagger.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * SmsReceiver 诊断工具
 * 用于检查短信接收功能是否正常工作
 */
object SmsReceiverDiagnostics {
    
    /**
     * 诊断结果数据类
     */
    data class DiagnosisResult(
        val isReceiverRegistered: Boolean,
        val hasReceiveSmsPermission: Boolean,
        val hasReadSmsPermission: Boolean,
        val hasPostNotificationPermission: Boolean,
        val canTestNotification: Boolean,
        val issues: List<String>,
        val suggestions: List<String>
    )
    
    /**
     * 执行诊断
     */
    fun diagnose(context: Context): DiagnosisResult {
        val issues = mutableListOf<String>()
        val suggestions = mutableListOf<String>()
        
        // 检查权限
        val hasReceiveSmsPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
        
        val hasReadSmsPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
        
        val hasPostNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Android 13 以下不需要此权限
        }
        
        // 检查 BroadcastReceiver 是否注册
        val isReceiverRegistered = checkReceiverRegistered(context)
        
        // 检查通知功能
        val canTestNotification = hasPostNotificationPermission
        
        // 收集问题
        if (!hasReceiveSmsPermission) {
            issues.add("缺少 RECEIVE_SMS 权限")
            suggestions.add("请在设置中授予短信接收权限")
        }
        
        if (!hasReadSmsPermission) {
            issues.add("缺少 READ_SMS 权限")
            suggestions.add("请在设置中授予短信读取权限")
        }
        
        if (!hasPostNotificationPermission) {
            issues.add("缺少 POST_NOTIFICATIONS 权限（Android 13+）")
            suggestions.add("请在设置中授予通知权限")
        }
        
        if (!isReceiverRegistered) {
            issues.add("SmsReceiver 可能未正确注册")
            suggestions.add("请重启应用或重新安装应用")
        }
        
        if (issues.isEmpty()) {
            suggestions.add("所有检查通过，如果仍无通知，可能是系统限制或应用被后台杀死")
            suggestions.add("请检查：1) 应用是否在后台运行 2) 是否设置了自启动 3) 是否关闭了省电模式")
            suggestions.add("某些 ROM（如 MIUI、EMUI）可能需要：1) 将应用设置为默认短信应用 2) 在应用管理中允许后台活动 3) 关闭电池优化")
            suggestions.add("提示：点击「测试短信处理」可以验证短信识别逻辑是否正常工作（即使 SmsReceiver 未被触发）")
        }
        
        return DiagnosisResult(
            isReceiverRegistered = isReceiverRegistered,
            hasReceiveSmsPermission = hasReceiveSmsPermission,
            hasReadSmsPermission = hasReadSmsPermission,
            hasPostNotificationPermission = hasPostNotificationPermission,
            canTestNotification = canTestNotification,
            issues = issues,
            suggestions = suggestions
        )
    }
    
    /**
     * 检查 BroadcastReceiver 是否注册
     */
    private fun checkReceiverRegistered(context: Context): Boolean {
        return try {
            val pm = context.packageManager
            val receiverInfo = pm.getReceiverInfo(
                android.content.ComponentName(context, "com.sms.tagger.util.SmsReceiver"),
                PackageManager.GET_RECEIVERS
            )
            receiverInfo != null && receiverInfo.enabled
        } catch (e: Exception) {
            AppLogger.e("SmsReceiverDiagnostics", "检查 Receiver 注册状态失败: ${e.message}")
            false
        }
    }
    
    /**
     * 测试通知功能
     */
    fun testNotification(context: Context) {
        val testExpressInfo = ExpressInfo(
            company = "测试快递",
            expressType = "default",
            pickupCode = "TEST-1234",
            location = "测试地点",
            sender = "测试发件人",
            receivedAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()),
            fullContent = "这是一条测试通知，用于验证通知功能是否正常工作",
            status = PickupStatus.PENDING,
            date = ""
        )
        NotificationHelper.showExpressNotification(context, testExpressInfo)
        AppLogger.d("SmsReceiverDiagnostics", "已发送测试通知")
    }
    
    /**
     * 手动测试短信处理逻辑
     * 用于验证即使 SmsReceiver 没有被触发，短信处理逻辑是否正常工作
     */
    fun testSmsProcessing(context: Context, testContent: String = "【菜鸟驿站】您的包裹已到站，凭4-1-3006到郑州市北文雅小区6号楼102店取件。") {
        AppLogger.d("SmsReceiverDiagnostics", "========== 开始手动测试短信处理 ==========")
        AppLogger.d("SmsReceiverDiagnostics", "测试短信内容: $testContent")
        
        try {
            val sms = com.sms.tagger.data.model.SmsCreate(
                sender = "10682914222204370",
                content = testContent,
                receivedAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()),
                phoneNumber = null
            )
            
            // 提取快递信息
            val expressInfo = ExpressExtractor.extractExpressInfo(sms)
            if (expressInfo == null) {
                AppLogger.w("SmsReceiverDiagnostics", "❌ 未识别为快递短信")
                return
            }
            
            AppLogger.d("SmsReceiverDiagnostics", "✅ 成功识别为快递短信")
            AppLogger.d("SmsReceiverDiagnostics", "  取件码: ${expressInfo.pickupCode}")
            AppLogger.d("SmsReceiverDiagnostics", "  地点: ${expressInfo.location}")
            AppLogger.d("SmsReceiverDiagnostics", "  公司: ${expressInfo.company}")
            
            // 发送通知
            NotificationHelper.showExpressNotification(context, expressInfo)
            AppLogger.d("SmsReceiverDiagnostics", "✅ 已发送通知")
            AppLogger.d("SmsReceiverDiagnostics", "========== 手动测试完成 ==========")
        } catch (e: Exception) {
            AppLogger.e("SmsReceiverDiagnostics", "测试短信处理失败: ${e.message}", e)
        }
    }
}

