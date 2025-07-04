package com.example.smart_vision_aid.cropUtils

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.smart_vision_aid.MinimalCropControls
import com.example.smart_vision_aid.cropUtils.*

private val CropperDialogProperties = DialogProperties(
    usePlatformDefaultWidth = false,
    dismissOnBackPress = false,
    dismissOnClickOutside = false
)

@Composable
fun ImageCropperDialog(
    state: CropState,
    style: CropperStyle = CropperStyle(
        backgroundColor = Color.White,
        overlay = Color.White.copy(alpha = 0.7f),
        rectColor = Color.White,
        rectStrokeWidth = 3.dp,
        touchRad = 30.dp,
        autoZoom = true
    ),
    dialogProperties: DialogProperties = CropperDialogProperties
) {
    Dialog(
        onDismissRequest = { state.done(accept = false) },
        properties = dialogProperties
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            shape = RoundedCornerShape(0.dp),
            color = Color.White// No corners to ensure full-screen
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Cropper preview fills the screen
                CropperPreview(
                    state = state,
                    modifier = Modifier.fillMaxSize()
                )

                // Custom controls at the bottom
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cancel button
                    Button(
                        onClick = { state.done(accept = false) },
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error, // ErrorRed
                            contentColor = MaterialTheme.colorScheme.onError // TextOnPrimary
                        ),
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Confirm button
                    Button(
                        onClick = { state.done(accept = true) },
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .padding(start = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary, // GreenPrimary
                            contentColor = MaterialTheme.colorScheme.onPrimary // TextOnPrimary
                        ),
                    ) {
                        Text(
                            text = "Confirm",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
