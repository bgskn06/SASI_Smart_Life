package com.example.sasi_smart_life.data.models

/**
 * Represents a Smart Scene in our own system (stored in Firebase).
 */
data class SmartScene(
    val sceneId: String = "",
    val homeId: String = "",
    val name: String = "",
    val isActive: Boolean = true,
    val ifData: SceneCondition = SceneCondition(),
    val schedule: SceneSchedule = SceneSchedule(),
    val thenAction: List<SceneAction> = emptyList()
)

data class SceneCondition(
    val devId: String = "",
    val operator: String = "==",
    val status: Int = 0
)

data class SceneSchedule(
    val enabled: Boolean = false,
    val startTime: String = "00:00",
    val endTime: String = "23:59",
    val days: Map<String, Boolean> = mapOf(
        "mon" to true, "tue" to true, "wed" to true, "thu" to true,
        "fri" to true, "sat" to true, "sun" to true
    )
)

data class SceneAction(
    val devId: String = "",
    val status: Int = 0
)