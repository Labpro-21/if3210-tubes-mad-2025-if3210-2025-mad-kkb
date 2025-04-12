package com.kkb.purrytify.data.dao

import androidx.room.*
import com.kkb.purrytify.data.model.Song

@Dao
interface SongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(song: Song): Long

    @Query("SELECT * FROM songs")
    suspend fun getAllSongs(): List<Song>

    @Delete
    suspend fun delete(song: Song)

    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun deleteById(id: Int)
}