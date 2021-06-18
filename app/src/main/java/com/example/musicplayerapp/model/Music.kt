package com.example.musicplayerapp.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "music_table")
data class Music(
    @ColumnInfo(name = "sp_id_col") var sp_id: String,
    @ColumnInfo(name = "name_col") var nameSong: String,
    @ColumnInfo(name = "artist_col") var artistName: String?,
    @ColumnInfo(name = "album_col") var album: String?,
    @ColumnInfo(name = "uri_col") var uri: String,
    @ColumnInfo(name = "favorite_col") var favorite:Boolean
): Serializable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_col")
    var id: Int = 0
}