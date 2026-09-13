package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.LulugramDatabase
import com.example.data.model.*
import com.example.data.remote.FirebaseService
import com.example.data.remote.ImageKitService
import com.example.data.repository.LulugramRepository
import com.example.util.AudioPlayerHelper
import com.example.util.VoiceRecorderHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class LulugramViewModel(application: Application) : AndroidViewModel(application) {

    private val database = LulugramDatabase.getInstance(application)
    private val dao = database.lulugramDao()
    private val firebaseService = FirebaseService()
    private val imageKitService = ImageKitService()
    val repository = LulugramRepository(dao, firebaseService, imageKitService)

    val voiceRecorder = VoiceRecorderHelper(application)
    val audioPlayer = AudioPlayerHelper(application)

    // Current logged in user ID
    private val _currentUserId = MutableStateFlow<String?>(firebaseService.currentUserId)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    // Current User Profile State
    val currentUser: StateFlow<User?> = _currentUserId
        .flatMapLatest { uid ->
            if (uid != null) {
                dao.getUserById(uid)
            } else {
                flowOf(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Feed Posts
    val feedPosts: StateFlow<List<Post>> = dao.getAllPosts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Stories
    val activeStories: StateFlow<List<Story>> = dao.getActiveStories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reels
    val reels: StateFlow<List<Reel>> = dao.getAllReels()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Conversations
    val conversations: StateFlow<List<Conversation>> = dao.getActiveConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Chat & Messages
    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    val currentMessages: StateFlow<List<DirectMessage>> = _activeConversationId
        .flatMapLatest { convId ->
            if (convId != null) {
                dao.getMessagesForConversation(convId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notifications
    val notifications: StateFlow<List<LuluNotification>> = _currentUserId
        .flatMapLatest { uid ->
            if (uid != null) {
                dao.getNotificationsForUser(uid)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Call State
    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    val callState: StateFlow<CallState> = _callState.asStateFlow()

    // Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<User>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                dao.searchUsers(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Saved Posts
    val savedPosts: StateFlow<List<Post>> = _currentUserId
        .flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList())
            else dao.getSavedPostsForUser(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Comments
    private val _activeCommentsPostId = MutableStateFlow<String?>(null)
    val activeCommentsPostId: StateFlow<String?> = _activeCommentsPostId.asStateFlow()

    val postComments: StateFlow<List<Comment>> = _activeCommentsPostId
        .flatMapLatest { postId ->
            if (postId == null) flowOf(emptyList())
            else dao.getCommentsForPost(postId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var activeCommentsJob: kotlinx.coroutines.Job? = null

    // UI Loading & Feedback
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Active listener jobs
    private val listenerJobs = mutableListOf<kotlinx.coroutines.Job>()
    private var activeChatJob: kotlinx.coroutines.Job? = null

    init {
        // Observe auth state
        firebaseService.auth.addAuthStateListener { auth ->
            val uid = auth.currentUser?.uid
            _currentUserId.value = uid
            if (uid != null) {
                refreshUserData(uid)
                startDataListeners(uid)
            }
        }

        // Also start listeners if already logged in
        firebaseService.currentUserId?.let { uid ->
            refreshUserData(uid)
            startDataListeners(uid)
        }
    }

    private fun startDataListeners(uid: String) {
        listenerJobs.forEach { it.cancel() }
        listenerJobs.clear()

        listenerJobs += viewModelScope.launch {
            try {
                firebaseService.observeFeedPosts().collect { posts ->
                    val reconciled = posts.map { post ->
                        val isLiked = dao.isLikedByMe(post.id, uid)
                        val isSaved = dao.isPostSavedByMe(post.id, uid)
                        post.copy(isLikedByMe = isLiked, isSavedByMe = isSaved)
                    }
                    dao.insertPosts(reconciled)
                }
            } catch (ignored: Exception) {}
        }

        listenerJobs += viewModelScope.launch {
            try {
                firebaseService.observeActiveStories().collect { stories ->
                    dao.insertStories(stories)
                }
            } catch (ignored: Exception) {}
        }

        listenerJobs += viewModelScope.launch {
            try {
                firebaseService.observeReels().collect { r ->
                    dao.insertReels(r)
                }
            } catch (ignored: Exception) {}
        }

        listenerJobs += viewModelScope.launch {
            try {
                firebaseService.observeConversations(uid).collect { convs ->
                    dao.insertConversations(convs)
                }
            } catch (ignored: Exception) {}
        }

        listenerJobs += viewModelScope.launch {
            try {
                firebaseService.observeNotifications(uid).collect { notifs ->
                    dao.insertNotifications(notifs)
                }
            } catch (ignored: Exception) {}
        }
    }

    fun refreshUserData(uid: String) {
        viewModelScope.launch {
            try {
                repository.syncUser(uid)
            } catch (e: Exception) {
                // logged
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank()) {
            viewModelScope.launch {
                try {
                    repository.searchUsers(query)
                } catch (ignored: Exception) {}
            }
        }
    }

    fun setActiveConversation(convId: String?) {
        _activeConversationId.value = convId
        activeChatJob?.cancel()
        activeChatJob = null
        if (convId != null) {
            activeChatJob = viewModelScope.launch {
                try {
                    firebaseService.observeMessages(convId).collect { msgs ->
                        dao.insertMessages(msgs)
                    }
                } catch (ignored: Exception) {}
            }
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    // Interactions
    fun toggleLikePost(post: Post) {
        val uid = _currentUserId.value ?: return
        val user = currentUser.value
        viewModelScope.launch {
            try {
                if (post.isLikedByMe) {
                    repository.unlikePost(post.id, uid)
                } else {
                    repository.likePost(
                        postId = post.id,
                        userId = uid,
                        postAuthorId = post.authorId,
                        senderUsername = user?.username ?: "",
                        senderAvatar = user?.avatarUrl ?: ""
                    )
                }
            } catch (e: Exception) {
                _statusMessage.value = "Failed to update like: ${e.localizedMessage}"
            }
        }
    }

    fun toggleSavePost(post: Post) {
        val uid = _currentUserId.value ?: return
        viewModelScope.launch {
            try {
                if (post.isSavedByMe) {
                    repository.unsavePost(post.id, uid)
                    _statusMessage.value = "Removed from saved"
                } else {
                    repository.savePost(post.id, uid)
                    _statusMessage.value = "Post saved"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Failed to save: ${e.localizedMessage}"
            }
        }
    }

    fun openCommentsForPost(postId: String) {
        _activeCommentsPostId.value = postId
        activeCommentsJob?.cancel()
        activeCommentsJob = viewModelScope.launch {
            try {
                firebaseService.observeComments(postId).collect { comments ->
                    dao.insertComments(comments)
                }
            } catch (ignored: Exception) {}
        }
    }

    fun closeComments() {
        _activeCommentsPostId.value = null
        activeCommentsJob?.cancel()
        activeCommentsJob = null
    }

    fun toggleLikeReel(reel: Reel) {
        val uid = _currentUserId.value ?: return
        viewModelScope.launch {
            try {
                if (reel.isLikedByMe) {
                    repository.unlikeReel(reel.id, uid)
                } else {
                    repository.likeReel(reel.id, uid)
                }
            } catch (e: Exception) {
                _statusMessage.value = "Failed to update like: ${e.localizedMessage}"
            }
        }
    }

    fun createPost(content: String, mediaFiles: List<File>, location: String = "", tags: List<String> = emptyList()) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.createPost(
                    authorId = user.id,
                    authorUsername = user.username,
                    authorDisplayName = user.displayName,
                    authorAvatarUrl = user.avatarUrl,
                    isAuthorVerified = user.isVerified,
                    content = content,
                    mediaFiles = mediaFiles,
                    location = location,
                    tags = tags
                )
                _statusMessage.value = "Post published successfully"
            } catch (e: Exception) {
                _statusMessage.value = "Error creating post: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createStory(mediaFile: File, caption: String = "") {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.createStory(
                    authorId = user.id,
                    authorUsername = user.username,
                    authorAvatarUrl = user.avatarUrl,
                    isAuthorVerified = user.isVerified,
                    mediaFile = mediaFile,
                    caption = caption
                )
                _statusMessage.value = "Story added successfully"
            } catch (e: Exception) {
                _statusMessage.value = "Error uploading story: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createReel(videoFile: File, description: String, audioTrack: String = "Original Audio") {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.createReel(
                    authorId = user.id,
                    authorUsername = user.username,
                    authorAvatarUrl = user.avatarUrl,
                    isAuthorVerified = user.isVerified,
                    videoFile = videoFile,
                    description = description,
                    audioTrack = audioTrack
                )
                _statusMessage.value = "Reel shared successfully"
            } catch (e: Exception) {
                _statusMessage.value = "Error sharing reel: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendDirectMessage(conversationId: String, text: String, mediaFile: File? = null, mediaType: String = "none") {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            try {
                repository.sendMessage(
                    conversationId = conversationId,
                    senderId = user.id,
                    senderUsername = user.username,
                    senderAvatarUrl = user.avatarUrl,
                    text = text,
                    mediaFile = mediaFile,
                    mediaType = mediaType
                )
            } catch (e: Exception) {
                _statusMessage.value = "Failed to send message: ${e.localizedMessage}"
            }
        }
    }

    fun sendVoiceMessage(conversationId: String, voiceFile: File, durationSeconds: Int) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            try {
                repository.sendMessage(
                    conversationId = conversationId,
                    senderId = user.id,
                    senderUsername = user.username,
                    senderAvatarUrl = user.avatarUrl,
                    text = "Voice note",
                    mediaFile = voiceFile,
                    mediaType = "audio",
                    audioDuration = durationSeconds
                )
            } catch (e: Exception) {
                _statusMessage.value = "Failed to send voice note: ${e.localizedMessage}"
            }
        }
    }

    fun startCall(recipientId: String, recipientName: String, isVideo: Boolean) {
        _callState.value = CallState.Calling(recipientId, recipientName, isVideo)
    }

    fun endCall() {
        _callState.value = CallState.Ended("Call ended")
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            _callState.value = CallState.Idle
        }
    }

    fun updateProfile(displayName: String, bio: String, website: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updated = user.copy(displayName = displayName, bio = bio, website = website)
                repository.saveUser(updated)
                _statusMessage.value = "Profile updated"
            } catch (e: Exception) {
                _statusMessage.value = "Failed to update profile: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadAvatar(file: File) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.uploadAvatar(user.id, file)
                _statusMessage.value = "Avatar updated"
            } catch (e: Exception) {
                _statusMessage.value = "Failed to update avatar: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun startConversationWithUser(targetUser: User, onReady: (String) -> Unit) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val conv = repository.getOrCreateConversation(
                    currentUserId = user.id,
                    currentUsername = user.username,
                    currentAvatar = user.avatarUrl,
                    otherUser = targetUser
                )
                setActiveConversation(conv.id)
                onReady(conv.id)
            } catch (e: Exception) {
                _statusMessage.value = "Could not open conversation: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addComment(postId: String, text: String) {
        val user = currentUser.value ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            try {
                repository.addComment(postId, user, text.trim())
                _statusMessage.value = "Comment posted"
            } catch (e: Exception) {
                _statusMessage.value = "Failed to post comment: ${e.localizedMessage}"
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            listenerJobs.forEach { it.cancel() }
            listenerJobs.clear()
            activeChatJob?.cancel()
            activeChatJob = null
            firebaseService.auth.signOut()
            _currentUserId.value = null
            _activeConversationId.value = null
            repository.clearLocalData()
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }
}
