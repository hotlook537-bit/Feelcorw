package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.Reel
import com.example.ui.theme.AccentRed
import com.example.ui.theme.GoldPrimary

@Composable
fun ReelsScreen(
    reels: List<Reel>,
    onLikeReel: (Reel) -> Unit,
    modifier: Modifier = Modifier
) {
    if (reels.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No Reels Yet",
                    style = MaterialTheme.typography.titleLarge,
                    color = GoldPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Share short videos with the Lulugram community!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        val pagerState = rememberPagerState(pageCount = { reels.size })
        VerticalPager(
            state = pagerState,
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
        ) { page ->
            val reel = reels[page]
            ReelItem(reel = reel, onLikeClick = { onLikeReel(reel) })
        }
    }
}

@Composable
fun ReelItem(
    reel: Reel,
    onLikeClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (reel.videoUrl.isNotBlank() || reel.thumbnailUrl.isNotBlank()) {
            AsyncImage(
                model = if (reel.thumbnailUrl.isNotBlank()) reel.thumbnailUrl else reel.videoUrl,
                contentDescription = "Reel Content",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Side Actions (Like, Comment, Share)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 48.dp)
        ) {
            IconButton(onClick = onLikeClick) {
                Icon(
                    imageVector = if (reel.isLikedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like Reel",
                    tint = if (reel.isLikedByMe) AccentRed else Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(text = "${reel.likesCount}", color = Color.White, style = MaterialTheme.typography.labelSmall)

            Spacer(modifier = Modifier.height(16.dp))

            IconButton(onClick = { /* comments */ }) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "Comment Reel",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(text = "${reel.commentsCount}", color = Color.White, style = MaterialTheme.typography.labelSmall)

            Spacer(modifier = Modifier.height(16.dp))

            IconButton(onClick = { /* share */ }) {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = "Share Reel",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(text = "${reel.sharesCount}", color = Color.White, style = MaterialTheme.typography.labelSmall)
        }

        // Bottom Details (Author, caption, music)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, end = 80.dp, bottom = 48.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(url = reel.authorAvatarUrl, size = 36.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = reel.authorUsername,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                if (reel.isAuthorVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    VerifiedBadge(size = 14.dp)
                }
            }
            if (reel.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = reel.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
            if (reel.audioTrack.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Audio Track",
                        tint = GoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = reel.audioTrack,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}
