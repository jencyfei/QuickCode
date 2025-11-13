package com.sms.tagger.util

import com.sms.tagger.data.model.*

/**
 * 规则引擎 - 执行自定义打标规则
 */
object RuleEngine {
    
    /**
     * 对短信执行规则
     * @param sender 发信人
     * @param content 短信内容
     * @param rules 规则列表
     * @return 匹配的规则和提取的值
     */
    fun executeRules(
        sender: String,
        content: String,
        rules: List<TagRule>
    ): List<RuleResult> {
        return rules
            .filter { it.isEnabled }
            .sortedByDescending { it.priority }
            .mapNotNull { rule ->
                when (rule.ruleType) {
                    RuleType.SENDER -> executeSenderRule(sender, rule)
                    RuleType.CONTENT -> executeContentRule(content, rule)
                }
            }
    }
    
    /**
     * 执行基于发信人的规则
     */
    private fun executeSenderRule(sender: String, rule: TagRule): RuleResult? {
        val matched = when {
            rule.condition.contains("|") -> {
                // 格式: "条件类型|关键词"
                val parts = rule.condition.split("|")
                if (parts.size >= 2) {
                    val conditionType = parts[0]
                    val keyword = parts[1]
                    when (conditionType) {
                        "contains" -> sender.contains(keyword, ignoreCase = true)
                        "startsWith" -> sender.startsWith(keyword, ignoreCase = true)
                        "endsWith" -> sender.endsWith(keyword, ignoreCase = true)
                        else -> false
                    }
                } else {
                    false
                }
            }
            else -> false
        }
        
        if (!matched) return null
        
        // 提取值
        val extractedValue = extractValue(sender, rule.extractPosition, rule.extractLength)
        
        return RuleResult(
            matched = true,
            extractedValue = extractedValue,
            tagName = rule.tagName
        )
    }
    
    /**
     * 执行基于短信内容的规则
     */
    private fun executeContentRule(content: String, rule: TagRule): RuleResult? {
        // 检查内容是否包含条件关键词
        if (!content.contains(rule.condition, ignoreCase = true)) {
            return null
        }
        
        // 提取值
        val extractedValue = extractValue(content, rule.extractPosition, rule.extractLength)
        
        return RuleResult(
            matched = true,
            extractedValue = extractedValue,
            tagName = rule.tagName
        )
    }
    
    /**
     * 从文本中提取值
     * @param text 源文本
     * @param position 提取位置（关键词）
     * @param length 提取长度
     */
    private fun extractValue(text: String, position: String, length: Int): String {
        if (position.isEmpty() || length <= 0) return ""
        
        val index = text.indexOf(position, ignoreCase = true)
        if (index == -1) return ""
        
        // 从关键词之后开始提取
        val startIndex = index + position.length
        val endIndex = minOf(startIndex + length, text.length)
        
        return if (startIndex < text.length) {
            text.substring(startIndex, endIndex).trim()
        } else {
            ""
        }
    }
    
    /**
     * 验证规则是否有效
     */
    fun validateRule(rule: TagRule): Boolean {
        return rule.ruleName.isNotBlank() &&
                rule.tagName.isNotBlank() &&
                rule.condition.isNotBlank() &&
                rule.extractPosition.isNotBlank() &&
                rule.extractLength > 0
    }
}
