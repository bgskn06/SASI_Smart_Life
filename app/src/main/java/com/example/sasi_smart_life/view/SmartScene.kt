package com.example.sasi_smart_life.view

import android.app.TimePickerDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.sasi_smart_life.R
import com.example.sasi_smart_life.data.models.Device
import com.example.sasi_smart_life.data.models.Room
import com.example.sasi_smart_life.data.models.SmartScene
import com.example.sasi_smart_life.view.theme.sasiColor
import com.example.sasi_smart_life.viewModel.MainViewModel
import java.util.Calendar
import kotlinx.coroutines.delay

data class ScheduleData(val time: String, val days: List<String>)

@Composable
fun SmartScene(
    viewModel: MainViewModel,
    sceneList: List<SmartScene>,
    onAddSceneClick: () -> Unit
) {
    Image(
        painter = painterResource(id = R.drawable.maintenance),
        contentDescription = "Under Maintenance",
        modifier = Modifier.fillMaxSize()
    )
//    Column(modifier = Modifier.fillMaxSize()) {
//        if (sceneList.isEmpty()) {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                    Image(
//                        painter = painterResource(id = R.drawable.scene_empty),
//                        contentDescription = "No scenes yet",
//                        modifier = Modifier.size(200.dp)
//                    )
//                    Text("No scenes yet", style = MaterialTheme.typography.bodyLarge)
//                }
//            }
//        } else {
//            LazyColumn(
//                verticalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                items(sceneList) { scene ->
//                    SceneCard(scene = scene, onExecute = { viewModel.executeScene(scene) })
//                }
//            }
//        }
//    }
//
//    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
//        IconButton(
//            onClick = onAddSceneClick,
//            modifier = Modifier
//                .padding(16.dp)
//                .size(56.dp)
//                .clip(CircleShape)
//                .background(sasiColor.blue500)
//        ) {
//            Icon(Icons.Default.Add, contentDescription = "Add Scene", tint = Color.White)
//        }
//    }
}

@Composable
fun SceneCard(scene: SmartScene, onExecute: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(scene.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                // Add more details about the scene if needed
            }
            IconButton(onClick = onExecute) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Execute Scene", tint = sasiColor.blue500)
            }
        }
    }
}

@Composable
fun AddSmartSceneDialog(
    devices: List<Device>,
    rooms: List<Room>,
    onDismissRequest: () -> Unit,
    onSave: (name: String, ifData: Map<String, Any>, thenActions: List<Map<String, Any>>) -> Unit
) {

    // --- State for the new scene being built ---
    var sceneName by remember { mutableStateOf("") }
    var ifCondition by remember { mutableStateOf<Map<String, Any>?>(null) }
    var thenActions by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var scheduleData by remember { mutableStateOf<ScheduleData?>(null) }

    // --- State for controlling sub-dialogs ---
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showAddActionDialog by remember { mutableStateOf(false) }

    // --- Sub-Dialogs ---
    if (showScheduleDialog) {
        Schedule(
            onBack = { showScheduleDialog = false },
            onConfirm = { data ->
                scheduleData = data
                ifCondition = mapOf(
                    "type" to "schedule",
                    "time" to data.time,
                    "days" to data.days
                )
                showScheduleDialog = false
            }
        )
    }

    if (showAddActionDialog) {
        SelectDeviceActionDialog(
            devices = devices,
            rooms = rooms,
            onDismissRequest = { showAddActionDialog = false },
            onActionSelected = { newAction ->
                thenActions = thenActions + newAction // Add the new action to the list
                showAddActionDialog = false
            }
        )
    }

    // --- Main Dialog UI ---
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
                colors = CardDefaults.cardColors(containerColor = sasiColor.grey50)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismissRequest) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Text("Add Smart Scene", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        TextButton(onClick = {
                            if (sceneName.isNotBlank() && ifCondition != null && thenActions.isNotEmpty()) {
                                onSave(sceneName, ifCondition!!, thenActions)
                            }
                        }) {
                            Text("Save", color = sasiColor.blue500, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = sasiColor.grey600)

                    // Content
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Scene Name
                        OutlinedTextField(
                            value = sceneName,
                            onValueChange = { sceneName = it },
                            label = { Text("Scene Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // --- IF Condition ---
                        Text("IF", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        // Schedule
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = { showScheduleDialog = true })
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = sasiColor.blue500,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(sasiColor.blue50)
                                    .padding(8.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Schedule", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                val scheduleText = if (scheduleData != null) {
                                    val days = if (scheduleData!!.days.size == 7) "Everyday" else scheduleData!!.days.joinToString(", ")
                                    "At ${scheduleData!!.time} on $days"
                                } else {
                                    "Example: at 08.00 at Weekdays"
                                }
                                Text(scheduleText, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                        }

                        HorizontalDivider(color = sasiColor.grey600)

                        // When Device Status Change
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { /* TODO */ }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = sasiColor.yellow500,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(sasiColor.yellow50)
                                    .padding(8.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text("When Device Status Change", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Text("Example: when cctv detect an unusual activity", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                        }

                        HorizontalDivider(color = sasiColor.grey600)

                        // --- THEN Actions ---
                        Text("THEN", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        LazyColumn(modifier = Modifier.height(100.dp)) { // Limit height for scroll
                            items(thenActions) { action ->
                                ActionItem(action = action, devices = devices, rooms = rooms)
                            }
                        }

                        Button(onClick = { showAddActionDialog = true }) {
                            Text("Add Action")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionItem(action: Map<String, Any>, devices: List<Device>, rooms: List<Room>) {
    val devId = action["devId"] as? String
    val status = action["status"] as? Boolean
    val device = devices.find { it.devId == devId }

    if (device != null && status != null) {
        val room = rooms.find { it.roomId == device.roomId }?.name ?: ""
        val actionText = "Turn ${if (status) "On" else "Off"} ${device.name} in $room"
        Row(
            modifier = Modifier.padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = sasiColor.yellow500)
            Text(actionText, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun Schedule(onBack: () -> Unit, onConfirm: (ScheduleData) -> Unit) {

    var selectedTime by remember { mutableStateOf("Select Time") }
    var showTimePicker by remember { mutableStateOf(false) }
    var showRepeatDialog by remember { mutableStateOf(false) }
    var selectedDays by remember { mutableStateOf(listOf("Select Day")) }
    val context = LocalContext.current

    if (showTimePicker) {
        val calendar = Calendar.getInstance()
        val initialHour = calendar.get(Calendar.HOUR_OF_DAY)
        val initialMinute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            context,
            { _, hour: Int, minute: Int ->
                val amPm = if (hour < 12) "AM" else "PM"
                val formattedHour = if (hour % 12 == 0) 12 else hour % 12
                selectedTime = String.format("%02d:%02d %s", formattedHour, minute, amPm)
                showTimePicker = false
            },
            initialHour,
            initialMinute,
            false // 12-hour format
        ).apply {
            setOnDismissListener { showTimePicker = false }
            show()
        }
    }

    if (showRepeatDialog) {
        Repeat(
            onBack = { showRepeatDialog = false },
            onConfirm = { days ->
                selectedDays = days
                showRepeatDialog = false
            }
        )
    }

    Dialog(
        onDismissRequest = onBack,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
                colors = CardDefaults.cardColors(containerColor = sasiColor.grey50)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Text("Schedule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        TextButton(onClick = {
                            onConfirm(
                                ScheduleData(
                                    time = selectedTime,
                                    days = selectedDays
                                )
                            )
                        }) {
                            Text("Confirm", color = sasiColor.blue500, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = sasiColor.grey600)

                    // Content
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Time
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { showTimePicker = true }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = sasiColor.green500,
                                modifier = Modifier.size(24.dp)
                            )
                            Text("Time", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            Text(selectedTime, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                        }

                        // Repeat
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showRepeatDialog = true }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Repeat,
                                contentDescription = null,
                                tint = sasiColor.purple500,
                                modifier = Modifier.size(24.dp)
                            )
                            Text("Repeat", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            Text(
                                text = selectedDays.joinToString(", "),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.End
                            )
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Repeat(onBack: () -> Unit, onConfirm: (List<String>) -> Unit) {
    val days = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    var selectedDays by remember { mutableStateOf(emptyList<String>()) }

    Dialog(
        onDismissRequest = onBack,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
                colors = CardDefaults.cardColors(containerColor = sasiColor.grey50)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Text("Repeat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        TextButton(onClick = { onConfirm(selectedDays) }) {
                            Text("Confirm", color = sasiColor.blue500, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = sasiColor.grey600)

                    // Content
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        days.forEach { day ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedDays =
                                            if (selectedDays.contains(day)) {
                                                selectedDays - day
                                            } else {
                                                selectedDays + day
                                            }
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(day, style = MaterialTheme.typography.bodyLarge)
                                if (selectedDays.contains(day)) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = sasiColor.blue500
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectDeviceActionDialog(
    devices: List<Device>,
    rooms: List<Room>,
    onDismissRequest: () -> Unit,
    onActionSelected: (Map<String, Any>) -> Unit
) {
    var selectedDevice by remember { mutableStateOf<Device?>(null) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
                colors = CardDefaults.cardColors(containerColor = sasiColor.grey50)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismissRequest) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Text("Select Device Action", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider(color = sasiColor.grey600)

                    // Device List
                    LazyColumn(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(devices) { device ->
                            val roomName = rooms.find { it.roomId == device.roomId }?.name ?: "Unassigned"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedDevice = device }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(Icons.Default.Lightbulb, contentDescription = null)
                                Text("${device.name} in $roomName", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }

                // --- Confirmation Dialog for Selected Device ---
                if (selectedDevice != null) {
                    Dialog(onDismissRequest = { selectedDevice = null }) {
                        Card(colors = CardDefaults.cardColors(containerColor = sasiColor.grey100)) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text("Select action for ${selectedDevice!!.name}", style = MaterialTheme.typography.titleMedium)
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Button(onClick = {
                                        val action = mapOf(
                                            "action" to "set_status",
                                            "devId" to selectedDevice!!.devId,
                                            "status" to true
                                        )
                                        onActionSelected(action)
                                    }, colors = ButtonDefaults.buttonColors(containerColor = sasiColor.green500)) {
                                        Text("Turn On")
                                    }
                                    Button(onClick = {
                                        val action = mapOf(
                                            "action" to "set_status",
                                            "devId" to selectedDevice!!.devId,
                                            "status" to false
                                        )
                                        onActionSelected(action)
                                    }, colors = ButtonDefaults.buttonColors(containerColor = sasiColor.red500)) {
                                        Text("Turn Off")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
