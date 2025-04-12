package com.kkb.purrytify.util

import android.content.ContentResolver
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log

object MediaPlayerManager {
    private var mediaPlayer: MediaPlayer? = null
    var isPlaying: Boolean = false
        private set

    fun play(
        uri: Uri,
        contentResolver: ContentResolver,
        onError: (Exception) -> Unit = {}
    ) {
        try {
            stop() // Stop any existing playback
            val afd = contentResolver.openAssetFileDescriptor(uri, "r")
            if (afd != null) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(afd.fileDescriptor)
                    prepare()
                    start()
                }
                isPlaying = true
                Log.d("MediaPlayerManager", "Playback started")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onError(e)
            Log.e("MediaPlayerManager", "Playback failed: ${e.message}")
        }
    }

    fun pause() {
        mediaPlayer?.pause()
        isPlaying = false
        Log.d("MediaPlayerManager", "Playback paused")
    }

    fun stop() {
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
        Log.d("MediaPlayerManager", "Playback stopped")
    }
}