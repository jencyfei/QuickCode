package com.sms.tagger.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.sms.tagger.data.model.SmsCreate
import com.sms.tagger.ui.components.FrostedGlassCard
import com.sms.tagger.ui.components.GradientBackground
import com.sms.tagger.ui.theme.TextSecondary
import com.sms.tagger.util.SmsReader
import com.sms.tagger.util.AppLogger
import com.sms.tagger.util.SmsListCache
import com.sms.tagger.util.TimeWindowSettings
import com.sms.tagger.util.LogControlSettings
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

private const val INITIAL_SMS_BATCH = 200
private const val SMS_BATCH_STEP = 200
private const val SMS_LIST_READ_LIMIT = 5000
private const val SMS_INCREMENTAL_FETCH_LIMIT = 400

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
    var smsCreateList by remember { mutableStateOf<List<SmsCreate>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchText by remember { mutableStateOf("") }
    var appliedSearchText by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    var smsRecentDays by remember { mutableStateOf(TimeWindowSettings.getSmsDays(context)) }
    
    LaunchedEffect(Unit) {
        AppLogger.setVerboseOverride(LogControlSettings.isVerboseLoggingEnabled(context))
        smsRecentDays = TimeWindowSettings.getSmsDays(context)
    }
    
    // 加载短信 - 从本地读取
    LaunchedEffect(smsRecentDays) {
        if (smsCreateList.isEmpty()) {
        isLoading = true
        }
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
            val latestMeta = smsReader.getLatestSmsMeta()
            val latestTimestamp = latestMeta?.timestamp
            val latestSmsId = latestMeta?.id
            SmsListCache.getIfFresh(latestTimestamp, latestSmsId)?.let {
                smsCreateList = it
                isLoading = false
                return@LaunchedEffect
            }
            
            // 先检查是否能访问短信
            if (!smsReader.hasPermission()) {
                errorMessage = "无法访问短信数据库，请检查是否已授予短信读取权限"
                isLoading = false
                return@LaunchedEffect
            }
            
            val cacheSnapshot = SmsListCache.getSnapshot()
            if (
                cacheSnapshot != null &&
                latestSmsId != null &&
                cacheSnapshot.latestSmsId != null &&
                latestSmsId > cacheSnapshot.latestSmsId
            ) {
                val incrementalSms = smsReader.readSmsAfterId(cacheSnapshot.latestSmsId, SMS_INCREMENTAL_FETCH_LIMIT)
                if (incrementalSms.isNotEmpty() && incrementalSms.size < SMS_INCREMENTAL_FETCH_LIMIT) {
                    val mergedList = mergeAndFilterSms(
                        incrementalSms,
                        cacheSnapshot.list,
                        smsRecentDays
                    )
                    smsCreateList = mergedList
                    val newestTimestamp = mergedList.firstOrNull()?.receivedAt ?: latestTimestamp
                    SmsListCache.update(mergedList, newestTimestamp, latestSmsId)
                    AppLogger.debug("SmsListScreen") {
                        "🔁 增量加载短信 ${incrementalSms.size} 条 -> 列表 ${mergedList.size} 条（lastId=${cacheSnapshot.latestSmsId} -> $latestSmsId）"
                    }
                    isLoading = false
                    return@LaunchedEffect
                } else if (incrementalSms.size >= SMS_INCREMENTAL_FETCH_LIMIT) {
                    AppLogger.w(
                        "SmsListScreen",
                        "⚠️ 增量短信达到 ${SMS_INCREMENTAL_FETCH_LIMIT} 条上限，回退全量加载"
                    )
                }
            }
            
            // 读取所有短信（增加数量限制，避免一次性读取过多）
            AppLogger.debug("SmsListScreen") { "========== 开始加载短信列表 ==========" }
            
            // 默认限制最多读取指定条数，已足够覆盖最近较长时间的短信记录
            val allSms = smsReader.readAllSms(SMS_LIST_READ_LIMIT)
            
            AppLogger.debug("SmsListScreen") { "✅ 读取到 ${allSms.size} 条短信" }
            
            // 如果权限已授予但读取不到短信，给出更明确的提示
            if (allSms.isEmpty()) {
                AppLogger.debug("SmsListScreen") { "⚠️ 权限已授予但未读取到任何短信，可能是设备上没有短信数据或需要设置为默认短信应用" }
            }
            
            // 打印前3条短信的详细信息
            AppLogger.debug("SmsListScreen") {
                buildString {
            allSms.take(3).forEachIndexed { index, sms ->
                        append("短信 ${index + 1}: 发件人=${sms.sender}, 内容=${sms.content.take(50)}, 时间=${sms.receivedAt}\n")
                    }
                }
            }
            
            AppLogger.debug("SmsListScreen") { "读取后短信数: ${allSms.size} 条" }
            
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
            
            AppLogger.debug("SmsListScreen") { "排序后短信数: ${sortedSms.size} 条" }
            
            val filteredByDate = mergeAndFilterSms(sortedSms, emptyList(), smsRecentDays)
            smsCreateList = filteredByDate
            val newestTimestamp = filteredByDate.firstOrNull()?.receivedAt ?: latestTimestamp
            SmsListCache.update(filteredByDate, newestTimestamp, latestSmsId)
            
            if (AppLogger.isVerboseEnabled()) {
                val senderStats = filteredByDate.groupingBy { it.sender }.eachCount()
                val count10684 = filteredByDate.count { it.sender.startsWith("10684") || it.sender.contains("10684") }
            AppLogger.d("SmsListScreen", "发件人统计:")
            AppLogger.d("SmsListScreen", "  - 10684开头发件人: $count10684 条")
            senderStats.filter { it.key.startsWith("10684") || it.key.contains("10684") }.forEach { (sender, count) ->
                AppLogger.d("SmsListScreen", "  - $sender: $count 条")
            }
                filteredByDate.take(10).forEachIndexed { index, sms ->
                AppLogger.d("SmsListScreen", "短信 ${index + 1}: 发件人=${sms.sender}, 时间=${sms.receivedAt}")
            }
            
                val sms10684 = filteredByDate.filter { 
                val sender = it.sender
                sender.startsWith("10684") || sender.contains("10684") || sender.matches(Regex(".*10684.*"))
            }
                AppLogger.d("SmsListScreen", "========== 10684短信检查结果 ==========")
                AppLogger.d("SmsListScreen", "总短信数: ${sortedSms.size}")
                AppLogger.d("SmsListScreen", "10684开头的短信数: ${sms10684.size}")
            if (sms10684.isNotEmpty()) {
                    AppLogger.d("SmsListScreen", "🔍 找到 ${sms10684.size} 条10684开头的短信:")
                sms10684.take(10).forEachIndexed { index, sms ->
                        AppLogger.d("SmsListScreen", "  10684短信 ${index + 1}: 发件人='${sms.sender}', 时间=${sms.receivedAt}, 内容=${sms.content.take(100)}")
                }
            } else {
                    AppLogger.d("SmsListScreen", "⚠️ 未找到10684开头的短信")
                val similarSenders = sortedSms.map { it.sender }.distinct().filter { 
                    it.contains("106") || it.contains("84") || it.length > 10 
                }.take(20)
                    AppLogger.d("SmsListScreen", "相似的发送人（包含106或84的）: ${similarSenders.joinToString(", ")}")
                }
                AppLogger.d("SmsListScreen", "=====================================")
                AppLogger.d("SmsListScreen", "========== 短信列表加载完成 ==========")
            }
            
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
    
    val listState = rememberLazyListState()
    var visibleCount by remember { mutableStateOf(INITIAL_SMS_BATCH) }
    
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
                
                val nextSmsOption = TimeWindowSettings.smsOptions().firstOrNull { it > smsRecentDays }
                Text(
                    text = "当前仅展示最近 ${smsRecentDays} 天的短信记录，可在设置页或下方按钮中调整。",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                if (nextSmsOption != null) {
                    TextButton(
                        onClick = {
                            TimeWindowSettings.setSmsDays(context, nextSmsOption)
                            smsRecentDays = nextSmsOption
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("加载更多历史（扩展到近 ${nextSmsOption} 天）")
                    }
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
                    
                    LaunchedEffect(filteredSmsList.size) {
                        visibleCount = minOf(
                            filteredSmsList.size,
                            visibleCount.coerceAtLeast(INITIAL_SMS_BATCH)
                        )
                    }
                    
                    LaunchedEffect(listState, filteredSmsList.size) {
                        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                            .distinctUntilChanged()
                            .collectLatest { lastVisible ->
                                if (lastVisible != null &&
                                    lastVisible >= visibleCount - 20 &&
                                    visibleCount < filteredSmsList.size
                                ) {
                                    visibleCount = (visibleCount + SMS_BATCH_STEP).coerceAtMost(filteredSmsList.size)
                                }
                            }
                    }
                    
                    // 诊断：检查10684短信是否在显示列表中
                    val displayed10684 = filteredSmsList.filter { 
                        val sender = it.sender
                        sender.startsWith("10684") || sender.contains("10684") || sender.matches(Regex(".*10684.*", RegexOption.IGNORE_CASE))
                    }
                    if (AppLogger.isVerboseEnabled() && (displayed10684.isNotEmpty() || filteredSmsList.isNotEmpty())) {
                        AppLogger.d("SmsListScreen", "显示列表统计: 总计 ${filteredSmsList.size} 条，其中10684短信 ${displayed10684.size} 条")
                            displayed10684.take(3).forEachIndexed { index, sms ->
                            AppLogger.d("SmsListScreen", "  显示的第${index + 1}条10684短信: 发件人='${sms.sender}', 内容=${sms.content.take(50)}")
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
            val displayedList = filteredSmsList.take(visibleCount)
            LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                state = listState
            ) {
                items(displayedList) { sms ->
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

private fun mergeAndFilterSms(
    newItems: List<SmsCreate>,
    existingItems: List<SmsCreate>,
    recentDays: Long
): List<SmsCreate> {
    if (newItems.isEmpty() && existingItems.isEmpty()) {
        return emptyList()
    }
    val combined = newItems + existingItems
    val dedupMap = LinkedHashMap<String, SmsCreate>()
    combined.forEach { sms ->
        val key = sms.dedupKey()
        if (!dedupMap.containsKey(key)) {
            dedupMap[key] = sms
        }
    }
    val sorted = dedupMap.values.sortedByDescending { it.receivedAt.toEpochMillisOrNull() ?: 0L }
    val cutoffMillis = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(recentDays)
    return sorted.filter { sms ->
        sms.receivedAt.toEpochMillisOrNull()?.let { it >= cutoffMillis } ?: true
    }
}

private fun SmsCreate.dedupKey(): String {
    return buildString {
        append(sender)
        append("|")
        append(receivedAt)
        append("|")
        append(content.hashCode())
    }
}

private fun String.toEpochMillisOrNull(): Long? {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        sdf.parse(this)?.time
    } catch (_: Exception) {
        null
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
