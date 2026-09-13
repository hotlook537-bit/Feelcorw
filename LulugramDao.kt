package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LulugramDao {

    // Users
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserById(userId: String): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserByIdOnce(userId: String): User?

    @Query("SELECT * FROM users WHERE username LIKE '%' || :query || '%' OR displayName LIKE '%' || :query || '%'")
    fun searchUsers(query: String): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)

    // Posts
    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<Post>>

    @Query("SELECT * FROM posts WHERE authorId = :userId ORDER BY createdAt DESC")
    fun getPostsByUserId(userId: String): Flow<List<Post>>

    @Query("SELECT * FROM posts WHERE id = :postId LIMIT 1")
    suspend fun getPostById(postId: String): Post?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<Post>)

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePostById(postId: String)

    // Stories
    @Query("SELECT * FROM stories WHERE expiresAt > :now ORDER BY createdAt ASC")
    fun getActiveStories(now: Long = System.currentTimeMillis()): Flow<List<Story>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: Story)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<Story>)

    @Query("DELETE FROM stories WHERE id = :storyId")
    suspend fun deleteStoryById(storyId: String)

    // Reels
    @Query("SELECT * FROM reels ORDER BY createdAt DESC")
    fun getAllReels(): Flow<List<Reel>>

    @Query("SELECT * FROM reels WHERE authorId = :userId ORDER BY createdAt DESC")
    fun getReelsByUserId(userId: String): Flow<List<Reel>>

    @Query("SELECT * FROM reels WHERE id = :reelId LIMIT 1")
    suspend fun getReelById(reelId: String): Reel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReel(reel: Reel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReels(reels: List<Reel>)

    @Query("DELETE FROM reels WHERE id = :reelId")
    suspend fun deleteReelById(reelId: String)

    // Conversations
    @Query("SELECT * FROM conversations WHERE isArchived = 0 ORDER BY isPinned DESC, lastMessageTimestamp DESC")
    fun getActiveConversations(): Flow<List<Conversation>>

    @Query("SELECT * FROM conversations WHERE isArchived = 1 ORDER BY lastMessageTimestamp DESC")
    fun getArchivedConversations(): Flow<List<Conversation>>

    @Query("SELECT * FROM conversations WHERE id = :convId LIMIT 1")
    fun getConversationById(convId: String): Flow<Conversation?>

    @Query("SELECT * FROM conversations WHERE id = :convId LIMIT 1")
    suspend fun getConversationByIdOnce(convId: String): Conversation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conv: Conversation)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(convs: List<Conversation>)

    @Query("DELETE FROM conversations WHERE id = :convId")
    suspend fun deleteConversationById(convId: String)

    // Messages
    @Query("SELECT * FROM direct_messages WHERE conversationId = :convId ORDER BY createdAt ASC")
    fun getMessagesForConversation(convId: String): Flow<List<DirectMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(msg: DirectMessage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(msgs: List<DirectMessage>)

    @Query("DELETE FROM direct_messages WHERE id = :msgId")
    suspend fun deleteMessageById(msgId: String)

    @Query("DELETE FROM direct_messages WHERE conversationId = :convId")
    suspend fun clearMessagesForConversation(convId: String)

    // Comments
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY createdAt ASC")
    fun getCommentsForPost(postId: String): Flow<List<Comment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<Comment>)

    @Query("DELETE FROM comments WHERE id = :commentId")
    suspend fun deleteCommentById(commentId: String)

    // Notifications
    @Query("SELECT * FROM notifications WHERE recipientId = :userId ORDER BY createdAt DESC")
    fun getNotificationsForUser(userId: String): Flow<List<LuluNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: LuluNotification)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<LuluNotification>)

    @Query("UPDATE notifications SET isRead = 1 WHERE recipientId = :userId")
    suspend fun markAllNotificationsRead(userId: String)

    // Saved Posts
    @Query("SELECT p.* FROM posts p INNER JOIN saved_posts s ON p.id = s.postId WHERE s.userId = :userId ORDER BY s.savedAt DESC")
    fun getSavedPostsForUser(userId: String): Flow<List<Post>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedPost(savedPost: SavedPost)

    @Query("DELETE FROM saved_posts WHERE postId = :postId AND userId = :userId")
    suspend fun deleteSavedPost(postId: String, userId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM saved_posts WHERE postId = :postId AND userId = :userId)")
    suspend fun isPostSavedByMe(postId: String, userId: String): Boolean

    // Likes
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLike(like: Like)

    @Query("DELETE FROM likes WHERE targetId = :targetId AND userId = :userId")
    suspend fun deleteLike(targetId: String, userId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM likes WHERE targetId = :targetId AND userId = :userId)")
    suspend fun isLikedByMe(targetId: String, userId: String): Boolean

    // Follows
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollow(follow: Follow)

    @Query("DELETE FROM follows WHERE followerId = :followerId AND followingId = :followingId")
    suspend fun deleteFollow(followerId: String, followingId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM follows WHERE followerId = :followerId AND followingId = :followingId AND status = 'accepted')")
    suspend fun isFollowing(followerId: String, followingId: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM follows WHERE followerId = :followerId AND followingId = :followingId AND status = 'pending')")
    suspend fun isFollowPending(followerId: String, followingId: String): Boolean

    // Blocks and Mutes
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlock(block: Block)

    @Query("DELETE FROM blocks WHERE blockerId = :blockerId AND blockedId = :blockedId")
    suspend fun deleteBlock(blockerId: String, blockedId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM blocks WHERE blockerId = :blockerId AND blockedId = :blockedId)")
    suspend fun isUserBlocked(blockerId: String, blockedId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMute(mute: Mute)

    @Query("DELETE FROM mutes WHERE muterId = :muterId AND mutedId = :mutedId")
    suspend fun deleteMute(muterId: String, mutedId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM mutes WHERE muterId = :muterId AND mutedId = :mutedId)")
    suspend fun isUserMuted(muterId: String, mutedId: String): Boolean

    // Story Highlights
    @Query("SELECT * FROM story_highlights WHERE authorId = :userId ORDER BY createdAt DESC")
    fun getHighlightsByUserId(userId: String): Flow<List<StoryHighlight>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighlight(highlight: StoryHighlight)

    @Query("DELETE FROM story_highlights WHERE id = :highlightId")
    suspend fun deleteHighlightById(highlightId: String)

    // Clear all tables (for logout/reset)
    @Query("DELETE FROM posts")
    suspend fun clearAllPosts()

    @Query("DELETE FROM users")
    suspend fun clearAllUsers()

    @Query("DELETE FROM stories")
    suspend fun clearAllStories()

    @Query("DELETE FROM reels")
    suspend fun clearAllReels()

    @Query("DELETE FROM conversations")
    suspend fun clearAllConversations()

    @Query("DELETE FROM direct_messages")
    suspend fun clearAllMessages()
}
