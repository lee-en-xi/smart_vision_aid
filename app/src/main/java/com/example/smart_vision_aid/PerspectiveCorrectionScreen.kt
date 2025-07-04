package com.example.smart_vision_aid

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc
import java.io.File
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerspectiveScreen(
    navController: NavController,
    imagePath: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val imageBitmap = remember { mutableStateOf<Bitmap?>(null) }
    val detectedCorners = remember { mutableStateListOf<Offset>() }
    val adjustedCorners = remember { mutableStateListOf<Offset>() }
    var imageSize by remember { mutableStateOf(Size(1f, 1f)) }
    var processingState by remember { mutableStateOf("Processing...") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // In the first LaunchedEffect block
    LaunchedEffect(imagePath) {
        processingState = "Processing..."
        try {
            Log.d("PerspectiveDebug", "Loading image from: $imagePath")
            val file = File(imagePath)
            if (!file.exists()) {
                Log.e("PerspectiveError", "File not found: ${file.absolutePath}")
                errorMessage = "File not found"
                showError = true
                processingState = "Processing Failed"
                return@LaunchedEffect
            }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            Log.d("PerspectiveDebug", "Parsed URI: $uri")
            context.contentResolver.openInputStream(uri)?.use { stream ->
                Log.d("PerspectiveDebug", "Stream opened successfully")
                val options = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
                imageBitmap.value = BitmapFactory.decodeStream(stream, null, options)
                if (imageBitmap.value == null) {
                    Log.e("PerspectiveError", "Failed to decode bitmap from stream")
                    errorMessage = "Invalid image format"
                    showError = true
                    processingState = "Processing Failed"
                } else {
                    Log.d("PerspectiveDebug", "Bitmap dimensions: ${imageBitmap.value!!.width}x${imageBitmap.value!!.height}")
                }
            } ?: run {
                Log.e("PerspectiveError", "Null input stream")
                errorMessage = "File not found"
                showError = true
                processingState = "Processing Failed"
            }
        } catch (e: Exception) {
            Log.e("PerspectiveError", "Image loading failed", e)
            errorMessage = "Error: ${e.localizedMessage}"
            showError = true
            processingState = "Processing Failed"
        }
    }

    LaunchedEffect(imageBitmap.value, imageSize) {
        if (imageBitmap.value != null && imageSize.width > 1 && imageSize.height > 1) {
            Log.d("PerspectiveDebug", "Starting text region detection")
            Log.d("PerspectiveDebug", "Image size: $imageSize")
            scope.launch(Dispatchers.Default) {
                try {
                    val bitmap = imageBitmap.value!!
                    val mat = Mat()
                    Utils.bitmapToMat(bitmap, mat)
                    Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2BGR)
                    val mergedBoxes = detectAndMergeTextBoxes(mat)

                    // CALCULATE BUTTON AREA HEIGHT (approx 15% of screen height)
                    val buttonAreaHeight = imageSize.height * 0.15f

                    if (mergedBoxes.isEmpty()) {
                        Log.w("PerspectiveDebug", "No text regions detected, using fallback box")

                        // Use proportional insets based on image size
                        val horizontalInset = imageSize.width * 0.1f
                        val verticalInset = imageSize.height * 0.1f

                        // Ensure bottom inset accounts for button area
                        val bottomInset = max(verticalInset, buttonAreaHeight + 20f)

                        val fallback = listOf(
                            Offset(horizontalInset, verticalInset),
                            Offset(imageSize.width - horizontalInset, verticalInset),
                            Offset(imageSize.width - horizontalInset, imageSize.height - bottomInset),
                            Offset(horizontalInset, imageSize.height - bottomInset)
                        )

                        withContext(Dispatchers.Main) {
                            detectedCorners.clear()
                            adjustedCorners.clear()
                            detectedCorners.addAll(fallback)
                            adjustedCorners.addAll(fallback)
                            processingState = "Confirm"
                        }
                    }  else {
                        val unionRect = mergedBoxes.reduce { acc, rect -> unionRect(acc, rect) }
                        val scaleX = imageSize.width / mat.width().toFloat()
                        val scaleY = imageSize.height / mat.height().toFloat()
                        val corners = listOf(
                            Offset(unionRect.x * scaleX, unionRect.y * scaleY),
                            Offset((unionRect.x + unionRect.width) * scaleX, unionRect.y * scaleY),
                            Offset((unionRect.x + unionRect.width) * scaleX, (unionRect.y + unionRect.height) * scaleY),
                            Offset(unionRect.x * scaleX, (unionRect.y + unionRect.height) * scaleY)
                        )
                        withContext(Dispatchers.Main) {
                            detectedCorners.clear()
                            adjustedCorners.clear()
                            detectedCorners.addAll(corners)
                            adjustedCorners.addAll(corners)
                            processingState = "Confirm" // Enable button when boxes are displayed
                        }
                    }
                } catch (e: Exception) {
                    errorMessage = "Error detecting text regions: ${e.localizedMessage}"
                    showError = true
                    processingState = "Processing Failed"
                    Log.e("PerspectiveError", "Text detection error", e)
                }
            }
        }
    }


    Box(modifier = Modifier.fillMaxSize().padding(bottom = 10.dp)) {
        imageBitmap.value?.let { bmp ->
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "Document",
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned {
                        imageSize = Size(it.size.width.toFloat(), it.size.height.toFloat())
                    }
            )
        }

        // Corner editing overlay
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val touch = change.position
                        adjustedCorners.indexOfClosestTo(touch)?.let { index ->
                            adjustedCorners[index] = touch
                        }
                    }
                }
        ) {
            if (adjustedCorners.size == 4) {
                drawPath(
                    path = Path().apply {
                        moveTo(adjustedCorners[0].x, adjustedCorners[0].y)
                        adjustedCorners.forEach { lineTo(it.x, it.y) }
                        close()
                    },
                    color = Color.Blue.copy(alpha = 0.8f),
                    style = Stroke(width = 4f)
                )

                adjustedCorners.forEach { point ->
                    drawCircle(Color.Red, radius = 20f, center = point)
                    drawCircle(
                        Color.White.copy(alpha = 0.5f),
                        radius = 20f,
                        center = point,
                        style = Stroke(width = 3f)
                    )
                }
            }
        }

        // Bottom buttons
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 8.dp) // Adjusted padding for better spacing
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .weight(1f) // Equal width for both buttons
                        .height(64.dp) // Slightly smaller than 70.dp as requested
                        .padding(end = 8.dp), // Space between buttons
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336), // Red for Cancel for clear distinction
                        contentColor = Color.White
                    ), // Rounded corners for modern look
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 18.sp, // Larger font for readability
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = {
                        try {
                            imageBitmap.value?.let { bmp ->
                                scope.launch(Dispatchers.Default) {
                                    // Get bitmap and layout sizes
                                    val bmpWidth = bmp.width.toFloat()
                                    val bmpHeight = bmp.height.toFloat()
                                    val containerWidth = imageSize.width
                                    val containerHeight = imageSize.height

                                    // Calculate aspect ratios
                                    val imageAspectRatio = bmpWidth / bmpHeight
                                    val containerAspectRatio = containerWidth / containerHeight

                                    // Calculate displayed image size and offset inside the container
                                    val displayedWidth: Float
                                    val displayedHeight: Float
                                    val offsetX: Float
                                    val offsetY: Float

                                    if (containerAspectRatio > imageAspectRatio) {
                                        // Container wider than image, scale to height
                                        displayedHeight = containerHeight
                                        displayedWidth = displayedHeight * imageAspectRatio
                                        offsetX = (containerWidth - displayedWidth) / 2f
                                        offsetY = 0f
                                    } else {
                                        // Container taller than image, scale to width
                                        displayedWidth = containerWidth
                                        displayedHeight = displayedWidth / imageAspectRatio
                                        offsetX = 0f
                                        offsetY = (containerHeight - displayedHeight) / 2f
                                    }

                                    // Map adjustedCorners from screen coords to bitmap coords
                                    val points = adjustedCorners.map {
                                        val xInImage = ((it.x - offsetX) / displayedWidth) * bmpWidth
                                        val yInImage = ((it.y - offsetY) / displayedHeight) * bmpHeight
                                        Point(
                                            xInImage.coerceIn(0.0f, bmpWidth).toDouble(),
                                            yInImage.coerceIn(0.0f, bmpHeight).toDouble()
                                        )
                                    }

                                    // Convert bitmap to OpenCV Mat
                                    val mat = Mat()
                                    Utils.bitmapToMat(bmp, mat)

                                    // Perform perspective crop using corrected points
                                    val corrected = DocumentScanHelper.cropDocument(mat, points)

                                    // Convert back to Bitmap
                                    val resultBmp = Bitmap.createBitmap(
                                        corrected.cols(), corrected.rows(),
                                        Bitmap.Config.ARGB_8888
                                    )
                                    Utils.matToBitmap(corrected, resultBmp)

                                    // Save and navigate on main thread
                                    withContext(Dispatchers.Main) {
                                        val file = saveBitmapToCache(context, resultBmp)
                                        navController.navigate("edit/${Uri.encode(file.absolutePath)}")
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            errorMessage = "Perspective correction failed: ${e.localizedMessage}"
                            showError = true
                            Log.e("PerspectiveError", "Correction failed", e)
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
                    enabled = processingState == "Confirm" // Disable during processing or error
                ) {
                    Text(
                        text = processingState, // Dynamic text based on processing state
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }


            }
        }
    }

    if (showError) {
        AlertDialog(
            onDismissRequest = { showError = false },
            title = { Text("Processing Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showError = false }) {
                    Text("OK")
                }
            }
        )
    }
}

// Helper extension
private fun List<Offset>.indexOfClosestTo(point: Offset): Int? {
    return withIndex().minByOrNull { (_, corner) ->
        sqrt((corner.x - point.x).pow(2) + (corner.y - point.y).pow(2))
    }?.index
}
