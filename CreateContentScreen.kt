package com.example.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldPrimary
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateContentScreen(
    onDismiss: () -> Unit,
    onSubmitPost: (content: String, mediaFiles: List<File>, location: String) -> Unit,
    onSubmitStory: (mediaFile: File, caption: String) -> Unit,
    onSubmitReel: (videoFile: File, description: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Post, 1: Story, 2: Reel
    var textContent by remember { mutableStateOf("") }
    var locationInput by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            validationError = null
        }
    }

    fun uriToFile(uri: Uri): File? {
        return try {
            val extension = if (selectedTab == 2) "mp4" else "jpg"
            val tempFile = File(context.cacheDir, "lulu_upload_${System.currentTimeMillis()}.$extension")
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (selectedTab) {
                            0 -> "New Post"
                            1 -> "New Story"
                            else -> "New Reel"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            val uri = selectedUri
                            when (selectedTab) {
                                0 -> {
                                    val files = if (uri != null) {
                                        val file = uriToFile(uri)
                                        if (file != null) listOf(file) else emptyList()
                                    } else emptyList()
                                    if (textContent.isBlank() && files.isEmpty()) {
                                        validationError = "Please add text or attach a photo"
                                        return@Button
                                    }
                                    onSubmitPost(textContent, files, locationInput)
                                }
                                1 -> {
                                    if (uri == null) {
                                        validationError = "Please select a photo or video for your story"
                                        return@Button
                                    }
                                    val file = uriToFile(uri)
                                    if (file != null) {
                                        onSubmitStory(file, textContent)
                                    }
                                }
                                else -> {
                                    if (uri == null) {
                                        validationError = "Please select a video for your reel"
                                        return@Button
                                    }
                                    val file = uriToFile(uri)
                                    if (file != null) {
                                        onSubmitReel(file, textContent)
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = Color(0xFF1A1100)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurface,
                contentColor = GoldPrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        selectedUri = null
                        validationError = null
                    },
                    text = { Text("Post") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        selectedUri = null
                        validationError = null
                    },
                    text = { Text("Story") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        selectedUri = null
                        validationError = null
                    },
                    text = { Text("Reel") }
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Media upload placeholder / preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceVariant)
                        .clickable {
                            val request = if (selectedTab == 2) {
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            } else {
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            }
                            mediaPickerLauncher.launch(request)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedUri != null) {
                        AsyncImage(
                            model = selectedUri,
                            contentDescription = "Selected Media Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        IconButton(
                            onClick = { selectedUri = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(50))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove Media", tint = Color.White)
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (selectedTab == 2) Icons.Default.VideoLibrary else Icons.Default.AddPhotoAlternate,
                                contentDescription = "Attach Media",
                                tint = GoldPrimary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (selectedTab == 2) "Tap to select Video for Reel" else "Tap to select Photo",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (validationError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = validationError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = textContent,
                    onValueChange = {
                        textContent = it
                        validationError = null
                    },
                    placeholder = {
                        Text(
                            when (selectedTab) {
                                0 -> "Write a caption..."
                                1 -> "Add a story caption..."
                                else -> "Add reel description & hashtags..."
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                if (selectedTab == 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = locationInput,
                        onValueChange = { locationInput = it },
                        placeholder = { Text("Add location (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
    }
}
