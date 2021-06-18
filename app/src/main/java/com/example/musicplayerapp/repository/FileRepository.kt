package com.example.musicplayerapp.repository

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.example.musicplayerapp.model.Music
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileRepository(val context: Context)  {

    suspend fun getAllAudioFromDevice():MutableList<Music>{
        return withContext(Dispatchers.IO){
            var musicList: MutableList<Music> = mutableListOf()
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null,
            )?.use { cursor ->
                if (cursor!=null){
                    while (cursor.moveToNext()) {
                        val id =
                            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
                        val contentUri: Uri =
                            Uri.parse(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString() + File.separator + id)

                        val musicFiles = Music(
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)),
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
                            contentUri.toString(),
                            false
                        )
                        musicList.add(musicFiles)
                    }
                }
            }
            return@withContext musicList
        }
    }
}