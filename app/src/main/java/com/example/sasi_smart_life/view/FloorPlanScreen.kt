package com.example.sasi_smart_life.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.sasi_smart_life.R
import com.example.sasi_smart_life.data.models.Device
import com.example.sasi_smart_life.data.models.DeviceNode
import com.example.sasi_smart_life.data.models.Home
import com.example.sasi_smart_life.data.models.Room
import com.example.sasi_smart_life.view.master.Header
import com.example.sasi_smart_life.view.theme.sasiColor
import com.example.sasi_smart_life.viewModel.MainViewModel
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun FloorPlanScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: (Home) -> Unit
) {
    val appState by viewModel.uiState.collectAsState()

    var showLocationDialog by remember { mutableStateOf(false) }
    var showAddLocationDialog by remember { mutableStateOf(false) }
    var isLocked by remember { mutableStateOf(true) }
    var showLockButton by remember { mutableStateOf(false) }

    LaunchedEffect(showLockButton, isLocked) {
        if (isLocked && showLockButton) {
            delay(3000) // Keep button visible for 3 seconds
            showLockButton = false
        }
    }

    if (showLocationDialog) {
        LocationSelectionDialog(
            homes = appState.homes,
            currentHomeId = appState.selectedHomeId?.homeId,
            onDismissRequest = { showLocationDialog = false },
            onLocationSelected = { selectedHome ->
                viewModel.selectHome(selectedHome)
                showLocationDialog = false
            },
            onAddLocationClick = {
                showLocationDialog = false
                showAddLocationDialog = true
            },
            onSettingsClick = {
                showLocationDialog = false
                onNavigateToSettings(it)
            }
        )
    }

    if (showAddLocationDialog) {
        AddLocationDialog(onDismissRequest = { showAddLocationDialog = false },onSave = { name ->
            viewModel.createHome(name)
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.15f)
        ) {
            Header(
                viewModel = viewModel,
                categories = appState.categories,
                devices = appState.devices,
                homeName = appState.selectedHomeId?.name,
                error = appState.error,
                roomCount = appState.rooms.count { it.isMap },
                deviceCount = appState.devices.size,
                onRoomNameClick = { showLocationDialog = true },
            )
        }
        Spacer(modifier = Modifier.height(10.dp))

        Box(modifier = Modifier
            .fillMaxWidth()
            .weight(0.85f)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { showLockButton = true } // Show button on any tap
                )
            }
        ) {
            AsyncImage(
                model = appState.selectedHomeId?.imageUrl,
                placeholder = painterResource(id = R.drawable.logo_sag),
                error = painterResource(id = R.drawable.scene_empty),
                contentDescription = "Floor Plan",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            appState.devices.forEach { device ->
                device.nodes.forEach { node ->
                    DraggableNodeIcon(
                        device = device,
                        node = node,
                        viewModel = viewModel,
                        isLock = isLocked
                    )
                }
            }

            appState.rooms.filter { it.isMap }.forEach { room ->
                DraggableRoomLabel(room = room, viewModel = viewModel, isLock = isLocked)
            }

            val containerColor = if (isLocked) sasiColor.red50 else sasiColor.green50

            if (!isLocked || showLockButton) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    onClick = {
                        isLocked = !isLocked
                        showLockButton = false // Immediately hide button when locking
                    },
                    colors = CardDefaults.cardColors(containerColor = containerColor)
                ) {
                    Icon(
                        imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Toggle Lock",
                        modifier = Modifier.padding(8.dp),
                        tint = if (isLocked) sasiColor.red500 else sasiColor.green500
                    )
                }
            }
        }
    }
}

@Composable
private fun DraggableNodeIcon(
    device: Device,
    node: DeviceNode,
    viewModel: MainViewModel,
    isLock: Boolean
) {
    var offsetX by remember { mutableStateOf(node.x) }
    var offsetY by remember { mutableStateOf(node.y) }
    var isBeingDragged by remember { mutableStateOf(false) }

    val appState by viewModel.uiState.collectAsState()
    val category = appState.categories.find { it.categoryId == node.categoryId }
    val imageUrl = if (device.status) category?.imageUrlOn else category?.imageUrlOff

    val dragModifier = if (!isLock) {
        Modifier.pointerInput(node.id) { // Keyed to the node ID
            detectDragGesturesAfterLongPress(
                onDragStart = {
                    isBeingDragged = true
                },
                onDragEnd = {
                    isBeingDragged = false
                    viewModel.updateDeviceNodePosition(device.devId, node.id, offsetX, offsetY)
                },
                onDragCancel = {
                    isBeingDragged = false
                }
            ) { change, dragAmount ->
                change.consume()
                offsetX += dragAmount.x
                offsetY += dragAmount.y
            }
        }
    } else {
        Modifier
    }

    Box(
        modifier = Modifier
            .offset {
                IntOffset(offsetX.roundToInt(), offsetY.roundToInt())
            }
            .then(dragModifier)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                if (device.roomId != null) {
                    viewModel.setDeviceStatus(device.devId, device.roomId, !device.status)
                }
            }
            .rotate(node.rotation)
    ) {
        Box {
            AsyncImage(
                model = imageUrl,
                placeholder = painterResource(id = R.drawable.logo_sag),
                error = painterResource(id = R.drawable.scene_empty),
                contentDescription = device.name,
                modifier = Modifier
                    .size(if (category?.name == "Wooden Lamp") 72.dp
                    else if(category?.name == "Leather Lamp") 52.dp
                    else if(category?.name == "Stop Kontak") 9.dp
                    else 36.dp)
            )
            if (isBeingDragged) {
                Text(
                    text = device.name,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = -28.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = sasiColor.grey50
                )
            }
        }
    }
}

// DraggableDeviceIcon has been removed.

@Composable
private fun DraggableRoomLabel(
    room: Room,
    viewModel: MainViewModel,
    isLock: Boolean
) {
    var offsetX by remember { mutableStateOf(room.x) }
    var offsetY by remember { mutableStateOf(room.y) }

    val dragModifier = if (!isLock) {
        Modifier.pointerInput(room.roomId) {
            detectDragGesturesAfterLongPress(
                onDragEnd = {
                    viewModel.updateRoomPosition(room.roomId, offsetX, offsetY)
                }
            ) { change, dragAmount ->
                change.consume()
                offsetX += dragAmount.x
                offsetY += dragAmount.y
            }
        }
    } else {
        Modifier
    }

    Box(
        modifier = Modifier
            .offset {
                IntOffset(offsetX.roundToInt(), offsetY.roundToInt())
            }
            .then(dragModifier)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = sasiColor.blue50),
            border = BorderStroke(1.dp, sasiColor.blue500),
        ) {
            Text(
                text = room.name,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = sasiColor.blue500
            )
        }
    }
}
