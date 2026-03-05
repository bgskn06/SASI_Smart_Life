package com.example.sasi_smart_life.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.RemoveCircle
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
    var showAddSceneDialog by remember { mutableStateOf(false) }

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

//    if (showAddSceneDialog) {
//        AddSmartSceneDialog(
//            devices = appState.devices,
//            rooms = appState.rooms,
//            onDismissRequest = { showAddSceneDialog = false },
//            onSave = { name, ifData, thenActions ->
//                val homeId = appState.selectedHomeId?.homeId
//                if (homeId != null) {
//                    viewModel.addScene(homeId, name, ifData, thenActions)
//                }
//                showAddSceneDialog = false
//            }
//        )
//    }

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
                .weight(0.05f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row{
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
                if(selectedTab == "Smart Scene"){
                    Text(
                        text = "One Tap",
                        style = MaterialTheme.typography.bodyMedium,
                        color = sasiColor.blue300,
                        modifier = Modifier.clickable{

                        }
                    )
                }
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
                        categories = appState.categories,
                        device = appState.devices,
                        viewModel = viewModel,
                        sceneList = appState.scenes,
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
    var errorMessage by remember { mutableStateOf<String?>(null) }

    if (showAddRoomDialog) {
        AddRoomDialog(
            onDismissRequest = { showAddRoomDialog = false },
            onSave = { roomName ->
                if (currentHomeId != null) {
                    viewModel.createRoom(
                        homeId = currentHomeId,
                        name = roomName
                    ) { success, error ->
                        if (success) {
                            showAddRoomDialog = false
                        } else {
                            errorMessage = error
                        }
                    }
                }
            },
            errorMessage = errorMessage
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
                },
                viewModel = viewModel
            )
        }
        item {
            AddRoomCard(onClick = { showAddRoomDialog = true }) // Show dialog here
        }
    }
}

@Composable
fun RoomCard(
    viewModel : MainViewModel,
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
                    Box { // This Box is a container for the icon and the popup
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "Options For ${room.name}",
                            tint = sasiColor.purple500,
                            modifier = Modifier.clickable(onClick = { showOptions = true })
                        )
                        if (showOptions) {
                            RoomOption(
                                room = room,
                                viewModel = viewModel ,
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
    errorMessage: String?
) {
    var name by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .fillMaxHeight(0.35f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Add Room",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Room Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = errorMessage != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = sasiColor.purple500,
                            unfocusedBorderColor = sasiColor.black300,
                            cursorColor = sasiColor.purple500,
                            focusedLabelColor = sasiColor.black300,
                        )
                    )
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismissRequest,
                        colors = ButtonDefaults.buttonColors(containerColor = sasiColor.red50)
                    ) {
                        Text("Cancel", color = sasiColor.red500)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(name) },
                        colors = ButtonDefaults.buttonColors(containerColor = sasiColor.blue500)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}




// SMART SCENE
//@Composable
//fun SmartScene(
//    viewModel: MainViewModel,
//    sceneList: List<SmartScene>,
//    onAddSceneClick: () -> Unit
//) {
//    LazyVerticalGrid(
//        columns = GridCells.Fixed(4),
//        verticalArrangement = Arrangement.spacedBy(8.dp),
//        horizontalArrangement = Arrangement.spacedBy(8.dp),
//    ) {
//        items(sceneList) { scene ->
//            SmartSceneCard(
//                scene = scene,
//                onClick = {
//                    viewModel.executeScene(scene)
//                }
//            )
//        }
//        item {
//            AddSceneCard(onClick = onAddSceneClick)
//        }
//    }
//}
//

@Composable
fun AddSceneCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
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
                contentDescription = "Add Smart Scene",
                tint = sasiColor.blue500,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Add Smart Scene",
                color = sasiColor.blue500,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SmartSceneCard(scene: SmartScene, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .height(120.dp)
            .fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = scene.name, fontWeight = FontWeight.Bold)
        }
    }
}

//@Composable
//fun AddSmartSceneDialog(
//    devices: List<Device>,
//    rooms: List<Room>,
//    onDismissRequest: () -> Unit,
//    onSave: (String, Map<String, Any>, List<Map<String, Any>>) -> Unit
//) {
//    var sceneName by remember { mutableStateOf("") }
//    var ifCondition by remember { mutableStateOf<Map<String, Any>>(emptyMap()) }
//    var thenActions by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
//
//    Dialog(onDismissRequest = onDismissRequest) {
//        Card(modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp)) {
//            Column(modifier = Modifier
//                .padding(16.dp)
//                .fillMaxWidth()) {
//                Text("Add Smart Scene", style = MaterialTheme.typography.headlineSmall)
//                Spacer(modifier = Modifier.height(16.dp))
//                OutlinedTextField(
//                    value = sceneName,
//                    onValueChange = { sceneName = it },
//                    label = { Text("Scene Name") },
//                    modifier = Modifier.fillMaxWidth()
//                )
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // "If" condition setup (simplified)
//                Text("If:", fontWeight = FontWeight.Bold)
//
//                // Example: Time-based condition
//                var selectedHour by remember { mutableStateOf(12) }
//                var selectedMinute by remember { mutableStateOf(0) }
//
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Text("Time is: ")
//                    // Simple time picker placeholder
//                    Button(onClick = { /* Implement time picker */ }) {
//                        Text("$selectedHour:$selectedMinute")
//                    }
//                    ifCondition = mapOf("type" to "time", "hour" to selectedHour, "minute" to selectedMinute)
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // "Then" action setup
//                Text("Then:", fontWeight = FontWeight.Bold)
//
//                // Example: Control a device
//                var selectedDevice by remember { mutableStateOf<Device?>(null) }
//                var selectedAction by remember { mutableStateOf(false) }
//                var showDeviceSelector by remember { mutableStateOf(false) }
//
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Button(onClick = { showDeviceSelector = true }) {
//                        Text(selectedDevice?.name ?: "Select Device")
//                    }
//                    if (showDeviceSelector) {
//                        DeviceSelector(devices, onDeviceSelected = {
//                            selectedDevice = it
//                            showDeviceSelector = false
//                        }, onDismiss = { showDeviceSelector = false })
//                    }
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text("Turn")
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Switch(checked = selectedAction, onCheckedChange = { selectedAction = it })
//                    Text(if (selectedAction) "On" else "Off")
//                }
//
//                Button(onClick = {
//                    selectedDevice?.let {
//                        val newAction = mapOf("devId" to it.devId, "status" to selectedAction)
//                        thenActions = thenActions + newAction
//                        selectedDevice = null // Reset for next action
//                    }
//                }) {
//                    Text("Add Action")
//                }
//
//                // Display added actions
//                thenActions.forEach { action ->
//                    val deviceName = devices.find { it.devId == action["devId"] }?.name
//                    val statusText = if (action["status"] == true) "On" else "Off"
//                    Text("Turn $deviceName $statusText")
//                }
//
//                Spacer(modifier = Modifier.height(32.dp))
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.End
//                ) {
//                    TextButton(onClick = onDismissRequest) {
//                        Text("Cancel")
//                    }
//                    Button(onClick = {
//                        if (sceneName.isNotBlank() && ifCondition.isNotEmpty() && thenActions.isNotEmpty()) {
//                            onSave(sceneName, ifCondition, thenActions)
//                        }
//                    }) {
//                        Text("Save")
//                    }
//                }
//            }
//        }
//    }
//}

@Composable
fun RoomOption(
    room: Room,
    viewModel: MainViewModel,
    onDismissRequest: () -> Unit,
    onToggleFloorPlan: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // The popup will be positioned relative to this Box
    Box {
        Popup(
            alignment = Alignment.TopStart,
            onDismissRequest = onDismissRequest,
        ) {
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = sasiColor.grey50
                )
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = { /* Handle Edit */ })
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Room", tint = sasiColor.yellow500)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Room",style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showDeleteConfirmation = true }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Room", tint = sasiColor.red500)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Room", style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onToggleFloorPlan)
                    ) {
                        Icon(
                            if (room.isMap) Icons.Default.AddCircle else Icons.Default.RemoveCircle,
                            contentDescription = if (room.isMap) "Hide from Floor Plan" else "Show on Floor Plan",
                            tint = if (room.isMap) sasiColor.red500 else sasiColor.green500)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (room.isMap) "Hide from Floor Plan" else "Show on Floor Plan",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            containerColor = sasiColor.grey50,
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Room") },
            text = { Text("Are you sure you want to delete this room? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRoom(room.roomId)
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = sasiColor.red500)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteConfirmation = false },
                    colors = ButtonDefaults.buttonColors(containerColor = sasiColor.red50)
                ) {
                    Text("Cancel", color = sasiColor.red500)
                }
            }
        )
    }
}


@Composable
fun DeviceSelector(devices: List<Device>, onDeviceSelected: (Device) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select a Device") },
        text = {
            LazyVerticalGrid(columns = GridCells.Fixed(3)) {
                items(devices) { device ->
                    Card(onClick = { onDeviceSelected(device) }, modifier = Modifier.padding(4.dp)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(device.name)
                        }
                    }
                }
            }
        },
        confirmButton = { }
    )
}



