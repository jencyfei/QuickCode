package com.sms.tagger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sms.tagger.ui.components.GradientBackground
import com.sms.tagger.util.LogFileWriter
import java.io.File
import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * 日志查看页面
 * 用于查看和导出应用日志
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogViewerScreen(onBack: (() -> Unit)? = null) {
    val context = LocalContext.current
    val logFileWriter = remember { LogFileWriter(context) }
    
    var logFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    var selectedLogFile by remember { mutableStateOf<File?>(null) }
    var logContent by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // 加载日志文件列表
    LaunchedEffect(Unit) {
        logFiles = logFileWriter.getLogFiles()
        if (logFiles.isNotEmpty()) {
            selectedLogFile = logFiles.first()
            logContent = logFileWriter.getLogContent(logFiles.first().name)
        }
    }
    
    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("日志查看器") },
                    navigationIcon = {
                        if (onBack != null) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                            }
                        }
                    },
                    actions = {
                        // 删除按钮
                        IconButton(
                            onClick = { showDeleteDialog = true }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "清空日志")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 左侧：日志文件列表
                Column(
                    modifier = Modifier
                        .width(200.dp)
                        .fillMaxHeight()
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "日志文件 (${logFiles.size})",
                        fontSize = 14.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        modifier = Modifier.padding(8.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(logFiles) { file ->
                            Button(
                                onClick = {
                                    selectedLogFile = file
                                    logContent = logFileWriter.getLogContent(file.name)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedLogFile == file) {
                                        Color(0xFF667EEA)
                                    } else {
                                        Color.White.copy(alpha = 0.3f)
                                    }
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = file.name.replace("sms_agent_", "").replace(".log", ""),
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
                
                // 右侧：日志内容
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    if (logFiles.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("暂无日志文件")
                        }
                    } else {
                        // 日志文件信息
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "文件: ${selectedLogFile?.name ?: ""}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF666666)
                                )
                                Text(
                                    text = "大小: ${formatFileSize(selectedLogFile?.length() ?: 0)}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF666666)
                                )
                            }
                            
                            // 导出按钮
                            Button(
                                onClick = {
                                    // TODO: 实现导出功能
                                },
                                modifier = Modifier.height(36.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "导出",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("导出", fontSize = 12.sp)
                            }
                        }
                        
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        // 日志内容显示
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    color = Color(0xFF1E1E1E),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(logContent.split("\n")) { line ->
                                    if (line.isNotEmpty()) {
                                        Text(
                                            text = line,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = when {
                                                line.contains("[ERROR]") || line.contains("❌") -> Color(0xFFFF6B6B)
                                                line.contains("[WARN]") || line.contains("⚠️") -> Color(0xFFFFD93D)
                                                line.contains("[DEBUG]") || line.contains("✅") -> Color(0xFF6BCB77)
                                                else -> Color(0xFFCCCCCC)
                                            },
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("清空所有日志") },
            text = { Text("确定要删除所有日志文件吗？此操作无法撤销。") },
            confirmButton = {
                Button(
                    onClick = {
                        logFileWriter.clearAllLogs()
                        logFiles = emptyList()
                        selectedLogFile = null
                        logContent = ""
                        showDeleteDialog = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 格式化文件大小
 */
private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${bytes / (1024 * 1024)} MB"
    }
}
