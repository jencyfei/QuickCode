package com.sms.tagger.data.model

/**
 * 自定义打标规则数据模型
 */
data class TagRule(
    val id: String = "",
    val ruleName: String = "",
    val tagName: String = "",
    val ruleType: RuleType = RuleType.SENDER,
    val condition: String = "",
    val extractPosition: String = "",
    val extractLength: Int = 0,
    val isEnabled: Boolean = true,
    val priority: Int = 0
)

/**
 * 规则类型
 */
enum class RuleType {
    SENDER,      // 基于发信人
    CONTENT      // 基于短信内容
}

/**
 * 发信人规则条件类型
 */
enum class SenderConditionType {
    CONTAINS,        // 包含
    STARTS_WITH,     // 以...开头
    ENDS_WITH        // 以...结尾
}

/**
 * 规则执行结果
 */
data class RuleResult(
    val matched: Boolean = false,
    val extractedValue: String = "",
    val tagName: String = ""
)
