package com.pharmachain.ai.core.common.session

import android.content.Context
import android.content.SharedPreferences
import com.pharmachain.ai.core.model.Role
import com.pharmachain.ai.core.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(context: Context) {
    // ARCHITECT DECISION: Wrapped encrypted/standard prefs abstraction to store secure auth session tokens and active user role
    private val prefs: SharedPreferences = context.getSharedPreferences("pharmachain_secure_session", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    init {
        val token = prefs.getString(KEY_ACCESS_TOKEN, null)
        val userId = prefs.getString(KEY_USER_ID, null)
        val roleStr = prefs.getString(KEY_USER_ROLE, null)
        val email = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        val name = prefs.getString(KEY_USER_NAME, "") ?: ""
        val phone = prefs.getString(KEY_USER_PHONE, "") ?: ""

        if (token != null && userId != null && roleStr != null) {
            val role = try { Role.valueOf(roleStr) } catch (e: Exception) { Role.PHARMACIST }
            _accessToken.value = token
            _currentUser.value = User(
                id = userId,
                role = role,
                fullName = name,
                email = email,
                phone = phone,
                createdAt = System.currentTimeMillis()
            )
        }
    }

    fun saveSession(token: String, refreshToken: String, user: User) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, token)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_USER_ROLE, user.role.name)
            .putString(KEY_USER_EMAIL, user.email)
            .putString(KEY_USER_NAME, user.fullName)
            .putString(KEY_USER_PHONE, user.phone)
            .apply()

        _accessToken.value = token
        _currentUser.value = user
    }

    fun clearSession() {
        prefs.edit().clear().apply()
        _accessToken.value = null
        _currentUser.value = null
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    // ==========================================================================
    // Secure Integration Credential Storage
    // The secret is strictly stored here and NEVER written to Room or plain logs.
    // ==========================================================================

    fun saveIntegrationSecret(credentialRef: String, secret: String) {
        prefs.edit()
            .putString(PREF_PREFIX_INTEGRATION_SECRET + credentialRef, secret)
            .apply()
    }

    fun getIntegrationSecret(credentialRef: String): String? {
        return prefs.getString(PREF_PREFIX_INTEGRATION_SECRET + credentialRef, null)
    }

    fun deleteIntegrationSecret(credentialRef: String) {
        prefs.edit()
            .remove(PREF_PREFIX_INTEGRATION_SECRET + credentialRef)
            .apply()
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_PHONE = "user_phone"
        private const val PREF_PREFIX_INTEGRATION_SECRET = "sec_cred_"
    }
}
