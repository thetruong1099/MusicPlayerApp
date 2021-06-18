package com.example.musicplayerapp.viewModel

import android.app.Application
import androidx.lifecycle.*
import com.example.musicplayerapp.model.Music
import com.example.musicplayerapp.repository.MusicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class MusicViewModel(application: Application) : ViewModel() {

    private val musicRepository = MusicRepository(application)

    fun insertMusic(music: Music) =
        viewModelScope.launch(Dispatchers.IO) { musicRepository.insertMusic(music) }

    fun updateStatusFavorite(status: Boolean, spID: String) =
        viewModelScope.launch(Dispatchers.IO) { musicRepository.updateStatusFavorite(status, spID) }

    fun getAllMusic(): LiveData<MutableList<Music>> = musicRepository.getAllMusic()

    fun getStatusFavorite(spID: String): LiveData<Boolean> = musicRepository.getStatusFavorite(spID)

    fun getFavoriteMusic(): LiveData<MutableList<Music>> = musicRepository.getFavoriteMusic()

    fun searchMusic(keyword: String): LiveData<MutableList<Music>> =
        musicRepository.searchMusic(keyword)

    class MusicViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {

            if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MusicViewModel(application) as T
            }

            throw IllegalArgumentException("unable construct viewModel")
        }

    }
}