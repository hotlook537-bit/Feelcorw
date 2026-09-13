package com.example.data.remote

import com.example.data.model.*
import com.google.firebase.firestore.DocumentSnapshot

object FirestoreMappers {

    fun docToUser(doc: DocumentSnapshot): User {
        val data = doc.data ?: emptyMap<String, Any>()
        return User(
            id = doc.id,
            username = data["username"] as? String ?: "",
            displayName = data["displayName"] as? String ?: "",
            email = data["email"] as? String ?: "",
            bio = data["bio"] as? String ?: "",
            avatarUrl = data["avatarUrl"] as? String ?: "",
            coverUrl = data["coverUrl"] as? String ?: "",
            isVerified = data["isVerified"] as? Boolean ?: false,
            isPrivate = data["isPrivate"] as? Boolean ?: false,
            followersCount = (data["followersCount"] as? Number)?.toInt() ?: 0,
            followingCount = (data["followingCount"] as? Number)?.toInt() ?: 0,
            postsCount = (data["postsCount"] as? Number)?.toInt() ?: 0,
            website = data["website"] as? String ?: "",
            role = data["role"] as? String ?: "user",
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }

    fun userToMap(user: User): Map<String, Any> {
        return mapOf(
            "username" to user.username,
            "displayName" to user.displayName,
            "email" to user.email,
            "bio" to user.bio,
            "avatarUrl" to user.avatarUrl,
            "coverUrl" to user.coverUrl,
            "isVerified" to user.isVerified,
            "isPrivate" to user.isPrivate,
            "followersCount" to user.followersCount,
            "followingCount" to user.followingCount,
            "postsCount" to user.postsCount,
            "website" to user.website,
            "role" to user.role,
            "createdAt" to user.createdAt
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun docToPost(doc: DocumentSnapshot): Post {
        val data = doc.data ?: emptyMap<String, Any>()
        return Post(
            id = doc.id,
            authorId = data["authorId"] as? String ?: "",
            authorUsername = data["authorUsername"] as? String ?: "",
            authorDisplayName = data["authorDisplayName"] as? String ?: "",
            authorAvatarUrl = data["authorAvatarUrl"] as? String ?: "",
            isAuthorVerified = data["isAuthorVerified"] as? Boolean ?: false,
            content = data["content"] as? String ?: "",
            mediaUrls = (data["mediaUrls"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            mediaType = data["mediaType"] as? String ?: "image",
            likesCount = (data["likesCount"] as? Number)?.toInt() ?: 0,
            commentsCount = (data["commentsCount"] as? Number)?.toInt() ?: 0,
            sharesCount = (data["sharesCount"] as? Number)?.toInt() ?: 0,
            location = data["location"] as? String ?: "",
            tags = (data["tags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            isLikedByMe = false,
            isSavedByMe = false,
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }

    fun postToMap(post: Post): Map<String, Any> {
        return mapOf(
            "authorId" to post.authorId,
            "authorUsername" to post.authorUsername,
            "authorDisplayName" to post.authorDisplayName,
            "authorAvatarUrl" to post.authorAvatarUrl,
            "isAuthorVerified" to post.isAuthorVerified,
            "content" to post.content,
            "mediaUrls" to post.mediaUrls,
            "mediaType" to post.mediaType,
            "likesCount" to post.likesCount,
            "commentsCount" to post.commentsCount,
            "sharesCount" to post.sharesCount,
            "location" to post.location,
            "tags" to post.tags,
            "createdAt" to post.createdAt
        )
    }

    fun docToStory(doc: DocumentSnapshot): Story {
        val data = doc.data ?: emptyMap<String, Any>()
        return Story(
            id = doc.id,
            authorId = data["authorId"] as? String ?: "",
            authorUsername = data["authorUsername"] as? String ?: "",
            authorAvatarUrl = data["authorAvatarUrl"] as? String ?: "",
            isAuthorVerified = data["isAuthorVerified"] as? Boolean ?: false,
            mediaUrl = data["mediaUrl"] as? String ?: "",
            mediaType = data["mediaType"] as? String ?: "image",
            caption = data["caption"] as? String ?: "",
            viewsCount = (data["viewsCount"] as? Number)?.toInt() ?: 0,
            likesCount = (data["likesCount"] as? Number)?.toInt() ?: 0,
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            expiresAt = (data["expiresAt"] as? Number)?.toLong() ?: (System.currentTimeMillis() + 86400000L)
        )
    }

    fun storyToMap(story: Story): Map<String, Any> {
        return mapOf(
            "authorId" to story.authorId,
            "authorUsername" to story.authorUsername,
            "authorAvatarUrl" to story.authorAvatarUrl,
            "isAuthorVerified" to story.isAuthorVerified,
            "mediaUrl" to story.mediaUrl,
            "mediaType" to story.mediaType,
            "caption" to story.caption,
            "viewsCount" to story.viewsCount,
            "likesCount" to story.likesCount,
            "createdAt" to story.createdAt,
            "expiresAt" to story.expiresAt
        )
    }

    fun docToReel(doc: DocumentSnapshot): Reel {
        val data = doc.data ?: emptyMap<String, Any>()
        return Reel(
            id = doc.id,
            authorId = data["authorId"] as? String ?: "",
            authorUsername = data["authorUsername"] as? String ?: "",
            authorAvatarUrl = data["authorAvatarUrl"] as? String ?: "",
            isAuthorVerified = data["isAuthorVerified"] as? Boolean ?: false,
            videoUrl = data["videoUrl"] as? String ?: "",
            thumbnailUrl = data["thumbnailUrl"] as? String ?: "",
            description = data["description"] as? String ?: "",
            audioTrack = data["audioTrack"] as? String ?: "",
            likesCount = (data["likesCount"] as? Number)?.toInt() ?: 0,
            commentsCount = (data["commentsCount"] as? Number)?.toInt() ?: 0,
            sharesCount = (data["sharesCount"] as? Number)?.toInt() ?: 0,
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }

    fun reelToMap(reel: Reel): Map<String, Any> {
        return mapOf(
            "authorId" to reel.authorId,
            "authorUsername" to reel.authorUsername,
            "authorAvatarUrl" to reel.authorAvatarUrl,
            "isAuthorVerified" to reel.isAuthorVerified,
            "videoUrl" to reel.videoUrl,
            "thumbnailUrl" to reel.thumbnailUrl,
            "description" to reel.description,
            "audioTrack" to reel.audioTrack,
            "likesCount" to reel.likesCount,
            "commentsCount" to reel.commentsCount,
            "sharesCount" to reel.sharesCount,
            "createdAt" to reel.createdAt
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun docToConversation(doc: DocumentSnapshot): Conversation {
        val data = doc.data ?: emptyMap<String, Any>()
        return Conversation(
            id = doc.id,
            participantIds = (data["participantIds"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            participantUsernames = (data["participantUsernames"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            participantAvatars = (data["participantAvatars"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            isGroup = data["isGroup"] as? Boolean ?: false,
            groupTitle = data["groupTitle"] as? String ?: "",
            groupAvatarUrl = data["groupAvatarUrl"] as? String ?: "",
            lastMessageText = data["lastMessageText"] as? String ?: "",
            lastMessageSenderId = data["lastMessageSenderId"] as? String ?: "",
            lastMessageTimestamp = (data["lastMessageTimestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            unreadCount = (data["unreadCount"] as? Number)?.toInt() ?: 0,
            isPinned = data["isPinned"] as? Boolean ?: false,
            isMuted = data["isMuted"] as? Boolean ?: false,
            isArchived = data["isArchived"] as? Boolean ?: false,
            isRequest = data["isRequest"] as? Boolean ?: false
        )
    }

    fun conversationToMap(conv: Conversation): Map<String, Any> {
        return mapOf(
            "participantIds" to conv.participantIds,
            "participantUsernames" to conv.participantUsernames,
            "participantAvatars" to conv.participantAvatars,
            "isGroup" to conv.isGroup,
            "groupTitle" to conv.groupTitle,
            "groupAvatarUrl" to conv.groupAvatarUrl,
            "lastMessageText" to conv.lastMessageText,
            "lastMessageSenderId" to conv.lastMessageSenderId,
            "lastMessageTimestamp" to conv.lastMessageTimestamp,
            "unreadCount" to conv.unreadCount,
            "isPinned" to conv.isPinned,
            "isMuted" to conv.isMuted,
            "isArchived" to conv.isArchived,
            "isRequest" to conv.isRequest
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun docToMessage(doc: DocumentSnapshot): DirectMessage {
        val data = doc.data ?: emptyMap<String, Any>()
        return DirectMessage(
            id = doc.id,
            conversationId = data["conversationId"] as? String ?: "",
            senderId = data["senderId"] as? String ?: "",
            senderUsername = data["senderUsername"] as? String ?: "",
            senderAvatarUrl = data["senderAvatarUrl"] as? String ?: "",
            text = data["text"] as? String ?: "",
            mediaUrl = data["mediaUrl"] as? String ?: "",
            mediaType = data["mediaType"] as? String ?: "none",
            audioDurationSeconds = (data["audioDurationSeconds"] as? Number)?.toInt() ?: 0,
            replyToMessageId = data["replyToMessageId"] as? String,
            reactions = (data["reactions"] as? Map<*, *>)?.mapNotNull { (k, v) ->
                if (k is String && v is String) k to v else null
            }?.toMap() ?: emptyMap(),
            isRead = data["isRead"] as? Boolean ?: false,
            isPinned = data["isPinned"] as? Boolean ?: false,
            isEdited = data["isEdited"] as? Boolean ?: false,
            isDeletedForEveryone = data["isDeletedForEveryone"] as? Boolean ?: false,
            deletedForUsers = (data["deletedForUsers"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }

    fun messageToMap(msg: DirectMessage): Map<String, Any?> {
        return mapOf(
            "conversationId" to msg.conversationId,
            "senderId" to msg.senderId,
            "senderUsername" to msg.senderUsername,
            "senderAvatarUrl" to msg.senderAvatarUrl,
            "text" to msg.text,
            "mediaUrl" to msg.mediaUrl,
            "mediaType" to msg.mediaType,
            "audioDurationSeconds" to msg.audioDurationSeconds,
            "replyToMessageId" to msg.replyToMessageId,
            "reactions" to msg.reactions,
            "isRead" to msg.isRead,
            "isPinned" to msg.isPinned,
            "isEdited" to msg.isEdited,
            "isDeletedForEveryone" to msg.isDeletedForEveryone,
            "deletedForUsers" to msg.deletedForUsers,
            "createdAt" to msg.createdAt
        )
    }

    fun docToComment(doc: DocumentSnapshot): Comment {
        val data = doc.data ?: emptyMap<String, Any>()
        return Comment(
            id = doc.id,
            postId = data["postId"] as? String ?: "",
            authorId = data["authorId"] as? String ?: "",
            authorUsername = data["authorUsername"] as? String ?: "",
            authorAvatarUrl = data["authorAvatarUrl"] as? String ?: "",
            isAuthorVerified = data["isAuthorVerified"] as? Boolean ?: false,
            text = data["text"] as? String ?: "",
            likesCount = (data["likesCount"] as? Number)?.toInt() ?: 0,
            replyToCommentId = data["replyToCommentId"] as? String,
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }

    fun commentToMap(comment: Comment): Map<String, Any?> {
        return mapOf(
            "postId" to comment.postId,
            "authorId" to comment.authorId,
            "authorUsername" to comment.authorUsername,
            "authorAvatarUrl" to comment.authorAvatarUrl,
            "isAuthorVerified" to comment.isAuthorVerified,
            "text" to comment.text,
            "likesCount" to comment.likesCount,
            "replyToCommentId" to comment.replyToCommentId,
            "createdAt" to comment.createdAt
        )
    }

    fun docToNotification(doc: DocumentSnapshot): LuluNotification {
        val data = doc.data ?: emptyMap<String, Any>()
        return LuluNotification(
            id = doc.id,
            recipientId = data["recipientId"] as? String ?: "",
            senderId = data["senderId"] as? String ?: "",
            senderUsername = data["senderUsername"] as? String ?: "",
            senderAvatarUrl = data["senderAvatarUrl"] as? String ?: "",
            type = data["type"] as? String ?: "",
            targetId = data["targetId"] as? String ?: "",
            previewText = data["previewText"] as? String ?: "",
            isRead = data["isRead"] as? Boolean ?: false,
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }

    fun notificationToMap(notif: LuluNotification): Map<String, Any> {
        return mapOf(
            "recipientId" to notif.recipientId,
            "senderId" to notif.senderId,
            "senderUsername" to notif.senderUsername,
            "senderAvatarUrl" to notif.senderAvatarUrl,
            "type" to notif.type,
            "targetId" to notif.targetId,
            "previewText" to notif.previewText,
            "isRead" to notif.isRead,
            "createdAt" to notif.createdAt
        )
    }
}
