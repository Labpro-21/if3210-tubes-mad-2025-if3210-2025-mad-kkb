package com.kkb.purrytify.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.kkb.purrytify.data.dao.LikeDao
import com.kkb.purrytify.data.dao.SongDao
import com.kkb.purrytify.data.dao.UserSongDao
import com.kkb.purrytify.data.model.Like
import com.kkb.purrytify.data.model.Song
import com.kkb.purrytify.data.model.UserSongs

@Database(entities = [Song::class, Like::class, UserSongs::class], version = 6, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun likeDao(): LikeDao
    abstract fun userSongDao(): UserSongDao
}