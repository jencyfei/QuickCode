package com.sms.tagger.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sms.tagger.R
import com.sms.tagger.util.ExpressExtractor
import com.sms.tagger.util.ExpressInfo
import com.sms.tagger.util.PickupStatus
import com.sms.tagger.util.SmsReader
import com.sms.tagger.util.UsageLimitManager
import com.sms.tagger.util.AppLogger
import com.sms.tagger.util.ExpressDataCache
import com.sms.tagger.util.TimeWindowSettings
import com.sms.tagger.util.LogControlSettings
import com.sms.tagger.ui.components.GradientBackground
import com.sms.tagger.ui.components.DailyLimitDialog
import com.sms.tagger.ui.components.HistoryLimitDialog
import com.sms.tagger.ui.theme.TextSecondary
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.BorderStroke
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import java.util.regex.Pattern

private const val SMS_READ_LIMIT_ACTIVATED = 50000
private const val SMS_READ_LIMIT_DEFAULT = 50000
private const val SMS_INCREMENTAL_FETCH_LIMIT = 400
private val EXPRESS_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

/**
 * 快递信息页面
 * 
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressScreen() {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isTrial = false
    var expressList by remember { mutableStateOf<List<ExpressInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showRuleManager by remember { mutableStateOf(false) }
    var selectedExpressIds by remember { mutableStateOf(setOf<String>()) }
    var selectAllChecked by remember { mutableStateOf(false) }
    var showToast by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var confirmDialogAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var confirmDialogTitle by remember { mutableStateOf("") }
    var confirmDialogMessage by remember { mutableStateOf("") }
    var showDebugDialog by remember { mutableStateOf(false) }
    var debugInfo by remember { mutableStateOf("") }
    var currentTab by remember { mutableStateOf("pending") }
    // 搜索和筛选状态
    var searchText by remember { mutableStateOf("") }
    var dateFilterType by remember { mutableStateOf("本月") }  // 本月、本周、本日、全部
    var trialExpired by remember { mutableStateOf(false) }
    // 刷新key，用于强制刷新列表
    var refreshKey by remember { mutableStateOf(0) }
    
    // 限制策略相关状态
    var showDailyLimitDialog by remember { mutableStateOf(false) }
    var showHistoryLimitDialog by remember { mutableStateOf(false) }
    
    // 如果显示规则管理，则显示规则管理页面
    if (showRuleManager) {
        RuleManageScreen(onBack = { showRuleManager = false })
        return
    }

    // Trial 已废弃，无操作
    
    // 不再限制每日识别，不展示对话框
    if (showDailyLimitDialog) {
        showDailyLimitDialog = false
    }
    if (showHistoryLimitDialog) {
        showHistoryLimitDialog = false
    }
    // 历史记录限制提示取消，不再弹窗
    
    val statusPrefs = remember {
        context.getSharedPreferences("express_status", android.content.Context.MODE_PRIVATE)
    }
    var rawSmsList by remember { mutableStateOf<List<com.sms.tagger.data.model.SmsCreate>>(emptyList()) }
    var expressRecentDays by remember { mutableStateOf(TimeWindowSettings.getExpressDays(context)) }
    val shouldLimitByWindow = expressRecentDays > 0

    LaunchedEffect(Unit) {
        AppLogger.setVerboseOverride(LogControlSettings.isVerboseLoggingEnabled(context))
    }
    LaunchedEffect(refreshKey) {
        expressRecentDays = TimeWindowSettings.getExpressDays(context)
    }

    fun applyStatusAndFilters(
        extractedList: List<ExpressInfo>,
        recentDays: Long,
        existingKeys: MutableSet<String> = mutableSetOf()
    ): List<ExpressInfo> {
        val cutoffDate = LocalDate.now().minusDays(recentDays)
        val result = mutableListOf<ExpressInfo>()
        extractedList.forEach { express ->
            val statusKey = "pickup_${express.pickupCode}"
            val isSaved = statusPrefs.getBoolean(statusKey, false)
            val adjustedExpress = if (isSaved) {
                express.copy(status = PickupStatus.PICKED)
            } else {
                express
            }
            val key = "${adjustedExpress.date}_${adjustedExpress.pickupCode}"
            if (existingKeys.contains(key)) {
                return@forEach
            }
            existingKeys.add(key)
            val keep = if (adjustedExpress.date.isBlank()) {
                true
            } else {
                try {
                    LocalDate.parse(adjustedExpress.date, EXPRESS_DATE_FORMATTER) >= cutoffDate
                } catch (_: DateTimeParseException) {
                    true
                }
            }
            if (keep) {
                result.add(adjustedExpress)
            }
        }
        return result
    }
    
    // 加载快递信息
    LaunchedEffect(isTrial, trialExpired, refreshKey) {
        try {
            if (expressList.isEmpty()) {
                isLoading = true
            }
            // 无 Trial / 限制逻辑
            val reader = SmsReader(context)
            val latestMeta = reader.getLatestSmsMeta()
            val latestTimestamp = latestMeta?.timestamp
            val latestSmsId = latestMeta?.id
            ExpressDataCache.getIfFresh(latestTimestamp, latestSmsId)?.let {
                expressList = it
                isLoading = false
                return@LaunchedEffect
            }

            val cacheSnapshot = ExpressDataCache.getSnapshot()
            if (
                cacheSnapshot != null &&
                latestSmsId != null &&
                cacheSnapshot.latestSmsId != null &&
                latestSmsId > cacheSnapshot.latestSmsId
            ) {
                val incrementalSms = reader.readSmsAfterId(cacheSnapshot.latestSmsId, SMS_INCREMENTAL_FETCH_LIMIT)
                if (incrementalSms.isNotEmpty() && incrementalSms.size < SMS_INCREMENTAL_FETCH_LIMIT) {
                    rawSmsList = incrementalSms
                    AppLogger.debug("ExpressScreen") {
                        "🔁 增量解析短信 ${incrementalSms.size} 条 (lastId=${cacheSnapshot.latestSmsId} -> $latestSmsId)"
                    }
                    UsageLimitManager.incrementIdentifyCount(context)
                    var incrementalExpress = ExpressExtractor.extractAllExpressInfo(incrementalSms)
                    AppLogger.debug("ExpressScreen") {
                        "🔁 增量提取到 ${incrementalExpress.size} 条快递信息"
                    }
                    val dedupKeys = cacheSnapshot.list.mapTo(mutableSetOf()) { "${it.date}_${it.pickupCode}" }
                    val processedIncremental = applyStatusAndFilters(incrementalExpress, expressRecentDays, dedupKeys)
                    val mergedList = processedIncremental + cacheSnapshot.list
                    expressList = mergedList
                    if (UsageLimitManager.shouldShowHistoryLimitHint(context, mergedList.size)) {
                        showHistoryLimitDialog = true
                    }
                    val newestTimestamp = incrementalSms.firstOrNull()?.receivedAt ?: latestTimestamp
                    ExpressDataCache.update(mergedList, newestTimestamp, latestSmsId)
                    isLoading = false
                    return@LaunchedEffect
                } else if (incrementalSms.size >= SMS_INCREMENTAL_FETCH_LIMIT) {
                    AppLogger.w("ExpressScreen", "⚠️ 增量短信条数达到上限，回退全量解析")
                }
            }

            val smsReadLimit = SMS_READ_LIMIT_ACTIVATED
            val smsList = reader.readAllSms(smsReadLimit)
            rawSmsList = smsList
            
            val count10684 = smsList.count { it.sender.startsWith("10684") || it.sender.contains("10684") }
            AppLogger.debug("ExpressScreen") {
                "✅ 读取到 ${smsList.size} 条短信，其中10684开头的短信 $count10684 条"
            }
            
            // 【限制策略】免费版识别延迟
            // 不再延迟
            
            // 1. 从短信提取快递信息
            var extractedList = ExpressExtractor.extractAllExpressInfo(smsList)
            
            AppLogger.debug("ExpressScreen") { "✅ 提取到 ${extractedList.size} 条快递信息" }
            val cainiaoExpress = extractedList.filter { it.company.contains("菜鸟") || it.expressType == "cainiao" }
            if (cainiaoExpress.isNotEmpty()) {
                AppLogger.debug("ExpressScreen") {
                    buildString {
                        append("  其中菜鸟驿站快递: ${cainiaoExpress.size} 条")
                cainiaoExpress.take(3).forEachIndexed { index, express ->
                            append("\n    菜鸟快递 ${index + 1}: 取件码=${express.pickupCode}, 日期=${express.date}, 发件人=${express.sender}")
                }
                    }
                }
            }
            
            // 【限制策略】增加识别次数计数
            // 不再计数
            
            val processedList = applyStatusAndFilters(extractedList, expressRecentDays)
            expressList = processedList
            val newestTimestamp = smsList.firstOrNull()?.receivedAt ?: latestTimestamp
            ExpressDataCache.update(processedList, newestTimestamp, latestSmsId)
            isLoading = false
        } catch (e: Exception) {
            e.printStackTrace()
            isLoading = false
        }
    }
    
    GradientBackground {
        // 获取 SharedPreferences 实例（统一读取，避免重复）
        // 获取今日快递
        val today = java.time.LocalDate.now().toString().replace("-", "-").takeLast(5) // MM-DD
        val todayItems = expressList.filter { item ->
            item.receivedAt.takeLast(5) == today
        }
        
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column {
                    TopAppBar(
                        title = { 
                            Text(
                                text = if (currentTab == "pending") "未取快递" else "已取快递",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            ) 
                        },
                        actions = {
                            // 右上角操作按钮
                            if (currentTab == "pending") {
                                // "未取快递"页面显示"批量"、"一键"和"设置"按钮
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    // 批量复制按钮
                            Button(
                                onClick = {
                                            if (isTrial && trialExpired) {
                                                showToast = "体验版已到期，批量操作不可用"
                                                return@Button
                                            }
                                            // 批量复制逻辑：复制当前显示的未取快递取件码（应用相同的筛选和限制策略）
                                    val today = java.time.LocalDate.now()
                                    val windowStart = today.minusDays(expressRecentDays)
                                            // 1. 先筛选出符合时间窗口过滤设置的未取快递
                                            val filteredList = expressList.filter { express ->
                                                val statusKey = "pickup_${express.pickupCode}"
                                                val isPicked = statusPrefs.getBoolean(statusKey, express.status == PickupStatus.PICKED)
                                                val isNotPicked = !isPicked
                                                if (shouldLimitByWindow && isNotPicked) {
                                                    try {
                                                        val expressDate = java.time.LocalDate.parse(express.date)
                                                        expressDate >= windowStart
                                                    } catch (e: Exception) {
                                                        true
                                                    }
                                                } else {
                                                    isNotPicked
                                                }
                                            }
                            // 2. 不再截断条数，直接使用过滤后的未取列表
                            val pendingList = filteredList
                                            if (pendingList.isNotEmpty()) {
                                                val codes = pendingList.map { it.pickupCode }.joinToString("\n")
                                                clipboardManager.setText(AnnotatedString(codes))
                                                showToast = "已复制 ${pendingList.size} 个取件码"
                                    } else {
                                                showToast = "没有未取快递"
                                            }
                                        },
                                        modifier = Modifier.height(28.dp),
                                        shape = RoundedCornerShape(20.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0x10059669)
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                    ) {
                                        Text(
                                            text = "📋 批量",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF059669)
                                        )
                                    }
                                    // 一键取件按钮
                                    Button(
                                        onClick = {
                                            if (isTrial && trialExpired) {
                                                showToast = "体验版已到期，批量操作不可用"
                                                return@Button
                                            }
                                            // 一键取件逻辑：标记当前显示的未取快递为已取（应用相同的筛选和限制策略）
                                            val today = java.time.LocalDate.now()
                                            val windowStart = today.minusDays(expressRecentDays)
                                            // 1. 先筛选出符合时间窗口过滤设置的未取快递
                                            val filteredList = expressList.filter { express ->
                                                val statusKey = "pickup_${express.pickupCode}"
                                                val isPicked = statusPrefs.getBoolean(statusKey, express.status == PickupStatus.PICKED)
                                                val isNotPicked = !isPicked
                                                if (shouldLimitByWindow && isNotPicked) {
                                                    try {
                                                        val expressDate = java.time.LocalDate.parse(express.date)
                                                        expressDate >= windowStart
                                                    } catch (e: Exception) {
                                                        true
                                                    }
                                                } else {
                                                    isNotPicked
                                                }
                                            }
                                            // 2. 应用限制策略（免费版只显示3条）
                                            val pendingList = UsageLimitManager.limitHistoryList(context, filteredList)
                                            if (pendingList.isNotEmpty()) {
                                        confirmDialogTitle = "一键取件"
                                                confirmDialogMessage = "确定要将 ${pendingList.size} 个快递标记为已取吗？"
                                        confirmDialogAction = {
                                                    pendingList.forEach { express ->
                                                val statusKey = "pickup_${express.pickupCode}"
                                                        statusPrefs.edit().putBoolean(statusKey, true).apply()
                                                    }
                                                    showToast = "已取件 ${pendingList.size} 个快递"
                                                    // 强制刷新列表
                                                    refreshKey++
                                                }
                                                showConfirmDialog = true
                                                } else {
                                                showToast = "没有未取快递"
                                            }
                                        },
                                        modifier = Modifier.height(28.dp),
                                        shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0x10667EEA)
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                            ) {
                                Text(
                                            text = "⚡ 一键",
                                            fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF667EEA)
                                )
                            }
                                    // 设置按钮
                            IconButton(
                                        onClick = { showRuleManager = true },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "设置",
                                            tint = Color(0xFF333333),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            } else {
                                // "已取快递"页面只显示设置按钮
                                IconButton(
                                    onClick = { showRuleManager = true },
                                    modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                        contentDescription = "设置",
                                        tint = Color(0xFF333333),
                                        modifier = Modifier.size(18.dp)
                                )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                    // 页签栏
                    // 计算各选项卡的快递数量
                    val today = java.time.LocalDate.now()
                    val sevenDaysAgo = today.minusDays(7)
                    val windowStart = today.minusDays(expressRecentDays)
                    
                    // 统计已取快递数量：
                    // 1. 从 expressList 中找到所有在 SharedPreferences 中标记为已取的快递
                    // 2. 过滤出符合时间窗口显示范围的快递
                    // 这样确保数量统计基于真实状态（SharedPreferences），但只统计可显示的快递（expressList + 时间窗口过滤）
                    val pickedCount = expressList.filter { express ->
                        val statusKey = "pickup_${express.pickupCode}"
                        val isPicked = statusPrefs.getBoolean(statusKey, express.status == PickupStatus.PICKED)
                        isPicked && (
                            if (shouldLimitByWindow) {
                                try {
                            val expressDate = java.time.LocalDate.parse(express.date)
                                    expressDate >= windowStart
                        } catch (e: Exception) {
                            true
                        }
                            } else {
                                true
                            }
                        )
                    }.size
                    
                    // 统计未取快递数量（显示逻辑需与免费版条数限制一致）
                    // 1) 先按状态/时间窗口过滤
                    val pendingFiltered = expressList.filter { express ->
                        val statusKey = "pickup_${express.pickupCode}"
                        val isPicked = statusPrefs.getBoolean(statusKey, express.status == PickupStatus.PICKED)
                        val isNotPicked = !isPicked
                        if (shouldLimitByWindow && isNotPicked) {
                            try {
                                val expressDate = java.time.LocalDate.parse(express.date)
                                expressDate >= windowStart
                            } catch (e: Exception) {
                                true
                            }
                        } else {
                            isNotPicked
                        }
                    }
                    // 2) 数量统计与显示一致：不截断条数
                    val pendingCount = pendingFiltered.size
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        // 选项卡按钮
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { currentTab = "pending" },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentTab == "pending") 
                                    Color(0xFF667EEA).copy(alpha = 0.15f) 
                                else 
                                    Color.White.copy(alpha = 0.3f)
                            ),
                            border = if (currentTab == "pending") 
                                BorderStroke(1.dp, Color(0xFF667EEA).copy(alpha = 0.3f))
                            else
                                null
                        ) {
                            Text(
                                text = "未取 ($pendingCount)",
                                fontSize = 14.sp,
                                fontWeight = if (currentTab == "pending") FontWeight.SemiBold else FontWeight.Normal,
                                color = if (currentTab == "pending") Color(0xFF667EEA) else Color(0xFF333333)
                            )
                        }
                        Button(
                            onClick = { currentTab = "picked" },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentTab == "picked") 
                                    Color(0xFF4CAF50).copy(alpha = 0.15f) 
                                else 
                                    Color.White.copy(alpha = 0.3f)
                            ),
                            border = if (currentTab == "picked") 
                                BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.3f))
                            else
                                null
                        ) {
                            Text(
                                text = "已取 ($pickedCount)",
                                fontSize = 14.sp,
                                fontWeight = if (currentTab == "picked") FontWeight.SemiBold else FontWeight.Normal,
                                color = if (currentTab == "picked") Color(0xFF4CAF50) else Color(0xFF333333)
                            )
                            }
                        }
                    }
                    // 搜索栏和日期筛选（仅在已取选项卡显示）
                    if (currentTab == "picked") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextField(
                                value = searchText,
                                onValueChange = { newValue -> searchText = newValue },
                                placeholder = {
                                    Text(
                                        "搜索取件码或日期...",
                                        color = Color(0xFF8A8A8A),
                                        fontSize = 14.sp
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .background(
                                        color = Color(0xFFFFFFFF).copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFFFFFFFF).copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedTextColor = Color(0xFF333333),
                                    unfocusedTextColor = Color(0xFF333333),
                                    cursorColor = Color(0xFF333333),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                            )
                            
                            // 日期筛选按钮 - 单个按钮循环切换（与HTML模板一致）
                            val filterOptions = listOf("本月", "本周", "本日", "全部")
                            Button(
                                onClick = { 
                                    // 循环切换筛选选项
                                    val currentIndex = filterOptions.indexOf(dateFilterType)
                                    val nextIndex = (currentIndex + 1) % filterOptions.size
                                    dateFilterType = filterOptions[nextIndex]
                                },
                                modifier = Modifier
                                    .height(40.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFFFFF).copy(alpha = 0.5f)
                                ),
                                border = BorderStroke(
                                    1.dp, 
                                    Color(0xFFFFFFFF).copy(alpha = 0.6f)
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                Text(
                                    text = "📅 $dateFilterType",
                                    fontSize = 14.sp,
                                    color = Color(0xFF333333)
                                )
                            }
                        }
                    }
                }
            },
            bottomBar = {}
        ) { paddingValues ->
            // Toast 提示 - 显示在顶部，避免被卡片遮挡
            if (showToast.isNotEmpty()) {
                LaunchedEffect(showToast) {
                    kotlinx.coroutines.delay(2000)
                    showToast = ""
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = showToast,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (expressList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无快递信息",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF8A8A8A)
                    )
                }
            } else {
                // 获取当天日期
                val today = java.time.LocalDate.now()
                val todayStr = today.toString().substring(5).replace("-", "-")  // MM-DD 格式
                
                // 根据当前页签过滤数据
                // 使用统一的 SharedPreferences 实例，确保显示逻辑与数量统计逻辑一致
                val filteredList = if (currentTab == "pending") {
                    val today = java.time.LocalDate.now()
                    val windowStart = today.minusDays(expressRecentDays)
                    
                    expressList.filter { express ->
                        val statusKey = "pickup_${express.pickupCode}"
                        val isPicked = statusPrefs.getBoolean(statusKey, express.status == PickupStatus.PICKED)
                        val isNotPicked = !isPicked
                        
                        if (shouldLimitByWindow && isNotPicked) {
                            try {
                                val expressDate = java.time.LocalDate.parse(express.date)
                                expressDate >= windowStart
                            } catch (e: Exception) {
                                true  // 如果解析失败，保留该项
                            }
                        } else {
                            isNotPicked
                        }
                    }
                } else {
                    val cutoffDate = when (dateFilterType) {
                        "本月" -> today.minusDays(expressRecentDays)
                        "本周" -> today.minusDays(7)
                        "本日" -> today
                        "全部" -> java.time.LocalDate.of(2000, 1, 1)
                        else -> today.minusDays(expressRecentDays)
                    }
                    expressList.filter { express ->
                        val statusKey = "pickup_${express.pickupCode}"
                        val isPicked = statusPrefs.getBoolean(statusKey, express.status == PickupStatus.PICKED)
                        isPicked && try {
                            val expressDate = java.time.LocalDate.parse(express.date)
                            expressDate >= cutoffDate
                        } catch (e: Exception) {
                            true  // 如果解析失败，保留该项
                        }
                    }
                }
                
                // 搜索过滤（如果是在已取选项卡且有搜索文本）
                val searchFilteredList = if (currentTab == "picked" && searchText.isNotEmpty()) {
                    filteredList.filter { express ->
                        express.pickupCode.contains(searchText, ignoreCase = true) ||
                        express.date.contains(searchText, ignoreCase = true)
                    }
                } else {
                    filteredList
                }
                
                // 显示全部（不再按条数截断）
                val limitedList = searchFilteredList
                
                // 按日期分组，再在日期内按地址分组；同一天相同地址只显示一次地址头
                val groupedByDate: Map<String, Map<String, List<ExpressInfo>>> = limitedList
                    .groupBy { it.date }
                    .mapValues { (_, list) -> list.groupBy { it.location ?: "未知地址" } }
                    .toSortedMap(compareBy<String> { it }.reversed())
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groupedByDate.forEach { (date, itemsInDate) ->
                        val groupedByLocation = itemsInDate.values.flatten()
                            .groupBy { it.location ?: "未知地址" }
                        groupedByLocation.forEach { (location, items) ->
                            val sortedItems = items.sortedBy { it.pickupCode }
                            item(key = "date_${date}_loc_$location") {
                                LocationGroup(
                                    location = location,
                                expressItems = sortedItems,
                                isEditMode = false,
                                selectedExpressIds = emptySet(),
                                    showHeader = true,
                                onSelectionChange = { _, _ -> }
                            )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // 一键取件确认对话框
    if (showConfirmDialog) {
        // 半透明遮罩背景
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(enabled = false) { },
            contentAlignment = Alignment.Center
        ) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { Text(confirmDialogTitle) },
                text = { Text(confirmDialogMessage) },
                confirmButton = {
                    Button(
                        onClick = {
                            confirmDialogAction?.invoke()
                            showConfirmDialog = false
                        }
                    ) {
                        Text("确定")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showConfirmDialog = false }
                    ) {
                        Text("取消")
                    }
                },
                containerColor = Color.White
            )
        }
    }
    
    // 调试对话框
    if (showDebugDialog) {
        AlertDialog(
            onDismissRequest = { showDebugDialog = false },
            title = { Text("📋 调试信息 - 原始数据与提取结果") },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    androidx.compose.foundation.lazy.LazyColumn {
                        item {
                            Text(
                                text = debugInfo,
                                fontSize = 11.sp,
                                color = Color(0xFF333333),
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // 复制到剪贴板
                        clipboardManager.setText(AnnotatedString(debugInfo))
                        showToast = "已复制调试信息"
                        showDebugDialog = false
                    }
                ) {
                    Text("复制")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDebugDialog = false }
                ) {
                    Text("关闭")
                }
            }
        )
    }
}

@Composable
fun DateGroup(
    date: String,
    expressItems: List<ExpressInfo>,
    isEditMode: Boolean = false,
    selectedExpressIds: Set<String> = emptySet(),
    lastAddress: String? = null,
    onLastAddressChange: (String?) -> Unit = {},
    onSelectionChange: ((String, Boolean) -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 日期头部 - 隐藏（不显示日期标题）
        // Row(
        //     modifier = Modifier
        //         .fillMaxWidth()
        //         .padding(horizontal = 4.dp),
        //     horizontalArrangement = Arrangement.Start,
        //     verticalAlignment = Alignment.CenterVertically
        // ) {
        //     // 只显示日期，移除日期数量和折叠图标
        //     Text(
        //         text = date,
        //         fontSize = 14.sp,
        //         fontWeight = FontWeight.SemiBold,
        //         color = Color(0xFF333333)
        //     )
        // }
        
        // 快递卡片列表 - 按地址分组，始终显示
        val groupedByLocation = expressItems.groupBy { it.location ?: "未知地址" }
        var currentLastAddress = lastAddress
        groupedByLocation.forEach { (location, items) ->
            LocationGroup(
                location = location,
                expressItems = items,
                isEditMode = isEditMode,
                selectedExpressIds = selectedExpressIds,
                showHeader = (location != currentLastAddress),
                onSelectionChange = onSelectionChange
            )
            // 更新最后一个地址
            items.lastOrNull()?.let { 
                currentLastAddress = it.location ?: "未知地址"
                onLastAddressChange(currentLastAddress)
            }
        }
    }
}

@Composable
fun LocationGroup(
    location: String,
    expressItems: List<ExpressInfo>,
    isEditMode: Boolean = false,
    selectedExpressIds: Set<String> = emptySet(),
    showHeader: Boolean = true,
    onSelectionChange: ((String, Boolean) -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 地址标题 - 条件显示（智能隐藏重复地址）
        if (showHeader) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFF667EEA).copy(alpha = 0.05f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "地址",
                    modifier = Modifier.size(18.dp),
                    tint = Color(0xFF667EEA)
                )
                Text(
                    text = location,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )
            }
        }
        
        // 该地址下的所有快递卡片
        expressItems.forEach { express ->
            ExpressItemCard(
                express = express,
                isEditMode = isEditMode,
                isSelected = selectedExpressIds.contains(express.pickupCode),
                onSelectionChange = { selected ->
                    onSelectionChange?.invoke(express.pickupCode, selected)
                }
            )
        }
    }
}

@Composable
fun ExpressItemCard(
    express: ExpressInfo,
    isEditMode: Boolean = false,
    isSelected: Boolean = false,
    onSelectionChange: ((Boolean) -> Unit)? = null
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    
    // 从 SharedPreferences 读取状态
    val sharedPref = context.getSharedPreferences("express_status", android.content.Context.MODE_PRIVATE)
    val statusKey = "pickup_${express.pickupCode}"
    
    // 实时从 SharedPreferences 读取状态，确保状态正确
    var isPicked by remember(express.pickupCode) { 
        mutableStateOf(sharedPref.getBoolean(statusKey, express.status == PickupStatus.PICKED))
    }
    
    // 当组件重新组合时，重新从 SharedPreferences 读取状态
    LaunchedEffect(express.pickupCode) {
        isPicked = sharedPref.getBoolean(statusKey, express.status == PickupStatus.PICKED)
    }
    
    // 提取时间信息
    val timeStr = express.receivedAt.let { time ->
        val timePattern = Pattern.compile("(\\d{2}):(\\d{2}):(\\d{2})")
        val timeMatcher = timePattern.matcher(time)
        if (timeMatcher.find()) {
            timeMatcher.group(0)  // 返回 HH:MM:SS
        } else {
            time
        }
    }
    
    val dateDisplay = remember(express.date) { formatDateLabel(express.date) }
    val timeDisplay = remember(timeStr) { formatTimeLabel(timeStr) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFF667EEA).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.7f),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> Color.White
                isPicked -> Color.White.copy(alpha = 0.3f)
                else -> Color.White
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPicked) 0.dp else 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CourierIcon(
                expressType = express.expressType,
                contentDescription = express.company
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                        Text(
                            text = express.pickupCode,
                        fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937),
                            letterSpacing = 1.sp
                        )
                    Column(
                        modifier = Modifier
                            .background(
                                color = Color(0xFF667EEA).copy(alpha = 0.12f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = dateDisplay,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF667EEA)
                        )
                        Text(
                            text = timeDisplay,
                            fontSize = 10.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isPicked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = if (isPicked) "已取" else "未取",
                        tint = if (isPicked) Color(0xFF4CAF50) else Color(0xFFFF9800),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isPicked) "已取" else "未取",
                        fontSize = 12.sp,
                        color = if (isPicked) Color(0xFF4CAF50) else Color(0xFFFF9800),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            IconButton(
                onClick = {
                    isPicked = !isPicked
                    // 保存状态到 SharedPreferences
                    sharedPref.edit().putBoolean(statusKey, isPicked).apply()
                },
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        color = if (isPicked) Color(0xFF4CAF50).copy(alpha = 0.15f) else Color(0xFF667EEA).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Icon(
                    imageVector = if (isPicked) 
                        Icons.Default.CheckCircle 
                    else 
                        Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (isPicked) "已取" else "未取",
                    tint = if (isPicked) Color(0xFF4CAF50) else Color(0xFF667EEA),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun CourierIcon(
    expressType: String,
    contentDescription: String
) {
    val iconRes = remember(expressType) { courierIconRes(expressType) }
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.6f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(6.dp))
        )
    }
}

private fun courierIconRes(expressType: String): Int {
    return when (expressType.lowercase(Locale.ROOT)) {
        "sf" -> R.drawable.sf
        "jd" -> R.drawable.jd
        "zto" -> R.drawable.zto
        "yto" -> R.drawable.yto
        "yunda" -> R.drawable.yunda
        "sto" -> R.drawable.sto
        "cainiao" -> R.drawable.cainiao
        "fengchao" -> R.drawable.fengchao
        "ems" -> R.drawable.ems
        else -> R.drawable.default_box
    }
}

private fun formatDateLabel(raw: String): String {
    return try {
        val date = java.time.LocalDate.parse(raw)
        String.format(Locale.getDefault(), "%02d-%02d", date.monthValue, date.dayOfMonth)
    } catch (e: Exception) {
        raw
    }
}

private fun formatTimeLabel(raw: String): String {
    val trimmed = raw.trim()
    return if (trimmed.length >= 5) trimmed.substring(0, 5) else trimmed
}
