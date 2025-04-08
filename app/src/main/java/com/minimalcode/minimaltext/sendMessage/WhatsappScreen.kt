package com.minimalcode.minimaltext.sendMessage



import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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

@Composable
fun WhatsAppScreen() {
    val context = LocalContext.current
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
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Phone Number") },
            placeholder = { Text("e.g., 919876543210 (country code required)") },
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )

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
            onClick = { openWhatsApp(context, phoneNumber.text, message.text) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Open in WhatsApp", style = MaterialTheme.typography.labelLarge)
        }
    }
}

private fun openWhatsApp(context: Context, phoneNumber: String, message: String) {
    if (phoneNumber.isBlank()) {
        Toast.makeText(context, "Please enter a phone number", Toast.LENGTH_SHORT).show()
        return
    }

    val formattedNumber = phoneNumber.replace("\\s".toRegex(), "")
    if (formattedNumber.length < 10) {
        Toast.makeText(context, "Invalid phone number. Include country code (e.g., 91 for India).", Toast.LENGTH_LONG).show()
        return
    }

    try {
        val packageManager = context.packageManager
        val whatsappPackage = getWhatsAppPackage(packageManager)

        if (whatsappPackage != null) {
            // Try the whatsapp:// scheme first
            val uri = Uri.parse("whatsapp://send?phone=$formattedNumber&text=${Uri.encode(message)}")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage(whatsappPackage)
            }
            context.startActivity(intent)
        } else {
            // Fallback to wa.me link if WhatsApp isn’t detected
            Toast.makeText(context, "WhatsApp not installed, opening in browser", Toast.LENGTH_SHORT).show()
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(
                "https://wa.me/$formattedNumber?text=${Uri.encode(message)}"
            ))
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