// ImageUtils.kt
package com.example.smart_vision_aid

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.util.UUID

fun saveBitmapToCache(context: Context, bitmap: Bitmap): File {
    val file = File(context.cacheDir, "processed_${UUID.randomUUID()}.jpg")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
    }
    return file
}

// Utility function to load a Bitmap from a URI using Coil
suspend fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return withContext(Dispatchers.IO) {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(uri)
            .allowHardware(false) // Prevents hardware bitmaps to avoid crashes
            .build()

        val result = loader.execute(request)
        if (result is SuccessResult) {
            result.drawable.toBitmap()  // Convert drawable to bitmap
        } else {
            null
        }
    }
}


fun Bitmap.rotateBitmapIfRequired(filePath: String): Bitmap {
    val ei = try {
        ExifInterface(filePath)
    } catch (e: IOException) {
        e.printStackTrace()
        return this
    }

    val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        else -> return this
    }

    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}



