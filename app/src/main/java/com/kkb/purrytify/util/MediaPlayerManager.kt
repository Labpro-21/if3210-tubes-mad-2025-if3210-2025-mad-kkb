package com.kkb.purrytify.util

import android.content.ContentResolver
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.kkb.purrytify.UserSong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object MediaPlayerManager {
    private var mediaPlayer: MediaPlayer? = null
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentSong = MutableStateFlow<UserSong?>(null)
    val currentSong: StateFlow<UserSong?> = _currentSong

    private var currentPosition: Int = 0
    private var songList = listOf<UserSong>()
    private var currentIndex = -1

    fun setPlaylist(songs: List<UserSong>, startIndex: Int) {
        songList = songs
        currentIndex = startIndex
    }

    fun play(
        song: UserSong,
        uri: Uri?,
        contentResolver: ContentResolver?,
        isRemote: Boolean = false,
        onError: (Exception) -> Unit = {},
        context: Context
    ) {
        try {
            val isResuming = _currentSong.value?.songId == song.songId && currentPosition > 0

            if (isResuming) {
                mediaPlayer?.let {
                    if (!it.isPlaying) {
                        it.start()
                        _isPlaying.value = true
                        Log.d("MediaPlayerManager", "Playback resumed at $currentPosition ms")
                        // Update notification with playing state
                        NotificationUtil.showMusicNotification(context, song, true)
                        return
                    }
                }
            } else {
                stop(context) // Stop previous playback and cancel notification
                currentPosition = 0
            }

            mediaPlayer = MediaPlayer().apply {
                if (isRemote) {
                    setDataSource(song.filePath)
                } else if (uri != null && contentResolver != null) {
                    val afd = contentResolver.openAssetFileDescriptor(uri, "r")
                    if (afd != null) {
                        afd.use {
                            setDataSource(it.fileDescriptor)
                        }
                    } else {
                        throw Exception("Unable to open file descriptor for local file")
                    }
                } else {
                    throw Exception("Invalid parameters for playback")
                }

                setOnPreparedListener {
                    start()
                    _isPlaying.value = true
                    Log.d("MediaPlayerManager", "Playback started from beginning")
                    // Show notification with playing state
                    NotificationUtil.showMusicNotification(context, song, true)
                }

                setOnCompletionListener {
                    if (currentIndex >= 0 && currentIndex < songList.size - 1) {
                        // Auto-play next song
                        next(context)
                    } else {
                        stop(context)
                    }
                }

                prepareAsync()
            }

            _currentSong.value = song
        } catch (e: Exception) {
            onError(e)
            Log.e("MediaPlayerManager", "Playback failed: ${e.message}")
        }
    }

    fun pause(context: Context? = null) {
        mediaPlayer?.let {
            if (it.isPlaying) {
                currentPosition = it.currentPosition
                it.pause()
                _isPlaying.value = false
                Log.d("MediaPlayerManager", "Playback paused at $currentPosition ms")

                // Update notification with paused state
                context?.let { ctx ->
                    _currentSong.value?.let { song ->
                        NotificationUtil.updateNotificationPlayState(ctx, song, false)
                    }
                }
            }
        }
    }

    fun resume(context: Context) {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
                _isPlaying.value = true

                // Update notification with playing state
                _currentSong.value?.let { song ->
                    NotificationUtil.updateNotificationPlayState(context, song, true)
                }
            }
        }
    }

    fun stop(context: Context? = null) {
        mediaPlayer?.release()
        mediaPlayer = null
        _isPlaying.value = false
        _currentSong.value = null
        currentPosition = 0
        Log.d("MediaPlayerManager", "Playback stopped")

        // Cancel notification
        context?.let { ctx ->
            NotificationUtil.cancelMusicNotification(ctx)
        }
    }

    fun previous(context: Context) {
        if (currentIndex > 0 && songList.isNotEmpty()) {
            currentIndex--
            val prevSong = songList[currentIndex]
            val uri = Uri.parse(prevSong.filePath)
            val isRemote = prevSong.filePath.startsWith("http://") || prevSong.filePath.startsWith("https://")

            play(
                song = prevSong,
                uri = if (isRemote) null else uri,
                contentResolver = if (isRemote) null else context.contentResolver,
                isRemote = isRemote,
                context = context
            )
        }
    }

    fun next(context: Context) {
        if (currentIndex < songList.size - 1 && songList.isNotEmpty()) {
            currentIndex++
            val nextSong = songList[currentIndex]
            val uri = Uri.parse(nextSong.filePath)
            val isRemote = nextSong.filePath.startsWith("http://") || nextSong.filePath.startsWith("https://")

            play(
                song = nextSong,
                uri = if (isRemote) null else uri,
                contentResolver = if (isRemote) null else context.contentResolver,
                isRemote = isRemote,
                context = context
            )
        }
    }

    fun getPlayer(): MediaPlayer? = mediaPlayer
}