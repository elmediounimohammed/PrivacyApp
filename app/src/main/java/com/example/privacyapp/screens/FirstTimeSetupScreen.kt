//package com.example.privacyapp.screens
//
//import android.net.Uri
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import androidx.camera.view.PreviewView
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.viewinterop.AndroidView
//import com.example.privacyapp.data.CameraManager
//import com.example.privacyapp.data.PreferencesManager
//import kotlinx.coroutines.launch
//import coil.compose.AsyncImage
//import com.example.privacyapp.navigation.Screen
//
//import androidx.compose.ui.layout.ContentScale
//
//
//@Composable
//fun FirstTimeSetupScreen(
//    cameraManager: CameraManager,
//    preferencesManager: PreferencesManager,
//    onSetupComplete: () -> Unit
//) {
//    var isCameraVisible by remember { mutableStateOf(false) }
//    var isProcessing by remember { mutableStateOf(false) }
//    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
//    val scope = rememberCoroutineScope()
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        if (!isCameraVisible) {
//            Text(
//                text = "First Time Setup",
//                style = MaterialTheme.typography.headlineMedium
//            )
//            Text(
//                text = "Please take a picture of your face for security.",
//                modifier = Modifier.padding(vertical = 16.dp)
//            )
//            Button(onClick = { isCameraVisible = true }) {
//                Text("Start Camera")
//            }
//        } else {
//            Box(modifier = Modifier.weight(1f)) {
//                if (capturedImageUri == null) {
//                    AndroidView(
//                        factory = { context ->
//                            PreviewView(context).also { previewView ->
//                                scope.launch {
//                                    cameraManager.startCamera(previewView)
//                                }
//                            }
//                        },
//                        modifier = Modifier.fillMaxSize()
//                    )
//
//                    Button(
//                        onClick = {
//                            isProcessing = true
//                            cameraManager.captureReference { success ->  // Modified this line
//                                isProcessing = false
//                                if (success) {
//                                    preferencesManager.getReferenceImageUri()?.let { uri ->
//                                        capturedImageUri = Uri.parse(uri)
//                                    }
//                                }
//                            }
//                        },
//                        modifier = Modifier
//                            .align(Alignment.BottomCenter)
//                            .padding(bottom = 32.dp),
//                        enabled = !isProcessing
//                    ) {
//                        Text(if (isProcessing) "Processing..." else "Take Picture")
//                    }
//                } else {
//                    AsyncImage(
//                        model = capturedImageUri,
//                        contentDescription = "Captured face",
//                        modifier = Modifier.fillMaxSize(),
//                        contentScale = ContentScale.Crop  // Add this line
//                    )
//
//                    Row(
//                        modifier = Modifier
//                            .align(Alignment.BottomCenter)
//                            .padding(bottom = 32.dp)
//                    ) {
//                        Button(
//                            onClick = { capturedImageUri = null },
//                            modifier = Modifier.padding(end = 8.dp)
//                        ) {
//                            Text("Retake")
//                        }
//                        Button(onClick = onSetupComplete) {
//                            Text("Continue")
//                        }
//                    }
//                }
//            }
//        }
//    }
//}