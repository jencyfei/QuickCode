package com.sms.tagger.util

import com.sms.tagger.data.model.SmsCreate
import java.util.regex.Pattern

/**
 * 严格快递短信过滤器
 * 根据《快递过滤.md》文档的规则，实现高精度的快递识别
 * 
 * 核心原则：
 * - 发件人信号：必须是品牌词或企业端口
 * - 语义信号：必须同时包含动作词和场景地点
 * - 取件码验证：4-6位数字，前后15字符内有动作词
 * - 数字组数限制：超过2组4-6位数字则排除
 * - 评分模型：总分≥80才判定为快递
 */
object StrictExpressFilter {
    
    private const val TAG = "StrictExpressFilter"
    
    // 快递品牌词
    private val expressBrands = listOf(
        "顺丰", "中通", "圆通", "韵达", "申通", "极兔", "菜鸟", "京东", "邮政", "EMS"
    )
    
    // 快递动作词
    private val actionWords = listOf(
        "取件", "取件码", "凭码", "领取", "提货", "取货", "领取码"
    )
    
    // 场景地点词
    private val locationWords = listOf(
        "快递柜", "驿站", "菜鸟", "丰巢", "代收点", "柜机", "站点"
    )
    
    /**
     * 判断是否为快递短信（严格模式）
     * 返回：Pair<是否为快递, 评分>
     */
    fun isExpressSms(sms: SmsCreate): Pair<Boolean, Int> {
        val sender = sms.sender
        val content = sms.content
        
        var score = 0
        
        // 1. 发件人信号检查
        val senderScore = checkSenderSignal(sender, content)
        if (senderScore == 0) {
            // 发件人信号为0，直接排除
            return Pair(false, 0)
        }
        score += senderScore
        
        // 2. 语义信号检查（核心过滤）
        val semanticScore = checkSemanticSignal(content)
        if (semanticScore == 0) {
            // 语义信号为0（缺少动作词或场景地点），直接排除
            return Pair(false, 0)
        }
        score += semanticScore
        
        // 3. 取件码格式验证
        val pickupCodeScore = checkPickupCodeFormat(content)
        score += pickupCodeScore
        
        // 4. 数字组数限制
        val digitGroupScore = checkDigitGroupCount(content)
        score += digitGroupScore
        
        // 5. 最终判断：总分≥80才判定为快递
        val isExpress = score >= 80
        
        if (isExpress) {
            AppLogger.d(TAG, "✅ 识别为快递短信: 评分=$score, 发件人=$sender, 内容=${content.take(50)}")
        } else {
            AppLogger.d(TAG, "❌ 非快递短信: 评分=$score, 发件人=$sender")
        }
        
        return Pair(isExpress, score)
    }
    
    /**
     * 1. 发件人信号检查
     * 返回值：0（不满足）或 20-40（满足）
     */
    private fun checkSenderSignal(sender: String, content: String): Int {
        var score = 0
        
        // 1.1 品牌识别
        val normalizedSender = sender.lowercase()
        val normalizedContent = content.lowercase()
        
        val hasBrandInSender = expressBrands.any { brand ->
            normalizedSender.contains(brand.lowercase())
        }
        val hasBrandInContent = expressBrands.any { brand ->
            normalizedContent.contains(brand.lowercase())
        }
        
        if (hasBrandInSender || hasBrandInContent) {
            score += 40  // 发件人包含快递品牌名 +40
            AppLogger.d(TAG, "  ✅ 品牌识别: +40")
        }
        
        // 1.2 企业端口号识别
        if (isEnterprisePortNumber(sender)) {
            score += 20  // 发件人属于企业端口 +20
            AppLogger.d(TAG, "  ✅ 企业端口: +20")
        }
        
        // 如果两项都不满足，返回0（直接排除）
        return score
    }
    
    /**
     * 判断是否为的企业端口号
     * 规则：
     * - 10xxxx (6位，如10684)
     * - 1069xxxxxx (10位，如1069558800)
     * - 95xxxx (5位，如95338)
     * - LBxxxx (虚拟运营商)
     */
    private fun isEnterprisePortNumber(sender: String): Boolean {
        // 排除普通手机号（11位数字）
        if (sender.matches(Regex("^1[3-9]\\d{9}$"))) {
            return false
        }
        
        // 企业端口格式
        return when {
            // 10xxxx (6位)
            sender.matches(Regex("^10\\d{4}$")) -> true
            // 1069xxxxxx (10位)
            sender.matches(Regex("^1069\\d{6}$")) -> true
            // 95xxxx (5位)
            sender.matches(Regex("^95\\d{3}$")) -> true
            // 106开头的其他格式（如10684开头的）
            sender.startsWith("106") -> true
            // LB开头的虚拟运营商
            sender.startsWith("LB", ignoreCase = true) -> true
            else -> false
        }
    }
    
    /**
     * 2. 语义信号检查（核心过滤）
     * 必须同时包含：动作词 + 场景地点
     * 返回值：0（不满足）或 30-50（满足）
     */
    private fun checkSemanticSignal(content: String): Int {
        val normalizedContent = content.lowercase()
        
        var score = 0
        
        // 2.1 检查动作词（至少1个）
        val hasActionWord = actionWords.any { word ->
            normalizedContent.contains(word.lowercase())
        }
        
        if (!hasActionWord) {
            // 缺少动作词，直接排除
            return 0
        }
        score += 30  // 包含快递动作词 +30
        AppLogger.d(TAG, "  ✅ 动作词: +30")
        
        // 2.2 检查场景地点（至少1个）
        val hasLocationWord = locationWords.any { word ->
            normalizedContent.contains(word.lowercase())
        }
        
        if (!hasLocationWord) {
            // 缺少场景地点，直接排除
            return 0
        }
        score += 20  // 包含场景地点词 +20
        AppLogger.d(TAG, "  ✅ 场景地点: +20")
        
        return score
    }
    
    /**
     * 3. 取件码格式验证
     * 规则：必须出现取件码格式（4-6位数字或连字符格式），且前后15字符内包含动作词
     * 返回值：0-60
     */
    private fun checkPickupCodeFormat(content: String): Int {
        var score = 0
        
        // 方案1：检查连字符格式的取件码（如菜鸟驿站的 9-5-5038）
        val hyphenPattern = Pattern.compile("([0-9]+-[0-9]+-[0-9]{1,8}(?:-[0-9]+)?)")
        val hyphenMatcher = hyphenPattern.matcher(content)
        
        var foundValidPickupCode = false
        
        while (hyphenMatcher.find()) {
            val code = hyphenMatcher.group(1)
            val startPos = hyphenMatcher.start()
            val endPos = hyphenMatcher.end()
            
            // 过滤日期格式（如 2025-11-20）
            if (code.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                continue
            }
            
            // 检查取件码前后15字符内是否有动作词
            val start = maxOf(0, startPos - 15)
            val end = minOf(content.length, endPos + 15)
            val surroundingText = content.substring(start, end).lowercase()
            
            val hasActionWordNearby = actionWords.any { word ->
                surroundingText.contains(word.lowercase())
            }
            
            if (hasActionWordNearby) {
                foundValidPickupCode = true
                score += 40  // 取件码附近有"取件码/凭码" +40
                score += 20  // 出现取件码格式 +20
                AppLogger.d(TAG, "  ✅ 取件码格式验证: +60 (连字符格式=$code)")
                break  // 找到一个有效的即可
            }
        }
        
        // 方案2：如果没有找到连字符格式，检查4-6位连续数字
        if (!foundValidPickupCode) {
            val digitPattern = Pattern.compile("[0-9]{4,6}")
            val matcher = digitPattern.matcher(content)
            
            while (matcher.find()) {
                val digitGroup = matcher.group()
                val startPos = matcher.start()
                val endPos = matcher.end()
                
                // 检查数字前后15字符内是否有动作词
                val start = maxOf(0, startPos - 15)
                val end = minOf(content.length, endPos + 15)
                val surroundingText = content.substring(start, end).lowercase()
                
                val hasActionWordNearby = actionWords.any { word ->
                    surroundingText.contains(word.lowercase())
                }
                
                if (hasActionWordNearby) {
                    foundValidPickupCode = true
                    score += 40  // 4-6位数字附近有"取件码/凭码" +40
                    score += 20  // 出现4-6位数字 +20
                    AppLogger.d(TAG, "  ✅ 取件码格式验证: +60 (数字=$digitGroup)")
                    break  // 找到一个有效的即可
                }
            }
        }
        
        return score
    }
    
    /**
     * 4. 数字组数限制
     * 规则：如果短信中存在超过2组4-6位数字，排除（通常是营销或银行短信）
     * 注意：连字符格式的取件码（如9-5-5038）内的数字不单独计数
     * 返回值：0 或 -30
     */
    private fun checkDigitGroupCount(content: String): Int {
        // 记录连字符格式取件码的位置范围（这些范围内的数字不计入统计）
        val hyphenPattern = Pattern.compile("[0-9]+-[0-9]+-[0-9]{1,8}(?:-[0-9]+)?")
        val hyphenMatcher = hyphenPattern.matcher(content)
        val excludedRanges = mutableListOf<IntRange>()
        
        while (hyphenMatcher.find()) {
            val code = hyphenMatcher.group()
            // 过滤日期格式
            if (code.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                continue
            }
            excludedRanges.add(hyphenMatcher.start()..hyphenMatcher.end())
        }
        
        // 查找所有4-6位数字
        val digitPattern = Pattern.compile("[0-9]{4,6}")
        val matcher = digitPattern.matcher(content)
        
        var count = 0
        while (matcher.find()) {
            val startPos = matcher.start()
            val endPos = matcher.end()
            
            // 跳过日期格式（YYYY-MM-DD）
            val matched = matcher.group()
            if (matched.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                continue
            }
            
            // 检查是否在连字符格式的取件码范围内
            val isInExcludedRange = excludedRanges.any { range ->
                startPos in range || endPos in range || (startPos < range.first && endPos > range.last)
            }
            
            if (isInExcludedRange) {
                continue  // 跳过连字符格式取件码内的数字
            }
            
            count++
            if (count > 2) {
                // 超过2组，排除
                AppLogger.d(TAG, "  ❌ 数字组数过多: -30 (共${count}组)")
                return -30  // 数字超过2组 -30
            }
        }
        
        return 0
    }
}

