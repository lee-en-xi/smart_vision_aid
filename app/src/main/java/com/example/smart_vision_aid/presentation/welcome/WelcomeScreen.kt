package com.example.smart_vision_aid.presentation.welcome

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WelcomeScreen(onStartClick: () -> Unit) {
    Column(
            modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Smart Vision Aid", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onStartClick, modifier = Modifier.size(200.dp, 60.dp)) {
            Text("Start", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
