package com.minimalcode.minimaltext.sendMessage



import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp



import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType

import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.minimalcode.minimaltext.gemini.GeminiUiState
import com.minimalcode.minimaltext.gemini.GeminiViewModel
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
@Composable
fun WhatsAppScreen(geminiViewModel: GeminiViewModel = viewModel()) {
    val context = LocalContext.current
    var countryCode by remember { mutableStateOf(TextFieldValue("91")) }
    var phoneNumber by remember { mutableStateOf(TextFieldValue("")) }
    var message by remember { mutableStateOf(TextFieldValue("")) }
    val geminiState by geminiViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()


    // Handle Gemini response updates
    LaunchedEffect(geminiState) {
        when (val state = geminiState) {
            is GeminiUiState.Success -> {
                message = TextFieldValue(state.response)
                geminiViewModel.resetState()
            }
            is GeminiUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                geminiViewModel.resetState()
            }
            else -> {}
        }}

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Send WhatsApp Message",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.primary
        )

        // Row for country code and phone number
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = countryCode,
                onValueChange = { countryCode = it },
                modifier = Modifier
                    .width(80.dp).padding(top = 8.dp), // Fixed width for country code

                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number) // Numeric input
            )
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                modifier = Modifier
                    .weight(1f), // Takes remaining width
                label = { Text("Phone Number") },
                placeholder = { Text("9876543210") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number) // Numeric input
            )
        }

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(120.dp),
            label = { Text("Message") },
            placeholder = {
                if (geminiState is GeminiUiState.Loading) {
                    Text("Gemini is thinking...")
                } else {
                    Text("Type your message here or use Gemini AI for text generation")
                }
            },
            shape = RoundedCornerShape(8.dp),
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (message.text.isNotEmpty() && geminiState !is GeminiUiState.Loading) {
                            scope.launch {
                                geminiViewModel.generateResponse(message.text)
                                message = TextFieldValue("") // Clear field while processing
                            }
                        }
                    },
                    enabled = message.text.isNotEmpty() && geminiState !is GeminiUiState.Loading
                ) {
                    if (geminiState is GeminiUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.width(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Message,
                            contentDescription = "Generate with Gemini",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            enabled = geminiState !is GeminiUiState.Loading
        )

        if (geminiState is GeminiUiState.Loading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.width(16.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gemini is generating a response...")
            }
        }

        Button(
            onClick = { openWhatsApp(context, countryCode.text, phoneNumber.text, message.text) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Open in WhatsApp", style = MaterialTheme.typography.labelLarge)
        }
    }
}

private fun openWhatsApp(context: Context, countryCode: String, phoneNumber: String, message: String) {
    if (phoneNumber.isBlank()) {
        Toast.makeText(context, "Please enter a phone number", Toast.LENGTH_SHORT).show()
        return
    }

    val formattedNumber = phoneNumber.replace("\\s".toRegex(), "")
    val formattedCountryCode = countryCode.replace("\\s".toRegex(), "")
    if (formattedNumber.length < 10) {
        Toast.makeText(context, "Invalid phone number. Include country code (e.g., 91 for India).", Toast.LENGTH_LONG).show()
        return
    }

    try {
        val packageManager = context.packageManager
        val whatsappPackage = getWhatsAppPackage(packageManager)

        if (whatsappPackage != null) {
            // Try the whatsapp:// scheme first
            val uri = "whatsapp://send?phone=$formattedCountryCode+$formattedNumber&text=${
                Uri.encode(message)
            }".toUri()
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage(whatsappPackage)
            }
            context.startActivity(intent)
        } else {

            val browserIntent = Intent(Intent.ACTION_VIEW,
                "https://wa.me/$formattedCountryCode+$formattedNumber?text=${Uri.encode(message)}".toUri())
            context.startActivity(browserIntent)
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error: ${e.localizedMessage}. Try installing WhatsApp.", Toast.LENGTH_LONG).show()
        e.printStackTrace()
    }
}

private fun getWhatsAppPackage(packageManager: PackageManager): String? {
    val packages = listOf("com.whatsapp", "com.whatsapp.w4b")
    for (pkg in packages) {
        if (isPackageInstalled(pkg, packageManager)) {
            return pkg
        }
    }
    return null
}

private fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
    return try {
        packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}