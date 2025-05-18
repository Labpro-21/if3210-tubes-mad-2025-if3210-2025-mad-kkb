package com.kkb.purrytify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkb.purrytify.data.dao.SongPlayDateInfo
import com.kkb.purrytify.data.dao.TopArtistTimeListened
import com.kkb.purrytify.data.dao.TopSongTimeListened
import com.kkb.purrytify.data.dao.UserSongDao
import com.kkb.purrytify.data.model.ProfileResponse
import com.kkb.purrytify.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: ProfileResponse? = null,
    val error: String? = null
)

data class ProfileStatsUiState(
    val topSong: TopSongTimeListened? = null,
    val topArtist: TopArtistTimeListened? = null,
    val totalTimeListened: Long = 0L,
    val dayStreakSong: DayStreakSongInfo? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val apiService: ApiService,
    private val userSongDao: UserSongDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    private val _statsState = MutableStateFlow(ProfileStatsUiState())
    val statsState: StateFlow<ProfileStatsUiState> = _statsState

    private var cachedProfile: ProfileResponse? = null

    fun fetchProfile(token: String, forceRefresh: Boolean = false) {
        if (_uiState.value.isLoading) return
        if (cachedProfile != null && !forceRefresh) {
            _uiState.value = ProfileUiState(profile = cachedProfile)
            return
        }
        _uiState.value = ProfileUiState(isLoading = true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val profile = apiService.getProfile("Bearer $token")
                cachedProfile = profile
                withContext(Dispatchers.Main) {
                    _uiState.value = ProfileUiState(profile = profile)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = ProfileUiState(error = e.message ?: "Unknown error")
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun fetchProfileStats(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val topSong = userSongDao.getTopSongByTimeListened(userId)
            val topArtist = userSongDao.getTopArtistsByTimeListened(userId, 1).firstOrNull()
            val totalTime = userSongDao.getUserTotalTimeListened(userId) ?: 0L
            val dayStreakSong = getSongWithHighestDayStreak(userId)
            withContext(Dispatchers.Main) {
                _statsState.value = ProfileStatsUiState(
                    topSong = topSong,
                    topArtist = topArtist,
                    totalTimeListened = totalTime,
                    dayStreakSong = dayStreakSong
                )
            }
        }
    }

    private fun calculateDayStreak(dates: List<LocalDate>): Int {
        if (dates.isEmpty()) return 0
        val sorted = dates.distinct().sortedDescending()
        var streak = 1
        var prev = sorted.first()
        for (date in sorted.drop(1)) {
            if (prev.minusDays(1) == date) {
                streak++
                prev = date
            } else {
                break
            }
        }
        // Only count streak if it includes today
        return if (sorted.first() == LocalDate.now()) streak else 0
    }

    private suspend fun getSongWithHighestDayStreak(userId: Int): DayStreakSongInfo? {
        val playInfos = userSongDao.getSongsWithPlayDates(userId)
        var maxStreak = 0
        var bestSong: SongPlayDateInfo? = null

        for (info in playInfos) {
            val lastPlayed = info.lastPlayed?.toLocalDate() ?: continue
            val streak = calculateDayStreak(listOf(lastPlayed))
            if (streak > maxStreak) {
                maxStreak = streak
                bestSong = info
            }
        }

        return bestSong?.let {
            DayStreakSongInfo(
                id = it.id,
                title = it.title,
                artist = it.artist,
                filePath = it.filePath,
                coverPath = it.coverPath,
                dayStreak = maxStreak
            )
        }
    }
}

data class DayStreakSongInfo(
    val id: Int,
    val title: String,
    val artist: String,
    val filePath: String,
    val coverPath: String?,
    val dayStreak: Int
)