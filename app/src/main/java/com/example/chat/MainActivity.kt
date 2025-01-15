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
import androidx.compose.foundation.Image
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.pm.PackageInfoCompat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Send
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.chat.model.ChatSession
import java.text.SimpleDateFormat
import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.lazy.rememberLazyListState
//import androidx.compose.material.icons.filled.Pets

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher: ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // 处理权限结果
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置沉浸式状态栏
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 检查并请求通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        lifecycleScope.launch {
            // 可以在这里安全地调用挂起函数
        }

        setContent {
            MaterialTheme {
                PetChatApp()
            }
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
    var currentScreen by remember { mutableStateOf(Screen.Chat) }
    var currentPetType by remember { mutableStateOf(PetTypes.CAT) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    MaterialTheme {
        ModalNavigationDrawer(
            drawerContent = {
                ModalDrawerSheet {
                    DrawerContent(
                        currentPetType = currentPetType,
                        onPetTypeSelected = {
                            currentPetType = it
                            scope.launch { drawerState.close() }
                        },
                        onClose = {
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            },
            drawerState = drawerState,
            gesturesEnabled = true,
            scrimColor = Color.Black.copy(alpha = 0.32f) // Material 3 的标准值
        ) {
            Scaffold(
                topBar = {
                    when (currentScreen) {
                        Screen.Chat -> {
                            TopAppBar(
                                title = { Text("聊天") },
                                navigationIcon = {
                                    IconButton(onClick = {
                                        scope.launch { drawerState.open() }
                                    }) {
                                        Icon(Icons.Filled.Menu, contentDescription = "打开抽屉菜单")
                                    }
                                },
                                actions = {
                                    IconButton(onClick = { /* 打开设置 */ }) {
                                        Icon(Icons.Filled.Settings, contentDescription = "设置")
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                        Screen.Cards -> {
                            TopAppBar(
                                title = { Text("名片夹") },
                                navigationIcon = {
                                    IconButton(onClick = {
                                        // 处理返回或其他导航
                                        // 例如，返回到聊天界面
                                        currentScreen = Screen.Chat
                                    }) {
                                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                                    }
                                },
                                actions = { /* 其他操作 */ },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                        Screen.Notes -> {
                            TopAppBar(
                                title = { Text("便利贴") },
                                navigationIcon = {
                                    IconButton(onClick = {
                                        // 处理返回或其他导航
                                        // 例如，返回到聊天界面
                                        currentScreen = Screen.Chat
                                    }) {
                                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                                    }
                                },
                                actions = { /* 其他操作 */ },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                        Screen.Social -> {
                            TopAppBar(
                                title = { Text("萌友圈") },
                                navigationIcon = {
                                    IconButton(onClick = {
                                        // 处理返回或其他导航
                                        // 例如，返回到聊天界面
                                        currentScreen = Screen.Chat
                                    }) {
                                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                                    }
                                },
                                actions = { /* 其他操作 */ },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                },
                bottomBar = {
                    NavigationBar {
                        BottomNavItems.forEach { item ->
                            NavigationBarItem(
                                icon = { Icon(item.icon, contentDescription = item.title) },
                                label = { Text(item.title) },
                                selected = currentScreen == item.screen,
                                onClick = { currentScreen = item.screen }
                            )
                        }
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    when (currentScreen) {
                        Screen.Chat -> {
                            ChatScreen(
                                viewModel = viewModel,
                                petType = currentPetType,
                                onDrawerClick = {
                                    scope.launch { drawerState.open() }
                                }
                            )
                        }
                        Screen.Cards -> CardsScreen()
                        Screen.Notes -> NotesScreen()
                        Screen.Social -> { /* 萌友圈暂不实现 */ }
                    }
                }
            }
        }
    }
}

// 底部导航项
private val BottomNavItems = listOf(
    NavItem(Screen.Chat, "聊天", Icons.Filled.Email),
    NavItem(Screen.Cards, "名片夹", Icons.Filled.AccountBox),
    NavItem(Screen.Notes, "便利贴", Icons.Filled.Create),
    NavItem(Screen.Social, "萌友圈", Icons.Filled.AccountCircle)
)

// 导航项数据类
private data class NavItem(
    val screen: Screen,
    val title: String,
    val icon: ImageVector
)

// 屏幕枚举
private enum class Screen {
    Chat, Cards, Notes, Social
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSessionList(
    onDrawerClick: () -> Unit,
    onSessionSelected: (ChatSession) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Pet Chat",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
//                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                navigationIcon = {
                    IconButton(onClick = onDrawerClick) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Localized description"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(PetTypes.values()) { petType ->
                ChatSessionItem(
                    session = ChatSession(
                        id = petType.name,
                        petType = petType,
                        displayName = petType.displayName,
                        avatarRes = when(petType) {
                            PetTypes.CAT -> R.drawable.ic_cat_avatar
                            PetTypes.DOG -> R.drawable.ic_dog_avatar
                        }
                    ),
                    onClick = { onSessionSelected(it) }
                )
            }
        }
    }
}

@Composable
fun ChatSessionItem(
    session: ChatSession,
    onClick: (ChatSession) -> Unit
) {
    Surface(
        onClick = { onClick(session) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .height(72.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 头像
            Image(
                painter = painterResource(id = session.avatarRes),
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 会话信息
            Column {
                Text(
                    text = session.displayName,
                    style = MaterialTheme.typography.titleMedium
                )
                if (session.lastMessage.isNotEmpty()) {
                    Text(
                        text = session.lastMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class
)
@Composable
fun ChatScreen(
    viewModel: PetChatViewModel,
    petType: PetTypes,
    onDrawerClick: () -> Unit
) {
    var showSettings by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Column {
        // 顶部栏
//        TopAppBar(
//            title = {
//                Text(
//                    "聊天",
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//            },
//            navigationIcon = {
//                IconButton(onClick = onDrawerClick) {
//                    Icon(
//                        imageVector = Icons.Filled.Menu,
//                        contentDescription = "Localized description"
//                    )
//                }
//            },
//            actions = {
//                IconButton(onClick = { showSettings = true }) {
//                    Icon(
//                        imageVector = Icons.Filled.Settings,
//                        contentDescription = "Localized description"
//                    )
//                }
//            },
//            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
//                containerColor = MaterialTheme.colorScheme.primaryContainer,
//                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
//                navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
//                actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
//            ),
////            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
//        )

        // 聊天内容
        Box(
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    //reverseLayout = true
                ) {
                    items(
                        items = viewModel.getChatHistory(petType),
                        key = { it.hashCode() }
                    ) { message ->
                        ChatBubble(
                            message = message,
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }

                ChatInput(
                    message = message,
                    onMessageChange = { message = it },
                    onSendClick = {
                        if (message.isNotEmpty()) {
                            viewModel.sendMessage(message, petType)
                            message = ""
                            coroutineScope.launch {
                                listState.animateScrollToItem(viewModel.getChatHistory(petType).size - 1)
                            }
                        }
                    }
                )
            }
        }
    }

    if (showSettings) {
        SettingsDialog(
            onDismiss = { showSettings = false }
        )
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val bubbleColor = if (message.isFromUser)
        Color(0xFF0084FF) // Telegram蓝色
    else Color(0xFFEEEEEE) // 浅灰色背景

    val textColor = if (message.isFromUser)
        Color.White else Color.Black

    val bubbleShape = if (message.isFromUser)
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    else RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)

    val arrangement = if (message.isFromUser)
        Arrangement.End else Arrangement.Start

    val timeString = remember(message.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(message.timestamp)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = arrangement
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            modifier = Modifier.widthIn(max = 280.dp),
            shape = bubbleShape
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = message.content,
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge
                )

                // 消息时间
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (message.isFromUser) Color.White.copy(alpha = 0.7f)
                    else Color.Gray,
                    modifier = Modifier.align(Alignment.End)
                )
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
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 表情按钮
            IconButton(
                onClick = { /* TODO: 显示表情选择器 */ }
            ) {
                Icon(
                    Icons.Default.Face,
                    contentDescription = "表情",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 输入框
            BasicTextField(
                value = message,
                onValueChange = onMessageChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                decorationBox = { innerTextField ->
                    Box {
                        if (message.isEmpty()) {
                            Text(
                                "Message...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        innerTextField()
                    }
                }
            )

            // 发送按钮
            IconButton(
                onClick = onSendClick,
                enabled = message.isNotEmpty()
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "发送",
                    tint = if (message.isNotEmpty())
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsScreen() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
//        // 顶部栏
//        TopAppBar(
//            title = {
//                Text(
//                    "名片夹",
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//            },
//            colors = TopAppBarDefaults.largeTopAppBarColors(
//                containerColor = MaterialTheme.colorScheme.primaryContainer,
//                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
//                navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
//            ),
////            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
//        )

        // 照片和基础信息区域
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 照片区域
            Card(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .padding(end = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("照片")
                }
            }

            // 基础信息区域
            Card(
                modifier = Modifier
                    .weight(2f)
                    .aspectRatio(2f)
                    .padding(start = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("基础信息")
                }
            }
        }

        // 详细信息区域
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("详细信息")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PetChatApp()
}

@Preview(showBackground = true, name = "聊天列表预览")
@Composable
fun ChatSessionListPreview() {
    MaterialTheme {
        ChatSessionList(
            onDrawerClick = {},
            onSessionSelected = {}
        )
    }
}

@Preview(showBackground = true, name = "聊天界面预览")
@Composable
fun ChatScreenPreview() {
    MaterialTheme {
        ChatScreen(
            viewModel = PetChatViewModel(Application()),
            petType = PetTypes.CAT,
            onDrawerClick = {}
        )
    }
}

@Preview(showBackground = true, name = "名片夹预览")
@Composable
fun CardsScreenPreview() {
    MaterialTheme {
        CardsScreen()
    }
}

@Preview(showBackground = true, name = "底部导航栏预览")
@Composable
fun BottomNavigationPreview() {
    MaterialTheme {
        NavigationBar {
            BottomNavItems.forEach { item ->
                NavigationBarItem(
                    icon = { Icon(item.icon, contentDescription = item.title) },
                    label = { Text(item.title) },
                    selected = item.screen == Screen.Chat,
                    onClick = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "聊天输入框预览")
@Composable
fun ChatInputPreview() {
    MaterialTheme {
        ChatInput(
            message = "Hello, World!",
            onMessageChange = {},
            onSendClick = {}
        )
    }
}

@Composable
private fun DrawerContent(
    currentPetType: PetTypes,
    onPetTypeSelected: (PetTypes) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .padding(top = 16.dp)
    ) {
        // 用户头像和信息
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(
                    id = when(currentPetType) {
                        PetTypes.CAT -> R.drawable.ic_cat_avatar
                        PetTypes.DOG -> R.drawable.ic_dog_avatar
                    }
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = currentPetType.displayName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f))

        // 宠物选择
        Text(
            text = "选择宠物",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
        PetTypes.values().forEach { petType ->
            NavigationDrawerItem(
                icon = {
                    Icon(
                        when (petType) {
                            PetTypes.CAT -> Icons.Default.Build
                            PetTypes.DOG -> Icons.Default.Favorite
                        },
                        contentDescription = null
                    )
                },
                label = { Text(petType.displayName) },
                selected = currentPetType == petType,
                onClick = { onPetTypeSelected(petType) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }

        Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f))

        // 其他菜单项
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text("个人资料") },
            selected = false,
            onClick = { /* TODO */ },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text("设置") },
            selected = false,
            onClick = { /* TODO */ },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}

@Composable
private fun SettingsDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置") },
        text = {
            Column {
                // 添加设置选项
                Text("设置选项将在这里显示")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}