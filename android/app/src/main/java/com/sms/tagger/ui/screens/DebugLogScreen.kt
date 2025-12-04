package com.sms.tagger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sms.tagger.ui.components.FrostedGlassCard
import com.sms.tagger.ui.components.GradientBackground
import com.sms.tagger.util.InMemoryLogger
import kotlinx.coroutines.launch
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugLogScreen(onBack: () -> Unit) {
    var logs by remember { mutableStateOf(InMemoryLogger.getAllLogs()) }
    var filterTag by remember { mutableStateOf<String?>(null) }
    var filterLevel by remember { mutableStateOf<String?>(null) }
    var searchKeyword by remember { mutableStateOf("") }
    var autoScroll by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    
    // 监听日志更新
    LaunchedEffect(Unit) {
        val listener: (List<InMemoryLogger.LogEntry>) -> Unit = { newLogs ->
            logs = newLogs
            if (autoScroll && newLogs.isNotEmpty()) {
                scope.launch {
                    listState.animateScrollToItem(newLogs.size - 1)
                }
            }
        }
        InMemoryLogger.addListener(listener)
        
        // 初始加载
        logs = InMemoryLogger.getAllLogs()
    }
    
    // 应用过滤
    val filteredLogs = remember(logs, filterTag, filterLevel, searchKeyword) {
        var result = logs
        if (filterTag != null) {
            result = result.filter { it.tag.contains(filterTag!!, ignoreCase = true) }
        }
        if (filterLevel != null) {
            result = result.filter { it.level.equals(filterLevel!!, ignoreCase = true) }
        }
        if (searchKeyword.isNotBlank()) {
            result = result.filter {
                it.message.contains(searchKeyword, ignoreCase = true) ||
                it.tag.contains(searchKeyword, ignoreCase = true)
            }
        }
        result
    }
    
    val stats = remember(logs) { InMemoryLogger.getStats() }
    
    GradientBackground {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("🔍 调试日志", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "返回")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                val exported = InMemoryLogger.exportLogs()
                                clipboardManager.setText(AnnotatedString(exported))
                                Toast.makeText(context, "日志已复制到剪贴板", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, "复制日志")
                        }
                        IconButton(
                            onClick = {
                                InMemoryLogger.clear()
                                logs = emptyList()
                                Toast.makeText(context, "日志已清空", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.Delete, "清空日志")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 统计信息栏
                FrostedGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("总数", fontSize = 11.sp, color = Color(0xFF999999))
                            Text("${logs.size}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        stats.forEach { (level, count) ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    level,
                                    fontSize = 11.sp,
                                    color = when (level) {
                                        "ERROR" -> Color(0xFFFF4444)
                                        "WARN" -> Color(0xFFFFAA00)
                                        "INFO" -> Color(0xFF4488FF)
                                        else -> Color(0xFF999999)
                                    }
                                )
                                Text("$count", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                
                // 过滤栏
                FrostedGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        // 搜索框
                        OutlinedTextField(
                            value = searchKeyword,
                            onValueChange = { searchKeyword = it },
                            label = { Text("搜索关键词（10684、9-5-5038等）") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Search, null) },
                            trailingIcon = {
                                if (searchKeyword.isNotBlank()) {
                                    IconButton(onClick = { searchKeyword = "" }) {
                                        Icon(Icons.Default.Clear, "清除")
                                    }
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.9f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.7f)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 标签和级别过滤
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = filterTag == "SmsReader",
                                onClick = {
                                    filterTag = if (filterTag == "SmsReader") null else "SmsReader"
                                },
                                label = { Text("SmsReader", fontSize = 12.sp) }
                            )
                            FilterChip(
                                selected = filterTag == "ExpressExtractor",
                                onClick = {
                                    filterTag = if (filterTag == "ExpressExtractor") null else "ExpressExtractor"
                                },
                                label = { Text("ExpressExtractor", fontSize = 12.sp) }
                            )
                            FilterChip(
                                selected = filterTag == "ExpressCompanyDetector",
                                onClick = {
                                    filterTag = if (filterTag == "ExpressCompanyDetector") null else "ExpressCompanyDetector"
                                },
                                label = { Text("Detector", fontSize = 12.sp) }
                            )
                            FilterChip(
                                selected = autoScroll,
                                onClick = { autoScroll = !autoScroll },
                                label = { Text("自动滚动", fontSize = 12.sp) }
                            )
                        }
                    }
                }
                
                // 日志列表
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredLogs) { entry ->
                        LogEntryItem(entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun LogEntryItem(entry: InMemoryLogger.LogEntry) {
    val levelColor = when (entry.level) {
        "ERROR" -> Color(0xFFFF4444)
        "WARN" -> Color(0xFFFFAA00)
        "INFO" -> Color(0xFF4488FF)
        else -> Color(0xFF888888)
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.7f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = entry.timeString,
                        fontSize = 10.sp,
                        color = Color(0xFF666666),
                        fontFamily = FontFamily.Monospace
                    )
                    Surface(
                        color = levelColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = entry.level,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            color = levelColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = entry.tag,
                        fontSize = 10.sp,
                        color = Color(0xFF888888),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = entry.message,
                fontSize = 12.sp,
                color = Color(0xFF333333),
                lineHeight = 16.sp
            )
            
            entry.throwable?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it.stackTraceToString().take(200),
                    fontSize = 10.sp,
                    color = Color(0xFFFF0000),
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

