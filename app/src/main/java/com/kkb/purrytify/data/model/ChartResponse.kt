package com.kkb.purrytify.data.model

import com.kkb.purrytify.UserSong
import java.time.LocalDateTime

data class ChartSong(
    val id: Int,
    val title: String,
    val artist: String,
    val artwork: String,
    val url: String,
    val duration: String,
    val country: String,
    val rank: Int,
    val createdAt: String,
    val updatedAt: String
)
fun ChartSong.toUserSong(): UserSong {
    return UserSong(
        userId = 0,
        songId = this.id,
        title = this.title,
        artist = this.artist,
        filePath = this.url,
        coverPath = this.artwork,
        createdAt = LocalDateTime.parse(this.createdAt),
        lastPlayed = null,
        isLiked = false,
    )
}
typealias ChartResponse = List<ChartSong>