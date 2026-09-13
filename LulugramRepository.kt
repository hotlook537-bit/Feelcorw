package com.example.data.repository

import com.example.data.local.LulugramDao
import com.example.data.model.*
import com.example.data.remote.FirebaseService
import com.example.data.remote.ImageKitService
import kotlinx.coroutines.flow.Flow
import java.io.File

class LulugramRepository(
    val dao: LulugramDao,
    val firebaseService: FirebaseService,
    val imageKitService: ImageKitService = ImageKitService()
) {
    val currentUserId: String?
        get() = firebaseService.currentUserId

    // Users
    fun getUser(userId: String): Flow<User?> = dao.getUserById(userId)

    suspend fun syncUser(userId: String): User? {
        var remoteUser = firebaseService.getUser(userId)
        if (remoteUser == null) {
            val authUser = firebaseService.auth.currentUser
            if (authUser != null && authUser.uid == userId) {
                val email = authUser.email ?: ""
                val defaultUsername = email.substringBefore("@").ifBlank { "user_${userId.take(5)}" }
                remoteUser = User(
                    id = userId,
                    username = defaultUsername.lowercase(),
                    displayName = authUser.displayName ?: defaultUsername,
                    email = email,
                    createdAt = System.currentTimeMillis()
                )
                firebaseService.saveUser(remoteUser)
            }
        }
        if (remoteUser != null) {
            dao.insertUser(remoteUser)
        }
        try {
            val userLikes = firebaseService.getUserLikes(userId)
            userLikes.forEach { postId ->
                dao.insertLike(Like(targetId = postId, userId = userId))
            }
        } catch (ignored: Exception) {}
        try {
            val userSaved = firebaseService.getUserSavedPosts(userId)
            userSaved.forEach { postId ->
                dao.insertSavedPost(SavedPost(postId = postId, userId = userId))
            }
        } catch (ignored: Exception) {}
        return remoteUser
    }

    suspend fun saveUser(user: User) {
        firebaseService.saveUser(user)
        dao.insertUser(user)
    }

    suspend fun uploadAvatar(userId: String, file: File): String {
        val url = imageKitService.uploadMedia(file, "avatar_${userId}_${System.currentTimeMillis()}.jpg", "/avatars")
        firebaseService.updateUserProfile(userId, mapOf("avatarUrl" to url))
        val current = dao.getUserByIdOnce(userId)
        if (current != null) {
            dao.insertUser(current.copy(avatarUrl = url))
        }
        return url
    }

    suspend fun uploadCover(userId: String, file: File): String {
        val url = imageKitService.uploadMedia(file, "cover_${userId}_${System.currentTimeMillis()}.jpg", "/covers")
        firebaseService.updateUserProfile(userId, mapOf("coverUrl" to url))
        val current = dao.getUserByIdOnce(userId)
        if (current != null) {
            dao.insertUser(current.copy(coverUrl = url))
        }
        return url
    }

    // Posts
    fun getPosts(): Flow<List<Post>> = dao.getAllPosts()

    fun getPostsByUser(userId: String): Flow<List<Post>> = dao.getPostsByUserId(userId)

    suspend fun syncPosts() {
        firebaseService.observeFeedPosts().collect { remotePosts ->
            val uid = currentUserId ?: ""
            val reconciled = remotePosts.map { post ->
                val isLiked = if (uid.isNotEmpty()) dao.isLikedByMe(post.id, uid) else false
                val isSaved = if (uid.isNotEmpty()) dao.isPostSavedByMe(post.id, uid) else false
                post.copy(isLikedByMe = isLiked, isSavedByMe = isSaved)
            }
            dao.insertPosts(reconciled)
        }
    }

    suspend fun createPost(
        authorId: String,
        authorUsername: String,
        authorDisplayName: String,
        authorAvatarUrl: String,
        isAuthorVerified: Boolean,
        content: String,
        mediaFiles: List<File>,
        location: String,
        tags: List<String>
    ): String {
        val uploadedUrls = mediaFiles.map { file ->
            imageKitService.uploadMedia(file, "post_${authorId}_${System.currentTimeMillis()}_${file.name}", "/posts")
        }

        val post = Post(
            authorId = authorId,
            authorUsername = authorUsername,
            authorDisplayName = authorDisplayName,
            authorAvatarUrl = authorAvatarUrl,
            isAuthorVerified = isAuthorVerified,
            content = content,
            mediaUrls = uploadedUrls,
            mediaType = if (uploadedUrls.isEmpty()) "none" else "image",
            location = location,
            tags = tags,
            createdAt = System.currentTimeMillis()
        )

        val newId = firebaseService.createPost(post)
        dao.insertPost(post.copy(id = newId))
        return newId
    }

    suspend fun likePost(postId: String, userId: String, postAuthorId: String? = null, senderUsername: String = "", senderAvatar: String = "") {
        firebaseService.likePost(postId, userId, postAuthorId, senderUsername, senderAvatar)
        dao.insertLike(Like(targetId = postId, userId = userId))
        val cached = dao.getPostById(postId)
        if (cached != null) {
            dao.insertPost(cached.copy(isLikedByMe = true, likesCount = cached.likesCount + 1))
        }
    }

    suspend fun unlikePost(postId: String, userId: String) {
        firebaseService.unlikePost(postId, userId)
        dao.deleteLike(postId, userId)
        val cached = dao.getPostById(postId)
        if (cached != null) {
            dao.insertPost(cached.copy(isLikedByMe = false, likesCount = maxOf(0, cached.likesCount - 1)))
        }
    }

    // Stories
    fun getActiveStories(): Flow<List<Story>> = dao.getActiveStories()

    suspend fun createStory(
        authorId: String,
        authorUsername: String,
        authorAvatarUrl: String,
        isAuthorVerified: Boolean,
        mediaFile: File,
        caption: String
    ): String {
        val url = imageKitService.uploadMedia(mediaFile, "story_${authorId}_${System.currentTimeMillis()}.jpg", "/stories")
        val story = Story(
            authorId = authorId,
            authorUsername = authorUsername,
            authorAvatarUrl = authorAvatarUrl,
            isAuthorVerified = isAuthorVerified,
            mediaUrl = url,
            caption = caption,
            createdAt = System.currentTimeMillis()
        )
        val id = firebaseService.createStory(story)
        dao.insertStory(story.copy(id = id))
        return id
    }

    // Reels
    fun getReels(): Flow<List<Reel>> = dao.getAllReels()

    suspend fun createReel(
        authorId: String,
        authorUsername: String,
        authorAvatarUrl: String,
        isAuthorVerified: Boolean,
        videoFile: File,
        description: String,
        audioTrack: String
    ): String {
        val videoUrl = imageKitService.uploadMedia(videoFile, "reel_${authorId}_${System.currentTimeMillis()}.mp4", "/reels")
        val reel = Reel(
            authorId = authorId,
            authorUsername = authorUsername,
            authorAvatarUrl = authorAvatarUrl,
            isAuthorVerified = isAuthorVerified,
            videoUrl = videoUrl,
            description = description,
            audioTrack = audioTrack,
            createdAt = System.currentTimeMillis()
        )
        val id = firebaseService.createReel(reel)
        dao.insertReel(reel.copy(id = id))
        return id
    }

    suspend fun likeReel(reelId: String, userId: String) {
        firebaseService.likeReel(reelId, userId)
        val reel = dao.getReelById(reelId)
        if (reel != null) {
            dao.insertReel(reel.copy(likesCount = reel.likesCount + 1, isLikedByMe = true))
        }
    }

    suspend fun unlikeReel(reelId: String, userId: String) {
        firebaseService.unlikeReel(reelId, userId)
        val reel = dao.getReelById(reelId)
        if (reel != null) {
            dao.insertReel(reel.copy(likesCount = maxOf(0, reel.likesCount - 1), isLikedByMe = false))
        }
    }

    // Direct Messages & Conversations
    fun getConversations(): Flow<List<Conversation>> = dao.getActiveConversations()

    fun getMessages(conversationId: String): Flow<List<DirectMessage>> = dao.getMessagesForConversation(conversationId)

    suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        senderUsername: String,
        senderAvatarUrl: String,
        text: String,
        mediaFile: File? = null,
        mediaType: String = "none",
        audioDuration: Int = 0
    ): String {
        val mediaUrl = if (mediaFile != null) {
            imageKitService.uploadMedia(mediaFile, "chat_${senderId}_${System.currentTimeMillis()}_${mediaFile.name}", "/messages")
        } else ""

        val msg = DirectMessage(
            conversationId = conversationId,
            senderId = senderId,
            senderUsername = senderUsername,
            senderAvatarUrl = senderAvatarUrl,
            text = text,
            mediaUrl = mediaUrl,
            mediaType = mediaType,
            audioDurationSeconds = audioDuration,
            createdAt = System.currentTimeMillis()
        )

        val id = firebaseService.sendMessage(msg)
        dao.insertMessage(msg.copy(id = id))
        return id
    }

    suspend fun getOrCreateConversation(
        currentUserId: String,
        currentUsername: String,
        currentAvatar: String,
        otherUser: User
    ): Conversation {
        val conv = firebaseService.getOrCreateConversation(currentUserId, currentUsername, currentAvatar, otherUser)
        dao.insertConversation(conv)
        return conv
    }

    suspend fun searchUsers(query: String): List<User> {
        val remoteResults = firebaseService.searchUsers(query)
        if (remoteResults.isNotEmpty()) {
            dao.insertUsers(remoteResults)
        }
        return remoteResults
    }

    suspend fun addComment(postId: String, author: User, text: String): String {
        val comment = Comment(
            postId = postId,
            authorId = author.id,
            authorUsername = author.username,
            authorAvatarUrl = author.avatarUrl,
            isAuthorVerified = author.isVerified,
            text = text,
            createdAt = System.currentTimeMillis()
        )
        val id = firebaseService.addComment(comment)
        val commentWithId = comment.copy(id = id)
        dao.insertComment(commentWithId)
        val cachedPost = dao.getPostById(postId)
        if (cachedPost != null) {
            dao.insertPost(cachedPost.copy(commentsCount = cachedPost.commentsCount + 1))
        }
        return id
    }

    suspend fun savePost(postId: String, userId: String) {
        firebaseService.savePost(postId, userId)
        dao.insertSavedPost(SavedPost(postId = postId, userId = userId))
        val post = dao.getPostById(postId)
        if (post != null) {
            dao.insertPost(post.copy(isSavedByMe = true))
        }
    }

    suspend fun unsavePost(postId: String, userId: String) {
        firebaseService.unsavePost(postId, userId)
        dao.deleteSavedPost(postId, userId)
        val post = dao.getPostById(postId)
        if (post != null) {
            dao.insertPost(post.copy(isSavedByMe = false))
        }
    }

    suspend fun clearLocalData() {
        dao.clearAllPosts()
        dao.clearAllUsers()
        dao.clearAllStories()
        dao.clearAllReels()
        dao.clearAllConversations()
        dao.clearAllMessages()
    }
}
