package com.sms.tagger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sms.tagger.util.UsageLimitManager
import com.sms.tagger.BuildConfig

/**
 * 每日识别次数限制对话框
 * 
 * 场景：免费版用户每日识别次数达到5次时显示
 */
@Composable
fun DailyLimitDialog(
    onDismiss: () -> Unit,
    onActivate: () -> Unit
) {
    val isTrial = false
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "⏰ 今日识别次数已用完",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111827)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "本版本为开源版，识别次数无限制。",
                    fontSize = 13.sp,
                    color = Color(0xFF666666)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color(0xFFE5E7EB), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "开源版已移除所有限制，无需激活、无次数上限、无延迟。",
                    fontSize = 14.sp,
                    color = Color(0xFF374151),
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (!isTrial) {
                    VersionCompareBox()
                    Spacer(modifier = Modifier.height(20.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF3F4F6)
                        )
                    ) {
                        Text(
                            text = if (isTrial) "稍后继续" else "明天再来",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF6B7280),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Button(
                        onClick = onActivate,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1F2937)
                        )
                    ) {
                        Text(
                            text = if (isTrial) "联系开发者" else "立即激活",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * 历史记录已满对话框
 * 
 * 场景：免费版用户历史记录达到3条时首次显示
 */
@Composable
fun HistoryLimitDialog(
    onDismiss: () -> Unit,
    onActivate: () -> Unit
) {
    val isTrial = false
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // 标题
                Text(
                    text = if (isTrial) "📦 体验版历史记录已达上限" else "📦 历史记录已满",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111827)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // 副标题
                Text(
                    text = "开源版历史记录无限制。",
                    fontSize = 13.sp,
                    color = Color(0xFF666666)
                )
                
                // 分割线
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color(0xFFE5E7EB), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                
                // 消息内容
                Text(
                    text = "开源版：历史记录保留无限制，不会覆盖旧记录。",
                    fontSize = 14.sp,
                    color = Color(0xFF374151),
                    lineHeight = 22.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 提示框
                if (!isTrial) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF0F9FF)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "🎁 一次激活，永久使用",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0369A1)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "仅需 ¥10，解锁全部功能",
                                fontSize = 12.sp,
                                color = Color(0xFF0C4A6E)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                // 按钮区
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 取消按钮
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF3F4F6)
                        )
                    ) {
                        Text(
                            text = if (isTrial) "知道了" else "继续使用",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF6B7280),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1F2937)
                        )
                    ) {
                        Text(
                            text = "好的",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * 简洁版限制提示对话框
 * 
 * 通用场景：显示简洁的限制提示
 */
@Composable
fun SimpleLimitDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onActivate: () -> Unit
) {
    val isTrial = BuildConfig.IS_TRIAL
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 图标
                Text(
                    text = "🔒",
                    fontSize = 32.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 标题
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111827),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 消息
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = Color(0xFF374151),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 按钮区
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF3F4F6)
                        )
                    ) {
                        Text(
                            text = "知道了",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF6B7280)
                        )
                    }
                    
                    Button(
                        onClick = onActivate,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1F2937)
                        )
                    ) {
                        Text(
                            text = if (isTrial) "联系开发者" else "立即激活",
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

/**
 * 版本对比组件
 */
@Composable
private fun VersionCompareBox() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF8F9FB)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = "免费版 vs 专业版",
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF)
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // 每日识别
            VersionCompareRow(
                label = "每日识别",
                freeValue = "${UsageLimitManager.FULL_FREE_DAILY_IDENTIFY_LIMIT}次",
                proValue = "无限"
            )
            
            Divider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = Color(0xFFE5E7EB),
                thickness = 1.dp
            )
            
            // 识别速度
            VersionCompareRow(
                label = "识别速度",
                freeValue = "1秒延迟",
                proValue = "即时"
            )
            
            Divider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = Color(0xFFE5E7EB),
                thickness = 1.dp
            )
            
            // 历史记录
            VersionCompareRow(
                label = "历史记录",
                freeValue = "${UsageLimitManager.FREE_HISTORY_LIMIT}条",
                proValue = "无限"
            )
        }
    }
}

/**
 * 版本对比行
 */
@Composable
private fun VersionCompareRow(
    label: String,
    freeValue: String,
    proValue: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color(0xFF374151)
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = freeValue,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF9CA3AF)
            )
            Text(
                text = " → ",
                fontSize = 13.sp,
                color = Color(0xFF9CA3AF)
            )
            Text(
                text = proValue,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF059669)
            )
        }
    }
}

