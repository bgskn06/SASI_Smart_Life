package com.example.sasi_smart_life.data.models

/**
 * Represents a Room in our own system (stored in Firebase).
 */
data class Room(
    val roomId: String = "", // Unique ID in Firebase
    val name: String = "",
    val homeId: String = "", // The ID of the home this room belongs to
    val x: Float = 0f,
    val y: Float = 0f,
    val isMap: Boolean = false
)
