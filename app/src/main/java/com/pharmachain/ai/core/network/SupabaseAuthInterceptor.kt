package com.pharmachain.ai.core.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp Interceptor adding 'apikey' and 'Authorization: Bearer <token>' headers
 * for Supabase REST (PostgREST) API requests.
 */
class SupabaseAuthInterceptor(
    private val apiKey: String,
    private val tokenProvider: (() -> String?)? = null
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = tokenProvider?.invoke() ?: apiKey

        val request = originalRequest.newBuilder()
            .header("apikey", apiKey)
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(request)
    }
}
