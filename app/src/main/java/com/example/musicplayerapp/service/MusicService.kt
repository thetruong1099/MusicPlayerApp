package com.example.musicplayerapp.service

import com.example.musicplayerapp.R
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.*
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.musicplayerapp.application.ApplicationClass
import com.example.musicplayerapp.application.ApplicationClass.Companion.CHANNEL_ID
import com.example.musicplayerapp.model.Music
import com.example.musicplayerapp.receiver.MusicReceiver
import com.example.musicplayerapp.viewModel.CurrentDataViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.random.Random

class MusicService : Service() {

    companion object {
        const val REPEAT_ONE = 1
        const val REPEAT_ALL = 2
        const val SHUFFLE_ALL = 3
        const val MSG_REGISTER_CLIENT = 0
        const val MSG_COMPLETED = 1
    }

    private var mBinder: IBinder = MyBinder()

    private lateinit var mediaPlayer: MediaPlayer

    private val currentDataViewModel = CurrentDataViewModel.instance

    private val currentSongsList: MutableList<Music> get() = currentDataViewModel.currentSongs
    private val currentPos: Int get() = currentDataViewModel.currentSongPos
    private val currentShuffle: Int get() = currentDataViewModel.currentShuffle

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var mediaSessionCompat: MediaSessionCompat

    val isPlaying get() = mediaPlayer.isPlaying
    val duration get() = mediaPlayer.duration
    val currentPosition get() = mediaPlayer.currentPosition

    inner class MyBinder : Binder() {
        val service: MusicService get() = this@MusicService
    }

    override fun onCreate() {
        super.onCreate()
        mediaSessionCompat = MediaSessionCompat(baseContext, "My Audio")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val actionName = intent?.getStringExtra("ActionName")
        if (actionName != null) {
            when (actionName) {
                "playPause" -> {
                    playPauseMusic()
                    sendActionToActivity()
                }
                "next" -> {
                    nextMusic()
                    sendActionToActivity()
                }
                "previous" -> {
                    previousMusic()
                    sendActionToActivity()
                }
            }
        }


        return START_STICKY
    }


    fun playMusic(position: Int) {
        mediaPlayer = MediaPlayer()

        if (::mediaPlayer.isInitialized) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }

        createMediaPlayer(position)
        mediaPlayer.start()
        startNotification()
    }

    private fun createMediaPlayer(positionInner: Int) {

        if (positionInner < currentSongsList.size) {
            currentDataViewModel.currentSongPos = positionInner
            val uri = currentSongsList[positionInner].uri
            mediaPlayer = MediaPlayer()
            mediaPlayer.apply {
                setDataSource(applicationContext, uri.toUri())
                prepare()
            }
        }
    }


    fun playPauseMusic() {
        if (isPlaying) {
            mediaPlayer.pause()
            updateNotification()
        } else {
            mediaPlayer.start()
            updateNotification()
        }
    }

    fun nextMusic() {
        mediaPlayer.stop()
        mediaPlayer.release()

        when (currentShuffle) {
            REPEAT_ALL, REPEAT_ONE -> currentDataViewModel.currentSongPos =
                ((currentDataViewModel.currentSongPos + 1) % currentSongsList.size)
            SHUFFLE_ALL -> currentDataViewModel.currentSongPos =
                Random.nextInt(0, currentSongsList.size - 1)
        }
        createMediaPlayer(currentPos)
        initListener()
        mediaPlayer.start()
        updateNotification()
    }

    fun previousMusic() {
        mediaPlayer.stop()
        mediaPlayer.release()
        when (currentShuffle) {
            REPEAT_ALL, REPEAT_ONE -> currentDataViewModel.currentSongPos =
                if (currentDataViewModel.currentSongPos == 0) currentSongsList.size - 1
                else currentDataViewModel.currentSongPos - 1
            SHUFFLE_ALL -> currentDataViewModel.currentSongPos =
                Random.nextInt(0, currentSongsList.size - 1)
        }

        createMediaPlayer(currentPos)
        initListener()
        mediaPlayer.start()
        updateNotification()
    }

    fun seekTo(position: Int) {
        mediaPlayer.seekTo(position)
    }

    fun initListener() {
        mediaPlayer.setOnCompletionListener {
            mediaPlayer.stop()
            mediaPlayer.release()

            when (currentDataViewModel.currentShuffle) {
                REPEAT_ALL -> currentDataViewModel.currentSongPos =
                    ((currentDataViewModel.currentSongPos + 1) % currentSongsList.size)
                SHUFFLE_ALL -> currentDataViewModel.currentSongPos =
                    Random.nextInt(0, currentSongsList.size - 1)
            }

            createMediaPlayer(currentPos)
            initListener()
            mediaPlayer.start()
            updateNotification()
            sentMsg(MSG_COMPLETED)
        }
    }

    private fun sentMsg(msg: Int) {
        for (client in clients) {
            try {
                client.send(Message.obtain(null, msg))
            } catch (e: RemoteException) {
                clients.remove(client)
            }
        }
    }

    val messenger = Messenger(IncomingHandler())
    val clients = ArrayList<Messenger>()

    inner class IncomingHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (msg.what == MSG_REGISTER_CLIENT) {
                clients.add(msg.replyTo)
            } else super.handleMessage(msg)
        }
    }

    private fun showNotification(): Notification {
        val prevIntent =
            Intent(this, MusicReceiver::class.java).setAction(ApplicationClass.ACTION_PREVIOUS)
        val prevPending =
            PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val pauseIntent =
            Intent(this, MusicReceiver::class.java).setAction(ApplicationClass.ACTION_PLAY)
        val pausePending =
            PendingIntent.getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val nextIntent =
            Intent(this, MusicReceiver::class.java).setAction(ApplicationClass.ACTION_NEXT)
        val nextPending =
            PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.note_music)

        val artistName = if (currentSongsList[currentPos].artistName == null) {
            "Unknown Artist"
        } else {
            currentSongsList[currentPos].artistName
        }

        notificationBuilder =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_music_note_24)
                .setLargeIcon(bitmap)
                .setContentTitle(currentSongsList[currentPos].nameSong)
                .setContentText(artistName)
                .setOnlyAlertOnce(true)
                .setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                        .setMediaSession(mediaSessionCompat.sessionToken)
                )
                .setPriority(NotificationCompat.PRIORITY_LOW)

        if (isPlaying) {
            notificationBuilder
                .addAction(R.drawable.baseline_skip_previous_24, "Previous", prevPending)
                .addAction(R.drawable.baseline_pause_24, "Pause", pausePending)
                .addAction(R.drawable.baseline_skip_next_24, "Next", nextPending)
        } else {
            notificationBuilder
                .addAction(R.drawable.baseline_skip_previous_24, "Previous", prevPending)
                .addAction(R.drawable.baseline_play_arrow_24, "Pause", pausePending)
                .addAction(R.drawable.baseline_skip_next_24, "Next", nextPending)
        }

        return notificationBuilder.build()
    }

    private fun startNotification() {
        startForeground(1, showNotification())
    }

    private fun updateNotification() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, showNotification())
    }

    private fun sendActionToActivity() {
        val intent = Intent("send_data_to_activity")
        intent.putExtra("action_music", "action_music")
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("aaaa", "onDestroy: ")
    }
}