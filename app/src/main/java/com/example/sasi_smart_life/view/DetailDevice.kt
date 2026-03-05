package com.example.sasi_smart_life.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.sasi_smart_life.data.models.Device
import com.example.sasi_smart_life.data.models.DeviceCategory
import com.example.sasi_smart_life.data.models.DoorSensorLog
import com.example.sasi_smart_life.data.models.Schedule
import com.example.sasi_smart_life.data.models.SmartLockLog
import com.example.sasi_smart_life.view.theme.sasiColor
import com.example.sasi_smart_life.viewModel.MainViewModel
import com.example.sasi_smart_life.viewModel.TuyaViewModel
import com.example.sasi_smart_life.viewModel.WifiSignalUiState

// Data class to hold context for the Time Picker
data class TimePickerContext(val day: String, val type: String) // type can be "On" or "Off"

@Composable
fun DetailDeviceDialog(
    device: Device,
    viewModel: MainViewModel,
    tuyaViewModel: TuyaViewModel,
    devId: String?,
    onBack: () -> Unit,
    categories: List<DeviceCategory>,
){
    val appState by viewModel.uiState.collectAsState()
    val wifiState by tuyaViewModel.wifiSignalState.collectAsState()
    val userMap by viewModel.userMappingState.collectAsState()
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

    var showEditDialog by remember { mutableStateOf(false) }
    var deviceToEdit by remember { mutableStateOf<Device?>(null) }

    if (showEditDialog && deviceToEdit != null) {
        EditDeviceDialog(
            mainViewModel = viewModel,
            currentName = deviceToEdit!!.name,
            currentCategory = deviceToEdit!!.category,
            onDismissRequest = { showEditDialog = false },
            onSave = { newName, newCategory ->
                viewModel.updateDevice(deviceToEdit!!.devId, newName, newCategory)
                showEditDialog = false
            }
        )
    }

    if (wifiState.isLoading || wifiState.signalValue != null || wifiState.error != null) {
        WifiResultDialog(
            state = wifiState,
            onDismiss = { tuyaViewModel.resetWifiSignalState() },
            onRetry = { tuyaViewModel.checkWifiSignal(currentDevice.devId) }
        )
    }

    LaunchedEffect(devId) {
        viewModel.loadDeviceUsers(currentDevice.devId)
    }

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
                            Row {
                                Text(currentDevice.name, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = {
                                        showEditDialog = true
                                        deviceToEdit = currentDevice
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = sasiColor.yellow500,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = categoryName ?: "Tuya Device",
                                style = MaterialTheme.typography.labelMedium,
                                color = sasiColor.black300,
                            )
                            if (device.isTuya) {
                                Row {
                                    Text(
                                        text = "MAC: ${currentDevice.tuyaInfo?.mac}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = sasiColor.black300,
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Button(onClick = { tuyaViewModel.checkWifiSignal(currentDevice.devId) }) {
                                        Text("Cek Sinyal Wifi")
                                    }
                                }
                            }
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
//                    Text(
//                        "Unlink",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = sasiColor.red500,
//                        modifier = Modifier
//                            .clickable { }
//                            .padding(horizontal = 12.dp),
//                        textDecoration = TextDecoration.Underline
//                    )
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
                    AccessTabContent(
                        userMap = userMap,
                        mainViewModel = viewModel,
                        devId = currentDevice.devId,
                    )
                }
            }
        }
    }
}

@Composable
fun WifiResultDialog(
    state: WifiSignalUiState,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        icon = {
            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.size(36.dp), color = sasiColor.purple500)
                state.error != null -> Icon(Icons.Default.SignalWifiOff, contentDescription = null, tint = sasiColor.red500)
                else -> Icon(Icons.Default.SignalWifi4Bar, contentDescription = null, tint = sasiColor.green500) // Pastikan ada warna ini atau pakai Color.Green
            }
        },
        title = {
            val titleText = when {
                state.isLoading -> "Memeriksa Sinyal..."
                state.error != null -> "Gagal"
                else -> "Kuat Sinyal"
            }
            Text(text = titleText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                if (state.isLoading) {
                    Text("Sedang menghubungi device...", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tips: Jika sensor baterai, pastikan layar menyala / trigger sensor.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                } else if (state.error != null) {
                    Text(state.error, color = sasiColor.red500, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Device mungkin Offline/Sleep.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                } else {
                    // HASIL SUKSES
                    Text(
                        text = state.signalValue ?: "Unknown",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = sasiColor.purple500
                    )
                    Text("Skala 0-100", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        },
        confirmButton = {
            if (!state.isLoading) {
                TextButton(onClick = onDismiss) {
                    Text("Tutup", color = sasiColor.blue500)
                }
            }
        },
        dismissButton = {
            if (state.error != null) {
                TextButton(onClick = onRetry) {
                    Text("Coba Lagi", color = sasiColor.blue500)
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDeviceDialog(
    mainViewModel: MainViewModel,
    currentName: String,
    currentCategory: String?,
    onDismissRequest: () -> Unit,
    onSave: (newName: String, newCategory: String) -> Unit
) {

    val appState by mainViewModel.uiState.collectAsState()

    val initialCategory = remember(appState.categories, currentCategory) {
        appState.categories.find { it.categoryId == currentCategory }
    }

    var name by remember { mutableStateOf(currentName) }
    var selectedCategory by remember { mutableStateOf<DeviceCategory?>(initialCategory) }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    val colorTextfield = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = sasiColor.purple500,
        unfocusedBorderColor = sasiColor.black300,
        cursorColor = sasiColor.purple500,
        focusedLabelColor = sasiColor.black300,
    )

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
                    text = "Edit Device Information",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Column (modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Enter Location Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = sasiColor.purple500,
                            unfocusedBorderColor = sasiColor.black300,
                            cursorColor = sasiColor.purple500,
                            focusedLabelColor = sasiColor.black300,
                        )
                    )
                    ExposedDropdownMenuBox(
                        expanded = isCategoryDropdownExpanded,
                        onExpandedChange = { isCategoryDropdownExpanded = !isCategoryDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = colorTextfield
                        )
                        ExposedDropdownMenu(
                            expanded = isCategoryDropdownExpanded,
                            onDismissRequest = { isCategoryDropdownExpanded = false },
                            containerColor = sasiColor.grey50,
                        ) {
                            appState.categories.forEach { category ->
                                DropdownMenuItem(
                                    leadingIcon = {
                                        AsyncImage(
                                            model = category.imageUrlOn,
                                            contentDescription = category.name,
                                            modifier = Modifier.size(24.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    },
                                    text = { Text(category.name) },
                                    onClick = {
                                        selectedCategory = category
                                        isCategoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
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
                            if (name.isNotBlank()) {
                                onSave(name, selectedCategory?.categoryId ?: "")
                                onDismissRequest()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = sasiColor.blue500),
                        enabled = name.isNotBlank()
                    ) {
                        Text("Save", color = Color.White)
                    }
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
fun AccessTabContent(
    userMap: Map<String, String>,
    mainViewModel: MainViewModel,
    devId: String,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<Pair<String, String>?>(null) }

    if (showDeleteDialog && selectedUser != null) {
        val (userId, userName) = selectedUser!!

        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "Hapus User?") },
            text = {
                Text("Apakah Anda yakin ingin menghapus akses untuk '$userName' (ID: $userId)? Riwayat log tidak akan hilang, hanya nama mappingnya.")
            },
            containerColor = Color.White,
            confirmButton = {
                Button(
                    onClick = {
                        mainViewModel.deleteDeviceUser(devId, userId)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = sasiColor.red500)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = sasiColor.red50)
                ) {
                    Text("Batal", color = sasiColor.red500)
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (userMap.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada user terdaftar.", color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3), // 🔥 Request Anda: 3 Kolom
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
                    .padding(vertical = 12.dp)
            ) {
                // Kita ubah Map jadi List agar bisa masuk ke items()
                items(userMap.toList()) { (userId, userName) ->
                    UserGridItem(
                        userId = userId,
                        userName = userName,
                        onClick = {
                            selectedUser = userId to userName
                            showAddEditDialog = true
                        },
                        onLongClick = {
                            selectedUser = userId to userName
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
        FloatingActionButton(
            onClick = {
                selectedUser = null
                showAddEditDialog = true
            },
            containerColor = sasiColor.blue500,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add,
                contentDescription = "Tambah User",
                tint = sasiColor.blue50)
        }
    }

    if (showAddEditDialog) {
        // Tentukan nilai awal: Kalau selectedUser null berarti Mode Add (kosong)
        val initialId = selectedUser?.first ?: ""
        val initialName = selectedUser?.second ?: ""

        AddEditUserDialog(
            initialId = initialId,
            initialName = initialName,
            onDismiss = { showAddEditDialog = false },
            onSave = { newId, newName ->
                // Panggil ViewModel untuk simpan ke Firebase
                mainViewModel.addDeviceUser(devId, newId, newName)
                showAddEditDialog = false
            }
        )
    }
}

@Composable
fun AddEditUserDialog(
    initialId: String = "",
    initialName: String = "",
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var id by remember { mutableStateOf(initialId) }
    var name by remember { mutableStateOf(initialName) }
    val isEditMode = initialId.isNotEmpty()

    AlertDialog(
        modifier = Modifier.fillMaxWidth(0.5f),
        onDismissRequest = onDismiss,
        title = { Text(text = if (isEditMode) "Edit User" else "Add User") },
        containerColor = Color.White,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = id,
                    onValueChange = { if (!isEditMode) id = it },
                    label = { Text("ID User") },
                    placeholder = { Text("ID from Smart Lock") },
                    singleLine = true,
                    readOnly = isEditMode,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = sasiColor.purple500,
                        unfocusedBorderColor = sasiColor.black300,
                        cursorColor = sasiColor.purple500,
                        focusedLabelColor = sasiColor.black300,
                    ),
                )

                // Input Nama
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = sasiColor.purple500,
                        unfocusedBorderColor = sasiColor.black300,
                        cursorColor = sasiColor.purple500,
                        focusedLabelColor = sasiColor.black300,
                    ),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (id.isNotEmpty() && name.isNotEmpty()) {
                        onSave(id, name)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = sasiColor.purple500)
            ) {
                Text("Simpan", color = sasiColor.purple50)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = sasiColor.black500)
            }
        }
    )
}

@Composable
fun UserGridItem(
    userId: String,
    userName: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val isNamed = userName != userId && !userName.contains("Unknown", ignoreCase = true)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isNamed) Color(0xFFE3F2FD) else Color(0xFFEEEEEE)), // Biru vs Abu
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isNamed) Icons.Default.Person else Icons.Default.PersonOutline,
                    contentDescription = null,
                    tint = if (isNamed) Color(0xFF1565C0) else Color.Gray
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = userName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 3. USER ID (Kecil)
                Text(
                    text = "ID: $userId",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
        }
    }
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