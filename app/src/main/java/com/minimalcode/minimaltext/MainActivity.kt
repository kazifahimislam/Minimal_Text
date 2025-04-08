package com.minimalcode.minimaltext

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send

import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

import androidx.core.net.toUri
import com.minimalcode.minimaltext.ui.theme.MinimalTextTheme
import androidx.activity.ComponentActivity
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import com.example.learningcompose.update.ShowUpdateDialog
import com.example.learningcompose.update.UpdateChecker
import com.minimalcode.minimaltext.sendMessage.WhatsAppScreen


class MainActivity : ComponentActivity() {
    private var showDialog = false
    private var apkUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MinimalTextTheme {
                var dialogVisible by remember { mutableStateOf(showDialog) }
                var updateUrl by remember { mutableStateOf(apkUrl) }

                AmbientWhatsAppUI()

                // Show the update dialog if needed
                if (showDialog && apkUrl != null) {
                    ShowUpdateDialog(
                        onDismiss = { showDialog = false },
                        onUpdate = { apkUrl?.let { downloadAndInstallApk(it) } }
                    )
                }
            }
        }
        checkForUpdates()
    }

    // Check for updates and show the dialog if an update is available
    private fun checkForUpdates() {
        val context = this

        UpdateChecker.checkForUpdates(context) { url ->
            apkUrl = url
            showDialog = true
        }
    }

    private fun downloadAndInstallApk(apkUrl: String) {
        // Launch the browser to open the APK download URL
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl))
        startActivity(intent)
    }}

@Composable
fun AmbientWhatsAppUI() {
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryDark = primaryColor.copy(alpha = 0.8f)
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    val infiniteTransition = rememberInfiniteTransition(label = "ambient")
    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .alpha(animatedAlpha)
                .blur(50.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(primaryColor, Color.Transparent),
                        center = Offset(150f, 150f),
                        radius = 300f
                    ),
                    shape = CircleShape
                )
                .align(Alignment.TopEnd)
        )

        Box(
            modifier = Modifier
                .size(250.dp)
                .alpha(animatedAlpha)
                .blur(40.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(secondaryColor, Color.Transparent),
                        center = Offset(125f, 125f),
                        radius = 250f
                    ),
                    shape = CircleShape
                )
                .align(Alignment.BottomStart)
        )

        Box(
            modifier = Modifier
                .size(200.dp)
                .alpha(animatedAlpha)
                .blur(30.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(tertiaryColor, Color.Transparent),
                        center = Offset(100f, 100f),
                        radius = 200f
                    ),
                    shape = CircleShape
                )
                .align(Alignment.CenterEnd)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            StatusBar()
            Spacer(modifier = Modifier.height(24.dp))
            WhatsAppScreen()
        }
    }
}

@Composable
fun StatusBar() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MinimalText",

                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

        }
    }
}