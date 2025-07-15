// TranslationScreen.kt
package com.example.smart_vision_aid

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
import androidx.navigation.compose.rememberNavController
import com.example.smart_vision_aid.ui.theme.GreenPrimary
import com.example.smart_vision_aid.ui.theme.TextPrimary
import com.google.mlkit.nl.translate.Translator
import OcrResultItem
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun TranslationScreen(
    navController: NavController,
    textToTranslate: String,
    ocrResults: List<OcrResultItem>,
    sourceLanguage: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var translator by remember { mutableStateOf<Translator?>(null) }

    // Language map for supported languages
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

        Text(
            "Translating from: ${languageMap.entries.find { it.value == sourceLanguage }?.key ?: "English"}",
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
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
                                try {
                                    isLoading = true
                                    errorMessage = null
                                    val targetLang = languageMap[lang] ?: return@launch

                                    // Skip translation if same language
                                    if (sourceLanguage == targetLang) {
                                        navController.popBackStack()
                                        return@launch
                                    }

                                    val options = TranslatorOptions.Builder()
                                        .setSourceLanguage(sourceLanguage)
                                        .setTargetLanguage(targetLang)
                                        .build()
                                    translator = Translation.getClient(options)

                                    translator?.downloadModelIfNeeded()?.await()
                                    val translatedText = translator?.translate(textToTranslate)?.await()
                                        ?: throw Exception("Translation failed")

                                    // Create new OCR results with translated text but same coordinates
                                    val newOcrResults = ocrResults.map {
                                        it.copy(text = "TRANSLATED") // Placeholder
                                    }

                                    navController.navigate(
                                        "processing/${
                                            Uri.encode(translatedText)
                                        }/${
                                            Uri.encode(newOcrResults.toJsonString())
                                        }/${
                                            Uri.encode(targetLang)
                                        }"
                                    ){
                                        // Pop up to the edit screen if it exists in the back stack
                                        popUpTo("processing"){ inclusive=true}
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Translation failed: ${e.message}"
                                    Log.e("TranslationScreen", "Translation error", e)
                                } finally {
                                    isLoading = false
                                    translator?.close()
                                    translator = null
                                }
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

// Extension function for OCR results serialization
