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
import androidx.compose.ui.unit.sp
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
                errorMessage = "无法访问短信数据库，请检查是否已授予短信读取权限"
                isLoading = false
                return@LaunchedEffect
            }
            
            // 读取所有短信（增加数量限制，避免一次性读取过多）
            AppLogger.d("SmsListScreen", "========== 开始加载短信列表 ==========")
            
            // 默认限制最多读取 5000 条，已足够覆盖最近较长时间的短信记录
            val allSms = smsReader.readAllSms(5000)
            
            AppLogger.d("SmsListScreen", "✅ 读取到 ${allSms.size} 条短信")
            
            // 如果权限已授予但读取不到短信，给出更明确的提示
            if (allSms.isEmpty()) {
                AppLogger.w("SmsListScreen", "⚠️ 权限已授予但未读取到任何短信，可能是设备上没有短信数据或需要设置为默认短信应用")
            }
            
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
            
            // 显示所有短信（不再限制为7天），确保能显示所有读取到的短信，包括10684开头的短信
            smsCreateList = sortedSms
            
            // 统计各发件人的短信数（包括10684开头）
            val senderStats = sortedSms.groupingBy { it.sender }.eachCount()
            val count10684 = sortedSms.count { it.sender.startsWith("10684") || it.sender.contains("10684") }
            AppLogger.d("SmsListScreen", "发件人统计:")
            AppLogger.d("SmsListScreen", "  - 10684开头发件人: $count10684 条")
            senderStats.filter { it.key.startsWith("10684") || it.key.contains("10684") }.forEach { (sender, count) ->
                AppLogger.d("SmsListScreen", "  - $sender: $count 条")
            }
            
            // 打印排序后的前10条短信（用于调试）
            sortedSms.take(10).forEachIndexed { index, sms ->
                AppLogger.d("SmsListScreen", "短信 ${index + 1}: 发件人=${sms.sender}, 时间=${sms.receivedAt}")
            }
            
            // 检查10684开头的短信（用于调试）
            val sms10684 = sortedSms.filter { 
                val sender = it.sender
                sender.startsWith("10684") || sender.contains("10684") || sender.matches(Regex(".*10684.*"))
            }
            AppLogger.w("SmsListScreen", "========== 10684短信检查结果 ==========")
            AppLogger.w("SmsListScreen", "总短信数: ${sortedSms.size}")
            AppLogger.w("SmsListScreen", "10684开头的短信数: ${sms10684.size}")
            if (sms10684.isNotEmpty()) {
                AppLogger.w("SmsListScreen", "🔍 找到 ${sms10684.size} 条10684开头的短信:")
                sms10684.take(10).forEachIndexed { index, sms ->
                    AppLogger.w("SmsListScreen", "  10684短信 ${index + 1}: 发件人='${sms.sender}', 时间=${sms.receivedAt}, 内容=${sms.content.take(100)}")
                }
            } else {
                AppLogger.w("SmsListScreen", "⚠️ 未找到10684开头的短信")
                // 检查是否有类似的发件人
                val similarSenders = sortedSms.map { it.sender }.distinct().filter { 
                    it.contains("106") || it.contains("84") || it.length > 10 
                }.take(20)
                AppLogger.w("SmsListScreen", "相似的发送人（包含106或84的）: ${similarSenders.joinToString(", ")}")
            }
            AppLogger.w("SmsListScreen", "=====================================")
            
            AppLogger.d("SmsListScreen", "========== 短信列表加载完成 ==========")
            
            // 改进空列表提示信息
            if (smsCreateList.isEmpty()) {
                if (allSms.isEmpty()) {
                    errorMessage = "未读取到任何短信\n\n可能的原因：\n1. 设备上没有短信数据\n2. 需要在系统设置中授予完整的短信读取权限\n3. 某些设备可能需要将应用设置为默认短信应用"
                } else {
                    errorMessage = "未读取到任何短信\n\n共尝试读取 ${allSms.size} 条短信，但筛选后为空"
                }
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
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFF6B6B),
                        modifier = Modifier.padding(bottom = 8.dp),
                        lineHeight = 20.sp
                    )
                    // 只在权限相关错误时显示提示
                    if (errorMessage?.contains("权限") == true || errorMessage?.contains("无法访问") == true) {
                    Text(
                        text = "提示：请确保已在系统设置中授予短信读取权限",
                        style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 8.dp)
                    )
                    }
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
                    
                    // 诊断：检查10684短信是否在显示列表中
                    val displayed10684 = filteredSmsList.filter { 
                        val sender = it.sender
                        sender.startsWith("10684") || sender.contains("10684") || sender.matches(Regex(".*10684.*", RegexOption.IGNORE_CASE))
                    }
                    if (displayed10684.isNotEmpty() || filteredSmsList.size > 0) {
                        AppLogger.d("SmsListScreen", "显示列表统计: 总计 ${filteredSmsList.size} 条，其中10684短信 ${displayed10684.size} 条")
                        if (displayed10684.isNotEmpty()) {
                            AppLogger.w("SmsListScreen", "✅ 10684短信已包含在显示列表中")
                            displayed10684.take(3).forEachIndexed { index, sms ->
                                AppLogger.w("SmsListScreen", "  显示的第${index+1}条10684短信: 发件人='${sms.sender}', 内容=${sms.content.take(50)}")
                            }
                        }
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
