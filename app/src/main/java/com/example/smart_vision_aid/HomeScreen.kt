package com.example.smart_vision_aid

import android.Manifest
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.navigation.compose.rememberNavController
import com.example.smart_vision_aid.ui.theme.GreenPrimary
import com.example.smart_vision_aid.ui.theme.GreenSecondary
import com.example.smart_vision_aid.ui.theme.GreenTertiary
import android.os.Build



@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current

    // Gallery launcher for picking image
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Navigate to upload screen with selected image URI
            navController.navigate("upload/${Uri.encode(it.toString())}")
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Smart Vision Aid",
            style = MaterialTheme.typography.displayLarge,
            color = GreenSecondary
        )

        Spacer(Modifier.height(48.dp))

        // Camera Button
        HomeButton(
            text = "Live Camera Scan",
            onClick = { navController.navigate("camera") },
            color = GreenPrimary
        )

        Spacer(Modifier.height(24.dp))

        // Upload Button with Permission Request
        HomeButton(
            text = "Upload Photo",
            onClick = {
                galleryLauncher.launch("image/*")
            },
            color = GreenSecondary
        )


    }

}

@Composable
fun HomeButton(
    text: String,
    onClick: () -> Unit,
    color: Color
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.White
        )
    ) {
        Text(text, fontSize = 22.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(navController = rememberNavController())
}
