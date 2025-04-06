package com.example.smart_vision_aid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_vision_aid.presentation.auth.LoginScreen
import com.example.smart_vision_aid.presentation.auth.RegisterScreen
import com.example.smart_vision_aid.ui.theme.Smart_Vision_AidTheme
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Smart_Vision_AidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser != null) {
                        // If user is logged in, show main content or camera
                        WelcomeScreen(onStartClick = { /* TODO: Navigate to main content */ })
                    } else {
                        // If user is not logged in, show login screen
                        LoginScreen(onLoginSuccess = { /* Handle after login success */ })
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeScreen(onStartClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Smart Vision Aid",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onStartClick,
            modifier = Modifier
                .height(60.dp)
                .width(220.dp)
        ) {
            Text(text = "Start", fontSize = 18.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomePreview() {
    Smart_Vision_AidTheme {
        WelcomeScreen(onStartClick = {})
    }
}
