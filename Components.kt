package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary

@Composable
fun VerifiedBadge(modifier: Modifier = Modifier, size: Dp = 16.dp) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(GoldSecondary, GoldPrimary, GoldDark)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Verified Official",
            tint = Color(0xFF1A1100),
            modifier = Modifier.size(size * 0.7f)
        )
    }
}

@Composable
fun UserAvatar(
    url: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    hasStoryRing: Boolean = false,
    isSeen: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val ringModifier = if (hasStoryRing) {
        val colors = if (isSeen) {
            listOf(Color.Gray, Color.DarkGray)
        } else {
            listOf(GoldPrimary, Color(0xFFFF007A), Color(0xFF7928CA))
        }
        Modifier
            .border(2.dp, Brush.linearGradient(colors), CircleShape)
            .padding(2.dp)
    } else {
        Modifier
    }

    val clickModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .size(size)
            .then(ringModifier)
            .then(clickModifier)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (url.isNotBlank()) {
            AsyncImage(
                model = url,
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Avatar Placeholder",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(size * 0.6f)
            )
        }
    }
}

@Composable
fun StoryCircle(
    username: String,
    avatarUrl: String,
    isSeen: Boolean = false,
    isCurrentUserAdd: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            UserAvatar(
                url = avatarUrl,
                size = 64.dp,
                hasStoryRing = !isCurrentUserAdd,
                isSeen = isSeen
            )
            if (isCurrentUserAdd) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", color = Color(0xFF1A1100), fontSize = 14.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = username,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
