package com.example.smart_vision_aid.helper


//import android.content.Context
//import android.graphics.Bitmap
//import android.util.Log
//import com.googlecode.tesseract.android.TessBaseAPI
//import org.opencv.android.Utils
//import org.opencv.core.Mat
//import java.io.File
//import java.io.FileOutputStream
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import org.opencv.imgproc.Imgproc
//
//object OCRHelper {
//    private const val TAG = "OCRHelper"
//    private const val TESSDATA_PATH = "tessdata"
//    private const val DEFAULT_LANGUAGE = "eng"
//
//    private var tessBaseAPI: TessBaseAPI? = null
//    private var isInitialized = false
//
//    suspend fun performOCR(context: Context, bitmap: Bitmap): String {
//        return withContext(Dispatchers.IO) {
//            try {
//                // Initialize Tesseract if not already done
//                if (!isInitialized) {
//                    initializeTesseract(context)
//                }
//
//                // Convert bitmap to grayscale for better OCR results
//                val mat = Mat()
//                Utils.bitmapToMat(bitmap, mat)
//
//// Create a new Mat for grayscale conversion
//                val grayMat = Mat()
//                Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_BGR2GRAY)
//
//// Convert Mat to Bitmap
//                val grayBitmap = Bitmap.createBitmap(grayMat.cols(), grayMat.rows(), Bitmap.Config.ARGB_8888)
//                Utils.matToBitmap(grayMat, grayBitmap)
//
//// Perform OCR
//                tessBaseAPI?.let { tesseract ->
//                    tesseract.setImage(grayBitmap) // Pass the converted Bitmap
//                    val result = tesseract.utF8Text
//                    Log.d(TAG, "OCR Result: $result")
//                    result
//                } ?: run {
//                    Log.e(TAG, "Tesseract not initialized")
//                    "OCR engine not initialized"
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "OCR failed", e)
//                "OCR failed: ${e.localizedMessage}"
//            }
//        }
//    }
//
//    private fun initializeTesseract(context: Context) {
//        try {
//            // Create tessdata directory if it doesn't exist
//            val tessDataDir = File(context.filesDir, TESSDATA_PATH)
//            if (!tessDataDir.exists()) {
//                tessDataDir.mkdir()
//            }
//
//            // Initialize Tesseract
//            tessBaseAPI = TessBaseAPI().apply {
//                val initSuccess = init(context.filesDir.absolutePath, DEFAULT_LANGUAGE)
//
//                if (!initSuccess) {
//                    Log.e(TAG, "Tesseract initialization failed")
//                    throw RuntimeException("Tesseract init failed")
//                }
//
//                // Set OCR engine mode and page segmentation mode
//                setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO)
//            }
//
//            isInitialized = true
//            Log.d(TAG, "Tesseract initialized successfully")
//        } catch (e: Exception) {
//            Log.e(TAG, "Tesseract initialization error", e)
//            tessBaseAPI = null
//            isInitialized = false
//        }
//    }
//
//    fun release() {
//        tessBaseAPI?.end()
//        tessBaseAPI = null
//        isInitialized = false
//    }
//}
//

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File
import java.io.FileOutputStream

object OcrHelper {
    private var tessBaseAPI: TessBaseAPI? = null
    private const val TESS_DATA_DIR = "tesseract"
    private const val TESS_DATA_SUBDIR = "tessdata"
    private const val LANGUAGE = "eng"

    fun initTesseract(context: Context) {
        if (tessBaseAPI == null) {
            // 1. Create directories
            val tessDir = File(context.filesDir, TESS_DATA_DIR)
            val tessDataDir = File(tessDir, TESS_DATA_SUBDIR)

            if (!tessDataDir.exists()) {
                val created = tessDataDir.mkdirs()
                Log.d("OCR", "Created tessdata dir: $created at ${tessDataDir.absolutePath}")
            }

            // 2. Copy trained data from assets
            copyTessDataFromAssets(context, tessDataDir)

            // 3. Initialize Tesseract with proper path
            tessBaseAPI = TessBaseAPI().apply {
                val initSuccess = init(tessDir.absolutePath, LANGUAGE)

                if (initSuccess) {
                    Log.d("OCR", "Tesseract initialized successfully")
                } else {
                    Log.e("OCR", "Tesseract initialization failed")
                    // Verify files exist
                    val files = tessDataDir.listFiles()
                    if (files.isNullOrEmpty()) {
                        Log.e("OCR", "No files in tessdata directory")
                    } else {
                        Log.d("OCR", "Files in tessdata: ${files.joinToString { it.name }}")
                    }
                    throw RuntimeException("Tesseract init failed. Path: ${tessDir.absolutePath}")
                }
            }
        }
    }

    private fun copyTessDataFromAssets(context: Context, targetDir: File) {
        try {
            // Check if assets/tessdata exists
            val assetFiles = context.assets.list("tessdata")
            if (assetFiles.isNullOrEmpty()) {
                Log.e("OCR", "No files found in assets/tessdata")
                return
            }

            Log.d("OCR", "Found ${assetFiles.size} files in assets/tessdata")

            for (filename in assetFiles) {
                val targetFile = File(targetDir, filename)
                if (!targetFile.exists()) {
                    context.assets.open("tessdata/$filename").use { input ->
                        FileOutputStream(targetFile).use { output ->
                            input.copyTo(output)
                            Log.d("OCR", "Copied $filename to ${targetFile.absolutePath}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("OCR", "Error copying tessdata", e)
        }
    }

    fun performOCR(bitmap: Bitmap): String {
        tessBaseAPI?.setImage(bitmap)
        return tessBaseAPI?.utF8Text ?: "No text found"
    }
}