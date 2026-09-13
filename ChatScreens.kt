package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Conversation
import com.example.data.model.DirectMessage
import com.example.data.model.User
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldPrimary

@Composable
fun ChatListScreen(
    conversations: List<Conversation>,
    onConversationClick: (Conversation) -> Unit,
    onNewChatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewChatClick,
                containerColor = GoldPrimary,
                contentColor = Color(0xFF1A1100)
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "New Message")
            }
        },
        modifier = modifier
    ) { padding ->
        if (conversations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Direct Messages",
                        style = MaterialTheme.typography.titleLarge,
                        color = GoldPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Message your friends or send voice notes privately",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(conversations, key = { it.id }) { conv ->
                    ConversationItem(conv = conv, onClick = { onConversationClick(conv) })
                }
            }
        }
    }
}

@Composable
fun ConversationItem(conv: Conversation, onClick: () -> Unit) {
    val displayTitle = if (conv.isGroup) conv.groupTitle else conv.participantUsernames.firstOrNull() ?: "Chat"
    val avatarUrl = if (conv.isGroup) conv.groupAvatarUrl else conv.participantAvatars.firstOrNull() ?: ""

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        UserAvatar(url = avatarUrl, size = 52.dp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayTitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = conv.lastMessageText.ifBlank { "No messages yet" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        if (conv.unreadCount > 0) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(GoldPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${conv.unreadCount}",
                    color = Color(0xFF1A1100),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    conversation: Conversation?,
    messages: List<DirectMessage>,
    currentUserId: String?,
    onSendMessage: (String) -> Unit,
    onSendMedia: (java.io.File) -> Unit = {},
    onStartVoiceRecord: () -> Unit,
    onStopVoiceRecord: () -> Unit,
    onAudioCallClick: () -> Unit,
    onVideoCallClick: () -> Unit,
    onBackClick: () -> Unit,
    isPlayingAudio: Boolean,
    playingAudioUrl: String?,
    onPlayAudio: (String) -> Unit,
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current
    val imagePicker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val tempFile = java.io.File(context.cacheDir, "chat_img_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    tempFile.outputStream().use { output -> input.copyTo(output) }
                }
                onSendMedia(tempFile)
            } catch (ignored: Exception) {}
        }
    }
    val title = conversation?.let {
        if (it.isGroup) it.groupTitle else it.participantUsernames.firstOrNull() ?: "Direct Message"
    } ?: "Chat"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onAudioCallClick) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Audio Call", tint = GoldPrimary)
                    }
                    IconButton(onClick = onVideoCallClick) {
                        Icon(imageVector = Icons.Default.Videocam, contentDescription = "Video Call", tint = GoldPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        bottomBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(8.dp)
            ) {
                IconButton(onClick = {
                    imagePicker.launch(
                        androidx.activity.result.PickVisualMediaRequest(
                            androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }) {
                    Icon(imageVector = Icons.Outlined.PhotoCamera, contentDescription = "Send Photo", tint = GoldPrimary)
                }

                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Message...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant
                    ),
                    maxLines = 4
                )

                if (textInput.isNotBlank()) {
                    IconButton(onClick = {
                        onSendMessage(textInput)
                        textInput = ""
                    }) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = GoldPrimary)
                    }
                } else {
                    IconButton(
                        onClick = {
                            if (isRecording) onStopVoiceRecord() else onStartVoiceRecord()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Mic,
                            contentDescription = "Voice Note",
                            tint = if (isRecording) Color.Red else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp),
            reverseLayout = false
        ) {
            items(messages, key = { it.id }) { msg ->
                val isMe = msg.senderId == currentUserId
                MessageBubble(
                    msg = msg,
                    isMe = isMe,
                    isPlaying = isPlayingAudio && playingAudioUrl == msg.mediaUrl,
                    onPlayAudio = { onPlayAudio(msg.mediaUrl) }
                )
            }
        }
    }
}

@Composable
fun MessageBubble(
    msg: DirectMessage,
    isMe: Boolean,
    isPlaying: Boolean,
    onPlayAudio: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 16.dp
            ),
            color = if (isMe) GoldPrimary else DarkSurfaceVariant,
            contentColor = if (isMe) Color(0xFF1A1100) else Color.White
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                if (msg.mediaType == "audio") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onPlayAudio) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play Audio"
                            )
                        }
                        Text(text = "Voice note (${msg.audioDurationSeconds}s)")
                    }
                } else if (msg.mediaType == "image" && msg.mediaUrl.isNotBlank()) {
                    coil.compose.AsyncImage(
                        model = msg.mediaUrl,
                        contentDescription = "Shared Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                    if (msg.text.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = msg.text, style = MaterialTheme.typography.bodyLarge)
                    }
                } else if (msg.text.isNotBlank()) {
                    Text(text = msg.text, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
