package com.kkb.purrytify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkb.purrytify.data.dao.SongDao
import com.kkb.purrytify.data.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongViewModel @Inject constructor(
    private val songDao: SongDao
) : ViewModel() {

    fun insertSong(song: Song) {
        viewModelScope.launch {
            songDao.insert(song)
        }
    }

    suspend fun getSongs(): List<Song> {
        return songDao.getAllSongs()
    }
}