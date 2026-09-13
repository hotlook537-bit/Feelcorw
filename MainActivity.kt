package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.*
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.LulugramTheme
import com.example.ui.viewmodel.CallState
import com.example.ui.viewmodel.LulugramViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: LulugramViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LulugramTheme {
                val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()
                val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
                val feedPosts by viewModel.feedPosts.collectAsStateWithLifecycle()
                val activeStories by viewModel.activeStories.collectAsStateWithLifecycle()
                val reels by viewModel.reels.collectAsStateWithLifecycle()
                val conversations by viewModel.conversations.collectAsStateWithLifecycle()
                val currentMessages by viewModel.currentMessages.collectAsStateWithLifecycle()
                val notifications by viewModel.notifications.collectAsStateWithLifecycle()
                val callState by viewModel.callState.collectAsStateWithLifecycle()
                val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
                val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
                val isPlayingAudio by viewModel.audioPlayer.isPlaying.collectAsStateWithLifecycle()
                val playingAudioUrl by viewModel.audioPlayer.playingUrl.collectAsStateWithLifecycle()
                val savedPosts by viewModel.savedPosts.collectAsStateWithLifecycle()
                val activeCommentsPostId by viewModel.activeCommentsPostId.collectAsStateWithLifecycle()
                val postComments by viewModel.postComments.collectAsStateWithLifecycle()
                val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
                val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

                val snackbarHostState = remember { SnackbarHostState() }
                LaunchedEffect(statusMessage) {
                    statusMessage?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.clearStatusMessage()
                    }
                }

                var currentScreen by remember { mutableStateOf("home") } // "home", "explore", "reels", "profile", "chat_list", "chat_detail", "notifications"
                var showCreateContent by remember { mutableStateOf(false) }
                var showEditProfileDialog by remember { mutableStateOf(false) }

                if (currentUserId == null) {
                    AuthScreen(onLoginSuccess = { _, _ -> currentScreen = "home" })
                } else {
                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        topBar = {
                            if (currentScreen in listOf("home", "explore", "profile")) {
                                TopAppBar(
                                    title = {
                                        Text(
                                            text = "Lulugram",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 24.sp,
                                            color = GoldPrimary
                                        )
                                    },
                                    actions = {
                                        IconButton(onClick = { currentScreen = "notifications" }) {
                                            BadgedBox(
                                                badge = {
                                                    val unreadNotifs = notifications.count { !it.isRead }
                                                    if (unreadNotifs > 0) {
                                                        Badge { Text("$unreadNotifs") }
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.FavoriteBorder,
                                                    contentDescription = "Notifications",
                                                    tint = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                        IconButton(onClick = { currentScreen = "chat_list" }) {
                                            BadgedBox(
                                                badge = {
                                                    val unreadChats = conversations.sumOf { it.unreadCount }
                                                    if (unreadChats > 0) {
                                                        Badge { Text("$unreadChats") }
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.ChatBubbleOutline,
                                                    contentDescription = "Direct Messages",
                                                    tint = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
                                )
                            }
                        },
                        bottomBar = {
                            if (currentScreen in listOf("home", "explore", "reels", "profile")) {
                                NavigationBar(
                                    containerColor = DarkSurface,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ) {
                                    NavigationBarItem(
                                        selected = currentScreen == "home",
                                        onClick = { currentScreen = "home" },
                                        icon = {
                                            Icon(
                                                imageVector = if (currentScreen == "home") Icons.Filled.Home else Icons.Outlined.Home,
                                                contentDescription = "Home"
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = GoldPrimary,
                                            indicatorColor = Color.Transparent
                                        )
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == "explore",
                                        onClick = { currentScreen = "explore" },
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = "Explore"
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = GoldPrimary,
                                            indicatorColor = Color.Transparent
                                        )
                                    )
                                    NavigationBarItem(
                                        selected = false,
                                        onClick = { showCreateContent = true },
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Filled.AddBox,
                                                contentDescription = "Create",
                                                tint = GoldPrimary,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == "reels",
                                        onClick = { currentScreen = "reels" },
                                        icon = {
                                            Icon(
                                                imageVector = if (currentScreen == "reels") Icons.Filled.VideoLibrary else Icons.Outlined.VideoLibrary,
                                                contentDescription = "Reels"
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = GoldPrimary,
                                            indicatorColor = Color.Transparent
                                        )
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == "profile",
                                        onClick = { currentScreen = "profile" },
                                        icon = {
                                            UserAvatar(url = currentUser?.avatarUrl ?: "", size = 26.dp)
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = GoldPrimary,
                                            indicatorColor = Color.Transparent
                                        )
                                    )
                                }
                            }
                        }
                    ) { padding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                        ) {
                            when (currentScreen) {
                                "home" -> {
                                    HomeScreen(
                                        currentUser = currentUser,
                                        stories = activeStories,
                                        posts = feedPosts,
                                        onLikePost = { viewModel.toggleLikePost(it) },
                                        onSavePost = { viewModel.toggleSavePost(it) },
                                        onCommentClick = { viewModel.openCommentsForPost(it.id) },
                                        onSharePost = { /* share */ },
                                        onStoryClick = { /* view story */ },
                                        onAddStoryClick = { showCreateContent = true }
                                    )
                                }
                                "explore" -> {
                                    ExploreScreen(
                                        searchQuery = searchQuery,
                                        onQueryChange = { viewModel.setSearchQuery(it) },
                                        searchResults = searchResults,
                                        explorePosts = feedPosts,
                                        onUserClick = { user ->
                                            viewModel.startConversationWithUser(user) {
                                                currentScreen = "chat_detail"
                                            }
                                        },
                                        onPostClick = { /* view post */ }
                                    )
                                }
                                "reels" -> {
                                    ReelsScreen(
                                        reels = reels,
                                        onLikeReel = { viewModel.toggleLikeReel(it) }
                                    )
                                }
                                "profile" -> {
                                    ProfileScreen(
                                        user = currentUser,
                                        userPosts = feedPosts.filter { it.authorId == currentUserId },
                                        userReels = reels.filter { it.authorId == currentUserId },
                                        savedPosts = savedPosts,
                                        onEditProfileClick = { showEditProfileDialog = true },
                                        onSettingsClick = { viewModel.logout() }
                                    )
                                }
                                "chat_list" -> {
                                    ChatListScreen(
                                        conversations = conversations,
                                        onConversationClick = {
                                            viewModel.setActiveConversation(it.id)
                                            currentScreen = "chat_detail"
                                        },
                                        onNewChatClick = { currentScreen = "explore" }
                                    )
                                }
                                "chat_detail" -> {
                                    ChatDetailScreen(
                                        conversation = conversations.find { it.id == viewModel.activeConversationId.value },
                                        messages = currentMessages,
                                        currentUserId = currentUserId,
                                        onSendMessage = { text ->
                                            viewModel.activeConversationId.value?.let { cid ->
                                                viewModel.sendDirectMessage(cid, text)
                                            }
                                        },
                                        onSendMedia = { file ->
                                            viewModel.activeConversationId.value?.let { cid ->
                                                viewModel.sendDirectMessage(cid, "", mediaFile = file, mediaType = "image")
                                            }
                                        },
                                        onStartVoiceRecord = { viewModel.voiceRecorder.startRecording() },
                                        onStopVoiceRecord = {
                                            val (file, duration) = viewModel.voiceRecorder.stopRecording()
                                            if (file != null && duration > 0) {
                                                viewModel.activeConversationId.value?.let { cid ->
                                                    viewModel.sendVoiceMessage(cid, file, duration)
                                                }
                                            }
                                        },
                                        onAudioCallClick = {
                                            viewModel.startCall("peer_id", "Peer User", isVideo = false)
                                        },
                                        onVideoCallClick = {
                                            viewModel.startCall("peer_id", "Peer User", isVideo = true)
                                        },
                                        onBackClick = {
                                             viewModel.setActiveConversation(null)
                                             currentScreen = "chat_list"
                                        },
                                        isPlayingAudio = isPlayingAudio,
                                        playingAudioUrl = playingAudioUrl,
                                        onPlayAudio = { url -> viewModel.audioPlayer.play(url) },
                                        isRecording = viewModel.voiceRecorder.isRecording
                                    )
                                }
                                "notifications" -> {
                                    NotificationsScreen(
                                        notifications = notifications
                                    )
                                }
                            }

                            // Create Content Modal
                            if (showCreateContent) {
                                CreateContentScreen(
                                    onDismiss = { showCreateContent = false },
                                    onSubmitPost = { content, files, loc ->
                                        viewModel.createPost(content, files, loc)
                                        showCreateContent = false
                                    },
                                    onSubmitStory = { file, caption ->
                                        viewModel.createStory(file, caption)
                                        showCreateContent = false
                                    },
                                    onSubmitReel = { file, desc ->
                                        viewModel.createReel(file, desc)
                                        showCreateContent = false
                                    }
                                )
                            }

                            // Comments Bottom Sheet
                            if (activeCommentsPostId != null) {
                                ModalBottomSheet(
                                    onDismissRequest = { viewModel.closeComments() },
                                    containerColor = DarkSurface
                                ) {
                                    var newCommentText by remember { mutableStateOf("") }
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "Comments",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = GoldPrimary,
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 320.dp)
                                        ) {
                                            if (postComments.isEmpty()) {
                                                item {
                                                    Text(
                                                        text = "No comments yet. Start the conversation!",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.padding(vertical = 16.dp)
                                                    )
                                                }
                                            } else {
                                                items(postComments, key = { it.id }) { c ->
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 6.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        UserAvatar(url = c.authorAvatarUrl, size = 32.dp)
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = c.authorUsername,
                                                                style = MaterialTheme.typography.labelMedium,
                                                                color = GoldPrimary
                                                            )
                                                            Text(
                                                                text = c.text,
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                color = MaterialTheme.colorScheme.onSurface
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp, bottom = 16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = newCommentText,
                                                onValueChange = { newCommentText = it },
                                                placeholder = { Text("Add a comment...") },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(20.dp),
                                                maxLines = 3
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            IconButton(
                                                onClick = {
                                                    activeCommentsPostId?.let { pid ->
                                                        if (newCommentText.isNotBlank()) {
                                                            viewModel.addComment(pid, newCommentText)
                                                            newCommentText = ""
                                                        }
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Send,
                                                    contentDescription = "Send Comment",
                                                    tint = GoldPrimary
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Edit Profile Dialog
                            if (showEditProfileDialog) {
                                EditProfileDialog(
                                    user = currentUser,
                                    onDismiss = { showEditProfileDialog = false },
                                    onSave = { name, bio, site ->
                                        viewModel.updateProfile(name, bio, site)
                                        showEditProfileDialog = false
                                    }
                                )
                            }

                            // Call Overlay
                            if (callState !is CallState.Idle) {
                                CallOverlay(callState = callState, onEndCall = { viewModel.endCall() })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditProfileDialog(
    user: com.example.data.model.User?,
    onDismiss: () -> Unit,
    onSave: (displayName: String, bio: String, website: String) -> Unit
) {
    var name by remember { mutableStateOf(user?.displayName ?: "") }
    var bio by remember { mutableStateOf(user?.bio ?: "") }
    var website by remember { mutableStateOf(user?.website ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = website,
                    onValueChange = { website = it },
                    label = { Text("Website") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, bio, website) },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF1A1100))
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CallOverlay(
    callState: CallState,
    onEndCall: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            val title = when (callState) {
                is CallState.Calling -> "Calling ${callState.recipientName}..."
                is CallState.Incoming -> "Incoming call from ${callState.callerName}"
                is CallState.Connected -> "In call with ${callState.peerName}"
                is CallState.Ended -> callState.reason
                else -> ""
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(32.dp))

            FloatingActionButton(
                onClick = onEndCall,
                containerColor = Color.Red,
                contentColor = Color.White,
                shape = RoundedCornerShape(32.dp)
            ) {
                Icon(imageVector = Icons.Default.CallEnd, contentDescription = "End Call")
            }
        }
    }
}
