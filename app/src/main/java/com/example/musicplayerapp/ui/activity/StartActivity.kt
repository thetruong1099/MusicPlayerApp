package com.example.musicplayerapp.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.musicplayerapp.R
import com.example.musicplayerapp.util.FilesManager
import com.example.musicplayerapp.viewModel.FileViewModel
import com.example.musicplayerapp.viewModel.MusicViewModel
import kotlinx.coroutines.*

class StartActivity : AppCompatActivity() {

    private val fileManager = FilesManager.instance

    private val fileViewModel by lazy {
        ViewModelProvider(
            this,
            FileViewModel.FileViewModelFactory(this)
        )[FileViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        requestPermission()

    }

    private val requestPermissionResultReadFile = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            getAllFile()
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            getAllFile()
            return
        }
        val result = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE

        )
        if (result == PackageManager.PERMISSION_GRANTED) {
            getAllFile()
        } else {
            requestPermissionResultReadFile.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun getAllFile() {

        fileViewModel.getAllAudioFromDevice().observe(this, Observer {
            it?.let {
                fileManager.spMusicFiles = it
            }
        })

        startMainActivity()
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            cancel()
            finish()
        }
    }

}