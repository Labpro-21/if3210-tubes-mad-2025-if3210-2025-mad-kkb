package com.kkb.purrytify.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kkb.purrytify.data.model.DailySongPlays
import com.kkb.purrytify.data.model.UserSongs
import java.time.LocalDate
import java.time.LocalDateTime

@Dao
interface DailySongPlaysDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dailySongPlay: DailySongPlays)

    @Query("SELECT * FROM DailySongPlays WHERE userId = :userId AND date = :date")
    suspend fun getUserSongsByUserIdDate(userId: Int, date: LocalDate): List<DailySongPlays>

    @Query("UPDATE DailySongPlays SET timeListened = :timeListened WHERE userId = :userId AND songId = :songId")
    suspend fun updateTimeListened(userId: Int, songId: Int, timeListened: Long)

    @Query("""
        SELECT s.id, s.title, s.artist, s.filePath, s.coverPath, SUM(d.timeListened) as timeListened
        FROM DailySongPlays d
        INNER JOIN songs s ON d.songId = s.id
        WHERE d.userId = :userId 
        AND strftime('%m-%Y', d.date) = :monthYear
        AND d.timeListened > 0
        GROUP BY s.id
        ORDER BY timeListened DESC
    """)
    suspend fun getTopSongByTimeListenedForMonth(userId: Int, monthYear: String): List<TopSongTimeListened>

    @Query("""
        SELECT s.artist, s.coverPath, SUM(d.timeListened) as totalTime
        FROM DailySongPlays d
        INNER JOIN songs s ON d.songId = s.id
        WHERE d.userId = :userId 
        AND strftime('%m-%Y', d.date) = :monthYear
        AND d.timeListened > 0
        GROUP BY s.artist
        ORDER BY totalTime DESC
    """)
    suspend fun getTopArtistsByTimeListenedForMonth(userId: Int, monthYear: String): List<TopArtistTimeListened>

    @Query("""
        SELECT SUM(timeListened) 
        FROM DailySongPlays
        WHERE userId = :userId 
        AND strftime('%m-%Y', date) = :monthYear
    """)
    suspend fun getUserTotalTimeListenedForMonth(userId: Int, monthYear: String): Long?

    @Query("""
        SELECT *
        FROM DailySongPlays
        WHERE userId = :userId
        ORDER BY date DESC
    """)
        suspend fun getSongPlayHistory(userId: Int): List<DailySongPlays>

    @Query("""
        SELECT strftime('%d', date) as day, SUM(timeListened) as totalTimeListened
        FROM DailySongPlays
        WHERE userId = :userId 
        AND strftime('%m-%Y', date) = :monthYear
        GROUP BY date
        ORDER BY date ASC
    """)
    suspend fun getDailyTimeListenedInMonth(userId: Int, monthYear: String): List<DailyTimeListened>
}

data class TopSongTimeListened(
    val id: Int,
    val title: String,
    val artist: String,
    val filePath: String,
    val coverPath: String?,
    val timeListened: Long
)

data class TopArtistTimeListened(
    val artist: String,
    val coverPath: String?,
    val totalTime: Long
)

data class SongPlayDateInfo(
    val id: Int,
    val title: String,
    val artist: String,
    val filePath: String,
    val coverPath: String?,
    val lastPlayed: java.time.LocalDateTime?
)

data class DailyTimeListened(
    val day: String,
    val totalTimeListened: Long
)