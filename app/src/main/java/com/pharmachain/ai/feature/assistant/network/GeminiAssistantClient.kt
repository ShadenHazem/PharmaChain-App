package com.pharmachain.ai.feature.assistant.network

import android.util.Log
import com.pharmachain.ai.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * ════════════════════════════════════════════════════════════════════════════════
 * ARCHITECT DECISION & CRITICAL SECURITY WARNING (SECTION 4.3):
 * ────────────────────────────────────────────────────────────────────────────────
 * For this prototype and testing phase, the Gemini API key is injected via
 * BuildConfig (managed through .env and Google AI Studio Secrets).
 *
 * CRITICAL WARNING BEFORE REAL LAUNCH:
 * Embedding an API key directly in a shipped Android APK is NOT safe for production.
 * Any compiled APK can be easily decompiled/reverse-engineered, allowing bad actors
 * to extract the key and abuse your billing quota.
 *
 * MANDATORY ACTION FOR PRODUCTION:
 * Prior to releasing to real pharmacy users, this direct mobile-to-Gemini call
 * MUST be replaced by a secure backend proxy (e.g. Firebase Cloud Function or
 * dedicated server endpoint) holding the API key server-side with Firebase App Check
 * / OAuth token validation.
 * ════════════════════════════════════════════════════════════════════════════════
 */
object GeminiAssistantClient {

    private const val TAG = "GeminiAssistantClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    // Primary fast, grounded models on Google AI Studio
    const val DEFAULT_MODEL = "gemini-3.5-flash"
    private val CANDIDATE_MODELS = listOf("gemini-3.5-flash", "gemini-flash-latest", "gemini-3.1-pro-preview")

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val api: GeminiAssistantApi by lazy {
        retrofit.create(GeminiAssistantApi::class.java)
    }

    /**
     * Executes content generation with structured system instructions, conversation turns,
     * and grounded data context. Returns response text and token metrics.
     */
    suspend fun generateGroundedResponse(
        systemInstructionText: String,
        conversationTurns: List<GeminiContent>,
        apiKey: String = BuildConfig.GEMINI_API_KEY,
        model: String = DEFAULT_MODEL
    ): GeminiGenerationResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is not configured in BuildConfig / Secrets panel")
            return@withContext GeminiGenerationResult.KeyMissing(
                message = "Gemini API Key is not configured. Please add your GEMINI_API_KEY in the AI Studio Secrets panel."
            )
        }

        val request = GeminiGenerateContentRequest(
            contents = conversationTurns,
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = systemInstructionText))
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.2f, // Low temperature for deterministic, grounded data answers
                topP = 0.95f,
                topK = 40,
                maxOutputTokens = 750
            )
        )

        val modelsToTry = if (CANDIDATE_MODELS.contains(model)) {
            listOf(model) + (CANDIDATE_MODELS - model)
        } else {
            listOf(model) + CANDIDATE_MODELS
        }

        var lastErrorMessage = "Failed to connect to PharmaChain AI Assistant."

        for (targetModel in modelsToTry) {
            try {
                Log.d(TAG, "Calling Gemini API model: $targetModel with ${conversationTurns.size} turns")
                val response = api.generateContent(
                    model = targetModel,
                    apiKey = apiKey,
                    request = request
                )

                val text = response.candidates
                    ?.firstOrNull()
                    ?.content
                    ?.parts
                    ?.mapNotNull { it.text }
                    ?.joinToString("\n")

                val usage = response.usageMetadata
                val tokensUsed = usage?.totalTokenCount ?: 0

                if (!text.isNullOrBlank()) {
                    Log.d(TAG, "Gemini response received successfully with $targetModel ($tokensUsed tokens).")
                    return@withContext GeminiGenerationResult.Success(
                        text = text.trim(),
                        promptTokens = usage?.promptTokenCount ?: 0,
                        candidateTokens = usage?.candidatesTokenCount ?: 0,
                        totalTokens = tokensUsed
                    )
                }
            } catch (e: retrofit2.HttpException) {
                val errorBody = try { e.response()?.errorBody()?.string() } catch (ignored: Exception) { null }
                Log.w(TAG, "Gemini API request failed for model $targetModel: HTTP ${e.code()} - $errorBody", e)
                lastErrorMessage = "Gemini API Error (HTTP ${e.code()}): ${e.message()}"
                // If 404 (model not found), continue loop to next candidate model
                if (e.code() == 404 || e.code() == 400) {
                    continue
                }
            } catch (e: Exception) {
                Log.e(TAG, "Gemini API request failed for model $targetModel: ${e.message}", e)
                lastErrorMessage = e.localizedMessage ?: "Network error connecting to AI Assistant."
            }
        }

        GeminiGenerationResult.Error(errorMessage = lastErrorMessage)
    }
}

sealed interface GeminiGenerationResult {
    data class Success(
        val text: String,
        val promptTokens: Int,
        val candidateTokens: Int,
        val totalTokens: Int
    ) : GeminiGenerationResult

    data class KeyMissing(
        val message: String
    ) : GeminiGenerationResult

    data class Error(
        val errorMessage: String
    ) : GeminiGenerationResult
}
