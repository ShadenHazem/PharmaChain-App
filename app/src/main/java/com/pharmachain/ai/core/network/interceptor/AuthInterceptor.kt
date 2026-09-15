package com.pharmachain.ai.core.network.interceptor

import com.pharmachain.ai.core.common.session.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val sessionManager: SessionManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        // Allow public auth endpoints without authorization header
        if (path.contains("api/v1/auth/login") || path.contains("api/v1/auth/register")) {
            return chain.proceed(originalRequest)
        }

        val token = sessionManager.getAccessToken()
        val authenticatedRequest = if (!token.isNullOrEmpty()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/json")
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(authenticatedRequest)

        // ARCHITECT DECISION: On 401 Unauthorized, automatically trigger session logout or token refresh
        if (response.code == 401 && !path.contains("api/v1/auth/refresh")) {
            // If token expired and cannot refresh, clear session
            sessionManager.clearSession()
        }

        return response
    }
}
