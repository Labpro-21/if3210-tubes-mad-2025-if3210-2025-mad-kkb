package com.kkb.purrytify.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kkb.purrytify.data.dao.SongDao
import com.kkb.purrytify.data.model.Song

@Database(entities = [Song::class], version = 4)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
}