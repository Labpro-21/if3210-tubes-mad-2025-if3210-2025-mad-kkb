package com.kkb.purrytify.data.dao

import androidx.room.*
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

    @Delete
    suspend fun delete(usersongs: UserSongs)

}