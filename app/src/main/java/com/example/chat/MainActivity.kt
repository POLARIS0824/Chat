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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.chat.model.ChatSession
import java.text.SimpleDateFormat
import android.app.Application
import android.view.WindowManager
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay

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
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

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
                                icon = {
                                    Icon(
                                        painter = painterResource(
                                            id = if (currentScreen == item.screen)
                                                item.selectedIcon
                                            else
                                                item.unselectedIcon
                                        ),
                                        contentDescription = item.title
                                    )
                                },
                                label = { Text(item.title) },
                                selected = currentScreen == item.screen,
                                onClick = { currentScreen = item.screen }
                            )
                        }
                    }
                }
            ) { padding ->
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                ) {
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
    NavItem(
        Screen.Chat,
        "聊天",
        R.drawable.chat_outline,
        R.drawable.chat_fill),
    NavItem(
        Screen.Cards,
        "名片夹",
        R.drawable.par_outline,
        R.drawable.par_fill),
    NavItem(
        Screen.Notes,
        "便利贴",
        R.drawable.bag_outline,
        R.drawable.bag_fill),
    NavItem(
        Screen.Social,
        "萌友圈",
        R.drawable.bag_outline,
        R.drawable.bag_fill)
)

// 导航项数据类
private data class NavItem(
    val screen: Screen,
    val title: String,
    val unselectedIcon: Int,
    val selectedIcon: Int
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

@OptIn(ExperimentalFoundationApi::class)
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

    val frames = listOf(
        R.drawable.frame1,
        R.drawable.frame2,
        R.drawable.frame3,
        R.drawable.frame4,
        R.drawable.frame5,
        R.drawable.frame6,
        R.drawable.frame7,
        R.drawable.frame8,
        R.drawable.frame9,
        R.drawable.frame10
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
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

                Box(
                    modifier = Modifier
                        .padding(start = 24.dp, top = 24.dp)
                        .zIndex(1f)
                ) {
                    AnimatedAvatar(
                        frameResIds = frames,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                }
            }

            ChatInput(
                message = message,
                onMessageChange = { message = it },
                onSendClick = {
                    if (message.isNotEmpty()) {
                        viewModel.sendMessage(message)
                        message = ""
                        coroutineScope.launch {
                            listState.animateScrollToItem(viewModel.getChatHistory(petType).size - 1)
                        }
                    }
                }
            )
        }
    }

    if (showSettings) {
        SettingsDialog(
            onDismiss = { showSettings = false }
        )
    }
}

@Composable
fun AnimatedAvatar(
    frameResIds: List<Int>,
    modifier: Modifier = Modifier,
    frameDelay: Long = 150L
) {
    var currentFrame by remember { mutableStateOf(0) }

// 添加过渡动画
    val transition = updateTransition(
        targetState = currentFrame,
        label = "Avatar Animation"
    )

    val alpha by transition.animateFloat(
        label = "Alpha",
        transitionSpec = { tween(frameDelay.toInt() / 2) }
    ) { frame ->
        1f
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(frameDelay)
            currentFrame = (currentFrame + 1) % frameResIds.size
        }
    }

    Image(
        painter = painterResource(id = frameResIds[currentFrame]),
        contentDescription = null,
        modifier = modifier
            .clip(CircleShape)
            .alpha(alpha),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val isFromUser = message.isFromUser
    val backgroundColor = if (isFromUser)
        Color(239,243,255)
    else
        Color(243,243,243)

    val textColor = if (isFromUser)
        MaterialTheme.colorScheme.onSurface
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    val arrangement = if (isFromUser)
        Arrangement.End else Arrangement.Start

    val bubbleShape = if (isFromUser)
        RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp)
    else
        RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)

    val timeString = SimpleDateFormat("HH:mm", Locale.getDefault())
        .format(System.currentTimeMillis())

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = arrangement
    ) {
        Column(
            horizontalAlignment = if (isFromUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = bubbleShape,
                color = backgroundColor,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 8.dp
                    )
                ) {
                    Text(
                        text = message.content,
                        color = textColor,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = timeString,
                        color = textColor.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
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
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 表情按钮
            IconButton(
                onClick = { /* TODO: 显示更多 */ }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_more),
                    contentDescription = "更多",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 输入框
            TextField(
                value = message,
                onValueChange = onMessageChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 1.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                placeholder = {
                    Text(
                        "Message...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true, // 确保单行输入
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Send // 设置IME动作为发送
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        onSendClick() // 用户点击发送按钮时触发发送逻辑
                    }
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Transparent,
                    unfocusedContainerColor = Transparent,
                    disabledContainerColor = Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedIndicatorColor = Transparent, // 有需要可以设置成其他颜色
                    unfocusedIndicatorColor = Transparent, // 有需要可以设置成其他颜色
                )
            )

            // 发送按钮
            IconButton(
                onClick = onSendClick,
                enabled = message.isNotEmpty()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_send),
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
            .padding(horizontal = 16.dp)
    ) {
        // 用户信息区域
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
//                // 用户头像
//                Image(
//                    painter = painterResource(id = R.drawable.avatar_placeholder), // 替换成你的默认头像
//                    contentDescription = null,
//                    modifier = Modifier
//                        .size(60.dp)
//                        .clip(CircleShape)
//                )

                Spacer(modifier = Modifier.width(12.dp))

                // 用户名和认证标识
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mrh Raju",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.width(4.dp))
//                        Icon(
//                            painter = painterResource(id = R.drawable.ic_verified), // 替换成你的认证图标
//                            contentDescription = "已认证",
//                            tint = Color(0xFF00C853),
//                            modifier = Modifier.size(16.dp)
//                        )
                    }
                    Text(
                        text = "Verified Profile",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }

//        // 深色模式开关
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 16.dp),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Icon(
//                    painter = painterResource(id = R.drawable.ic_dark_mode), // 替换成你的深色模式图标
//                    contentDescription = "深色模式",
//                    modifier = Modifier.size(24.dp)
//                )
//                Spacer(modifier = Modifier.width(12.dp))
//                Text(
//                    text = "深色模式",
//                    style = MaterialTheme.typography.bodyLarge
//                )
//            }
//            Switch(
//                checked = false, // 这里需要绑定实际的深色模式状态
//                onCheckedChange = { /* 处理深色模式切换 */ }
//            )
//        }

        // 设置选项列表
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            DrawerMenuItem(
                icon = R.drawable.ic_account,
                text = "账号信息"
            )
            DrawerMenuItem(
                icon = R.drawable.ic_password,
                text = "密码设置"
            )
            DrawerMenuItem(
                icon = R.drawable.ic_favorite,
                text = "偏好设置"
            )
            DrawerMenuItem(
                icon = R.drawable.ic_settings,
                text = "系统设置"
            )
        }

        // 退出登录按钮
        Spacer(modifier = Modifier.weight(1f))
        TextButton(
            onClick = { /* 处理退出登录 */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_logout), // 替换成你的退出图标
                    contentDescription = "退出登录",
                    tint = Color(0xFFFF5252)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "退出登录",
                    color = Color(0xFFFF5252),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun DrawerMenuItem(
    icon: Int,
    text: String,
    onClick: () -> Unit = {}
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = text,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge
            )
        }
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