package com.example.sasi_smart_life.data.models

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AreaPopupType { GRID, MAP }

data class AreaMapInfo(
    val areaId: String,
    val name: String,
    val roomId: String,
    val homeId: String,
    val popupType: AreaPopupType,
    val buildingIconOnRes: Int,
    val buildingIconOffRes: Int,
    val mapImageRes: Int? = null,
    val buildingIconSize: Dp = 220.dp,
    val buildingOffsetX: Dp = 0.dp,
    val buildingOffsetY: Dp = 0.dp
)