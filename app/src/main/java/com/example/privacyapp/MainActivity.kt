package com.example.privacyapp

import CaptureScreen
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.privacyapp.data.CameraManager
import com.example.privacyapp.data.PreferencesManager
import com.example.privacyapp.screens.*
import com.example.privacyapp.ui.theme.PrivacyAppTheme
import com.example.privacyapp.navigation.Screen

class MainActivity : ComponentActivity() {
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var cameraManager: CameraManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferencesManager = PreferencesManager(this)
        cameraManager = CameraManager(this, this)
        try {
            preferencesManager = PreferencesManager(this)
            cameraManager = CameraManager(this, this)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error initializing managers", e)
        }
        setContent {
            PrivacyAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Pin.route
                    ) {
//                        composable(Screen.Setup.route) {
//                            FirstTimeSetupScreen(
//                                cameraManager = cameraManager,
//                                preferencesManager = preferencesManager,
//                                onSetupComplete = {
//                                    preferencesManager.setFirstTimeDone()
//                                    navController.navigate(Screen.Pin.route) {
//                                        popUpTo(Screen.Setup.route) { inclusive = true }
//                                    }
//                                }
//                            )
//                        }
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
                            AppSelectionScreen(
                                appList = emptyList(),  // This will be handled by the screen itself
                                onAppSelected = { },    // This will be handled by the screen itself
                                onSaveSelection = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraManager.stopCamera()
    }
}