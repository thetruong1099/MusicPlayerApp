package com.example.musicplayerapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.musicplayerapp.application.ApplicationClass.Companion.ACTION_NEXT
import com.example.musicplayerapp.application.ApplicationClass.Companion.ACTION_PLAY
import com.example.musicplayerapp.application.ApplicationClass.Companion.ACTION_PREVIOUS
import com.example.musicplayerapp.service.MusicService

class MusicReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val actionName = intent?.action
        val serviceIntent = Intent(context, MusicService::class.java)

        if (actionName != null) {
            when (actionName) {
                ACTION_PLAY -> {
                    serviceIntent.putExtra("ActionName", "playPause")
                    context?.startService(serviceIntent)
                }
                ACTION_NEXT -> {
                    serviceIntent.putExtra("ActionName", "next")
                    context?.startService(serviceIntent)
                }
                ACTION_PREVIOUS -> {
                    serviceIntent.putExtra("ActionName", "previous")
                    context?.startService(serviceIntent)
                }
            }
        }
    }
}