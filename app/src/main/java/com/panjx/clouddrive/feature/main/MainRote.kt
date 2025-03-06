package com.panjx.clouddrive.feature.main

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainRote(
    finishPage: () -> Unit
) {
    MainScreen(finishPage)
}

@Composable
fun MainScreen(
    finishPage: () -> Unit = {}
) {
    Button(
        onClick = {
            Log.d("MainScreen", "关闭页面")
            finishPage()
        },
        modifier = Modifier.padding(top = 100.dp)
    ) {
        Text("关闭")
    }
}
