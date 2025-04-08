package com.minimalcode.minimaltext.gemini

// UI states for Gemini interactions
sealed class GeminiUiState {
    object Idle : GeminiUiState()
    object Loading : GeminiUiState()
    data class Success(val response: String) : GeminiUiState()
    data class Error(val message: String) : GeminiUiState()
}