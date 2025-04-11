package com.minimalcode.minimaltext.sendMessage

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.minimalcode.minimaltext.R
import com.minimalcode.minimaltext.gemini.GeminiUiState
import com.minimalcode.minimaltext.gemini.GeminiViewModel
import kotlinx.coroutines.launch

data class Contact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val extractedCountryCode: String = "",
    val extractedNationalNumber: String = ""
)

data class PhoneNumberParts(
    val countryCode: String,
    val nationalNumber: String,
    val hasCountryCode: Boolean
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun WhatsAppScreen(geminiViewModel: GeminiViewModel = viewModel()) {
    val context = LocalContext.current
    var countryCode by remember { mutableStateOf(TextFieldValue("91")) }  // Default country code
    var phoneNumber by remember { mutableStateOf(TextFieldValue("")) }
    var message by remember { mutableStateOf(TextFieldValue("")) }
    val geminiState by geminiViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    // Contact search states
    var contactQuery by remember { mutableStateOf("") }
    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var showContactsList by remember { mutableStateOf(false) }
    var hasContactPermission by remember { mutableStateOf(
        context.checkSelfPermission(Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED
    )}

    // Contact permission request
    val contactPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasContactPermission = isGranted
            if (isGranted) {
                // Load contacts if permission granted
                contacts = loadContacts(context, contactQuery)
                showContactsList = true
            } else {
                Toast.makeText(context, "Contact permission required to search contacts", Toast.LENGTH_SHORT).show()
            }
        }
    )

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
        }
    }

    // Search contacts when query changes
    LaunchedEffect(contactQuery) {
        if (hasContactPermission && contactQuery.isNotEmpty()) {
            contacts = loadContacts(context, contactQuery)
            showContactsList = contacts.isNotEmpty()
        } else {
            showContactsList = false
        }
    }

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
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.secondary,
                    unfocusedTextColor = MaterialTheme.colorScheme.secondary,
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.secondary,
                    focusedLabelColor = MaterialTheme.colorScheme.secondary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.secondary,
                ),
                label = { Text("Country Code", color = MaterialTheme.colorScheme.secondary) },
                modifier = Modifier.width(80.dp),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = {
                    val parts = extractPhoneNumberParts(it.text)
                    if (parts.hasCountryCode) {
                        // If country code detected, split it
                        countryCode = TextFieldValue(parts.countryCode)
                        phoneNumber = TextFieldValue(parts.nationalNumber)
                    } else {
                        phoneNumber = it
                    }
                    contactQuery = it.text
                    if (it.text.isEmpty()) {
                        showContactsList = false
                    }
                },
                modifier = Modifier.weight(1f),
                label = { Text("Phone Number", color = MaterialTheme.colorScheme.secondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.secondary,
                    unfocusedTextColor = MaterialTheme.colorScheme.secondary,
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.secondary,
                    focusedLabelColor = MaterialTheme.colorScheme.secondary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.secondary,
                ),
                placeholder = { Text("Search contacts or enter number", color = MaterialTheme.colorScheme.secondary) },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                trailingIcon = {
                    IconButton(onClick = {
                        if (phoneNumber.text.isNotEmpty()) {
                            phoneNumber = TextFieldValue("")
                            contactQuery = ""
                            showContactsList = false
                        } else {
                            if (hasContactPermission) {
                                showContactsList = true
                                contacts = loadContacts(context, "")
                            } else {
                                contactPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                            }
                        }
                    }) {
                        if (phoneNumber.text.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.ContactPhone,
                                contentDescription = "Search Contacts",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            )
        }

        // Contacts search results
        if (showContactsList) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (contacts.isEmpty()) {
                        item {
                            Text(
                                text = "No contacts found",
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    } else {
                        items(contacts) { contact ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // When selecting a contact, update both fields appropriately
                                        countryCode = TextFieldValue(contact.extractedCountryCode)
                                        phoneNumber = TextFieldValue(contact.extractedNationalNumber)
                                        showContactsList = false
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContactPhone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = contact.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = contact.phoneNumber,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    if (contact.extractedCountryCode.isNotEmpty()) {
                                        Text(
                                            text = "Country code: ${contact.extractedCountryCode}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(120.dp),
            label = { Text("Message", color = MaterialTheme.colorScheme.secondary) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.secondary,
                unfocusedTextColor = MaterialTheme.colorScheme.secondary,
                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                unfocusedBorderColor = MaterialTheme.colorScheme.secondary,
                focusedLabelColor = MaterialTheme.colorScheme.secondary,
                unfocusedLabelColor = MaterialTheme.colorScheme.secondary,
            ),
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
                    modifier = Modifier.padding(8.dp),
                    onClick = {
                        if (message.text.isNotEmpty() && geminiState !is GeminiUiState.Loading) {
                            scope.launch {
                                geminiViewModel.generateResponse(message.text)
                                message = TextFieldValue("") // Clear field while processing
                            }
                        }
                    },
                    enabled = message.text.isNotEmpty() && geminiState !is GeminiUiState.Loading,
                ) {
                    if (geminiState is GeminiUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.width(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.gemini),
                            contentDescription = "Generate with Gemini",
                            tint = Color.Unspecified,
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
                Text("Gemini is generating a response...", color = MaterialTheme.colorScheme.secondary)
            }
        }

        Button(
            onClick = {
                openWhatsApp(context, countryCode.text, phoneNumber.text, message.text)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Open in WhatsApp", style = MaterialTheme.typography.labelLarge)
        }
    }
}

// Function to extract country code and national number from a phone number
fun extractPhoneNumberParts(phoneNumber: String): PhoneNumberParts {
    val trimmedNumber = phoneNumber.trim().replace("\\s+".toRegex(), "")

    // Case 1: Starts with +
    if (trimmedNumber.startsWith("+")) {
        // Most country codes are 1-3 digits
        val countryCodeEndIndex = minOf(4, trimmedNumber.length - 1)
        for (i in 1..countryCodeEndIndex) {
            val potentialCountryCode = trimmedNumber.substring(1, i + 1)
            val nationalNumber = trimmedNumber.substring(i + 1)
            if (isValidCountryCode(potentialCountryCode)) {
                return PhoneNumberParts(potentialCountryCode, nationalNumber, true)
            }
        }
        // Fallback if no valid code found: assume first 2 digits as country code
        val codeLength = minOf(2, trimmedNumber.length - 1)
        return PhoneNumberParts(
            trimmedNumber.substring(1, codeLength + 1),
            trimmedNumber.substring(codeLength + 1),
            true
        )
    }

    // Case 2: Starts with 00 (international format)
    else if (trimmedNumber.startsWith("00")) {
        val countryCodeEndIndex = minOf(5, trimmedNumber.length - 1)
        for (i in 2..countryCodeEndIndex) {
            val potentialCountryCode = trimmedNumber.substring(2, i + 1)
            val nationalNumber = trimmedNumber.substring(i + 1)
            if (isValidCountryCode(potentialCountryCode)) {
                return PhoneNumberParts(potentialCountryCode, nationalNumber, true)
            }
        }
        // Fallback: assume first 2 digits after 00 as country code
        val codeLength = minOf(2, trimmedNumber.length - 2)
        return PhoneNumberParts(
            trimmedNumber.substring(2, codeLength + 2),
            trimmedNumber.substring(codeLength + 2),
            true
        )
    }

    // Case 3: No country code format detected
    return PhoneNumberParts("", trimmedNumber, false)
}

// Function to check if a string is a valid country code
// This is a simplified version - in a real app, you might want to use a library like libphonenumber
fun isValidCountryCode(code: String): Boolean {
    // Common country codes (1-3 digits)
    val commonCodes = listOf(
        "1", "7", "20", "27", "30", "31", "32", "33", "34", "36", "39",
        "40", "41", "43", "44", "45", "46", "47", "48", "49", "51", "52",
        "53", "54", "55", "56", "57", "58", "60", "61", "62", "63", "64",
        "65", "66", "81", "82", "84", "86", "90", "91", "92", "93", "94",
        "95", "98", "212", "213", "216", "218", "220", "221", "222", "223",
        "224", "225", "226", "227", "228", "229", "230", "231", "232", "233",
        "234", "235", "236", "237", "238", "239", "240", "241", "242", "243",
        "244", "245", "246", "247", "248", "249", "250", "251", "252", "253",
        "254", "255", "256", "257", "258", "260", "261", "262", "263", "264",
        "265", "266", "267", "268", "269", "290", "291", "297", "298", "299",
        "350", "351", "352", "353", "354", "355", "356", "357", "358", "359",
        "370", "371", "372", "373", "374", "375", "376", "377", "378", "380",
        "381", "382", "383", "385", "386", "387", "389", "420", "421", "423",
        "500", "501", "502", "503", "504", "505", "506", "507", "508", "509",
        "590", "591", "592", "593", "594", "595", "596", "597", "598", "599",
        "670", "672", "673", "674", "675", "676", "677", "678", "679", "680",
        "681", "682", "683", "685", "686", "687", "688", "689", "690", "691",
        "692", "850", "852", "853", "855", "856", "880", "886", "960", "961",
        "962", "963", "964", "965", "966", "967", "968", "970", "971", "972",
        "973", "974", "975", "976", "977", "992", "993", "994", "995", "996",
        "998"
    )

    return code.isNotEmpty() && code.all { it.isDigit() } && code in commonCodes
}

// Function to load contacts from the device
private fun loadContacts(context: Context, query: String): List<Contact> {
    val contacts = mutableListOf<Contact>()

    try {
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val selection = if (query.isNotEmpty()) {
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ? OR ${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ?"
        } else null

        val selectionArgs = if (query.isNotEmpty()) {
            arrayOf("%$query%", "%$query%")
        } else null

        val cursor: Cursor? = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        )

        cursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val id = it.getString(idIndex)
                val name = it.getString(nameIndex) ?: "Unknown"
                val number = it.getString(numberIndex)?.replace("\\s+".toRegex(), "") ?: ""

                if (number.isNotEmpty()) {
                    // Extract country code parts
                    val parts = extractPhoneNumberParts(number)
                    contacts.add(Contact(
                        id = id,
                        name = name,
                        phoneNumber = number,
                        extractedCountryCode = parts.countryCode,
                        extractedNationalNumber = parts.nationalNumber
                    ))
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    // Limit to 10 contacts for better performance
    return contacts.distinctBy { it.phoneNumber }.take(10)
}

private fun openWhatsApp(context: Context, countryCode: String, phoneNumber: String, message: String) {
    if (phoneNumber.isBlank()) {
        Toast.makeText(context, "Please enter a phone number", Toast.LENGTH_SHORT).show()
        return
    }

    // Format the phone number, removing any spaces
    val formattedNumber = phoneNumber.replace("\\s".toRegex(), "")
    val formattedCountryCode = countryCode.replace("\\s".toRegex(), "")

    // Combine the country code and number
    val fullNumber = if (formattedNumber.startsWith("+")) {
        // If number already has + symbol, use it as is but remove the + for WhatsApp URL
        formattedNumber.substring(1)
    } else {
        formattedCountryCode + formattedNumber
    }

    // Check for minimum length (different countries have different lengths)
    if (fullNumber.length < 8) { // Most phone numbers are at least 8 digits including country code
        Toast.makeText(context, "Phone number appears to be incomplete.", Toast.LENGTH_LONG).show()
        return
    }

    try {
        val packageManager = context.packageManager
        val whatsappPackage = getWhatsAppPackage(packageManager)

        if (whatsappPackage != null) {
            // Try the whatsapp:// scheme first
            val uri = "whatsapp://send?phone=$fullNumber&text=${
                Uri.encode(message)
            }".toUri()
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage(whatsappPackage)
            }
            context.startActivity(intent)
        } else {
            val browserIntent = Intent(Intent.ACTION_VIEW,
                "https://wa.me/$fullNumber?text=${Uri.encode(message)}".toUri())
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