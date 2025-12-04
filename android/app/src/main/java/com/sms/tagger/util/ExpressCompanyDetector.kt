package com.sms.tagger.util

import java.util.Locale

/**
 * 根据短信发件人及内容推断快递公司与类型
 */
object ExpressCompanyDetector {
    
    private const val TAG = "ExpressCompanyDetector"

    data class Result(
        val displayName: String,
        val type: String
    )

    private val displayNameByType = mapOf(
        "sf" to "顺丰速运",
        "jd" to "京东物流",
        "zto" to "中通快递",
        "yto" to "圆通速递",
        "sto" to "申通快递",
        "cainiao" to "菜鸟驿站",
        "fengchao" to "丰巢柜",
        "ems" to "中国邮政",
        "default" to "包裹"
    )

    private val senderRules: Map<String, List<String>> = mapOf(
        "sf" to listOf("SF", "95338"),
        "jd" to listOf("JD", "950618", "106550618"),
        "zto" to listOf("ZTO", "95311"),
        "yto" to listOf("YTO", "95554"),
        "sto" to listOf("STO", "95543"),
        "cainiao" to listOf("CAINIAO", "95188", "10684"),
        "ems" to listOf("EMS", "11185")
    ).mapValues { entry ->
        entry.value.map { it.uppercase(Locale.ROOT) }
    }

    private val keywordRules: Map<String, List<String>> = mapOf(
        "sf" to listOf("顺丰", "SF", "已投柜", "派送员"),
        "jd" to listOf("京东", "JD", "京东快递"),
        "zto" to listOf("中通", "ZTO"),
        "yto" to listOf("圆通", "YTO"),
        "sto" to listOf("申通", "STO"),
        "cainiao" to listOf("菜鸟", "菜鸟驿站"),
        "fengchao" to listOf("丰巢"),
        "ems" to listOf("邮政", "EMS", "中国邮政")
    ).mapValues { entry ->
        entry.value.map { it.lowercase(Locale.ROOT) }
    }

    private val generalKeywords = listOf(
        "取件码", "取货码", "提货码", "快递", "包裹", "驿站", "自提柜"
    ).map { it.lowercase(Locale.ROOT) }

    fun detect(sender: String?, content: String): Result? {
        val is10684Sender = sender?.startsWith("10684") == true
        val containsTargetCode = content.contains("9-5-5038")
        
        if (is10684Sender || containsTargetCode) {
            AppLogger.d(TAG, "🔍 开始检测快递公司: 发件人=$sender, 内容=${content.take(100)}")
        }
        
        val type = detectType(sender, content)
        
        if (type != null) {
            val displayName = displayNameByType[type] ?: displayNameByType.getValue("default")
            if (is10684Sender || containsTargetCode) {
                AppLogger.w(TAG, "✅ 识别为快递: ${displayName} (类型=$type)")
            }
            return Result(displayName, type)
        } else {
            if (is10684Sender || containsTargetCode) {
                AppLogger.w(TAG, "❌ 未能识别为快递: 发件人=$sender, 内容=${content.take(200)}")
            }
            return null
        }
    }

    private fun detectType(sender: String?, content: String): String? {
        val normalizedSender = sender?.uppercase(Locale.ROOT) ?: ""
        val is10684Sender = normalizedSender.startsWith("10684")
        
        if (is10684Sender) {
            AppLogger.d(TAG, "  检查发件人规则: $normalizedSender")
        }
        
        if (normalizedSender.isNotEmpty()) {
            senderRules.forEach { (type, tokens) ->
                if (tokens.any { normalizedSender.contains(it) }) {
                    if (is10684Sender) {
                        AppLogger.w(TAG, "  ✅ 发件人匹配: 类型=$type, 规则=${tokens.joinToString(", ")}")
                    }
                    return type
                }
            }
        }
        
        if (is10684Sender) {
            AppLogger.d(TAG, "  发件人规则未匹配，检查内容关键词")
        }

        val normalizedContent = content.lowercase(Locale.ROOT)
        keywordRules.forEach { (type, keywords) ->
            if (keywords.any { normalizedContent.contains(it) }) {
                if (is10684Sender || content.contains("9-5-5038")) {
                    AppLogger.w(TAG, "  ✅ 内容关键词匹配: 类型=$type, 关键词=${keywords.joinToString(", ")}")
                }
                return type
            }
        }

        if (generalKeywords.any { normalizedContent.contains(it) }) {
            if (is10684Sender || content.contains("9-5-5038")) {
                AppLogger.w(TAG, "  ✅ 通用关键词匹配: default")
            }
            return "default"
        }

        if (is10684Sender || content.contains("9-5-5038")) {
            AppLogger.w(TAG, "  ❌ 所有规则都未匹配")
        }
        return null
    }
}

