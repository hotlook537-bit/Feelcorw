package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.Post
import com.example.data.model.Reel
import com.example.data.model.User
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldPrimary

@Composable
fun ProfileScreen(
    user: User?,
    userPosts: List<Post>,
    userReels: List<Reel>,
    savedPosts: List<Post> = emptyList(),
    onEditProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Posts, 1: Reels, 2: Saved

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Profile Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                UserAvatar(url = user?.avatarUrl ?: "", size = 80.dp)
                Spacer(modifier = Modifier.width(24.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.weight(1f)
                ) {
                    ProfileStat(count = user?.postsCount ?: 0, label = "Posts")
                    ProfileStat(count = user?.followersCount ?: 0, label = "Followers")
                    ProfileStat(count = user?.followingCount ?: 0, label = "Following")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // User Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = user?.displayName?.ifBlank { user.username } ?: "Lulugram User",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (user?.isVerified == true) {
                    Spacer(modifier = Modifier.width(4.dp))
                    VerifiedBadge(size = 16.dp)
                }
            }

            if (!user?.bio.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user!!.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (!user?.website.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = user!!.website,
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actions (Edit Profile, Settings)
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onEditProfileClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkSurfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("Edit Profile")
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Media Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = GoldPrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                icon = { Icon(Icons.Default.GridOn, contentDescription = "Posts") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                icon = { Icon(Icons.Default.VideoLibrary, contentDescription = "Reels") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                icon = { Icon(Icons.Default.Bookmark, contentDescription = "Saved") }
            )
        }

        // Grid Content
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(1.dp),
            horizontalArrangement = Arrangement.spacedBy(1.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            when (selectedTab) {
                0 -> {
                    items(userPosts, key = { it.id }) { post ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .background(DarkSurfaceVariant)
                        ) {
                            if (post.mediaUrls.isNotEmpty()) {
                                AsyncImage(
                                    model = post.mediaUrls.first(),
                                    contentDescription = "Post Grid Item",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
                1 -> {
                    items(userReels, key = { it.id }) { reel ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .background(DarkSurfaceVariant)
                        ) {
                            if (reel.thumbnailUrl.isNotBlank() || reel.videoUrl.isNotBlank()) {
                                AsyncImage(
                                    model = reel.thumbnailUrl.ifBlank { reel.videoUrl },
                                    contentDescription = "Reel Grid Item",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
                2 -> {
                    if (savedPosts.isEmpty()) {
                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(3) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No saved posts yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(savedPosts, key = { it.id }) { post ->
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .background(DarkSurfaceVariant)
                            ) {
                                if (post.mediaUrls.isNotEmpty()) {
                                    AsyncImage(
                                        model = post.mediaUrls.first(),
                                        contentDescription = "Saved Post Item",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileStat(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
