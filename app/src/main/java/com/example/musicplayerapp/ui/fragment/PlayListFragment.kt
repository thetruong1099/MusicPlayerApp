package com.example.musicplayerapp.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayerapp.R
import com.example.musicplayerapp.adapter.ListSongAdapter
import com.example.musicplayerapp.model.Music
import com.example.musicplayerapp.ui.activity.MusicPlayerActivity
import com.example.musicplayerapp.ui.activity.SearchActivity
import com.example.musicplayerapp.viewModel.CurrentDataViewModel
import com.example.musicplayerapp.viewModel.MusicViewModel
import kotlinx.android.synthetic.main.fragment_play_list.*


class PlayListFragment : Fragment() {

    private var tempList:MutableList<Music> = mutableListOf()

    private val currentDataViewModel  = CurrentDataViewModel.instance

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
        return inflater.inflate(R.layout.fragment_play_list, container, false)
    }

    override fun onStart() {
        super.onStart()

        recyclerViewSongs.apply {
            adapter = listSongAdapter
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        musicViewModel.getAllMusic().observe(viewLifecycleOwner){
            if(it.size!=0){
                currentDataViewModel.currentSongs = it
                listSongAdapter.setListSong(it)
                tv_null_data.visibility = View.GONE
            }else{
                tv_null_data.visibility = View.VISIBLE
            }
        }

        val navController = findNavController()

        btnFavoriteList.setOnClickListener {
            navController.navigate(R.id.action_playListFragment_to_favoriteFragment)
        }

        btnSearch.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            requireActivity().startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private val onItemClick: (music:Music) -> Unit = {
        val intent = Intent(requireContext(), MusicPlayerActivity::class.java)
        intent.putExtra("music", it)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_up, R.anim.slide_stationary)
    }

}