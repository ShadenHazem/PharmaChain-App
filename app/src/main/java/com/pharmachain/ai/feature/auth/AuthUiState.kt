package com.pharmachain.ai.feature.auth

import com.pharmachain.ai.core.model.Role
import com.pharmachain.ai.core.model.User

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Authenticated(val user: User) : AuthUiState
    data class Error(val message: String) : AuthUiState
}
