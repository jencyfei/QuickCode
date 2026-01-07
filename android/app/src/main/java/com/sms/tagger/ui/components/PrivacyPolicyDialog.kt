package com.sms.tagger.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * 隐私政策对话框（参照 privacy_policy_mock.html 布局）
 */
@Composable
fun PrivacyPolicyDialog(
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Dialog(
        onDismissRequest = { /* 不允许关闭，必须选择 */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF5F6FA),
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "🔒 隐私政策",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    IntroCard()

                    PrivacySectionCard(
                        title = "一、我们如何处理您的个人信息",
                        paragraphs = listOf(
                            "本应用为离线工具，所有数据均在本地设备上运行，我们不会上传、收集、存储、分析或共享您的任何个人信息。",
                            "本应用不会建立服务器，不会向任何第三方发送数据，不集成任何第三方 SDK。"
                        )
                    )

                    PermissionSection()

                    PrivacySectionCard(
                        title = "三、我们不会收集的内容",
                        paragraphs = listOf(
                            "在您使用本应用期间，我们不会收集或上传以下信息：",
                            "• 短信内容",
                            "• 通讯录信息",
                            "• 设备信息（如 IMEI、Android ID、MAC 地址等）",
                            "• 位置信息",
                            "• 使用行为信息",
                            "• 支付信息",
                            "• 日志数据",
                            "• 任何可用于识别您身份的信息",
                            "所有数据均仅保存在您的设备中，由您自行管理。"
                        )
                    )

                    PrivacySectionCard(
                        title = "四、我们不会使用的技术或行为",
                        paragraphs = listOf(
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

                    PrivacySectionCard(
                        title = "五、第三方 SDK 情况",
                        paragraphs = listOf("本应用未集成任何第三方 SDK，不存在由第三方收集数据的情况。")
                    )

                    PrivacySectionCard(
                        title = "六、未成年人保护",
                        paragraphs = listOf(
                            "本应用面向一般用户，不专门向未成年人提供服务。",
                            "如您为未成年人，请在监护人指导下阅读并使用本应用。"
                        )
                    )

                    PrivacySectionCardWithHighlight(
                        title = "七、权限管理与撤销",
                        paragraphs = listOf(
                            "您可以随时通过系统设置管理或撤销相关权限。",
                            "撤销权限后，部分功能可能无法正常使用，但不影响您关闭并卸载本应用。"
                        ),
                        highlightTitle = "路径示例：",
                        highlightContent = "设置 → 应用管理 → 本应用 → 权限"
                    )

                    PrivacySectionCard(
                        title = "📝 八、免责声明",
                        paragraphs = listOf(
                            "1. 本应用仅作为短信辅助工具，不保证所有短信解析的 100% 准确性。",
                            "2. 使用本应用期间，如因短信格式变动、运营商变更或设备原因导致解析失败，本应用不承担任何责任。",
                            "3. 您应确保在使用本应用过程中遵守当地法律法规。",
                            "4. 本应用不对因使用或无法使用本应用导致的任何直接或间接损失承担责任。"
                        )
                    )

                    PrivacySectionCard(
                        title = "九、政策更新",
                        paragraphs = listOf(
                            "我们可能在必要时更新本隐私政策。更新后的政策将在本应用内展示，您再次确认后方可继续使用。"
                        )
                    )

                    PrivacySectionCard(
                        title = "十、联系我们",
                        paragraphs = listOf(
                            "如您在使用本应用过程中有任何疑问、建议或投诉，请通过应用内提供的联系方式与开发者联系。"
                        )
                    )

                    Text(
                        text = "更新日期：2025-11-19 | 生效日期：2025-11-19",
                        fontSize = 12.sp,
                        color = Color(0xFF999999),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                }

                ActionBar(
                    onAccept = onAccept,
                    onReject = onReject
                )
            }
        }
    }
}

@Composable
private fun PrivacySectionCard(
    title: String,
    paragraphs: List<String>
) {
    GlassCard {
        SectionTitle(text = title)
        SectionParagraphs(paragraphs = paragraphs)
    }
}

@Composable
private fun PrivacySectionCardWithHighlight(
    title: String,
    paragraphs: List<String>,
    highlightTitle: String,
    highlightContent: String
) {
    GlassCard {
        SectionTitle(text = title)
        SectionParagraphs(paragraphs = paragraphs)
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFF8F9FB),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFFF0F1F5))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = highlightTitle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4F46E5),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Text(
                    text = highlightContent,
                    fontSize = 13.sp,
                    color = Color(0xFF666666)
                )
            }
        }
    }
}

@Composable
private fun IntroCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(14.dp),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0x334F46E5),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🔒", fontSize = 18.sp)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "隐私保护承诺",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111827)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "本应用为纯本地运行的工具软件，所有功能均在您的设备本地完成，不会收集、存储、上传或共享任何个人信息。",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                lineHeight = 21.sp
            )
        }
    }
}

@Composable
private fun PermissionSection() {
    GlassCard {
        SectionTitle(text = "📨 二、我们申请的权限及使用目的")
        SectionParagraphs(
            paragraphs = listOf("为实现核心功能，本应用仅在必要范围内申请以下系统权限：")
        )
        PermissionBox()
        SectionParagraphs(
            paragraphs = listOf("如您拒绝授权短信权限，本应用的相关功能将无法使用，但不影响应用的其他基础功能。")
        )
    }
}

@Composable
private fun PermissionBox() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        color = Color(0xFFF8F9FB),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFF0F1F5))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "📱 读取短信（SMS）权限",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF4F46E5)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "用途：解析短信内容，从中提取快递取件码。",
                fontSize = 13.sp,
                color = Color(0xFF666666)
            )
            Spacer(modifier = Modifier.height(8.dp))
            SectionParagraphs(
                paragraphs = listOf(
                    "• 所有解析过程均在本地完成",
                    "• 不会上传短信内容",
                    "• 不会保存或共享短信数据",
                    "• 不会进行行为分析或构建用户画像"
                )
            )
        }
    }
}

@Composable
private fun ActionBar(
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "使用本应用前，请您仔细阅读并充分理解本隐私政策的全部内容。",
                fontSize = 12.sp,
                color = Color(0xFF666666),
                lineHeight = 18.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFF6B7280)
                    )
                ) {
                    Text(
                        text = "拒绝",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = onAccept,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1F2937)
                    )
                ) {
                    Text(
                        text = "同意",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(14.dp),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF111827),
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun SectionParagraphs(paragraphs: List<String>) {
    paragraphs.forEach { line ->
        if (line.isEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
        } else {
            Text(
                text = line,
                fontSize = 14.sp,
                color = Color(0xFF666666),
                lineHeight = 21.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}

