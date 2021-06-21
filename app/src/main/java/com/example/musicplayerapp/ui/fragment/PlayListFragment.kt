package com.example.musicplayerapp.ui.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayerapp.R
import com.example.musicplayerapp.adapter.ListSongAdapter
import com.example.musicplayerapp.model.Music
import com.example.musicplayerapp.ui.activity.MusicPlayerActivity
import com.example.musicplayerapp.ui.activity.SearchActivity
import com.example.musicplayerapp.util.FilesManager
import com.example.musicplayerapp.viewModel.CurrentDataViewModel
import com.example.musicplayerapp.viewModel.FileViewModel
import com.example.musicplayerapp.viewModel.MusicViewModel
import kotlinx.android.synthetic.main.fragment_play_list.*


class PlayListFragment : Fragment() {

    private val currentDataViewModel = CurrentDataViewModel.instance

    private val fileViewModel by lazy {
        ViewModelProvider(
            this,
            FileViewModel.FileViewModelFactory(requireContext())
        )[FileViewModel::class.java]
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

        requestPermission()

        val navController = findNavController()

        btnFavoriteList.setOnClickListener {
            navController.navigate(R.id.action_playListFragment_to_favoriteFragment)
        }

        btnSearch.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            requireActivity().startActivity(intent)
            requireActivity().overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
        }
    }

    private val onItemClick: (music: Music) -> Unit = {
        val intent = Intent(requireContext(), MusicPlayerActivity::class.java)
        intent.putExtra("music", it)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_up, R.anim.slide_stationary)
    }

    private val requestPermissionResultReadFile = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            getAllFile()
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            getAllFile()
            return
        }
        val result = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE

        )
        if (result == PackageManager.PERMISSION_GRANTED) {
            getAllFile()
        } else {
            requestPermissionResultReadFile.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun getAllFile() {
        fileViewModel.getAllAudioFromDevice().observe(viewLifecycleOwner) {
            currentDataViewModel.currentSongs = it
            if (it.size != 0) {
                listSongAdapter.setListSong(it)
                tv_null_data.visibility = View.GONE
            } else {
                tv_null_data.visibility = View.VISIBLE
            }
        }

    }

}