package com.example.smart_vision_aid

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.camera.core.Preview
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.io.File
import java.util.concurrent.Executors
import androidx.compose.foundation.Image
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.viewinterop.AndroidView
import org.opencv.android.OpenCVLoader
import org.opencv.imgproc.Imgproc
import androidx.compose.ui.graphics.Path
import java.io.ByteArrayOutputStream


//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CameraScreen(navController: NavController) {
//    val context = LocalContext.current
//    val lifecycleOwner = LocalLifecycleOwner.current
//    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
//
//    var showPermissionDialog by remember { mutableStateOf(false) }
//    val imageCapture = remember { ImageCapture.Builder().build() }
//
//    // Initialize OpenCV (optional if needed later)
//    val openCvInitialized = remember { mutableStateOf(false) }
//    LaunchedEffect(Unit) {
//        openCvInitialized.value = OpenCVLoader.initDebug()
//    }
//
//    val permissionLauncher = rememberLauncherForActivityResult(
//        ActivityResultContracts.RequestPermission()
//    ) { granted ->
//        showPermissionDialog = !granted
//    }
//
//    LaunchedEffect(Unit) {
//        permissionLauncher.launch(Manifest.permission.CAMERA)
//    }
//
//    Box(modifier = Modifier.fillMaxSize()) {
//        if (openCvInitialized.value) {
//            AndroidView(
//                factory = { ctx ->
//                    val previewView = PreviewView(ctx)
//                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
//
//                    cameraProviderFuture.addListener({
//                        try {
//                            val cameraProvider = cameraProviderFuture.get()
//                            val preview = Preview.Builder().build().also {
//                                it.setSurfaceProvider(previewView.surfaceProvider)
//                            }
//
//                            cameraProvider.unbindAll()
//                            cameraProvider.bindToLifecycle(
//                                lifecycleOwner,
//                                CameraSelector.DEFAULT_BACK_CAMERA,
//                                preview,
//                                imageCapture
//                            )
//                        } catch (e: Exception) {
//                            Log.e("CameraScreen", "Camera setup failed", e)
//                        }
//                    }, ContextCompat.getMainExecutor(ctx))
//
//                    previewView
//                },
//                modifier = Modifier.fillMaxSize()
//            )
//        }
//
//        Column(
//            modifier = Modifier.fillMaxSize(),
//            verticalArrangement = Arrangement.SpaceBetween
//        ) {
//            TopAppBar(
//                title = { Text("Point Camera at Text", color = Color.White) },
//                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF4CAF50))
//            )
//
//            Button(
//                onClick = {
//                    val file = File(context.cacheDir, "captured.jpg")
//                    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
//
//                    imageCapture.takePicture(
//                        outputOptions,
//                        ContextCompat.getMainExecutor(context),
//                        object : ImageCapture.OnImageSavedCallback {
//                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
//                                val file = File(context.cacheDir, "captured.jpg")
//
//                                // Decode raw bitmap
//                                val rawBitmap = BitmapFactory.decodeFile(file.absolutePath)
//
//                                // Correct orientation
//                                val correctedBitmap = rawBitmap.rotateBitmapIfRequired(file.absolutePath)
//
//                                // Overwrite the saved file with the corrected bitmap
//                                file.outputStream().use { outputStream ->
//                                    correctedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
//                                }
//
//                                // Navigate to perspective screen with the correctly rotated image path
//                                navController.navigate("perspective/${Uri.encode(file.absolutePath)}")
//                            }
//
//
//                            override fun onError(exc: ImageCaptureException) {
//                                Log.e("CameraScreen", "Capture failed", exc)
//                            }
//                        }
//                    )
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp)
//                    .height(70.dp),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFF4CAF50),
//                    contentColor = Color.White
//                )
//            ) {
//                Text("Capture Text", fontSize = MaterialTheme.typography.titleLarge.fontSize)
//            }
//        }
//    }
//
//    if (showPermissionDialog) {
//        AlertDialog(
//            onDismissRequest = { showPermissionDialog = false },
//            title = { Text("Camera Permission Required") },
//            text = { Text("This app needs access to your camera to scan text") },
//            confirmButton = {
//                TextButton(onClick = { showPermissionDialog = false }) {
//                    Text("OK")
//                }
//            }
//        )
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var showPermissionDialog by remember { mutableStateOf(false) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    // Initialize OpenCV (optional if needed later)
    val openCvInitialized = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        openCvInitialized.value = OpenCVLoader.initDebug()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        showPermissionDialog = !granted
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (openCvInitialized.value) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                        cameraProviderFuture.addListener({
                            try {
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }

                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageCapture
                                )
                            } catch (e: Exception) {
                                Log.e("CameraScreen", "Camera setup failed", e)
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Controls (TopAppBar and Button)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Button(
                onClick = {
                    val file = File(context.cacheDir, "captured.jpg")
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

                    imageCapture.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                val file = File(context.cacheDir, "captured.jpg")
                                val rawBitmap = BitmapFactory.decodeFile(file.absolutePath)
                                val correctedBitmap = rawBitmap.rotateBitmapIfRequired(file.absolutePath)
                                file.outputStream().use { outputStream ->
                                    correctedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                                }
                                navController.navigate("perspective/${Uri.encode(file.absolutePath)}")
                            }

                            override fun onError(exc: ImageCaptureException) {
                                Log.e("CameraScreen", "Capture failed", exc)
                            }
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                )
            ) {
                Text("Capture Text", fontSize = MaterialTheme.typography.titleLarge.fontSize)
            }
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Camera Permission Required") },
            text = { Text("This app needs access to your camera to scan text") },
            confirmButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}




