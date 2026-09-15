package com.pharmachain.ai.core.common.util

import com.pharmachain.ai.core.common.locale.AppLanguage
import com.pharmachain.ai.core.common.locale.LanguageManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    fun formatDateTime(
        epochMillis: Long,
        language: AppLanguage = LanguageManager.currentLanguage.value
    ): String {
        val locale = language.toLocale()
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", locale)
        return sdf.format(Date(epochMillis))
    }

    fun formatDate(
        epochMillis: Long,
        language: AppLanguage = LanguageManager.currentLanguage.value
    ): String {
        val locale = language.toLocale()
        val sdf = SimpleDateFormat("dd MMM yyyy", locale)
        return sdf.format(Date(epochMillis))
    }

    fun formatRelativeTime(
        epochMillis: Long,
        language: AppLanguage = LanguageManager.currentLanguage.value
    ): String {
        val now = System.currentTimeMillis()
        val diff = now - epochMillis
        val minutes = diff / (1000 * 60)
        val hours = diff / (1000 * 60 * 60)
        val days = diff / (1000 * 60 * 60 * 24)

        val isArabic = language == AppLanguage.ARABIC

        return when {
            minutes < 1 -> if (isArabic) "الآن" else "Just now"
            minutes < 60 -> if (isArabic) "منذ $minutes دقيقة" else "$minutes min ago"
            hours < 24 -> if (isArabic) "منذ $hours ساعة" else "$hours hr ago"
            days < 7 -> if (isArabic) "منذ $days يوم" else "$days day(s) ago"
            else -> formatDate(epochMillis, language)
        }
    }
}
