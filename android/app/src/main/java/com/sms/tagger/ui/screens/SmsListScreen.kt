package com.sms.tagger.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.sms.tagger.ui.components.FrostedGlassCard
import com.sms.tagger.ui.components.GradientBackground
import com.sms.tagger.ui.theme.TextSecondary
import com.sms.tagger.util.SmsReader
import com.sms.tagger.util.AppLogger
import java.text.SimpleDateFormat
import java.util.*

/**
 * 短信列表页面
 * @param onBack 返回回调（可选）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsListScreen(
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var smsCreateList by remember { mutableStateOf<List<com.sms.tagger.data.model.SmsCreate>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchText by remember { mutableStateOf("") }
    var appliedSearchText by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    
    // 加载短信 - 从本地读取
    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        try {
            // 检查权限
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_SMS
            ) == PackageManager.PERMISSION_GRANTED
            
            if (!hasPermission) {
                errorMessage = "没有短信读取权限，请在设置中授予权限"
                isLoading = false
                return@LaunchedEffect
            }
            
            val smsReader = SmsReader(context)
            
            // 先检查是否能访问短信
            if (!smsReader.hasPermission()) {
                errorMessage = "无法访问短信，请检查权限设置"
                isLoading = false
                return@LaunchedEffect
            }
            
            // 读取所有短信（增加数量限制，确保显示完整）
            AppLogger.d("SmsListScreen", "========== 开始加载短信列表 ==========")
            
            // 增加读取数量限制到50000条，确保能读取到所有短信（包括运营商短信等）
            // 如果没有标签过滤，显示所有短信；否则只显示指定标签的短信
            val allSms = smsReader.readAllSms(50000)
            
            AppLogger.d("SmsListScreen", "✅ 读取到 ${allSms.size} 条短信")
            
            // 打印前3条短信的详细信息
            allSms.take(3).forEachIndexed { index, sms ->
                AppLogger.d("SmsListScreen", "短信 ${index + 1}: 发件人=${sms.sender}, 内容=${sms.content.take(50)}, 时间=${sms.receivedAt}")
            }
            
            AppLogger.d("SmsListScreen", "读取后短信数: ${allSms.size} 条")
            
            // 数据库查询已经按 DATE DESC, _ID DESC 排序，理论上无需再次排序
            // 但为了确保顺序正确，保留排序作为安全措施（使用更高效的时间戳比较）
            val sortedSms = allSms.sortedByDescending { sms ->
                try {
                    // 解析ISO日期字符串为时间戳进行比较（比字符串比较更准确）
                    java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                        .parse(sms.receivedAt)?.time ?: 0L
                } catch (e: Exception) {
                    0L  // 解析失败时放在最后
                }
            }
            
            AppLogger.d("SmsListScreen", "排序后短信数: ${sortedSms.size} 条")
            
            // 临时调整：只显示最近7天的短信，用于检测短信是否有缺失
            val sevenDaysAgo = java.time.LocalDate.now().minusDays(7)
            val recentSms = sortedSms.filter { sms ->
                try {
                    val smsDate = java.time.LocalDate.parse(sms.receivedAt.substring(0, 10))
                    smsDate >= sevenDaysAgo
                } catch (e: Exception) {
                    AppLogger.w("SmsListScreen", "⚠️ 日期解析失败: ${sms.receivedAt}")
                    true  // 日期解析失败时保留该短信
                }
            }
            
            AppLogger.d("SmsListScreen", "最近7天的短信数: ${recentSms.size} 条（总共 ${sortedSms.size} 条）")
            AppLogger.d("SmsListScreen", "过滤时间范围: $sevenDaysAgo 至今")
            
            // 统计最近7天内各发件人的短信数
            val senderStats = recentSms.groupingBy { it.sender }.eachCount()
            AppLogger.d("SmsListScreen", "最近7天发件人统计:")
            senderStats.forEach { (sender, count) ->
                AppLogger.d("SmsListScreen", "  - $sender: $count 条")
            }
            
            // 打印排序后的前3条短信
            recentSms.take(3).forEachIndexed { index, sms ->
                AppLogger.d("SmsListScreen", "最近7天短信 ${index + 1}: 发件人=${sms.sender}, 时间=${sms.receivedAt}")
            }
            
            // 搜索目标短信（用于调试）- 只在未找到时记录警告
            val targetSms = recentSms.filter { 
                it.content.contains("菜鸟驿站", ignoreCase = true) && 
                (it.content.contains("1-4-4011") || it.content.contains("凭1-4-4011"))
            }
            if (targetSms.isNotEmpty()) {
                AppLogger.w("SmsListScreen", "🔍 在最近7天的短信中找到 ${targetSms.size} 条目标短信（包含'菜鸟驿站'和'1-4-4011'）")
                // 只记录第一条目标短信的详细信息
                targetSms.firstOrNull()?.let { sms ->
                    AppLogger.w("SmsListScreen", "  目标短信: 发件人=${sms.sender}, 内容=${sms.content.take(80)}, 时间=${sms.receivedAt}")
                }
            } else {
                AppLogger.w("SmsListScreen", "⚠️ 在最近7天的 ${recentSms.size} 条短信中未找到目标短信")
                // 只统计数量，不列出所有短信（减少日志量）
                val cainiaoSms = recentSms.filter { it.content.contains("菜鸟驿站", ignoreCase = true) }
            AppLogger.d("SmsListScreen", "最近7天内包含'菜鸟驿站'的短信共 ${cainiaoSms.size} 条")
            }
            
            smsCreateList = recentSms
            
            AppLogger.d("SmsListScreen", "========== 短信列表加载完成 ==========")
            
            if (smsCreateList.isEmpty()) {
                errorMessage = "暂无短信"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            errorMessage = "加载失败: ${e.message}"
            AppLogger.e("SmsListScreen", "加载短信错误", e)
        } finally {
            isLoading = false
        }
    }
    
    GradientBackground {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { 
                        Text("短信列表") 
                    },
                    navigationIcon = {
                        if (onBack != null) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                if (!showSearchBar) {
                                    showSearchBar = true
                                } else {
                                    showSearchBar = false
                                    searchText = ""
                                    appliedSearchText = ""
                                    focusManager.clearFocus()
                                }
                            }
                        ) {
                            val icon = if (showSearchBar) Icons.Default.Close else Icons.Default.Search
                            val desc = if (showSearchBar) "关闭搜索" else "搜索"
                            Icon(icon, contentDescription = desc)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 搜索栏
                if (showSearchBar) {
                    TextField(
                        value = searchText,
                        onValueChange = { 
                            searchText = it
                            if (it.isBlank()) {
                                appliedSearchText = ""
                            }
                        },
                        placeholder = { Text("搜索短信内容或发件人") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                appliedSearchText = searchText
                                focusManager.clearFocus()
                            }
                        ),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    appliedSearchText = searchText
                                    focusManager.clearFocus()
                                }
                            ) {
                                Icon(Icons.Default.Search, contentDescription = "执行搜索")
                            }
                        }
                    )
                }
                
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(
                        modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = errorMessage ?: "加载失败",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFFF6B6B),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = "提示：请确保已在系统设置中授予短信读取权限",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
                } else {
                    // 应用搜索过滤
                    val filteredSmsList = if (appliedSearchText.isNotBlank()) {
                        smsCreateList.filter { sms ->
                            sms.content.contains(appliedSearchText, ignoreCase = true) ||
                            sms.sender.contains(appliedSearchText, ignoreCase = true)
                        }
                    } else {
                        smsCreateList
                    }
                    
                    if (filteredSmsList.isEmpty() && appliedSearchText.isNotBlank()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "未找到匹配的短信",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary
                            )
                        }
                    } else if (filteredSmsList.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无短信",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
            }
        } else {
            LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                            items(filteredSmsList) { sms ->
                    SmsItemCard(
                        sender = sms.sender,
                        content = sms.content,
                        time = formatTime(sms.receivedAt)
                    )
                            }
                        }
                }
            }
        }
        }
    }
}

/**
 * 格式化时间显示
 */
fun formatTime(isoTime: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = sdf.parse(isoTime)
        val now = Date()
        val diff = now.time - (date?.time ?: 0)
        
        when {
            diff < 60 * 1000 -> "刚刚"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}分钟前"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}小时前"
            else -> {
                val displayFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                displayFormat.format(date ?: Date())
            }
        }
    } catch (e: Exception) {
        isoTime
    }
}

@Composable
fun SmsItemCard(
    sender: String,
    content: String,
    time: String
) {
    FrostedGlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = sender,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = time,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            maxLines = 2
        )
    }
}
