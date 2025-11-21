package com.sms.tagger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sms.tagger.util.ExpressExtractor
import com.sms.tagger.util.ExpressInfo
import com.sms.tagger.util.PickupStatus
import com.sms.tagger.util.SmsReader
import com.sms.tagger.ui.components.GradientBackground
import com.sms.tagger.ui.theme.TextSecondary
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Settings
import java.util.regex.Pattern

/**
 * 快递信息页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressScreen() {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
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
    
    // 如果显示规则管理，则显示规则管理页面
    if (showRuleManager) {
        RuleManageScreen(onBack = { showRuleManager = false })
        return
    }
    
    var rawSmsList by remember { mutableStateOf<List<com.sms.tagger.data.model.SmsCreate>>(emptyList()) }
    
    // 加载快递信息
    LaunchedEffect(Unit) {
        try {
            val reader = SmsReader(context)
            // 读取最近5000条短信，确保包含所有快递信息
            val smsList = reader.readLatestSms(5000)
            rawSmsList = smsList
            
            // 1. 从短信提取快递信息
            var extractedList = ExpressExtractor.extractAllExpressInfo(smsList)
            
            // 2. 从 SharedPreferences 读取保存的状态
            val prefs = context.getSharedPreferences("express_status", android.content.Context.MODE_PRIVATE)
            extractedList = extractedList.map { express ->
                val statusKey = "pickup_${express.pickupCode}"
                val isSaved = prefs.getBoolean(statusKey, false)
                if (isSaved) {
                    express.copy(status = PickupStatus.PICKED)
                } else {
                    express
                }
            }
            
            // 3. 按日期和取件码去重（保留第一条记录）
            val seenKeys = mutableSetOf<String>()
            extractedList = extractedList.filter { express ->
                // 使用"日期_取件码"作为唯一键
                val key = "${express.date}_${express.pickupCode}"
                if (seenKeys.contains(key)) {
                    false  // 已存在，过滤掉
                } else {
                    seenKeys.add(key)
                    true   // 保留第一条
                }
            }
            
            // 4. 更新内存
            expressList = extractedList
            isLoading = false
        } catch (e: Exception) {
            e.printStackTrace()
            isLoading = false
        }
    }
    
    GradientBackground {
        // 获取 SharedPreferences 实例（统一读取，避免重复）
        val statusPrefs = remember { 
            context.getSharedPreferences("express_status", android.content.Context.MODE_PRIVATE)
        }
        
        // 获取今日快递
        val today = java.time.LocalDate.now().toString().replace("-", "-").takeLast(5) // MM-DD
        val todayItems = expressList.filter { item ->
            item.receivedAt.takeLast(5) == today
        }
        
        // Toast 提示
        if (showToast.isNotEmpty()) {
            LaunchedEffect(showToast) {
                kotlinx.coroutines.delay(2000)
                showToast = ""
            }
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
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
        
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column {
                    TopAppBar(
                        title = { Text("未取快递") },
                        actions = {
                            // 一键取件按钮
                            Button(
                                onClick = {
                                    // 获取当前页签的未取快递列表（已过滤）
                                    val today = java.time.LocalDate.now()
                                    val sevenDaysAgo = today.minusDays(7)
                                    // 一键取件：使用 SharedPreferences 状态判断，确保与数量统计逻辑一致
                                    val tempPrefs = context.getSharedPreferences("express_status", android.content.Context.MODE_PRIVATE)
                                    val pendingItems = if (currentTab == "pending") {
                                        expressList.filter { express ->
                                            val statusKey = "pickup_${express.pickupCode}"
                                            val isPicked = tempPrefs.getBoolean(statusKey, express.status == PickupStatus.PICKED)
                                            !isPicked && try {
                                                val expressDate = java.time.LocalDate.parse(express.date)
                                                expressDate >= sevenDaysAgo
                                            } catch (e: Exception) {
                                                true
                                            }
                                        }
                                    } else {
                                        emptyList()
                                    }
                                    
                                    if (pendingItems.isEmpty()) {
                                        showToast = "暂无未取快递"
                                    } else {
                                        // 显示确认对话框
                                        showConfirmDialog = true
                                        confirmDialogTitle = "一键取件"
                                        confirmDialogMessage = "确定要一键取件 ${pendingItems.size} 个快递吗？"
                                        confirmDialogAction = {
                                            // 标记所有未取快递为已取
                                            val sharedPref = context.getSharedPreferences("express_status", android.content.Context.MODE_PRIVATE)
                                            val editor = sharedPref.edit()
                                            pendingItems.forEach { express ->
                                                val statusKey = "pickup_${express.pickupCode}"
                                                editor.putBoolean(statusKey, true)
                                            }
                                            editor.apply()
                                            showToast = "已取件 ${pendingItems.size} 个快递"
                                            // 刷新列表：更新 expressList 中对应快递的状态，确保 UI 立即反映变化
                                            expressList = expressList.map { express ->
                                                val statusKey = "pickup_${express.pickupCode}"
                                                val isPicked = sharedPref.getBoolean(statusKey, express.status == PickupStatus.PICKED)
                                                if (isPicked) {
                                                    express.copy(status = PickupStatus.PICKED)
                                                } else {
                                                    express
                                                }
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF667EEA).copy(alpha = 0.1f)
                                )
                            ) {
                                Text(
                                    text = "一键取件",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF667EEA)
                                )
                            }
                            // 设置按钮 - 打开规则管理
                            IconButton(
                                onClick = { 
                                    showRuleManager = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "设置规则",
                                    tint = Color(0xFF333333)
                                )
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
                    val thirtyDaysAgo = today.minusDays(30)
                    
                    // 统计已取快递数量：
                    // 1. 从 expressList 中找到所有在 SharedPreferences 中标记为已取的快递
                    // 2. 过滤出符合30天显示范围的快递
                    // 这样确保数量统计基于真实状态（SharedPreferences），但只统计可显示的快递（expressList + 30天过滤）
                    val pickedCount = expressList.filter { express ->
                        val statusKey = "pickup_${express.pickupCode}"
                        val isPicked = statusPrefs.getBoolean(statusKey, express.status == PickupStatus.PICKED)
                        isPicked && try {
                            val expressDate = java.time.LocalDate.parse(express.date)
                            expressDate >= thirtyDaysAgo
                        } catch (e: Exception) {
                            true
                        }
                    }.size
                    
                    // 统计未取快递数量：
                    // expressList 中未在 SharedPreferences 中标记为已取，且符合7天显示范围的快递
                    val pendingCount = expressList.filter { express ->
                        val statusKey = "pickup_${express.pickupCode}"
                        val isPicked = statusPrefs.getBoolean(statusKey, express.status == PickupStatus.PICKED)
                        !isPicked && try {
                            val expressDate = java.time.LocalDate.parse(express.date)
                            expressDate >= sevenDaysAgo
                        } catch (e: Exception) {
                            true
                        }
                    }.size
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
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
                    // 未取快递：默认显示最近7天的信息
                    val sevenDaysAgo = today.minusDays(7)
                    expressList.filter { express ->
                        val statusKey = "pickup_${express.pickupCode}"
                        val isPicked = statusPrefs.getBoolean(statusKey, express.status == PickupStatus.PICKED)
                        !isPicked && try {
                            // 解析日期 (YYYY-MM-DD 格式)
                            val expressDate = java.time.LocalDate.parse(express.date)
                            expressDate >= sevenDaysAgo
                        } catch (e: Exception) {
                            true  // 如果解析失败，保留该项
                        }
                    }
                } else {
                    // 已取快递：根据日期筛选类型过滤
                    val cutoffDate = when (dateFilterType) {
                        "本月" -> today.minusDays(30)
                        "本周" -> today.minusDays(7)
                        "本日" -> today.minusDays(0)
                        "全部" -> java.time.LocalDate.of(2000, 1, 1)  // 显示所有
                        else -> today.minusDays(30)
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
                
                // 按日期分组，然后按日期倒序（日期较新的在前）
                val groupedByDate = searchFilteredList
                    .groupBy { it.date }  // 按日期分组
                    .toSortedMap(compareBy<String> { it }.reversed())  // 日期倒序（日期较新的在前）
                
                // 添加地址追踪变量
                var lastAddress: String? = null
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 日期分组
                    groupedByDate.forEach { (date, expressItems) ->
                        item {
                            // 同一天内按取件码顺序排列
                            val sortedItems = expressItems.sortedBy { it.pickupCode }
                            DateGroup(
                                date = date,
                                expressItems = sortedItems,
                                isEditMode = false,
                                selectedExpressIds = emptySet(),
                                lastAddress = lastAddress,
                                onLastAddressChange = { newAddress ->
                                    lastAddress = newAddress
                                },
                                onSelectionChange = { _, _ -> }
                            )
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
            // 取件码和日期时间
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 取件码和日期时间框
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "取件码",
                            fontSize = 11.sp,
                            color = Color(0xFF999999),
                            fontWeight = FontWeight.Normal
                        )
                        Text(
                            text = express.pickupCode,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333),
                            letterSpacing = 1.sp
                        )
                    }
                    
                    // 日期和时间框 - 调整到中间位置
                    Column(
                        modifier = Modifier
                            .background(
                                color = Color(0xFF667EEA).copy(alpha = 0.08f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Text(
                            text = express.date,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF667EEA)
                        )
                        Text(
                            text = timeStr,
                            fontSize = 10.sp,
                            color = Color(0xFFAAAAAA)
                        )
                    }
                }
                
                // 状态标签
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Icon(
                        imageVector = if (isPicked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = if (isPicked) "已取" else "未取",
                        tint = if (isPicked) Color(0xFF4CAF50) else Color(0xFFFF9800),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (isPicked) "已取" else "未取",
                        fontSize = 12.sp,
                        color = if (isPicked) Color(0xFF4CAF50) else Color(0xFFFF9800),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // 状态按钮 - 始终显示
            IconButton(
                onClick = {
                    isPicked = !isPicked
                    // 保存状态到 SharedPreferences
                    sharedPref.edit().putBoolean(statusKey, isPicked).apply()
                },
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = if (isPicked) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(0xFF667EEA).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(50)
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
