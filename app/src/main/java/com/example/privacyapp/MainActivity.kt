package com.example.privacyapp
import com.example.privacyapp.data.CameraManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.privacyapp.data.PreferencesManager
import com.example.privacyapp.screens.MainScreen
import com.example.privacyapp.screens.PinScreen
import com.example.privacyapp.screens.SettingsScreen
import com.example.privacyapp.ui.theme.PrivacyAppTheme

class MainActivity : ComponentActivity() {
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var cameraManager: CameraManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferencesManager = PreferencesManager(this)
        cameraManager = CameraManager(this, this)

        setContent {
            PrivacyAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "pin"
                    ) {
                        composable("pin") {
                            PinScreen(
                                navController = navController,
                                preferencesManager = preferencesManager
                            )
                        }
                        composable("main") {
                            MainScreen(
                                navController = navController,
                                cameraManager = cameraManager
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                navController = navController,
                                preferencesManager = preferencesManager
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