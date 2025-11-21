package com.sms.tagger.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.sms.tagger.ui.components.FrostedGlassCard
import com.sms.tagger.ui.components.GradientBackground
import com.sms.tagger.ui.theme.TextSecondary
import com.sms.tagger.util.SmsReader
import com.sms.tagger.util.SmsClassifier
import java.text.SimpleDateFormat
import java.util.*

/**
 * 短信列表页面
 * @param tagFilter 标签筛选（可选）
 * @param onBack 返回回调（可选）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsListScreen(
    tagFilter: String? = null,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var smsCreateList by remember { mutableStateOf<List<com.sms.tagger.data.model.SmsCreate>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // 加载短信 - 从本地读取
    LaunchedEffect(tagFilter) {
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
            android.util.Log.d("SmsListScreen", "========== 开始加载短信列表 ==========")
            android.util.Log.d("SmsListScreen", "标签过滤: $tagFilter")
            
            // 增加读取数量限制到20000条，确保能读取到所有短信（包括运营商短信等）
            // 如果没有标签过滤，显示所有短信；否则只显示指定标签的短信
            val allSms = smsReader.readAllSms(20000)
            
            android.util.Log.d("SmsListScreen", "✅ 读取到 ${allSms.size} 条短信")
            
            // 打印前3条短信的详细信息
            allSms.take(3).forEachIndexed { index, sms ->
                android.util.Log.d("SmsListScreen", "短信 ${index + 1}: 发件人=${sms.sender}, 内容=${sms.content.take(50)}, 时间=${sms.receivedAt}")
            }
            
            // 按标签过滤短信
            val filteredSms = if (tagFilter != null) {
                // 对短信进行分类
                android.util.Log.d("SmsListScreen", "开始分类短信...")
                val classified = SmsClassifier.classifySmsList(allSms)
                
                // 打印分类结果统计
                android.util.Log.d("SmsListScreen", "分类结果统计:")
                classified.forEach { (tag, smsList) ->
                    android.util.Log.d("SmsListScreen", "  - $tag: ${smsList.size} 条")
                }
                
                // 获取指定标签的短信
                val taggedSms = classified[tagFilter] ?: emptyList()
                android.util.Log.d("SmsListScreen", "✅ 标签 '$tagFilter' 的短信数: ${taggedSms.size} 条")
                
                if (taggedSms.isEmpty()) {
                    android.util.Log.w("SmsListScreen", "⚠️ 标签 '$tagFilter' 下没有短信，可用标签: ${classified.keys}")
                }
                
                taggedSms
            } else {
                // 没有标签过滤时，显示所有短信
                android.util.Log.d("SmsListScreen", "未指定标签过滤，显示所有短信")
                allSms
            }
            
            android.util.Log.d("SmsListScreen", "过滤后短信数: ${filteredSms.size} 条")
            
            // 按时间倒序排列（最新的短信在最前）
            smsCreateList = filteredSms.sortedByDescending { it.receivedAt }
            
            android.util.Log.d("SmsListScreen", "排序后短信数: ${smsCreateList.size} 条")
            
            // 打印排序后的前3条短信
            smsCreateList.take(3).forEachIndexed { index, sms ->
                android.util.Log.d("SmsListScreen", "排序后短信 ${index + 1}: 发件人=${sms.sender}, 时间=${sms.receivedAt}")
            }
            
            android.util.Log.d("SmsListScreen", "========== 短信列表加载完成 ==========")
            
            if (smsCreateList.isEmpty()) {
                errorMessage = "暂无短信"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            errorMessage = "加载失败: ${e.message}"
            android.util.Log.e("SmsListScreen", "加载短信错误", e)
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
                        Text(if (tagFilter != null) "$tagFilter - 短信" else "短信列表") 
                    },
                    navigationIcon = {
                        if (onBack != null) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* TODO: 搜索功能 */ }) {
                            Icon(Icons.Default.Search, contentDescription = "搜索")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent
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
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
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
        } else if (smsCreateList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (tagFilter != null) "该标签下暂无短信" else "暂无短信",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(smsCreateList) { sms ->
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

/**
 * 格式化时间显示
 */
private fun formatTime(isoTime: String): String {
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
