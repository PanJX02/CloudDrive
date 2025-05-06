package com.panjx.clouddrive.feature.profile.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.panjx.clouddrive.feature.profile.ProfileViewModel
import com.panjx.clouddrive.feature.profile.components.widgets.PasswordInput
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPasswordRoute(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("修改密码") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            // 验证输入
                            when {
                                oldPassword.isBlank() -> {
                                    scope.launch { snackbarHostState.showSnackbar("请输入当前密码") }
                                }
                                newPassword.isBlank() -> {
                                    scope.launch { snackbarHostState.showSnackbar("请输入新密码") }
                                }
                                newPassword != confirmPassword -> {
                                    scope.launch { snackbarHostState.showSnackbar("两次密码输入不一致") }
                                }
                                newPassword.length < 6 -> {
                                    scope.launch { snackbarHostState.showSnackbar("密码长度至少6位") }
                                }
                                else -> {
                                    isLoading = true
                                    scope.launch {
                                        try {
                                            val result = viewModel.updatePassword(oldPassword, newPassword)
                                            if (result.first) {
                                                snackbarHostState.showSnackbar("密码修改成功")
                                                onNavigateBack()
                                            } else {
                                                snackbarHostState.showSnackbar("密码修改失败: ${result.second ?: "请重试"}")
                                            }
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar("密码修改失败: ${e.message}")
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Done, contentDescription = "保存")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                
                // 当前密码输入
                PasswordInput(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = "当前密码",
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 新密码输入
                PasswordInput(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = "新密码",
                    supportingText = "密码至少6位",
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 确认新密码
                PasswordInput(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "确认新密码",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
} 