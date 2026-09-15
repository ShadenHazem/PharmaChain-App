package com.pharmachain.ai.feature.assistant.ui

import com.pharmachain.ai.core.database.entity.ChatMessageEntity

data class SuggestedPrompt(
    val id: String,
    val titleRes: Int,
    val promptTextRes: Int
)

data class AssistantUiState(
    val messages: List<ChatMessageEntity> = emptyList(),
    val isLoading: Boolean = false,
    val inputText: String = "",
    val errorMessage: String? = null,
    val pharmacyName: String = "El-Ezaby Pharmacy",
    val isRtl: Boolean = false
)
