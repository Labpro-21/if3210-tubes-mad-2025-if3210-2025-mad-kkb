package com.kkb.purrytify.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "DailySongPlays",
    foreignKeys = [
        ForeignKey(
            entity = Song::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
)
data class DailySongPlays(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val songId: Int,
    val date: LocalDate,
    val timeListened: Long = 0L
)