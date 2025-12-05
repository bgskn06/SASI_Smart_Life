package com.example.sasi_smart_life.data.models

/**
 * Represents a Home in our own system (stored in Firebase).
 */
data class Home(
    val homeId: String = "",
    val name: String = "",
    val ownerUid: String = "",
    val imageUrl: String = ""
)
