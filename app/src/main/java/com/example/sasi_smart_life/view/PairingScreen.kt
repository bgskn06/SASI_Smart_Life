package com.example.sasi_smart_life.view

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.example.sasi_smart_life.data.models.DeviceCategory
import com.example.sasi_smart_life.viewModel.MainViewModel
import com.example.sasi_smart_life.viewModel.PairingStep
import com.example.sasi_smart_life.viewModel.TuyaViewModel
import com.example.sasi_smart_life.view.theme.sasiColor

@Composable
fun PairingScreen(
    mainViewModel: MainViewModel,
    tuyaViewModel: TuyaViewModel,
    onBack: () -> Unit,
    roomId: String? // Pass roomId for custom device addition
) {
    val context = LocalContext.current
    val pairingState by tuyaViewModel.pairingState.collectAsState()

    LaunchedEffect(Unit) {
        tuyaViewModel.resetState()
    }

    LaunchedEffect(pairingState.error) {
        pairingState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    fun isWifiConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
    }

    @Suppress("DEPRECATION")
    fun getCurrentSsid(context: Context): String {
        if (!isWifiConnected(context)) return ""
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        return wifiInfo.ssid.removeSurrounding("\"")
    }

    var isWifiOn by remember { mutableStateOf(isWifiConnected(context)) }
    var ssid by remember { mutableStateOf(getCurrentSsid(context)) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isWifiOn = isWifiConnected(context)
                ssid = getCurrentSsid(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.CenterEnd
    ) {
        androidx.compose.material3.Card(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = sasiColor.grey50)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = {
                        tuyaViewModel.stopPairing()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        "Add Device",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                var selectedTab by remember { mutableStateOf("Custom") }
                Row(modifier = Modifier.fillMaxWidth()) {
                    val buttonShape = RoundedCornerShape(50)

                    Text(
                        "Custom Device",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selectedTab == "Custom") sasiColor.blue50 else sasiColor.black300,
                        modifier = Modifier
                            .clip(buttonShape)
                            .clickable { selectedTab = "Custom" }
                            .background(if (selectedTab == "Custom") sasiColor.blue500 else Color.Transparent)
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (selectedTab == "Custom") Color.Transparent else sasiColor.grey600
                                ),
                                buttonShape
                            )
                            .padding(horizontal = 12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Tuya Device",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selectedTab == "Tuya") sasiColor.blue50 else sasiColor.black300,
                        modifier = Modifier
                            .clip(buttonShape)
                            .clickable { selectedTab = "Tuya" }
                            .background(if (selectedTab == "Tuya") sasiColor.blue500 else Color.Transparent)
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (selectedTab == "Tuya") Color.Transparent else sasiColor.grey600
                                ),
                                buttonShape
                            )
                            .padding(horizontal = 12.dp),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == "Tuya") {
                    PairingTuya(
                        ssid = ssid, 
                        tuyaViewModel = tuyaViewModel,
                        mainViewModel = mainViewModel,
                        onPairingSuccess = onBack,
                        roomId = roomId // Close the dialog on success
                    )
                } else {
                    AddDeviceCustom(
                        mainViewModel = mainViewModel,
                        roomId = roomId,
                        onDeviceAdded = onBack
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDeviceCustom(
    mainViewModel: MainViewModel,
    roomId: String?,
    onDeviceAdded: () -> Unit
){
    val appState by mainViewModel.uiState.collectAsState()
    
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<DeviceCategory?>(null) }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    val currentHome = appState.selectedHomeId
    val currentRoom = appState.rooms.find { it.roomId == roomId }
    val colorTextfield = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = sasiColor.purple500,
        unfocusedBorderColor = sasiColor.black300,
        cursorColor = sasiColor.purple500,
        focusedLabelColor = sasiColor.black300,
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Device Name") },
            modifier = Modifier.fillMaxWidth(),
            colors = colorTextfield
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

        OutlinedTextField(
            value = currentHome?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Home") },
            modifier = Modifier.fillMaxWidth(),
            colors = colorTextfield
        )

        OutlinedTextField(
            value = currentRoom?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Room") },
            modifier = Modifier.fillMaxWidth(),
            colors = colorTextfield
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                if (name.isNotBlank() && selectedCategory != null && currentHome != null) {
                    mainViewModel.addDevice(
                        name = name,
                        categoryId = selectedCategory!!.categoryId,
                        homeId = currentHome.homeId,
                        roomId = roomId,
                    )
                    onDeviceAdded()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank() && selectedCategory != null,
            colors = ButtonDefaults.buttonColors(sasiColor.purple500)
        ) {
            Text("Save Device")
        }
    }
}

@Composable
fun PairingTuya(
    ssid: String, 
    tuyaViewModel: TuyaViewModel,
    mainViewModel: MainViewModel,
    onPairingSuccess: () -> Unit,
    roomId: String?
) {
    val context = LocalContext.current
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val pairingState by tuyaViewModel.pairingState.collectAsState()
    val appState by mainViewModel.uiState.collectAsState()
    val colorTextfield = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = sasiColor.purple500,
        unfocusedBorderColor = sasiColor.black300,
        cursorColor = sasiColor.purple500,
        focusedLabelColor = sasiColor.black300,
    )


    LaunchedEffect(pairingState.step) {
        if (pairingState.step == PairingStep.SUCCESS) {
            onPairingSuccess()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // WIFI STATUS CARD
        Box(modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(BorderStroke(1.dp, Color(0xFFCCC1FF)), RoundedCornerShape(8.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFF7F8FF),
                        Color(0xFF9F90F4)
                    )
                )
            )
            .clickable { context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) },
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Wifi: $ssid",
                        fontWeight = FontWeight.Bold
                    )
                    Text("Wi-Fi must be 2.4GHz for pairing", style = MaterialTheme.typography.bodySmall)
                }
                Box(
                    modifier = Modifier
                        .background(sasiColor.grey50, CircleShape)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Wifi,
                        contentDescription = "Wifi On",
                        tint = sasiColor.green500,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Wi-Fi Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(image, "toggle password visibility")
                }
            },
            colors = colorTextfield
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { 
                val homeId = appState.selectedHomeId?.tuyaHomeId
                if (homeId == null) {
                    Toast.makeText(context, "No home selected", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                tuyaViewModel.startPairing(context, homeId, ssid, password) { deviceBean ->
                    // Convert DeviceBean to your custom Device model and save
                    mainViewModel.addDevice(
                        devId = deviceBean.devId,
                        name = deviceBean.name,
                        categoryId = deviceBean.category, // You might need to map this to your own category system
                        homeId = appState.selectedHomeId?.homeId ?: "",
                        roomId = roomId, // Or prompt user to select a room
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = ssid.isNotBlank() && password.isNotBlank() && pairingState.step == PairingStep.IDLE,
            colors = ButtonDefaults.buttonColors(sasiColor.blue500)
        ) {
            if (pairingState.step == PairingStep.IDLE) {
                Text("Search for Devices", color = sasiColor.blue50)
            } else {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            }
        }
        if (pairingState.step == PairingStep.SCANNING) {
            Text("Searching...", modifier = Modifier.padding(top = 8.dp))
        }
        if (pairingState.step == PairingStep.CONNECTING) {
            Text("Connecting to device...", modifier = Modifier.padding(top = 8.dp))
        }
        if (pairingState.step == PairingStep.SUCCESS) {
            Text("Pairing successful!", color = sasiColor.green500)
        }
    }
}
