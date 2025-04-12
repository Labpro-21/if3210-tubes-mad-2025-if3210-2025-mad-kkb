package com.kkb.purrytify.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkb.purrytify.data.dao.SongDao
import com.kkb.purrytify.data.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class SongViewModel @Inject constructor(
    private val songDao: SongDao
) : ViewModel() {
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()
    private val _selectedSong = MutableStateFlow<Song?>(null)
    val selectedSong: StateFlow<Song?> = _selectedSong.asStateFlow()

    init {
        refreshSongs()
    }

    private fun refreshSongs() {
        viewModelScope.launch {
            _songs.value = songDao.getAllSongs()
        }
    }

    fun insertSong(song: Song) {
        viewModelScope.launch {
            songDao.insert(song)
            refreshSongs() // refresh after inserting
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