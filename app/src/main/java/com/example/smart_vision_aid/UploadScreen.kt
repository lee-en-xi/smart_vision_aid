package com.example.smart_vision_aid

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@Composable
fun UploadScreen(navController: NavController, uriString: String) {
    val context = LocalContext.current
    val uri = remember { Uri.parse(uriString) }

    LaunchedEffect(uri) {
        if (uriString.isNotEmpty()) {
            // Convert Uri to a file path
            val imagePath = uriToFilePath(context, uri)
            if (imagePath != null) {
                Log.d("UploadScreen", "Navigating to perspective with imagePath=$imagePath")
                navController.navigate("perspective/${Uri.encode(imagePath)}"){
                    popUpTo("upload/{uri}") { inclusive = true }
                }
            } else {
                navController.popBackStack()
            }
        } else {
            navController.popBackStack()
        }
    }

    Box(Modifier.fillMaxSize(), Alignment.Center) {
        CircularProgressIndicator()
    }
}

// Convert Uri to a file path by saving to cache
//private fun uriToFilePath(context: Context, uri: Uri): String? {
//    try {
//        // Create a temporary file in cache
//        val file = File(context.cacheDir, "captured.jpg")
//
//        // Copy Uri content to the file
//        context.contentResolver.openInputStream(uri)?.use { input ->
//            FileOutputStream(file).use { output ->
//                input.copyTo(output)
//            }
//        }
//
//        return file.absolutePath
//    } catch (e: IOException) {
//        e.printStackTrace()
//        return null
//    }
//}

private fun uriToFilePath(context: Context, uri: Uri): String? {
    return try {
        val file = File(context.cacheDir, "uploaded_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        Log.d("UploadScreen", "File saved at: ${file.absolutePath}, exists: ${file.exists()}")
        file.absolutePath
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}
