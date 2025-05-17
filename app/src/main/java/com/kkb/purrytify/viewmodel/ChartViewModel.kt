package com.kkb.purrytify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkb.purrytify.data.model.ChartSong
import com.kkb.purrytify.data.model.Song
import com.kkb.purrytify.data.model.UserSongs
import com.kkb.purrytify.data.remote.ApiService
import com.kkb.purrytify.data.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class ChartViewModel @Inject constructor(
    private val apiService: ApiService,
    private val songRepository: SongRepository
) : ViewModel() {

    private val _chartSongs = MutableStateFlow<List<ChartSong>>(emptyList())
    val chartSongs: StateFlow<List<ChartSong>> = _chartSongs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var fetchJob: Job? = null

    fun fetchGlobalChart(forceRefresh: Boolean = false) {
        if (_isLoading.value) return
        if (_chartSongs.value.isNotEmpty() && !forceRefresh) return

        _isLoading.value = true
        _error.value = null

        fetchJob?.cancel()
        fetchJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getTopGlobal()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        _chartSongs.value = response.body() ?: emptyList()
                    } else {
                        _error.value = response.message()
                    }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = e.localizedMessage ?: "Unknown error"
                    _isLoading.value = false
                }
            }
        }
    }

    fun downloadChartToLocal(userId: Int) {
        val chartSongs = _chartSongs.value
        if (chartSongs.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            chartSongs.forEach { chartSong ->
                // Map ChartSong to Song and UserSongs
                val song = Song(
                    id = chartSong.id,
                    title = chartSong.title,
                    artist = chartSong.artist,
                    filePath = chartSong.url,
                    coverPath = chartSong.artwork
                )
                val userSong = UserSongs(
                    userId = userId,
                    songId = chartSong.id,
                    isLiked = false,
                    createdAt = LocalDateTime.now(),
                    lastPlayed = null
                )
                songRepository.insertSongWithUserSong(song, userSong)
            }
        }
    }
}