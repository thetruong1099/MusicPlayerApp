package com.example.musicplayerapp.ui.activity

import android.content.*
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.musicplayerapp.R
import com.example.musicplayerapp.model.Music
import com.example.musicplayerapp.service.MusicService
import com.example.musicplayerapp.service.MusicService.Companion.REPEAT_ALL
import com.example.musicplayerapp.service.MusicService.Companion.REPEAT_ONE
import com.example.musicplayerapp.service.MusicService.Companion.SHUFFLE_ALL
import com.example.musicplayerapp.util.Extension
import com.example.musicplayerapp.viewModel.CurrentDataViewModel
import com.example.musicplayerapp.viewModel.MusicViewModel
import kotlinx.android.synthetic.main.activity_music_player.*
import kotlinx.coroutines.*

class MusicPlayerActivity : AppCompatActivity(), ServiceConnection {

    private val musicViewModel by lazy {
        ViewModelProvider(
            this,
            MusicViewModel.MusicViewModelFactory(this.application)
        )[MusicViewModel::class.java]
    }

    private val localBroadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val actionName = intent?.getStringExtra("action_music")
            if (actionName == "action_music") {
                refreshView()
            }
        }
    }

    private lateinit var music: Music

    private val currentDataViewModel = CurrentDataViewModel.instance

    private lateinit var musicService: MusicService

    private var lavProcess = 0.0F

    private lateinit var job: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_player)

        job = Job()
        job.cancel()

        getDataFromIntent()

        initListenerBtn()

        handleSeekBar()

        updateUiFavorite()


    }

    override fun onStart() {
        super.onStart()
        val serviceIntent = Intent(this, MusicService::class.java)

//        if (currentDataViewModel.currentSongPos==-1){
        ContextCompat.startForegroundService(this, serviceIntent)
//        }

        applicationContext.bindService(serviceIntent, this, Context.BIND_AUTO_CREATE)

        registerLocalBroadcasrReceiver()
    }

    override fun onResume() {
        super.onResume()

        job.cancel()
        handleSeekBarCoroutine()
    }

    override fun onPause() {
        super.onPause()
        applicationContext.unbindService(this)
        job.cancel()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_stationary, R.anim.slide_out_down)
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val myBinder = service as MusicService.MyBinder
        musicService = myBinder.service

        var position = currentDataViewModel.currentSongs.indexOf(music)

        if (position != currentDataViewModel.currentSongPos) {
            musicService.playMusic(position)
        }

        musicService.initListener()

        refreshView()

        val serviceMsg = musicService.messenger
        try {
            val msg = Message.obtain(null, MusicService.MSG_REGISTER_CLIENT)
            msg.replyTo = messenger
            serviceMsg.send(msg)
        } catch (ignore: RemoteException) {
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {

    }

    private val messenger = Messenger(IncomingHandler())

    inner class IncomingHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (msg.what == MusicService.MSG_COMPLETED) {
                refreshView()
            } else super.handleMessage(msg)
        }
    }

    private fun refreshView() {
        if (::musicService.isInitialized) {

            if (musicService.isPlaying) {
                btn_play.setImageResource(R.drawable.baseline_pause_24)
                lav_music_disc.setMinProgress(lavProcess)
                lav_music_disc.playAnimation()
                lav_music_disc.setMinProgress(0.0F)
            } else {
                btn_play.setImageResource(R.drawable.baseline_play_arrow_24)
                lav_music_disc.pauseAnimation()
                lavProcess = lav_music_disc.progress
            }

            seekBar.max = musicService.duration / 1000
            val mCurrentPosition = musicService.currentPosition / 1000
            seekBar.progress = mCurrentPosition
            tv_current_time.text = Extension.formatTime(mCurrentPosition)
            tv_duration_time.text = Extension.formatTime(musicService.duration / 1000)
        }

        music = currentDataViewModel.currentSongs[currentDataViewModel.currentSongPos]

        tv_name_song.text = music.nameSong
        tv_name_song.isSelected = true

        if (music.artistName == null) {
            tv_artist.text = "Unknown Artist"
        } else {
            tv_artist.text = music.artistName
        }

        setUiBtnShuffle()
    }

    private fun setUiBtnShuffle() {
        when (currentDataViewModel.currentShuffle) {
            REPEAT_ONE -> {
                btn_shuffle.setImageResource(R.drawable.baseline_repeat_one_24)
            }
            REPEAT_ALL -> {
                btn_shuffle.setImageResource(R.drawable.baseline_repeat_24)
            }
            SHUFFLE_ALL -> {
                btn_shuffle.setImageResource(R.drawable.baseline_shuffle_24)
            }
        }
    }

    private fun handleSeekBar() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (::musicService.isInitialized && fromUser) {
                    musicService.seekTo(progress * 1000)
                    seekBar!!.progress = progress
                    tv_current_time.text = Extension.formatTime(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })


//        this@MusicPlayerActivity.runOnUiThread(object : Runnable{
//            override fun run() {
//                if (::musicService.isInitialized) {
//                    val currentPosition = musicService.currentPosition / 1000
//                    seekBar.progress = currentPosition
//                    tv_current_time.text = Extension.formatTime(currentPosition)
//                    Log.d("aaaa", "handleSeekBar: $currentPosition")
//                }
//                Handler(Looper.getMainLooper()).postDelayed(this, 1000)
//            }
//        })
    }

    private fun handleSeekBarCoroutine() {
        job = CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                if (::musicService.isInitialized) {
                    val currentPosition = musicService.currentPosition / 1000
                    updateUi(currentPosition)
                }
                delay(1000)
            }
        }
    }

    suspend fun updateUi(process: Int) {
        withContext(Dispatchers.Main) {
            seekBar.progress = process
            tv_current_time.text = Extension.formatTime(process)
        }
    }

    private fun getDataFromIntent() {
        music = intent.getSerializableExtra("music") as Music
    }

    private fun initListenerBtn() {

        btn_back.setOnClickListener {
            onBackPressed()
        }

        btn_play.setOnClickListener { playPauseBtnClicked() }
        btn_previous_song.setOnClickListener { prevBtnClicked() }
        btn_next_song.setOnClickListener { nextBtnClicked() }

        btn_shuffle.setOnClickListener {
            if (currentDataViewModel.currentShuffle == 4) currentDataViewModel.currentShuffle = 1
            else currentDataViewModel.currentShuffle++
            setUiBtnShuffle()
        }

    }

    private fun nextBtnClicked() {
        musicService.nextMusic()
        refreshView()
    }

    private fun prevBtnClicked() {
        musicService.previousMusic()
        refreshView()
    }

    private fun playPauseBtnClicked() {
        if (musicService.isPlaying) {
            btn_play.setImageResource(R.drawable.baseline_play_arrow_24)
            lav_music_disc.pauseAnimation()
            lavProcess = lav_music_disc.progress
        } else {
            btn_play.setImageResource(R.drawable.baseline_pause_24)
            lav_music_disc.setMinProgress(lavProcess)
            lav_music_disc.playAnimation()
            lav_music_disc.setMinProgress(0.0F)
        }
        musicService.playPauseMusic()
    }

    private fun updateUiFavorite() {

        val idFileMusic = music.sp_id

        musicViewModel.getStatusFavorite(idFileMusic).observe(this, Observer {
            it?.let {
                val status = it
                if (status) {
                    btn_favorite.setImageResource(R.drawable.baseline_favorite_24)
                } else btn_favorite.setImageResource(R.drawable.baseline_favorite_border_24)

                btn_favorite.setOnClickListener {
                    if (status) {
                        btn_favorite.setImageResource(R.drawable.baseline_favorite_border_24)
                        musicViewModel.updateStatusFavorite(false, idFileMusic)
                    } else {
                        btn_favorite.setImageResource(R.drawable.baseline_favorite_24)
                        musicViewModel.updateStatusFavorite(true, idFileMusic)
                    }
                }
            }
        })
    }

    private fun registerLocalBroadcasrReceiver() {
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(localBroadCastReceiver, IntentFilter("send_data_to_activity"))
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localBroadCastReceiver)
    }
}