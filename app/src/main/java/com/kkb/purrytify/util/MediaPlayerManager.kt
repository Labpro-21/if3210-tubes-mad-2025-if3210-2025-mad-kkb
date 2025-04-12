package com.kkb.purrytify.util

import android.content.ContentResolver
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.kkb.purrytify.data.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object MediaPlayerManager {
    private var mediaPlayer: MediaPlayer? = null
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong

    fun play(
        song: Song,
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
                _currentSong.value = song
                _isPlaying.value = true
                Log.d("MediaPlayerManager", "Playback started")
            }
        } catch (e: Exception) {
            onError(e)
            Log.e("MediaPlayerManager", "Playback failed: ${e.message}")
        }
    }

    fun pause() {
        mediaPlayer?.pause()
        _isPlaying.value = false
        Log.d("MediaPlayerManager", "Playback paused")
    }

    fun stop() {
        mediaPlayer?.release()
        mediaPlayer = null
        _isPlaying.value = false
        _currentSong.value = null
        Log.d("MediaPlayerManager", "Playback stopped")
    }

    fun getPlayer(): MediaPlayer? = mediaPlayer
}