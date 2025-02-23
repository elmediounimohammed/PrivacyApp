package com.example.privacyapp.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.privacyapp.R
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import androidx.camera.core.ExperimentalGetImage
import java.io.File



@androidx.annotation.OptIn(ExperimentalGetImage::class)

class       CameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {
    private val preferencesManager = PreferencesManager(context)  // Add this line

    private var imageCapture: ImageCapture? = null
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var faceDetector: FaceDetector
    private var referenceImage: ByteArray? = null
    private var lastNotificationTime = 0L
    private val notificationCooldown = 5000L

    init {

        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.15f)
            .enableTracking()
            .build()

        faceDetector = FaceDetection.getClient(options)
        createNotificationChannel()
        loadReferenceImage()
    }

    private fun loadReferenceImage() {
        val preferences = context.getSharedPreferences("privacy_app", Context.MODE_PRIVATE)
        val encodedImage = preferences.getString("reference_face", null)
        referenceImage = encodedImage?.let { Base64.decode(it, Base64.DEFAULT) }
    }

    fun captureReference(onComplete: (Boolean) -> Unit) {
        val photoFile = File(
            context.getExternalFilesDir(null),
            "reference_face.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    output.savedUri?.let { uri ->
                        preferencesManager.setReferenceImageUri(uri.toString())
                        Log.d(TAG, "Image saved successfully: ${uri.path}")
                        onComplete(true)
                    } ?: run {
                        Log.e(TAG, "Error: Saved image URI is null")
                        onComplete(false)
                    }
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    onComplete(false)
                }
            }
        )
    }
    private fun processReferenceImage(image: ImageProxy, onComplete: (Boolean) -> Unit) {
        val mediaImage = image.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)

            faceDetector.process(inputImage)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty()) {
                        // Convert face to bitmap and then to bytes
                        val faceBitmap = faces[0].boundingBox.let { box ->
                            // Convert media image to bitmap
                            // This is a simplified version, you might need to implement proper image conversion
                            Bitmap.createBitmap(box.width(), box.height(), Bitmap.Config.ARGB_8888)
                        }
                        val stream = ByteArrayOutputStream()
                        faceBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                        referenceImage = stream.toByteArray()
                        saveReferenceImage(referenceImage!!)
                        onComplete(true)
                    } else {
                        onComplete(false)
                    }
                }
                .addOnFailureListener {
                    Log.e(TAG, "Face detection failed", it)
                    onComplete(false)
                }
        }
        image.close()
    }

    private fun saveReferenceImage(imageData: ByteArray) {
        context.getSharedPreferences("privacy_app", Context.MODE_PRIVATE)
            .edit()
            .putString("reference_face", Base64.encodeToString(imageData, Base64.DEFAULT))
            .apply()
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Privacy Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when someone is detected behind you"
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    suspend fun startCamera(previewView: PreviewView) = suspendCoroutine { continuation ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                preview = Preview.Builder().build()

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .apply {
                        setAnalyzer(cameraExecutor, createFaceAnalyzer())
                    }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    .build()

                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture,
                    imageAnalyzer
                )

                preview?.setSurfaceProvider(previewView.surfaceProvider)
                continuation.resume(Unit)

            } catch (e: Exception) {
                Log.e(TAG, "Use case binding failed", e)
                continuation.resume(Unit)
            }
        }, ContextCompat.getMainExecutor(context))
    }
    private fun createFaceAnalyzer(): ImageAnalysis.Analyzer {
        return ImageAnalysis.Analyzer { imageProxy ->
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.imageInfo.rotationDegrees
                )

                faceDetector.process(image)
                    .addOnSuccessListener { faces ->
                        if (faces.size > 1) {
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastNotificationTime > notificationCooldown) {
                                sendNotification()
                                lastNotificationTime = currentTime
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Face detection failed", e)
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            } else {
                imageProxy.close()
            }
        }
    }

    private fun sendNotification() {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Privacy Alert!")
            .setContentText("Someone was detected behind you")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    fun stopCamera() {
        try {
            cameraExecutor.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping camera", e)
        }
    }

    companion object {
        private const val TAG = "CameraManager"
        private const val CHANNEL_ID = "privacy_alerts"
        private const val NOTIFICATION_ID = 1
    }
}