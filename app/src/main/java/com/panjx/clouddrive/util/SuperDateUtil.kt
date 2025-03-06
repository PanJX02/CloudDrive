package com.panjx.clouddrive.util

import java.util.Calendar

object SuperDateUtil {
    /**
     * 获取当前年
     */
    fun currentYear(): Int {
        return Calendar.getInstance().get(Calendar.YEAR)
    }
}