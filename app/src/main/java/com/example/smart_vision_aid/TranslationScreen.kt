// TranslationScreen.kt
package com.example.smart_vision_aid

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.net.URLEncoder
import java.util.*
import kotlin.coroutines.CoroutineContext
import androidx.compose.ui.unit.sp
import com.example.smart_vision_aid.ui.theme.GreenPrimary
import com.example.smart_vision_aid.ui.theme.TextPrimary

@Composable
fun TranslationScreen(navController: NavController, textToTranslate: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val languageMap = mapOf(
        "English" to TranslateLanguage.ENGLISH,
        "Malay" to TranslateLanguage.MALAY,
        "Chinese" to TranslateLanguage.CHINESE,
        "Tamil" to TranslateLanguage.TAMIL,
        "Arabic" to TranslateLanguage.ARABIC
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            "Select Language:",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
        )

        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f)
            ) {
                items(languageMap.keys.toList()) { lang ->
                    TranslationButton(
                        language = lang,
                        onClick = {
                            scope.launch {
                                handleTranslation(
                                    lang = lang,
                                    textToTranslate = textToTranslate,
                                    languageMap = languageMap,
                                    onLoading = { isLoading = it },
                                    onError = { errorMessage = it },
                                    onSuccess = { translatedText ->
                                        val encoded = URLEncoder.encode(translatedText, "UTF-8")
                                        navController.navigate("audio/$encoded")
                                    }
                                )
                            }
                        }
                    )
                }
            }
        }

        errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun TranslationButton(
    language: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .padding(8.dp)
            .height(70.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = GreenPrimary,
            contentColor = Color.White
        )
    ) {
        Text(language, fontSize = 18.sp)
    }
}

private suspend fun handleTranslation(
    lang: String,
    textToTranslate: String,
    languageMap: Map<String, String>,
    onLoading: (Boolean) -> Unit,
    onError: (String) -> Unit,
    onSuccess: (String) -> Unit
) {
    try {
        onLoading(true)

        // Detect source language
        val sourceLang = LanguageIdentification.getClient()
            .identifyLanguage(textToTranslate)
            .await()

        val targetLang = languageMap[lang] ?: throw Exception("Invalid language selection")

        // Create translator
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLang)
            .setTargetLanguage(targetLang)
            .build()

        val translator = Translation.getClient(options)

        // Download model if needed
        translator.downloadModelIfNeeded().await()

        // Perform translation
        val translatedText = translator.translate(textToTranslate).await()

        // Return success
        onSuccess(translatedText)
    } catch (e: Exception) {
        Log.e("Translation", "Error: ${e.message}")
        onError("Translation failed: ${e.localizedMessage ?: "Unknown error"}")
    } finally {
        onLoading(false)
    }
}