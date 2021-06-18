package com.example.musicplayerapp.application

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class ApplicationClass : Application() {

    companion object {
        const val CHANNEL_ID = "CHANNEL_ID"
        const val ACTION_NEXT = "NEXT"
        const val ACTION_PLAY = "PLAY"
        const val ACTION_PREVIOUS = "PREVIOUS"
    }

    override fun onCreate() {
        super.onCreate()

        createChanelNotification()
    }

    private fun createChanelNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationChannel2 =
                NotificationChannel(CHANNEL_ID, "Channel ID", NotificationManager.IMPORTANCE_HIGH)
            notificationChannel2.description = "Channel 2 Description"
            notificationChannel2.setSound(null, null)

            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java) as NotificationManager
            notificationManager?.let {
                notificationManager.createNotificationChannel(
                    notificationChannel2
                )
            }
        }
    }

}