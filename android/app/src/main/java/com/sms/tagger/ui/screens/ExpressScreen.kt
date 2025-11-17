package com.sms.tagger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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

/**
 * 快递信息页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressScreen() {
    val context = LocalContext.current
    var expressList by remember { mutableStateOf<List<ExpressInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showRuleManager by remember { mutableStateOf(false) }
    
    // 如果显示规则管理，则显示规则管理页面
    if (showRuleManager) {
        RuleManageScreen(onBack = { showRuleManager = false })
        return
    }
    
    // 加载快递信息
    LaunchedEffect(Unit) {
        try {
            val reader = SmsReader(context)
            val smsList = reader.readLatestSms(200)
            expressList = ExpressExtractor.extractAllExpressInfo(smsList)
            isLoading = false
        } catch (e: Exception) {
            e.printStackTrace()
            isLoading = false
        }
    }
    
    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("快递取件码") },
                    actions = {
                        IconButton(
                            onClick = { showRuleManager = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "取件码规则配置",
                                tint = Color(0xFF333333)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
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
                // 按日期分组，然后按日期倒序排列
                val groupedByDate = expressList
                    .groupBy { it.date }  // 按日期分组
                    .toSortedMap(compareBy<String> { it }.reversed())  // 日期倒序
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // 日期分组
                    groupedByDate.forEach { (date, expressItems) ->
                        item {
                            // 同一天内按取件码顺序排列
                            val sortedItems = expressItems.sortedBy { it.pickupCode }
                            DateGroup(date, sortedItems)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DateGroup(date: String, expressItems: List<ExpressInfo>) {
    val clipboardManager = LocalClipboardManager.current
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 日期头部
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // 日期 + 快递数量
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    Text(
                        text = date,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF333333)
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFF667EEA).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${expressItems.size}件",
                            fontSize = 12.sp,
                            color = Color(0xFF8A8A8A)
                        )
                    }
                }
            }
            
            // 操作按钮
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        // 复制所有取件码，用换行符分隔
                        val allCodes = expressItems.map { it.pickupCode }.joinToString("\n")
                        clipboardManager.setText(AnnotatedString(allCodes))
                    },
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.5f)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f))
                ) {
                    Text("📋 复制全部", fontSize = 11.sp, color = Color(0xFF333333))
                }
                
                Button(
                    onClick = {
                        // 标记所有快递为已取
                        expressItems.forEach { express ->
                            // 这里应该更新状态并持久化
                        }
                    },
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.5f)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f))
                ) {
                    Text("✓ 全部已取", fontSize = 11.sp, color = Color(0xFF333333))
                }
            }
        }
        
        // 快递卡片列表
        expressItems.forEach { express ->
            ExpressItemCard(express)
        }
    }
}

@Composable
fun LocationGroup(location: String, expressItems: List<ExpressInfo>) {
    val clipboardManager = LocalClipboardManager.current
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 地点头部
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // 地点名称 + 快递数量
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    Text(
                        text = location,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF333333)
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFF667EEA).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${expressItems.size}件",
                            fontSize = 12.sp,
                            color = Color(0xFF8A8A8A)
                        )
                    }
                }
                // 地址信息 - 只显示一次
                val addressText = expressItems.firstOrNull()?.location
                if (addressText != null && addressText != location) {
                    Text(
                        text = addressText,
                        fontSize = 12.sp,
                        color = Color(0xFF8A8A8A),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            
            // 操作按钮
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        // 复制所有取件码，用换行符分隔
                        val allCodes = expressItems.map { it.pickupCode }.joinToString("\n")
                        clipboardManager.setText(AnnotatedString(allCodes))
                    },
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.5f)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f))
                ) {
                    Text("📋 复制全部", fontSize = 11.sp, color = Color(0xFF333333))
                }
                
                Button(
                    onClick = {
                        // 标记所有快递为已取（通过状态更新）
                        // 注：实际应用中应该保存到数据库
                        expressItems.forEach { express ->
                            // 这里应该更新状态，但当前实现中状态是本地的
                        }
                    },
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.5f)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f))
                ) {
                    Text("✓ 全部已取", fontSize = 11.sp, color = Color(0xFF333333))
                }
            }
        }
        
        // 快递卡片列表
        expressItems.forEach { express ->
            ExpressItemCard(express)
        }
    }
}

@Composable
fun ExpressItemCard(express: ExpressInfo) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    
    // 从 SharedPreferences 读取状态
    val sharedPref = context.getSharedPreferences("express_status", android.content.Context.MODE_PRIVATE)
    val statusKey = "pickup_${express.pickupCode}"
    var isPicked by remember { 
        mutableStateOf(sharedPref.getBoolean(statusKey, express.status == PickupStatus.PICKED))
    }
    
    // 根据状态确定颜色
    val statusColor = when {
        isPicked -> Color(0xFF4CAF50)      // 绿色 - 已取
        express.status == PickupStatus.EXPIRED -> Color(0xFFFF9800)     // 橙色 - 已过期
        else -> Color(0xFF667EEA)     // 蓝色 - 未取
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.7f),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPicked) 
                Color(0xFF4CAF50).copy(alpha = 0.1f)
            else 
                Color.White.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 顶部：操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(express.pickupCode))
                        },
                        modifier = Modifier
                            .height(32.dp)
                            .weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF667EEA).copy(alpha = 0.2f)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF667EEA).copy(alpha = 0.3f)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text("📋 复制", fontSize = 12.sp, color = Color(0xFF667EEA))
                    }
                    
                    Button(
                        onClick = { 
                            isPicked = true
                            // 保存状态到 SharedPreferences
                            sharedPref.edit().putBoolean(statusKey, true).apply()
                        },
                        enabled = !isPicked,
                        modifier = Modifier
                            .height(32.dp)
                            .weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF667EEA).copy(alpha = 0.2f),
                            disabledContainerColor = Color(0xFF4CAF50).copy(alpha = 0.2f)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isPicked) Color(0xFF4CAF50).copy(alpha = 0.3f)
                            else Color(0xFF667EEA).copy(alpha = 0.3f)
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(
                            if (isPicked) "✓ 已取" else "取出",
                            fontSize = 12.sp,
                            color = if (isPicked) Color(0xFF4CAF50) else Color(0xFF667EEA)
                        )
                    }
                }
                
                // 取件码区域（大字显示）
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFF667EEA).copy(alpha = 0.08f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "PICKUP CODE",
                            fontSize = 10.sp,
                            color = Color(0xFF8A8A8A),
                            letterSpacing = 0.1.sp
                        )
                        Text(
                            text = express.pickupCode,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333),
                            letterSpacing = 2.sp
                        )
                    }
                }
                
                // 取件日期
                if (express.date.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "取件日期：",
                            fontSize = 13.sp,
                            color = Color(0xFF8A8A8A),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = express.date,
                            fontSize = 13.sp,
                            color = Color(0xFF333333),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                // 接收时间 - 只显示时分秒
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "接收时间：",
                        fontSize = 13.sp,
                        color = Color(0xFF8A8A8A),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = express.receivedAt.let { time ->
                            // 提取时分秒部分 (HH:MM:SS)
                            val parts = time.split(" ")
                            if (parts.size >= 2) {
                                parts[1]  // 取时间部分
                            } else {
                                time
                            }
                        },
                        fontSize = 13.sp,
                        color = Color(0xFF333333)
                    )
                }
            }
            
            // 右上角状态圆点
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = statusColor,
                        shape = RoundedCornerShape(50)
                    )
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )
        }
    }
}
