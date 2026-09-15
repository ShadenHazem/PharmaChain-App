package com.pharmachain.ai.core.common.locale

import java.util.Locale

/**
 * Supported application languages in PharmaChain AI.
 *
 * Requirements:
 * - Default to Arabic (ARABIC) on first launch, reflecting the Egyptian target market.
 * - Medication and brand names are standardized INN identifiers and are NEVER translated.
 */
enum class AppLanguage(
    val code: String,
    val displayNameNative: String,
    val displayNameSecondary: String
) {
    ARABIC(
        code = "ar",
        displayNameNative = "العربية",
        displayNameSecondary = "Arabic"
    ),
    ENGLISH(
        code = "en",
        displayNameNative = "English",
        displayNameSecondary = "الإنجليزية"
    );

    val isRtl: Boolean
        get() = this == ARABIC

    fun toLocale(): Locale = when (this) {
        ARABIC -> Locale("ar", "EG")
        ENGLISH -> Locale("en", "US")
    }

    companion object {
        val DEFAULT = ARABIC

        fun fromCode(code: String?): AppLanguage {
            if (code == null) return DEFAULT
            val lower = code.lowercase()
            return when {
                lower.startsWith("ar") -> ARABIC
                lower.startsWith("en") -> ENGLISH
                else -> DEFAULT
            }
        }
    }
}
