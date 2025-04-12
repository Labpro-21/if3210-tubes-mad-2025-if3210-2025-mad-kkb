package com.kkb.purrytify.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkb.purrytify.TokenStorage
import com.kkb.purrytify.data.dao.SongDao
import com.kkb.purrytify.data.dao.UserSongDao
import com.kkb.purrytify.data.model.Song
import com.kkb.purrytify.data.model.UserSongs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime

@HiltViewModel
class SongViewModel @Inject constructor(
    private val songDao: SongDao,
    private val userSongDao: UserSongDao
) : ViewModel() {
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()
    private val _selectedSong = MutableStateFlow<Song?>(null)
    val selectedSong: StateFlow<Song?> = _selectedSong.asStateFlow()

    private val _userSongs = MutableStateFlow<List<UserSongs>>(emptyList())
    val userSongs: StateFlow<List<UserSongs>> = _userSongs.asStateFlow()

    init {
        refreshSongs()
    }

    private fun refreshSongs() {
        viewModelScope.launch {
            // Refresh songs
            _songs.value = songDao.getAllSongs()
        }
    }

    fun insertSong(context: Context, song: Song) {
        viewModelScope.launch {
            val song_id = songDao.insert(song).toInt()
            val user_id = TokenStorage.getUserId(context)?.toIntOrNull()
            if (user_id != null) {
                val userSong = UserSongs(
                    userId = user_id,
                    songId = song_id,
                    createdAt = LocalDateTime.now(),
                    lastPlayed = null
                )
                userSongDao.insert(userSong)
                _userSongs.value = userSongDao.getUserSongsByUserId(user_id)
            } else {
                Log.e("SongViewModel", "Failed to insert UserSongs: userId is null or invalid")
            }
            refreshSongs() // refresh after inserting
        }
    }

    fun selectSong(song: Song) {
        _selectedSong.value = song
    }

    fun getSongById(id: Int?):Song?{
        return _songs.value.find { it.id == id }
    }

    fun deleteSong(song: Song) {
        viewModelScope.launch {
            songDao.delete(song)
            refreshSongs() // refresh after deleting
        }

    }

//    fun insertSong(song: Song) {
//        viewModelScope.launch {
//            songDao.insert(song)
//        }
//    }
//
//    suspend fun getSongs(): List<Song> {
//        return songDao.getAllSongs()
//    }
}