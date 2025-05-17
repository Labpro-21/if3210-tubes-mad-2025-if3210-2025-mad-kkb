package com.kkb.purrytify.data.model

data class ProfileResponse(
    val id: Int,
    val username: String,
    val email: String,
    val location: String,
    val profilePhoto: String,
    val createdAt: String,
    val updatedAt: String
)