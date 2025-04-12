package com.kkb.purrytify.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkb.purrytify.TokenStorage
import com.kkb.purrytify.data.dao.LikeDao
import com.kkb.purrytify.data.model.Like
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LikeViewModel @Inject constructor(
    private val likeDao: LikeDao
) : ViewModel() {

    private val _likes = MutableStateFlow<List<Like>>(emptyList())
    val likes: StateFlow<List<Like>> = _likes.asStateFlow()

    fun loadLikes(context: Context) {
        val userIdString = TokenStorage.getUserId(context)
        userIdString?.toIntOrNull()?.let { userId ->
            viewModelScope.launch {
                _likes.value = likeDao.getLikesByUserId(userId)
            }
        }
    }

    fun likeSong(context: Context, songId: Int) {
        val userIdString = TokenStorage.getUserId(context)
        userIdString?.toIntOrNull()?.let { userId ->
            val like = Like(userId = userId, songId = songId)
            viewModelScope.launch {
                likeDao.insert(like)
                loadLikes(context)
            }
        } ?: run {
            Log.e("SongViewModel", "User ID is null or invalid")
        }
    }

    fun unlikeSong(context: Context, songId: Int) {
        val userIdString = TokenStorage.getUserId(context)
        userIdString?.toIntOrNull()?.let { userId ->
            viewModelScope.launch {
                likeDao.delete(Like(userId, songId))
                loadLikes(context)
            }
        } ?: run {
            Log.e("SongViewModel", "User ID is null or invalid")
        }
    }

    fun isLiked(songId: Int): Boolean {
        return _likes.value.any { it.songId == songId }
    }
}
