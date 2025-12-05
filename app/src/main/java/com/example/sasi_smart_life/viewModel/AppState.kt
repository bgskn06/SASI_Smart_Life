package com.example.sasi_smart_life.viewModel

import com.example.sasi_smart_life.data.models.*

data class AppState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,

    val currentUser: User? = null,
    val homes: List<Home> = emptyList(),
    val selectedHomeId: Home? = null,
    val rooms: List<Room> = emptyList(),
    val devices: List<Device> = emptyList(),
    val categories: List<DeviceCategory> = emptyList(),
    val scenes: List<SmartScene> = emptyList(),

    val error: String? = null,
    val info: String? = null
)
