package com.example.musicplayerapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayerapp.R
import com.example.musicplayerapp.model.Music
import kotlinx.android.synthetic.main.recycler_view_audio_item.view.*

class ListSongAdapter(
    private val onclick: (Music) -> Unit
) : RecyclerView.Adapter<ListSongAdapter.ViewHolder>() {

    private var statusSearchActivity = false
    private var listSong: MutableList<Music> = mutableListOf()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName = itemView.tv_name_song_rv
        val tvArtile = itemView.tv_artist_rv

        fun onBind(musicModel: Music, position: Int) {
            tvName.text = musicModel.nameSong
            if (musicModel.artistName == null) {
                if (musicModel.album == null) {
                    tvArtile.text = "Unknown Artist | Unknown Album"
                } else {
                    tvArtile.text = "Unknown Artist | ${musicModel.album}"
                }

            } else {
                if (musicModel.album == null) {
                    tvArtile.text = "${musicModel.artistName} | Unknown Album"
                } else {
                    tvArtile.text = "${musicModel.artistName} | ${musicModel.album}"
                }

            }

            itemView.setOnClickListener { onclick(musicModel) }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView: View =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_view_audio_item, parent, false)
        return ViewHolder((itemView))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(listSong[position], position)
    }

    override fun getItemCount(): Int = listSong.size

    fun setListSong(listSong: MutableList<Music>) {
        this.listSong = listSong
        notifyDataSetChanged()
    }

    fun clearListSong() {
        listSong.clear()
        notifyDataSetChanged()
    }

    fun setStatusActivity(status: Boolean) {
        statusSearchActivity = status
    }

}