package com.example.sasi_smart_life.data.models

import com.example.sasi_smart_life.view.ScheduleData

/**
 * Represents a single node of a device, with its own position and category.
 */
data class DeviceNode(
    val id: String = "",
    val categoryId: String = "", // The source of truth for the node's category
    val x: Float = 0f,
    val y: Float = 0f,
    val rotation: Float = 0f
)

/**
 * Represents a smart device in our own system (stored in Firebase).
 */

data class Schedule(
    val On: String = "",
    val Off: String = "",
    val Status: Int = 0
)
data class TuyaInfo(
    val iconUrl: String? = null,
    val dps: Map<String, Any> = emptyMap(),
    val isOnline: Boolean = false,
    val category: String? = null,
    val ip: String? = null,
    val mac: String? = null
)

data class Device(
    val devId: String = "",
    val name: String = "",
    val category: String? = null,
    val homeId: String = "",
    val roomId: String? = null,
    val isOnline: Boolean = false,
    val isScene: Boolean = false,
    val status: Boolean = false,
    val schedule: Map<String, Schedule> = emptyMap(),
    val nodes: List<DeviceNode> = emptyList(),
    val isTuya: Boolean = false,
    val tuyaInfo: TuyaInfo? = null
)

