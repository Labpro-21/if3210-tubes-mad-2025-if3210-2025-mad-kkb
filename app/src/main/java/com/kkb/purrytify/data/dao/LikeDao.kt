package com.kkb.purrytify.data.dao

import androidx.room.*
import com.kkb.purrytify.data.model.Like

@Dao
interface LikeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(like: Like)

    @Query("SELECT * FROM likes")
    suspend fun getAllSongs(): List<Like>

    @Delete
    suspend fun delete(like: Like)

    @Query("SELECT * FROM likes WHERE userId = :userId")
    suspend fun getLikesByUserId(userId: Int): List<Like>
}