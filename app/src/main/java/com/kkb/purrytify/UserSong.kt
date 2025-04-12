package com.kkb.purrytify

import java.time.LocalDateTime

data class UserSong(
    val userId: Int,
    val songId: Int,
    val title: String,
    val artist: String,
    val filePath: String,
    val coverPath: String? = null,
    val isLiked: Boolean,
    val createdAt: LocalDateTime,
    val lastPlayed: LocalDateTime?
)
