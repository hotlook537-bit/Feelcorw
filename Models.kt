package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String = "",
    val username: String = "",
    val displayName: String = "",
    val email: String = "",
    val bio: String = "",
    val avatarUrl: String = "",
    val coverUrl: String = "",
    val isVerified: Boolean = false,
    val isPrivate: Boolean = false,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val postsCount: Int = 0,
    val website: String = "",
    val role: String = "user", // "user", "creator", "admin"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey val id: String = "",
    val authorId: String = "",
    val authorUsername: String = "",
    val authorDisplayName: String = "",
    val authorAvatarUrl: String = "",
    val isAuthorVerified: Boolean = false,
    val content: String = "",
    val mediaUrls: List<String> = emptyList(),
    val mediaType: String = "image", // "image", "video", "carousel"
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val sharesCount: Int = 0,
    val location: String = "",
    val tags: List<String> = emptyList(),
    val isLikedByMe: Boolean = false,
    val isSavedByMe: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "stories")
data class Story(
    @PrimaryKey val id: String = "",
    val authorId: String = "",
    val authorUsername: String = "",
    val authorAvatarUrl: String = "",
    val isAuthorVerified: Boolean = false,
    val mediaUrl: String = "",
    val mediaType: String = "image", // "image", "video"
    val caption: String = "",
    val viewsCount: Int = 0,
    val likesCount: Int = 0,
    val isSeen: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
)

@Entity(tableName = "story_highlights")
data class StoryHighlight(
    @PrimaryKey val id: String = "",
    val authorId: String = "",
    val title: String = "",
    val coverUrl: String = "",
    val storyIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reels")
data class Reel(
    @PrimaryKey val id: String = "",
    val authorId: String = "",
    val authorUsername: String = "",
    val authorAvatarUrl: String = "",
    val isAuthorVerified: Boolean = false,
    val videoUrl: String = "",
    val thumbnailUrl: String = "",
    val description: String = "",
    val audioTrack: String = "",
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val sharesCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey val id: String = "",
    val participantIds: List<String> = emptyList(),
    val participantUsernames: List<String> = emptyList(),
    val participantAvatars: List<String> = emptyList(),
    val isGroup: Boolean = false,
    val groupTitle: String = "",
    val groupAvatarUrl: String = "",
    val lastMessageText: String = "",
    val lastMessageSenderId: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val isArchived: Boolean = false,
    val isRequest: Boolean = false
)

@Entity(tableName = "direct_messages")
data class DirectMessage(
    @PrimaryKey val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderUsername: String = "",
    val senderAvatarUrl: String = "",
    val text: String = "",
    val mediaUrl: String = "",
    val mediaType: String = "none", // "none", "image", "audio", "video"
    val audioDurationSeconds: Int = 0,
    val replyToMessageId: String? = null,
    val reactions: Map<String, String> = emptyMap(), // userId -> emoji
    val isRead: Boolean = false,
    val isPinned: Boolean = false,
    val isEdited: Boolean = false,
    val isDeletedForEveryone: Boolean = false,
    val deletedForUsers: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey val id: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorUsername: String = "",
    val authorAvatarUrl: String = "",
    val isAuthorVerified: Boolean = false,
    val text: String = "",
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val replyToCommentId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "likes", primaryKeys = ["targetId", "userId"])
data class Like(
    val targetId: String = "",
    val userId: String = "",
    val targetType: String = "post", // "post", "reel", "comment"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "follows", primaryKeys = ["followerId", "followingId"])
data class Follow(
    val followerId: String = "",
    val followingId: String = "",
    val status: String = "accepted", // "accepted", "pending"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "story_likes", primaryKeys = ["storyId", "userId"])
data class StoryLike(
    val storyId: String = "",
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "story_views", primaryKeys = ["storyId", "userId"])
data class StoryView(
    val storyId: String = "",
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_posts", primaryKeys = ["postId", "userId"])
data class SavedPost(
    val postId: String = "",
    val userId: String = "",
    val collectionId: String = "default",
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_collections")
data class SavedCollection(
    @PrimaryKey val id: String = "",
    val userId: String = "",
    val name: String = "All Posts",
    val coverUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "blocks", primaryKeys = ["blockerId", "blockedId"])
data class Block(
    val blockerId: String = "",
    val blockedId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "mutes", primaryKeys = ["muterId", "mutedId"])
data class Mute(
    val muterId: String = "",
    val mutedId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reports")
data class Report(
    @PrimaryKey val id: String = "",
    val reporterId: String = "",
    val targetId: String = "",
    val targetType: String = "", // "post", "user", "comment", "message"
    val reason: String = "",
    val details: String = "",
    val status: String = "pending", // "pending", "resolved", "dismissed"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "admin_actions")
data class AdminAction(
    @PrimaryKey val id: String = "",
    val adminId: String = "",
    val targetUserId: String = "",
    val actionType: String = "", // "grant_verification", "revoke_verification", "ban", "unban"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class LuluNotification(
    @PrimaryKey val id: String = "",
    val recipientId: String = "",
    val senderId: String = "",
    val senderUsername: String = "",
    val senderAvatarUrl: String = "",
    val type: String = "", // "like", "comment", "follow", "follow_request", "mention"
    val targetId: String = "",
    val previewText: String = "",
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
