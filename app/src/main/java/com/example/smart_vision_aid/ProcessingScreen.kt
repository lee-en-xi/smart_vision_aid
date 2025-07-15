package com.example.smart_vision_aid

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.smart_vision_aid.ui.theme.TextPrimary
import OcrResultItem
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.Settings.Global.putFloat
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.times
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.times
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import org.json.JSONArray
import org.json.JSONObject
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.net.URLDecoder
import java.net.URLEncoder
import com.example.smart_vision_aid.ui.theme.GreenPrimary
import com.example.smart_vision_aid.ui.theme.GreenSecondary



//@Composable
//fun ProcessingScreen(
//    navController: NavController,
//    combinedText: String,
//    ocrResults: List<OcrResultItem>
//) {
//    val context = LocalContext.current
//    val scaleState = rememberZoomState()
//    var selectedWord by remember { mutableStateOf<OcrResultItem?>(null) }
//    var isPlayingAll by remember { mutableStateOf(false) }
//    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
//
//    // Clean up media player
//    DisposableEffect(Unit) {
//        onDispose {
//            mediaPlayer?.release()
//        }
//    }
//
//    // Play audio for a specific word
//    fun playWord(word: String) {
//        mediaPlayer?.release()
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val audioBytes = CambAiService.textToSpeech(word, "en") // Default to English
//                val tempFile = File.createTempFile("tts_word", ".mp3", context.cacheDir)
//                tempFile.writeBytes(audioBytes)
//
//                withContext(Dispatchers.Main) {
//                    mediaPlayer = MediaPlayer().apply {
//                        setDataSource(tempFile.path)
//                        setVolume(1f, 1f)
//                        setOnCompletionListener { release() }
//                        prepare()
//                        start()
//                    }
//                }
//            } catch (e: Exception) {
//                Log.e("ProcessingScreen", "Word audio failed", e)
//            }
//        }
//    }
//
//    // Play entire text
//    fun playAllText() {
//        isPlayingAll = true
//        mediaPlayer?.release()
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val audioBytes = CambAiService.textToSpeech(combinedText, "en")
//                val tempFile = File.createTempFile("tts_all", ".mp3", context.cacheDir)
//                tempFile.writeBytes(audioBytes)
//
//                withContext(Dispatchers.Main) {
//                    mediaPlayer = MediaPlayer().apply {
//                        setDataSource(tempFile.path)
//                        setVolume(1f, 1f)
//                        setOnCompletionListener {
//                            isPlayingAll = false
//                            release()
//                        }
//                        prepare()
//                        start()
//                    }
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    isPlayingAll = false
//                }
//                Log.e("ProcessingScreen", "Full text audio failed", e)
//            }
//        }
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        // Zoomable text display area
//        Box(
//            modifier = Modifier
//                .weight(1f)
//                .border(1.dp, Color.Gray)
//                .pointerInput(Unit) {
//                    detectTransformGestures { _, pan, zoom, _ ->
//                        scaleState.applyScaleAndPan(zoom, pan)
//                    }
//                }
//        ) {
//            // Calculate max coordinates for scaling
//            val maxX = ocrResults.maxOfOrNull { it.coordinates.getOrNull(2) ?: 0f } ?: 0f
//            val maxY = ocrResults.maxOfOrNull { it.coordinates.getOrNull(3) ?: 0f } ?: 0f
//
//            // Apply zoom/pan transformations
//            Box(
//                modifier = Modifier
//                    .graphicsLayer {
//                        scaleX = scaleState.scale
//                        scaleY = scaleState.scale
//                        translationX = scaleState.offset.x
//                        translationY = scaleState.offset.y
//                    }
//                    .size(maxX.dp, maxY.dp)
//            ) {
//                ocrResults.forEach { result ->
//                    if (result.coordinates.size >= 4) {
//                        val left = result.coordinates[0]
//                        val top = result.coordinates[1]
//                        val right = result.coordinates[2]
//                        val bottom = result.coordinates[3]
//
//                        Box(
//                            modifier = Modifier
//                                .offset(left.dp, top.dp)
//                                .size((right - left).dp, (bottom - top).dp)
//                                .border(1.dp, if (selectedWord == result) Color.Red else Color.Blue)
//                                .background(
//                                    if (selectedWord == result)
//                                        Color.Yellow.copy(alpha = 0.5f)
//                                    else
//                                        Color.LightGray.copy(alpha = 0.3f)
//                                )
//                                .clickable {
//                                    selectedWord = result
//                                    playWord(result.text)
//                                }
//                        ) {
//                            Text(
//                                text = result.text,
//                                modifier = Modifier
//                                    .padding(4.dp)
//                                    .align(Alignment.Center),
//                                fontSize = 12.sp,
//                                color = Color.Black
//                            )
//                        }
//                    }
//                }
//            }
//        }
//
//        Spacer(Modifier.height(16.dp))
//
//        // Action buttons
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceEvenly
//        ) {
//            Button(
//                onClick = { playAllText() },
//                modifier = Modifier
//                    .weight(1f)
//                    .height(50.dp)
//                    .padding(end = 8.dp),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = if (isPlayingAll) Color.Red else MaterialTheme.colorScheme.primary,
//                    contentColor = MaterialTheme.colorScheme.onPrimary
//                ),
//            ) {
//                Icon(
//                    if (isPlayingAll) Icons.Default.Pause else Icons.Default.VolumeUp,
//                    contentDescription = "Speak"
//                )
//                Spacer(Modifier.width(8.dp))
//                Text(if (isPlayingAll) "Stop" else "Read All", fontSize = 17.sp)
//            }
//
//            Button(
//                onClick = {
//                    mediaPlayer?.release()
//                    navController.navigate("translation/${Uri.encode(combinedText)}")
//                },
//                modifier = Modifier
//                    .weight(1f)
//                    .height(50.dp)
//                    .padding(start = 8.dp),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = MaterialTheme.colorScheme.secondary,
//                    contentColor = MaterialTheme.colorScheme.onSecondary
//                ),
//            ) {
//                Icon(Icons.Default.Translate, contentDescription = "Translate")
//                Spacer(Modifier.width(8.dp))
//                Text("Translate", fontSize = 17.sp)
//            }
//        }
//    }
//}
//
// Helper class for zoom state

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessingScreen(
    navController: NavController,
    combinedText: String,
    ocrResults: List<OcrResultItem>,
    sourceLanguage: String = TranslateLanguage.ENGLISH
) {
    val context = LocalContext.current
    val scaleState = rememberZoomState()
    var textToSpeech by remember { mutableStateOf<TextToSpeech?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var speechSpeed by remember { mutableStateOf(1f) }
    var volumeLevel by remember { mutableStateOf(1f) }
    var isPlaying by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState()


    // Initialize TextToSpeech
    DisposableEffect(Unit) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.forLanguageTag(
                    when (sourceLanguage) {
                        TranslateLanguage.MALAY -> "ms"
                        TranslateLanguage.CHINESE -> "zh"
                        TranslateLanguage.TAMIL -> "ta"
                        TranslateLanguage.ARABIC -> "ar"
                        else -> "en"
                    }
                )
                textToSpeech?.setSpeechRate(speechSpeed)
            }
        }
        onDispose {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        }
    }

    // Function to play or pause TTS
    fun speakOrPause() {
        if (isPlaying) {
            textToSpeech?.stop()
            isPlaying = false
        } else {
            val params = Bundle().apply {
                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volumeLevel)
            }
            textToSpeech?.setSpeechRate(speechSpeed)
            textToSpeech?.speak(combinedText, TextToSpeech.QUEUE_FLUSH, params, "tts1")
            isPlaying = true
        }
    }

    // Set up utterance listener to detect when speech finishes
    DisposableEffect(textToSpeech) {
        val listener = object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                isPlaying = false
            }
            override fun onError(utteranceId: String?) {
                isPlaying = false
            }
        }

        textToSpeech?.setOnUtteranceProgressListener(listener)

        onDispose {
            textToSpeech?.setOnUtteranceProgressListener(null)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Zoomable text display area
        Box(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, Color.Gray)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scaleState.applyScaleAndPan(zoom, pan)
                    }
                }
        ) {
            // Apply zoom/pan transformations
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scaleState.scale
                        scaleY = scaleState.scale
                        translationX = scaleState.offset.x
                        translationY = scaleState.offset.y
                    }
                    .fillMaxSize()
            ) {
                Text(
                    text = combinedText,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { showBottomSheet = true },
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .padding(end = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
            ) {
                Icon(Icons.Default.VolumeUp, contentDescription = "Audio Control")
                Spacer(Modifier.width(8.dp))
                Text("Audio", fontSize = 18.sp)
            }

            Button(
                onClick = {

                    navController.navigate(
                        "translation/${
                            Uri.encode(combinedText)
                        }/${
                            Uri.encode(ocrResults.toJsonString())
                        }/${
                            Uri.encode(sourceLanguage)
                        }"
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .padding(start = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
            ) {
                Icon(Icons.Default.Translate, contentDescription = "Select Language")
                Spacer(Modifier.width(8.dp))
                Text("Translate", fontSize = 18.sp)
            }
        }
    }

    // Audio Control Bottom Sheet
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = bottomSheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Audio Controls",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Play/Pause Button
                Button(
                    onClick = { speakOrPause() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlaying) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (isPlaying) "Pause" else "Play", fontSize = 20.sp)
                }

                Spacer(Modifier.height(24.dp))

                // Speed Control
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Speed,
                        contentDescription = "Speed",
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Slider(
                        value = speechSpeed,
                        onValueChange = {
                            speechSpeed = it
                            textToSpeech?.setSpeechRate(it)
                        },
                        valueRange = 0.5f..2f,
                        steps = 2,
                        modifier = Modifier.weight(1f)
                    )
                    Text("${"%.1f".format(speechSpeed)}x", fontSize = 18.sp, modifier = Modifier.width(40.dp))
                }

                Spacer(Modifier.height(16.dp))

                // Volume Control
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.VolumeUp,
                        contentDescription = "Volume",
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Slider(
                        value = volumeLevel,
                        onValueChange = {
                            volumeLevel = it
                            // Volume change will take effect on next playback
                        },
                        modifier = Modifier.weight(1f)
                    )
                    Text("${(volumeLevel * 100).toInt()}%", fontSize = 18.sp, modifier = Modifier.width(40.dp))
                }
            }
        }
    }
}

// Extension function for OCR results serialization
fun List<OcrResultItem>.toJsonString(): String {
    val jsonArray = JSONArray()
    forEach { item ->
        val jsonObject = JSONObject().apply {
            put("text", item.text)
            put("coordinates", JSONArray(item.coordinates))
        }
        jsonArray.put(jsonObject)
    }
    return jsonArray.toString()
}

class ZoomState(
    initialScale: Float = 1f,
    initialOffset: Offset = Offset.Zero
) {
    var scale by mutableStateOf(initialScale)
    var offset by mutableStateOf(initialOffset)

    fun applyScaleAndPan(zoomChange: Float, panChange: Offset) {
        scale = max(0.5f, min(5f, scale * zoomChange))
        offset += panChange
    }
}

@Composable
fun rememberZoomState(
    initialScale: Float = 1f,
    initialOffset: Offset = Offset.Zero
): ZoomState {
    return remember { ZoomState(initialScale, initialOffset) }
}

