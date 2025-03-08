package com.panjx.clouddrive.core.design.theme

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 间距系统基础单位（4dp基准）
 * 通过倍数和细分单位实现灵活间距控制
 */
val SpaceUnit = 4.dp
val HalfSpaceUnit = 1.dp // 新增细分单位

// ----------------- 超小间距 -----------------
/** 极小间距（0.5倍基础单位） */
val SpaceXXXSmall = HalfSpaceUnit * 1  // 1.dp

/** 超小间距（1倍基础单位） */
val SpaceXXSmall = HalfSpaceUnit * 2     // 2.dp

/** 微小间距（1倍细分单位） */
val SpaceTiny = HalfSpaceUnit * 3     // 3.dp

// ----------------- 标准间距系统 -----------------
/** 小间距（1.5倍基础单位）*/
val SpaceSmall = SpaceUnit + HalfSpaceUnit // 6.dp

/** 中等间距（2倍基础单位）*/
val SpaceMedium = SpaceUnit * 2      // 8.dp

/** 大间距（3倍基础单位）*/
val SpaceLarge = SpaceUnit * 3       // 12.dp

// ----------------- 扩展间距系统 -----------------
val SpaceXLarge = SpaceUnit * 4      // 16.dp
val Space2XLarge = SpaceUnit * 6     // 24.dp
val Space3XLarge = SpaceUnit * 8     // 32.dp
val Space4XLarge = SpaceUnit * 12    // 48.dp

// ----------------- Composable间距组件 -----------------

/** 1dp间距组件（用于极精细布局）*/
@Composable
fun SpaceXXXSmall() {
    Spacer(modifier = Modifier.height(SpaceXXXSmall))
}

/** 2dp间距组件（用于极精细布局）*/
@Composable
fun SpaceXXSmall() {
    Spacer(modifier = Modifier.height(SpaceXXSmall))
}

/** 3dp间距组件（微小元素间隔）*/
@Composable
fun SpaceTiny() {
    Spacer(modifier = Modifier.height(SpaceTiny))
}

/** 4dp间距组件（次级小间距）*/
@Composable
fun SpaceSmall() {
    Spacer(modifier = Modifier.height(SpaceSmall))
}

/** 8dp间距组件（标准中等间距）*/
@Composable
fun SpaceMedium() {
    Spacer(modifier = Modifier.height(SpaceMedium))
}

/** 12dp间距组件（内容区块间隔）*/
@Composable
fun SpaceLarge() {
    Spacer(modifier = Modifier.height(SpaceLarge))
}

/** 16dp间距组件（大元素分隔）*/
@Composable
fun SpaceXLarge() {
    Spacer(modifier = Modifier.height(SpaceXLarge))
}

/** 24dp间距组件（页面级分隔）*/
@Composable
fun Space2XLarge() {
    Spacer(modifier = Modifier.height(Space2XLarge))
}

/** 32dp间距组件（超大留白区域）*/
@Composable
fun Space3XLarge() {
    Spacer(modifier = Modifier.height(Space3XLarge))
}

/** 48dp间距组件（极端留白场景）*/
@Composable
fun Space4XLarge() {
    Spacer(modifier = Modifier.height(Space4XLarge))
}