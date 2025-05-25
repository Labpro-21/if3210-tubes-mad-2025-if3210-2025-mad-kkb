package com.kkb.purrytify.module

import android.content.Context
import androidx.room.Room
import com.kkb.purrytify.data.dao.DailySongPlaysDao
import com.kkb.purrytify.data.database.AppDatabase
import com.kkb.purrytify.data.dao.SongDao
import com.kkb.purrytify.data.dao.LikeDao
import com.kkb.purrytify.data.dao.UserSongDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "purrytify_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideSongDao(appDatabase: AppDatabase): SongDao {
        return appDatabase.songDao()
    }

    @Provides
    @Singleton
    fun provideLikeDao(appDatabase: AppDatabase): LikeDao {
        return appDatabase.likeDao()
    }

    @Provides
    @Singleton
    fun provideUserSongDao(appDatabase: AppDatabase): UserSongDao {
        return appDatabase.userSongDao()
    }

    @Provides
    @Singleton
    fun provideDailySongPlaysDao(appDatabase: AppDatabase): DailySongPlaysDao {
        return appDatabase.dailySongPlaysDao()
    }
}
