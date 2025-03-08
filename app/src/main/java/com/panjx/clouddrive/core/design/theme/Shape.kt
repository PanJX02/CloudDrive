package com.panjx.clouddrive.core.design.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// 基础形状集合
val replyShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)
// 扩展形状 -------------------------------------------------
// 圆形（用于头像/图标）
val CircleShape = RoundedCornerShape(50) // 百分比参数

// 顶部圆角卡片
val CardTopShape = RoundedCornerShape(
    topStart = 16.dp,
    topEnd = 16.dp,
    bottomEnd = 0.dp,
    bottomStart = 0.dp
)

// 底部导航栏形状
val BottomNavShape = RoundedCornerShape(
    topStart = 20.dp,
    topEnd = 20.dp,
    bottomEnd = 0.dp,
    bottomStart = 0.dp
)

// 标签页指示器形状
val TabIndicatorShape = RoundedCornerShape(
    topStart = 8.dp,
    topEnd = 8.dp,
    bottomStart = 0.dp,
    bottomEnd = 0.dp
)

// 不对称圆角按钮
val AsymmetricButtonShape = RoundedCornerShape(
    topStart = 12.dp,
    bottomEnd = 12.dp,
    topEnd = 4.dp,
    bottomStart = 4.dp
)

// 直角形状（用于分割线等）
val SharpShape = RoundedCornerShape(0.dp)