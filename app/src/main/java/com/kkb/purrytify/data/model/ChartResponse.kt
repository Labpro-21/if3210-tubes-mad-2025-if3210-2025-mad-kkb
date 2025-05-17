package com.kkb.purrytify.data.model

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

typealias ChartResponse = List<ChartSong>