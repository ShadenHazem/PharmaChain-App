package com.pharmachain.ai.feature.assistant.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pharmachain.ai.core.common.session.SessionManager
import com.pharmachain.ai.feature.assistant.data.AssistantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AssistantViewModel(
    private val assistantRepository: AssistantRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    private val currentUserId: String
        get() = sessionManager.currentUser.value?.id ?: "u_pharm_demo"

    init {
        val user = sessionManager.currentUser.value
        _uiState.update {
            it.copy(
                pharmacyName = if (user != null) "El-Ezaby Pharmacy" else "Community Pharmacy"
            )
        }
        observeMessages()
    }

    fun initWelcome(isArabic: Boolean) {
        viewModelScope.launch {
            assistantRepository.seedWelcomeMessageIfEmpty(currentUserId, isArabic)
        }
    }

    private fun observeMessages() {
        viewModelScope.launch {
            assistantRepository.getConversationHistory(currentUserId)
                .catch { e ->
                    _uiState.update { it.copy(errorMessage = e.localizedMessage) }
                }
                .collect { messages ->
                    _uiState.update { it.copy(messages = messages) }
                }
        }
    }

    fun onInputChanged(newText: String) {
        _uiState.update { it.copy(inputText = newText, errorMessage = null) }
    }

    fun sendMessage(explicitPrompt: String? = null) {
        val textToSend = (explicitPrompt ?: _uiState.value.inputText).trim()
        if (textToSend.isBlank() || _uiState.value.isLoading) return

        _uiState.update {
            it.copy(
                inputText = "",
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            val result = assistantRepository.sendMessage(textToSend, currentUserId)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Connection error. Please try again."
                        )
                    }
                }
            )
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            assistantRepository.clearHistory(currentUserId)
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
