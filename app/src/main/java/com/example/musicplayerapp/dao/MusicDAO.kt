package com.example.musicplayerapp.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.musicplayerapp.model.Music

@Dao
interface MusicDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMusic(music: Music)

    @Query("UPDATE music_table SET favorite_col = :status WHERE sp_id_col = :spID")
    suspend fun updateStatusFavorite(status: Boolean, spID: String)

    @Query("delete from music_table")
    suspend fun deleteAllMusic()

    @Query("SELECT * FROM music_table")
    fun getAllMusic(): LiveData<MutableList<Music>>

    @Query("SELECT favorite_col FROM music_table WHERE sp_id_col = :spID")
    fun getStatusFavorite(spID: String): LiveData<Boolean>

    @Query("SELECT * FROM music_table WHERE favorite_col = 1")
    fun getFavoriteMusic(): LiveData<MutableList<Music>>

    @Query("select * from music_table where name_col like :keyword")
    fun searchMusic(keyword: String): LiveData<MutableList<Music>>
}