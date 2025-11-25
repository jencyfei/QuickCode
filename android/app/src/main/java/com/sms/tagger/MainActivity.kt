package com.sms.tagger

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.sms.tagger.ui.screens.*
import com.sms.tagger.ui.theme.SmsAgentTheme
import com.sms.tagger.ui.components.GradientBackground
import com.sms.tagger.util.PreferencesManager
import com.sms.tagger.util.LogFileWriter
import com.sms.tagger.util.AppLogger
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private val prefsManager by lazy { PreferencesManager(this) }
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            // 权限已授予
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // ✅ 初始化日志系统
        val logFileWriter = LogFileWriter(this)
        AppLogger.init(logFileWriter)
        android.util.Log.d("MainActivity", "✅ 日志系统已初始化，日志目录: ${logFileWriter.getLogDirPath()}")
        
        // 请求权限
        requestSmsPermissions()
        
        setContent {
            SmsAgentTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
    
    private fun requestSmsPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS
        )
        
        val needRequest = permissions.any {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (needRequest) {
            requestPermissionLauncher.launch(permissions)
        }
    }
    
    @Composable
    fun MainScreen() {
        // 直接显示主应用（无需登录）
        MainAppScreen(
            onLogout = {
                // 本地应用，无需登出
            }
        )
    }
    
    @Composable
    fun WelcomeScreen(onLoginClick: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "短信助手",
                style = MaterialTheme.typography.headlineLarge
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("未登录")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onLoginClick) {
                Text("去登录")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "版本 1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    
    @Composable
    fun LoginScreen(
        onLoginSuccess: () -> Unit,
        onBack: () -> Unit
    ) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        val scope = rememberCoroutineScope()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "登录",
                style = MaterialTheme.typography.headlineLarge
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("邮箱") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                enabled = !isLoading
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("密码") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                enabled = !isLoading
            )
            
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        // 简单演示：直接保存token
                        prefsManager.saveAccessToken("demo_token_${System.currentTimeMillis()}")
                        prefsManager.saveUserInfo(1, email)
                        onLoginSuccess()
                    } else {
                        errorMessage = "请输入邮箱和密码"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("登录")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = onBack,
                enabled = !isLoading
            ) {
                Text("返回")
            }
        }
    }
    
    @Composable
    fun MainAppScreen(onLogout: () -> Unit) {
        var selectedTab by remember { mutableStateOf(0) } // 默认显示快递页面
        
        // 判断是否超过2025年11月24号（用于测试弹窗）
        val isBetaExpired = remember {
            val currentTime = java.util.Calendar.getInstance()
            val expirationDate = java.util.Calendar.getInstance().apply {
                set(2025, java.util.Calendar.NOVEMBER, 24, 0, 0, 0)
            }
            currentTime.after(expirationDate)
        }
        
        var showBetaDialog by remember { mutableStateOf(isBetaExpired) } // 仅在过期时显示
        
        // Beta到期弹窗
        if (showBetaDialog) {
            BetaExpirationDialog(
                onContinue = { showBetaDialog = false },
                onFeedback = { 
                    // 打开反馈链接或QQ群
                    showBetaDialog = false
                }
            )
        }
        
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                bottomBar = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(64.dp)
                            .background(
                                color = androidx.compose.ui.graphics.Color(0x66FFFFFF),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = androidx.compose.ui.graphics.Color(0x99FFFFFF),
                                shape = RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 快递 - 第1个
                            GlassNavButton(
                                emoji = "📦",
                                isSelected = selectedTab == 0,
                                onClick = { selectedTab = 0 }
                            )
                            // 标签 - 第2个
                            GlassNavButton(
                                emoji = "🏷️",
                                isSelected = selectedTab == 1,
                                onClick = { selectedTab = 1 }
                            )
                            // 短信 - 第3个
                            GlassNavButton(
                                emoji = "💬",
                                isSelected = selectedTab == 2,
                                onClick = { selectedTab = 2 }
                            )
                            // 设置 - 第4个
                            GlassNavButton(
                                emoji = "⚙️",
                                isSelected = selectedTab == 3,
                                onClick = { selectedTab = 3 }
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    when (selectedTab) {
                        0 -> ExpressScreen()
                        1 -> TagManageScreen()
                        2 -> SmsListScreen()
                        3 -> SettingsScreen(onLogout = onLogout)
                    }
                }
            }
        }
    }
    
    @Composable
    fun GlassNavButton(
        emoji: String,
        isSelected: Boolean,
        onClick: () -> Unit
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = if (isSelected) 
                        androidx.compose.ui.graphics.Color(0x4D667EEA) 
                    else 
                        androidx.compose.ui.graphics.Color(0x4DFFFFFF),
                    shape = CircleShape
                )
                .border(
                    width = 1.dp,
                    color = androidx.compose.ui.graphics.Color(0x80FFFFFF),
                    shape = CircleShape
                ),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSelected) 
                    androidx.compose.ui.graphics.Color(0x4D667EEA) 
                else 
                    androidx.compose.ui.graphics.Color(0x4DFFFFFF)
            ),
            contentPadding = PaddingValues(0.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            )
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
    
    @Composable
    fun BetaExpirationDialog(
        onContinue: () -> Unit,
        onFeedback: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onContinue,
            modifier = Modifier
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            containerColor = androidx.compose.ui.graphics.Color.White,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🔔 Beta 测试版到期提醒",
                        style = MaterialTheme.typography.headlineSmall,
                        color = androidx.compose.ui.graphics.Color(0xFF333333)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "感谢你在这段时间使用这款离线短信筛选工具！",
                        style = MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.ui.graphics.Color(0xFF555555),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = "当前版本的 Beta 测试期已结束（完全离线，不联网，不上传数据）。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.ui.graphics.Color(0xFF555555),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Text(
                        text = "为了让工具更好用，我们非常希望收集你的体验反馈：",
                        style = MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.ui.graphics.Color(0xFF333333),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // 邮箱
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📧 邮箱：",
                            style = MaterialTheme.typography.bodySmall,
                            color = androidx.compose.ui.graphics.Color(0xFF667EEA),
                            modifier = Modifier.width(80.dp)
                        )
                        Text(
                            text = "ChazRussel@outlook.com",
                            style = MaterialTheme.typography.bodySmall,
                            color = androidx.compose.ui.graphics.Color(0xFF333333)
                        )
                    }
                    
                    // QQ群
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💬 QQ 群：",
                            style = MaterialTheme.typography.bodySmall,
                            color = androidx.compose.ui.graphics.Color(0xFF667EEA),
                            modifier = Modifier.width(80.dp)
                        )
                        Text(
                            text = "1064696594",
                            style = MaterialTheme.typography.bodySmall,
                            color = androidx.compose.ui.graphics.Color(0xFF333333)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "你可以选择继续使用当前功能，也可以加入群聊参与新版测试！",
                        style = MaterialTheme.typography.bodySmall,
                        color = androidx.compose.ui.graphics.Color(0xFF666666),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onFeedback,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFF667EEA)
                    )
                ) {
                    Text(
                        text = "【提交反馈】",
                        style = MaterialTheme.typography.labelMedium,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFFE8E8E8)
                    )
                ) {
                    Text(
                        text = "【继续使用 Beta 版本】",
                        style = MaterialTheme.typography.labelMedium,
                        color = androidx.compose.ui.graphics.Color(0xFF333333)
                    )
                }
            }
        )
    }
}
