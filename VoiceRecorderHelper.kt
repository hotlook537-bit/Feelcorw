package com.example.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.IOException

class VoiceRecorderHelper(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var startTime: Long = 0

    val isRecording: Boolean
        get() = recorder != null

    fun startRecording(): File? {
        val outputDir = context.cacheDir
        val file = File(outputDir, "voice_${System.currentTimeMillis()}.mp4")
        currentOutputFile = file

        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128000)
            setAudioSamplingRate(44100)
            setOutputFile(file.absolutePath)
            try {
                prepare()
                start()
                startTime = System.currentTimeMillis()
            } catch (e: IOException) {
                release()
                recorder = null
                return null
            }
        }
        return file
    }

    fun stopRecording(): Pair<File?, Int> {
        val durationSeconds = if (startTime > 0) {
            ((System.currentTimeMillis() - startTime) / 1000).toInt()
        } else 0

        return try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            Pair(currentOutputFile, durationSeconds)
        } catch (e: Exception) {
            recorder = null
            Pair(null, 0)
        }
    }

    fun cancelRecording() {
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            // ignore
        } finally {
            recorder = null
            currentOutputFile?.delete()
            currentOutputFile = null
        }
    }
}
