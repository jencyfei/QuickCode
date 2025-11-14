package com.sms.tagger.ui.theme

import androidx.compose.ui.graphics.Color

// Soft Glassmorphism 柔和玻璃拟态风格 - 与网页版保持一致
// 参考: style_library.json 中的 "soft_glassmorphism" 风格

// 背景色 - 极浅的薰衣草/近白色
val BackgroundMain = Color(0xFFF9F8FF) // 极浅的薰衣草色

// 渐变色 - 用于光晕效果
val GradientPink = Color(0xFFFAD0C4) // 柔和粉
val GradientPurple = Color(0xFFD9C8FF) // 柔和紫

// 玻璃拟态效果色 - 突出玻璃效果
val GlassFill = Color(0x80FFFFFF) // 白色, 50%透明度 - 用于卡片填充（从40%增加到50%）
val GlassBorder = Color(0xB3FFFFFF) // 白色, 70%透明度 - 用于描边（从60%增加到70%）

// 文字颜色
val TextPrimary = Color(0xFF333333) // 深灰色
val TextSecondary = Color(0xFF8A8A8A) // 中度灰色

// 强调色
val AccentActive = Color(0xFF333333) // 深灰 - 用于选中的标签
val AccentInactive = Color(0xFFE0E0E0) // 浅灰 - 用于未选中的标签

// 状态色 - 柔和版本
val StatusSuccess = Color(0xFFC8E6C9) // 柔和绿
val StatusWarning = Color(0xFFFFE0B2) // 柔和橙
val StatusError = Color(0xFFFFCDD2) // 柔和红
val StatusInfo = Color(0xFFBBDEFB) // 柔和蓝
