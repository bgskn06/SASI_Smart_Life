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
    val gatePositions: Map<String, Pair<Float, Float>> = emptyMap(),
    val gateCommands: Map<String, Int> = emptyMap(),
    val gateSafety: Map<String, Boolean> = emptyMap(),   // gateId -> true (active) / false (normal)
    val gateLiveStatus: Map<String, GateLiveStatus> = emptyMap(),
    val error: String? = null,
    val info: String? = null
)

data class GateLiveStatus(
    val state: String = "UNKNOWN",
    val progress: Int = 0
)
