package com.sms.tagger.util

import com.sms.tagger.data.model.SmsCreate
import java.util.regex.Pattern

/**
 * 快递信息提取工具类
 */
object ExpressExtractor {
    
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
        // 【严格过滤】使用评分模型判断是否为快递短信
        val (isExpress, score) = StrictExpressFilter.isExpressSms(sms)
        if (!isExpress) {
            return null
        }
        
        val content = sms.content
        val detection = ExpressCompanyDetector.detect(sms.sender, content)
        val companyName = detection?.displayName ?: "包裹"
        val companyType = detection?.type ?: "default"
        
        // 提取取件码
        val pickupCode = extractPickupCode(content) ?: return null
        
        // 提取地址信息（如果有）
        val location = extractLocation(content)
        
        // 提取日期信息（从接收时间中获取）
        val date = extractDateFromReceivedTime(sms.receivedAt)
        
        // 检测取件状态
        val status = detectPickupStatus(content)
        
        return ExpressInfo(
            company = companyName,
            expressType = companyType,
            pickupCode = pickupCode,
            location = location,
            sender = sms.sender,
            receivedAt = sms.receivedAt,
            fullContent = content,
            status = status,
            date = date
        )
    }
    
    private const val TAG = "ExpressExtractor"
    
    /**
     * 从短信列表中提取所有快递信息
     * 支持从一条短信中提取多个快递（多个取件码）
     */
    fun extractAllExpressInfo(smsList: List<SmsCreate>): List<ExpressInfo> {
        val allExpressInfo = mutableListOf<ExpressInfo>()
        
        AppLogger.d(TAG, "========== 开始提取快递信息 ==========")
        AppLogger.d(TAG, "短信总数: ${smsList.size}")
        
        // 检查是否包含目标短信
        val targetSms = smsList.filter { 
            it.sender.startsWith("10684") && it.content.contains("9-5-5038")
        }
        if (targetSms.isNotEmpty()) {
            AppLogger.w(TAG, "🎯🎯🎯 找到包含9-5-5038的10684发件人短信，共${targetSms.size}条")
            targetSms.forEachIndexed { index, sms ->
                AppLogger.w(TAG, "  目标短信${index + 1}: 发件人=${sms.sender}, 内容=${sms.content}")
            }
        }
        
        for (sms in smsList) {
            // 检查是否是10684发件人
            val is10684Sender = sms.sender.startsWith("10684")
            if (is10684Sender) {
                AppLogger.d(TAG, "📦 处理10684发件人短信: 内容=${sms.content.take(150)}")
            }
            
            // 检查是否包含目标取件码
            val containsTargetCode = sms.content.contains("9-5-5038")
            if (containsTargetCode) {
                AppLogger.w(TAG, "🎯 发现包含9-5-5038的短信！发件人=${sms.sender}, 内容=${sms.content}")
            }
            
            // 【严格过滤】使用评分模型判断是否为快递短信
            val (isExpress, score) = StrictExpressFilter.isExpressSms(sms)
            if (!isExpress) {
                if (is10684Sender || containsTargetCode) {
                    AppLogger.w(TAG, "⚠️ 严格过滤未通过: 评分=$score, 发件人=${sms.sender}, 内容=${sms.content.take(200)}")
                }
                continue
            }
            
            if (is10684Sender || containsTargetCode) {
                AppLogger.d(TAG, "✅ 严格过滤通过: 评分=$score")
            }
            
            // 检查快递公司（用于显示）
            val detection = ExpressCompanyDetector.detect(sms.sender, sms.content)
            val companyName = detection?.displayName ?: "包裹"
            val companyType = detection?.type ?: "default"
            
            if (is10684Sender || containsTargetCode) {
                AppLogger.d(TAG, "✅ 检测到快递公司: $companyName, 类型=$companyType")
            }
            
            // 提取所有取件码
            val pickupCodes = extractAllPickupCodes(sms.content)
            if (pickupCodes.isEmpty()) {
                if (is10684Sender || containsTargetCode) {
                    AppLogger.w(TAG, "❌ 未能提取到取件码！发件人=${sms.sender}, 内容=${sms.content}")
                }
                continue
            }
            
            if (is10684Sender || containsTargetCode || pickupCodes.any { it == "9-5-5038" }) {
                AppLogger.w(TAG, "✅✅✅ 成功提取取件码: ${pickupCodes.joinToString(", ")}")
            }
            
            // 提取地址和日期（对所有取件码都相同）
            val location = extractLocation(sms.content)
            val date = extractDateFromReceivedTime(sms.receivedAt)
            val status = detectPickupStatus(sms.content)
            
            // 为每个取件码创建一个快递信息
            for (pickupCode in pickupCodes) {
                allExpressInfo.add(
                    ExpressInfo(
                        company = companyName,
                        expressType = companyType,
                        pickupCode = pickupCode,
                        location = location,
                        sender = sms.sender,
                        receivedAt = sms.receivedAt,
                        fullContent = sms.content,
                        status = status,
                        date = date
                    )
                )
            }
        }
        
        AppLogger.d(TAG, "✅ 成功提取 ${allExpressInfo.size} 条快递信息")
        if (allExpressInfo.any { it.pickupCode == "9-5-5038" }) {
            AppLogger.w(TAG, "✅✅✅ 最终结果中包含9-5-5038取件码！")
        } else {
            AppLogger.w(TAG, "⚠️⚠️⚠️ 最终结果中未包含9-5-5038取件码！")
        }
        AppLogger.d(TAG, "========== 快递信息提取完成 ==========")
        
        return allExpressInfo
    }
    
    /**
     * 提取所有取件码（支持多个）
     */
    private fun extractAllPickupCodes(content: String): List<String> {
        val codes = mutableListOf<String>()
        
        // 【内置规则】菜鸟驿站：【菜鸟驿站】...凭XXX...取件 → 在"凭"之后取数字和横杠组成的取件码
        if (content.contains("【菜鸟驿站】") || content.contains("[菜鸟驿站]")) {
            val caiNiaoCodes = extractAllCaiNiaoPickupCodes(content)
            if (caiNiaoCodes.isNotEmpty()) {
                return caiNiaoCodes
            }
        }

        // 【新增规则】兔喜生活：支持包含连字符的取件码（如 00-7956）
        if (content.contains("【兔喜生活】") || content.contains("兔喜生活")) {
            val tuXiCodes = extractAllTuXiPickupCodes(content)
            if (tuXiCodes.isNotEmpty()) {
                return tuXiCodes
            }
        }
        
        // 其他规则
        for (pattern in pickupCodePatterns) {
            val matcher = pattern.matcher(content)
            while (matcher.find()) {
                matcher.group(1)?.let { code ->
                    codes.add(code)
                }
            }
        }
        
        return if (codes.isNotEmpty()) codes else listOf()
    }
    
    /**
     * 提取取件码（单个，用于向后兼容）
     */
    private fun extractPickupCode(content: String): String? {
        val codes = extractAllPickupCodes(content)
        return codes.firstOrNull()
    }
    
    /**
     * 【内置规则】菜鸟驿站取件码提取（支持多个）
     * 规则：在"凭"或"取件码为"之后提取数字和横杠组成的取件码
     * 支持多个取件码（逗号或中文逗号分隔）
     * 示例：
     * - 【菜鸟驿站】您有2个包裹在郑州市北文雅小区6号楼102店，取件码为6-5-3002, 6-2-3006。
     * - 【菜鸟驿站】您的包裹已到站，凭1-4-4011到郑州市北文雅小区6号楼102店取件。
     * - 【菜鸟驿站】您的包裹已到站，凭9-5-5038到郑州市北文雅小区6号楼102店取件。
     */
    private fun extractAllCaiNiaoPickupCodes(content: String): List<String> {
        val containsTarget = content.contains("9-5-5038")
        if (containsTarget) {
            AppLogger.w(TAG, "🎯 开始提取菜鸟驿站取件码，内容包含9-5-5038: ${content.take(200)}")
        }
        
        // 查找"凭"或"取件码为"的位置
        var startIndex = -1
        var bengIndex = content.indexOf("凭")
        var codeIndex = content.indexOf("取件码为")
        
        if (containsTarget) {
            AppLogger.d(TAG, "  查找关键词: '凭'在位置$bengIndex, '取件码为'在位置$codeIndex")
        }
        
        // 优先使用"凭"，其次使用"取件码为"
        startIndex = when {
            bengIndex != -1 -> bengIndex + 1
            codeIndex != -1 -> codeIndex + 4
            else -> {
                if (containsTarget) {
                    AppLogger.w(TAG, "❌ 未找到'凭'或'取件码为'关键词！")
                }
                return emptyList()
            }
        }
        
        if (containsTarget) {
            AppLogger.d(TAG, "  使用开始位置: $startIndex")
        }
        
        val restContent = content.substring(startIndex)
        
        if (containsTarget) {
            AppLogger.d(TAG, "  提取后的内容: ${restContent.take(100)}")
        }
        
        // 匹配格式：
        // 1. 标准格式：数字-数字-数字（如：6-5-3002、9-5-5038）
        // 2. 标准格式：数字-数字-数字-数字（如：6-5-3-002）
        // 3. 特殊格式：数字-数字-多位数字（如：1-4-4011，第三部分是2-6位数字）
        // 支持多个取件码（逗号或中文逗号分隔）
        
        // 收集所有匹配的取件码
        val codes = mutableListOf<String>()
        
        // 方案1：优先匹配标准格式（至少3段，每段至少1位数字）
        // 例如：6-5-3002、1-4-4011、6-5-3-002、9-5-5038
        // 格式：数字-数字-数字（第三部分可以是1-8位数字，支持更长的取件码）
        // 使用非贪婪匹配，遇到非数字字符时停止
        val standardPattern = Pattern.compile("([0-9]+-[0-9]+-[0-9]{1,8}(?:-[0-9]+)?)")
        val standardMatcher = standardPattern.matcher(restContent)
        var matchCount = 0
        while (standardMatcher.find()) {
            val code = standardMatcher.group(1)?.trim() ?: ""
            if (code.isNotEmpty()) {
                // 过滤掉明显不是取件码的匹配（如日期格式：2025-11-20）
                // 但保留"1-4-4011"、"9-5-5038"这种格式
                if (!code.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                    codes.add(code)
                    matchCount++
                    if (containsTarget || code == "9-5-5038") {
                        AppLogger.w(TAG, "  ✅ 匹配到取件码: $code (标准格式)")
                    }
                } else {
                    if (containsTarget) {
                        AppLogger.d(TAG, "  ⏭️ 跳过日期格式: $code")
                    }
                }
            }
        }
        
        if (containsTarget) {
            AppLogger.d(TAG, "  标准格式匹配结果: 找到 $matchCount 个，去重后 ${codes.size} 个")
        }
        
        // 方案2：如果标准格式未匹配到，尝试更宽松的格式匹配
        // 匹配：数字-数字-数字的组合（第三部分可以是1-8位数字）
        // 这样可以匹配各种变体格式
        if (codes.isEmpty()) {
            val fallbackPattern = Pattern.compile("([0-9]+-[0-9]+-[0-9]{1,8}(?:-[0-9]+)?)")
            val fallbackMatcher = fallbackPattern.matcher(restContent)
            while (fallbackMatcher.find()) {
                val code = fallbackMatcher.group(1)?.trim() ?: ""
                // 过滤掉明显不是取件码的匹配（如日期格式）
                if (code.isNotEmpty() && !code.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                    codes.add(code)
                }
            }
        }
        
        return if (codes.isNotEmpty()) {
            codes.distinct()  // 去重
        } else {
            // 如果所有格式都未匹配到，尝试提取纯数字（4-8位）
            val pureNumberPattern = Pattern.compile("([0-9]{4,8})")
            val numberMatcher = pureNumberPattern.matcher(restContent)
            val pureNumbers = mutableListOf<String>()
            while (numberMatcher.find()) {
                pureNumbers.add(numberMatcher.group(1)?.trim() ?: "")
            }
            pureNumbers.distinct()
        }
    }

    /**
     * 【新增规则】兔喜生活取件码提取（支持包含连字符的编码）
     * 示例：【兔喜生活】...取件码为00-7956
     */
    private fun extractAllTuXiPickupCodes(content: String): List<String> {
        val codes = mutableListOf<String>()
        
        // 方案1：优先匹配"取件码为"或"取件码:"后面的连字符编码
        // 示例：【兔喜生活】...取件码为00-7956
        val pattern1 = Pattern.compile("取件码[为:：]\\s*([0-9A-Za-z]+-[0-9A-Za-z]+(?:-[0-9A-Za-z]+)*)")
        val matcher1 = pattern1.matcher(content)
        while (matcher1.find()) {
            val code = matcher1.group(1)?.trim()
            if (!code.isNullOrEmpty()) {
                codes.add(code)
            }
        }

        // 方案2：如果方案1未匹配到，尝试匹配"凭"后面的连字符编码
        if (codes.isEmpty()) {
            val pattern2 = Pattern.compile("凭\\s*([0-9A-Za-z]+-[0-9A-Za-z]+(?:-[0-9A-Za-z]+)*)")
            val matcher2 = pattern2.matcher(content)
            while (matcher2.find()) {
                val code = matcher2.group(1)?.trim()
                if (!code.isNullOrEmpty()) {
                    codes.add(code)
                }
            }
        }
        
        // 方案3：兜底 - 如果上述规则未命中，但文本中存在"00-7956"样式的编码，也尝试提取
        if (codes.isEmpty()) {
            val fallbackPattern = Pattern.compile("([0-9A-Za-z]{2,}-[0-9A-Za-z]{2,}(?:-[0-9A-Za-z]{2,})*)")
            val fallbackMatcher = fallbackPattern.matcher(content)
            while (fallbackMatcher.find()) {
                val code = fallbackMatcher.group(1)?.trim()
                // 过滤掉明显不是取件码的匹配（如日期格式）
                if (!code.isNullOrEmpty() && !code.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                    codes.add(code)
                }
            }
        }

        return codes.distinct()
    }
    
    /**
     * 【内置规则】菜鸟驿站取件码提取（单个，用于向后兼容）
     */
    private fun extractCaiNiaoPickupCode(content: String): String {
        val codes = extractAllCaiNiaoPickupCodes(content)
        return codes.firstOrNull() ?: ""
    }
    
    /**
     * 提取地址信息（简化版本，只提取地址部分）
     */
    private fun extractLocation(content: String): String? {
        // 提取地址信息，优先级：到XXX > 在XXX > 其他
        val locationPatterns = listOf(
            // 菜鸟驿站格式：到郑州市北文雅小区6号楼102取件
            Pattern.compile("到([^，。,。]*?(?:小区|楼|店|驿站|超市|便利店|快递柜)[^，。,。]*)"),
            // 菜鸟驿站格式：在郑州市北文雅小区6号楼102店
            Pattern.compile("在([^，。,。]*?(?:小区|楼|店|驿站|超市|便利店|快递柜)[^，。,。]*)"),
            // 其他格式
            Pattern.compile("(菜鸟驿站[^，。,\\s]{0,30})"),
            Pattern.compile("(.*?超市)"),
            Pattern.compile("(.*?便利店)"),
            Pattern.compile("(.*?驿站)"),
            Pattern.compile("(.*?快递柜)")
        )
        
        for (pattern in locationPatterns) {
            val matcher = pattern.matcher(content)
            if (matcher.find()) {
                var location = matcher.group(1)?.trim() ?: continue
                // 移除【菜鸟驿站】等前缀
                location = location.replace("【菜鸟驿站】", "")
                location = location.replace("[菜鸟驿站]", "")
                location = location.replace("菜鸟驿站", "")
                // 移除"您有X个包裹在"等前缀
                location = location.replace(Regex("您有.*?在"), "")
                return location.trim()
            }
        }
        
        return null
    }
    
    /**
     * 从接收时间中提取日期
     * 接收时间格式：2025-11-05T12:42:25
     * 返回格式：2025-11-05
     */
    private fun extractDateFromReceivedTime(receivedAt: String): String {
        // 接收时间格式：2025-11-05T12:42:25
        // 提取前10个字符作为日期：2025-11-05
        return if (receivedAt.length >= 10) {
            receivedAt.substring(0, 10)
        } else {
            receivedAt
        }
    }
    
    /**
     * 提取日期信息
     * 优先级：完整日期 > 菜鸟驿站格式 > 其他格式
     */
    private fun extractDate(content: String): String {
        // 【优先级1】匹配完整日期格式：2025-11-13 或 2025年11月13
        val fullDatePattern = Pattern.compile("(\\d{4})[-年](\\d{1,2})[-月](\\d{1,2})")
        val fullDateMatcher = fullDatePattern.matcher(content)
        if (fullDateMatcher.find()) {
            val year = fullDateMatcher.group(1)
            val month = fullDateMatcher.group(2)
            val day = fullDateMatcher.group(3)
            return "$year-$month-$day"
        }
        
        // 【优先级2】菜鸟驿站特殊处理：查找"凭X-X-XXXX"格式
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
        
        // 【优先级3】查找"货X-X-XXXX"格式（旧格式）
        val oldCaiNiaoPattern = Pattern.compile("货(\\d+)-(\\d+)-(\\d+)")
        val oldCaiNiaoMatcher = oldCaiNiaoPattern.matcher(content)
        if (oldCaiNiaoMatcher.find()) {
            // 格式：货2-4-2029 → 提取为 "2-4-2029"
            val month = oldCaiNiaoMatcher.group(1)
            val day = oldCaiNiaoMatcher.group(2)
            val year = oldCaiNiaoMatcher.group(3)
            return "$year-$month-$day"
        }
        
        // 【优先级4】匹配其他日期格式：12-24、12月24日 等
        val datePatterns = listOf(
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
    val expressType: String,       // 快递类型（用于图标）
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
