package com.kkb.purrytify.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkb.purrytify.TokenStorage
import com.kkb.purrytify.UserSong
import com.kkb.purrytify.data.dao.SongDao
import com.kkb.purrytify.data.dao.UserSongDao
import com.kkb.purrytify.data.model.Song
import com.kkb.purrytify.data.model.UserSongs
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class SongViewModel @Inject constructor(
    private val songDao: SongDao,
    private val userSongDao: UserSongDao,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _selectedSong = MutableStateFlow<Song?>(null)
    val selectedSong: StateFlow<Song?> = _selectedSong.asStateFlow()

    private val _userSongs = MutableStateFlow<List<UserSongs>>(emptyList())
    val userSongs: StateFlow<List<UserSongs>> = _userSongs.asStateFlow()

    private val _userSongList = MutableStateFlow<List<UserSong>>(emptyList())
    val userSongList: StateFlow<List<UserSong>> = _userSongList.asStateFlow()

    init {
        setupUserSongListMerger()
        refreshSongs()
    }

    private fun setupUserSongListMerger() {
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                _songs,
                _userSongs
            ) { songs, userSongs ->
                userSongs.mapNotNull { userSong ->
                    songs.find { it.id == userSong.songId }?.let { song ->
                        UserSong(
                            userId = userSong.userId,
                            songId = userSong.songId,
                            title = song.title,
                            artist = song.artist,
                            filePath = song.filePath,
                            coverPath = song.coverPath,
                            isLiked = userSong.isLiked,
                            createdAt = userSong.createdAt,
                            lastPlayed = userSong.lastPlayed
                        )
                    }
                }
            }.collect { mergedList ->
                _userSongList.value = mergedList
            }
        }
    }

    private fun refreshSongs() {
        viewModelScope.launch(Dispatchers.IO) {
            val allSongs = songDao.getAllSongs()
            _songs.value = allSongs
            TokenStorage.getUserId(context)?.toIntOrNull()?.let { userId ->
                _userSongs.value = userSongDao.getUserSongsByUserId(userId)
            }
        }
    }

    fun insertSong(context: Context, song: Song) {
        viewModelScope.launch(Dispatchers.IO) {
            val songId = songDao.insert(song).toInt()
            val userId = TokenStorage.getUserId(context)?.toIntOrNull()
            if (userId != null) {
                val userSong = UserSongs(
                    userId = userId,
                    songId = songId,
                    createdAt = LocalDateTime.now(),
                    lastPlayed = null,
                    isLiked = false,
                )
                userSongDao.insert(userSong)
                // Update state directly for instant feedback
                _userSongs.update { it + userSong }
            } else {
                Log.e("SongViewModel", "Failed to insert UserSongs: userId is null or invalid")
            }
            // Update songs state directly
            _songs.update { it + song.copy(id = songId) }
        }
    }

    fun updateLastPlayed(songId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val userId = TokenStorage.getUserId(context)?.toIntOrNull()
            if (userId != null) {
                userSongDao.updateLastPlayed(
                    userId = userId,
                    songId = songId,
                    lastPlayed = LocalDateTime.now()
                )
                // Update state in memory for instant feedback
                _userSongs.update { list ->
                    list.map {
                        if (it.songId == songId) it.copy(lastPlayed = LocalDateTime.now()) else it
                    }
                }
            } else {
                Log.e("SongViewModel", "updateLastPlayed failed: userId is null")
            }
        }
    }

    fun selectSong(song: Song) {
        _selectedSong.value = song
    }

    fun getSongById(id: Int?): Song? {
        return _songs.value.find { it.id == id }
    }

    fun toggleLike(songId: Int?) {
        if (songId == null) return
        viewModelScope.launch(Dispatchers.IO) {
            val userId = TokenStorage.getUserId(context)?.toIntOrNull()
            if (userId != null) {
                val userSongs = userSongDao.getUserSongsByUserId(userId)
                val userSong = userSongs.find { it.songId == songId }
                if (userSong != null) {
                    val isLiked = !userSong.isLiked
                    userSongDao.updateIsLiked(userId, songId, isLiked)
                    // Update state in memory for instant feedback
                    _userSongs.update { list ->
                        list.map {
                            if (it.songId == songId) it.copy(isLiked = isLiked) else it
                        }
                    }
                } else {
                    val newUserSong = UserSongs(
                        userId = userId,
                        songId = songId,
                        createdAt = LocalDateTime.now(),
                        lastPlayed = null,
                        isLiked = true
                    )
                    userSongDao.insert(newUserSong)
                    _userSongs.update { it + newUserSong }
                }
            }
        }
    }

    val totalSongsCount: StateFlow<Int> = userSongList.map { it.size }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )

    val likedSongsCount: StateFlow<Int> = userSongList.map { list ->
        list.count { it.isLiked }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )

    val listenedSongsCount: StateFlow<Int> = userSongList.map { list ->
        list.count { it.lastPlayed != null }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )

    fun deleteSong(song: UserSong) {
        viewModelScope.launch(Dispatchers.IO) {
            songDao.deleteById(song.songId)
            _songs.update { it.filterNot { s -> s.id == song.songId } }
            _userSongs.update { it.filterNot { us -> us.songId == song.songId } }
        }
    }
}