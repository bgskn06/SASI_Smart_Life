package com.example.sasi_smart_life.view

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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.sasi_smart_life.R
import com.example.sasi_smart_life.data.models.Home
import com.example.sasi_smart_life.view.theme.sasiColor

@Composable
fun LocationSelectionDialog(
    homes: List<Home>,
    currentHomeId: String?,
    onDismissRequest: () -> Unit,
    onLocationSelected: (Home) -> Unit,
    onAddLocationClick: () -> Unit,
    onSettingsClick: (Home) -> Unit
) {
    var selectedHome by remember { mutableStateOf(homes.find { it.homeId == currentHomeId }) }
    val gridItems = homes + "ADD_LOCATION"

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth(1f)
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Title
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.1f)
                        .clip(RoundedCornerShape(8.dp))
                        .border(BorderStroke(1.dp, Color(0xFFCCC1FF)), RoundedCornerShape(8.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFF7F8FF),
                                    Color(0xFF9F90F4)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(modifier = Modifier.size(20.dp), imageVector = Icons.Default.LocationOn, contentDescription = "Select Location", tint = sasiColor.purple500)
                        Text(text = "Select Location", style = MaterialTheme.typography.titleSmall, color = sasiColor.purple500)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Location Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(gridItems) { item ->
                        when (item) {
                            is Home -> {
                                LocationCard(
                                    home = item,
                                    isSelected = item.homeId == selectedHome?.homeId,
                                    onLocationClick = { selectedHome = it },
                                    onSettingsClick = onSettingsClick
                                )
                            }
                            is String -> {
                                AddLocationCard(onClick = onAddLocationClick)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = sasiColor.red50),
                    ) {
                        Text("Cancel", color = sasiColor.red500)
                    }
                    Button(
                        onClick = { selectedHome?.let { onLocationSelected(it) } },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = sasiColor.blue500)
                    ) {
                        Text("Apply", color = sasiColor.blue50)
                    }
                }
            }
        }
    }
}

@Composable
fun AddLocationDialog(
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
                    text = "Add Location",
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
fun LocationCard(
    home: Home,
    isSelected: Boolean,
    onLocationClick: (Home) -> Unit,
    onSettingsClick: (Home) -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1.25f)
            .clickable { onLocationClick(home) },
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) BorderStroke(2.dp, sasiColor.purple500) else null,
        colors = CardDefaults.cardColors(sasiColor.blue50)
    ) {
        Box {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {

                AsyncImage(
                    model = home.imageUrl,
                    placeholder = painterResource(id = R.drawable.logo_sag),
                    error = painterResource(id = R.drawable.scene_empty),
                    contentDescription = "Floor Plan",
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .weight(1f),
                    contentScale = ContentScale.Fit
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(sasiColor.purple300)
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = home.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = Color.White)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(sasiColor.purple50)
                            .clickable { onSettingsClick(home) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = sasiColor.purple500,
                            modifier = Modifier
                                .padding(2.dp)
                                .size(18.dp)
                        )
                    }
                }
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = "Selected",
                    tint = sasiColor.purple500,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun AddLocationCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .aspectRatio(1.25f)
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = sasiColor.blue50),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = "Add Location",
                tint = sasiColor.blue500,
                modifier = Modifier.size(24.dp)
            )
            Text("Add Location", style = MaterialTheme.typography.bodySmall, color = sasiColor.blue500, fontWeight = FontWeight.SemiBold)
        }
    }
}
