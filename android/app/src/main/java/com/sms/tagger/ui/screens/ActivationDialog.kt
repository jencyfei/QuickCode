package com.sms.tagger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sms.tagger.util.ActivationManager
import com.sms.tagger.util.DeviceIdManager

/**
 * 激活弹窗 - 严格对齐 activation_dialog_mock.html
 */
@Composable
fun ActivationDialog(
    onActivated: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var activationCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    val deviceId = remember { ActivationManager.getDeviceIdForUser(context) }
    val deviceIdShortCode = remember { DeviceIdManager.getDeviceIdShortCode(context) }

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 340.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                // 标题
                Text(
                    text = "🔐 绑定设备",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 设备ID区块
                Text(
                    text = "设备 ID",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF666666),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFFF8F9FB),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
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
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(deviceId))
                            message = "设备ID已复制"
                            isError = false
                        },
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
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 激活码输入
                Text(
                    text = "激活码",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF666666),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = activationCode,
                    onValueChange = { activationCode = it },
                    placeholder = { Text("请输入激活码", color = Color(0xFF9CA3AF)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions.Default,
                    shape = RoundedCornerShape(8.dp)
                )

                // 消息提示
                if (message != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = if (isError) Color(0xFFFEF2F2) else Color(0xFFECFDF5),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isError) "✗ $message" else "✓ $message",
                            color = if (isError) Color(0xFF991B1B) else Color(0xFF065F46),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 提示信息区
                Surface(
                    color = Color(0xFFF0F9FF),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "☕ 请我喝一杯奶茶（¥10）· 永久使用",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0369A1),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Text(
                            text = "• 全离线运行，不联网更安全",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF0C4A6E),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "• 每个激活码最多支持 3 次激活",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF0C4A6E),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 按钮区
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 取消按钮
                    Button(
                        onClick = onCancel,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF3F4F6)
                        )
                    ) {
                        Text(
                            text = "取消",
                            color = Color(0xFF6B7280),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                    // 确认激活按钮
                    Button(
                        onClick = {
                            if (activationCode.isBlank()) {
                                message = "请输入激活码"
                                isError = true
                                return@Button
                            }
                            isLoading = true
                            val result = ActivationManager.validateActivationCode(context, activationCode)
                            isLoading = false
                            result.onSuccess {
                                message = "激活成功！感谢支持"
                                isError = false
                                onActivated()
                            }.onFailure { e ->
                                message = e.message ?: "激活失败，请稍后再试"
                                isError = true
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1F2937)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "确认激活",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
