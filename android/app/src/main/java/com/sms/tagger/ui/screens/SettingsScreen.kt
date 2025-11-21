package com.sms.tagger.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sms.tagger.ui.components.FrostedGlassCard
import com.sms.tagger.ui.components.GradientBackground
import com.sms.tagger.ui.theme.TextSecondary

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

    GradientBackground {
        Crossfade(targetState = currentPage, label = "settings_pages") { page ->
            when (page) {
                SettingsPage.Main -> SettingsHome(
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
    onFeedbackClick: () -> Unit,
    onStatementClick: () -> Unit
) {
    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("设置") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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
            FrostedGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "短信助手",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "版本 1.0.0 - 本地工具",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            SettingItem(
                icon = Icons.Default.Chat,
                title = "反馈与建议",
                subtitle = "分享您的想法或问题",
                onClick = onFeedbackClick
            )

            SettingItem(
                icon = Icons.Default.Description,
                title = "软件声明",
                subtitle = "了解隐私说明与免责条款",
                onClick = onStatementClick
            )
        }
    }
}

@Composable
fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    FrostedGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(enabled = onClick != null) { onClick?.invoke() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            trailing?.invoke()
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
        "短信打标" to "根据短信内容自动或手动添加标签（如营销、通知、银行、快递、验证码），便于分类管理。",
        "短信批量处理" to "针对同一标签的短信，支持批量删除、归档、转发或自动化回复等操作，提高效率。"
    )

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("软件声明") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
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
        "Bug 报告" to "如果发现应用崩溃、标签分类错误、批量处理失败或其他异常，请描述问题细节（设备型号、系统版本、操作步骤、短信示例等）。",
        "新增功能需求" to "欢迎提出希望添加的标签类型（例如“医疗”“社交”）、自定义规则、第三方平台支持或 UI 改进等。",
        "其他建议" to "如性能优化、隐私增强、交互体验等任何想法，我们都乐于倾听。"
    )

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("反馈与建议") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
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
                        text = "请发送您的反馈到以下邮箱，并在主题中注明“[应用反馈] - [反馈类型]”，以便我们快速响应。",
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
