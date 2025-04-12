package com.kkb.purrytify.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "UserSongs",
    foreignKeys = [
        ForeignKey(
            entity = Song::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        )
    ])
data class UserSongs(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val songId: Int,
    val createdAt: LocalDateTime,
    val lastPlayed: LocalDateTime?
)