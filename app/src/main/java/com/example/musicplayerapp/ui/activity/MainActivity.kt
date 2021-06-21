package com.example.musicplayerapp.ui.activity

import android.content.*
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.musicplayerapp.R
import com.example.musicplayerapp.service.MusicService
import com.example.musicplayerapp.util.Extension
import com.example.musicplayerapp.util.FilesManager
import com.example.musicplayerapp.viewModel.CurrentDataViewModel
import com.example.musicplayerapp.viewModel.MusicViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_music_player.*
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity(), ServiceConnection {

    private val fileManager = FilesManager.instance

    private val currentDataViewModel = CurrentDataViewModel.instance

    private lateinit var musicService: MusicService

    private val musicViewModel by lazy {
        ViewModelProvider(
            this,
            MusicViewModel.MusicViewModelFactory(this.application)
        )[MusicViewModel::class.java]
    }

    private var lavProcess: Float = 0.0F

    private val localBroadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val actionName = intent?.getStringExtra("action_music")
            if (actionName == "action_music") {
                refreshView()
            }
        }
    }

    private lateinit var job: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fileManager.musicViewModel = musicViewModel

        musicViewModel.getAllMusic().observe(this, Observer {
            fileManager.viewModelReady(it)
        })

        job = Job()
        job.cancel()

    }

    override fun onStart() {
        super.onStart()

        if (currentDataViewModel.currentSongPos != -1 && currentDataViewModel.currentSongs.size != 0) {
            val intent = Intent(this, MusicService::class.java)
            ContextCompat.startForegroundService(this, intent)

            applicationContext.bindService(intent, this, Context.BIND_AUTO_CREATE)

            initListenerBtn()

            registerLocalBroadcastReceiver()

            startMediaPlayerActivity()

            refreshView()
        }
    }

    override fun onResume() {
        super.onResume()

        job.cancel()
        handleProcessBar()
    }

    override fun onPause() {
        super.onPause()
        job.cancel()
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val myBinder = service as MusicService.MyBinder
        musicService = myBinder.service
        refreshView()

        musicService.initListener()
        val serviceMsg = musicService.messenger
        try {
            val msg = Message.obtain(null, MusicService.MSG_REGISTER_CLIENT)
            msg.replyTo = messenger
            serviceMsg.send(msg)
        } catch (ignore: RemoteException) {
        }
    }

    private val messenger = Messenger(IncomingHandler())

    inner class IncomingHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (msg.what == MusicService.MSG_COMPLETED) {
                refreshView()
            } else super.handleMessage(msg)
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {

    }

    private fun refreshView() {
        if (currentDataViewModel.currentSongPos != -1) {
            val music = currentDataViewModel.currentSongs[currentDataViewModel.currentSongPos]

            if (::musicService.isInitialized) {
                if (musicService.isPlaying) {
                    btn_play_main.setImageResource(R.drawable.baseline_pause_24)
                    lav_music_disc_main.setMinProgress(lavProcess)
                    lav_music_disc_main.playAnimation()
                    lav_music_disc_main.setMinProgress(0.0F)
                } else {
                    btn_play_main.setImageResource(R.drawable.baseline_play_arrow_24)
                    lav_music_disc_main.pauseAnimation()
                    lavProcess = lav_music_disc_main.progress
                }

                progress_bar.max = musicService.duration / 1000
                progress_bar.progress = musicService.currentPosition / 1000
            }

            tv_name_song_main.text = music.nameSong
            if (music.artistName == null && music.album == null) {
                tv_name_singer_main.text = "Unknown Artist | Unknown Album"

            } else if (music.artistName == null && music.album != null) {
                tv_name_singer_main.text = "Unknown Artist | ${music.album}"
            } else if (music.artistName != null && music.album == null) {
                tv_name_singer_main.text = "${music.artistName} | ${music.album}"
            }

        } else {
            tv_name_song_main.text = "Chưa có bài được chọn"
        }

    }

    private fun initListenerBtn() {

        btn_play_main.setOnClickListener {
            if (musicService.isPlaying) {
                btn_play_main.setImageResource(R.drawable.baseline_play_arrow_24)
                lav_music_disc_main.pauseAnimation()
                lavProcess = lav_music_disc_main.progress
            } else {
                btn_play_main.setImageResource(R.drawable.baseline_pause_24)
                lav_music_disc_main.setMinProgress(lavProcess)
                lav_music_disc_main.playAnimation()
                lav_music_disc_main.setMinProgress(0.0F)
            }
            musicService.playPauseMusic()
        }

        btn_next_main.setOnClickListener {
            musicService.nextMusic()
            refreshView()
        }
    }

    private fun registerLocalBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(localBroadCastReceiver, IntentFilter("send_data_to_activity"))
    }

    private fun startMediaPlayerActivity() {
        layout_my_song.setOnClickListener {
            val music = currentDataViewModel.currentSongs[currentDataViewModel.currentSongPos]
            val intent = Intent(this, MusicPlayerActivity::class.java)
            intent.putExtra("music", music)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_stationary)
        }
    }

    private fun handleProcessBar() {
        job = CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                if (::musicService.isInitialized) {
                    updateUIProcessBar(musicService.currentPosition, musicService.duration)
                }
                delay(1000)
            }
        }
    }

    suspend fun updateUIProcessBar(position: Int, positionMax: Int) {
        withContext(Dispatchers.Main) {
            progress_bar.max = positionMax / 1000
            progress_bar.progress = position / 1000
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localBroadCastReceiver)
    }
}