package com.kkb.purrytify.data.dao

import androidx.room.*
import com.kkb.purrytify.UserSong
import com.kkb.purrytify.data.model.UserSongs
import java.time.LocalDateTime

@Dao
interface UserSongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(usersongs: UserSongs)

    @Query("SELECT * FROM UserSongs WHERE userId = :userId")
    suspend fun getUserSongsByUserId(userId: Int): List<UserSongs>

    @Query("SELECT * FROM UserSongs")
    suspend fun getAllSongs(): List<UserSongs>

    @Query("UPDATE UserSongs SET isLiked = :isLiked WHERE userId = :userId AND songId = :songId")
    suspend fun updateIsLiked(userId: Int, songId: Int, isLiked: Boolean)

    @Query("UPDATE UserSongs SET lastPlayed = :lastPlayed WHERE userId = :userId AND songId = :songId")
    suspend fun updateLastPlayed(userId: Int, songId: Int, lastPlayed: LocalDateTime)

    @Query("UPDATE UserSongs SET timeListened = :timeListened WHERE userId = :userId AND songId = :songId")
    suspend fun updateTimeListened(userId: Int, songId: Int, timeListened: Long)
    
    @Query("""
        SELECT s.id, s.title, s.artist, s.filePath, s.coverPath, u.timeListened
        FROM UserSongs u
        INNER JOIN songs s ON u.songId = s.id
        WHERE u.userId = :userId
        ORDER BY u.timeListened DESC
        LIMIT 1
    """)
    suspend fun getTopSongByTimeListened(userId: Int): TopSongTimeListened?

    @Query("""
        SELECT s.artist, SUM(u.timeListened) as totalTime
        FROM UserSongs u
        INNER JOIN songs s ON u.songId = s.id
        WHERE u.userId = :userId
        GROUP BY s.artist
        ORDER BY totalTime DESC
        LIMIT :limit
    """)
    suspend fun getTopArtistsByTimeListened(userId: Int, limit: Int = 1): List<TopArtistTimeListened>

    @Query("SELECT SUM(timeListened) FROM UserSongs WHERE userId = :userId")
    suspend fun getUserTotalTimeListened(userId: Int): Long?

    @Query("""
        SELECT s.id, s.title, s.artist, s.filePath, s.coverPath, u.lastPlayed
        FROM UserSongs u
        INNER JOIN songs s ON u.songId = s.id
        WHERE u.userId = :userId
    """)
    suspend fun getSongsWithPlayDates(userId: Int): List<SongPlayDateInfo>

    @Delete
    suspend fun delete(usersongs: UserSongs)

}

data class TopArtistTimeListened(
    val artist: String,
    val totalTime: Long
)

data class TopSongTimeListened(
    val id: Int,
    val title: String,
    val artist: String,
    val filePath: String,
    val coverPath: String?,
    val timeListened: Long
)

data class SongPlayDateInfo(
    val id: Int,
    val title: String,
    val artist: String,
    val filePath: String,
    val coverPath: String?,
    val lastPlayed: java.time.LocalDateTime?
)