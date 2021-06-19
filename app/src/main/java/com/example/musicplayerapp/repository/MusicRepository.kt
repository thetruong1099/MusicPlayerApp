package com.example.musicplayerapp.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.musicplayerapp.dao.MusicDAO
import com.example.musicplayerapp.database.MusicDataBase
import com.example.musicplayerapp.model.Music
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class MusicRepository(app: Application) {
    private val musicDAO: MusicDAO

    init {
        val musicDatabase: MusicDataBase = MusicDataBase.getInstance(app)
        musicDAO = musicDatabase.getMusicDao()
    }

    suspend fun insertMusic(music: Music) = musicDAO.insertMusic(music)

    suspend fun updateStatusFavorite(status: Boolean, spID: String) =
        musicDAO.updateStatusFavorite(status, spID)

    suspend fun deleteAllMusic() = musicDAO.deleteAllMusic()

    fun getAllMusic(): LiveData<MutableList<Music>> = musicDAO.getAllMusic()

    fun getStatusFavorite(spID: String): LiveData<Boolean> = musicDAO.getStatusFavorite(spID)

    fun getFavoriteMusic(): LiveData<MutableList<Music>> = musicDAO.getFavoriteMusic()

    fun searchMusic(keyword: String): LiveData<MutableList<Music>> = runBlocking(Dispatchers.IO) {
        delay(1000)
        musicDAO.searchMusic(keyword)
    }
}