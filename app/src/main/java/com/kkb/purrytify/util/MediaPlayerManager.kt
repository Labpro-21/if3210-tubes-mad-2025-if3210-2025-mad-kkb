package com.kkb.purrytify.util

import android.content.ContentResolver
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.kkb.purrytify.UserSong
import com.kkb.purrytify.data.repository.SongRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

object MediaPlayerManager {
    private var mediaPlayer: MediaPlayer? = null
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentSong = MutableStateFlow<UserSong?>(null)
    val currentSong: StateFlow<UserSong?> = _currentSong

    private var currentPosition: Int = 0
    private var songList = listOf<UserSong>()
    private var currentIndex = -1

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private var timeListenedJob: Job? = null
    private var lastReportedSeconds: Long = 0

    private var songRepository: SongRepository? = null

    fun setPlaylist(songs: List<UserSong>, startIndex: Int) {
        songList = songs
        currentIndex = startIndex
//        Log.d("media songs", songList.toString())
//        Log.d("media songs", currentIndex.toString())
        if (startIndex >= 0 && startIndex < songs.size) {
            _currentSong.value = songs[startIndex]
        }
    }

    fun setSongRepository(repository: SongRepository) {
        songRepository = repository
    }

    private fun updateLastPlayed(songId: Int, userId: Int) {
        songRepository?.let { repository ->
            coroutineScope.launch {
                try {
                    repository.updateLastPlayed(songId, userId, LocalDateTime.now())
                    Log.d("MediaPlayerManager", "Updated last played time for song $songId for user id $userId at ${LocalDateTime.now()}")
//                    Log.d("MediaPlayerManager", repository.getUserSongsByUserId(userId).toString())
                } catch (e: Exception) {
                    Log.e("MediaPlayerManager", "Failed to update last played time: ${e.message}")
                }
            }
        }
    }

    fun play(
        song: UserSong,
        uri: Uri?,
        contentResolver: ContentResolver?,
        isRemote: Boolean = false,
        onError: (Exception) -> Unit = {},
        context: Context,
        onSongStarted: ((Int) -> Unit)? = null
    ) {
        try {

            val isResuming = _currentSong.value?.songId == song.songId && currentPosition > 0
            Log.d("playingsong", song.toString())
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
                    song.userId?.let { userId ->
                        updateLastPlayed(song.songId, userId)
                    }

                    startTimeListenedTracking(song.songId)

                    // Show notification with playing state
                    NotificationUtil.showMusicNotification(context, song, true)
                    onSongStarted?.invoke(song.songId)
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

    private fun startTimeListenedTracking(songId: Int) {
        timeListenedJob?.cancel()
        lastReportedSeconds = 0L

        timeListenedJob = coroutineScope.launch {
            while (isPlaying.value && mediaPlayer != null) {
                val player = mediaPlayer
                if (player != null && player.isPlaying) {
                    val currentSeconds = player.currentPosition / 1000L
                    if (currentSeconds > lastReportedSeconds) {
                        val delta = currentSeconds - lastReportedSeconds
                        if (delta > 0) {
                            try {
                                songRepository?.let { repository ->
                                    currentSong.value?.userId?.let { userId ->
                                        repository.updateTimeListened(songId, userId, delta)
                                        lastReportedSeconds = currentSeconds
                                        Log.d("MediaPlayerManager", "Updated time listened: +$delta seconds, total: $lastReportedSeconds seconds")
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("MediaPlayerManager", "Error updating time listened: ${e.message}", e)
                                // Don't update lastReportedSeconds if update failed, so we can try again
                            }
                        }
                    }
                }
                delay(3000)
            }
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
        if (songList.isNotEmpty()) {
            currentIndex = if (currentIndex <= 0) {
                songList.size - 1
            } else {
                currentIndex - 1
            }
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
        if (songList.isNotEmpty()) {
            currentIndex = if (currentIndex >= songList.size - 1) {
                0
            } else {
                currentIndex + 1
            }
            val nextSong = songList[currentIndex]
            Log.d("nextsong", nextSong.toString())
            Log.d("nextsong", songList.toString())
            Log.d("nextsong(idx)", currentIndex.toString())
            val uri = Uri.parse(nextSong.filePath)
            val isRemote = nextSong.filePath.startsWith("http://") || nextSong.filePath.startsWith("https://")

            play(
                song = nextSong,
                uri = if (isRemote) null else uri,
                contentResolver = if (isRemote) null else context.contentResolver,
                isRemote = isRemote,
                context = context,

            )
        }
    }

    fun getPlayer(): MediaPlayer? = mediaPlayer

    fun getCurrentSong(): UserSong? = songList[currentIndex]
}