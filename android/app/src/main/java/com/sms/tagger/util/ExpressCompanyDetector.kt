package com.sms.tagger.util

import java.util.Locale

/**
 * 根据短信发件人及内容推断快递公司与类型
 */
object ExpressCompanyDetector {

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
        "cainiao" to listOf("CAINIAO", "95188"),
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
        val type = detectType(sender, content) ?: return null
        val displayName = displayNameByType[type] ?: displayNameByType.getValue("default")
        return Result(displayName, type)
    }

    private fun detectType(sender: String?, content: String): String? {
        val normalizedSender = sender?.uppercase(Locale.ROOT) ?: ""
        if (normalizedSender.isNotEmpty()) {
            senderRules.forEach { (type, tokens) ->
                if (tokens.any { normalizedSender.contains(it) }) {
                    return type
                }
            }
        }

        val normalizedContent = content.lowercase(Locale.ROOT)
        keywordRules.forEach { (type, keywords) ->
            if (keywords.any { normalizedContent.contains(it) }) {
                return type
            }
        }

        if (generalKeywords.any { normalizedContent.contains(it) }) {
            return "default"
        }

        return null
    }
}

