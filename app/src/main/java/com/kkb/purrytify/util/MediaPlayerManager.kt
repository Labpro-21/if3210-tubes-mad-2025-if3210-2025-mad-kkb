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

    private var currentPosition: Int = 0

    fun play(
        song: Song,
        uri: Uri,
        contentResolver: ContentResolver,
        onError: (Exception) -> Unit = {}
    ) {
        try {
            val isResuming = _currentSong.value?.id == song.id && currentPosition > 0

            if (isResuming) {
                mediaPlayer?.let {
                    if (!it.isPlaying) {
                        it.start()
                        _isPlaying.value = true
                        Log.d("MediaPlayerManager", "Playback resumed at $currentPosition ms")
                        return
                    }
                }
            } else {
                stop()
                currentPosition = 0
            }

            val afd = contentResolver.openAssetFileDescriptor(uri, "r")
            if (afd != null) {
                afd.use { // Pastikan afd ditutup setelah digunakan
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(it.fileDescriptor)

                        setOnPreparedListener {
                            start()
                            _isPlaying.value = true
                            Log.d("MediaPlayerManager", "Playback started from beginning")
                        }

                        setOnCompletionListener {
                            stop()
                        }

                        prepareAsync()
                    }
                }

                _currentSong.value = song
            }
        } catch (e: Exception) {
            onError(e)
            Log.e("MediaPlayerManager", "Playback failed: ${e.message}")
        }
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                currentPosition = it.currentPosition
                it.pause()
                _isPlaying.value = false
                Log.d("MediaPlayerManager", "Playback paused at $currentPosition ms")
            }
        }
    }

    fun stop() {
        mediaPlayer?.release()
        mediaPlayer = null
        _isPlaying.value = false
        _currentSong.value = null
        currentPosition = 0
        Log.d("MediaPlayerManager", "Playback stopped")
    }

    fun getPlayer(): MediaPlayer? = mediaPlayer
}