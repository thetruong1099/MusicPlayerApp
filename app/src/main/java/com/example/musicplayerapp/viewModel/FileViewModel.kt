package com.example.musicplayerapp.viewModel

import android.content.Context
import androidx.lifecycle.*
import com.example.musicplayerapp.model.Music
import com.example.musicplayerapp.repository.FileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class FileViewModel(context: Context) : ViewModel() {

    private val fileRepository: FileRepository = FileRepository(context)
    private val listAudioMutableLiveData = MutableLiveData<MutableList<Music>>()

    init {
        viewModelScope.launch(Dispatchers.IO) { listAudioMutableLiveData.postValue(fileRepository.getAllAudioFromDevice()) }
    }

    fun getAllAudioFromDevice(): MutableLiveData<MutableList<Music>> = listAudioMutableLiveData

    class FileViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FileViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FileViewModel(context) as T
            }
            throw IllegalArgumentException("unable construct viewModel")
        }
    }
}