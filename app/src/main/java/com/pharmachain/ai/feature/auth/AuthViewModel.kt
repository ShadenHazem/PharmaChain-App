package com.pharmachain.ai.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pharmachain.ai.core.common.result.NetworkResult
import com.pharmachain.ai.core.common.session.SessionManager
import com.pharmachain.ai.core.model.Role
import com.pharmachain.ai.core.model.User
import com.pharmachain.ai.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _selectedRole = MutableStateFlow<Role>(Role.PHARMACIST)
    val selectedRole: StateFlow<Role> = _selectedRole.asStateFlow()

    init {
        val user = sessionManager.currentUser.value
        if (user != null) {
            _uiState.value = AuthUiState.Authenticated(user)
        }
    }

    fun selectRole(role: Role) {
        _selectedRole.value = role
    }

    fun login(email: String) {
        if (email.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter your email address")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = authRepository.login(email, _selectedRole.value)) {
                is NetworkResult.Success -> _uiState.value = AuthUiState.Authenticated(result.data)
                is NetworkResult.Error -> _uiState.value = AuthUiState.Error(result.message)
                is NetworkResult.Loading -> _uiState.value = AuthUiState.Loading
            }
        }
    }

    fun register(
        fullName: String,
        email: String,
        phone: String,
        businessName: String,
        regOrLicenseNumber: String,
        governorate: String,
        city: String,
        address: String
    ) {
        if (fullName.isBlank() || email.isBlank() || businessName.isBlank()) {
            _uiState.value = AuthUiState.Error("Please fill in all required fields")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = authRepository.register(
                fullName = fullName,
                email = email,
                phone = phone,
                role = _selectedRole.value,
                businessName = businessName,
                regOrLicenseNumber = regOrLicenseNumber,
                governorate = governorate,
                city = city,
                address = address
            )) {
                is NetworkResult.Success -> _uiState.value = AuthUiState.Authenticated(result.data)
                is NetworkResult.Error -> _uiState.value = AuthUiState.Error(result.message)
                is NetworkResult.Loading -> _uiState.value = AuthUiState.Loading
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState.Idle
        _selectedRole.value = Role.PHARMACIST
    }
}
