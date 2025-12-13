package com.example.sasi_smart_life.view

import android.inputmethodservice.Keyboard
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.sasi_smart_life.data.models.Device
import com.example.sasi_smart_life.data.models.DeviceCategory
import com.example.sasi_smart_life.data.models.DoorSensorLog
import com.example.sasi_smart_life.data.models.Schedule
import com.example.sasi_smart_life.data.models.SmartLockLog
import com.example.sasi_smart_life.view.theme.sasiColor
import com.example.sasi_smart_life.viewModel.MainViewModel

// Data class to hold context for the Time Picker
data class TimePickerContext(val day: String, val type: String) // type can be "On" or "Off"

@Composable
fun DetailDeviceDialog(
    device: Device,
    viewModel: MainViewModel,
    devId: String?,
    onBack: () -> Unit,
    categories: List<DeviceCategory>,
){
    val appState by viewModel.uiState.collectAsState()
    val currentDevice = appState.devices.find { it.devId == devId }
    if (currentDevice == null) {
        Dialog(onDismissRequest = onBack) {
            Card(modifier = Modifier.padding(16.dp)) {
                Text(text = "Device not found.", modifier = Modifier.padding(16.dp))
            }
        }
        return
    }
    val categoryData = categories.find { it.categoryId == device.category }
    val finalImageUrl = if (device.isTuya && !device.tuyaInfo?.iconUrl.isNullOrEmpty()) {
        device.tuyaInfo.iconUrl
    } else {
        categoryData?.imageUrlOn
    }
    val categoryName = categories.find { it.categoryId == currentDevice.category }?.name

    Dialog(onDismissRequest = onBack) {
        Card(
            modifier = Modifier
                .fillMaxWidth(1f)
                .fillMaxHeight(0.8f),
            colors = CardDefaults.cardColors(containerColor = sasiColor.grey50),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "Device Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth()
                )
                // Header Info Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(1.dp, Color(0xFFCCC1FF)), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFF7F8FF),
                                        Color(0xFF9F90F4)
                                    )
                                )
                            )
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = finalImageUrl,
                            contentDescription = "Device Image",
                            modifier = Modifier
                                .size(60.dp)
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .weight(0.1f)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(0.65f)) {
                            Text(currentDevice.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = categoryName ?: "Tuya Device",
                                style = MaterialTheme.typography.labelMedium,
                                color = sasiColor.black300,
                            )
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            modifier = Modifier.weight(0.25f)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Bolt,
                                        contentDescription = "Usage Icon",
                                        tint = sasiColor.yellow50,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(shape = CircleShape)
                                            .background(sasiColor.yellow500)
                                            .padding(2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Column(verticalArrangement = Arrangement.Center) {
                                        Text(
                                            "This Month",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                platformStyle = PlatformTextStyle(
                                                    includeFontPadding = false
                                                )
                                            ),
                                            color = Color.Gray
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                "825.40",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                " kWh",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                                HorizontalDivider(color = sasiColor.grey600)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "IDR. 700.000",
                                    color = sasiColor.purple500,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        platformStyle = PlatformTextStyle(
                                            includeFontPadding = false
                                        )
                                    )
                                )
                            }
                        }
                    }
                }

                var selectedTab by remember { mutableStateOf("Information") }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(){
                        val buttonShape = RoundedCornerShape(50)
                        Text(
                            "Information",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (selectedTab == "Information") sasiColor.blue50 else sasiColor.black300,
                            modifier = Modifier
                                .clip(buttonShape)
                                .clickable { selectedTab = "Information" }
                                .background(if (selectedTab == "Information") sasiColor.blue500 else Color.Transparent)
                                .border(
                                    BorderStroke(
                                        1.dp,
                                        if (selectedTab == "Information") Color.Transparent else sasiColor.grey600
                                    ),
                                    buttonShape
                                )
                                .padding(horizontal = 12.dp),)
                        Spacer(modifier = Modifier.width(8.dp))
                        if (device.isTuya){
                            if(device.tuyaInfo?.category == "ms" ){
                                Text(
                                    "Access",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (selectedTab == "Access") sasiColor.blue50 else sasiColor.black300,
                                    modifier = Modifier
                                        .clip(buttonShape)
                                        .clickable { selectedTab = "Access" }
                                        .background(if (selectedTab == "Access") sasiColor.blue500 else Color.Transparent)
                                        .border(
                                            BorderStroke(
                                                1.dp,
                                                if (selectedTab == "Access") Color.Transparent else sasiColor.grey600
                                            ),
                                            buttonShape
                                        )
                                        .padding(horizontal = 12.dp)
                                )
                            }
                        } else {
                            Text(
                                "Schedule",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (selectedTab == "Schedule") sasiColor.blue50 else sasiColor.black300,
                                modifier = Modifier
                                    .clip(buttonShape)
                                    .clickable { selectedTab = "Schedule" }
                                    .background(if (selectedTab == "Schedule") sasiColor.blue500 else Color.Transparent)
                                    .border(
                                        BorderStroke(
                                            1.dp,
                                            if (selectedTab == "Schedule") Color.Transparent else sasiColor.grey600
                                        ),
                                        buttonShape
                                    )
                                    .padding(horizontal = 12.dp)
                            )
                        }

                    }
                    Text(
                        "Unlink",
                        style = MaterialTheme.typography.bodySmall,
                        color = sasiColor.red500,
                        modifier = Modifier
                            .clickable { }
                            .padding(horizontal = 12.dp),
                        textDecoration = TextDecoration.Underline
                    )
                }

                if (selectedTab == "Schedule") {
                    ScheduleTab(
                        modifier = Modifier.weight(1f),
                        device = currentDevice,
                        viewModel = viewModel
                    )
                }

                if (selectedTab == "Information" && currentDevice.isTuya == true) {
                    if(currentDevice.tuyaInfo?.category == "ms"){
                        SmartLockInformation(
                            device = currentDevice,
                            viewModel = viewModel
                        )
                    }
                    if(currentDevice.tuyaInfo?.category == "mcs"){
                        DoorSensorInformation(
                            device = currentDevice,
                            viewModel = viewModel
                        )
                    }
                } else if (selectedTab == "Information") {
                    InformationTabContent(
                        modifier = Modifier.weight(1f),
                        device = currentDevice,
                        categories = categories,
                        viewModel = viewModel
                    )
                }

                if(selectedTab == "Access"){
                    AccessTabContent()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleTab(
    modifier: Modifier = Modifier,
    device: Device,
    viewModel: MainViewModel
) {
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val scheduleMap = device.schedule ?: emptyMap()
    var timePickerContext by remember { mutableStateOf<TimePickerContext?>(null) }

    if (timePickerContext != null) {
        val context = timePickerContext!!
        val initialTime = scheduleMap[context.day]?.let {
            if (context.type == "On") it.On else it.Off
        } ?: "00:00"

        // FIX: Split by '.' and handle potential parsing errors gracefully.
        val timeParts = initialTime.split('.').mapNotNull { it.toIntOrNull() }
        val initialHour = timeParts.getOrNull(0) ?: 0
        val initialMinute = timeParts.getOrNull(1) ?: 0

        TimePickerDialog(
            initialHour = initialHour,
            initialMinute = initialMinute,
            onDismissRequest = { timePickerContext = null },
            onTimeSelected = { hour, minute ->
                val newTime = String.format("%02d.%02d", hour, minute) // Save with dot
                val update = mapOf(context.day to mapOf(context.type to newTime))
                viewModel.updateDeviceSchedule(device.devId, update)
                timePickerContext = null
            }
        )
    }

    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items(days) { day ->
            ScheduleItem(
                day = day,
                schedule = scheduleMap[day],
                onStatusClick = {
                    val currentStatus = scheduleMap[day]?.Status ?: 0
                    val newStatus = if (currentStatus == 1) 0 else 1
                    val update = mapOf(day to mapOf("Status" to newStatus))
                    viewModel.updateDeviceSchedule(device.devId, update)
                },
                onTimeClick = { type ->
                    timePickerContext = TimePickerContext(day, type)
                }
            )
        }
    }
}

@Composable
fun ScheduleItem(
    day: String,
    schedule: Schedule?,
    onStatusClick: () -> Unit,
    onTimeClick: (type: String) -> Unit
) {
    val status = schedule?.Status == 1
    val textColor = if (status) sasiColor.blue50 else sasiColor.black500
    val bg = if(status) sasiColor.blue500 else Color.Transparent


    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                //Icon(Icons.Default.CalendarToday, contentDescription = "Day", tint = Color.Gray)
                Text(
                    text = day,
                    color = textColor,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.clip(shape = RoundedCornerShape(50))
                        .background(bg)
                        .padding(horizontal = 16.dp, vertical = 2.dp)
                        .clickable { onStatusClick() }
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.Timer, contentDescription = "On", tint = sasiColor.green800,
                        modifier = Modifier.size(16.dp)
                    )
                    Text("ON", style = MaterialTheme.typography.labelSmall)
                    Text(
                        schedule?.On ?: "--:--",
                        fontWeight = FontWeight.Bold,
                        color = sasiColor.green500,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            )
                        ),
                        modifier = Modifier.clip(RoundedCornerShape(4.dp))
                            .background(sasiColor.grey500)
                            .padding(6.dp)
                            .clickable { onTimeClick("On") }
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.Timer, contentDescription = "Off", tint = sasiColor.red800,
                        modifier = Modifier.size(16.dp)
                    )
                    Text("OFF", style = MaterialTheme.typography.labelSmall)
                    Text(
                        schedule?.Off ?: "--:--",
                        fontWeight = FontWeight.Bold,
                        color = sasiColor.red500,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            )
                        ),
                        modifier = Modifier.clip(RoundedCornerShape(4.dp))
                            .background(sasiColor.grey500)
                            .padding(6.dp)
                            .clickable { onTimeClick("Off") }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismissRequest: () -> Unit,
    onTimeSelected: (hour: Int, minute: Int) -> Unit
) {
    val timePickerState = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute, is24Hour = true)

    Dialog(onDismissRequest = onDismissRequest) {
        Card (
            colors = CardDefaults.cardColors(containerColor = sasiColor.grey50),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Select Time", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = sasiColor.purple50,
                        clockDialSelectedContentColor = sasiColor.purple50,
                        selectorColor = sasiColor.purple500,
                        timeSelectorSelectedContainerColor = sasiColor.purple500,
                        timeSelectorSelectedContentColor = sasiColor.purple50,
                        timeSelectorUnselectedContainerColor = sasiColor.purple300,
                        timeSelectorUnselectedContentColor = sasiColor.purple50
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onDismissRequest,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = sasiColor.blue500
                        )
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onTimeSelected(timePickerState.hour, timePickerState.minute) }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}


@Composable
private fun Information(device: Device, categories: List<DeviceCategory>){
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(device.nodes) { node ->
            val category = categories.find { it.categoryId == node.categoryId }
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = sasiColor.grey50),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, sasiColor.grey600)

            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = category?.imageUrlOn,
                        contentDescription = "Node Image",
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = category?.name ?: "Unknown",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Composable
fun InformationTabContent(
    modifier: Modifier = Modifier,
    device: Device,
    categories: List<DeviceCategory>,
    viewModel: MainViewModel
) {
    var showAddNodeDialog by remember { mutableStateOf(false) }

    if (showAddNodeDialog) {
        AddNodeDialog(
            categories = categories,
            onDismissRequest = { showAddNodeDialog = false },
            onCategorySelected = { category ->
                viewModel.addNodeToDevice(device.devId, category.categoryId)
                showAddNodeDialog = false
            }
        )
    }

    Column(modifier = modifier) {
        Box(modifier = Modifier.weight(1f)) {
            Information(device = device, categories = categories)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { showAddNodeDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = sasiColor.blue500)
        ) {
            Text("Add New Node")
        }
    }
}

@Composable
fun AddNodeDialog(
    categories: List<DeviceCategory>,
    onDismissRequest: () -> Unit,
    onCategorySelected: (DeviceCategory) -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier.fillMaxHeight(0.7f),
            colors = CardDefaults.cardColors(containerColor = sasiColor.grey50)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Select Category for New Node",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        Card(
                            modifier = Modifier.clickable { onCategorySelected(category) },
                            colors = CardDefaults.cardColors(containerColor = sasiColor.grey50),
                            elevation = CardDefaults.cardElevation(2.dp),
                            border = BorderStroke(0.5.dp, sasiColor.grey600)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                AsyncImage(
                                    model = category?.imageUrlOn,
                                    contentDescription = "Node Image",
                                    modifier = Modifier.size(30.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    category.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SmartLockInformation(
    device: Device,
    viewModel: MainViewModel
) {
    LaunchedEffect(device.devId) {
        viewModel.listenToSmartLockLogs(device.devId)
    }

    // 2. Ambil Data
    val logs by viewModel.smartLockLogs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // --- List History ---
        if (logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada riwayat akses", color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs) { log ->
                    SmartLockLogItem(log)
                }
            }
        }
    }
}

@Composable
fun SmartLockLogItem(log: SmartLockLog) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. IKON (Berdasarkan Method)
            val iconConfig = when (log.method) {
                "CARD" -> Pair(Icons.Default.CreditCard, Color(0xFF3F51B5))
                "FINGERPRINT" -> Pair(Icons.Default.Fingerprint, Color(0xFFE91E63))
                "PASSWORD" -> Pair(Icons.Default.Pin, Color(0xFFFF9800))
                "APP" -> Pair(Icons.Default.Smartphone, Color(0xFF4CAF50))
                else -> Pair(Icons.Default.LockOpen, Color.Gray)
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color = Color.White, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconConfig.first,
                    contentDescription = null,
                    tint = iconConfig.second,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 2. DESKRIPSI & WAKTU
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = log.time,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun AccessTabContent(){
    Text("Access Tab Content")
}

@Composable
fun DoorSensorInformation(
    device: Device,
    viewModel: MainViewModel
) {
    LaunchedEffect(device.devId) {
        viewModel.listenToDoorSensorLogs(device.devId)
    }

    val logs by viewModel.doorSensorLogs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // --- List History ---
        if (logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada riwayat akses", color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs) { log ->
                    DoorSensorLogItem(log)
                }
            }
        }
    }
}

@Composable
fun DoorSensorLogItem(log: DoorSensorLog) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Spacer(modifier = Modifier.width(12.dp))

            // 2. DESKRIPSI & WAKTU
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = log.time,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}