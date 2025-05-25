package com.kkb.purrytify.data.repository

import android.util.Log
import com.kkb.purrytify.data.dao.DailySongPlaysDao
import com.kkb.purrytify.data.dao.SongDao
import com.kkb.purrytify.data.dao.UserSongDao
import com.kkb.purrytify.data.model.DailySongPlays
import com.kkb.purrytify.data.model.Song
import com.kkb.purrytify.data.model.UserSongs
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepository @Inject constructor(
    private val songDao: SongDao,
    private val userSongDao: UserSongDao,
    private val dailySongPlaysDao: DailySongPlaysDao
) {
    suspend fun insertSongWithUserSong(song: Song, userSong: UserSongs) {
        songDao.insert(song)
        userSongDao.insert(userSong)
    }

    suspend fun updateLastPlayed(songId: Int, userId: Int, lastPlayed: LocalDateTime) {
        userSongDao.updateLastPlayed(userId, songId, lastPlayed)
    }

    suspend fun updateTimeListened(songId: Int, userId: Int, delta: Long) {
        try {
            val userSongs = userSongDao.getUserSongsByUserId(userId)
            val songExists = userSongs.any { it.songId == songId }

            if (!songExists) {
                Log.w("SongRepository", "Cannot update time listened: Song $songId not found for user $userId")
                return
            }

            val dailySongPlays = dailySongPlaysDao.getUserSongsByUserIdDate(userId, LocalDate.now())
            val dailySongPlay = dailySongPlays.find { it.songId == songId }

            if (dailySongPlay != null) {
                val newTimeListened = dailySongPlay.timeListened + delta
                dailySongPlaysDao.updateTimeListened(userId, songId, newTimeListened)
            }else{
                Log.d("SongRepository","Song Not Found")
                val newDailySongPlay = DailySongPlays(
                    userId = userId,
                    songId = songId,
                    date = LocalDate.now(),
                    timeListened = delta
                )
                dailySongPlaysDao.insert(newDailySongPlay)
                Log.d("SongRepository","Song Created")
            }
        } catch (e: Exception) {
            Log.e("SongRepository", "Error updating time listened: ${e.message}", e)
        }
    }

    suspend fun getUserSongsByUserId(userId: Int) : List<UserSongs> {
        return userSongDao.getUserSongsByUserId(userId)
    }
}