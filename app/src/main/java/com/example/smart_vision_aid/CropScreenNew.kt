package com.example.smart_vision_aid

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smart_vision_aid.cropUtils.*
import com.example.smart_vision_aid.ui.theme.SmartVisionAidTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

//@Composable
//fun CropScreenNew(
//    navController: NavController,
//    imageUri: Uri
//) {
//    val context = LocalContext.current
//    val cropper = remember { ImageCropper() }
//    val scope = rememberCoroutineScope()
//    var cropState by remember { mutableStateOf<CropState?>(null) }
//    var isCropping by remember { mutableStateOf(false) }
//    var errorMessage by remember { mutableStateOf<String?>(null) }
//
//    // Initialize CropState from URI
//    LaunchedEffect(imageUri) {
//        cropState = withContext(Dispatchers.IO) {
//            imageUri.toImageSrc(context)?.let { CropState(it) }
//        }
//    }
//
//    cropState?.let { state ->
//        CompositionLocalProvider(LocalCropperStyle provides DefaultCropperStyle) {
//            Column(modifier = Modifier.fillMaxSize()) {
//
//                // Crop Preview
//                Box(modifier = Modifier.weight(1f)) {
//                    CropperPreview(state = state, modifier = Modifier.fillMaxSize())
//                }
//
//                // Confirm and Cancel Controls
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp),
//                    horizontalArrangement = Arrangement.SpaceEvenly
//                ) {
//                    IconButton(
//                        onClick = { navController.popBackStack() },
//                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.error)
//                    ) {
//                        Icon(Icons.Default.Close, contentDescription = "Cancel")
//                    }
//
//                    IconButton(
//                        onClick = {
//                            scope.launch {
//                                isCropping = true
//                                val result = cropper.crop(
//                                    uri = imageUri,
//                                    context = context,
//                                    maxResultSize = IntSize(1080, 1920)
//                                )
//                                isCropping = false
//
//                                when (result) {
//                                    is CropResult.Success -> {
//                                        val bitmap: Bitmap = result.bitmap.asAndroidBitmap()
//                                        val savedUri = saveBitmapToFile(context, bitmap)
//                                        navController.navigate("edit/${Uri.encode(savedUri.toString())}")
//                                    }
//                                    is CropError -> {
//                                        errorMessage = "Crop failed: ${result.name}"
//                                    }
//                                    CropResult.Cancelled -> {
//                                        navController.popBackStack()
//                                    }
//
//                                    else -> {}
//                                }
//                            }
//                        },
//                        enabled = !isCropping,
//                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
//                    ) {
//                        if (isCropping) CircularProgressIndicator(modifier = Modifier.size(16.dp))
//                        else Icon(Icons.Default.Check, contentDescription = "Confirm")
//                    }
//                }
//
//                // Error Message (if any)
//                errorMessage?.let {
//                    Text(
//                        text = it,
//                        color = MaterialTheme.colorScheme.error,
//                        modifier = Modifier
//                            .padding(horizontal = 16.dp)
//                            .fillMaxWidth()
//                    )
//                }
//            }
//        }
//    }
//}
//
//fun saveBitmapToFile(context: Context, bitmap: Bitmap): Uri? {
//    val file = File(context.cacheDir, "cropped_image_${System.currentTimeMillis()}.jpg")
//    try {
//        FileOutputStream(file).use { out ->
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
//            out.flush()
//        }
//        return Uri.fromFile(file)
//    } catch (e: IOException) {
//        e.printStackTrace()
//        return null
//    }
//}


//testing version
//@Composable
//fun CropScreenNew(
//    navController: NavController,
//    imageUri: Uri
//) {
//    val context = LocalContext.current
//    val cropper = rememberImageCropper()
//    val scope = rememberCoroutineScope()
//    var error by remember { mutableStateOf<CropError?>(null) }
//    var isProcessing by remember { mutableStateOf(false) }
//    var showDialog by remember { mutableStateOf(false) }
//
//    // Start crop session when screen is launched
//    LaunchedEffect(imageUri) {
//        isProcessing = true
//        try {
//            // Initialize crop session
//            cropper.cropState?.done(false) // Reset any existing session
//            val result = cropper.crop(
//                uri = imageUri,
//                context = context,
//                maxResultSize = IntSize(1080, 1080)
//            )
//
//            when (result) {
//                is CropResult.Success -> {
//                    val bitmap = result.bitmap.asAndroidBitmap()
//                    val savedUri = saveBitmapToFile(context, bitmap)
//                    if (savedUri != null) {
//                        navController.navigate("edit/${Uri.encode(savedUri.toString())}") {
//                            popUpTo("crop") { inclusive = true }
//                        }
//                    } else {
//                        error = CropError.SavingError
//                    }
//                }
//                is CropError -> error = result
//                CropResult.Cancelled -> navController.popBackStack()
//            }
//        } catch (e: Exception) {
//            error = CropError.LoadingError
//            Log.e("CropScreenNew", "Crop error", e)
//        } finally {
//            isProcessing = false
//        }
//    }
//
//    // Show UI based on state
//    Box(modifier = Modifier.fillMaxSize()) {
//        // Show cropper dialog when session is active
//        if (cropper.cropState != null) {
//            showDialog = true
//            ImageCropperDialog(
//                state = cropper.cropState!!,
//                style = CropperStyle(
//                    backgroundColor = Color.White,
//                    overlay = Color.Black.copy(alpha = 0.7f),
//                    rectColor = Color.White,
//                    rectStrokeWidth = 3.dp,
//                    touchRad = 30.dp,
//                    autoZoom = true
//                ),
//                cropControls = { state ->
//                    CustomControls(state, onConfirm = { state.done(true) })
//                }
//            )
//        }
//
//        // Show loading indicator during processing
//        if (isProcessing || cropper.loadingStatus != null) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(Color.Black.copy(alpha = 0.7f)),
//                contentAlignment = Alignment.Center
//            ) {
//                CircularProgressIndicator(
//                    modifier = Modifier.size(48.dp),
//                    color = Color.White,
//                    strokeWidth = 4.dp
//                )
//            }
//        }
//
//        // Show error dialog if needed
//        error?.let { cropError ->
//            AlertDialog(
//                onDismissRequest = {
//                    error = null
//                    navController.popBackStack()
//                },
//                title = { Text("Crop Error") },
//                text = { Text("Error occurred: ${cropError.name}") },
//                confirmButton = {
//                    Button(
//                        onClick = {
//                            error = null
//                            navController.popBackStack()
//                        }
//                    ) {
//                        Text("OK")
//                    }
//                }
//            )
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//private fun BoxScope.CustomControls(state: CropState, onConfirm: () -> Unit) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp),
//        horizontalArrangement = Arrangement.SpaceEvenly,
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        // Cancel button
//        IconButton(
//            onClick = { state.done(false) },
//            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.error)
//        ) {
//            Icon(Icons.Default.Close, contentDescription = "Cancel")
//        }
//
//        // Confirm button
//        Button(
//            onClick = onConfirm,
//            modifier = Modifier
//                .fillMaxWidth(0.8f)
//                .height(50.dp),
//            colors = ButtonDefaults.buttonColors(
//                containerColor = MaterialTheme.colorScheme.primary
//            )
//        ) {
//            Text("Confirm", fontWeight = FontWeight.Bold)
//        }
//    }
//}
//
//fun saveBitmapToFile(context: Context, bitmap: Bitmap): Uri? {
//    val file = File(context.cacheDir, "cropped_image_${System.currentTimeMillis()}.jpg")
//    return try {
//        FileOutputStream(file).use { out ->
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
//            out.flush()
//        }
//        FileProvider.getUriForFile(
//            context,
//            "${context.packageName}.provider",
//            file
//        ).also {
//            Log.d("CropScreenNew", "Saved cropped image to URI: $it")
//        }
//    } catch (e: Exception) {
//        Log.e("CropScreenNew", "Failed to save bitmap: ${e.message}", e)
//        null
//    } finally {
//        bitmap.recycle()
//    }
//}




@Composable
fun CropScreenNew(
    navController: NavController,
    imageUri: Uri
) {
    val context = LocalContext.current
    val cropper = rememberImageCropper()
    val scope = rememberCoroutineScope()
    var error by remember { mutableStateOf<CropError?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    // Start crop session when screen is launched
    LaunchedEffect(imageUri) {
        isProcessing = true
        try {
            // Initialize crop session
            cropper.cropState?.done(false) // Reset any existing session
            val result = cropper.crop(
                uri = imageUri,
                context = context,
                maxResultSize = IntSize(1080, 1080)
            )

            when (result) {
                is CropResult.Success -> {
                    val bitmap = result.bitmap.asAndroidBitmap()
                    val savedUri = saveBitmapToFile(context, bitmap)
                    if (savedUri != null) {
                        navController.navigate("edit/${Uri.encode(savedUri.toString())}") {
                            popUpTo("crop") { inclusive = true }
                        }
                    } else {
                        error = CropError.SavingError
                    }
                }
                is CropError -> error = result
                CropResult.Cancelled -> navController.popBackStack()
            }
        } catch (e: Exception) {
            error = CropError.LoadingError
            Log.e("CropScreenNew", "Crop error", e)
        } finally {
            isProcessing = false
        }
    }

    // Full-screen cropper UI with SmartVisionAidTheme
    SmartVisionAidTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            // Show cropper dialog when session is active
            cropper.cropState?.let { state ->
                ImageCropperDialog(state = state)
            }

            // Show loading indicator during processing
            if (isProcessing || cropper.loadingStatus != null) {
                LoadingDialog(status = cropper.loadingStatus ?: CropperLoading.PreparingImage)
            }

            // Show error dialog if needed
            error?.let { cropError ->
                CropErrorDialog(
                    error = cropError,
                    onDismiss = {
                        error = null
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}


fun saveBitmapToFile(context: Context, bitmap: Bitmap): Uri? {
    val file = File(context.cacheDir, "cropped_image_${System.currentTimeMillis()}.jpg")
    return try {
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
        }
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        ).also {
            Log.d("CropScreenNew", "Saved cropped image to URI: $it")
        }
    } catch (e: Exception) {
        Log.e("CropScreenNew", "Failed to save bitmap: ${e.message}", e)
        null
    } finally {
        bitmap.recycle()
    }
}


