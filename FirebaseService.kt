package com.example.data.remote

import com.example.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseService(
    val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val currentUserId: String?
        get() = auth.currentUser?.uid

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    // Realtime User Flow
    fun observeUser(userId: String): Flow<User?> = callbackFlow {
        val listener = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    trySend(FirestoreMappers.docToUser(snapshot))
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun getUser(userId: String): User? {
        val doc = firestore.collection("users").document(userId).get().await()
        return if (doc.exists()) FirestoreMappers.docToUser(doc) else null
    }

    suspend fun saveUser(user: User) {
        firestore.collection("users").document(user.id)
            .set(FirestoreMappers.userToMap(user))
            .await()
    }

    suspend fun updateUserProfile(userId: String, updates: Map<String, Any>) {
        firestore.collection("users").document(userId)
            .update(updates)
            .await()
    }

    // Realtime Posts Flow
    fun observeFeedPosts(): Flow<List<Post>> = callbackFlow {
        val listener = firestore.collection("posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val posts = snapshot?.documents?.map { FirestoreMappers.docToPost(it) } ?: emptyList()
                trySend(posts)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createPost(post: Post): String {
        val docRef = if (post.id.isNotEmpty()) {
            firestore.collection("posts").document(post.id)
        } else {
            firestore.collection("posts").document()
        }
        val postWithId = post.copy(id = docRef.id)
        docRef.set(FirestoreMappers.postToMap(postWithId)).await()

        // Increment user posts count
        firestore.collection("users").document(post.authorId)
            .update("postsCount", FieldValue.increment(1))
            .await()

        return docRef.id
    }

    suspend fun deletePost(postId: String, authorId: String) {
        firestore.collection("posts").document(postId).delete().await()
        firestore.collection("users").document(authorId)
            .update("postsCount", FieldValue.increment(-1))
            .await()
    }

    suspend fun likePost(postId: String, userId: String, postAuthorId: String? = null, senderUsername: String = "", senderAvatar: String = "") {
        val likeRef = firestore.collection("posts").document(postId)
            .collection("likes").document(userId)
        likeRef.set(mapOf("userId" to userId, "createdAt" to System.currentTimeMillis())).await()

        firestore.collection("users").document(userId)
            .collection("likes").document(postId)
            .set(mapOf("targetId" to postId, "targetType" to "post", "createdAt" to System.currentTimeMillis()))
            .await()

        firestore.collection("posts").document(postId)
            .update("likesCount", FieldValue.increment(1))
            .await()

        if (!postAuthorId.isNullOrBlank() && postAuthorId != userId) {
            try {
                val notifRef = firestore.collection("users").document(postAuthorId)
                    .collection("notifications").document()
                val notif = LuluNotification(
                    id = notifRef.id,
                    recipientId = postAuthorId,
                    senderId = userId,
                    senderUsername = senderUsername,
                    senderAvatarUrl = senderAvatar,
                    type = "like",
                    targetId = postId,
                    previewText = "liked your post",
                    isRead = false,
                    createdAt = System.currentTimeMillis()
                )
                notifRef.set(FirestoreMappers.notificationToMap(notif)).await()
            } catch (ignored: Exception) {}
        }
    }

    suspend fun unlikePost(postId: String, userId: String) {
        val likeRef = firestore.collection("posts").document(postId)
            .collection("likes").document(userId)
        likeRef.delete().await()

        firestore.collection("users").document(userId)
            .collection("likes").document(postId)
            .delete()
            .await()

        firestore.collection("posts").document(postId)
            .update("likesCount", FieldValue.increment(-1))
            .await()
    }

    suspend fun getUserLikes(userId: String): List<String> {
        val snapshot = firestore.collection("users").document(userId)
            .collection("likes")
            .get()
            .await()
        return snapshot.documents.map { it.id }
    }

    // Realtime Stories Flow
    fun observeActiveStories(): Flow<List<Story>> = callbackFlow {
        val now = System.currentTimeMillis()
        val listener = firestore.collection("stories")
            .whereGreaterThan("expiresAt", now)
            .orderBy("expiresAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val stories = snapshot?.documents?.map { FirestoreMappers.docToStory(it) } ?: emptyList()
                trySend(stories)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createStory(story: Story): String {
        val docRef = if (story.id.isNotEmpty()) {
            firestore.collection("stories").document(story.id)
        } else {
            firestore.collection("stories").document()
        }
        val storyWithId = story.copy(id = docRef.id)
        docRef.set(FirestoreMappers.storyToMap(storyWithId)).await()
        return docRef.id
    }

    // Realtime Reels Flow
    fun observeReels(): Flow<List<Reel>> = callbackFlow {
        val listener = firestore.collection("reels")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(30)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val reels = snapshot?.documents?.map { FirestoreMappers.docToReel(it) } ?: emptyList()
                trySend(reels)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createReel(reel: Reel): String {
        val docRef = if (reel.id.isNotEmpty()) {
            firestore.collection("reels").document(reel.id)
        } else {
            firestore.collection("reels").document()
        }
        val reelWithId = reel.copy(id = docRef.id)
        docRef.set(FirestoreMappers.reelToMap(reelWithId)).await()
        return docRef.id
    }

    suspend fun likeReel(reelId: String, userId: String) {
        val likeRef = firestore.collection("reels").document(reelId)
            .collection("likes").document(userId)
        likeRef.set(mapOf("userId" to userId, "createdAt" to System.currentTimeMillis())).await()

        firestore.collection("reels").document(reelId)
            .update("likesCount", FieldValue.increment(1)).await()
    }

    suspend fun unlikeReel(reelId: String, userId: String) {
        val likeRef = firestore.collection("reels").document(reelId)
            .collection("likes").document(userId)
        likeRef.delete().await()

        firestore.collection("reels").document(reelId)
            .update("likesCount", FieldValue.increment(-1)).await()
    }

    // Realtime Conversations Flow
    fun observeConversations(userId: String): Flow<List<Conversation>> = callbackFlow {
        val listener = firestore.collection("conversations")
            .whereArrayContains("participantIds", userId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val convs = snapshot?.documents?.map { FirestoreMappers.docToConversation(it) } ?: emptyList()
                trySend(convs)
            }
        awaitClose { listener.remove() }
    }

    // Realtime Direct Messages Flow
    fun observeMessages(conversationId: String): Flow<List<DirectMessage>> = callbackFlow {
        val listener = firestore.collection("conversations").document(conversationId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val msgs = snapshot?.documents?.map { FirestoreMappers.docToMessage(it) } ?: emptyList()
                trySend(msgs)
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(msg: DirectMessage): String {
        val convRef = firestore.collection("conversations").document(msg.conversationId)
        val msgRef = convRef.collection("messages").document()
        val msgWithId = msg.copy(id = msgRef.id)
        msgRef.set(FirestoreMappers.messageToMap(msgWithId)).await()

        convRef.update(
            mapOf(
                "lastMessageText" to if (msg.mediaType != "none") "[${msg.mediaType.uppercase()}] ${msg.text}" else msg.text,
                "lastMessageSenderId" to msg.senderId,
                "lastMessageTimestamp" to msg.createdAt
            )
        ).await()

        return msgRef.id
    }

    suspend fun createConversation(conv: Conversation): String {
        val convRef = if (conv.id.isNotEmpty()) {
            firestore.collection("conversations").document(conv.id)
        } else {
            firestore.collection("conversations").document()
        }
        val convWithId = conv.copy(id = convRef.id)
        convRef.set(FirestoreMappers.conversationToMap(convWithId)).await()
        return convRef.id
    }

    suspend fun getOrCreateConversation(
        currentUserId: String,
        currentUsername: String,
        currentAvatar: String,
        otherUser: User
    ): Conversation {
        val existing = firestore.collection("conversations")
            .whereArrayContains("participantIds", currentUserId)
            .get()
            .await()

        val match = existing.documents.firstOrNull { doc ->
            val pIds = (doc.get("participantIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            val isGrp = (doc.get("isGroup") as? Boolean) ?: false
            !isGrp && pIds.contains(otherUser.id)
        }

        if (match != null) {
            return FirestoreMappers.docToConversation(match)
        }

        val newConvRef = firestore.collection("conversations").document()
        val conv = Conversation(
            id = newConvRef.id,
            participantIds = listOf(currentUserId, otherUser.id),
            participantUsernames = listOf(currentUsername, otherUser.username),
            participantAvatars = listOf(currentAvatar, otherUser.avatarUrl),
            isGroup = false,
            groupTitle = otherUser.displayName.ifBlank { otherUser.username },
            groupAvatarUrl = otherUser.avatarUrl,
            lastMessageText = "",
            lastMessageSenderId = "",
            lastMessageTimestamp = System.currentTimeMillis()
        )
        newConvRef.set(FirestoreMappers.conversationToMap(conv)).await()
        return conv
    }

    suspend fun searchUsers(query: String): List<User> {
        if (query.isBlank()) return emptyList()
        val cleanQuery = query.trim()
        val snapshot = firestore.collection("users")
            .whereGreaterThanOrEqualTo("username", cleanQuery.lowercase())
            .whereLessThanOrEqualTo("username", cleanQuery.lowercase() + "\uf8ff")
            .limit(25)
            .get()
            .await()
        return snapshot.documents.map { FirestoreMappers.docToUser(it) }
    }

    // Comments
    fun observeComments(postId: String): Flow<List<Comment>> = callbackFlow {
        val listener = firestore.collection("posts").document(postId)
            .collection("comments")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val comments = snapshot?.documents?.map { FirestoreMappers.docToComment(it) } ?: emptyList()
                trySend(comments)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addComment(comment: Comment): String {
        val postRef = firestore.collection("posts").document(comment.postId)
        val commentRef = postRef.collection("comments").document()
        val commentWithId = comment.copy(id = commentRef.id)
        commentRef.set(FirestoreMappers.commentToMap(commentWithId)).await()

        postRef.update("commentsCount", FieldValue.increment(1)).await()
        return commentRef.id
    }

    // Follows
    suspend fun followUser(currentUserId: String, targetUserId: String) {
        val followDoc = firestore.collection("users").document(currentUserId)
            .collection("following").document(targetUserId)
        followDoc.set(mapOf("userId" to targetUserId, "createdAt" to System.currentTimeMillis())).await()

        val followerDoc = firestore.collection("users").document(targetUserId)
            .collection("followers").document(currentUserId)
        followerDoc.set(mapOf("userId" to currentUserId, "createdAt" to System.currentTimeMillis())).await()

        firestore.collection("users").document(currentUserId)
            .update("followingCount", FieldValue.increment(1)).await()
        firestore.collection("users").document(targetUserId)
            .update("followersCount", FieldValue.increment(1)).await()
    }

    suspend fun unfollowUser(currentUserId: String, targetUserId: String) {
        val followDoc = firestore.collection("users").document(currentUserId)
            .collection("following").document(targetUserId)
        followDoc.delete().await()

        val followerDoc = firestore.collection("users").document(targetUserId)
            .collection("followers").document(currentUserId)
        followerDoc.delete().await()

        firestore.collection("users").document(currentUserId)
            .update("followingCount", FieldValue.increment(-1)).await()
        firestore.collection("users").document(targetUserId)
            .update("followersCount", FieldValue.increment(-1)).await()
    }

    // Notifications
    fun observeNotifications(userId: String): Flow<List<LuluNotification>> = callbackFlow {
        val listener = firestore.collection("users").document(userId)
            .collection("notifications")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val notifs = snapshot?.documents?.map { FirestoreMappers.docToNotification(it) } ?: emptyList()
                trySend(notifs)
            }
        awaitClose { listener.remove() }
    }

    // Saved Posts
    suspend fun savePost(postId: String, userId: String) {
        val data = mapOf(
            "postId" to postId,
            "savedAt" to System.currentTimeMillis()
        )
        firestore.collection("users").document(userId)
            .collection("saved_posts").document(postId)
            .set(data).await()
    }

    suspend fun unsavePost(postId: String, userId: String) {
        firestore.collection("users").document(userId)
            .collection("saved_posts").document(postId)
            .delete().await()
    }

    suspend fun getUserSavedPosts(userId: String): List<String> {
        val snapshot = firestore.collection("users").document(userId)
            .collection("saved_posts").get().await()
        return snapshot.documents.map { it.id }
    }
}
