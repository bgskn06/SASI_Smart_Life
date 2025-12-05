package com.example.sasi_smart_life.view.master

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.sasi_smart_life.Routes
import com.example.sasi_smart_life.view.theme.sasiColor

@Composable
fun BottomNavBar(
    navController: NavController,
    currentRoute: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = CircleShape,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(100.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.GridView,
                    contentDescription = "Analytics",
                    tint = if (currentRoute == Routes.ANALYTICS) sasiColor.purple500 else sasiColor.black300,
                    modifier = Modifier.clickable {
                        navController.navigate(Routes.ANALYTICS) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Settings",
                    tint = if (currentRoute == Routes.SETTINGS) sasiColor.purple500 else sasiColor.black300,
                    modifier = Modifier.clickable {
                        navController.navigate(Routes.SETTINGS) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(sasiColor.blue500)
                .clickable { navController.popBackStack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Home,
                contentDescription = "Home",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}