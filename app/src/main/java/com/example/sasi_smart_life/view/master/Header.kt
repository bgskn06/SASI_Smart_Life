package com.example.sasi_smart_life.view.master

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.example.sasi_smart_life.R
import com.example.sasi_smart_life.view.theme.sasiColor
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.sasi_smart_life.data.models.*
import com.example.sasi_smart_life.viewModel.MainViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun Header(
    viewModel: MainViewModel,
    categories: List<DeviceCategory>,
    devices: List<Device>,
    homeName: String?,
    error: String?,
    roomCount: Int,
    deviceCount: Int,
    onRoomNameClick: () -> Unit,
) {
    var expandedDevice by remember { mutableStateOf<DeviceCategory?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    val currentHomeId = viewModel.uiState.collectAsState().value.selectedHomeId?.homeId

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            onSave = {categoryName ->
                if (currentHomeId != null) {
                    viewModel.createCategory(
                        name = categoryName,
                        image = "",
                        imageUrlOn = "",
                        imageUrlOff = "",
                    )
                }
                showAddCategoryDialog = false
            }
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Card(
            modifier = Modifier
                .weight(0.32f)
                .fillMaxHeight(),
            colors = CardDefaults.cardColors(containerColor = sasiColor.purple50),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, sasiColor.purple100)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight()
                        .padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {

                    if (error != null) {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red,
                            modifier = Modifier.clickable(onClick = onRoomNameClick)
                        )
                    } else {
                        Text(
                            text = homeName ?: "home not found",
                            style = if (homeName != null)MaterialTheme.typography.headlineMedium else MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable(onClick = onRoomNameClick)
                        )
                    }

                    Image(
                        painter = painterResource(id = R.drawable.logo_sag),
                        contentDescription = "Room Image"
                    )
                }
                DateTime(modifier = Modifier.weight(0.65f))
            }
        }
        Column(
            modifier = Modifier
                .weight(0.1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = sasiColor.grey50),
                border = BorderStroke(1.dp, sasiColor.purple500),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(text = "Total Room", color = sasiColor.black300, style = MaterialTheme.typography.bodySmall)
                    Text(text = "$roomCount", color = sasiColor.purple500, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = sasiColor.grey50),
                border = BorderStroke(1.dp, sasiColor.blue500)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(text = "Total Device", color = sasiColor.black300, style = MaterialTheme.typography.bodySmall)
                    Text(text = "$deviceCount", color = sasiColor.blue500, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Card(
            modifier = Modifier
                .weight(0.58f)
                .fillMaxHeight(),
            colors = CardDefaults.cardColors(containerColor = sasiColor.blue50),
            border = BorderStroke(1.dp, sasiColor.blue300)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    items(categories) { category ->
                        Box {
                            CategoryCard(
                                category = category,
                                devices = devices,
                                onCardClick = { expandedDevice = category }
                            )
                            if (expandedDevice == category) {
                                Popup(
                                    onDismissRequest = { expandedDevice = null },
                                    alignment = Alignment.TopStart
                                ) {
                                    Column {
                                        Spacer(modifier = Modifier.height(90.dp))
                                        CategoryDetailPopup(category = category, devices = devices)
                                    }
                                }
                            }
                        }
                    }
                    item {
                        AddCategoryCard(
                            onClick = { showAddCategoryDialog = true }
                        )
                    }
                }
                val alarmDevice = devices.find { it.name == "Alarm"}

                Card(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(100.dp)
                        .padding(horizontal = 5.dp)
                        .clickable(
                            enabled = alarmDevice != null,
                            onClick = {
                                alarmDevice?.let { device ->
                                    device.roomId?.let { roomId ->
                                        viewModel.setDeviceStatus(
                                            device.devId,
                                            roomId,
                                            !device.status
                                        )
                                    }
                                }
                            }
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF44336)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoNotDisturbOn,
                                contentDescription = "Alarm Icon",
                                tint = Color(0xFFF44336),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Alarm",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DateTime(modifier: Modifier = Modifier) {
    var currentTime by remember { mutableStateOf("") }
    var currentDayOfWeek by remember { mutableStateOf("") }
    var currentDayOfMonth by remember { mutableStateOf("") }
    var currentMonthAndYear by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            val cal = Calendar.getInstance()
            currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(cal.time)
            currentDayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(cal.time)
            currentDayOfMonth = SimpleDateFormat("d", Locale.getDefault()).format(cal.time)
            currentMonthAndYear = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(cal.time)
            delay(1000)
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxHeight()
            .padding(vertical = 8.dp, horizontal = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFDC54F4),
                        Color(0xFFA528CB)
                    )
                )
            )
    )
    {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Card(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 5.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = currentDayOfWeek, style = MaterialTheme.typography.labelSmall, color = sasiColor.purple500)
                    Text(text = currentDayOfMonth, style = MaterialTheme.typography.titleLarge, color = sasiColor.purple500, fontWeight = FontWeight.SemiBold)
                    Text(text = currentMonthAndYear, style = MaterialTheme.typography.labelSmall, color = sasiColor.purple500)
                }
            }
            Text(
                text = currentTime,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
        }
    }
}

@Composable
fun CategoryCard(
    category: DeviceCategory,
    devices: List<Device>,
    onCardClick: (DeviceCategory) -> Unit,
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
            .width(110.dp)
            .clickable { onCardClick(category) }
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
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleSmall.copy(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                ),
                fontWeight = FontWeight.SemiBold
            )
            Row(modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (totalCount == 1) "$totalCount Unit" else "$totalCount Units",
                    style = MaterialTheme.typography.labelSmall.copy(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    ),
                    color = Color.Gray
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(Color.Green, CircleShape)
                    )
                    Text(
                        text = "$activeCount",
                        style = MaterialTheme.typography.labelSmall.copy(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            )
                        ),
                        fontWeight = FontWeight.Bold,
                        color = sasiColor.black300
                    )
                    Spacer(Modifier.width(2.dp))
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(Color.Gray, CircleShape)
                    )
                    Text(
                        text = "$inactiveCount",
                        style = MaterialTheme.typography.labelSmall.copy(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            )
                        ),
                        fontWeight = FontWeight.Bold,
                        color = sasiColor.black300
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryDetailPopup(
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
        modifier = Modifier.width(280.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = sasiColor.grey50)
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = category.image.ifEmpty { category.imageUrlOn },
                    placeholder = null,
                    error = painterResource(id = R.drawable.scene_empty),
                    contentDescription = category.name,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = category.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
//                var isChecked by remember { mutableStateOf(true) }
//                Switch(
//                    checked = isChecked,
//                    onCheckedChange = { isChecked = it },
//                    modifier = Modifier.scale(0.8f),
//                    colors = SwitchDefaults.colors(
//                        checkedThumbColor = sasiColor.grey50,
//                        uncheckedThumbColor = sasiColor.grey50,
//                        checkedTrackColor = sasiColor.green500,
//                        uncheckedTrackColor = sasiColor.black50,
//                        uncheckedBorderColor = sasiColor.black50
//                    ),
//                )
            }
            HorizontalDivider()
            Row {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Information", color = Color.Gray,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = "Device",
                        Modifier.padding(vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(Color.Green, CircleShape)
                        )
                        Text(
                            text = activeCount.toString(),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(Color.Gray, CircleShape)
                        )
                        Text(
                            text = inactiveCount.toString(),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Daily Usage", color = Color.Gray,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = "35.40 kWh",
                        Modifier.padding(vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "IDR. 74.000", color = sasiColor.purple500,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
fun AddCategoryCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxHeight()
            .width(110.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = sasiColor.blue50),
        border = BorderStroke(1.dp, sasiColor.blue100)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = "Add Device Type",
                tint = sasiColor.blue500,
                modifier = Modifier.size(24.dp)
            )
            Text(text = "Device Type", color = sasiColor.blue500, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun AddCategoryDialog(onDismissRequest: () -> Unit, onSave: (String) -> Unit) {

    Dialog(onDismissRequest = onDismissRequest, properties = DialogProperties(dismissOnClickOutside = false)) {
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
                    text = "Add Device Type",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .border(BorderStroke(1.dp, Color.Gray), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Image Placeholder",
                                modifier = Modifier.size(40.dp),
                                tint = Color.Gray
                            )
                            Text(text = "No Image Uploaded",
                                style = MaterialTheme.typography.labelSmall,
                                color = sasiColor.black300,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Column {
                        Text(text = "File type: PNG or JPG", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text(text = "Max file size: 2MB", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { /* TODO: Handle image upload */ },
                            colors = ButtonDefaults.buttonColors(containerColor = sasiColor.blue50),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, sasiColor.blue500)
                        ) {
                            Text("Upload", color = sasiColor.blue500, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                var name by remember { mutableStateOf("") }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Enter Device Category Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = sasiColor.purple500,
                        unfocusedBorderColor = sasiColor.black300,
                        cursorColor = sasiColor.purple500,
                        focusedLabelColor = sasiColor.black300,
                    )
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { onDismissRequest() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = sasiColor.red50)
                    ) {
                        Text("Cancel", color = sasiColor.red500)
                    }
                    Button(
                        onClick = {
                            onSave(name)
                            onDismissRequest() },
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
