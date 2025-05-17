package com.kkb.purrytify.data.repository

import com.kkb.purrytify.data.dao.SongDao
import com.kkb.purrytify.data.dao.UserSongDao
import com.kkb.purrytify.data.model.Song
import com.kkb.purrytify.data.model.UserSongs
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepository @Inject constructor(
    private val songDao: SongDao,
    private val userSongDao: UserSongDao
) {
    suspend fun insertSong(song: Song): Long {
        return songDao.insert(song)
    }

    suspend fun insertUserSong(userSong: UserSongs) {
        userSongDao.insert(userSong)
    }

    suspend fun insertSongWithUserSong(song: Song, userSong: UserSongs) {
        songDao.insert(song)
        userSongDao.insert(userSong)
    }

    suspend fun getAllSongs(): List<Song> {
        return songDao.getAllSongs()
    }

    suspend fun getUserSongsByUserId(userId: Int): List<UserSongs> {
        return userSongDao.getUserSongsByUserId(userId)
    }

    suspend fun deleteSongById(songId: Int) {
        songDao.deleteById(songId)
    }

    suspend fun updateIsLiked(userId: Int, songId: Int, isLiked: Boolean) {
        userSongDao.updateIsLiked(userId, songId, isLiked)
    }

    suspend fun updateLastPlayed(userId: Int, songId: Int, lastPlayed: java.time.LocalDateTime) {
        userSongDao.updateLastPlayed(userId, songId, lastPlayed)
    }
}