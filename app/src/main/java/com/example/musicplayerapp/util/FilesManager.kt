package com.example.musicplayerapp.util

import com.example.musicplayerapp.model.Music
import com.example.musicplayerapp.viewModel.MusicViewModel

class FilesManager {

    companion object {
        val instance = FilesManager()
    }

    var spMusicFiles: MutableList<Music> = mutableListOf()
    private var dbMusicFiles: MutableList<Music> = mutableListOf()
    lateinit var musicViewModel: MusicViewModel

    fun viewModelReady(musics: MutableList<Music>) {
        dbMusicFiles = musics
        mergerAll()
    }

    private fun mergerAll() {
        deleteDB()
        for (it in 0 until spMusicFiles.size) {
            var isNew = true
            for (dbMus in dbMusicFiles) {
                if (spMusicFiles[it].sp_id == dbMus.sp_id) {
                    spMusicFiles[it] = dbMus
                    isNew = false
                }
            }
            if (isNew) {
                saveToDB(spMusicFiles[it])
            }
        }
    }

    private fun saveToDB(music: Music) {
        musicViewModel.insertMusic(music)
    }

    private fun deleteDB() {
        musicViewModel.deleteAllMusic()
    }
}