package com.panjx.clouddrive.core.design.theme

import androidx.compose.ui.graphics.Color

/*---------------- 基础颜色 (浅色主题) ----------------*/
// 明亮紫色，用于浅色主题主色（接近 Material Purple 100 #E1BEE7）
val Purple80 = Color(0xFFD0BCFF)
// 浅紫灰色，用于浅色主题次级元素（接近 Material Purple Grey 200 #B0BEC5）
val PurpleGrey80 = Color(0xFFCCC2DC)
// 柔和粉色，用于浅色主题强调色（接近 Material Pink 200 #F48FB1）
val Pink80 = Color(0xFFEFB8C8)

/*---------------- 基础颜色 (深色主题) ----------------*/
// 深邃紫色，用于深色主题主色（接近 Material Purple 700 #7B1FA2）
val Purple40 = Color(0xFF6650a4)
// 深紫灰色，用于深色主题背景/边框（接近 Material Purple Grey 700 #455A64）
val PurpleGrey40 = Color(0xFF625b71)
// 暗玫瑰色，用于深色主题强调色（接近 Material Pink 800 #AD1457）
val Pink40 = Color(0xFF7D5260)

/*---------------- 扩展颜色（原有代码保留） ----------------*/
/* 红色系 - 5种深浅 */
val Red10 = Color(0xFFFFEBEE)   // 极浅红 (Material Red 50)
val Red80 = Color(0xFFEF9A9A)   // 浅红 (Material Red 200)
val Red100 = Color(0xFFE57373)  // 标准红 (Material Red 300)
val Red200 = Color(0xFFEF5350)  // 强调红 (Material Red 400)
val Red40 = Color(0xFFD32F2F)   // 深红 (Material Red 700)

/* 粉色系 - 4种层次 */
val Pink10 = Color(0xFFFCE4EC)  // 极浅粉 (Material Pink 50)
val Pink100 = Color(0xFFF48FB1) // 标准粉 (Material Pink 300)
val Pink200 = Color(0xFFEC407A) // 玫粉色 (Material Pink 400)
val PinkDark = Color(0xFFAD1457) // 深粉 (Material Pink 800)

/* 紫色系 - 扩展3种 */
val Purple10 = Color(0xFFF3E5F5) // 极浅紫 (Material Purple 50)
val Purple100 = Color(0xFFBA68C8) // 标准紫 (Material Purple 300)
val Purple200 = Color(0xFF9C27B0) // 紫罗兰 (Material Purple 500)

/* 蓝色系 - 6种层次 */
val Blue10 = Color(0xFFE3F2FD)   // 极浅蓝 (Material Blue 50)
val Blue80 = Color(0xFF90CAF9)   // 浅蓝 (Material Blue 200)
val Blue100 = Color(0xFF64B5F6)  // 天蓝 (Material Blue 300)
val Blue200 = Color(0xFF2196F3)  // 标准蓝 (Material Blue 500)
val Blue300 = Color(0xFF1565C0)  // 深蓝 (Material Blue 800)
val Navy = Color(0xFF0D47A1)     // 藏青 (Material Blue 900)

/* 绿色系 - 5种层次 */
val Green10 = Color(0xFFE8F5E9)  // 极浅绿 (Material Green 50)
val Green80 = Color(0xFFA5D6A7)  // 浅绿 (Material Green 200)
val Green100 = Color(0xFF81C784) // 薄荷绿 (Material Green 300)
val Green200 = Color(0xFF4CAF50) // 标准绿 (Material Green 500)
val Green40 = Color(0xFF1B5E20)  // 深绿 (Material Green 900)

/* 黄色系 - 4种层次 */
val Yellow10 = Color(0xFFFFFDE7) // 极浅黄 (Material Yellow 50)
val Yellow80 = Color(0xFFFFF59D) // 浅黄 (Material Yellow 200)
val Yellow100 = Color(0xFFFFF176) // 柠檬黄 (Material Yellow 300)
val Yellow200 = Color(0xFFFFEB3B) // 标准黄 (Material Yellow 500)

/* 橙色系 - 5种层次 */
val Orange10 = Color(0xFFFFF3E0) // 极浅橙 (Material Orange 50)
val Orange80 = Color(0xFFFFCC80) // 浅橙 (Material Orange 200)
val Orange100 = Color(0xFFFFB74D) // 蜜橙 (Material Orange 300)
val Orange200 = Color(0xFFFF9800) // 标准橙 (Material Orange 500)
val Orange40 = Color(0xFFE65100)  // 深橙 (Material Orange 900)

/*---------------- 中性灰色系（12种层次） ----------------*/
// 极浅灰组（适合背景/留白）
val Gray0 = Color(0xFFFFFFFF)    // 纯白（Material Grey 50替代方案）
val Gray10 = Color(0xFFFAFAFA)   // 雪白（Material Grey 50）
val Gray20 = Color(0xFFF5F5F5)   // 雾白（Material Grey 100）
val Gray30 = Color(0xFFEEEEEE)   // 银灰（Material Grey 200）

// 中等灰组（适合边框/分割线）
val Gray40 = Color(0xFFE0E0E0)   // 铅灰（Material Grey 300）
val Gray50 = Color(0xFFBDBDBD)   // 中灰（Material Grey 400）
val Gray60 = Color(0xFF9E9E9E)   // 岩灰（Material Grey 500）
val Gray70 = Color(0xFF757575)   // 炭灰（Material Grey 600）

// 深灰组（适合文本/图标）
val Gray80 = Color(0xFF616161)   // 石板灰（Material Grey 700）
val Gray90 = Color(0xFF424242)   // 深空灰（Material Grey 800）
val Gray95 = Color(0xFF303030)   // 夜灰（Material Grey 900）
val Gray100 = Color(0xFF000000)  // 纯黑（Material Grey 950替代）

/*---------------- 特殊灰色变体 ----------------*/
// 冷调灰（带蓝色倾向）
val CoolGray30 = Color(0xFFECEFF1) // Material Blue Grey 50
val CoolGray60 = Color(0xFF78909C) // Material Blue Grey 400

// 暖调灰（带棕色倾向）
val WarmGray30 = Color(0xFFEFEBE9) // Material Brown 50
val WarmGray60 = Color(0xFF8D6E63) // Material Brown 400

/* 棕色系 - 5种层次 */
val Brown10 = Color(0xFFEFEBE9)  // 极浅棕 (Material Brown 50)
val Brown80 = Color(0xFFBCAAA4)  // 浅棕 (Material Brown 200)
val Brown100 = Color(0xFFA1887F) // 沙棕 (Material Brown 300)
val Brown200 = Color(0xFF795548) // 标准棕 (Material Brown 500)
val Brown40 = Color(0xFF4E342E)  // 深棕 (Material Brown 800)