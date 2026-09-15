package com.pharmachain.ai.core.common.locale

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

object LanguageManager {

    private val _currentLanguage = MutableStateFlow(AppLanguage.DEFAULT)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    /**
     * Applies the given language app-wide using AndroidX's AppCompatDelegate per-app locale API.
     * Updates default Locale, per-app locales, and optionally triggers activity.recreate()
     * so Jetpack Compose stringResource() and Android Resources caches refresh completely.
     */
    fun applyLanguage(language: AppLanguage, activity: Activity? = null) {
        _currentLanguage.value = language
        val locale = Locale.forLanguageTag(language.code)
        Locale.setDefault(locale)
        val localeList = LocaleListCompat.forLanguageTags(language.code)
        AppCompatDelegate.setApplicationLocales(localeList)
        activity?.recreate()
    }

    /**
     * Synchronizes current language state from stored preferences or application locales.
     */
    fun syncCurrentLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        val locale = Locale.forLanguageTag(language.code)
        Locale.setDefault(locale)
    }

    /**
     * Creates a localized Context configured with the specified AppLanguage locale and layout direction.
     */
    fun createLocalizedContext(baseContext: Context, language: AppLanguage): Context {
        val locale = Locale.forLanguageTag(language.code)
        val config = Configuration(baseContext.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return baseContext.createConfigurationContext(config)
    }
}

