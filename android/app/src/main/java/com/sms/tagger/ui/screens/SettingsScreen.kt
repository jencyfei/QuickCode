package com.sms.tagger.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sms.tagger.ui.components.FrostedGlassCard
import com.sms.tagger.ui.components.GradientBackground
import com.sms.tagger.ui.theme.TextSecondary
import com.sms.tagger.util.ActivationManager
import com.sms.tagger.util.DeviceIdManager
import com.sms.tagger.BuildConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class SettingsPage {
    Main,
    Feedback,
    SoftwareStatement
}

/**
 * 设置页面
 */
@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogout: () -> Unit
) {
    var currentPage by remember { mutableStateOf(SettingsPage.Main) }
    val context = LocalContext.current
    val deviceId = remember { ActivationManager.getDeviceIdForUser(context) }
    val deviceIdShortCode = remember(deviceId) { DeviceIdManager.getDeviceIdShortCode(context) }
    var activationInfo by remember { mutableStateOf(ActivationManager.getActivationInfo(context)) }
    var isActivated by remember { mutableStateOf(ActivationManager.isActivated(context)) }
    var showActivationDialog by remember { mutableStateOf(false) }

    if (showActivationDialog) {
        ActivationDialog(
            onActivated = {
                activationInfo = ActivationManager.getActivationInfo(context)
                isActivated = ActivationManager.isActivated(context)
                showActivationDialog = false
            },
            onCancel = { showActivationDialog = false }
        )
    }

    GradientBackground {
        Crossfade(targetState = currentPage, label = "settings_pages") { page ->
            when (page) {
                SettingsPage.Main -> SettingsHome(
                    isActivated = isActivated,
                    remainingActivations = activationInfo?.remaining ?: 0,
                    deviceId = deviceId,
                    deviceIdShortCode = deviceIdShortCode,
                    activatedAt = activationInfo?.activatedAt,
                    onActivateClick = { showActivationDialog = true },
                    onFeedbackClick = { currentPage = SettingsPage.Feedback },
                    onStatementClick = { currentPage = SettingsPage.SoftwareStatement }
                )
                SettingsPage.Feedback -> FeedbackSuggestionsScreen(
                    onBack = { currentPage = SettingsPage.Main }
                )
                SettingsPage.SoftwareStatement -> SoftwareStatementScreen(
                    onBack = { currentPage = SettingsPage.Main }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsHome(
    isActivated: Boolean,
    remainingActivations: Int,
    deviceId: String,
    deviceIdShortCode: String,
    activatedAt: Long?,
    onActivateClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onStatementClick: () -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("设置", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 短信助手卡片
            item { AppInfoCard() }
            // 绑定设备卡片
            item {
                BindDeviceCard(
                    isActivated = isActivated,
                    remainingActivations = remainingActivations,
                    deviceId = deviceId,
                    deviceIdShortCode = deviceIdShortCode,
                    activatedAt = activatedAt,
                    onActivateClick = onActivateClick
                )
            }
            // 反馈与支持卡片
            item { SupportCard(onSupportClick = onFeedbackClick) }
            // 隐私说明卡片
            item { PrivacyCard(onStatementClick = onStatementClick) }
        }
    }
}

/**
 * 短信助手卡片 - 对齐 settings_page_mock_v2.html
 */
@Composable
private fun AppInfoCard() {
    FrostedGlassCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 标题行：📨 短信助手 v1.2.0
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📨 短信助手",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "v${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF999999),
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            // 标签
            Surface(
                color = Color(0xFFF0F1F5),
                shape = RoundedCornerShape(999.dp)
            ) {
                Text(
                    text = "独立运行 · 无需登录",
                    color = Color(0xFF666666),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }
        }
    }
}

/**
 * 绑定设备卡片 - 对齐 settings_page_mock_v2.html
 */
@Composable
private fun BindDeviceCard(
    isActivated: Boolean,
    remainingActivations: Int,
    deviceId: String,
    deviceIdShortCode: String,
    activatedAt: Long?,
    onActivateClick: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val formattedDate = remember(activatedAt) {
        activatedAt?.let {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            formatter.format(Date(it))
        }
    }

    FrostedGlassCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 标题
            Text(
                text = "🔐 绑定设备",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            // 状态
            Text(
                text = if (isActivated) "状态：已激活" else "状态：未激活",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF999999),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(12.dp))
            // 分割线
            Divider(color = Color(0xFFF0F1F5), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // 重点文案区
            Text(
                text = "🌟 一次授权 · 长期可用",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "为当前设备解锁完整功能",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF666666),
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "¥10",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4F46E5),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 设备ID行 - 紧凑横向布局
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFF8F9FB),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .height(44.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = deviceIdShortCode,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFF374151),
                    modifier = Modifier.weight(1f)
                )
                // 复制按钮
                OutlinedButton(
                    onClick = { clipboardManager.setText(AnnotatedString(deviceId)) },
                    modifier = Modifier.height(28.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "复制",
                        fontSize = 12.sp,
                        color = Color(0xFF4F46E5)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                // 激活按钮
                Button(
                    onClick = onActivateClick,
                    modifier = Modifier.height(28.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1F2937)
                    )
                ) {
                        Text(
                        text = "激活",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "复制后通过「反馈与支持」联系开发者获取激活码",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF999999),
                fontSize = 12.sp
            )
        }
    }
}

/**
 * 反馈与支持卡片 - 对齐 settings_page_mock_v2.html
 */
@Composable
private fun SupportCard(onSupportClick: () -> Unit) {
    FrostedGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSupportClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🤝",
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "反馈与支持",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = "遇到问题或有想法？欢迎告诉我们！",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF666666),
                    fontSize = 13.sp
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * 隐私说明卡片 - 对齐 settings_page_mock_v2.html
 */
@Composable
private fun PrivacyCard(onStatementClick: () -> Unit) {
    FrostedGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onStatementClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📄",
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "隐私说明与免责声明",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                    Text(
                    text = "了解我们如何保护你的数据",
                        style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF666666),
                    fontSize = 13.sp
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SoftwareStatementScreen(
    onBack: () -> Unit
) {
    val features = listOf(
        "快递取件码" to "帮助您快速提取和查看快递短信中的取件码，支持复制和分享。",
        "短信管理" to "提供最新短信列表、搜索与筛选，方便快速定位关键信息。"
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("隐私说明与免责声明") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                FrostedGlassCard {
                    Text(
                        text = "本地运行与隐私保护",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "本应用是一款纯本地工具，所有功能均在您的设备上运行，不涉及任何联网功能（如数据上传、云端同步或远程访问）。我们不会收集、存储或传输您的任何个人信息，包括短信内容、联系人数据或其他隐私信息。您的所有数据均保留在本地设备中，由您自行管理。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            item {
                FrostedGlassCard {
                    Text(
                        text = "主要功能",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        features.forEach { (title, desc) ->
                            FeatureRow(title = title, description = desc)
                        }
                    }
                }
            }

            item {
                FrostedGlassCard {
                    Text(
                        text = "免责条款",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "本应用仅提供辅助工具，不保证所有短信分类的 100% 准确性（如因短信内容模糊导致的误标）。在使用过程中，请确保遵守当地法律法规。我们不对因使用本应用导致的任何直接或间接损失负责。如果您有任何疑问或建议，请通过应用内反馈渠道联系开发者。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedbackSuggestionsScreen(
    onBack: () -> Unit
) {
    val feedbackTypes = listOf(
        "Bug 报告" to "如果发现应用崩溃、短信识别错误或其他异常，请描述问题细节（设备型号、系统版本、操作步骤、短信示例等）。",
        "新增功能需求" to "欢迎提出希望添加的新功能（例如更多筛选方式、第三方平台支持或 UI 改进等）。",
        "其他建议" to "如性能优化、隐私增强、交互体验等任何想法，我们都乐于倾听。"
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("反馈与支持") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                FrostedGlassCard {
                    Text(
                        text = "感谢您的支持",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "感谢您使用本应用！我们致力于不断改进工具的功能和用户体验。如果您在使用过程中遇到任何问题，或有宝贵建议，请随时通过下方渠道联系我们。您的反馈对我们非常重要。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            item {
                FrostedGlassCard {
                    Text(
                        text = "反馈类型示例",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        feedbackTypes.forEach { (title, desc) ->
                            FeatureRow(title = title, description = desc)
                        }
                    }
                }
            }

            item {
                FrostedGlassCard {
                    Text(
                        text = "联系方式",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "请发送您的反馈到以下邮箱，并在主题中注明「应用反馈 - 反馈类型」，以便我们快速响应。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "邮箱：ChazRussel@outlook.com",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "企鹅群：1064696594",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "我们会尽快回复，并在未来版本中考虑采纳优秀建议。感谢您的支持！",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureRow(
    title: String,
    description: String
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}
