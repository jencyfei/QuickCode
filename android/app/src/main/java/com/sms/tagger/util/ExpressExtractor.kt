package com.sms.tagger.util

import com.sms.tagger.data.model.SmsCreate
import java.util.regex.Pattern

/**
 * 快递信息提取工具类
 */
object ExpressExtractor {
    
    // 快递公司关键词
    private val expressCompanies = mapOf(
        "顺丰" to "顺丰速运",
        "SF" to "顺丰速运",
        "中通" to "中通快递",
        "ZTO" to "中通快递",
        "圆通" to "圆通速递",
        "YTO" to "圆通速递",
        "申通" to "申通快递",
        "STO" to "申通快递",
        "韵达" to "韵达快递",
        "YD" to "韵达快递",
        "百世" to "百世快递",
        "BEST" to "百世快递",
        "京东" to "京东物流",
        "JD" to "京东物流",
        "邮政" to "中国邮政",
        "EMS" to "中国邮政",
        "德邦" to "德邦快递",
        "极兔" to "极兔速递",
        "菜鸟" to "菜鸟驿站"
    )
    
    // 取件码正则表达式
    private val pickupCodePatterns = listOf(
        // 菜鸟驿站格式：货2-4-2029 或 货6-5-5011 等
        Pattern.compile("货([0-9]+-[0-9]+-[0-9]+)"),
        // 标准取件码格式
        Pattern.compile("取件码[：:为是]?\\s*([A-Za-z0-9]{4,8})"),
        Pattern.compile("提货码[：:为是]?\\s*([A-Za-z0-9]{4,8})"),
        Pattern.compile("验证码[：:为是]?\\s*([A-Za-z0-9]{4,8})"),
        Pattern.compile("取货码[：:为是]?\\s*([A-Za-z0-9]{4,8})"),
        Pattern.compile("取件\\s*[码号][：:为是]?\\s*([A-Za-z0-9]{4,8})"),
        Pattern.compile("\\[([A-Za-z0-9]{4,8})\\]"),
        Pattern.compile("【([A-Za-z0-9]{4,8})】"),
        // 数字组合（4-8位）
        Pattern.compile("([0-9]{4,8})")
    )
    
    /**
     * 从短信中提取快递信息
     */
    fun extractExpressInfo(sms: SmsCreate): ExpressInfo? {
        val content = sms.content
        
        // 检查是否包含快递关键词
        val company = detectExpressCompany(content) ?: return null
        
        // 提取取件码
        val pickupCode = extractPickupCode(content) ?: return null
        
        // 提取地址信息（如果有）
        val location = extractLocation(content)
        
        // 提取日期信息
        val date = extractDate(content)
        
        // 检测取件状态
        val status = detectPickupStatus(content)
        
        return ExpressInfo(
            company = company,
            pickupCode = pickupCode,
            location = location,
            sender = sms.sender,
            receivedAt = sms.receivedAt,
            fullContent = content,
            status = status,
            date = date
        )
    }
    
    /**
     * 从短信列表中提取所有快递信息
     */
    fun extractAllExpressInfo(smsList: List<SmsCreate>): List<ExpressInfo> {
        return smsList.mapNotNull { extractExpressInfo(it) }
    }
    
    /**
     * 检测快递公司
     */
    private fun detectExpressCompany(content: String): String? {
        for ((keyword, company) in expressCompanies) {
            if (content.contains(keyword, ignoreCase = true)) {
                return company
            }
        }
        return null
    }
    
    /**
     * 提取取件码
     */
    private fun extractPickupCode(content: String): String? {
        // 【内置规则】菜鸟驿站：【菜鸟驿站】...凭XXX...取件 → 在"凭"之后取8个字符
        if (content.contains("【菜鸟驿站】") || content.contains("[菜鸟驿站]")) {
            val caiNiaoCode = extractCaiNiaoPickupCode(content)
            if (caiNiaoCode.isNotEmpty()) {
                return caiNiaoCode
            }
        }
        
        // 其他规则
        for (pattern in pickupCodePatterns) {
            val matcher = pattern.matcher(content)
            if (matcher.find()) {
                return matcher.group(1)
            }
        }
        return null
    }
    
    /**
     * 【内置规则】菜鸟驿站取件码提取
     * 规则：在"凭"之后提取数字和横杠组成的取件码
     * 示例：【菜鸟驿站】您的包裹已到站，凭6-4-1006到郑州市...取件。
     */
    private fun extractCaiNiaoPickupCode(content: String): String {
        // 查找"凭"的位置
        val bengIndex = content.indexOf("凭")
        if (bengIndex == -1) return ""
        
        // 从"凭"之后开始，提取数字和横杠组成的取件码
        val startIndex = bengIndex + 1
        val restContent = content.substring(startIndex)
        
        // 匹配格式：数字-数字-数字 或 数字-数字-数字-数字 等
        val codePattern = Pattern.compile("^\\s*([0-9]+-[0-9]+-[0-9]+(?:-[0-9]+)?)")
        val matcher = codePattern.matcher(restContent)
        
        return if (matcher.find()) {
            matcher.group(1)?.trim() ?: ""
        } else {
            // 如果没有找到X-X-XXXX格式，尝试提取纯数字（4-8位）
            val pureNumberPattern = Pattern.compile("^\\s*([0-9]{4,8})")
            val numberMatcher = pureNumberPattern.matcher(restContent)
            if (numberMatcher.find()) {
                numberMatcher.group(1)?.trim() ?: ""
            } else {
                ""
            }
        }
    }
    
    /**
     * 提取地址信息
     */
    private fun extractLocation(content: String): String? {
        // 简单提取驿站、超市等关键词
        val locationPatterns = listOf(
            // 菜鸟驿站格式：到郑州市北文雅小区6号楼102取件
            Pattern.compile("到([^，。,。]*?(?:小区|楼|店|驿站|超市|便利店|快递柜)[^，。,。]*)"),
            Pattern.compile("(菜鸟驿站[^，。,\\s]{0,30})"),
            Pattern.compile("(.*?超市)"),
            Pattern.compile("(.*?便利店)"),
            Pattern.compile("(.*?驿站)"),
            Pattern.compile("(.*?快递柜)")
        )
        
        for (pattern in locationPatterns) {
            val matcher = pattern.matcher(content)
            if (matcher.find()) {
                return matcher.group(1)?.trim()
            }
        }
        
        return null
    }
    
    /**
     * 提取日期信息
     */
    private fun extractDate(content: String): String {
        // 【菜鸟驿站特殊处理】先查找"凭X-X-XXXX"格式，提取日期
        // 规则：凭后面的第一个数字是月份，第二个数字是日期
        val caiNiaoPattern = Pattern.compile("凭\\s*([0-9]+)-([0-9]+)-[0-9]+")
        val caiNiaoMatcher = caiNiaoPattern.matcher(content)
        if (caiNiaoMatcher.find()) {
            // 格式：凭6-4-1006 → 提取为 "6-4"（月-日）
            val month = caiNiaoMatcher.group(1)
            val day = caiNiaoMatcher.group(2)
            // 获取当前年份
            val currentYear = java.time.LocalDate.now().year
            return "$currentYear-$month-$day"
        }
        
        // 【备选方案】查找"货X-X-XXXX"格式（旧格式）
        val oldCaiNiaoPattern = Pattern.compile("货(\\d+)-(\\d+)-(\\d+)")
        val oldCaiNiaoMatcher = oldCaiNiaoPattern.matcher(content)
        if (oldCaiNiaoMatcher.find()) {
            // 格式：货2-4-2029 → 提取为 "2-4-2029"
            val month = oldCaiNiaoMatcher.group(1)
            val day = oldCaiNiaoMatcher.group(2)
            val year = oldCaiNiaoMatcher.group(3)
            return "$year-$month-$day"
        }
        
        // 匹配日期格式：12-24、12月24日、2025-11-13 等
        val datePatterns = listOf(
            Pattern.compile("(\\d{4})[-年](\\d{1,2})[-月](\\d{1,2})"), // 2025-11-13 或 2025年11月13（优先匹配完整日期）
            Pattern.compile("(\\d{1,2})[-月](\\d{1,2})"),           // 12-24 或 12月24
            Pattern.compile("(\\d{1,2})日"),                         // 24日
            Pattern.compile("(今天|明天|后天)")                       // 相对日期
        )
        
        for (pattern in datePatterns) {
            val matcher = pattern.matcher(content)
            if (matcher.find()) {
                return matcher.group(0) ?: ""
            }
        }
        
        return ""
    }
    
    /**
     * 检测取件状态
     */
    private fun detectPickupStatus(content: String): PickupStatus {
        return when {
            // 已取状态关键词
            content.contains("已取") || 
            content.contains("已领取") || 
            content.contains("已取件") -> PickupStatus.PICKED
            
            // 已过期关键词
            content.contains("已过期") || 
            content.contains("已失效") || 
            content.contains("已超期") -> PickupStatus.EXPIRED
            
            // 默认为未取
            else -> PickupStatus.PENDING
        }
    }
}

/**
 * 快递信息数据类
 */
data class ExpressInfo(
    val company: String,           // 快递公司
    val pickupCode: String,        // 取件码
    val location: String?,         // 取件地点
    val sender: String,            // 短信发送者
    val receivedAt: String,        // 接收时间
    val fullContent: String,       // 完整短信内容
    val status: PickupStatus = PickupStatus.PENDING,  // 取件状态
    val date: String = ""          // 取件码日期
)

/**
 * 取件状态枚举
 */
enum class PickupStatus {
    PENDING,   // 未取
    PICKED,    // 已取
    EXPIRED    // 已过期
}
