package com.kkb.purrytify.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "likes",
    primaryKeys = ["userId", "songId"], // Composite Primary Key
    foreignKeys = [
        ForeignKey(
            entity = Song::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Like(
    val userId: Int,
    val songId: Int
)

