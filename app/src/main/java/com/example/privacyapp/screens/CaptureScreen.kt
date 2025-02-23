import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.privacyapp.data.CameraManager
import com.example.privacyapp.data.PreferencesManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
@Composable
fun CaptureScreen(
    cameraManager: CameraManager,
    preferencesManager: PreferencesManager,
    onComplete: () -> Unit
) {
    var isProcessing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        // Request camera permission if needed
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Handle permission request
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                PreviewView(context).also { previewView ->
                    scope.launch {
                        try {
                            cameraManager.startCamera(previewView)
                        } catch (e: Exception) {
                            Log.e("CaptureScreen", "Error starting camera", e)
                            // Show error to user
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        Button(
            onClick = {
                isProcessing = true
                cameraManager.captureReference { success ->
                    if (success) {
                        scope.launch {
                            delay(500) // Give time for the image to be saved
                            isProcessing = false
                            onComplete()
                        }
                    } else {
                        isProcessing = false
                        // Show error to user
                    }
                } }, modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            enabled = !isProcessing
        ) {
            Text(if (isProcessing) "Processing..." else "Take Picture")
        }
    }
}