package com.example.sasi_smart_life.view

import android.util.Log
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
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
            delay(3000)
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
            },
            onLogoutClick = { viewModel.logout() }
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
//  pintu barat x : 738, y : 76
//  pintu belakang 1 x : 1650, y : 446, rotation : 90, mirror : true
//  pintu belakang 2 x : 1650, y : 397, rotation : 90
//  pintu depan 1 x : 313, y : 559, rotation : 270, mirror : true
//  pintu depan 2 x : 313, y : 608, rotation : 270
//  pintu timur 1 x : 739, y : 722, rotation : 180, mirror : true
//  pintu timur 2 x : 788, y : 722, rotation : 180

            appState.devices.forEach { device ->
                if (!device.category.isNullOrEmpty()) {
                    device.nodes.forEach { node ->
                        DraggableNodeIcon(
                            device = device,
                            node = node,
                            viewModel = viewModel,
                            isLock = isLocked
                        )
                    }
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

//Helper Ukuran Icon
fun getDeviceSize(categoryName: String?): Dp {
    return when (categoryName) {
        "Wooden Lamp" -> 72.dp
        "Leather Lamp" -> 52.dp
        "Stop Kontak" -> 9.dp
        "Lampu KMI" -> 148.dp
        "Street Lamp" -> 128.dp
        "Spot Lamp" -> 24.dp
        "Door Sensor" -> 28.dp
        "Lampu Taman" -> 92.dp
        "Limasan", "Mushola" -> 240.dp
        "Kandang Kebo", "Stage" -> 180.dp
        "Layar" -> 144.dp
        "Lampu Sorot" -> 18.dp
        "Sensor Gerbang" -> 172.dp
        "Lampu Highbay" -> 52.dp
        "Door Sensor Gerbang" -> 118.dp
        "Door Sensor Gerbang 2" -> 144.dp
        else -> 36.dp
    }
}

@Composable
private fun DraggableNodeIcon(
    device: Device,
    node: DeviceNode,
    viewModel: MainViewModel,
    isLock: Boolean,
) {
    var offsetX by remember { mutableStateOf(node.x) }
    var offsetY by remember { mutableStateOf(node.y) }
    var isBeingDragged by remember { mutableStateOf(false) }

    val appState by viewModel.uiState.collectAsState()
    val category = appState.categories.find { it.categoryId == node.categoryId }
    val imageUrl = if (device.status) {
        category?.imageUrlOn
    } else {
        category?.imageUrlOff
    }

    val size = getDeviceSize(category?.name)
    val scaleX = if (node.mirror) -1f else 1f

    val dragModifier = if (!isLock) {
        Modifier.pointerInput(node.id) {
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
                    if(!device.isTuya){
                        viewModel.setDeviceStatus(device.devId, device.roomId, !device.status)
                    }
                }
            }
    ) {
        Box(
            modifier = Modifier.rotate(node.rotation)
        ) {
            DeviceImage(
                imageUrl = imageUrl,
                contentDescription = device.name,
                size = size,
                scaleX = scaleX
            )
        }
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

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier
) {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition(label = "")

    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 800f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Restart
        ),
        label = ""
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(translateAnim.value, translateAnim.value)
    )

    Box(
        modifier = modifier.background(brush)
    )
}

@Composable
fun DeviceImage(
    imageUrl: String?,
    contentDescription: String?,
    size: Dp,
    scaleX: Float
) {
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val model = remember(imageUrl) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .size(Size.ORIGINAL)
            .build()
    }


    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {

        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .scale(scaleX, 1f),
            onState = { state ->
                isLoading = state is AsyncImagePainter.State.Loading
//                isError = state is AsyncImagePainter.State.Error
            }
        )

        when {
            isLoading -> {
                ShimmerBox(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(8.dp))
                )
            }

            isError -> {
                Image(
                    painter = painterResource(R.drawable.scene_empty),
                    contentDescription = "error"
                )
            }
        }
    }
}

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
