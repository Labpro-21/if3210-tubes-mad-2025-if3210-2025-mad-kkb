package com.kkb.purrytify.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kkb.purrytify.util.MediaPlayerManager

class MediaControlReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_PLAY = "com.kkb.purrytify.PLAY"
        const val ACTION_PAUSE = "com.kkb.purrytify.PAUSE"
        const val ACTION_PREVIOUS = "com.kkb.purrytify.PREVIOUS"
        const val ACTION_NEXT = "com.kkb.purrytify.NEXT"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_PLAY -> MediaPlayerManager.resume(context)
            ACTION_PAUSE -> MediaPlayerManager.pause(context)
            ACTION_PREVIOUS -> MediaPlayerManager.previous(context)
            ACTION_NEXT -> MediaPlayerManager.next(context)
        }
    }
}