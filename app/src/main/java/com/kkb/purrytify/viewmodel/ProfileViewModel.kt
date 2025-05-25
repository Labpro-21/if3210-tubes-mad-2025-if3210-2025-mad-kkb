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
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import android.util.Log
import com.kkb.purrytify.data.dao.DailySongPlaysDao
import com.kkb.purrytify.data.dao.DailyTimeListened

data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: ProfileResponse? = null,
    val error: String? = null
)

data class MonthlySoundCapsule(
    val month: String,
    val topSong: TopSongTimeListened?,
    val topSongs: List<TopSongTimeListened> = emptyList(),
    val topArtist: TopArtistTimeListened?,
    val topArtists: List<TopArtistTimeListened> = emptyList(),
    val totalTimeListened: Long,
    val dailyTime: List<DailyTimeListened> = emptyList(),
    val totalArtistsListened: Int,
    val totalSongsListened: Int,
    val dayStreakSong: DayStreakSongInfo? = null,
)

data class ProfileStatsUiState(
    val monthlyCapsules: List<MonthlySoundCapsule> = emptyList()
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val apiService: ApiService,
    private val userSongDao: UserSongDao,
    private val dailySongPlaysDao: DailySongPlaysDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    private val _statsState = MutableStateFlow(ProfileStatsUiState())
    val statsState: StateFlow<ProfileStatsUiState> = _statsState

    private var cachedProfile: ProfileResponse? = null

    private val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    val currentMonthYear: String = monthYearFormatter.format(LocalDate.now())

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

    fun fetchMonthlySoundCapsules(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val formatter = DateTimeFormatter.ofPattern("MM-yyyy")
            val monthsCapsules = mutableListOf<MonthlySoundCapsule>()
            val dayStreakSong = getSongWithHighestDayStreak(userId)
            for (i in 0..5) {
                val monthDate = LocalDate.now().minusMonths(i.toLong())
                val monthYearStr = formatter.format(monthDate)
                val displayMonthYear = monthYearFormatter.format(monthDate)

                val topSongs = dailySongPlaysDao.getTopSongByTimeListenedForMonth(userId, monthYearStr)
                val topArtists = dailySongPlaysDao.getTopArtistsByTimeListenedForMonth(userId, monthYearStr)
                val totalTime = dailySongPlaysDao.getUserTotalTimeListenedForMonth(userId, monthYearStr) ?: 0L
                val dailyTime = dailySongPlaysDao.getDailyTimeListenedInMonth(userId, monthYearStr)

//                val dayStreakSong = getSongWithHighestDayStreak(userId)
                
                Log.d("ProfileViewModel", "Month: $displayMonthYear, Artists: ${topArtists.size}, Songs: ${topSongs.size}")
                
                if (totalTime > 0) {
                    monthsCapsules.add(
                        MonthlySoundCapsule(
                            month = displayMonthYear,
                            topSong = topSongs.firstOrNull(),
                            topSongs = topSongs,
                            topArtist = topArtists.firstOrNull(),
                            topArtists = topArtists,
                            totalTimeListened = totalTime,
                            dailyTime = dailyTime,
                            totalArtistsListened = topArtists.size,
                            totalSongsListened = topSongs.size,
                            dayStreakSong = dayStreakSong
                        )
                    )
                }
            }

            withContext(Dispatchers.Main) {
                _statsState.value = _statsState.value.copy(
                    monthlyCapsules = monthsCapsules
                )
            }
        }
    }

    fun fetchProfileStats(userId: Int) {
        fetchMonthlySoundCapsules(userId)
    }

    private fun calculateDayStreak(dates: List<LocalDate>): Int {
        if (dates.isEmpty()) return 0
        
        val today = LocalDate.now()
        val sortedDates = dates.distinct().sortedDescending()

        if (sortedDates.first() != today && sortedDates.first() != today.minusDays(1)) {
            return 0
        }
        
        var streak = 1
        var previousDate = sortedDates.first()

        for (date in sortedDates.drop(1)) {
            if (previousDate.minusDays(1) == date) {
                streak++
                previousDate = date
            } else {
                break
            }
        }
        
        return streak
    }

    private suspend fun getSongWithHighestDayStreak(userId: Int): DayStreakSongInfo? {
        val allSongPlays = dailySongPlaysDao.getSongPlayHistory(userId)
        var maxStreak = 0
        var bestSongId = -1
        var bestSongInfo: SongPlayDateInfo? = null

        for (songId in allSongPlays.map { it.songId }.distinct()) {
            val songDates = allSongPlays
                .filter { it.songId == songId && it.timeListened > 0 }
                .map { it.date }
                .distinct()
                .sorted()
                
            if (songDates.isEmpty()) continue
            val streak = calculateDayStreak(songDates)

            if (streak > maxStreak) {
                maxStreak = streak
                bestSongId = songId
            }
        }

        if (bestSongId != -1 && maxStreak > 0) {
            bestSongInfo = userSongDao.getSongById(bestSongId)
        }
        
        return bestSongInfo?.let {
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