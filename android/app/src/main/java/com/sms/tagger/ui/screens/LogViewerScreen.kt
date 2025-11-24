package com.sms.tagger.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import com.sms.tagger.ui.theme.TextSecondary
import com.sms.tagger.util.LogFileWriter
import java.io.File

/**
 * 日志查看页面
 * 显示应用日志，便于用户查看和分享
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogViewerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val logFileWriter = remember { LogFileWriter(context) }
    
    var logContent by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var showClearDialog by remember { mutableStateOf(false) }
    
    // 加载日志内容
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            logContent = logFileWriter.getLatestLogContent()
        } catch (e: Exception) {
            logContent = "读取日志失败: ${e.message}"
        } finally {
            isLoading = false
        }
    }
    
    GradientBackground {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("调试日志") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                        }
                    },
                    actions = {
                        // 分享日志
                        IconButton(onClick = {
                            shareLog(context, logContent)
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "分享日志")
                        }
                        // 清空日志
                        IconButton(onClick = {
                            showClearDialog = true
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "清空日志")
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
                // 日志目录提示
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF667EEA).copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "📁 日志文件位置",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0xFF667EEA)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = logFileWriter.getLogDirPath(),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "提示：日志文件保存在下载目录，可以直接访问",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
                
                // 日志内容
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (logContent.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无日志",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )
                    }
                } else {
                    // 显示日志内容
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            // 按行分割日志
                            val logLines = logContent.split("\n")
                            items(logLines.size) { index ->
                                val line = logLines[index]
                                Text(
                                    text = line,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = when {
                                        line.contains("ERROR", ignoreCase = true) -> Color(0xFFFF6B6B)
                                        line.contains("WARN", ignoreCase = true) -> Color(0xFFFFA500)
                                        line.contains("INFO", ignoreCase = true) -> Color(0xFF4ECDC4)
                                        line.contains("DEBUG", ignoreCase = true) -> Color(0xFF95A5A6)
                                        line.contains("🔍", ignoreCase = false) -> Color(0xFF4ECDC4)
                                        line.contains("⚠️", ignoreCase = false) -> Color(0xFFFFA500)
                                        line.contains("✅", ignoreCase = false) -> Color(0xFF4ECDC4)
                                        line.contains("❌", ignoreCase = false) -> Color(0xFFFF6B6B)
                                        else -> Color(0xFF2C3E50)
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
    
    // 清空日志确认对话框
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("清空日志") },
            text = { Text("确定要清空所有日志吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        logFileWriter.clearAllLogs()
                        logContent = ""
                        showClearDialog = false
                    }
                ) {
                    Text("确定", color = Color(0xFFFF6B6B))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 分享日志内容
 */
private fun shareLog(context: Context, logContent: String) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, logContent)
            putExtra(Intent.EXTRA_SUBJECT, "SMS Agent 调试日志")
        }
        context.startActivity(Intent.createChooser(intent, "分享日志"))
    } catch (e: Exception) {
        android.util.Log.e("LogViewerScreen", "分享日志失败", e)
    }
}
