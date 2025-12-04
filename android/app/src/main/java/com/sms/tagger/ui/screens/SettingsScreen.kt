package com.sms.tagger.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Sms
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
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em
import com.sms.tagger.BuildConfig
import com.sms.tagger.ui.components.FrostedGlassCard
import com.sms.tagger.ui.components.GradientBackground
import com.sms.tagger.ui.theme.TextSecondary
import com.sms.tagger.util.ActivationManager
import com.sms.tagger.util.DeviceIdManager
import com.sms.tagger.util.TimeWindowSettings
import com.sms.tagger.util.LogControlSettings
import com.sms.tagger.util.AppLogger
import com.sms.tagger.util.TrialManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class SettingsPage {
    Main,
    Feedback,
    SoftwareStatement,
    PaidVsFree,
    PermissionSettings
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
    val isTrial = BuildConfig.IS_TRIAL
    val deviceId = remember { ActivationManager.getDeviceIdForUser(context) }
    val deviceIdShortCode = remember(deviceId) { DeviceIdManager.getDeviceIdShortCode(context) }
    var activationInfo by remember { mutableStateOf(ActivationManager.getActivationInfo(context)) }
    var isActivated by remember { mutableStateOf(ActivationManager.isActivated(context)) }
    var showActivationDialog by remember { mutableStateOf(false) }
    var trialRemainingDays by remember { mutableStateOf(if (isTrial) TrialManager.getRemainingDays(context) else 0) }

    LaunchedEffect(isTrial) {
        if (isTrial) {
            TrialManager.ensureTrialStartTime(context)
            trialRemainingDays = TrialManager.getRemainingDays(context)
        }
    }

    if (!isTrial && showActivationDialog) {
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
                    isTrial = isTrial,
                    trialRemainingDays = trialRemainingDays,
                    isActivated = isActivated,
                    remainingActivations = activationInfo?.remaining ?: 0,
                    deviceId = deviceId,
                    deviceIdShortCode = deviceIdShortCode,
                    activatedAt = activationInfo?.activatedAt,
                    onActivateClick = { showActivationDialog = true },
                    onFeedbackClick = { currentPage = SettingsPage.Feedback },
                    onStatementClick = { currentPage = SettingsPage.SoftwareStatement },
                    onPaidDiffClick = { currentPage = SettingsPage.PaidVsFree },
                    onContactDeveloper = { currentPage = SettingsPage.Feedback },
                    onPermissionSettingsClick = { currentPage = SettingsPage.PermissionSettings }
                )
                SettingsPage.Feedback -> FeedbackSuggestionsScreen(
                    onBack = { currentPage = SettingsPage.Main }
                )
                SettingsPage.SoftwareStatement -> SoftwareStatementScreen(
                    onBack = { currentPage = SettingsPage.Main }
                )
                SettingsPage.PaidVsFree -> {
                    if (!isTrial) {
                        PaidVsFreeScreen(
                            onBack = { currentPage = SettingsPage.Main },
                            onActivateClick = { showActivationDialog = true }
                        )
                    } else {
                        SettingsHome(
                            isTrial = true,
                            trialRemainingDays = trialRemainingDays,
                            isActivated = false,
                            remainingActivations = 0,
                            deviceId = deviceId,
                            deviceIdShortCode = deviceIdShortCode,
                            activatedAt = null,
                            onActivateClick = {},
                            onFeedbackClick = { currentPage = SettingsPage.Feedback },
                            onStatementClick = { currentPage = SettingsPage.SoftwareStatement },
                            onPaidDiffClick = {},
                            onContactDeveloper = { currentPage = SettingsPage.Feedback },
                            onPermissionSettingsClick = { currentPage = SettingsPage.PermissionSettings }
                        )
                    }
                }
                SettingsPage.PermissionSettings -> PermissionSettingsScreen(
                    onBack = { currentPage = SettingsPage.Main }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsHome(
    isTrial: Boolean,
    trialRemainingDays: Int,
    isActivated: Boolean,
    remainingActivations: Int,
    deviceId: String,
    deviceIdShortCode: String,
    activatedAt: Long?,
    onActivateClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onStatementClick: () -> Unit,
    onPaidDiffClick: () -> Unit,
    onContactDeveloper: () -> Unit,
    onPermissionSettingsClick: () -> Unit,
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
            // QuickCode 卡片
            item { AppInfoCard() }
            if (isTrial) {
                item {
                    TrialInfoCard(
                        remainingDays = trialRemainingDays,
                        onContactDeveloper = onContactDeveloper
                    )
                }
            } else {
                item {
                    BindDeviceCard(
                        isActivated = isActivated,
                        remainingActivations = remainingActivations,
                        deviceId = deviceId,
                        deviceIdShortCode = deviceIdShortCode,
                        activatedAt = activatedAt,
                        onActivateClick = onActivateClick,
                        onPaidDiffClick = onPaidDiffClick
                    )
                }
            }
            // 权限设置入口
            item { PermissionSettingsEntryCard(onPermissionSettingsClick) }
            // 反馈与支持卡片
            item { SupportCard(onSupportClick = onFeedbackClick) }
            // 时间窗口 & 日志配置
            item { TimeWindowDiagnosticsCard() }
            // 隐私说明卡片
            item { PrivacyCard(onStatementClick = onStatementClick) }
        }
    }
}

@Composable
private fun TrialInfoCard(
    remainingDays: Int,
    onContactDeveloper: () -> Unit
) {
    FrostedGlassCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🧪 当前版本：体验版 Trial",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Text(
                text = if (remainingDays > 0) "有效期剩余：${remainingDays} 天" else "有效期：已到期",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9CA3AF),
                fontSize = 13.sp
            )
            Surface(
                color = Color(0xFFEEF2FF),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "体验版仅供测试，部分功能与高级能力已做限制。",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4C1D95),
                        lineHeight = 18.sp
                    )
                    Text(
                        text = "如需续期或获取完整版，请联系开发者获取帮助。",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4C1D95),
                        lineHeight = 18.sp
                    )
                }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "技术支持：",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4F46E5),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "QQ 709662224",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF374151)
                )
                Text(
                    text = "邮箱 ChazRussel@outlook.com",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF374151)
                )
            }
            Button(
                onClick = onContactDeveloper,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1F2937)
                )
            ) {
                Text(
                    text = "联系开发者",
                    fontSize = 14.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * QuickCode 卡片 - 对齐 settings_page_mock_v2.html
 */
@Composable
private fun AppInfoCard() {
    FrostedGlassCard {
        Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
            // 标题行：📨 QuickCode v1.2.0
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                        Text(
                    text = "📨 QuickCode",
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

@Composable
private fun TimeWindowDiagnosticsCard() {
    val context = LocalContext.current
    var expressDays by remember { mutableStateOf(TimeWindowSettings.getExpressDays(context)) }
    var smsDays by remember { mutableStateOf(TimeWindowSettings.getSmsDays(context)) }
    var verboseLogging by remember { mutableStateOf(LogControlSettings.isVerboseLoggingEnabled(context)) }

    LaunchedEffect(Unit) {
        AppLogger.setVerboseOverride(verboseLogging)
    }

    FrostedGlassCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "⚙️ 数据时间窗口 & 日志",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "可调整快递/短信展示范围，并控制诊断日志输出",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "快递展示范围",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimeWindowSettings.expressOptions().forEach { days ->
                        TimeWindowOptionChip(
                            label = "近${days}天",
                            selected = expressDays == days
                        ) {
                            expressDays = days
                            TimeWindowSettings.setExpressDays(context, days)
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "短信展示范围",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "当前仅展示近 ${smsDays} 天，如需更早请手动扩展",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimeWindowSettings.smsOptions().forEach { days ->
                        TimeWindowOptionChip(
                            label = "近${days}天",
                            selected = smsDays == days
                        ) {
                            smsDays = days
                            TimeWindowSettings.setSmsDays(context, days)
                        }
                    }
                }
            }

            Divider(color = Color(0x1A000000))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "诊断日志开关",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "默认关闭，仅在排查问题时开启以捕获详细日志",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = verboseLogging,
                    onCheckedChange = { enabled ->
                        verboseLogging = enabled
                        LogControlSettings.setVerboseLoggingEnabled(context, enabled)
                    }
                )
            }
        }
    }
}

@Composable
private fun TimeWindowOptionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, if (selected) Color(0xFF4F46E5) else Color(0xFFE5E7EB)),
        color = if (selected) Color(0x1A4F46E5) else Color.White,
        modifier = Modifier
            .clickable { onClick() }
    ) {
        Text(
            text = label,
            color = if (selected) Color(0xFF312E81) else Color(0xFF6B7280),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
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
    onActivateClick: () -> Unit,
    onPaidDiffClick: () -> Unit
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
                text = "🧋 请我喝一杯奶茶(¥10)·永久使用",
                style = MaterialTheme.typography.bodyMedium,
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
                text = "激活流程：\n点击复制设备ID -> 发送设备ID给开发者 (QQ/微信/邮箱) -> 获取激活码 -> 输入激活码 -> 激活成功 ✅",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF555555),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onPaidDiffClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF4F46E5)
                )
            ) {
                Text(
                    text = "查看免费 vs 付费差异 →",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
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

@Composable
private fun PermissionSettingsEntryCard(
    onClick: () -> Unit
) {
    FrostedGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🔐",
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "权限设置",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = "查看短信读取等必需权限的开启方法",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
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
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("隐私政策", fontSize = 18.sp) },
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
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 介绍卡片 - 渐变背景
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF667EEA),
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                    Text(
                            text = "🔒 隐私政策",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                            text = "本应用为纯本地运行的工具软件，所有功能均在您的设备本地完成，不会收集、存储、上传或共享任何个人信息。",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.95f),
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // 一、我们如何处理您的个人信息
            item {
                PrivacySectionCard(
                    title = "一、我们如何处理您的个人信息",
                    content = listOf(
                        "本应用为 离线工具，所有数据均在本地设备上运行，我们不会上传、收集、存储、分析或共享您的任何个人信息。",
                        "本应用不会建立服务器，不会向任何第三方发送数据，不集成任何第三方 SDK。"
                    )
                )
            }

            // 二、我们申请的权限及使用目的
            item {
                PrivacySectionCard(
                    title = "二、我们申请的权限及使用目的",
                    content = listOf(
                        "为实现核心功能，本应用仅在必要范围内申请以下系统权限："
                    ),
                    highlightContent = listOf(
                        "📱 读取短信（SMS）权限",
                        "用途：解析短信内容，从中提取快递取件码。",
                        "• 所有解析过程均在本地完成",
                        "• 不会上传短信内容",
                        "• 不会保存或共享短信数据",
                        "• 不会进行行为分析或构建用户画像"
                    ),
                    footerContent = listOf(
                        "如您拒绝授权短信权限，本应用的相关功能将无法使用，但不影响应用的其他基础功能。"
                    )
                )
            }

            // 三、我们不会收集的内容
            item {
                PrivacySectionCard(
                    title = "三、我们不会收集的内容",
                    content = listOf(
                        "在您使用本应用期间，我们 不会收集或上传 以下信息：",
                        "• 短信内容",
                        "• 通讯录信息",
                        "• 设备信息（如 IMEI、Android ID、MAC 地址等）",
                        "• 位置信息",
                        "• 使用行为信息",
                        "• 支付信息",
                        "• 日志数据",
                        "• 任何可用于识别您身份的信息",
                        "",
                        "所有数据均仅保存在您的设备中，由您自行管理。"
                    )
                )
            }

            // 四、我们不会使用的技术或行为
            item {
                PrivacySectionCard(
                    title = "四、我们不会使用的技术或行为",
                    content = listOf(
                        "本应用不包含以下任何可能涉及隐私风险的行为：",
                        "• 不联网（无上传、无同步、无远程访问）",
                        "• 不内置广告 SDK",
                        "• 不集成推送 SDK",
                        "• 不统计用户行为",
                        "• 不与第三方共享数据",
                        "• 不读取无关权限（如相机、麦克风、位置等）",
                        "• 不进行敏感信息分析"
                    )
                )
            }

            // 五、第三方 SDK 情况
            item {
                PrivacySectionCard(
                    title = "五、第三方 SDK 情况",
                    content = listOf(
                        "本应用 未集成任何第三方 SDK，不存在由第三方收集数据的情况。"
                    )
                )
            }

            // 六、未成年人保护
            item {
                PrivacySectionCard(
                    title = "六、未成年人保护",
                    content = listOf(
                        "本应用面向一般用户，不专门向未成年人提供服务。",
                        "如您为未成年人，请在监护人指导下阅读并使用本应用。"
                    )
                )
            }

            // 七、权限管理与撤销
            item {
                PrivacySectionCard(
                    title = "七、权限管理与撤销",
                    content = listOf(
                        "您可以随时通过系统设置管理或撤销相关权限。",
                        "撤销权限后，部分功能可能无法正常使用，但不影响您关闭并卸载本应用。"
                    ),
                    highlightContent = listOf(
                        "路径示例：",
                        "设置 → 应用管理 → 本应用 → 权限"
                    )
                )
            }

            // 八、免责声明
            item {
                PrivacySectionCard(
                    title = "八、免责声明",
                    content = listOf(
                        "1. 本应用仅作为短信辅助工具，不保证所有短信解析的 100% 准确性。",
                        "2. 使用本应用期间，如因短信格式变动、运营商变更或设备原因导致解析失败，本应用不承担任何责任。",
                        "3. 您应确保在使用本应用过程中遵守当地法律法规。",
                        "4. 本应用不对因使用或无法使用本应用导致的任何直接或间接损失承担责任。"
                    ),
                    isNumbered = true
                )
            }

            // 九、政策更新
            item {
                PrivacySectionCard(
                    title = "九、政策更新",
                    content = listOf(
                        "我们可能在必要时更新本隐私政策。更新后的政策将在本应用内展示，您再次确认后方可继续使用。"
                    )
                )
            }

            // 十、联系我们
            item {
                PrivacySectionCard(
                    title = "十、联系我们",
                    content = listOf(
                        "如您在使用本应用过程中有任何疑问、建议或投诉，请通过应用内提供的联系方式与开发者联系。"
                    )
                )
            }

            // 更新日期
            item {
                    Text(
                    text = "更新日期：2025-11-28 | 生效日期：2025-11-28",
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun PrivacySectionCard(
    title: String,
    content: List<String>,
    highlightContent: List<String>? = null,
    footerContent: List<String>? = null,
    isNumbered: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 主要内容
            content.forEach { line ->
                if (line.isNotEmpty()) {
                    Text(
                        text = line,
                        fontSize = 13.sp,
                        color = Color(0xFF374151),
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // 高亮框内容
            if (highlightContent != null && highlightContent.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF8F9FB),
                    border = BorderStroke(3.dp, Color(0xFF4F46E5))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        highlightContent.forEachIndexed { index, line ->
                            if (line.isNotEmpty()) {
                                if (line.startsWith("📱") || line.startsWith("路径示例")) {
                    Text(
                                        text = line,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF4F46E5),
                                        modifier = Modifier.padding(bottom = if (index < highlightContent.lastIndex) 6.dp else 0.dp)
                                    )
                                } else {
                                    Text(
                                        text = line,
                                        fontSize = 13.sp,
                                        color = Color(0xFF374151),
                                        lineHeight = 20.sp,
                                        modifier = Modifier.padding(bottom = if (index < highlightContent.lastIndex) 4.dp else 0.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 底部内容
            if (footerContent != null && footerContent.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                footerContent.forEach { line ->
                    if (line.isNotEmpty()) {
                    Text(
                            text = line,
                            fontSize = 13.sp,
                            color = Color(0xFF374151),
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaidVsFreeScreen(
    onBack: () -> Unit,
    onActivateClick: () -> Unit
) {
    val flowSteps = listOf("免费体验", "激活设备", "完整功能")
    val diffRows = listOf(
        Triple("⏱️ 每日识别次数", "5 次/天", "不限"),
        Triple("🗂️ 历史记录", "最近 3 条", "全部记录"),
        Triple("📋 批量操作", "部分记录", "全部记录"),
        Triple("🎛️ UI 提示", "显示限制横幅", "UI 更简洁")
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("免费 vs 付费（激活后）", fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            Surface(
                color = Color.White.copy(alpha = 0.95f),
                tonalElevation = 2.dp,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = onActivateClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF111827)
                        )
                    ) {
                        Text(
                            text = "立即激活 🔐",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "¥10 · 永久使用",
                            fontSize = 13.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "流程示意",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF111827)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            flowSteps.forEachIndexed { index, step ->
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF8F9FB)
                                ) {
                                    Text(
                                        text = step,
                                        textAlign = TextAlign.Center,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                                if (index != flowSteps.lastIndex) {
                                    Text(
                                        text = "➜",
                                        color = Color(0xFF9CA3AF),
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "核心差异",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF111827),
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "功能",
                                    color = Color(0xFF6B7280),
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1.2f)
                                )
                                Text(
                                    text = "免费版",
                                    color = Color(0xFF6B7280),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(0.9f)
                                )
                                Text(
                                    text = "付费版",
                                    color = Color(0xFF6B7280),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(0.9f)
                                )
                            }
                            diffRows.forEachIndexed { index, row ->
                                PaidVsFreeRow(
                                    feature = row.first,
                                    freeValue = row.second,
                                    paidValue = row.third,
                                    showDivider = index != diffRows.lastIndex
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun PaidVsFreeRow(
    feature: String,
    freeValue: String,
    paidValue: String,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = feature,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF374151),
                modifier = Modifier.weight(1.2f)
            )
            Surface(
                modifier = Modifier
                    .weight(0.9f)
                    .padding(horizontal = 4.dp),
                color = Color(0xFFF4F4F5),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = freeValue,
                    fontSize = 13.sp,
                    color = Color(0xFF4B5563),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                )
            }
            Surface(
                modifier = Modifier
                    .weight(0.9f)
                    .padding(horizontal = 4.dp),
                color = Color(0xFFECFDF5),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = paidValue,
                    fontSize = 13.sp,
                    color = Color(0xFF047857),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                )
            }
        }
        if (showDivider) {
            Divider(color = Color(0xFFF1F2F6))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PermissionSettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val showToast = remember(context) {
        { message: String ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
    val permissionGuides = listOf(
        PermissionGuide(
            icon = Icons.Outlined.Sms,
            title = "读取短信",
            description = "允许应用读取手机短信内容",
            purpose = "解析短信内容，提取快递取件码、验证码等信息。",
            badgeText = "✔ 已授予",
            badgeType = PermissionBadgeType.Granted,
            actionLabel = "⚙️ 打开短信权限设置",
            onAction = {
                if (!openSmsPermissionSettings(context)) {
                showToast("请前往 系统设置 → 应用管理 → 短信助手 → 权限 → 短信，并选择「始终允许」。")
            }
            }
        )
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("权限设置", fontSize = 18.sp) },
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
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                FrostedGlassCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "权限说明",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "为保证快递取件码识别、短信标签功能正常运行，请开启必要权限。本应用所有数据均在本地处理，不上传不分享。",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            item {
                FrostedGlassCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "正确设置 2 个关键点",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        val highlights = listOf(
                            "短信权限需选择「始终允许」，不要选择拒绝。",
                            "关闭「空白通行证」，避免系统返回空白短信内容。"
                        )
                        highlights.forEach { line ->
                            Text(
                                text = "• $line",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF4B5563),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "核心权限（必需）",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary,
                    letterSpacing = 0.08.em
                )
            }

            item {
                FrostedGlassCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        permissionGuides.forEachIndexed { index, guide ->
                            PermissionDetailItem(guide)
                            if (index != permissionGuides.lastIndex) {
                                Divider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = Color(0x1AFFFFFF)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "🔒 本应用所有数据仅在本地处理。如需了解更多请查看《隐私政策》。",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun PermissionDetailItem(
    guide: PermissionGuide
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = guide.icon,
                        contentDescription = guide.title,
                        tint = Color(0xFF64748B)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = guide.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = guide.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
            PermissionStatusBadge(
                label = guide.badgeText,
                type = guide.badgeType
            )
        }

        Text(
            text = guide.purpose,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF6B7280),
            lineHeight = 20.sp
        )

        Button(
            onClick = guide.onAction,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.65f),
                contentColor = Color(0xFF1F2937)
            )
        ) {
            Text(
                text = guide.actionLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun PermissionStatusBadge(
    label: String,
    type: PermissionBadgeType
) {
    val (background, content) = when (type) {
        PermissionBadgeType.Granted -> Color(0x2622C55E) to Color(0xFF2F7655)
        PermissionBadgeType.Attention -> Color(0x33FBBF24) to Color(0xFF9A5A00)
    }
    Surface(
        color = background,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = label,
            color = content,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

private data class PermissionGuide(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val purpose: String,
    val badgeText: String,
    val badgeType: PermissionBadgeType,
    val actionLabel: String,
    val onAction: () -> Unit
)

private enum class PermissionBadgeType {
    Granted,
    Attention
}

private fun openSmsPermissionSettings(context: Context): Boolean {
    return openAppDetailsSettings(context)
}

private fun openAppDetailsSettings(context: Context): Boolean {
    return try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        true
    } catch (e: Exception) {
        false
    }
}

