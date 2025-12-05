package com.example.sasi_smart_life.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.sasi_smart_life.R
import com.example.sasi_smart_life.data.models.Device
import com.example.sasi_smart_life.data.models.DeviceCategory
import com.example.sasi_smart_life.viewModel.MainViewModel
import com.example.sasi_smart_life.viewModel.TuyaPairingViewModel
import com.example.sasi_smart_life.view.theme.sasiColor

@Composable
fun DeviceManagementScreen(
    viewModel: MainViewModel,
    devices: List<Device>,
    roomId: String?,
    onBack: () -> Unit,
) {

    var isPairing by remember { mutableStateOf(false) }
    val appState by viewModel.uiState.collectAsState()

    val currentRoom = appState.rooms.find { it.roomId == roomId }
    val currentHome = appState.selectedHomeId

    var selectedDeviceForDetail by remember { mutableStateOf<Device?>(null) }

    if (selectedDeviceForDetail != null) {
        DetailDeviceDialog(
            viewModel = viewModel,
            devId = selectedDeviceForDetail!!.devId,
            categories = appState.categories,
            onBack = { selectedDeviceForDetail = null }
        )
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(top = 4.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().weight(0.05f)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Device Management",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Box(modifier = Modifier
            .fillMaxWidth()
            .weight(0.15f)
        ) {
            Header(
                homeName = currentHome?.name,
                roomName = currentRoom?.name,
                devices = devices
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier
            .fillMaxWidth()
            .weight(0.8f)
        ) {
            Body(
                devices = devices,
                categories = appState.categories,
                onAddDeviceClicked = { isPairing = true },
                onDeviceClicked = { device ->
                    selectedDeviceForDetail = device
                },
                onStatusChange = { devId, newStatus ->
                    if (roomId != null) {
                        viewModel.setDeviceStatus(devId, roomId, newStatus)
                    }
                }
            )
        }
    }
    if (isPairing) {
        val pairingViewModel: TuyaPairingViewModel = viewModel()
        PairingScreen(
            mainViewModel = viewModel,
            pairingViewModel = pairingViewModel,
            onBack = { isPairing = false },
            roomId = roomId
        )
    }
}

@Composable
private fun Header(homeName: String?, roomName: String?, devices: List<Device>) {
    val totalDeviceCount = devices.size
    val deviceOnCount = devices.count { it.status }
    val deviceOffCount = totalDeviceCount - deviceOnCount

    Row (modifier = Modifier
        .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card (modifier = Modifier.weight(0.8f),
            colors = CardDefaults.cardColors(containerColor = sasiColor.purple50),
            border = BorderStroke(1.dp, sasiColor.purple500)
        ) {
            Row(modifier = Modifier.padding(8.dp).fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column (modifier = Modifier.padding(horizontal = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Card (
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = sasiColor.grey50)
                        ) {
                            Icon(imageVector = Icons.Outlined.Home,
                                contentDescription = "Home Name",
                                modifier = Modifier
                                    .size(30.dp)
                                    .padding(4.dp),
                                tint = sasiColor.purple500)
                        }
                        Text(text = homeName ?: "Home Name", style = MaterialTheme.typography.titleLarge, color = sasiColor.purple500, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = roomName ?: "Room", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = sasiColor.grey50),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Row(modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Energy",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier
                                .size(40.dp)
                                .background(color = Color(0xFFFFC107).copy(alpha = 0.2f), shape = CircleShape)
                                .padding(8.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = "This Month", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text(text = "825.40 kWh", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(text = "IDR. 700.000", style = MaterialTheme.typography.bodyMedium, color = sasiColor.purple500, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
        Card(modifier = Modifier.weight(0.1f),
            colors = CardDefaults.cardColors(containerColor = sasiColor.blue500),
        ) {
            Column(modifier = Modifier.padding(8.dp).fillMaxSize(),
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = "Total Device", color = sasiColor.grey50, style = MaterialTheme.typography.bodySmall)
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = sasiColor.grey50)
                    ) {
                        Icon(imageVector = Icons.Default.Usb,
                            contentDescription = "Total devices",
                            tint = sasiColor.blue500,
                            modifier = Modifier.size(20.dp).padding(2.dp)
                        )
                    }
                    Text(text = "$totalDeviceCount", color = sasiColor.grey50, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
        Column(modifier = Modifier.fillMaxWidth().weight(0.1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(modifier = Modifier.weight(1f).fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = sasiColor.green50),
                border = BorderStroke(1.dp, sasiColor.green500)
            ) {
                Column (modifier = Modifier.padding(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(text = "Device On", style = MaterialTheme.typography.labelSmall, color = sasiColor.green500)
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(sasiColor.green500, CircleShape)
                        )
                    }
                    Text(text = "$deviceOnCount", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = sasiColor.black300)
                }
            }
            Card(modifier = Modifier.weight(1f).fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = sasiColor.grey50),
                border = BorderStroke(1.dp, sasiColor.black300)
            ) {
                Column (modifier = Modifier.padding(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(text = "Device Off", style = MaterialTheme.typography.labelSmall)
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(sasiColor.black300, CircleShape)
                        )
                    }
                    Text(text = "$deviceOffCount", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun Body(
    devices: List<Device>,
    categories: List<DeviceCategory>,
    onAddDeviceClicked: () -> Unit,
    onDeviceClicked: (Device) -> Unit,
    onStatusChange: (devId: String, newStatus: Boolean) -> Unit
) {

    Column(modifier = Modifier.fillMaxSize()) {
        if (devices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(0.9f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Tidak ada device")
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onAddDeviceClicked,
                        colors = ButtonDefaults.buttonColors(containerColor = sasiColor.blue500)
                    ) {
                        Text("Add Device")
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(0.9f),
                columns = GridCells.Fixed(4),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(devices) { device ->
                    DeviceCard(
                        device = device,
                        categories = categories,
                        onClick = { onDeviceClicked(device) },
                        onStatusChange = onStatusChange
                    )
                }
                item {
                    AddDeviceCard(onClick = onAddDeviceClicked)
                }
            }
        }
    }
}

@Composable
private fun AddDeviceCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .height(130.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = sasiColor.blue50),
        border = BorderStroke(1.dp, sasiColor.blue500)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(sasiColor.blue500, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Device",
                    tint = sasiColor.grey50
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add Device",
                color = sasiColor.blue500,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun DeviceCard(
    device: Device,
    categories: List<DeviceCategory>,
    onClick: () -> Unit,
    onStatusChange: (devId: String, newStatus: Boolean) -> Unit
) {
    val categoryImage = categories.find { it.categoryId == device.category }?.imageUrlOn
    val categoryName = categories.find { it.categoryId == device.category }?.name
    val bg = if (device.status) sasiColor.purple300 else sasiColor.grey50
    val textColorPrimary = if (device.status) sasiColor.grey50 else sasiColor.black500
    val textColorSecondary = if (device.status) sasiColor.grey50 else sasiColor.black300

    Card(
        modifier = Modifier
            .height(130.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = sasiColor.blue50),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            colors = CardDefaults.cardColors(containerColor = bg),
            border = BorderStroke(1.dp, sasiColor.grey600)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Left Side: Image
                Surface(
                    modifier = Modifier
                        .size(80.dp)
                        .aspectRatio(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = sasiColor.blue50
                ) {
                    AsyncImage(
                        model = categoryImage,
                        placeholder = painterResource(id = R.drawable.logo_sag),
                        error = painterResource(id = R.drawable.scene_empty),
                        contentDescription = device.name,
                        modifier = Modifier.padding(4.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Right Side: Content
                Column(modifier = Modifier.weight(1f).fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Row (modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ){
                                Text(
                                    text = device.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = textColorPrimary
                                )
                                Switch(
                                    checked = device.status,
                                    onCheckedChange = { newStatus -> onStatusChange(device.devId, newStatus) },
                                    modifier = Modifier.size(20.dp).scale(0.7f).padding(end = 20.dp),
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = sasiColor.grey50,
                                        uncheckedThumbColor = sasiColor.grey50,
                                        checkedTrackColor = sasiColor.green500,
                                        uncheckedTrackColor = sasiColor.black50,
                                        uncheckedBorderColor = sasiColor.black50
                                    )
                                )
                            }
                             Text(
                                 text = categoryName ?: "error",
                                 style = MaterialTheme.typography.bodySmall,
                                 color = textColorSecondary
                             )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val (wifiIcon, wifiColor, wifiText) = if (device.isOnline) {
                                Triple(Icons.Outlined.Wifi, sasiColor.green500, "Online")
                            } else {
                                Triple(Icons.Default.WifiOff, sasiColor.red500, "Offline")
                            }
                            Icon(
                                imageVector = wifiIcon,
                                contentDescription = wifiText,
                                tint = wifiColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(wifiText,
                                style = MaterialTheme.typography.bodySmall,
                                color = textColorSecondary
                            )
                        }
                        Text(
                            "Scheduled", style = MaterialTheme.typography.labelSmall, color = sasiColor.purple50,
                            modifier = Modifier
                                .background(sasiColor.purple500, shape = RoundedCornerShape(50))
                                .padding(vertical = 2.dp, horizontal = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
