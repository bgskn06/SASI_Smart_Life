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
import com.example.sasi_smart_life.data.models.AreaMapInfo
import com.example.sasi_smart_life.data.models.AreaPopupType
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import androidx.compose.material3.Switch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Surface
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.unit.sp
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import com.example.sasi_smart_life.viewModel.GateLiveStatus
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyRow
import com.example.sasi_smart_life.data.models.DeviceCategory
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

@Composable
fun FloorPlanScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: (Home) -> Unit
) {
    val appState by viewModel.uiState.collectAsState()

    // ---- state declared BEFORE anything uses it ----
    var selectedGate by remember { mutableStateOf<GateInfo?>(null) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var showAddLocationDialog by remember { mutableStateOf(false) }
    var isLocked by remember { mutableStateOf(true) }
    var showLockButton by remember { mutableStateOf(false) }
    var selectedArea by remember { mutableStateOf<AreaMapInfo?>(null) }

    val popupOnlyRoomIds = remember { allAreaMaps.map { it.roomId }.toSet() }

    LaunchedEffect(showLockButton, isLocked) {
        if (isLocked && showLockButton) {
            delay(3000)
            showLockButton = false
        }
    }

    val currentHomeId = appState.selectedHomeId?.homeId
    val gateForThisHome = allGates.find { it.homeId == currentHomeId }

    // ---- dialogs (location / add-location / rumah kampung) unchanged, keep as-is ----
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
        AddLocationDialog(
            onDismissRequest = { showAddLocationDialog = false },
            onSave = { name ->
                viewModel.createHome(name)
                showAddLocationDialog = false
            }
        )
    }

    // ---- gate control dialog, single source of truth ----
    selectedGate?.let { gate ->
        val isSafetyActive = appState.gateSafety[gate.gateId] ?: false   // <-- ADD THIS LINE

        GateControlDialog(
            viewModel = viewModel,
            gateName = gate.name,
            roomId = gate.roomId,
            devIds = gate.devIds,
            isSafetyActive = isSafetyActive,
            onDismiss = { selectedGate = null }
        )
    }

    selectedArea?.let { area ->
        val areaDevices = appState.devices.filter { it.roomId == area.roomId }
        when (area.popupType) {
            AreaPopupType.GRID -> Unit // not used currently — kept for future flexibility
            AreaPopupType.MAP -> AreaMapDialog(
                area = area,
                devices = areaDevices,
                categories = appState.categories,
                viewModel = viewModel,
                onDismiss = { selectedArea = null }
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Column(modifier = Modifier.fillMaxWidth().weight(0.15f)) {
            Header(
                viewModel = viewModel,
                categories = appState.categories,
                devices = appState.devices,
                homeName = appState.selectedHomeId?.name,
                error = appState.error,
                roomCount = appState.rooms.count { it.isMap },
                deviceCount = appState.devices.size,
                onRoomNameClick = { showLocationDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.85f)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { showLockButton = true })
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

            // ---- ONE unified gate icon, works for Barat/Timur/Utara ----
            gateForThisHome?.let { gate ->
                val savedPosition = appState.gatePositions[gate.gateId]
                val gateX = savedPosition?.first ?: 700f
                val gateY = savedPosition?.second ?: 400f
                val liveStatus = appState.gateLiveStatus[gate.gateId] ?: GateLiveStatus()

                DraggableGateIcon(
                    gateId = gate.gateId,
                    gateName = gate.name,
                    iconRes = gate.iconRes,
                    progress = liveStatus.progress,
                    state = liveStatus.state,
                    size = gate.iconSize,
                    slideDistance = gate.slideDistance,
                    x = gateX,
                    y = gateY,
                    isLock = isLocked,
                    onPositionChanged = { newX, newY ->
                        viewModel.updateGatePosition(gateId = gate.gateId, x = newX, y = newY)
                    },
                    onClick = { selectedGate = gate }
                )
            }

            allAreaMaps.filter { it.homeId == currentHomeId }.forEach { area ->
                val isAreaOn = appState.devices
                    .filter { it.roomId == area.roomId }
                    .any { it.status }

                Box(modifier = Modifier.offset(x = area.buildingOffsetX, y = area.buildingOffsetY)) {
                    AreaBuildingIcon(
                        area = area,
                        isOn = isAreaOn,
                        onClick = { selectedArea = area }
                    )
                }
            }

            appState.devices.forEach { device ->
                if (!device.category.isNullOrEmpty() && device.roomId !in popupOnlyRoomIds) {
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
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                    onClick = {
                        isLocked = !isLocked
                        showLockButton = false
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
        "Safety" -> 100.dp
        else -> 36.dp
    }
}

@Composable
private fun DraggableNodeIcon(
    device: Device,
    node: DeviceNode,
    viewModel: MainViewModel,
    isLock: Boolean,
    scale: Float = 1f,
    canvasOffsetX: Float = 0f,
    canvasOffsetY: Float = 0f,
) {
    // Track position in RENDER-space (actual on-screen pixels), not design-space.
    // Re-derive whenever the node, scale, or canvas offset changes (e.g. popup reopened).
    var renderX by remember(node.id, scale, canvasOffsetX, canvasOffsetY) {
        mutableStateOf(canvasOffsetX + node.x * scale)
    }
    var renderY by remember(node.id, scale, canvasOffsetX, canvasOffsetY) {
        mutableStateOf(canvasOffsetY + node.y * scale)
    }
    var isBeingDragged by remember { mutableStateOf(false) }

    val appState by viewModel.uiState.collectAsState()
    val category = appState.categories.find { it.categoryId == node.categoryId }
    val imageUrl = if (device.status) {
        category?.imageUrlOn
    } else {
        category?.imageUrlOff
    }

    val rawSize = getDeviceSize(category?.name) * scale
    val size = if (scale < 1f) rawSize.coerceAtLeast(50.dp) else rawSize
    val scaleX = if (node.mirror) -1f else 1f

    val dragModifier = if (!isLock) {
        Modifier.pointerInput(node.id) {
            detectDragGesturesAfterLongPress(
                onDragStart = {
                    isBeingDragged = true
                },
                onDragEnd = {
                    isBeingDragged = false
                    // convert render-space back to design-space before saving
                    val savedX = (renderX - canvasOffsetX) / scale
                    val savedY = (renderY - canvasOffsetY) / scale
                    viewModel.updateDeviceNodePosition(device.devId, node.id, savedX, savedY)
                },
                onDragCancel = {
                    isBeingDragged = false
                }
            ) { change, dragAmount ->
                change.consume()
                renderX += dragAmount.x
                renderY += dragAmount.y
            }
        }
    } else {
        Modifier
    }

    Box(
        modifier = Modifier
            .offset {
                IntOffset(renderX.roundToInt(), renderY.roundToInt())
            }
            .then(dragModifier)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                if (device.roomId != null) {
                    if (!device.isTuya) {
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

val allAreaMaps = listOf(
    AreaMapInfo(
        areaId = "area_rumah_kampung",
        name = "Rumah Kampung",
        roomId = "room_Taman_1778902444680",
        homeId = "home_1772593337399",
        popupType = AreaPopupType.MAP,
        buildingIconOnRes = R.drawable.rumahkampung_on,
        buildingIconOffRes = R.drawable.rumahkampung,
        mapImageRes = R.drawable.maprumahkampung,
        buildingIconSize = 240.dp,
        buildingOffsetX = 720.dp,
        buildingOffsetY = 320.dp
    )
)

@Composable
fun AreaBuildingIcon(
    area: AreaMapInfo,
    isOn: Boolean,
    onClick: () -> Unit
) {
    val imageRes = if (isOn) area.buildingIconOnRes else area.buildingIconOffRes
    Image(
        painter = painterResource(id = imageRes),
        contentDescription = area.name,
        modifier = Modifier
            .size(area.buildingIconSize)
            .clickable { onClick() },
        contentScale = ContentScale.Fit
    )
}

@Composable
fun AreaMapDialog(
    area: AreaMapInfo,
    devices: List<Device>,
    categories: List<DeviceCategory>,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var isLocked by remember { mutableStateOf(true) }
    var showLockButton by remember { mutableStateOf(false) }

    LaunchedEffect(showLockButton, isLocked) {
        if (isLocked && showLockButton) {
            delay(3000)
            showLockButton = false
        }
    }

    val designWidth = 1920f
    val designHeight = 1200f

    val areaCategoryIds = remember(devices) {
        devices.flatMap { d -> d.nodes.map { it.categoryId } }.toSet()
    }
    val areaCategories = categories.filter { it.categoryId in areaCategoryIds }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.55f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {

                // ---- HEADER ROW: title card + total device card + category outline box ----
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Title card
                    Card(
                        modifier = Modifier
                            .weight(0.3f)
                            .fillMaxHeight(),
                        colors = CardDefaults.cardColors(containerColor = sasiColor.purple50),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = BorderStroke(1.5.dp, sasiColor.purple300),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = area.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Image(
                            painter = painterResource(id = R.drawable.logo_sag),
                            contentDescription = "Room Image"
                        )
                    }

                    // Total Device card
                    Card(
                        modifier = Modifier
                            .weight(0.2f)
                            .fillMaxHeight(),
                        colors = CardDefaults.cardColors(containerColor = sasiColor.grey50),
                        border = BorderStroke(1.5.dp, sasiColor.blue500),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(text = "Total Device", color = sasiColor.black300, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                            Text(
                                text = "${devices.size}",
                                color = sasiColor.blue500,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // ---- Separate outlined box for category cards (scrollable) ----
                    Card(
                        modifier = Modifier
                            .weight(0.5f)
                            .fillMaxHeight(),
                        colors = CardDefaults.cardColors(containerColor = sasiColor.blue50),
                        border = BorderStroke(1.dp, sasiColor.blue300),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(5.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(areaCategories) { category ->
                                AreaCategoryCard(category = category, devices = devices)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ---- MAP ----
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .aspectRatio(designWidth / designHeight)
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = { showLockButton = true })
                        }
                ) {
                    val boxWidthPx = constraints.maxWidth.toFloat()
                    val boxHeightPx = constraints.maxHeight.toFloat()

                    val scale = minOf(boxWidthPx / designWidth, boxHeightPx / designHeight)
                    val renderedImgWidth = designWidth * scale
                    val renderedImgHeight = designHeight * scale
                    val offsetXPx = (boxWidthPx - renderedImgWidth) / 2f
                    val offsetYPx = (boxHeightPx - renderedImgHeight) / 2f

                    Image(
                        painter = painterResource(id = area.mapImageRes ?: R.drawable.scene_empty),
                        contentDescription = area.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    devices.forEach { device ->
                        if (!device.category.isNullOrEmpty()) {
                            device.nodes.forEach { node ->
                                DraggableNodeIcon(
                                    device = device,
                                    node = node,
                                    viewModel = viewModel,
                                    isLock = isLocked,
                                    scale = scale,
                                    canvasOffsetX = offsetXPx,
                                    canvasOffsetY = offsetYPx
                                )
                            }
                        }
                    }

                    val containerColor = if (isLocked) sasiColor.red50 else sasiColor.green50
                    if (!isLocked || showLockButton) {
                        Card(
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                            onClick = {
                                isLocked = !isLocked
                                showLockButton = false
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

                Spacer(modifier = Modifier.height(12.dp))

                // ---- "Tutup" close button under the map ----
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = sasiColor.blue500
                        )
                    ) {
                        Text(
                            text = "Tutup",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AreaCategoryCard(
    category: DeviceCategory,
    devices: List<Device>,
) {
    val totalCount = devices.sumOf { device ->
        device.nodes.count { node -> node.categoryId == category.categoryId }
    }
    val activeCount = devices.filter { it.status }.sumOf { device ->
        device.nodes.count { node -> node.categoryId == category.categoryId }
    }
    val inactiveCount = totalCount - activeCount

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, sasiColor.blue100),
        modifier = Modifier
            .fillMaxHeight()
            .width(100.dp)
    ) {
        Column(
            modifier = Modifier.padding(5.dp),
            horizontalAlignment = Alignment.Start
        ) {
            AsyncImage(
                model = category.image.ifEmpty { category.imageUrlOn },
                placeholder = null,
                error = painterResource(id = R.drawable.scene_empty),
                contentDescription = category.name,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$totalCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Box(modifier = Modifier.size(4.dp).background(Color.Green, CircleShape))
                    Text(text = "$activeCount", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = sasiColor.black300)
                    Box(modifier = Modifier.size(4.dp).background(Color.Gray, CircleShape))
                    Text(text = "$inactiveCount", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = sasiColor.black300)
                }
            }
        }
    }
}

data class GateInfo(
    val gateId: String,
    val name: String,
    val roomId: String,
    val devIds: List<String>,
    val safetyDevId: String,
    val homeId: String,
    val iconRes: Int,              // <-- replaces openIconRes/closeIconRes
    val iconSize: Dp = 220.dp,
    val slideDistance: Dp = 40.dp  // <-- how far it visually slides from closed to open
)

val allGates = listOf(
    GateInfo(
        gateId = "gerbang_barat",
        name = "Gerbang Barat",
        roomId = "room_Gedung_A_1787305379335",
        devIds = listOf("dev_Gerbang_Barat_002", "dev_Gerbang_Barat_003"),
        safetyDevId = "dev_Gerbang_Barat_001",
        homeId = "home_1773196739073",
        iconRes = R.drawable.gerbangbarat_close,   // pick whichever single image represents the gate structure
        iconSize = 220.dp,
        slideDistance = 40.dp
    ),
    GateInfo(
        gateId = "gerbang_timur",
        name = "Gerbang Timur",
        roomId = "room_Office_1787886573803",
        devIds = listOf("dev_Gerbang_Timur_002"),
        safetyDevId = "dev_Gerbang_Timur_001",
        homeId = "home_1772589167730",
        iconRes = R.drawable.gerbangtimur_close,
        iconSize = 170.dp,
        slideDistance = 30.dp
    ),
    GateInfo(
        gateId = "gerbang_utara",
        name = "Gerbang Utara",
        roomId = "room_Gedung_B_1788139696516",
        devIds = listOf("dev_Gerbang_Utara_002"),
        safetyDevId = "dev_Gerbang_Utara_001",
        homeId = "home_1773196769406",
        iconRes = R.drawable.gerbangutara_close,
        iconSize = 220.dp,
        slideDistance = 40.dp
    )
)

@Composable
fun DraggableGateIcon(
    gateId: String,
    gateName: String,
    iconRes: Int,
    progress: Int,
    state: String,
    size: Dp,
    slideDistance: Dp,
    x: Float,
    y: Float,
    isLock: Boolean,
    onPositionChanged: (Float, Float) -> Unit,
    onClick: () -> Unit
) {
    var offsetX by remember(gateId) { mutableStateOf(x) }
    var offsetY by remember(gateId) { mutableStateOf(y) }
    var isBeingDragged by remember { mutableStateOf(false) }

    LaunchedEffect(x, y) {
        if (!isBeingDragged) {
            offsetX = x
            offsetY = y
        }
    }

    val dragModifier = if (!isLock) {
        Modifier.pointerInput(gateId) {
            detectDragGesturesAfterLongPress(
                onDragStart = { isBeingDragged = true },
                onDragEnd = {
                    isBeingDragged = false
                    onPositionChanged(offsetX, offsetY)
                },
                onDragCancel = { isBeingDragged = false }
            ) { change, dragAmount ->
                change.consume()
                offsetX += dragAmount.x
                offsetY += dragAmount.y
            }
        }
    } else {
        Modifier
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress / 100f,
        animationSpec = tween(300),
        label = "gateProgress"
    )

    // slides upward as it opens: 0% progress = no offset (closed),
    // 100% progress = slideDistance upward (fully open)
    val slideY = -slideDistance * animatedProgress

    Column(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .then(dragModifier),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable(enabled = isLock, onClick = onClick)
                .padding(2.dp)
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = gateName,
                modifier = Modifier
                    .size(size)
                    .offset(y = slideY),
                contentScale = ContentScale.Fit
            )
        }

        if (isBeingDragged) {
            Text(
                text = gateName,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                fontSize = 12.sp,
                color = Color.White
            )
        }
    }
}

@Composable
fun GateControl(
    viewModel: MainViewModel,
    roomId: String,
    devIds: List<String>,          // <-- was gateDevId: String
    modifier: Modifier = Modifier
) {
    Spacer(modifier = Modifier.height(10.dp))

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GateButtonImage(
            normalImage = R.drawable.tombolopen,
            pressedImage = R.drawable.tombolopen_pressed,
            contentDescription = "OPEN",
            onClick = { devIds.forEach { viewModel.openGate(roomId = roomId, devId = it) } }
        )
        GateButtonImage(
            normalImage = R.drawable.tombolstop,
            pressedImage = R.drawable.tombolstop_pressed,
            contentDescription = "STOP",
            onClick = { devIds.forEach { viewModel.stopGate(roomId = roomId, devId = it) } }
        )
        GateButtonImage(
            normalImage = R.drawable.tombolclose,
            pressedImage = R.drawable.tombolclose_pressed,
            contentDescription = "CLOSE",
            onClick = { devIds.forEach { viewModel.closeGate(roomId = roomId, devId = it) } }
        )
    }
}

@Composable
fun GateControlDialog(
    viewModel: MainViewModel,
    gateName: String,
    roomId: String,
    devIds: List<String>,
    isSafetyActive: Boolean,      // <-- add this
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = gateName, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = sasiColor.black500)
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = if (isSafetyActive) "SAFETY AKTIF" else "SAFETY NORMAL",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSafetyActive) sasiColor.red500 else sasiColor.green500
                )

                Spacer(modifier = Modifier.height(20.dp))

                GateControl(
                    viewModel = viewModel,
                    roomId = roomId,
                    devIds = devIds
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = sasiColor.purple500
                        )
                    ) {
                        Text(
                            text = "Tutup",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun GateButtonImage(
    normalImage: Int,
    pressedImage: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }

    val isPressed by interactionSource.collectIsPressedAsState()

    Image(
        painter = painterResource(
            id = if (isPressed) {
                pressedImage
            } else {
                normalImage
            }
        ),

        contentDescription = contentDescription,

        modifier = Modifier
            .size(200.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),

        contentScale = ContentScale.Fit
    )
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
