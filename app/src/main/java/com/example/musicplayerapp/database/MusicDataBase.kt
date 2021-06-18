package com.example.musicplayerapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.musicplayerapp.dao.MusicDAO
import com.example.musicplayerapp.model.Music

@Database(entities = [Music::class], version = 1)
abstract class MusicDataBase: RoomDatabase() {

    abstract fun getMusicDao(): MusicDAO

    companion object statics {
        @Volatile
        private var instance: MusicDataBase? = null

        fun getInstance(context: Context): MusicDataBase {
            if (instance == null) {
                instance =
                    Room.databaseBuilder(context, MusicDataBase::class.java, "DiaryDataBase")
                        .build()
            }
            return instance!!
        }
    }
}