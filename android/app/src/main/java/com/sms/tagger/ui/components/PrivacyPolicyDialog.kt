package com.sms.tagger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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
 * 隐私政策对话框
 * 
 * 首次运行应用时显示，用户必须同意才能继续使用
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
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(0.dp)
            ) {
                // 标题栏
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    color = Color(0xFF667EEA)
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

                // 内容区域 - 可滚动
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    PrivacySection(
                        title = "一、我们如何处理您的个人信息",
                        content = listOf(
                            "本应用为 离线工具，所有数据均在本地设备上运行，我们不会上传、收集、存储、分析或共享您的任何个人信息。",
                            "本应用不会建立服务器，不会向任何第三方发送数据，不集成任何第三方 SDK。"
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PrivacySection(
                        title = "二、我们申请的权限及使用目的",
                        content = listOf(
                            "为实现核心功能，本应用仅在必要范围内申请以下系统权限：",
                            "📱 读取短信（SMS）权限：",
                            "• 用途：解析短信内容，从中提取快递取件码",
                            "• 所有解析过程均在本地完成",
                            "• 不会上传短信内容",
                            "• 不会保存或共享短信数据",
                            "• 不会进行行为分析或构建用户画像",
                            "",
                            "如您拒绝授权短信权限，本应用的相关功能将无法使用，但不影响应用的其他基础功能。"
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PrivacySection(
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

                    Spacer(modifier = Modifier.height(16.dp))

                    PrivacySection(
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

                    Spacer(modifier = Modifier.height(16.dp))

                    PrivacySection(
                        title = "五、第三方 SDK 情况",
                        content = listOf(
                            "本应用 未集成任何第三方 SDK，不存在由第三方收集数据的情况。"
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PrivacySection(
                        title = "六、未成年人保护",
                        content = listOf(
                            "本应用面向一般用户，不专门向未成年人提供服务。",
                            "如您为未成年人，请在监护人指导下阅读并使用本应用。"
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PrivacySection(
                        title = "七、权限管理与撤销",
                        content = listOf(
                            "您可以随时通过系统设置管理或撤销相关权限。",
                            "撤销权限后，部分功能可能无法正常使用，但不影响您关闭并卸载本应用。",
                            "",
                            "路径示例：设置 → 应用管理 → 本应用 → 权限"
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PrivacySection(
                        title = "八、免责声明",
                        content = listOf(
                            "1. 本应用仅作为短信辅助工具，不保证所有短信解析的 100% 准确性。",
                            "2. 使用本应用期间，如因短信格式变动、运营商变更或设备原因导致解析失败，本应用不承担任何责任。",
                            "3. 您应确保在使用本应用过程中遵守当地法律法规。",
                            "4. 本应用不对因使用或无法使用本应用导致的任何直接或间接损失承担责任。"
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PrivacySection(
                        title = "九、政策更新",
                        content = listOf(
                            "我们可能在必要时更新本隐私政策。更新后的政策将在本应用内展示，您再次确认后方可继续使用。"
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PrivacySection(
                        title = "十、联系我们",
                        content = listOf(
                            "如您在使用本应用过程中有任何疑问、建议或投诉，请通过应用内提供的联系方式与开发者联系。"
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "更新日期：2025-11-28 | 生效日期：2025-11-28",
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 按钮栏
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                    color = Color(0xFFF8F9FA)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
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
                            // 拒绝按钮
                            Button(
                                onClick = onReject,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF3F4F6)
                                )
                            ) {
                                Text(
                                    text = "拒绝",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF6B7280)
                                )
                            }

                            // 同意按钮
                            Button(
                                onClick = onAccept,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF667EEA)
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
        }
    }
}

@Composable
private fun PrivacySection(
    title: String,
    content: List<String>
) {
    Column {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF111827),
            modifier = Modifier.padding(bottom = 8.dp)
        )

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
    }
}

