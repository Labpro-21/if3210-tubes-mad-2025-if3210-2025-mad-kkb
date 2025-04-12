package com.kkb.purrytify.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime

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
        refreshSongs()
    }

    private fun refreshSongs() {

        viewModelScope.launch {
            _songs.value = songDao.getAllSongs()
            val user_id = TokenStorage.getUserId(context)?.toIntOrNull()
            if (user_id != null){
                _userSongs.value = userSongDao.getUserSongsByUserId(user_id)
            }
            combine(_songs, _userSongs) { songs, userSongs ->
                userSongs.mapNotNull { userSong ->
                    val song = songs.find { it.id == userSong.songId }
                    song?.let {
                        UserSong(
                            userId = userSong.userId,
                            songId = userSong.songId,
                            title = it.title,
                            artist = it.artist,
                            filePath = it.filePath,
                            coverPath = it.coverPath,
                            isLiked = userSong.isLiked,
                            createdAt = userSong.createdAt,
                            lastPlayed = userSong.lastPlayed
                        )
                    }
                }
            }.collect { mergedList ->
                _userSongList.value = mergedList
                Log.d("usersong","${_userSongList.value}")
            }
            Log.d("usersong","${_userSongList.value}")
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
                    lastPlayed = null,
                    isLiked = false,
                )
                songDao.insert(song)
                userSongDao.insert(userSong)
//                _userSongs.value = userSongDao.getUserSongsByUserId(user_id)
                Log.d("usersongs","${_userSongs.value}")
            } else {
                Log.e("SongViewModel", "Failed to insert UserSongs: userId is null or invalid")
            }
            refreshSongs()
        }
    }

    fun selectSong(song: Song) {
        _selectedSong.value = song
    }

    fun getSongById(id: Int?):Song?{
        return _songs.value.find { it.id == id }
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