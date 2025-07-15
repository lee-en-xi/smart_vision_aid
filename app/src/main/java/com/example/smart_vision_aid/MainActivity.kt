package com.example.smart_vision_aid

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.smart_vision_aid.ui.theme.SmartVisionAidTheme
import androidx.navigation.navArgument
import org.json.JSONArray
import java.net.URLDecoder
import OcrResultItem
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.nl.translate.TranslateLanguage


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartVisionAidTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        // Define your composable destinations here

        // Home Screen
        composable("home") {
            HomeScreen(navController = navController)
        }

        // Camera Screen
        composable("camera") {
            CameraScreen(navController = navController)
        }


        // Translation Screen
        composable(
            "translation/{textToTranslate}/{ocrData}/{sourceLanguage}",
            arguments = listOf(
                navArgument("textToTranslate") { type = NavType.StringType },
                navArgument("ocrData") { type = NavType.StringType },
                navArgument("sourceLanguage") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val textToTranslate = URLDecoder.decode(
                backStackEntry.arguments?.getString("textToTranslate") ?: "",
                "UTF-8"
            )
            val ocrData = URLDecoder.decode(
                backStackEntry.arguments?.getString("ocrData") ?: "",
                "UTF-8"
            )
            val sourceLanguage = URLDecoder.decode(
                backStackEntry.arguments?.getString("sourceLanguage") ?: TranslateLanguage.ENGLISH,
                "UTF-8"
            )

            // Parse ocrData to List<OcrResultItem>
            val ocrResults = try {
                val jsonArray = JSONArray(ocrData)
                (0 until jsonArray.length()).map { i ->
                    val item = jsonArray.getJSONObject(i)
                    OcrResultItem(
                        text = item.getString("text"),
                        coordinates = (0 until item.getJSONArray("coordinates").length())
                            .map { j -> item.getJSONArray("coordinates").getDouble(j).toFloat() }
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }

            TranslationScreen(
                navController = navController,
                textToTranslate = textToTranslate,
                ocrResults = ocrResults,
                sourceLanguage = sourceLanguage
            )
        }

        // Processing Screen
        composable(
            "processing/{combinedText}/{ocrData}/{targetLanguage}",
            arguments = listOf(
                navArgument("combinedText") { type = NavType.StringType },
                navArgument("ocrData") { type = NavType.StringType },
                navArgument("targetLanguage") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val combinedText = URLDecoder.decode(
                backStackEntry.arguments?.getString("combinedText") ?: "",
                "UTF-8"
            )
            val ocrDataJson = URLDecoder.decode(
                backStackEntry.arguments?.getString("ocrData") ?: "",
                "UTF-8"
            )
            val targetLanguage = URLDecoder.decode(
                backStackEntry.arguments?.getString("targetLanguage") ?: TranslateLanguage.ENGLISH,
                "UTF-8"
            )

            // Parse OCR data back to objects
            val ocrResults = try {
                val jsonArray = JSONArray(ocrDataJson)
                (0 until jsonArray.length()).map { i ->
                    val item = jsonArray.getJSONObject(i)
                    OcrResultItem(
                        text = item.getString("text"),
                        coordinates = (0 until item.getJSONArray("coordinates").length())
                            .map { j -> item.getJSONArray("coordinates").getDouble(j).toFloat() }
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }

            ProcessingScreen(
                navController = navController,
                combinedText = combinedText,
                ocrResults = ocrResults,
                sourceLanguage = targetLanguage // Pass as sourceLanguage to ProcessingScreen
            )
        }

        composable("upload/{uri}", arguments = listOf(navArgument("uri") { defaultValue = "" })) {
            UploadScreen(
                navController = navController,
                uriString = it.arguments?.getString("uri") ?: ""
            )
        }

        composable("edit/{uri}") { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("uri") ?: ""
            EditScreen(
                navController = navController,
                imageUri = imageUri
            )
        }

        composable("rotate/{uri}",
            arguments = listOf(navArgument("uri") { defaultValue = ""  })) {
            RotateScreen(
                navController = navController,
                imageUri = it.arguments?.getString("uri") ?: ""
            )
        }

        composable(
            "crop/{uri}",
            arguments = listOf(navArgument("uri") { defaultValue = "" })
        ) { backStackEntry ->
            val uriString = backStackEntry.arguments?.getString("uri") ?: ""
            CropScreenNew(
                navController = navController,
                imageUri = Uri.parse(uriString) // Parse the URI string into a Uri object
            )
        }


        // Perspective Screen
        composable("perspective/{uri}",
            arguments = listOf(navArgument("uri") { defaultValue = "" })
        ) {
            PerspectiveScreen(
                navController = navController,
                imagePath = it.arguments?.getString("uri") ?: ""
            )
        }


    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainActivity() {
    SmartVisionAidTheme {
        AppNavigation()
    }
}
