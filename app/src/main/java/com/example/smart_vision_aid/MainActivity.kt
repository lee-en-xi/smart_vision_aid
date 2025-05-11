package com.example.smart_vision_aid

import UploadScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.smart_vision_aid.ui.theme.SmartVisionAidTheme
import androidx.navigation.navArgument





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

        // Processing Screen
        composable("processing/{text}") { backStackEntry ->
            ProcessingScreen(
                navController = navController,
                extractedText = backStackEntry.arguments?.getString("text") ?: ""
            )
        }

        // Translation Screen
        composable("translation/{text}") { backStackEntry ->
            TranslationScreen(
                navController = navController,
                textToTranslate = backStackEntry.arguments?.getString("text") ?: ""
            )
        }

        // Audio Control Screen
        composable("audio/{text}") { backStackEntry ->
            AudioControlScreen(
                textToSpeak = backStackEntry.arguments?.getString("text") ?: ""
            )
        }
        composable("upload/{uri}", arguments = listOf(navArgument("uri") { defaultValue = "" })) {
            UploadScreen(
                navController = navController,
                uriString = it.arguments?.getString("uri") ?: ""
            )
        }

        composable("edit/{uri}",
            arguments = listOf(navArgument("uri") { defaultValue = "" })) {
            EditScreen(
                navController = navController,
                imageUri = it.arguments?.getString("uri") ?: ""
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
            "rectify/{imagePath}",
            arguments = listOf(navArgument("imagePath") { defaultValue = "" })
        ) { backStackEntry ->
            val imagePath = backStackEntry.arguments?.getString("imagePath") ?: ""
            RectifyScreen(
                navController = navController,
                imagePath = imagePath
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
