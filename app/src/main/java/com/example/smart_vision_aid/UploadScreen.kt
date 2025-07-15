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

private fun uriToFilePath(context: Context, uri: Uri): String? {
    return try {
        // Clean up old files in cache dir (optional but recommended)
        cleanUpCacheDir(context, "uploaded_")

        // Create a uniquely named file
        val file = File.createTempFile(
            "uploaded_${System.currentTimeMillis()}_",
            ".jpg",
            context.cacheDir
        ).apply { deleteOnExit() }

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
                output.flush() // Ensure all bytes are written
            }
        }

        Log.d("UploadScreen", "File saved at: ${file.absolutePath}")
        file.absolutePath
    } catch (e: Exception) {
        Log.e("UploadScreen", "Failed to save file", e)
        null
    }
}

/**
 * Deletes all files in cache dir with the given prefix.
 */
private fun cleanUpCacheDir(context: Context, prefix: String) {
    try {
        context.cacheDir.listFiles()?.forEach { file ->
            if (file.name.startsWith(prefix)) {
                file.delete()
                Log.d("UploadScreen", "Deleted old file: ${file.name}")
            }
        }
    } catch (e: Exception) {
        Log.e("UploadScreen", "Failed to clean cache", e)
    }
}