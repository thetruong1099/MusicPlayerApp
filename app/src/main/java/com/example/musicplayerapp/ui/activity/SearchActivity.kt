package com.example.musicplayerapp.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayerapp.R
import com.example.musicplayerapp.adapter.ListSongAdapter
import com.example.musicplayerapp.model.Music
import com.example.musicplayerapp.viewModel.MusicViewModel
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : AppCompatActivity() {

    private val musicViewModel by lazy {
        ViewModelProvider(
            this,
            MusicViewModel.MusicViewModelFactory(this.application)
        )[MusicViewModel::class.java]
    }

    private val listSongAdapter: ListSongAdapter by lazy {
        ListSongAdapter(onItemClick)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
    }

    override fun onStart() {
        super.onStart()

        btn_back_search.setOnClickListener { onBackPressed() }

        rv_music_search.apply {
            adapter = listSongAdapter
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        listSongAdapter.setStatusActivity(true)

        edt_search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    var keyword =
                        "%${s.toString().lowercase()}%"

                    musicViewModel.searchMusic(keyword).observe(this@SearchActivity, Observer {
                        listSongAdapter.setListSong(it)
                    })
                } else listSongAdapter.clearListSong()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private val onItemClick: (music: Music) -> Unit = {
        val intent = Intent(this, MusicPlayerActivity::class.java)
        intent.putExtra("music", it)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_stationary)
        finish()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}