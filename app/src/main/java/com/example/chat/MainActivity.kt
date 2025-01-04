package com.example.chat

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.pm.PackageInfoCompat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chat.model.ChatMessage
import com.example.chat.model.PetTypes
import kotlinx.coroutines.launch
import java.util.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.chat.ui.NotesScreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person


class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher: ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // 处理权限结果
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 检查并请求通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        lifecycleScope.launch {  // 使用 lifecycleScope 创建协程
            // 在这里可以安全地调用挂起函数
        }
        
        setContent {
            PetChatApp()
        }
    }

    override fun onPause() {
        super.onPause()
        // 保存当前状态
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PetChatApp(viewModel: PetChatViewModel = viewModel()) {
    var message by remember { mutableStateOf("") }
    // 添加导航状态
    var currentScreen by remember { mutableStateOf(Screen.Chat) }

    MaterialTheme {
        // 添加深色模式支持
        // 添加动态颜色支持
        Scaffold(
            // 添加宠物类型选择器到顶部
            topBar = {
                TopAppBar(
                    title = { Text(currentScreen.title) },
                    actions = {
                        // 只在聊天界面显示宠物选择器
                        if (currentScreen == Screen.Chat) {
                            PetTypeSelector(
                                currentType = viewModel.currentPetType,
                                onTypeSelected = { viewModel.selectPetType(it) }
                            )
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    Screen.values().forEach { screen ->
                        NavigationBarItem(
                            selected = currentScreen == screen,
                            onClick = { currentScreen = screen },
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        ) { paddingValues ->
            // 根据当前选中的屏幕显示不同的内容
            when (currentScreen) {
                Screen.Chat -> ChatScreen(
                    viewModel = viewModel,
                    message = message,
                    onMessageChange = { message = it },
                    modifier = Modifier.padding(paddingValues)
                )
                Screen.Notes -> NotesScreen(
                    modifier = Modifier.padding(paddingValues)
                )
                Screen.Contacts -> {
                    // TODO: 实现联系人界面
                }
            }
        }
    }
}

// 添加屏幕枚举
enum class Screen(val title: String, val icon: ImageVector) {
    Chat("聊天", Icons.Filled.Email),
    Notes("便利贴", Icons.Filled.List),
    Contacts("名片夹", Icons.Filled.Person)
}

// 添加宠物类型选择器
@Composable
fun PetTypeSelector(
    currentType: PetTypes,
    onTypeSelected: (PetTypes) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PetTypes.values().forEach { type ->
            FilterChip(
                selected = currentType == type,
                onClick = { onTypeSelected(type) },
                label = { Text(type.displayName) }
            )
        }
    }
}

// 抽取聊天界面为单独的组件
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(
    viewModel: PetChatViewModel,
    message: String,
    onMessageChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = viewModel.chatHistory,
                key = { message -> message.hashCode() }
            ) { message ->
                ChatBubble(
                    message = message,
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }

        ChatInput(
            message = message,
            onMessageChange = onMessageChange,
            onSendClick = {
                if (message.isNotEmpty()) {
                    viewModel.sendMessage(message)
                    onMessageChange("")
                }
            }
        )
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val bubbleColor = if (message.isFromUser)
        MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.secondary
    
    val alignment = if (message.isFromUser)
        Arrangement.End else Arrangement.Start

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = alignment
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            modifier = Modifier.widthIn(max = 340.dp)  // 限制气泡最大宽度
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = message.content,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyLarge
                )
                
//                if (message.imageUrl.isNotEmpty()) {
//                    Spacer(modifier = Modifier.height(8.dp))
//                    AsyncImage(
//                        model = message.imageUrl,
//                        contentDescription = null,
//                        modifier = Modifier
//                            .size(200.dp)
//                            .clip(MaterialTheme.shapes.medium),
//                        contentScale = ContentScale.Crop
//                    )
//                }
            }
        }
    }
}

@Composable
fun ChatInput(
    message: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // TODO: 添加更多输入选项的图标(语音、表情等)
            TextField(
                value = message,
                onValueChange = onMessageChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                placeholder = { Text("输入消息...") },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            
            Button(
                onClick = onSendClick,
                enabled = message.isNotEmpty()
            ) {
                Text("发送")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PetChatApp()
}