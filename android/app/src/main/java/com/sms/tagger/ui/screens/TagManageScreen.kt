package com.sms.tagger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.TextField
import androidx.compose.material3.IconButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.sms.tagger.ui.components.FrostedGlassCard
import com.sms.tagger.ui.components.GradientBackground
import com.sms.tagger.ui.theme.TextSecondary
import com.sms.tagger.util.SmsClassifier
import com.sms.tagger.util.SmsReader

/**
 * 标签管理页面
 */
// 可用的 emoji 列表
private val availableEmojis = listOf(
    "🔐", "📦", "🏦", "🔔", "📢",
    "💬", "📱", "🎯", "⭐", "❤️",
    "🎁", "🎉", "🎊", "🎈", "🎀",
    "📝", "📋", "📌", "📍", "🔖",
    "💼", "👔", "🎓", "🏆", "🥇",
    "🌟", "✨", "💫", "🌈", "🔥",
    "💰", "💳", "💵", "💴", "💶",
    "🚀", "✈️", "🚗", "🚕", "🚙",
    "🍔", "🍕", "🍜", "🍱", "🍰",
    "☕", "🍷", "🍺", "🥤", "🧃"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagManageScreen() {
    val context = LocalContext.current
    var selectedTag by remember { mutableStateOf<String?>(null) }
    var tagCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var showAddTagDialog by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("📌") }
    var showRuleManager by remember { mutableStateOf(false) }
    
    // 加载短信并分类
    LaunchedEffect(Unit) {
        try {
            val smsReader = SmsReader(context)
            val allSms = smsReader.readLatestSms(500)
            
            // 按标签分类
            val classified = SmsClassifier.classifySmsList(allSms)
            tagCounts = mapOf(
                "验证码" to (classified["验证码"]?.size ?: 0),
                "快递" to (classified["快递"]?.size ?: 0),
                "银行" to (classified["银行"]?.size ?: 0),
                "通知" to (classified["通知"]?.size ?: 0),
                "营销" to (classified["营销"]?.size ?: 0)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // 如果显示规则管理，则显示规则管理页面
    if (showRuleManager) {
        RuleManageScreen(onBack = { showRuleManager = false })
        return
    }
    
    // 如果选中了标签，显示该标签的短信列表
    if (selectedTag != null) {
        SmsListScreen(
            tagFilter = selectedTag,
            onBack = { selectedTag = null }
        )
        return
    }
    GradientBackground {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("标签管理") },
                    actions = {
                        IconButton(
                            onClick = { showRuleManager = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "自定义规则",
                                tint = Color(0xFF333333)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddTagDialog = true },
                    modifier = Modifier
                        .size(56.dp)
                        .border(
                            width = 1.2.dp,
                            color = Color.White.copy(alpha = 0.6f),
                            shape = CircleShape
                        ),
                    shape = CircleShape,
                    containerColor = Color.White.copy(alpha = 0.35f),
                    contentColor = Color(0xFF667EEA),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "添加标签",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                listOf(
                    TagItem("验证码", "#FF6B9D", tagCounts["验证码"] ?: 0, "🔐"),
                    TagItem("快递", "#4A90E2", tagCounts["快递"] ?: 0, "📦"),
                    TagItem("银行", "#7ED321", tagCounts["银行"] ?: 0, "🏦"),
                    TagItem("通知", "#F5A623", tagCounts["通知"] ?: 0, "🔔"),
                    TagItem("营销", "#9013FE", tagCounts["营销"] ?: 0, "📢")
                )
            ) { tag ->
                TagItemCard(
                    tag = tag,
                    onClick = { selectedTag = tag.name }
                )
            }
        }
        }
    }
    
    // 添加标签对话框
    if (showAddTagDialog) {
        AlertDialog(
            onDismissRequest = { 
                showAddTagDialog = false
                newTagName = ""
                selectedEmoji = "📌"
            },
            modifier = Modifier
                .background(
                    color = Color.White.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(24.dp)
                )
                .border(
                    width = 1.5.dp,
                    color = Color.White.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(24.dp)
                ),
            containerColor = Color.White.copy(alpha = 0.95f),
            title = { 
                Text(
                    "添加新标签",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                ) 
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // 标签名称输入
                    Text(
                        "标签名称",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF666666),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = newTagName,
                        onValueChange = { newTagName = it },
                        placeholder = { Text("输入标签名称") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFFF5F5F5),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color(0xFF667EEA)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Emoji 选择器
                    Text(
                        "选择图标",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF666666),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Emoji 网格
                    Column {
                        availableEmojis.chunked(5).forEach { row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { emoji ->
                                    Button(
                                        onClick = { selectedEmoji = emoji },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .border(
                                                width = if (selectedEmoji == emoji) 2.dp else 1.dp,
                                                color = if (selectedEmoji == emoji) 
                                                    Color(0xFF667EEA) 
                                                else 
                                                    Color(0xFFDDDDDD),
                                                shape = RoundedCornerShape(12.dp)
                                            ),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (selectedEmoji == emoji)
                                                Color(0xFF667EEA).copy(alpha = 0.15f)
                                            else
                                                Color(0xFFFAFAFA)
                                        ),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            text = emoji,
                                            style = MaterialTheme.typography.headlineSmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTagName.isNotBlank()) {
                            // 这里可以添加保存标签的逻辑
                            // 目前只是关闭对话框
                            showAddTagDialog = false
                            newTagName = ""
                            selectedEmoji = "📌"
                        }
                    },
                    modifier = Modifier
                        .height(40.dp)
                        .background(
                            color = Color(0xFF667EEA),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF667EEA)
                    )
                ) {
                    Text(
                        "确定",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showAddTagDialog = false
                        newTagName = ""
                        selectedEmoji = "📌"
                    },
                    modifier = Modifier
                        .height(40.dp)
                        .background(
                            color = Color(0xFFE8E8E8),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE8E8E8)
                    )
                ) {
                    Text(
                        "取消",
                        color = Color(0xFF333333),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        )
    }
}

data class TagItem(
    val name: String,
    val color: String,
    val count: Int,
    val emoji: String = "📌"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagItemCard(
    tag: TagItem,
    onClick: () -> Unit = {}
) {
    val tagColor = Color(android.graphics.Color.parseColor(tag.color))
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .border(
                width = 1.5.dp,
                color = Color.White.copy(alpha = 0.6f),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Emoji 图标 - 柔和玻璃拟态风格
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                    .border(
                        width = 1.2.dp,
                        color = Color.White.copy(alpha = 0.5f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tag.emoji,
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 28.sp
                )
            }
            
            // 标签信息
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = tag.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF333333),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${tag.count} 条短信",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
            
            // 右侧装饰 - 小圆点（柔和玻璃拟态风格）
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
                    .border(
                        width = 0.8.dp,
                        color = Color.White.copy(alpha = 0.6f),
                        shape = CircleShape
                    )
            )
        }
    }
}
