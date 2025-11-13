package com.sms.tagger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sms.tagger.ui.theme.*

/**
 * 毛玻璃卡片组件 (Frosted Glass Card)
 * 核心特性: 圆角极大、背景模糊、半透明、描边
 */
@Composable
fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .border(
                width = 1.dp,
                color = GlassBorder,
                shape = RoundedCornerShape(cornerRadius)
            ),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = GlassFill
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp // 极微妙的阴影
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

/**
 * 渐变背景容器
 * 用于创建玻璃拟态风格的光晕效果
 */
@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundMain)
    ) {
        // 顶部黄色光晕
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GradientPurple.copy(alpha = 0.4f),
                            Color.Transparent
                        ),
                        radius = 200f
                    )
                )
                .align(androidx.compose.ui.Alignment.TopEnd)
        )
        
        // 底部粉色光晕
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GradientPink.copy(alpha = 0.3f),
                            Color.Transparent
                        ),
                        radius = 250f
                    )
                )
                .align(androidx.compose.ui.Alignment.BottomStart)
        )
        
        // 内容层
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

/**
 * 胶囊形按钮/标签 (Pill Button/Tab)
 * 用于标签选择器
 */
@Composable
fun PillButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(18.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = if (isSelected) AccentActive else AccentInactive,
            contentColor = if (isSelected) Color.White else TextSecondary
        ),
        elevation = androidx.compose.material3.ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        androidx.compose.material3.Text(
            text = text,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

/**
 * 玻璃拟态表面 (Glass Surface)
 * 用于创建具有模糊效果的表面
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(GlassFill)
            .border(
                width = 1.dp,
                color = GlassBorder,
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(12.dp)
    ) {
        content()
    }
}
