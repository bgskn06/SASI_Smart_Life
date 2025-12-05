package com.example.sasi_smart_life.view.master

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.sasi_smart_life.R
import com.example.sasi_smart_life.data.models.SmartScene
import com.example.sasi_smart_life.view.theme.sasiColor
import com.thingclips.smart.sdk.bean.DeviceBean
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.collections.component1
import kotlin.collections.component2


@Composable
private fun Information(device: DeviceBean){
    Column (modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row (Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Card (modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(sasiColor.purple50)) {
                Text("Detail Device",
                    style = MaterialTheme.typography.titleSmall,
                    color = sasiColor.purple500,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
            }
            Card (modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(sasiColor.purple50)) {
                Text("Device Information",
                    style = MaterialTheme.typography.titleSmall,
                    color = sasiColor.purple500,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
            }
        }
        Row (modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Card(modifier = Modifier.weight(1f).fillMaxSize(),
                colors = CardDefaults.cardColors(sasiColor.grey50),
                elevation = CardDefaults.cardElevation(2.dp)){
                Column (modifier = Modifier.padding(10.dp)) {
                    device.dps?.forEach { (dpId, dpValue) ->
                        Text("DP $dpId: $dpValue", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Card(modifier = Modifier.weight(1f).fillMaxSize(),
                colors = CardDefaults.cardColors(sasiColor.grey50),
                elevation = CardDefaults.cardElevation(2.dp)){
                Column (modifier = Modifier.padding(10.dp)) {
                    val accessType = when (device.accessType) {
                        0 -> "Tuya Device"
                        1 -> "Matter Device"
                        2 -> "TuyaLink Device"
                        else -> "Unknown"
                    }
                    val capability = when (device.productBean.capability) {
                        0 -> "Wi-Fi"
                        1 -> "wired"
                        2 -> "General Packet Radio Service (GPRS)"
                        3 -> "Narrowband Internet of Things (NB-IoT)"
                        10 -> "Bluetooth"
                        11 -> "Bluetooth mesh"
                        12 -> "Zigbee"
                        13 -> "infrared"
                        14 -> "pairing over Zigbee)"
                        else -> "${device.productBean.capability}"
                    }
                    val readableTime = convertTime(device.time, device.timezoneId ?: "Asia/Jakarta")

                    Text("devId = ${device.devId}", style = MaterialTheme.typography.bodySmall)
                    Text("mac = ${device.mac}", style = MaterialTheme.typography.bodySmall)
                    Text("ip = ${device.ip}", style = MaterialTheme.typography.bodySmall)
                    Text("name = ${device.name}", style = MaterialTheme.typography.bodySmall)
                    Text("productId = ${device.productId}", style = MaterialTheme.typography.bodySmall)
                    Text("category = ${device.productBean.category}", style = MaterialTheme.typography.bodySmall)
                    Text("capability = $capability", style = MaterialTheme.typography.bodySmall)
                    Text("resptime = ${device.productBean.resptime}", style = MaterialTheme.typography.bodySmall)
                    Text("time = $readableTime", style = MaterialTheme.typography.bodySmall)
                    Text("accessType = $accessType", style = MaterialTheme.typography.bodySmall)
                    Text("timezoneId = ${device.timezoneId}", style = MaterialTheme.typography.bodySmall)
                    Text("getIsOnline = ${device.isOnline}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

fun convertTime(epochValue: Long, timezoneId: String = "Asia/Jakarta"): String {
    // Deteksi apakah epoch dalam detik atau milidetik
    val epochMillis = if (epochValue < 1_000_000_000_000L) epochValue * 1000 else epochValue

    val date = Date(epochMillis)
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone(timezoneId)

    return sdf.format(date)
}

@Composable
private fun Schedule(day: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Icon(Icons.Default.CalendarToday, contentDescription = "Day", tint = Color.Gray)
                Text(
                    text = day,
                    color = sasiColor.blue50,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.clip(shape = RoundedCornerShape(50))
                        .background(sasiColor.blue500)
                        .padding(horizontal = 16.dp, vertical = 2.dp)
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
                        "08:00",
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
                        "16:00",
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
                    )
                }
            }
        }
    }
}


// SMART SCENE
data class ScheduleData(
    val time: String,
    val days: List<String>
)
@Composable
fun SmartScene(sceneList: List<SmartScene>, onAddSceneClick: () -> Unit) {
    if (sceneList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(painterResource(R.drawable.scene_empty),
                        contentDescription = "Empty Scene",
                        modifier = Modifier.size(64.dp))
                }
                Text(
                    text = "Run Automatically According To Conditions Such As Weather, Device Status And Time",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = onAddSceneClick, // Updated onClick
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = sasiColor.blue50,
                        contentColor = sasiColor.blue500
                    )
                ) {
                    Text(text = "Add Scene")
                }
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(sceneList) { scene ->
                SmartSceneCard(scene = scene)
            }
        }
    }
}

@Composable
fun SmartSceneCard(scene: SmartScene) {
    Card(
        modifier = Modifier
            .height(120.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = sasiColor.grey50)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = scene.name)
        }
    }
}

@Composable
fun AddSmartSceneDialog(onDismissRequest: () -> Unit) {

    var showIfConditionDialog by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var ifSchedule by remember { mutableStateOf<ScheduleData?>(null) }

    var showThenConditionDialog by remember { mutableStateOf(false)}

    if (showIfConditionDialog) {
        IfCondition(
            onDismissRequest = { showIfConditionDialog = false },
            onScheduleClick = {
                showIfConditionDialog = false
                showScheduleDialog = true
            }
        )
    }

    if (showThenConditionDialog) {
        ThenCondition(
            onDismissRequest = { showThenConditionDialog = false },
        )
    }

    if (showScheduleDialog) {
        Schedule(
            onBack = {
                showScheduleDialog = false
                showIfConditionDialog = true
            },
            onConfirm = { schedule ->
                ifSchedule = schedule
                showScheduleDialog = false
            }
        )
    }

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
                        Text("Add Scene", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    // Content
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .weight(1f)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, color = Color(0xFFCCC1FF))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(Color(0xFFF7F8FF), Color(0xFF9F90F4))
                                        )
                                    )
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(modifier = Modifier.size(36.dp)
                                    .background(sasiColor.purple50, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.Repeat, contentDescription = null, tint = sasiColor.purple500)
                                }
                                Text("Create Scene", fontWeight = FontWeight.SemiBold, color = sasiColor.purple500)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // IF Condition
                        Card (
                            colors = CardDefaults.cardColors(containerColor = sasiColor.grey50),
                            border = BorderStroke(1.dp, sasiColor.grey600)
                        ) {
                            Column {
                                Row (modifier = Modifier.fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("If", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Condition",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(sasiColor.blue500)
                                            .clickable { showIfConditionDialog = true }
                                    )
                                }
                                HorizontalDivider(color = sasiColor.grey600)
                                Box(modifier = Modifier.padding(8.dp)){
                                    if (ifSchedule == null){
                                        Text("No Condition Selected",
                                            color = Color.Gray,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(vertical = 8.dp))
                                    } else {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CalendarToday,
                                                contentDescription = null,
                                                tint = sasiColor.blue500,
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .padding(end = 8.dp)
                                            )

                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Text("Schedule", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                                Row(){
                                                    Row(verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                        modifier = Modifier.weight(0.25f)){
                                                        Icon(
                                                            imageVector = Icons.Default.Timer,
                                                            contentDescription = null,
                                                            tint = sasiColor.green500,
                                                        )
                                                        Text(ifSchedule!!.time,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = Color.Gray,
                                                            maxLines = 1)
                                                    }
                                                    Row(verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.weight(0.75f)){
                                                        Icon(
                                                            imageVector = Icons.Outlined.Repeat,
                                                            contentDescription = null,
                                                            tint = sasiColor.purple500,
                                                        )
                                                        Text(ifSchedule!!.days.joinToString(),
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = Color.Gray,
                                                            maxLines = 1,
                                                            modifier = Modifier.weight(1f))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // THEN Action
                        Card (
                            colors = CardDefaults.cardColors(containerColor = sasiColor.grey50),
                            border = BorderStroke(1.dp, sasiColor.grey600)
                        ) {
                            Column {
                                Row (modifier = Modifier.fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Then", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Condition",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(sasiColor.blue500)
                                            .clickable { showThenConditionDialog = true }
                                    )
                                }
                                HorizontalDivider(color = sasiColor.grey600)
                                Box(modifier = Modifier.padding(8.dp)){
                                    Text("No Condition Selected",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(vertical = 8.dp))
                                }
                            }

                        }
                    }

                    // Footer Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = onDismissRequest,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = sasiColor.red50,
                                contentColor = sasiColor.red500
                            )
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = { /*TODO*/ },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = sasiColor.blue500,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IfCondition(onDismissRequest: () -> Unit, onScheduleClick: () -> Unit) {
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismissRequest) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Text("Add Condition", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = sasiColor.grey600)

                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Schedule
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable(onClick = onScheduleClick).padding(vertical = 8.dp),
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
                                Text("Example: at 08.00 at Weekdays", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
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
                    }
                }
            }
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
fun ThenCondition(onDismissRequest: () -> Unit){
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismissRequest) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Text("Add Condition", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = sasiColor.grey600)

                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Schedule
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable(onClick = {}).padding(vertical = 8.dp),
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
                                Text("Device", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Text("Example: turn on the light", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
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
                                imageVector = Icons.Default.Mail,
                                contentDescription = null,
                                tint = sasiColor.green500,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(sasiColor.green50)
                                    .padding(8.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Send Notification", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Text("Example: send a notification when the alarm on", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

