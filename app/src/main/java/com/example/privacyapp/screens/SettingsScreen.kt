package com.example.privacyapp.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.privacyapp.components.ClickableSettingItem
import com.example.privacyapp.components.SettingsSection
import com.example.privacyapp.components.SliderSettingItem
import com.example.privacyapp.components.SwitchSettingItem
import com.example.privacyapp.data.CameraManager
import com.example.privacyapp.data.PreferencesManager
import coil.compose.AsyncImage

import com.example.privacyapp.navigation.NavigationGraph
import com.example.privacyapp.navigation.Screen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    preferencesManager: PreferencesManager,
    cameraManager: CameraManager
) {
    var notificationsEnabled by rememberSaveable { mutableStateOf(true) }
    var vibrationEnabled by rememberSaveable { mutableStateOf(true) }
    var autoStartEnabled by rememberSaveable { mutableStateOf(false) }
    var sensitivityLevel by rememberSaveable { mutableStateOf(0.7f) }
    var showChangePinDialog by rememberSaveable { mutableStateOf(false) }
    var showImageDialog by remember { mutableStateOf(false) }
    val referenceImageUri by remember {
        mutableStateOf(preferencesManager.getReferenceImageUri())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                // Security Section
                SettingsSection(title = "Security") {
                    ClickableSettingItem(
                        title = "Change PIN",
                        onClick = { showChangePinDialog = true }
                    )
                    ClickableSettingItem(
                        title = "Reference Face Image",
                        subtitle = if (referenceImageUri != null) "Change Image" else "Set Image",
                        onClick = { showImageDialog = true }
                    ) {
                        if (referenceImageUri != null) {
                            AsyncImage(
                                model = referenceImageUri,
                                contentDescription = "Reference face",
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }

                // Notifications Section
                SettingsSection(title = "Notifications") {
                    SwitchSettingItem(
                        title = "Enable Notifications",
                        description = "Show alerts when someone is detected",
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                    SwitchSettingItem(
                        title = "Vibration",
                        description = "Vibrate on detection",
                        checked = vibrationEnabled,
                        onCheckedChange = { vibrationEnabled = it }
                    )
                }

                // General Settings Section
                SettingsSection(title = "General") {
                    SwitchSettingItem(
                        title = "Auto-start on boot",
                        description = "Start monitoring when device boots",
                        checked = autoStartEnabled,
                        onCheckedChange = { autoStartEnabled = it }
                    )
                    SliderSettingItem(
                        title = "Detection Sensitivity",
                        value = sensitivityLevel,
                        onValueChange = { sensitivityLevel = it }
                    )
                }

                // About Section
                SettingsSection(title = "About") {
                    ClickableSettingItem(
                        title = "Privacy Policy",
                        onClick = { /* Open privacy policy */ }
                    )
                    ClickableSettingItem(
                        title = "Version",
                        onClick = { /* Show version info */ }
                    )
                }
            }
        }

        if (showChangePinDialog) {
            ChangePinDialog(
                onDismiss = { showChangePinDialog = false },
                onConfirm = { newPin ->
                    preferencesManager.savePin(newPin)
                    showChangePinDialog = false
                },
                preferencesManager = preferencesManager
            )
        }

        if (showImageDialog) {
            AlertDialog(
                onDismissRequest = { showImageDialog = false },
                title = { Text("Reference Face Image") },
                text = {
                    Column {
                        if (referenceImageUri != null) {
                            AsyncImage(
                                model = referenceImageUri,
                                contentDescription = "Reference face",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showImageDialog = false
                            try {
                                // Add a small delay to ensure dialog is dismissed before navigation
                                navController.navigate(Screen.Capture.route)
                            } catch (e: Exception) {
                                Log.e("SettingsScreen", "Navigation error", e)
                            }
                        }
                    ) {
                        Text(if (referenceImageUri != null) "Change Image" else "Take Picture")
                    }
                },
                dismissButton = if (referenceImageUri != null) {
                    {
                        Button(
                            onClick = {
                                preferencesManager.clearReferenceImage()
                                showImageDialog = false
                            }
                        ) {
                            Text("Remove Image")
                        }
                    }
                } else null
            )
        }
    }
}

@Composable
fun ChangePinDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    preferencesManager: PreferencesManager
) {
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change PIN") },
        text = {
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = currentPin,
                    onValueChange = { if (it.length <= 4) currentPin = it },
                    label = { Text("Current PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 4) newPin = it },
                    label = { Text("New PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 4) confirmPin = it },
                    label = { Text("Confirm New PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )

                if (error.isNotEmpty()) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        currentPin != preferencesManager.getPin() ->
                            error = "Current PIN is incorrect"
                        newPin.length != 4 ->
                            error = "PIN must be 4 digits"
                        !newPin.all { it.isDigit() } ->
                            error = "PIN must contain only numbers"
                        newPin != confirmPin ->
                            error = "PINs don't match"
                        else -> onConfirm(newPin)
                    }
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
