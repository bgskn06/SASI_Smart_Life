package com.example.sasi_smart_life.view

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoubleArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.sasi_smart_life.R
import com.example.sasi_smart_life.data.models.Device
import com.example.sasi_smart_life.data.models.DeviceCategory
import com.example.sasi_smart_life.data.models.SmartScene
import com.example.sasi_smart_life.view.theme.sasiColor
import com.example.sasi_smart_life.viewModel.MainViewModel
import java.util.Calendar


@Composable
fun SmartScene(
    device: List<Device>,
    viewModel: MainViewModel,
    sceneList: List<SmartScene>,
    categories: List<DeviceCategory>
) {
//    Image(
//        painter = painterResource(id = R.drawable.maintenance),
//        contentDescription = "Under Maintenance",
//        modifier = Modifier.fillMaxSize()
//    )
    var showDialog by remember { mutableStateOf(false) }
    var sceneToDelete by remember { mutableStateOf<SmartScene?>(null) }

    if (showDialog) {
        AddScene(
            devices = device,
            category = categories,
            onDismiss = { showDialog = false },
            onSave = { newScene ->
                // 1. Panggil ViewModel untuk kirim ke Firebase
                viewModel.addScene(newScene)
                // 2. Setelah selesai, tutup dialog
                showDialog = false
            }
        )
    }

    if (sceneToDelete != null) {
        AlertDialog(
            onDismissRequest = { sceneToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = sasiColor.red500) },
            title = { Text(text = "Delete Scene?") },
            text = { Text(text = "Are you sure you want to delete '${sceneToDelete?.name}'? This action cannot be undone.") },
            containerColor = Color.White,
            confirmButton = {
                Button(
                    onClick = {
                        sceneToDelete?.let { scene ->
                            viewModel.deleteScene(scene.sceneId)
                        }
                        sceneToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = sasiColor.red500)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(
                    onClick =  { sceneToDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = sasiColor.red50)
                ) {
                    Text("Cancel", color = sasiColor.red500)
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (sceneList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.scene_empty),
                        contentDescription = "No scenes yet",
                        modifier = Modifier.size(200.dp)
                    )
                    Text("No scenes yet", style = MaterialTheme.typography.bodyLarge)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sceneList) { scene ->
                    SceneCard(
                        scene = scene,
                        devices = device,
                        categories = categories,
                        onToggle = { isActive ->
                            viewModel.toggleSceneActive(scene.sceneId, isActive)
                        },
                        onLongClick = { sceneToDelete = scene }
                    )
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        IconButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .padding(16.dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(sasiColor.blue500)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Scene", tint = Color.White)
        }
    }
}

@Composable
fun SceneCard(
    scene: SmartScene,
    devices: List<Device>,
    categories: List<DeviceCategory>,
    onToggle: (Boolean) -> Unit,
    onLongClick: () -> Unit
) {
    fun getDeviceName(devId: String?): String {
        return devices.find { it.devId == devId }?.name ?: devId ?: "?"
    }

    fun getCategoryIcon(devId: String?): String? {
        val device = devices.find { it.devId == devId } ?: return null
        val category = categories.find { it.categoryId == device.category }
        return category?.image?.ifEmpty { category?.imageUrlOn }
    }

    val cardAlpha = if (scene.isActive) 1f else 0.6f
    val containerColor = if (scene.isActive) Color.White else Color(0xFFF0F0F0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .combinedClickable(
                onClick = {},
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (scene.isActive) 4.dp else 0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .background(sasiColor.purple50, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoMode,
                            contentDescription = null,
                            tint = sasiColor.purple500
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = scene.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Switch(
                    checked = scene.isActive,
                    onCheckedChange = { onToggle(scene.isActive) },
                    modifier = Modifier.scale(0.7f).height(24.dp),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = sasiColor.purple500,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.LightGray
                    )
                )
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {

                val isTimeEnabled = scene.time["enabled"] as? Boolean ?: false
                val start = scene.time["start"] as? String ?: ""
                val end = scene.time["end"] as? String ?: ""

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(sasiColor.green50, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = sasiColor.green500
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isTimeEnabled) "$start - $end" else "All day",
                        style = MaterialTheme.typography.bodySmall,
                        color = sasiColor.black300,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // IF TRIGGER
                val ifDevId = scene.ifData["devId"] as? String
                val ifStatus = scene.ifData["status"].toString()
                val ifStateText = if (ifStatus == "1" || ifStatus == "true") "ON" else "OFF"

                val firstAction = scene.thenAction.firstOrNull()
                val thenDevId = firstAction?.get("devId") as? String
                val thenStatus = firstAction?.get("status").toString()
                val thenStateText = if (thenStatus == "1" || thenStatus == "true") "ON" else "OFF"

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.basicMarquee(
                        iterations = Int.MAX_VALUE,
                        animationMode = MarqueeAnimationMode.Immediately,
                        initialDelayMillis = 3000,
                        repeatDelayMillis = 3000
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .background(sasiColor.blue50, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        AsyncImage(
                            model = getCategoryIcon(ifDevId),
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${getDeviceName(ifDevId)} ($ifStateText)",
                        style = MaterialTheme.typography.bodySmall,
                        color = sasiColor.black300,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(imageVector = Icons.Default.ArrowRightAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(sasiColor.blue50, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        AsyncImage(
                            model = getCategoryIcon(thenDevId),
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${getDeviceName(thenDevId)} ($thenStateText)",
                        style = MaterialTheme.typography.bodySmall,
                        color = sasiColor.black300,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// Komponen Kecil untuk Baris Info di dalam Card Grid
@Composable
fun MiniInfoRow(label: String, color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .background(color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = label,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 10.sp,
            color = Color.Gray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
)

data class StatusOption(val label: String, val value: Int)

@Composable
fun AddScene(
    devices: List<Device>,
    onDismiss: () -> Unit,
    category: List<DeviceCategory>,
    onSave: (SmartScene) -> Unit
) {
    var sceneName by remember { mutableStateOf("") }

    var ifDevice by remember { mutableStateOf<Device?>(null) }
    var ifStatus by remember { mutableStateOf(1) }

    var isTimeEnabled by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf("00:00") }
    var endTime by remember { mutableStateOf("00:00") }

    var thenDevice by remember { mutableStateOf<Device?>(null) }
    var thenStatus by remember { mutableStateOf(1) }

    val statusOptions = listOf(
        StatusOption("ON", 1),
        StatusOption("OFF", 0)
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Agar bisa full width
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterEnd // POSISI KANAN
        ) {
            // Background Dim (klik luar untuk tutup)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onDismiss() }
                    .background(Color.Black.copy(alpha = 0.5f))
            )

            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.5f)
                    .clickable(enabled = false) {},
                color = Color(0xFFF5F5F5)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Add New Scene", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    HorizontalDivider(
                        color = sasiColor.grey600
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f) // KUNCI: Ambil semua ruang sisa
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp) // Sedikit diperlega agar rapi
                    ) {

                        OutlinedTextField(
                            value = sceneName,
                            onValueChange = { sceneName = it },
                            label = { Text("Scene Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = sasiColor.purple500,
                                unfocusedBorderColor = sasiColor.black300,
                                cursorColor = sasiColor.purple500,
                                focusedLabelColor = sasiColor.black300,
                            )
                        )

                        // --- 2. IF SECTION ---
                        SectionLabel("IF (Trigger)")

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(0.7f)) {
                                DeviceDropdown(
                                    label = "Select Device",
                                    options = devices,
                                    category = category,
                                    selected = ifDevice,
                                    onSelect = { ifDevice = it }
                                )
                            }
                            Box(modifier = Modifier.weight(0.3f)) {
                                StatusDropdown(
                                    selectedValue = ifStatus,
                                    onSelect = { ifStatus = it }
                                )
                            }
                        }

                        SectionLabel("TIME (Validity)")

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Enable Time Restriction")
                                Switch(
                                    checked = isTimeEnabled,
                                    onCheckedChange = { isTimeEnabled = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = sasiColor.grey50,
                                        uncheckedThumbColor = sasiColor.grey50,
                                        checkedTrackColor = sasiColor.green500,
                                        uncheckedTrackColor = sasiColor.black50,
                                        uncheckedBorderColor = sasiColor.black50
                                    )
                                )
                            }
                        }

                        // Field Waktu (Hanya muncul jika switch ON)
                        if (isTimeEnabled) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TimePickerField(
                                    label = "Start Time",
                                    time = startTime,
                                    onTimeSelected = { startTime = it },
                                    modifier = Modifier.weight(1f)
                                )
                                TimePickerField(
                                    label = "End Time",
                                    time = endTime,
                                    onTimeSelected = { endTime = it },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // --- 4. THEN SECTION ---
                        SectionLabel("THEN (Action)")

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Pilih Device (Lebar 70%)
                            Box(modifier = Modifier.weight(0.7f)) {
                                DeviceDropdown(
                                    label = "Select Target",
                                    options = devices,
                                    selected = thenDevice,
                                    category = category,
                                    onSelect = { thenDevice = it }
                                )
                            }
                            // Pilih Status (Lebar 30%)
                            Box(modifier = Modifier.weight(0.3f)) {
                                StatusDropdown(
                                    selectedValue = thenStatus,
                                    onSelect = { thenStatus = it }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (ifDevice != null && thenDevice != null) {
                                val newScene = SmartScene(
                                    sceneId = System.currentTimeMillis().toString(),
                                    name = sceneName,
                                    isActive = true,
                                    ifData = mapOf(
                                        "devId" to ifDevice!!.devId,
                                        "status" to ifStatus
                                    ),
                                    time = if (isTimeEnabled) mapOf(
                                        "start" to startTime,
                                        "end" to endTime,
                                        "enabled" to true
                                    ) else emptyMap(),
                                    thenAction = listOf(
                                        mapOf(
                                            "devId" to thenDevice!!.devId,
                                            "status" to thenStatus
                                        )
                                    )
                                )
                                onSave(newScene)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(sasiColor.purple500)
                    ) {
                        Text("SAVE SCENE", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDropdown(
    label: String,
    options: List<Device>,
    category: List<DeviceCategory>,
    selected: Device?,
    onSelect: (Device) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val findCategoryForDevice: (Device) -> DeviceCategory? = { device ->
        category.find { it.categoryId == device.category }
    }
    val selectedCategory = selected?.let { findCategoryForDevice(it) }


    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            leadingIcon = if (selectedCategory != null) {
                {
                    AsyncImage(
                        model = selectedCategory.image.ifEmpty { selectedCategory.imageUrlOn }, // Fallback jika imageUrlOn kosong
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else null,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = sasiColor.purple500,
                unfocusedBorderColor = sasiColor.black300,
                cursorColor = sasiColor.purple500,
                focusedLabelColor = sasiColor.black300,
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = sasiColor.grey300,
            border = BorderStroke(1.dp, sasiColor.purple500),
        ) {
            options.forEach { device ->
                val category = findCategoryForDevice(device)
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // --- GAMBAR DEVICE ---
                            AsyncImage(
                                model = category?.image?.ifEmpty { category.imageUrlOn },
                                contentDescription = null,
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(end = 8.dp),
                            )
                            Text(device.name)
                        }
                    },
                    onClick = {
                        onSelect(device)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusDropdown(
    selectedValue: Int,
    onSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("OFF" to 0, "ON" to 1)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = if(selectedValue == 1) "ON" else "OFF",
            onValueChange = {},
            readOnly = true,
            label = { Text("Status") },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = sasiColor.purple500,
                unfocusedBorderColor = sasiColor.black300,
                cursorColor = sasiColor.purple500,
                focusedLabelColor = sasiColor.black300,
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = sasiColor.grey300,
            border = BorderStroke(1.dp, sasiColor.purple500),
        ) {
            options.forEach { (label, value) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onSelect(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun TimePickerField(
    label: String,
    time: String,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // Parsing jam yang ada (misal "22:00")
    val parts = time.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

    val timePickerDialog = TimePickerDialog(
        context,
        { _, h, m ->
            // Format HH:mm (contoh: 05:00)
            val formattedTime = String.format("%02d:%02d", h, m)
            onTimeSelected(formattedTime)
        },
        hour,
        minute,
        true // 24 hour format
    )

    OutlinedTextField(
        value = time,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = modifier.clickable { timePickerDialog.show() }, // Klik field -> Muncul Dialog Jam
        enabled = false, // Hack biar klik tembus ke modifier clickable
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = Color.Black,
            disabledContainerColor = Color.White,
            disabledBorderColor = Color.Gray,
            disabledLabelColor = Color.Gray
        )
    )
}