package com.example.ui.viewmodel

sealed class CallState {
    object Idle : CallState()
    data class Calling(val recipientId: String, val recipientName: String, val isVideo: Boolean) : CallState()
    data class Incoming(val callerId: String, val callerName: String, val isVideo: Boolean) : CallState()
    data class Connected(val peerId: String, val peerName: String, val isVideo: Boolean, val startTime: Long) : CallState()
    data class Ended(val reason: String) : CallState()
}
