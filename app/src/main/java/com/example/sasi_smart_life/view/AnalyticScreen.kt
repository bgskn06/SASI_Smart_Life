package com.example.sasi_smart_life.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Straight
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.MeetingRoom
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sasi_smart_life.view.master.DateTime
import com.example.sasi_smart_life.view.theme.sasiColor
import kotlin.collections.forEach
import kotlin.collections.forEachIndexed
import kotlin.collections.maxOrNull
import com.example.sasi_smart_life.R
import androidx.navigation.NavController
import com.example.sasi_smart_life.view.master.BottomNavBar


@Composable
fun AnalyticScreen(
    navController: NavController, // Tambahkan ini
    currentRoute: String?
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(Modifier.weight(0.15f)) {
                Header()
            }
            Box(Modifier.weight(0.85f)) {
                Body()
            }
        }

        BottomNavBar(
            navController = navController,
            currentRoute = currentRoute,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun Header(){
    Row (
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ){
        Card(
            modifier = Modifier
                .weight(0.30f)
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
                    Text(
                        text = "Office",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Image(
                        painter = painterResource(id = R.drawable.logo_sag),
                        contentDescription = "Room Image"
                    )
                }
                DateTime(modifier = Modifier.weight(0.65f))
            }
        }
        Card(
            modifier = Modifier.weight(0.08f),
            colors = CardDefaults.cardColors(containerColor = sasiColor.grey50),
            border = BorderStroke(1.dp, sasiColor.blue500)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Total Device", fontSize = 12.sp)
                Row {
                    Text(
                        "36",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = sasiColor.blue500
                    )
                }
            }
        }
        Card(
            modifier = Modifier.weight(0.08f),
            colors = CardDefaults.cardColors(containerColor = sasiColor.grey50),
            border = BorderStroke(1.dp, sasiColor.purple500)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Total Room", fontSize = 12.sp)
                Text(
                    "9",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = sasiColor.purple500
                )
            }
        }
        Card(
            modifier = Modifier.weight(0.22f),
            colors = CardDefaults.cardColors(containerColor = sasiColor.grey50),
            border = BorderStroke(1.dp, sasiColor.grey600),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = "Flash Icon",
                        tint = sasiColor.yellow50,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(shape = CircleShape)
                            .background(sasiColor.yellow500)
                            .padding(6.dp)
                    )
                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("1.825.40", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "kWh",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 2.dp) // Align baseline
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Straight,
                                contentDescription = "Increase",
                                tint = sasiColor.red500,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                "30%",
                                fontSize = 12.sp,
                                color = sasiColor.red500,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("vs last month", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
                HorizontalDivider(color = sasiColor.grey600.copy(alpha = 0.5f))
                Text(
                    "IDR. 12.000.000",
                    color = sasiColor.purple500,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
        Card(
            modifier = Modifier.weight(0.22f),
            colors = CardDefaults.cardColors(containerColor = sasiColor.blue50),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { },
                    shape = CircleShape,
                    modifier = Modifier.size(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    contentPadding = PaddingValues(0.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Month",
                        tint = Color.Gray
                    )
                }
                Text("August 2025", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                Button(
                    onClick = { },
                    shape = CircleShape,
                    modifier = Modifier.size(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    contentPadding = PaddingValues(0.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Month",
                        tint = Color.Gray
                    )
                }
            }
        }

    }
}

data class RoomUsageData(
    val roomName: String,
    val deviceCount: Int,
    val usage: String,
    val cost: String,
)

@Composable
private fun Body(){
    val rooms = remember {
        listOf(
            RoomUsageData("PNC Room", 8, "365.40", "IDR. 2.100.000"),
            RoomUsageData("Engineer Room", 8, "365.40", "IDR. 2.100.000"),
            RoomUsageData("FTA Room", 8, "365.40", "IDR. 2.100.000"),
            RoomUsageData("Marketing Room", 8, "365.40", "IDR. 2.100.000"),
            RoomUsageData("APD Room", 8, "365.40", "IDR. 2.100.000"),
            RoomUsageData("IT Room", 8, "365.40", "IDR. 2.100.000"),
            RoomUsageData("Hallway", 8, "365.40", "IDR. 2.100.000"),
        )
    }
    Column (modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row (modifier = Modifier.weight(0.45f),
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Chart(modifier = Modifier.weight(0.5f))
            TopRoom(modifier = Modifier.weight(0.25f))
            TopDevice(modifier = Modifier.weight(0.25f))
        }
        Text(
            "Room Usage",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(0.05f)
        )
        Box(modifier = Modifier.weight(0.5f),){
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(rooms) { room ->
                    RoomUsageCard(room)
                }
            }
        }
    }
}

@Composable
private fun Chart(modifier: Modifier = Modifier) {
    val monthlyData = remember {
        listOf(65f, 75f, 40f, 95f, 100f, 85f, 50f, 70f, 0f, 0f, 0f, 0f)
    }
    val months = remember {
        listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")
    }
    val maxValue = monthlyData.maxOrNull() ?: 0f

    var selectedBar by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(sasiColor.grey50),
        border = BorderStroke(1.dp, sasiColor.grey600),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Statistics", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("2025", fontSize = 12.sp)
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Select Year",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()

            // Chart Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .weight(1f),
                contentAlignment = Alignment.BottomCenter
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    monthlyData.forEachIndexed { index, value ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (selectedBar == index) {
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(
                                            0xFF3B3B3B
                                        )
                                    ),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Text(
                                        text = "1.825.40 kWh",
                                        modifier = Modifier.padding(
                                            horizontal = 8.dp,
                                            vertical = 4.dp
                                        ),
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            val barHeight = (value / maxValue)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .fillMaxHeight(barHeight)
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                    .background(sasiColor.purple300)
                                    .clickable {
                                        selectedBar = if (selectedBar == index) null else index
                                    }
                            )
                        }
                    }
                }
            }
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                months.forEach {
                    Text(
                        it,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

data class TopRoomData(
    val rank: Int,
    val name: String,
    val usage: String,
    val cost: String,
    val rankColor: Color
)

@Composable
private fun TopRoom(modifier: Modifier = Modifier){
    val topRoomsData = remember {
        listOf(
            TopRoomData(1, "PNC Room", "365.40", "IDR. 2.100.000", sasiColor.yellow50),
            TopRoomData(2, "Marketing Room", "365.40", "IDR. 2.100.000", sasiColor.black50),
            TopRoomData(3, "FTA Room", "365.40", "IDR. 2.100.000", sasiColor.purple50)
        )
    }
    Card(
        modifier = modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(sasiColor.grey50),
        border = BorderStroke(1.dp, sasiColor.grey600),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp).fillMaxSize()) {
            Text("Top 3 Room Usage", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                topRoomsData.forEach { roomData ->
                    TopRoomListItem(data = roomData, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TopRoomListItem(data: TopRoomData, modifier: Modifier = Modifier){
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(sasiColor.grey50),
        border = BorderStroke(1.dp, sasiColor.grey600)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(color = data.rankColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${data.rank}",
                    modifier = Modifier.padding(horizontal = 4.dp),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    data.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Start
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = "Flash Icon",
                        tint = sasiColor.yellow50,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(shape = CircleShape)
                            .background(sasiColor.yellow500)
                            .padding(2.dp)
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                data.usage, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false
                                    )
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "kWh", fontSize = 10.sp, color = Color.Gray,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false
                                    )
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            data.cost, fontSize = 10.sp, color = sasiColor.purple500,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(
                                    includeFontPadding = false
                                )
                            )
                        )
                    }
                }
            }
        }
    }
}

data class TopDeviceData(
    val rank: Int,
    val name: String,
    val usage: String,
    val cost: String,
    val rankColor: Color
)

@Composable
private fun TopDevice(modifier: Modifier = Modifier){
    val topDevicesData = remember {
        listOf(
            TopDeviceData(1, "Air Conditioner", "365.40", "IDR. 2.100.000", sasiColor.yellow50),
            TopDeviceData(2, "Wooden Lamp", "365.40", "IDR. 2.100.000", sasiColor.black50),
            TopDeviceData(3, "Acrylic Lamp", "365.40", "IDR. 2.100.000", sasiColor.purple50)
        )
    }
    Card(
        modifier = modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(sasiColor.grey50),
        border = BorderStroke(1.dp, sasiColor.grey600),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp).fillMaxSize()) {
            Text("Top 3 Device Usage", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                topDevicesData.forEach { roomData ->
                    TopDeviceListItem(data = roomData, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TopDeviceListItem(data: TopDeviceData, modifier : Modifier = Modifier){
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(sasiColor.grey50),
        border = BorderStroke(1.dp, sasiColor.grey600)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(color = data.rankColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${data.rank}",
                    modifier = Modifier.padding(horizontal = 4.dp),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    data.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Start
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = "Flash Icon",
                        tint = sasiColor.yellow50,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(shape = CircleShape)
                            .background(sasiColor.yellow500)
                            .padding(2.dp)
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                data.usage, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false
                                    )
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "kWh", fontSize = 10.sp, color = Color.Gray,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false
                                    )
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            data.cost, fontSize = 10.sp, color = sasiColor.purple500,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(
                                    includeFontPadding = false
                                )
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoomUsageCard(data: RoomUsageData){
    Card(
        colors = CardDefaults.cardColors(sasiColor.grey50),
        border = BorderStroke(1.dp, sasiColor.grey600),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Rounded.MeetingRoom,
                    contentDescription = "Flash Icon",
                    tint = sasiColor.purple500,
                    modifier = Modifier.size(36.dp)
                        .background(sasiColor.purple50, RoundedCornerShape(4.dp))
                        .padding(4.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = "Flash Icon",
                        tint = sasiColor.yellow50,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(shape = CircleShape)
                            .background(sasiColor.yellow500)
                            .padding(2.dp)
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                data.usage, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false
                                    )
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "kWh", fontSize = 10.sp, color = Color.Gray,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false
                                    )
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            data.cost, fontSize = 10.sp, color = sasiColor.purple500,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(
                                    includeFontPadding = false
                                )
                            )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                data.roomName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "${data.deviceCount} Devices", fontSize = 10.sp, color = sasiColor.black300,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                ),
                modifier = Modifier.background(sasiColor.grey600, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}
