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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType

import androidx.compose.ui.unit.dp
import androidx.core.net.toUri

@Composable
fun WhatsAppScreen() {
    val context = LocalContext.current
    var countryCode by remember { mutableStateOf(TextFieldValue("91")) }
    var phoneNumber by remember { mutableStateOf(TextFieldValue("")) }
    var message by remember { mutableStateOf(TextFieldValue("")) }

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
                .height(120.dp),
            label = { Text("Message") },
            placeholder = { Text("Type your message here") },
            shape = RoundedCornerShape(8.dp)
        )

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