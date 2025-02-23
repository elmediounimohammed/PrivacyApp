package com.example.privacyapp.navigation

import CaptureScreen
import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.privacyapp.data.CameraManager
import com.example.privacyapp.data.PreferencesManager
import com.example.privacyapp.data.manager.AppListManager
import com.example.privacyapp.data.model.AppInfo
import com.example.privacyapp.data.repository.AppRepository
import com.example.privacyapp.screens.*
import com.example.privacyapp.navigation.NavigationGraph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.Modifier


sealed class Screen(val route: String) {
    object Setup : Screen("setup")
    object Pin : Screen("pin")
    object Main : Screen("main")
    object Settings : Screen("settings")
    object Capture : Screen("capture")
    object AppSelection : Screen("app_selection")


    // Add a companion object to access routes easily
    companion object Routes {
        const val SETUP = "setup"
        const val PIN = "pin"
        const val MAIN = "main"
        const val SETTINGS = "settings"
        const val CAPTURE = "capture"
        const val APPSELECTION = "app_selection"

    }
}

@Composable
fun NavigationGraph(
    navController: NavHostController,
    cameraManager: CameraManager,
    preferencesManager: PreferencesManager
) {
    NavHost(
        navController = navController,
        startDestination =Screen.Pin.route
    ) {
//        composable(Screen.Setup.route) {
//            FirstTimeSetupScreen(
//                cameraManager = cameraManager,
//                preferencesManager = preferencesManager,
//                onSetupComplete = {
//                    preferencesManager.setFirstTimeDone()
//                    navController.navigate(Screen.Pin.route) {
//                        popUpTo(Screen.Setup.route) { inclusive = true }
//                    }
//                }
//            )
//        }

        composable(Screen.Capture.route) {
            CaptureScreen(
                cameraManager = cameraManager,
                preferencesManager = preferencesManager,
                onComplete = {
                    // Use navigate instead of popBackStack to ensure proper navigation
                    navController.navigate(Screen.Settings.route) {
                        popUpTo(Screen.Capture.route) { inclusive = true }
                    }
                }

            )
        }

        composable(Screen.Pin.route) {
            PinScreen(
                navController = navController,
                preferencesManager = preferencesManager
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                navController = navController,
                cameraManager = cameraManager
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController,
                preferencesManager = preferencesManager,
                cameraManager = cameraManager
            )
        }
        composable(Screen.Setup.route) {
//            FirstTimeSetupScreen(
//                cameraManager = cameraManager,
//                preferencesManager = preferencesManager,
//                onSetupComplete = {
//                    preferencesManager.setFirstTimeDone()
//                    navController.navigate(Screen.Pin.route) {
//                        popUpTo(Screen.Setup.route) { inclusive = true }
//                    }
//                }
//            )
        }
        composable(Screen.Pin.route) {
            PinScreen(
                navController = navController,
                preferencesManager = preferencesManager
            )
        }
        composable(Screen.Main.route) {
            MainScreen(
                navController = navController,
                cameraManager = cameraManager
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController,
                preferencesManager = preferencesManager,
                cameraManager = cameraManager
            )
        }
        composable(Screen.Capture.route) {
            CaptureScreen(
                cameraManager = cameraManager,
                preferencesManager = preferencesManager,
                onComplete = { navController.popBackStack() }
            )
        }

        composable(Screen.AppSelection.route) {
            val context = LocalContext.current
            var appList by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
            var isLoading by remember { mutableStateOf(true) }
            val scope = rememberCoroutineScope()

            Log.d("NavigationDebug", "Entering AppSelection screen")
            LaunchedEffect(Unit) {
                Log.d("NavigationDebug", "LaunchedEffect triggered")
                withContext(Dispatchers.IO) {
                    try {
                        val manager = AppListManager(context)
                        Log.d("NavigationDebug", "Created AppListManager")
                        val apps = manager.getInstalledApps()
                        Log.d("NavigationDebug", "Got apps list, size: ${apps.size}")

                        withContext(Dispatchers.Main) {
                            appList = apps
                            isLoading = false
                            Log.d("NavigationDebug", "Updated UI state")
                        }
                    } catch (e: Exception) {
                        Log.e("NavigationDebug", "Error: ${e.message}")
                    }
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                )
            } else {
                Log.d("AppSelection", "Rendering AppSelectionScreen with ${appList.size} apps")
                AppSelectionScreen(
                    appList = appList,
                    onAppSelected = { app ->
                        appList = appList.map { currentApp ->
                            if (currentApp.packageName == app.packageName) {
                                currentApp.copy(isSelected = !currentApp.isSelected)
                            } else currentApp
                        }
                    },
                    onSaveSelection = {
                        navController.popBackStack()
                    }
                )
            }
        }
        }
    }
