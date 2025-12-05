package com.example.sasi_smart_life.data.models

/**
 * Represents a Smart Scene in our own system (stored in Firebase).
 */
data class SmartScene(
    val sceneId: String = "",
    val homeId: String = "",
    val name: String = "",
    val ifData: Map<String, Any> = emptyMap(),
    val thenActions: List<Map<String, Any>> = emptyList()
)
