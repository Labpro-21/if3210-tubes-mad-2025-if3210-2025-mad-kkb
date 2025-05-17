package com.kkb.purrytify.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkb.purrytify.TokenStorage
import com.kkb.purrytify.data.dao.LikeDao
import com.kkb.purrytify.data.model.Like
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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

    private var currentUserId: Int? = null

    fun loadLikes(context: Context) {
        val userIdString = TokenStorage.getUserId(context)
        val userId = userIdString?.toIntOrNull() ?: return
        currentUserId = userId
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val likes = likeDao.getLikesByUserId(userId)
                _likes.value = likes
            } catch (e: Exception) {
                Log.e("LikeViewModel", "Failed to load likes: ${e.message}")
            }
        }
    }

    fun likeSong(songId: Int) {
        val userId = currentUserId ?: return
        val like = Like(userId = userId, songId = songId)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                likeDao.insert(like)
                // Update state in memory for instant UI feedback
                _likes.value = _likes.value + like
            } catch (e: Exception) {
                Log.e("LikeViewModel", "Failed to like song: ${e.message}")
            }
        }
    }

    fun unlikeSong(songId: Int) {
        val userId = currentUserId ?: return
        val like = Like(userId, songId)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                likeDao.delete(like)
                // Update state in memory for instant UI feedback
                _likes.value = _likes.value.filterNot { it.songId == songId }
            } catch (e: Exception) {
                Log.e("LikeViewModel", "Failed to unlike song: ${e.message}")
            }
        }
    }

    fun isLiked(songId: Int): Boolean {
        return _likes.value.any { it.songId == songId }
    }
}