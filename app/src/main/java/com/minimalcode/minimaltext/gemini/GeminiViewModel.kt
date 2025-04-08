package com.minimalcode.minimaltext.gemini

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ViewModel to handle Gemini API interactions
class GeminiViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<GeminiUiState>(GeminiUiState.Idle)
    val uiState: StateFlow<GeminiUiState> = _uiState.asStateFlow()

    // Initialize the Gemini model with your API key
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash", // Use "gemini-pro" or another available model name
        apiKey = Constants().geminAPIKey // Ensure this provides a valid API key
    )

    fun generateResponse(userMessage: String) {
        viewModelScope.launch {
            try {
                _uiState.value = GeminiUiState.Loading

                // Create content for the API request using the updated syntax
                val content = content {
                    text("I am using gemini API for text generation in my application I want that you take users propmt and genrate response straight forwad" +
                            "for example the user might say generate a formal response for my client that the work has been done" +
                            "and you will only response the text that will be directly used by the user and answer will be like " +
                            "I have completed your work properly like this do not response any extra things and also do not use anything like alright I am " +
                            "generating a response for you , just generate the answer that user is waiting for " +"users message:"+userMessage) // Simplified way to add text content

                }

                // Make the API call to Gemini
                val response = generativeModel.generateContent(content)

                // Extract the text from the response
                val generatedText = response.text ?: throw Exception("No response text received")

                _uiState.value = GeminiUiState.Success(generatedText)
            } catch (e: Exception) {
                _uiState.value = GeminiUiState.Error("Failed to get response: ${e.message}")
                Log.e("GeminiViewModel", "Error: ${e.message}", e)
            }
        }
    }

    fun resetState() {
        _uiState.value = GeminiUiState.Idle
    }
}