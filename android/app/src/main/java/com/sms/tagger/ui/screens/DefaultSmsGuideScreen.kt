package com.sms.tagger.ui.screens

import android.content.Intent
import android.provider.Telephony
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sms.tagger.ui.components.FrostedGlassCard
import com.sms.tagger.ui.components.GradientBackground
import com.sms.tagger.util.AppLogger
import com.sms.tagger.util.SmsDefaultAppChecker

/**
 * 默认短信应用引导页面
 * 
 * 引导用户将本应用设置为默认短信应用，以读取所有短信（包括10684等）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultSmsGuideScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isDefaultSmsApp by remember { mutableStateOf(false) }
    
    // 检查当前是否为默认短信应用
    LaunchedEffect(Unit) {
        isDefaultSmsApp = SmsDefaultAppChecker.isDefaultSmsApp(context)
        AppLogger.d("DefaultSmsGuideScreen", "当前是否为默认短信应用: $isDefaultSmsApp")
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("设置默认短信应用") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        GradientBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 状态提示卡片
                FrostedGlassCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isDefaultSmsApp) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isDefaultSmsApp) Color(0xFF10B981) else Color(0xFFF59E0B),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Text(
                                text = if (isDefaultSmsApp) "✅ 已是默认短信应用" else "⚠️ 尚未设置为默认短信应用",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp,
                                color = if (isDefaultSmsApp) Color(0xFF10B981) else Color(0xFFF59E0B)
                            )
                        }
                        
                        if (isDefaultSmsApp) {
                            Text(
                                text = "当前应用已设置为系统默认短信应用，可以读取所有短信。",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Text(
                                text = "当前应用尚未设置为默认短信应用，可能无法读取所有短信（如10684开头的短信）。",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                
                // 说明卡片
                FrostedGlassCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "为什么需要设置为默认短信应用？",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        
                        Text(
                            text = "为了让【QuickCode】自动识别您的快递取件码，需要您将本应用设为系统短信服务。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
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
                                    text = "重要说明：",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF4C1D95)
                                )
                                Text(
                                    text = "• 不会影响您继续使用微信 / QQ 收发消息",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF4C1D95),
                                    lineHeight = 20.sp
                                )
                                Text(
                                    text = "• 不会修改您的短信内容",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF4C1D95),
                                    lineHeight = 20.sp
                                )
                                Text(
                                    text = "• 仅用于读取短信以识别取件码",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF4C1D95),
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
                
                // 操作按钮
                if (!isDefaultSmsApp) {
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                                intent.putExtra(
                                    Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                                    context.packageName
                                )
                                context.startActivity(intent)
                                AppLogger.d("DefaultSmsGuideScreen", "已启动设置默认短信应用界面")
                            } catch (e: Exception) {
                                AppLogger.e("DefaultSmsGuideScreen", "启动设置界面失败: ${e.message}", e)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4F46E5)
                        )
                    ) {
                        Text(
                            text = "设置为默认短信应用",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                                context.startActivity(intent)
                                AppLogger.d("DefaultSmsGuideScreen", "已启动更改默认短信应用界面")
                            } catch (e: Exception) {
                                AppLogger.e("DefaultSmsGuideScreen", "启动设置界面失败: ${e.message}", e)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF6B7280)
                        )
                    ) {
                        Text(
                            text = "更改默认短信应用",
                            fontSize = 16.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

