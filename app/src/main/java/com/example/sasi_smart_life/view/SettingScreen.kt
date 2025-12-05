
package com.example.sasi_smart_life.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.rounded.MeetingRoom
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.navigation.NavController
import com.example.sasi_smart_life.data.models.Device
import com.example.sasi_smart_life.data.models.Home
import com.example.sasi_smart_life.data.models.Room
import com.example.sasi_smart_life.data.models.SmartScene
import com.example.sasi_smart_life.view.master.BottomNavBar
import com.example.sasi_smart_life.view.master.Header
import com.example.sasi_smart_life.view.theme.sasiColor
import com.example.sasi_smart_life.viewModel.MainViewModel

@Composable
fun SettingScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: (Home) -> Unit,
    navController: NavController,
    currentRoute: String?
) {
    val appState by viewModel.uiState.collectAsState()
    var showLocationDialog by remember { mutableStateOf(false) }
    var showAddLocationDialog by remember { mutableStateOf(false) }
    var showAddSceneDialog by remember { mutableStateOf(false) } // State for the new dialog

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

    if (showAddSceneDialog) {
        AddSmartSceneDialog(
            devices = appState.devices,
            rooms = appState.rooms,
            onDismissRequest = { showAddSceneDialog = false },
            onSave = { name, ifData, thenActions ->
                val homeId = appState.selectedHomeId?.homeId
                if (homeId != null) {
                    viewModel.addScene(homeId, name, ifData, thenActions)
                }
                showAddSceneDialog = false
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .weight(0.15f)) {
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

            var selectedTab by remember { mutableStateOf("Room") }
            Row(modifier = Modifier
                .fillMaxWidth()
                .weight(0.05f)) {
                val buttonShape = RoundedCornerShape(50)
                Text(
                    "Room",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selectedTab == "Room") sasiColor.blue50 else sasiColor.black300,
                    modifier = Modifier
                        .clip(buttonShape)
                        .clickable { selectedTab = "Room" }
                        .background(if (selectedTab == "Room") sasiColor.blue500 else Color.Transparent)
                        .border(
                            BorderStroke(
                                1.dp,
                                if (selectedTab == "Room") Color.Transparent else sasiColor.grey600
                            ),
                            buttonShape
                        )
                        .padding(horizontal = 12.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Smart Scene",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selectedTab == "Smart Scene") sasiColor.blue50 else sasiColor.black300,
                    modifier = Modifier
                        .clip(buttonShape)
                        .clickable { selectedTab = "Smart Scene" }
                        .background(if (selectedTab == "Smart Scene") sasiColor.blue500 else Color.Transparent)
                        .border(
                            BorderStroke(
                                1.dp,
                                if (selectedTab == "Smart Scene") Color.Transparent else sasiColor.grey600
                            ),
                            buttonShape
                        )
                        .padding(horizontal = 12.dp)
                )
            }

            Box(modifier = Modifier
                .fillMaxWidth()
                .weight(0.8f)) {
                if (selectedTab == "Room") {
                    RoomManagement(
                        navController = navController,
                        viewModel = viewModel,
                        rooms = appState.rooms,
                        devices = appState.devices
                    )
                }
                if (selectedTab == "Smart Scene") {
                    SmartScene(
                        viewModel = viewModel,
                        sceneList = appState.scenes,
                        onAddSceneClick = { showAddSceneDialog = true } // Trigger the dialog
                    )
                }
            }
        }

        BottomNavBar(
            navController = navController,
            currentRoute = currentRoute,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

// ROOM MANAGEMENT
@Composable
fun RoomManagement(
    navController: NavController,
    viewModel: MainViewModel,
    rooms: List<Room>,
    devices: List<Device>
) {
    var showAddRoomDialog by remember { mutableStateOf(false) }
    val currentHomeId = viewModel.uiState.collectAsState().value.selectedHomeId?.homeId

    if (showAddRoomDialog) {
        AddRoomDialog(
            onDismissRequest = { showAddRoomDialog = false },
            onSave = { roomName ->
                if (currentHomeId != null) {
                    viewModel.createRoom(
                        homeId = currentHomeId,
                        name = roomName
                    )
                }
                showAddRoomDialog = false
            }
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(rooms) { room ->
            val deviceCount = devices.count { it.roomId == room.roomId }
            RoomCard(
                room = room,
                deviceCount = deviceCount,
                onRoomClick = {
                    navController.navigate("device_management_screen/${room.roomId}")
                },
                onToggleFloorPlan = { roomId, isMap ->
                    viewModel.updateRoomIsMap(roomId, isMap)
                }
            )
        }
        item {
            AddRoomCard(onClick = { showAddRoomDialog = true })
        }
    }
}

@Composable
fun RoomCard(
    room: Room,
    deviceCount: Int,
    onRoomClick: () -> Unit,
    onToggleFloorPlan: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showOptions by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = sasiColor.grey50),
        modifier = modifier
            .height(120.dp)
            .clickable(onClick = onRoomClick),
        border = BorderStroke(1.dp, sasiColor.grey600)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                sasiColor.purple500.copy(alpha = 0.5f),
                                Color.Transparent
                            ),
                            center = Offset(x = 75f, y = 0f),
                            radius = 150f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MeetingRoom,
                            contentDescription = room.name,
                            modifier = Modifier
                                .size(40.dp)
                                .padding(8.dp),
                            tint = sasiColor.purple500
                        )
                    }
                    Box {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "Options For ${room.name}",
                            tint = sasiColor.purple500,
                            modifier = Modifier.clickable(onClick = { showOptions = true })
                        )
                        if (showOptions) {
                            RoomOption(
                                room = room,
                                onDismissRequest = { showOptions = false },
                                onToggleFloorPlan = { onToggleFloorPlan(room.roomId, !room.isMap) }
                            )
                        }
                    }
                }
                Column {
                    Text(text = room.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = sasiColor.grey50),
                        border = BorderStroke(1.dp, sasiColor.grey600)
                    ) {
                        Text(
                            text = "$deviceCount Devices",
                            style = MaterialTheme.typography.labelMedium,
                            color = sasiColor.black300,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddRoomCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = sasiColor.blue50),
        border = BorderStroke(2.dp, sasiColor.blue100),
        modifier = modifier
            .height(120.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = "Add Room",
                tint = sasiColor.blue500,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Add Room",
                color = sasiColor.blue500,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AddRoomDialog(
    onDismissRequest: () -> Unit,
    onSave: (String) -> Unit,
) {

    var name by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .fillMaxHeight(0.45f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Add Room",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Enter Room Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = sasiColor.purple500,
                        unfocusedBorderColor = sasiColor.black300,
                        cursorColor = sasiColor.purple500,
                        focusedLabelColor = sasiColor.black300,
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = sasiColor.red50)
                    ) {
                        Text("Cancel", color = sasiColor.red500)
                    }
                    Button(
                        onClick = {
                            if (name.isNotBlank()) onSave(name)
                            if (name.isNotBlank()) onDismissRequest()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = sasiColor.blue500)
                    ) {
                        Text("Save", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun RoomOption(
    room: Room,
    onDismissRequest: () -> Unit,
    onToggleFloorPlan: () -> Unit
) {
    Popup(onDismissRequest = onDismissRequest) {
        Card (
            colors = CardDefaults.cardColors(containerColor = sasiColor.grey50),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),

            ) {
            Column (modifier = Modifier
                .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row (verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.clickable(onClick = {})
                ) {
                    Icon(imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Room",
                        modifier = Modifier
                            .size(18.dp),
                        tint = sasiColor.yellow500,
                    )
                    Text("Edit", style = MaterialTheme.typography.labelMedium)
                }
                Row (verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.clickable(onClick = {})
                ) {
                    Icon(imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Room",
                        modifier = Modifier
                            .size(18.dp),
                        tint = sasiColor.red500,
                    )
                    Text("Delete", style = MaterialTheme.typography.labelMedium)
                }
                Row (verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.clickable(onClick = onToggleFloorPlan)
                ) {
                    Icon(imageVector = Icons.Default.AddCircle,
                        contentDescription = "Add Room",
                        modifier = Modifier
                            .size(18.dp),
                        tint = sasiColor.blue500,
                    )
                    Text(
                        text = if (room.isMap) "Floor Plan Enabled" else "Floor Plan Disabled",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}
// END ROOM MANAGEMENT
