package com.example.smart_vision_aid

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Size
import com.example.smart_vision_aid.ui.theme.GreenPrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.smart_vision_aid.ui.theme.GreenSecondary
import java.util.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import org.opencv.android.OpenCVLoader
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.MatOfPoint2f
import org.opencv.core.MatOfPoint
import java.io.File
import java.io.FileOutputStream
import com.example.smart_vision_aid.helper.OcrHelper
import kotlinx.coroutines.CoroutineScope


//@Composable
//fun EditScreen(navController: NavController, imageUri: String) {
//    val context = LocalContext.current
//    var isOpenCvLoaded by remember { mutableStateOf(false) }
//    val uri = remember { Uri.parse(imageUri) }
//    var processingState by remember { mutableStateOf("Confirm") }
//    var bitmapState by remember { mutableStateOf<Bitmap?>(null) }
//    var isLoading by remember { mutableStateOf(true) }
//    var errorMessage by remember { mutableStateOf<String?>(null) }
//    val tessBaseAPI = by remember { OcrHelper.initTesseract(context) }
//    val scope = rememberCoroutineScope()
//    var isTessInitialized by remember { mutableStateOf(false) }
//    var initializationError by remember { mutableStateOf<String?>(null) }
//
//    LaunchedEffect(Unit) {
//        isOpenCvLoaded = try {
//            OpenCVLoader.initDebug()
//            true
//        } catch (e: Exception) {
//            false
//        }
//    }
//
//
//    // Load image only (no OCR processing)
//    // In EditScreen's LaunchedEffect:
//    LaunchedEffect(uri) {
//        try {
//            val request = ImageRequest.Builder(context)
//                .data(uri)
//                .size(Size.ORIGINAL) // Maintain original size
//                .allowHardware(false)
//                .build()
//
//            val drawable = context.imageLoader.execute(request).drawable
//            bitmapState = (drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
//                ?.copy(Bitmap.Config.ARGB_8888, true) // Preserve quality
//            Log.d("EditScreen", "Image loaded successfully")
//            isLoading = false
//        } catch (e: Exception) {
//            errorMessage = "Image load failed: ${e.message}"
//            Log.e("EditScreen", "Error loading image: ${e.message}", e)
//            isLoading = false
//        }
//    }
//
//    Column(modifier = Modifier.fillMaxSize()) {
//        // Image Preview
//        Box(
//            modifier = Modifier
//                .weight(1f)
//                .fillMaxWidth(),
//            contentAlignment = Alignment.Center
//        ) {
//            when {
//                isLoading -> CircularProgressIndicator()
//                bitmapState != null -> Image(
//                    bitmap = bitmapState!!.asImageBitmap(),
//                    contentDescription = "Image Preview",
//                    modifier = Modifier.fillMaxSize()
//                )
//                else -> Text("No image available", color = MaterialTheme.colorScheme.error)
//            }
//        }
//
//        // Error Message
//        errorMessage?.let { message ->
//            Text(
//                text = message,
//                color = MaterialTheme.colorScheme.error,
//                modifier = Modifier.padding(16.dp)
//            )
//        }
//
//        // Control Buttons
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(8.dp)
//                .padding(horizontal = 16.dp, vertical = 8.dp),
//            horizontalArrangement = Arrangement.SpaceEvenly
//        ) {
//            Button(
//                onClick = { navController.navigate("rotate/${Uri.encode(imageUri)}") },
//                modifier = Modifier
//                    .weight(1f)
//                    .height(64.dp)
//                    .padding(end = 8.dp),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFF2196F3), // Blue for editing actions
//                    contentColor = Color.White
//                ),
//            ) {
//                Icon(Icons.Default.RotateRight, "Rotate")
//                Spacer(Modifier.width(8.dp))
//                Text(
//                    text = "Rotate",
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.Medium
//                )
//            }
//
//            Button(
//                onClick = {
//                    // Create a new file to save the cropped image
//                    val file = File(context.cacheDir, "edit_${UUID.randomUUID()}.png")
//
//                    // Save the bitmap to the file
//                    bitmapState?.let {
//                        FileOutputStream(file).use { out ->
//                            it.compress(Bitmap.CompressFormat.PNG, 100, out)
//                        }
//                    }
//
//                    // Create URI for the file
//                    val fileUri: Uri = FileProvider.getUriForFile(
//                        context,
//                        "${context.packageName}.provider",
//                        file
//                    )
//
//                    // Navigate to crop screen and pass the Uri
//                    navController.navigate("crop/${Uri.encode(fileUri.toString())}")
//                },
//                modifier = Modifier
//                    .weight(1f)
//                    .height(64.dp)
//                    .padding(start = 8.dp),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFF2196F3), // Blue for editing actions
//                    contentColor = Color.White
//                ),
//            ) {
//                Icon(Icons.Default.Crop, "Crop")
//                Spacer(Modifier.width(8.dp))
//                Text(
//                    text = "Crop",
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.Medium
//                )
//            }
//
//        }
//
//        // Confirm/Cancel
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp, vertical = 8.dp),
//            horizontalArrangement = Arrangement.SpaceEvenly
//        ) {
//            Button(
//                onClick = { navController.popBackStack() },
//                modifier = Modifier
//                    .weight(1f)
//                    .height(64.dp)
//                    .padding(end = 8.dp),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFFF44336), // Red for Cancel
//                    contentColor = Color.White
//                ),
//            ) {
//                Text(
//                    text = "Cancel",
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.Medium
//                )
//            }
//
//            Button(
//                onClick = {
//                    processingState = "Processing..."
//                    scope.launch {
//                        try {
//                            isLoading = true
//                            errorMessage = null
//
//                            val processedBitmap = bitmapState?.let { preprocessImage(it) }
//                                ?: throw Exception("No image available")
//
//                            val text = OcrHelper.performOCR(processedBitmap)
//                            navController.navigate("processing/${Uri.encode(text)}")
//                            // No reset to "Confirm" here; navigation handles the transition
//                        } catch (e: Exception) {
//                            errorMessage = "OCR failed: ${e.message}"
//                            processingState = "Processing Failed"
//                        } finally {
//                            isLoading = false
//                        }
//                    }
//                },
//                modifier = Modifier
//                    .weight(1f)
//                    .height(64.dp)
//                    .padding(start = 8.dp),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFF4CAF50),
//                    contentColor = Color.White
//                ),
//                enabled = processingState == "Confirm"
//            ) {
//                Text(
//                    text = processingState,
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.Medium
//                )
//            }
//        }
//    }
//}
//
//private fun preprocessImage(bitmap: Bitmap): Bitmap {
//    return DocumentScanHelper.enhanceBitmap(bitmap)
//}
//



@Composable
fun EditScreen(navController: NavController, imageUri: String) {
    val context = LocalContext.current
    val uri = remember { Uri.parse(imageUri) }

    // State variables
    var isOpenCvLoaded by remember { mutableStateOf(false) }
    var processingState by remember { mutableStateOf("Confirm") }
    var bitmapState by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isTessInitialized by remember { mutableStateOf(false) }
    var tessInitializationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        // Initialize OpenCV
        isOpenCvLoaded = try {
            OpenCVLoader.initDebug()
            true
        } catch (e: Exception) {
            Log.e("EditScreen", "OpenCV init failed", e)
            false
        }

        // Initialize Tesseract
        try {
            OcrHelper.initTesseract(context)
            isTessInitialized = true
        } catch (e: Exception) {
            tessInitializationError = "Retry failed: ${e.message}"
        }


        // Initialize Tesseract in background
        withContext(Dispatchers.IO) {
            try {
                OcrHelper.initTesseract(context)
                isTessInitialized = true
                Log.d("EditScreen", "Tesseract initialized successfully")
            } catch (e: Exception) {
                tessInitializationError = "OCR init failed: ${e.message}"
                Log.e("EditScreen", "Tesseract initialization failed", e)
            }
        }
    }

    // Load image
    LaunchedEffect(uri) {
        try {
            val request = ImageRequest.Builder(context)
                .data(uri)
                .size(Size.ORIGINAL)
                .allowHardware(false)
                .build()

            val drawable = context.imageLoader.execute(request).drawable
            bitmapState = (drawable as? BitmapDrawable)?.bitmap
                ?.copy(Bitmap.Config.ARGB_8888, true)
            Log.d("EditScreen", "Image loaded successfully")
        } catch (e: Exception) {
            errorMessage = "Image load failed: ${e.message}"
            Log.e("EditScreen", "Error loading image", e)
        } finally {
            isLoading = false
        }
    }

    // Show loading/initialization states
    if (isLoading || !isOpenCvLoaded || !isTessInitialized) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Show initialization errors
            if (tessInitializationError != null) {
                Text(
                    text = tessInitializationError!!,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    // Retry initialization
                    tessInitializationError = null
                    isTessInitialized = false
                }) {
                    Text("Retry OCR Initialization")
                }
                return@Column
            }

            // Show loading states
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))

            val loadingText = buildString {
                if (isLoading) append("Loading image... ")
                if (!isOpenCvLoaded) append("Initializing OpenCV... ")
                if (!isTessInitialized) append("Initializing OCR... ")
            }

            Text(loadingText.trim())
        }
        return
    }

    // Main UI
    Column(modifier = Modifier.fillMaxSize()) {
        // Image Preview
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when {
                bitmapState != null -> Image(
                    bitmap = bitmapState!!.asImageBitmap(),
                    contentDescription = "Image Preview",
                    modifier = Modifier.fillMaxSize()
                )
                else -> Text("No image available", color = MaterialTheme.colorScheme.error)
            }
        }

        // Error Message
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        }

        // Control Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { navController.navigate("rotate/${Uri.encode(imageUri)}") },
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .padding(end = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3),
                    contentColor = Color.White
                ),
            ) {
                Icon(Icons.Default.RotateRight, "Rotate")
                Spacer(Modifier.width(8.dp))
                Text("Rotate", fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }

            Button(
                onClick = {
                    val file = File(context.cacheDir, "edit_${UUID.randomUUID()}.png")
                    bitmapState?.let {
                        FileOutputStream(file).use { out ->
                            it.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                    }
                    val fileUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )
                    navController.navigate("crop/${Uri.encode(fileUri.toString())}")
                },
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .padding(start = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3),
                    contentColor = Color.White
                ),
            ) {
                Icon(Icons.Default.Crop, "Crop")
                Spacer(Modifier.width(8.dp))
                Text("Crop", fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }
        }

        // Confirm/Cancel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .padding(end = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF44336),
                    contentColor = Color.White
                ),
            ) {
                Text("Cancel", fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }

            Button(
                onClick = {
                    processingState = "Processing..."
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val processedBitmap = bitmapState?.let {
                                DocumentScanHelper.enhanceBitmap(it)
                            } ?: throw Exception("No image available")

                            val text = OcrHelper.performOCR(processedBitmap)
                            withContext(Dispatchers.Main) {
                                navController.navigate("processing/${Uri.encode(text)}")
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                errorMessage = "OCR failed: ${e.message}"
                                processingState = "Processing Failed"
                            }
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .padding(start = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                ),
                enabled = processingState == "Confirm" && bitmapState != null
            ) {
                Text(processingState, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}