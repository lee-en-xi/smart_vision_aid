package com.example.smart_vision_aid.cropUtils

import android.Manifest
import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import java.io.File
import java.io.IOException
import java.util.*

@Composable
fun VoiceUploadScreen(navController: NavController) {
    val context = LocalContext.current
    var recordingState by remember { mutableStateOf(RecordingState.IDLE) }
    var playbackState by remember { mutableStateOf(PlaybackState.STOPPED) }
    var uploadState by remember { mutableStateOf(UploadState.NOT_STARTED) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var elapsedTime by remember { mutableStateOf(0) }
    var voiceId by remember { mutableStateOf("") }

    val mediaRecorder = remember { MediaRecorder() }
    val mediaPlayer = remember { MediaPlayer() }
    var audioFile: File? by remember { mutableStateOf(null) }

    // Clean up resources
    DisposableEffect(Unit) {
        onDispose {
            mediaRecorder.release()
            mediaPlayer.release()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        showPermissionDialog = !granted
    }

    LaunchedEffect(Unit) {
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else true

        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Create Your Voice Clone",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4CAF50)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Progress stepper
        VoiceUploadStepper(
            recordingState = recordingState,
            uploadState = uploadState,
            voiceId = voiceId
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Recording section
        when (recordingState) {
            RecordingState.IDLE -> {
                RecordingInstructions()
                Spacer(modifier = Modifier.height(24.dp))
                StartRecordingButton {
                    startRecording(context, mediaRecorder) { file ->
                        audioFile = file
                        recordingState = RecordingState.RECORDING
                    }
                }
            }

            RecordingState.RECORDING -> {
                RecordingProgress(elapsedTime, progress)
                Spacer(modifier = Modifier.height(24.dp))
                StopRecordingButton {
                    stopRecording(mediaRecorder)
                    recordingState = RecordingState.RECORDED
                }
            }

            RecordingState.RECORDED -> {
                RecordingComplete()
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PlaybackButton(
                        playbackState = playbackState,
                        onPlay = { playRecording(mediaPlayer, audioFile) { playbackState = PlaybackState.PLAYING } },
                        onStop = { stopPlayback(mediaPlayer) { playbackState = PlaybackState.STOPPED } }
                    )

                    ReRecordButton {
                        stopPlayback(mediaPlayer) { playbackState = PlaybackState.STOPPED }
                        recordingState = RecordingState.IDLE
                    }

                    UploadButton(
                        enabled = playbackState == PlaybackState.STOPPED,
                        onClick = {
                            uploadState = UploadState.UPLOADING
                            simulateVoiceUpload(audioFile) { id ->
                                voiceId = id
                                uploadState = UploadState.COMPLETE
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Upload progress
        if (uploadState == UploadState.UPLOADING) {
            LinearProgressIndicator(
                progress = progress, // Pass the Float value directly
                modifier = Modifier.fillMaxWidth(0.8f),
                color = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Uploading voice data...", color = Color.Gray)
        }

        // Upload complete
        if (uploadState == UploadState.COMPLETE) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Voice Clone Created!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text("Voice ID: $voiceId", color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    )
                ) {
                    Text("Finish Setup")
                }
            }
        }
    }

    // Timer for recording progress
    LaunchedEffect(recordingState) {
        if (recordingState == RecordingState.RECORDING) {
            while (recordingState == RecordingState.RECORDING) {
                delay(1000L)
                elapsedTime++
                progress = minOf(elapsedTime / 30f, 1f) // Max 30 seconds
            }
        }
    }
}

@Composable
fun VoiceUploadStepper(
    recordingState: RecordingState,
    uploadState: UploadState,
    voiceId: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StepIndicator(
            number = 1,
            title = "Record",
            active = recordingState != RecordingState.IDLE,
            completed = recordingState == RecordingState.RECORDED || uploadState == UploadState.COMPLETE
        )

        StepIndicator(
            number = 2,
            title = "Upload",
            active = uploadState == UploadState.UPLOADING,
            completed = uploadState == UploadState.COMPLETE
        )

        StepIndicator(
            number = 3,
            title = "Use",
            active = false,
            completed = voiceId.isNotEmpty()
        )
    }
}

@Composable
fun StepIndicator(number: Int, title: String, active: Boolean, completed: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = when {
                        completed -> Color(0xFF4CAF50)
                        active -> Color(0x334CAF50) // 20% opacity
                        else -> Color.LightGray
                    },
                    shape = RoundedCornerShape(18.dp)
                )
        ) {
            if (completed) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = number.toString(),
                    color = if (active) Color(0xFF4CAF50) else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            color = if (active || completed) Color(0xFF4CAF50) else Color.Gray,
            fontSize = 12.sp
        )
    }
}

@Composable
fun RecordingInstructions() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0x334CAF50))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Recording Instructions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("1. Find a quiet place without background noise")
            Text("2. Hold the phone about 6 inches from your mouth")
            Text("3. Speak clearly in a natural voice")
            Text("4. Read different types of text (stories, news, conversations)")
            Text("5. Aim for 1-5 minutes of clear speech")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "For best results, record in English",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )
        }
    }
}

@Composable
fun StartRecordingButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4CAF50),
            contentColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(56.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = "Record",
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Start Recording", fontSize = 18.sp)
    }
}

@Composable
fun RecordingProgress(elapsedTime: Int, progress: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Recording...",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4CAF50)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = formatTime(elapsedTime),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = progress, // Pass the Float value directly
            modifier = Modifier.fillMaxWidth(0.8f),
            color = Color(0xFF4CAF50)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Speak clearly into the microphone",
            color = Color.Gray
        )
        Text(
            text = "Recommended: ${30 - elapsedTime} seconds remaining",
            color = if (elapsedTime >= 25) Color(0xFFF44336) else Color.Gray
        )
    }
}

@Composable
fun StopRecordingButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFF44336),
            contentColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(56.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Stop,
            contentDescription = "Stop",
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Stop Recording", fontSize = 18.sp)
    }
}

@Composable
fun RecordingComplete() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0x334CAF50))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Recording Complete!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )
            Text("Review your recording before uploading", color = Color.Gray)
        }
    }
}

@Composable
fun PlaybackButton(
    playbackState: PlaybackState,
    onPlay: () -> Unit,
    onStop: () -> Unit
) {
    IconButton(
        onClick = { if (playbackState == PlaybackState.PLAYING) onStop() else onPlay() },
        modifier = Modifier.size(56.dp)
    ) {
        Icon(
            imageVector = if (playbackState == PlaybackState.PLAYING) Icons.Default.Stop else Icons.Default.PlayArrow,
            contentDescription = if (playbackState == PlaybackState.PLAYING) "Stop" else "Play",
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
fun ReRecordButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(56.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Replay,
            contentDescription = "Re-record",
            tint = Color(0xFFF44336),
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
fun UploadButton(enabled: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(56.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CloudUpload,
            contentDescription = "Upload",
            tint = if (enabled) Color(0xFF4CAF50) else Color.Gray,
            modifier = Modifier.size(36.dp)
        )
    }
}

// Recording state
enum class RecordingState { IDLE, RECORDING, RECORDED }
enum class PlaybackState { STOPPED, PLAYING }
enum class UploadState { NOT_STARTED, UPLOADING, COMPLETE }

// Recording functions
private fun startRecording(context: Context, mediaRecorder: MediaRecorder, onFileCreated: (File) -> Unit) {
    val outputFile = File(context.cacheDir, "voice_recording_${Date().time}.mp3")

    try {
        mediaRecorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFile.absolutePath)
            prepare()
            start()
        }
        onFileCreated(outputFile)
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

private fun stopRecording(mediaRecorder: MediaRecorder) {
    try {
        mediaRecorder.apply {
            stop()
            reset()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun playRecording(mediaPlayer: MediaPlayer, audioFile: File?, onStarted: () -> Unit) {
    audioFile?.let {
        try {
            mediaPlayer.apply {
                reset()
                setDataSource(it.absolutePath)
                prepare()
                start()
                setOnCompletionListener {
                    onStarted() // Reset playback state when completed
                }
            }
            onStarted()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

private fun stopPlayback(mediaPlayer: MediaPlayer, onStopped: () -> Unit) {
    try {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        onStopped()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// Simulate voice upload to ElevenLabs API
private fun simulateVoiceUpload(audioFile: File?, onComplete: (String) -> Unit) {
    // In a real app, this would make a network call to your backend
    // which would then call the ElevenLabs API

    // Simulate upload process
    Thread {
        // Simulate processing time
        Thread.sleep(3000L)

        // Generate a fake voice ID
        val voiceId = "vc_${UUID.randomUUID().toString().substring(0, 8)}"

        onComplete(voiceId)
    }.start()
}

// Helper function to format time
private fun formatTime(seconds: Int): String {
    val min = seconds / 60
    val sec = seconds % 60
    return String.format("%02d:%02d", min, sec)
}