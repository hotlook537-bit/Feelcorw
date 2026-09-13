package com.example.util

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioPlayerHelper(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingUrl: String? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playingUrl = MutableStateFlow<String?>(null)
    val playingUrl: StateFlow<String?> = _playingUrl.asStateFlow()

    fun play(url: String, onCompletion: () -> Unit = {}) {
        if (currentPlayingUrl == url && mediaPlayer?.isPlaying == true) {
            pause()
            return
        }

        stop()

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, Uri.parse(url))
                prepareAsync()
                setOnPreparedListener {
                    start()
                    currentPlayingUrl = url
                    _playingUrl.value = url
                    _isPlaying.value = true
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    _playingUrl.value = null
                    currentPlayingUrl = null
                    onCompletion()
                }
                setOnErrorListener { _, _, _ ->
                    stop()
                    false
                }
            }
        } catch (e: Exception) {
            stop()
        }
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
            }
        }
    }

    fun resume() {
        mediaPlayer?.let {
            it.start()
            _isPlaying.value = true
        }
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // ignore
        } finally {
            mediaPlayer = null
            currentPlayingUrl = null
            _playingUrl.value = null
            _isPlaying.value = false
        }
    }

    fun release() {
        stop()
    }
}
