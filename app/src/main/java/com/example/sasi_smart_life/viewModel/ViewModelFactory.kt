package com.example.sasi_smart_life.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sasi_smart_life.data.repository.*

class MainViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {

            val userRepo = FBUserRepository()
            val homeRepo = FBHomeRepository()
            val roomRepo = FBRoomRepository()
            val deviceRepo = FBDeviceRepository()
            val sceneRepo = FBSceneRepository()
            val categoryRepo = FBCategoryRepository()
            val scheduleRepo = FBScheduleRepository()


            return MainViewModel(
                userRepo,
                homeRepo,
                roomRepo,
                deviceRepo,
                sceneRepo,
                categoryRepo,
                scheduleRepo
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
