package com.sms.tagger.util

import com.sms.tagger.data.model.SmsCreate

/**
 * 按日期分组的快递信息
 * 用于在UI中显示按日期分组的快递列表
 */
data class ExpressGroupByDate(
    val date: String,                           // 日期：2025-11-13
    val count: Int,                             // 该日期的快递数量
    val expressList: List<ExpressInfo>,         // 该日期的快递列表
    var isExpanded: Boolean = true              // 是否展开
)
