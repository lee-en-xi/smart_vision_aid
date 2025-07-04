package com.example.smart_vision_aid

import androidx.compose.foundation.layout.*
import android.util.Log
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.painterResource
import com.example.smart_vision_aid.ui.theme.GreenPrimary
import com.example.smart_vision_aid.ui.theme.GreenSecondary
import com.example.smart_vision_aid.ui.theme.TextPrimary
import com.example.smart_vision_aid.R
import androidx.compose.foundation.Image
import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.ui.platform.LocalContext
import java.util.*
import android.os.Bundle
import androidx.compose.runtime.DisposableEffect


@Composable
fun AudioControlScreen(textToSpeak: String) {
    val context = LocalContext.current
    var isPlaying by rememberSaveable { mutableStateOf(false) }
    var speechSpeed by rememberSaveable { mutableStateOf(1f) }
    var volumeLevel by rememberSaveable { mutableStateOf(0.8f) }
    val tts = remember(context) {
        TextToSpeech(context, null)
    }

    // TextToSpeech setup
    LaunchedEffect(tts) {
        val result = tts.setLanguage(Locale.US)

        val voices = tts.voices
        if (voices != null) {
            val femaleVoice = voices.find {
                it.locale.language == "en"
            }
            if (femaleVoice != null) {
                tts.voice = femaleVoice
            }
        } else {
            Log.w("TTS", "tts.voices is null")
        }
    }


    // Stop speaking when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    // Function to play or pause TTS
    fun speakOrPause() {
        if (isPlaying) {
            tts.stop()
        } else {
            tts.setSpeechRate(speechSpeed)
            val params = Bundle().apply {
                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volumeLevel)
            }
            tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, params, "tts1")
        }
        isPlaying = !isPlaying
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Display text
        Text(
            textToSpeak,
            fontSize = 24.sp,
            color = TextPrimary,
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        )

        Spacer(Modifier.height(24.dp))

        // Play/Pause Button
        Button(
            onClick = { speakOrPause() },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPlaying) GreenSecondary else GreenPrimary
            )
        ) {
            Image(
                painter = painterResource(id = if (isPlaying) R.drawable.pause else R.drawable.play_arrow),
                contentDescription = if (isPlaying) "Pause" else "Play",
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(if (isPlaying) "Pause" else "Play", fontSize = 20.sp)
        }

        Spacer(Modifier.height(24.dp))

        // Speed Control
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painter = painterResource(id = R.drawable.speed), contentDescription = "Speed", modifier = Modifier.size(32.dp))
            Slider(
                value = speechSpeed,
                onValueChange = {
                    speechSpeed = it
                    if (isPlaying) {
                        tts.setSpeechRate(it)
                    }
                },
                valueRange = 0.5f..2f,
                steps = 2,
                modifier = Modifier.weight(1f)
            )
            Text("${"%.1f".format(speechSpeed)}x", fontSize = 18.sp)
        }

        Spacer(Modifier.height(16.dp))

        // Volume Control
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painter = painterResource(id = R.drawable.volume), contentDescription = "Volume", modifier = Modifier.size(32.dp))
            Slider(
                value = volumeLevel,
                onValueChange = {
                    volumeLevel = it
                    // No immediate change needed — will apply on next speak
                },
                modifier = Modifier.weight(1f)
            )
            Text("${(volumeLevel * 100).toInt()}%", fontSize = 18.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AudioControlScreenPreview() {
    AudioControlScreen(textToSpeak = "This is an example of text to speak.")
}
