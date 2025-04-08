package com.minimalcode.minimaltext

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import com.minimalcode.minimaltext.ui.theme.MinimalTextTheme
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import com.example.learningcompose.update.ShowUpdateDialog
import com.example.learningcompose.update.UpdateChecker
import com.minimalcode.minimaltext.sendMessage.AmbientWhatsAppUI



class MainActivity : ComponentActivity() {
    // Convert to state variables that can be observed by Compose
    private val showDialogState = mutableStateOf(false)
    private val apkUrlState = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkForUpdates()

        setContent {
            MinimalTextTheme {

                // Use remember to create a stable reference to the state
                var showDialog by remember { showDialogState }
                var apkUrl by remember { apkUrlState }


                // Show the update dialog if needed
                if (showDialog && apkUrl != null) {
                    ShowUpdateDialog(
                        onDismiss = { showDialog = false },
                        onUpdate = { apkUrl?.let { downloadAndInstallApk(it) } }
                    )
                }

                AmbientWhatsAppUI()


            }
        }

    }

    // Check for updates and show the dialog if an update is available
    private fun checkForUpdates() {
        val context = this

        UpdateChecker.checkForUpdates(context) { url ->
            // Update the state variables to trigger recomposition
            apkUrlState.value = url
            showDialogState.value = true
        }
    }

    private fun downloadAndInstallApk(apkUrl: String) {
        // Launch the browser to open the APK download URL
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl))
        startActivity(intent)
    }}

