package com.kkb.purrytify.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import com.kkb.purrytify.MainActivity
import com.kkb.purrytify.R
import com.kkb.purrytify.UserSong
import com.kkb.purrytify.receivers.MediaControlReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object NotificationUtil {
    private const val CHANNEL_ID = "purrytify_music_channel"
    private const val NOTIFICATION_ID = 1

    private const val ACTION_PLAY = "com.kkb.purrytify.PLAY"
    private const val ACTION_PAUSE = "com.kkb.purrytify.PAUSE"
    private const val ACTION_PREV = "com.kkb.purrytify.PREVIOUS"
    private const val ACTION_NEXT = "com.kkb.purrytify.NEXT"

    private fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true // Permission automatically granted on older Android versions
        }
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notification for music playback controls"
                setShowBadge(false)
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showMusicNotification(context: Context, song: UserSong, isPlaying: Boolean) {
        // Don't proceed if we don't have permission
        if (!hasNotificationPermission(context)) {
            return
        }

        createNotificationChannel(context)

        // Create pending intents for notification actions
        val contentIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIntent = PendingIntent.getBroadcast(
            context, 1,
            Intent(if (isPlaying) ACTION_PAUSE else ACTION_PLAY).apply {
                component = ComponentName(context, MediaControlReceiver::class.java)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevIntent = PendingIntent.getBroadcast(
            context, 2,
            Intent(ACTION_PREV).apply {
                component = ComponentName(context, MediaControlReceiver::class.java)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent = PendingIntent.getBroadcast(
            context, 3,
            Intent(ACTION_NEXT).apply {
                component = ComponentName(context, MediaControlReceiver::class.java)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle(song.title)
            .setContentText(song.artist)
            .setContentIntent(contentIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .addAction(R.drawable.ic_skip_previous, "Previous", prevIntent)
            .addAction(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                if (isPlaying) "Pause" else "Play",
                playPauseIntent
            )
            .addAction(R.drawable.ic_skip_next, "Next", nextIntent)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2))

        // Show notification immediately, artwork will be added when loaded
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
        } catch (e: SecurityException) {
            // Permission denied, can't show notification
        }

        // Load album artwork
        CoroutineScope(Dispatchers.IO).launch {
            val albumArt = loadAlbumArt(context, song.coverPath)
            albumArt?.let {
                builder.setLargeIcon(it)
                // Update notification with album art safely
                try {
                    if (hasNotificationPermission(context)) {
                        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
                    }
                } catch (e: SecurityException) {
                    // Permission denied, can't show notification
                }
            }
        }
    }

    private suspend fun loadAlbumArt(context: Context, coverPath: String?): Bitmap? {
        return try {
            val request = ImageRequest.Builder(context)
                .data(coverPath)
                .allowHardware(false)
                .build()

            val result = ImageLoader(context).execute(request)
            result.drawable?.toBitmap()
        } catch (e: Exception) {
            null
        }
    }

    fun cancelMusicNotification(context: Context) {
        try {
            if (hasNotificationPermission(context)) {
                NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
            }
        } catch (e: SecurityException) {
            // Permission denied, can't cancel notification
        }
    }

    fun updateNotificationPlayState(context: Context, song: UserSong, isPlaying: Boolean) {
        showMusicNotification(context, song, isPlaying)
    }
}