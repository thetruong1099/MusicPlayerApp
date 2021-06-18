package com.example.musicplayerapp.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.musicplayerapp.model.Music
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class CurrentDataViewModel() {

    companion object{
        val instance = CurrentDataViewModel()
    }

    var currentSongs:MutableList<Music> = mutableListOf()
    var currentSongPos: Int = -1
    var currentShuffle:Int = 2

}