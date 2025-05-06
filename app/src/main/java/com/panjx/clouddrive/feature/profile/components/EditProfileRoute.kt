package com.panjx.clouddrive.feature.profile.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.panjx.clouddrive.feature.profile.ProfileViewModel
import com.panjx.clouddrive.feature.profile.components.dialogs.NicknameEditDialog
import com.panjx.clouddrive.feature.profile.components.dialogs.PasswordEditDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileRoute(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val userState by viewModel.userState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // 弹窗状态
    var showNicknameDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑个人资料") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        snackbarHost = { 
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column {
                    // 修改昵称选项
                    ListItem(
                        headlineContent = { Text("修改昵称") },
                        supportingContent = { Text("当前昵称: ${userState.userInfo?.nickname ?: "未设置"}") },
                        leadingContent = { 
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null
                            )
                        },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Default.ArrowForwardIos,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.clickable { showNicknameDialog = true }
                    )
                    
                    Divider()
                    
                    // 修改密码选项
                    ListItem(
                        headlineContent = { Text("修改密码") },
                        supportingContent = { Text("定期修改密码可提高账号安全性") },
                        leadingContent = { 
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null
                            )
                        },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Default.ArrowForwardIos,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.clickable { showPasswordDialog = true }
                    )
                }
            }
        }
    }
    
    // 昵称修改弹窗
    if (showNicknameDialog) {
        NicknameEditDialog(
            initialNickname = userState.userInfo?.nickname ?: "",
            onDismiss = { showNicknameDialog = false },
            onConfirm = { newNickname ->
                scope.launch {
                    try {
                        showNicknameDialog = false
                        val result = viewModel.updateUserInfo(
                            nickname = newNickname,
                            email = userState.userInfo?.email ?: "",
                            avatar = userState.userInfo?.avatar ?: ""
                        )
                        if (result) {
                            snackbarHostState.showSnackbar("昵称修改成功")
                        } else {
                            snackbarHostState.showSnackbar("昵称修改失败，请重试")
                        }
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("修改失败: ${e.message}")
                    }
                }
            }
        )
    }
    
    // 密码修改弹窗
    if (showPasswordDialog) {
        var passwordError by remember { mutableStateOf<String?>(null) }
        
        PasswordEditDialog(
            onDismiss = { showPasswordDialog = false },
            onConfirm = { oldPassword, newPassword ->
                // 每次点击确认按钮时，先重置错误状态
                passwordError = null
                
                scope.launch {
                    try {
                        val result = viewModel.updatePassword(oldPassword, newPassword)
                        if (result.first) {
                            // 成功时关闭弹窗并显示成功消息
                            showPasswordDialog = false
                            snackbarHostState.showSnackbar("密码修改成功")
                        } else {
                            // 设置错误消息，不关闭弹窗
                            passwordError = result.second
                        }
                    } catch (e: Exception) {
                        // 设置错误消息，不关闭弹窗
                        passwordError = e.message ?: "未知错误"
                    }
                }
            },
            externalError = passwordError,
            canSubmitWithSameError = true  // 允许相同错误时重新提交
        )
        
        // 监听错误状态，处理非特定错误
        LaunchedEffect(passwordError) {
            passwordError?.let { error ->
                if (!(error.contains("原密码不正确") || error.contains("新密码不能与原密码相同"))) {
                    // 对于非特定错误，关闭弹窗并显示snackbar
                    showPasswordDialog = false
                    snackbarHostState.showSnackbar("修改失败: $error")
                }
            }
        }
    }
} 