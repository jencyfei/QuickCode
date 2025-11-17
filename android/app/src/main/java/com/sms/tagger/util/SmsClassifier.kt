package com.sms.tagger.util

import com.sms.tagger.data.model.SmsCreate

/**
 * 短信分类工具类
 * 根据短信内容自动分类为：验证码、银行、快递、营销、通知
 */
object SmsClassifier {
    
    // 验证码关键词
    private val verificationCodeKeywords = listOf(
        "验证码", "code", "otp", "verification", "动态码", "确认码"
    )
    
    // 银行关键词
    private val bankKeywords = listOf(
        "银行", "余额", "交易", "转账", "信用卡", "debit", "credit", 
        "alipay", "wechat pay", "微信支付", "支付宝", "消费", "入账", "出账"
    )
    
    // 快递关键词（包含"取件通知"这类快递相关通知）
    private val expressKeywords = listOf(
        "快递", "包裹", "物流", "签收", "派送", "ems", "sf express", 
        "jd logistics", "取件码", "运单", "菜鸟", "驿站", "取件通知", "待取件"
    )
    
    // 营销关键词
    private val marketingKeywords = listOf(
        "优惠", "促销", "折扣", "特价", "活动", "coupon", "sale", "广告", "推广"
    )
    
    // 通知关键词（排除快递相关的通知）
    private val notificationKeywords = listOf(
        "通知", "提醒", "预约", "更新", "会议", "alert", "notice", "reminder",
        "中国移动", "中国联通", "中国电信", "停车", "积分", "流量", "话费"
    )
    
    /**
     * 对短信进行分类
     * @param message 短信内容
     * @return 分类标签：验证码、银行、快递、营销、通知、未知
     */
    fun classifySms(message: String): String {
        val lowerMessage = message.lowercase().trim()
        
        // 1. 验证码 - 优先级最高
        if (isVerificationCode(lowerMessage)) {
            return "验证码"
        }
        
        // 2. 快递 - 优先级次高（必须在通知之前，避免"取件通知"被误分类）
        if (isExpress(lowerMessage)) {
            return "快递"
        }
        
        // 3. 银行 - 优先级中等
        if (isBank(lowerMessage)) {
            return "银行"
        }
        
        // 4. 营销 - 优先级较低
        if (isMarketing(lowerMessage)) {
            return "营销"
        }
        
        // 5. 通知 - 优先级最低（兜底）
        // 但要排除已经被分类为快递的通知
        if (isNotification(lowerMessage) && !isExpress(lowerMessage)) {
            return "通知"
        }
        
        return "未知"
    }
    
    /**
     * 检查是否为验证码短信
     */
    private fun isVerificationCode(message: String): Boolean {
        // 检查关键词 + 数字组合
        val hasKeyword = verificationCodeKeywords.any { message.contains(it) }
        val hasDigits = Regex("""\d{4,6}""").containsMatchIn(message)
        return hasKeyword && hasDigits
    }
    
    /**
     * 检查是否为快递短信
     */
    private fun isExpress(message: String): Boolean {
        return expressKeywords.any { message.contains(it) }
    }
    
    /**
     * 检查是否为银行短信
     */
    private fun isBank(message: String): Boolean {
        return bankKeywords.any { message.contains(it) }
    }
    
    /**
     * 检查是否为营销短信
     */
    private fun isMarketing(message: String): Boolean {
        return marketingKeywords.any { message.contains(it) }
    }
    
    /**
     * 检查是否为通知短信
     */
    private fun isNotification(message: String): Boolean {
        return notificationKeywords.any { message.contains(it) }
    }
    
    /**
     * 对短信列表进行分类
     */
    fun classifySmsList(smsList: List<SmsCreate>): Map<String, List<SmsCreate>> {
        return smsList.groupBy { classifySms(it.content) }
    }
}
