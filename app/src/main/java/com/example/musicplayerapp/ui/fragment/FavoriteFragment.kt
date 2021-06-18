package com.example.musicplayerapp.ui.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayerapp.R
import com.example.musicplayerapp.adapter.ListSongAdapter
import com.example.musicplayerapp.model.Music
import com.example.musicplayerapp.ui.activity.MusicPlayerActivity
import com.example.musicplayerapp.viewModel.MusicViewModel
import kotlinx.android.synthetic.main.fragment_favorite.*


class FavoriteFragment : Fragment() {

    private val musicViewModel by lazy {
        ViewModelProvider(
            this,
            MusicViewModel.MusicViewModelFactory(requireActivity().application)
        )[MusicViewModel::class.java]
    }

    private val listSongAdapter: ListSongAdapter by lazy {
        ListSongAdapter(onItemClick)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onStart() {
        super.onStart()

        val navController = findNavController()
        btn_back_favorite.setOnClickListener {
            navController.popBackStack()
        }

        rv_favorite_music.apply {
            adapter = listSongAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        musicViewModel.getFavoriteMusic().observe(viewLifecycleOwner, Observer {
            if (it!=null){
                listSongAdapter.setListSong(it)
                tv_null_data.visibility = View.GONE
            }else{
                tv_null_data.visibility = View.VISIBLE
            }
        })

    }
    private val onItemClick: (music: Music) -> Unit = {
        val intent = Intent(requireContext(), MusicPlayerActivity::class.java)
        intent.putExtra("music", it)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_up, R.anim.slide_stationary)
    }

}