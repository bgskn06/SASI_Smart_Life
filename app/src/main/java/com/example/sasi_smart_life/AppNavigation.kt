package com.example.sasi_smart_life

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sasi_smart_life.view.AnalyticScreen
import com.example.sasi_smart_life.view.AuthScreen
import com.example.sasi_smart_life.view.DeviceManagementScreen
import com.example.sasi_smart_life.view.FloorPlanScreen
import com.example.sasi_smart_life.view.SettingScreen
import com.example.sasi_smart_life.viewModel.AuthViewModel
import com.example.sasi_smart_life.viewModel.MainViewModel

object Routes {
    const val AUTH = "Auth"
    const val FLOOR_PLAN = "FloorPlan"
    const val SETTINGS = "Setting"
    const val DEVICE_MANAGEMENT = "device_management_screen/{roomId}"
    const val ANALYTICS = "Analytic"
}

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val appState by viewModel.uiState.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavHost(navController = navController, startDestination = if (appState.isLoggedIn) Routes.FLOOR_PLAN else Routes.AUTH) {
        composable(Routes.AUTH) {
            val authViewModel: AuthViewModel = viewModel()
            AuthScreen(viewModel = authViewModel, appState = appState, onLoginSuccess = {
                navController.navigate(Routes.FLOOR_PLAN) {
                    popUpTo(Routes.AUTH) { inclusive = true }
                }
            })
        }
        composable(Routes.FLOOR_PLAN) {
            FloorPlanScreen(
                viewModel = viewModel,
                onNavigateToSettings = {
                    // Pass the home object if needed in the future, for now just navigate
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }
        composable(Routes.SETTINGS) {
            SettingScreen(
                viewModel = viewModel,
                onNavigateToSettings = {},
                navController = navController,
                currentRoute = currentRoute
            )
        }
        composable(
            route = Routes.DEVICE_MANAGEMENT,
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId")
            DeviceManagementScreen(
                viewModel = viewModel,
                devices = appState.devices.filter { it.roomId == roomId },
                roomId = roomId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.ANALYTICS) {
            AnalyticScreen(
                navController = navController,
                currentRoute = currentRoute
            )
        }
    }
}
