package com.pharmachain.ai.core.common.util

import com.pharmachain.ai.core.common.locale.AppLanguage
import com.pharmachain.ai.core.common.locale.LanguageManager
import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    /**
     * Formats an amount in Egyptian Pounds (EGP / ج.م) following the selected app language.
     * In Arabic: "١٥٠٫٠٠ ج.م" or "150.00 ج.م"
     * In English: "150.00 EGP"
     */
    fun formatEgp(
        amount: Double,
        language: AppLanguage = LanguageManager.currentLanguage.value
    ): String {
        return try {
            val locale = language.toLocale()
            val format = NumberFormat.getNumberInstance(locale).apply {
                minimumFractionDigits = 2
                maximumFractionDigits = 2
            }
            val formattedNumber = format.format(amount)
            if (language == AppLanguage.ARABIC) {
                "$formattedNumber ج.م"
            } else {
                "$formattedNumber EGP"
            }
        } catch (e: Exception) {
            if (language == AppLanguage.ARABIC) {
                "%.2f ج.م".format(amount)
            } else {
                "%.2f EGP".format(amount)
            }
        }
    }

    fun formatEgp(amount: Double, locale: Locale): String {
        val language = if (locale.language.startsWith("ar")) AppLanguage.ARABIC else AppLanguage.ENGLISH
        return formatEgp(amount, language)
    }
}
