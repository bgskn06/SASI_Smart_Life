package com.example.sasi_smart_life.view

import android.app.TimePickerDialog
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoubleArrow
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.sasi_smart_life.R
import com.example.sasi_smart_life.data.models.Device
import com.example.sasi_smart_life.data.models.DeviceCategory
import com.example.sasi_smart_life.data.models.SceneAction
import com.example.sasi_smart_life.data.models.SceneCondition
import com.example.sasi_smart_life.data.models.SceneSchedule
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
                viewModel.addScene(newScene)
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
        return devices.find { it.devId == devId }?.name ?: "?"
    }

    fun getCategoryIcon(devId: String?): String? {
        val device = devices.find { it.devId == devId } ?: return null
        val category = categories.find { it.categoryId == device.category }
        return category?.image?.ifEmpty { category?.imageUrlOn }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 140.dp)
            .combinedClickable(
                onClick = {},
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(containerColor = sasiColor.blue50),
        shape = RoundedCornerShape(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            colors = CardDefaults.cardColors(containerColor = sasiColor.grey50),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, sasiColor.grey600)
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

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Input,
                            contentDescription = null,
                            tint = sasiColor.red500,
                            modifier = Modifier.size(20.dp)
                        )

                        Row(
                            modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            scene.ifData.forEachIndexed { index, condition ->
                                DeviceItemSmall(
                                    deviceName = getDeviceName(condition.devId),
                                    statusText = if (condition.status == 1) "ON" else "OFF",
                                    iconUrl = getCategoryIcon(condition.devId)
                                )

                                if (index < scene.ifData.size - 1) {
                                    Text(
                                        text = scene.logic.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = sasiColor.purple500,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = null,
                            tint = sasiColor.yellow500,
                            modifier = Modifier.size(20.dp)
                        )

                        Row(
                            modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            scene.thenAction.forEach { action ->
                                DeviceItemSmall(
                                    deviceName = getDeviceName(action.devId),
                                    statusText = if (action.status == 1) "ON" else "OFF",
                                    iconUrl = getCategoryIcon(action.devId)
                                )
                            }
                        }
                    }
                }
            }
        }

        fun SceneSchedule.getActiveDaysText(): String {

            if (days.isEmpty()) return "-"

            val orderedKeys = listOf(
                "mon", "tue", "wed", "thu",
                "fri", "sat", "sun"
            )

            val activeDays = orderedKeys.filter { days[it] == true }

            if (activeDays.isEmpty()) return "-"

            if (activeDays.size == 7) return "Everyday"

            val dayMap = mapOf(
                "mon" to "Mon",
                "tue" to "Tue",
                "wed" to "Wed",
                "thu" to "Thu",
                "fri" to "Fri",
                "sat" to "Sat",
                "sun" to "Sun"
            )

            return activeDays
                .mapNotNull { dayMap[it] }
                .joinToString(" ")
        }

        Row (
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 6.dp, end = 6.dp)
        ) {
            Row (
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isTimeEnabled = scene.schedule.enabled
                val start = scene.schedule.startTime
                val end = scene.schedule.endTime

                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = sasiColor.green500,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (isTimeEnabled) "$start - $end" else "All day",
                    style = MaterialTheme.typography.bodySmall,
                    color = sasiColor.black300,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = scene.schedule.getActiveDaysText(),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(6.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun DeviceItemSmall(
    deviceName: String,
    statusText: String,
    iconUrl: String?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .background(Color.White, RoundedCornerShape(6.dp))
            .border(0.5.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        AsyncImage(
            model = iconUrl,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "$deviceName ($statusText)",
            style = MaterialTheme.typography.labelSmall,
            color = sasiColor.black500,
            maxLines = 1
        )
    }
}

fun mapSelectedDaysToSchedule(selectedDays: Set<Int>): Map<String, Boolean> {

    val dayKeys = listOf(
        "mon", // 1
        "tue", // 2
        "wed", // 3
        "thu", // 4
        "fri", // 5
        "sat", // 6
        "sun"  // 7
    )

    return dayKeys.mapIndexed { index, key ->
        key to selectedDays.contains(index + 1)
    }.toMap()
}

@Composable
fun AddScene(
    devices: List<Device>,
    onDismiss: () -> Unit,
    category: List<DeviceCategory>,
    onSave: (SmartScene) -> Unit
) {
    var sceneName by remember { mutableStateOf("") }
    var selectedCategoryName by remember { mutableStateOf("General") }
    var logicOperator by remember { mutableStateOf("AND") }

    var ifConditions = remember { mutableStateListOf(SceneCondition())}

    var isTimeEnabled by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf("00:00") }
    var endTime by remember { mutableStateOf("00:00") }
    var selectedDays by rememberSaveable {
        mutableStateOf(setOf(1,2,3,4,5,6,7))
    }

    var thenActions = remember { mutableStateListOf(SceneAction())}

    data class SceneCategoryItem(
        val name: String,
        val icon: ImageVector,
    )
    val sceneCategories = listOf(
        SceneCategoryItem("General", Icons.Default.Category),
        SceneCategoryItem("Security", Icons.Default.Shield),
        SceneCategoryItem("Lighting", Icons.Default.Lightbulb),
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterEnd
        ) {
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
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
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

                        Text("Category", style = MaterialTheme.typography.labelMedium, color = sasiColor.black300)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(sceneCategories) { item ->
                                val isSelected = selectedCategoryName == item.name
                                Surface(
                                    modifier = Modifier.clickable { selectedCategoryName = item.name },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) sasiColor.purple500.copy(alpha = 0.1f) else Color.White,
                                    border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) sasiColor.purple500 else sasiColor.black100)
                                ) {
                                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(item.icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = if (isSelected) sasiColor.purple500 else sasiColor.black300)
                                        Text(item.name, style = MaterialTheme.typography.bodyMedium, color = if (isSelected) sasiColor.purple500 else sasiColor.black500)
                                    }
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SectionLabel("IF (Trigger)")
                            Spacer(Modifier.weight(1f))
                            LogicToggle(logicOperator) { logicOperator = it }
                        }

                        ifConditions.forEachIndexed { index, cond ->
                            ConditionRow(
                                devices = devices,
                                categories = category,
                                condition = cond,
                                onUpdate = { ifConditions[index] = it },
                                onDelete = { if (ifConditions.size > 1) ifConditions.removeAt(index) }
                            )
                        }
                        TextButton(
                            onClick = { ifConditions.add(SceneCondition()) },
                            colors = ButtonDefaults.textButtonColors(contentColor = sasiColor.purple500)
                        ) {
                            Icon(Icons.Default.Add, null, tint = sasiColor.purple500); Text("Add Condition")
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

                        SectionLabel("REPEAT DAYS")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
                            dayLabels.forEachIndexed { index, label ->
                                val dayNum = index + 1
                                val isSelected = selectedDays.contains(dayNum)

                                Box(
                                    modifier = Modifier
                                        .size(35.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) sasiColor.purple500 else Color.LightGray)
                                        .clickable {
                                            selectedDays = if (isSelected)
                                                selectedDays - dayNum
                                            else
                                                selectedDays + dayNum
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        SectionLabel("THEN (Action)")
                        thenActions.forEachIndexed { index, action ->
                            ActionRow(
                                devices = devices,
                                categories = category,
                                action = action,
                                onUpdate = { thenActions[index] = it },
                                onDelete = { if (thenActions.size > 1) thenActions.removeAt(index) }
                            )
                        }
                        TextButton(
                            onClick = { thenActions.add(SceneAction()) },
                            colors = ButtonDefaults.textButtonColors(contentColor = sasiColor.purple500)
                        ) {
                            Icon(Icons.Default.Add, null, tint = sasiColor.purple500); Text("Add Action")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val validIf = ifConditions.filter { it.devId.isNotEmpty() }
                            val validThen = thenActions.filter { it.devId.isNotEmpty() }
                            if (validIf.isNotEmpty() && validThen.isNotEmpty()) {
                                onSave(SmartScene(
                                    sceneId = System.currentTimeMillis().toString(),
                                    name = sceneName,
                                    isActive = true,
                                    category = selectedCategoryName,
                                    ifData = validIf,
                                    logic = logicOperator,
                                    schedule = SceneSchedule(isTimeEnabled, startTime, endTime, mapSelectedDaysToSchedule(selectedDays)),
                                    thenAction = validThen
                                ))
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
fun ConditionRow(
    devices: List<Device>,
    categories: List<DeviceCategory>,
    condition: SceneCondition,
    onUpdate: (SceneCondition) -> Unit,
    onDelete: () -> Unit
) {
    val selectedDev = devices.find { it.devId == condition.devId }
    val cat = categories.find { it.categoryId == selectedDev?.category }
    val isSuhu = cat?.name?.lowercase()?.contains("suhu") == true

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(0.5f)) {
            DeviceDropdown(label = "Device", options = devices, category = categories, selected = selectedDev, onSelect = { onUpdate(condition.copy(devId = it?.devId ?: "")) })
        }
        Box(modifier = Modifier.weight(0.4f)) {
            if (isSuhu) {
                // UI untuk Suhu (Operator + Value)
                Row { /* Sama seperti kode lamamu tapi panggil onUpdate(condition.copy(...)) */ }
            } else {
                StatusDropdown(
                    label = "Status",
                    selectedValue = condition.status,
                    options = if (cat?.name?.lowercase()?.contains("door") == true) listOf("Open" to 0, "Closed" to 1) else listOf("On" to 1, "Off" to 0),
                    onSelect = { onUpdate(condition.copy(status = it)) }
                )
            }
        }
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete", tint = Color.Red) }
    }
}

@Composable
fun ActionRow(
    devices: List<Device>,
    categories: List<DeviceCategory>,
    action: SceneAction,
    onUpdate: (SceneAction) -> Unit,
    onDelete: () -> Unit
) {
    val selectedDev = devices.find { it.devId == action.devId }
    val cat = categories.find { it.categoryId == selectedDev?.category }
    val isSuhu = cat?.name?.lowercase()?.contains("suhu") == true

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(0.5f)) {
            DeviceDropdown(label = "Device", options = devices, category = categories, selected = selectedDev, onSelect = { onUpdate(action.copy(devId = it?.devId ?: "")) })
        }
        Box(modifier = Modifier.weight(0.4f)) {
            if (isSuhu) {
                Row { /* Sama seperti kode lamamu tapi panggil onUpdate(condition.copy(...)) */ }
            } else {
                StatusDropdown(
                    label = "Status",
                    selectedValue = action.status,
                    options = if (cat?.name?.lowercase()?.contains("door") == true) listOf("Open" to 0, "Closed" to 1) else listOf("On" to 1, "Off" to 0),
                    onSelect = { onUpdate(action.copy(status = it)) }
                )
            }
        }
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete", tint = Color.Red) }
    }
}

@Composable
fun LogicToggle(selected: String, onSelect: (String) -> Unit) {
    Row(modifier = Modifier.background(sasiColor.grey600, RoundedCornerShape(8.dp)).padding(2.dp)) {
        listOf("AND", "OR").forEach { label ->
            val isSelected = selected == label
            Box(modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isSelected) sasiColor.purple500 else Color.Transparent)
                .clickable { onSelect(label) }
                .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(label, color = if (isSelected) Color.White else Color.Black, fontSize = 12.sp)
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
fun OperatorDropdown(
    selectedOp: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val operators = listOf(
        ">" to "Besar dari",
        "<" to "Kecil dari",
        "==" to "Sama dengan"
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedOp,
            onValueChange = {},
            readOnly = true,
            label = { Text("Kondisi") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = sasiColor.purple500,
                unfocusedBorderColor = sasiColor.black300
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = Color.White
        ) {
            operators.forEach { (op, desc) ->
                DropdownMenuItem(
                    text = { Text("$op ($desc)") },
                    onClick = {
                        onSelect(op)
                        expanded = false
                    }
                )
            }
        }
    }
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
    label: String,
    selectedValue: Int,
    options: List<Pair<String, Int>>,
    onSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val currentLabel = options.find { it.second == selectedValue }?.first ?: "Pilih"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = currentLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = sasiColor.purple500,
                unfocusedBorderColor = sasiColor.black300
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = Color.White
        ) {
            options.forEach { (text, value) ->
                DropdownMenuItem(
                    text = { Text(text) },
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

    val parts = time.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

    val timePickerDialog = TimePickerDialog(
        context,
        { _, h, m ->
            val formattedTime = String.format("%02d:%02d", h, m)
            onTimeSelected(formattedTime)
        },
        hour,
        minute,
        true
    )

    OutlinedTextField(
        value = time,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = modifier.clickable { timePickerDialog.show() },
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = Color.Black,
            disabledContainerColor = Color.White,
            disabledBorderColor = Color.Gray,
            disabledLabelColor = Color.Gray
        )
    )
}
