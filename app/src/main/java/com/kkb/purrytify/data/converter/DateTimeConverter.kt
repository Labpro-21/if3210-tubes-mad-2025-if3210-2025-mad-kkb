package com.kkb.purrytify.data.converter

import androidx.room.TypeConverter
import java.time.LocalDateTime

class DateTimeConverter {
    @TypeConverter
    fun fromTimestamp(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }

    @TypeConverter
    fun dateTimeToTimestamp(dateTime: LocalDateTime?): String? {
        return dateTime?.toString()
    }
}