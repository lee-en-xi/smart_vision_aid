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
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Translate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.smart_vision_aid.ui.theme.TextPrimary



@Composable
fun ProcessingScreen(navController: NavController, extractedText: String) {
    var translatedText by remember { mutableStateOf(extractedText) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            "Detected Text:",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = translatedText,
            fontSize = 18.sp,
            color = TextPrimary,
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        )

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { navController.navigate("audio/$translatedText") },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .padding(end = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
            ) {
                Icon(Icons.Default.VolumeUp, contentDescription = "Speak")
                Spacer(Modifier.width(8.dp))
                Text("Read", fontSize = 17.sp, fontWeight = FontWeight.Medium)
            }

            Button(
                onClick = { navController.navigate("translation/$translatedText") },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .padding(start = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
            ) {
                Icon(Icons.Default.Translate, contentDescription = "Translate")
                Spacer(Modifier.width(8.dp))
                Text("Translate", fontSize = 17.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Preview
@Composable
fun ProcessingScreenPreview() {
    val navController = rememberNavController()
    ProcessingScreen(navController = navController, extractedText = "This is some sample text detected")
}
