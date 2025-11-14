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
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.sms.tagger.ui.viewmodel.ExpressViewModel
import com.sms.tagger.util.ExpressGroupByDate
import kotlinx.coroutines.flow.collectLatest

/**
 * 快递信息页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressScreen(viewModel: ExpressViewModel = viewModel()) {
    val context = LocalContext.current
    var expressList by remember { mutableStateOf<List<ExpressInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showRuleManager by remember { mutableStateOf(false) }
    val pickupStatusMap by viewModel.pickupStatusMap.collectAsState()
    
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
                // 按日期分组
                val groupedByDate = expressList
                    .groupBy { it.date }
                    .map { (date, items) ->
                        ExpressGroupByDate(
                            date = date,
                            count = items.size,
                            expressList = items.sortedByDescending { it.receivedAt }
                        )
                    }
                    // 按日期倒序排序（使用 receivedAt 进行正确的日期比较）
                    .sortedByDescending { group ->
                        // 从分组中的第一条快递的 receivedAt 提取日期进行排序
                        group.expressList.firstOrNull()?.receivedAt ?: ""
                    }
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 日期分组
                    items(groupedByDate.size) { index ->
                        DateGroupItem(
                            group = groupedByDate[index],
                            viewModel = viewModel,
                            pickupStatusMap = pickupStatusMap,
                            onExpandChange = { expanded ->
                                groupedByDate[index].isExpanded = expanded
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DateGroupItem(
    group: ExpressGroupByDate,
    viewModel: ExpressViewModel,
    pickupStatusMap: Map<String, Boolean>,
    onExpandChange: (Boolean) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var isExpanded by remember { mutableStateOf(group.isExpanded) }
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 日期分组标题
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    isExpanded = !isExpanded
                    onExpandChange(isExpanded)
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 折叠/展开箭头 + 日期 + 数量
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isExpanded) "▼" else "▶",
                        fontSize = 14.sp,
                        color = Color(0xFF333333),
                        modifier = Modifier.width(20.dp)
                    )
                    Text(
                        text = group.date,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = "（${group.count}件）",
                        fontSize = 12.sp,
                        color = Color(0xFF8A8A8A)
                    )
                }
                
                // 操作按钮
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = {
                            val allCodes = group.expressList.map { it.pickupCode }.joinToString("\n")
                            clipboardManager.setText(AnnotatedString(allCodes))
                        },
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.4f)
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        Text("📋 复制全部", fontSize = 10.sp, color = Color(0xFF333333))
                    }
                    
                    Button(
                        onClick = {
                            group.expressList.forEach { express ->
                                viewModel.updatePickupStatus(express.pickupCode, true)
                            }
                        },
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.4f)
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        Text("✓ 全部已取", fontSize = 10.sp, color = Color(0xFF333333))
                    }
                }
            }
        }
        
        // 快递卡片列表（展开时显示）
        if (isExpanded) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                group.expressList.forEach { express ->
                    ExpressItemCard(express, viewModel, pickupStatusMap)
                }
            }
        }
    }
}

@Composable
fun ExpressItemCard(
    express: ExpressInfo,
    viewModel: ExpressViewModel,
    pickupStatusMap: Map<String, Boolean>
) {
    val clipboardManager = LocalClipboardManager.current
    // 从 ViewModel 获取状态，如果没有则使用默认值
    val isPicked = pickupStatusMap[express.pickupCode] ?: (express.status == PickupStatus.PICKED)
    
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
                            // 更新 ViewModel 中的状态
                            viewModel.updatePickupStatus(express.pickupCode, true)
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
