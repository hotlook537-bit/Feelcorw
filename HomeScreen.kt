package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
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
import com.example.data.model.Story
import com.example.data.model.User
import com.example.ui.theme.AccentRed
import com.example.ui.theme.GoldPrimary

@Composable
fun HomeScreen(
    currentUser: User?,
    stories: List<Story>,
    posts: List<Post>,
    onLikePost: (Post) -> Unit,
    onSavePost: (Post) -> Unit = {},
    onCommentClick: (Post) -> Unit,
    onSharePost: (Post) -> Unit,
    onStoryClick: (Story) -> Unit,
    onAddStoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Stories Row
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    StoryCircle(
                        username = "Your Story",
                        avatarUrl = currentUser?.avatarUrl ?: "",
                        isCurrentUserAdd = true,
                        onClick = onAddStoryClick
                    )
                }
                items(stories, key = { it.id }) { story ->
                    StoryCircle(
                        username = story.authorUsername,
                        avatarUrl = story.authorAvatarUrl,
                        isSeen = story.isSeen,
                        onClick = { onStoryClick(story) }
                    )
                }
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                thickness = 0.5.dp
            )
        }

        // Posts
        if (posts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Welcome to Lulugram",
                            style = MaterialTheme.typography.titleLarge,
                            color = GoldPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No posts yet. Be the first to share!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(posts, key = { it.id }) { post ->
                PostCard(
                    post = post,
                    onLikeClick = { onLikePost(post) },
                    onSaveClick = { onSavePost(post) },
                    onCommentClick = { onCommentClick(post) },
                    onShareClick = { onSharePost(post) }
                )
            }
        }
    }
}

@Composable
fun PostCard(
    post: Post,
    onLikeClick: () -> Unit,
    onSaveClick: () -> Unit = {},
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(0.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserAvatar(url = post.authorAvatarUrl, size = 36.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.authorUsername,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (post.isAuthorVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            VerifiedBadge(size = 14.dp)
                        }
                    }
                    if (post.location.isNotBlank()) {
                        Text(
                            text = post.location,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Media
            if (post.mediaUrls.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(Color.Black)
                ) {
                    AsyncImage(
                        model = post.mediaUrls.first(),
                        contentDescription = "Post Media",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onLikeClick) {
                    Icon(
                        imageVector = if (post.isLikedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.isLikedByMe) AccentRed else MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onCommentClick) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onShareClick) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onSaveClick) {
                    Icon(
                        imageVector = if (post.isSavedByMe) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (post.isSavedByMe) GoldPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Likes & Caption
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                if (post.likesCount > 0) {
                    Text(
                        text = "${post.likesCount} likes",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (post.content.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = post.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (post.commentsCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "View all ${post.commentsCount} comments",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { onCommentClick() }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
