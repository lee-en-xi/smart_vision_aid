// RotateScreen.kt
package com.example.smart_vision_aid

import android.graphics.Bitmap
import android.graphics.Matrix
import com.example.smart_vision_aid.saveBitmapToCache
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavController
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.FlipCameraIos
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


@Composable
fun RotateScreen(navController: NavController, imageUri: String) {
    val context = LocalContext.current
    val uri = remember { Uri.parse(imageUri) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(uri) {
        bitmap = loadBitmapFromUri(context, uri)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        bitmap?.let { bmp ->
            var rotatedBitmap by remember { mutableStateOf(bmp) }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = rotatedBitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    rotatedBitmap = rotateBitmap(rotatedBitmap, -90f)
                },modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3), // Blue for editing actions
                        contentColor = Color.White
                    ),
                ) {
                    Icon(Icons.Default.RotateLeft, contentDescription = "Rotate Left")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Left 90°",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(onClick = {
                    rotatedBitmap = rotateBitmap(rotatedBitmap, 90f)
                }, modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .padding(start = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3), // Blue for editing actions
                        contentColor = Color.White
                    ),
                ) {
                    Icon(Icons.Default.RotateRight, contentDescription = "Rotate Right")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Right 90°",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    rotatedBitmap = flipBitmapHorizontal(rotatedBitmap)
                },modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3), // Blue for editing actions
                        contentColor = Color.White
                    ),
                    ) {
                    Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Flip Horizontal")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Horizontal",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(onClick = {
                    rotatedBitmap = flipBitmapVertical(rotatedBitmap)
                },
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .padding(start = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3), // Blue for editing actions
                        contentColor = Color.White
                    ),
                    ) {
                    Icon(Icons.Default.FlipCameraIos, contentDescription = "Flip Vertical")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Vertical",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }


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
                        containerColor = Color(0xFFF44336), // Red for Cancel
                        contentColor = Color.White
                    ),
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(onClick = {
                    val tempFile = saveBitmapToCache(context, rotatedBitmap)
                    navController.popBackStack() // remove RotateScreen
                    navController.popBackStack() // remove previous EditScreen
                    navController.navigate("edit/${Uri.encode(tempFile.toURI().toString())}")
                }, modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .padding(start = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50), // Green for Confirm
                        contentColor = Color.White
                    ),
                ) {
                    Text(
                        text = "Confirm",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}


fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(angle)
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
}

fun flipBitmapHorizontal(source: Bitmap): Bitmap {
    val matrix = Matrix()
    matrix.preScale(-1f, 1f)
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
}

fun flipBitmapVertical(source: Bitmap): Bitmap {
    val matrix = Matrix()
    matrix.preScale(1f, -1f)
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
}


