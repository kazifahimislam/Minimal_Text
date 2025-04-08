package com.minimalcode.minimaltext

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import com.minimalcode.minimaltext.ui.theme.MinimalTextTheme
import androidx.activity.ComponentActivity

import com.example.learningcompose.update.ShowUpdateDialog
import com.example.learningcompose.update.UpdateChecker
import com.minimalcode.minimaltext.sendMessage.AmbientWhatsAppUI



class MainActivity : ComponentActivity() {
    private var showDialog = false
    private var apkUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MinimalTextTheme {

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

