package com.sms.tagger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Download
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sms.tagger.util.ExpressExtractor
import com.sms.tagger.util.ExpressInfo
import com.sms.tagger.util.PickupStatus
import com.sms.tagger.util.SmsReader
import com.sms.tagger.ui.components.GradientBackground
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.BorderStroke
import java.time.LocalDate

/**
 * 已取快递页面 - 柔和玻璃拟态风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressPickedScreen() {
    val context = LocalContext.current
    var expressList by remember { mutableStateOf<List<ExpressInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchText by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("本月") }
    var showToast by remember { mutableStateOf("") }
    
    // 获取 SharedPreferences 实例
    val statusPrefs = remember { 
        context.getSharedPreferences("express_status", android.content.Context.MODE_PRIVATE)
    }

    // 加载快递信息
    LaunchedEffect(Unit) {
        try {
            val reader = SmsReader(context)
            val smsList = reader.readLatestSms(5000)
            var extractedList = ExpressExtractor.extractAllExpressInfo(smsList)
            
            // 从 SharedPreferences 读取保存的状态
            extractedList = extractedList.map { express ->
                val statusKey = "pickup_${express.pickupCode}"
                val isSaved = statusPrefs.getBoolean(statusKey, false)
                if (isSaved) {
                    express.copy(status = PickupStatus.PICKED)
                } else {
                    express
                }
            }
            
            expressList = extractedList
            isLoading = false
        } catch (e: Exception) {
            e.printStackTrace()
            isLoading = false
        }
    }

    // 玻璃拟态背景
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFF9F8FF),  // 极浅薰衣草
                        Color(0xFFFAD0C4),  // 柔和粉
                        Color(0xFFD9C8FF),  // 柔和紫
                        Color(0xFFF9F8FF)   // 极浅薰衣草
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column {
                    // 顶部栏 - 毛玻璃效果
                    TopAppBar(
                        title = { Text("已取快递", fontSize = 24.sp, fontWeight = FontWeight.Bold) },
                        actions = {
                            IconButton(onClick = { /* 导出记录 */ }) {
                                Icon(Icons.Default.Download, contentDescription = "导出记录", tint = Color(0xFF333333))
                            }
                            IconButton(onClick = { /* 设置 */ }) {
                                Icon(Icons.Default.Settings, contentDescription = "设置", tint = Color(0xFF333333))
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFFFFFFFF).copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .background(
                                color = Color(0xFFFFFFFF).copy(alpha = 0.5f),
                                shape = RoundedCornerShape(0.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = Color(0xFFFFFFFF).copy(alpha = 0.7f),
                                shape = RoundedCornerShape(0.dp)
                            )
                    )

                    // 选项卡 - 玻璃拟态
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { /* 切换到未取 */ },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFFFFF).copy(alpha = 0.4f)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFFFFFFF).copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = "未取",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF333333)
                            )
                        }

                        Button(
                            onClick = { /* 已取 */ },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFFFFF).copy(alpha = 0.7f)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFFFFFFF).copy(alpha = 0.8f))
                        ) {
                            Text(
                                text = "已取",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                        }
                    }

                    // 搜索栏 - 玻璃拟态
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            placeholder = { Text("搜索取件码或日期...", color = Color(0xFF8A8A8A)) },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .background(
                                    color = Color(0xFFFFFFFF).copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFFFFFFFF).copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFFFFFFF).copy(alpha = 0.7f),
                                unfocusedContainerColor = Color(0xFFFFFFFF).copy(alpha = 0.5f),
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
                                val currentIndex = filterOptions.indexOf(filterType)
                                val nextIndex = (currentIndex + 1) % filterOptions.size
                                filterType = filterOptions[nextIndex]
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
                                text = "📅 $filterType",
                                fontSize = 14.sp,
                                color = Color(0xFF333333)
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
            } else {
                // 过滤已取快递 - 根据筛选类型
                // 使用 SharedPreferences 状态，而不是 express.status（临时状态）
                val today = LocalDate.now()
                val cutoffDate = when (filterType) {
                    "本月" -> today.minusDays(30)
                    "本周" -> today.minusDays(7)
                    "本日" -> today.minusDays(0)
                    "全部" -> LocalDate.of(2000, 1, 1)  // 显示所有
                    else -> today.minusDays(30)
                }
                
                val filteredList = expressList.filter { express ->
                    // 从 SharedPreferences 读取真实状态
                    val statusKey = "pickup_${express.pickupCode}"
                    val isPicked = statusPrefs.getBoolean(statusKey, express.status == PickupStatus.PICKED)
                    isPicked && try {
                        val expressDate = java.time.LocalDate.parse(express.date)
                        expressDate >= cutoffDate
                    } catch (e: Exception) {
                        true
                    }
                }

                // 按日期分组
                val groupedByDate = filteredList
                    .groupBy { it.date }
                    .toSortedMap(compareBy<String> { it }.reversed())

                // 搜索过滤
                val searchFiltered = if (searchText.isEmpty()) {
                    groupedByDate
                } else {
                    groupedByDate.mapValues { (_, items) ->
                        items.filter { express ->
                            express.pickupCode.contains(searchText, ignoreCase = true) ||
                            express.date.contains(searchText, ignoreCase = true)
                        }
                    }.filterValues { it.isNotEmpty() }
                }

                if (searchFiltered.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text("📦", fontSize = 64.sp, modifier = Modifier.padding(bottom = 16.dp))
                            Text(
                                "暂无已取记录",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF333333),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                "去取件吧",
                                fontSize = 14.sp,
                                color = Color(0xFF8A8A8A)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        searchFiltered.forEach { (date, items) ->
                            item {
                                // 日期分组头
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(4.dp)
                                            .background(
                                                color = Color(0xFF333333),
                                                shape = RoundedCornerShape(2.dp)
                                            )
                                    )
                                    Text(
                                        text = date,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF333333)
                                    )
                                    Text(
                                        text = "(${items.size}件)",
                                        fontSize = 12.sp,
                                        color = Color(0xFF999999)
                                    )
                                }
                            }

                            items(items) { express ->
                                PickedExpressItemCard(express = express)
                            }
                        }
                    }
                }
            }
        }
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
}

/**
 * 已取快递卡片 - 玻璃拟态风格
 */
@Composable
fun PickedExpressItemCard(express: ExpressInfo) {
    val timeStr = try {
        val parts = express.receivedAt.split("T")
        if (parts.size > 1) {
            parts[1].substring(0, 8)  // HH:MM:SS
        } else {
            "未知时间"
        }
    } catch (e: Exception) {
        "未知时间"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFFFFFFF).copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFFFFFFF).copy(alpha = 0.7f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // 取件码 - 左对齐
                Text(
                    text = express.pickupCode,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // 日期和时间 - 左对齐
                Row(
                    modifier = Modifier,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = express.date,
                        fontSize = 12.sp,
                        color = Color(0xFF667EEA),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = timeStr,
                        fontSize = 12.sp,
                        color = Color(0xFF757575)
                    )
                }
            }

            // 状态图标 - 右对齐
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        fontSize = 20.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "已取",
                    fontSize = 11.sp,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
