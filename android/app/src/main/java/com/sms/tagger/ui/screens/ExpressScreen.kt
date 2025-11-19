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
            expressList = ExpressExtractor.extractAllExpressInfo(smsList)
            isLoading = false
        } catch (e: Exception) {
            e.printStackTrace()
            isLoading = false
        }
    }
    
    GradientBackground {
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
                                    // 获取未取快递列表
                                    val pendingItems = expressList.filter { it.status != PickupStatus.PICKED }
                                    
                                    if (pendingItems.isEmpty()) {
                                        showToast = "暂无未取快递"
                                    } else {
                                        // 显示确认对话框
                                        showConfirmDialog = true
                                        confirmDialogTitle = "一键取件"
                                        confirmDialogMessage = "确定要一键取件 ${pendingItems.size} 个快递吗？"
                                        confirmDialogAction = {
                                            // 标记所有未取快递为已取
                                            pendingItems.forEach { express ->
                                                val statusKey = "pickup_${express.pickupCode}"
                                                context.getSharedPreferences("express_status", android.content.Context.MODE_PRIVATE)
                                                    .edit()
                                                    .putBoolean(statusKey, true)
                                                    .apply()
                                            }
                                            showToast = "已取件 ${pendingItems.size} 个快递"
                                            // 刷新列表
                                            expressList = expressList.map { express ->
                                                if (express.status != PickupStatus.PICKED) {
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
                            // 调试按钮
                            IconButton(
                                onClick = { 
                                    val debugText = buildString {
                                        append("=== 原始短信数据 (JSON格式) ===\n")
                                        append("[\n")
                                        rawSmsList.take(10).forEachIndexed { index, sms ->
                                            append("  {\n")
                                            append("    \"收信时间\": \"${sms.receivedAt}\",\n")
                                            append("    \"发信号码\": \"${sms.sender}\",\n")
                                            append("    \"短信内容\": \"${sms.content.replace("\"", "\\\"")}\"\n")
                                            append("  }")
                                            if (index < rawSmsList.take(10).size - 1) {
                                                append(",")
                                            }
                                            append("\n")
                                        }
                                        append("]\n\n")
                                        append("=== 提取结果 ===\n")
                                        append("总快递数: ${expressList.size}\n\n")
                                        expressList.take(10).forEachIndexed { index, express ->
                                            append("【快递 ${index + 1}】\n")
                                            append("快递公司: ${express.company}\n")
                                            append("取件码: ${express.pickupCode}\n")
                                            append("提取日期: ${express.date}\n")
                                            append("地址: ${express.location ?: "未提取"}\n")
                                            append("发件人: ${express.sender}\n")
                                            append("接收时间: ${express.receivedAt}\n")
                                            append("取件状态: ${express.status}\n")
                                            append("原始短信: ${express.fullContent.take(80)}\n")
                                            append("\n")
                                        }
                                    }
                                    debugInfo = debugText
                                    showDebugDialog = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "调试信息",
                                    tint = Color(0xFF333333)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                    // 页签栏
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
                                text = "未取",
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
                                text = "已取",
                                fontSize = 14.sp,
                                fontWeight = if (currentTab == "picked") FontWeight.SemiBold else FontWeight.Normal,
                                color = if (currentTab == "picked") Color(0xFF4CAF50) else Color(0xFF333333)
                            )
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
                val filteredList = if (currentTab == "pending") {
                    // 未取快递：默认显示最近7天的信息
                    val sevenDaysAgo = today.minusDays(7)
                    expressList.filter { express ->
                        express.status != PickupStatus.PICKED && try {
                            // 解析日期 (YYYY-MM-DD 格式)
                            val expressDate = java.time.LocalDate.parse(express.date)
                            expressDate >= sevenDaysAgo
                        } catch (e: Exception) {
                            true  // 如果解析失败，保留该项
                        }
                    }
                } else {
                    // 已取快递：最多显示最近30天的信息
                    val thirtyDaysAgo = today.minusDays(30)
                    expressList.filter { express ->
                        express.status == PickupStatus.PICKED && try {
                            val expressDate = java.time.LocalDate.parse(express.date)
                            expressDate >= thirtyDaysAgo
                        } catch (e: Exception) {
                            true  // 如果解析失败，保留该项
                        }
                    }
                }
                
                // 按日期分组，然后按日期倒序（日期较新的在前）
                val groupedByDate = filteredList
                    .groupBy { it.date }  // 按日期分组
                    .toSortedMap(compareBy<String> { it }.reversed())  // 日期倒序（日期较新的在前）
                
                // 添加地址追踪变量
                var lastAddress: String? = null
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
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
            }
        )
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
    var isPicked by remember { 
        mutableStateOf(sharedPref.getBoolean(statusKey, express.status == PickupStatus.PICKED))
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
