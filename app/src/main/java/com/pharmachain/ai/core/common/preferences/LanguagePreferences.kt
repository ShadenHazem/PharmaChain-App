package com.pharmachain.ai.core.common.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pharmachain.ai.core.common.locale.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.preferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "pharmachain_app_prefs")

class LanguagePreferences(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "pharmachain_app_prefs_sync"
        private const val KEY_SYNC_APP_LANGUAGE = "sync_app_language_code"
        private val KEY_APP_LANGUAGE = stringPreferencesKey("app_language_code")
        private val KEY_FIRST_LAUNCH_PROMPT_COMPLETED = booleanPreferencesKey("is_first_launch_language_completed")

        fun getStoredLanguageSync(context: Context): AppLanguage {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val code = prefs.getString(KEY_SYNC_APP_LANGUAGE, null)
            return AppLanguage.fromCode(code)
        }

        fun saveStoredLanguageSync(context: Context, language: AppLanguage) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_SYNC_APP_LANGUAGE, language.code)
                .apply()
        }
    }

    /**
     * Flow emitting the user's selected language.
     * Defaults to ARABIC on first launch (Egypt market focus).
     */
    val languageFlow: Flow<AppLanguage> = context.preferencesDataStore.data.map { preferences ->
        val code = preferences[KEY_APP_LANGUAGE]
        AppLanguage.fromCode(code)
    }

    /**
     * Flow tracking whether the user has completed the first launch language selection onboarding.
     */
    val isLanguageOnboardingCompletedFlow: Flow<Boolean> = context.preferencesDataStore.data.map { preferences ->
        preferences[KEY_FIRST_LAUNCH_PROMPT_COMPLETED] ?: false
    }

    suspend fun getSelectedLanguage(): AppLanguage {
        val preferences = context.preferencesDataStore.data.first()
        val code = preferences[KEY_APP_LANGUAGE]
        return AppLanguage.fromCode(code)
    }

    suspend fun isLanguageOnboardingCompleted(): Boolean {
        val preferences = context.preferencesDataStore.data.first()
        return preferences[KEY_FIRST_LAUNCH_PROMPT_COMPLETED] ?: false
    }

    suspend fun setLanguage(language: AppLanguage) {
        saveStoredLanguageSync(context, language)
        context.preferencesDataStore.edit { preferences ->
            preferences[KEY_APP_LANGUAGE] = language.code
        }
    }

    suspend fun setLanguageOnboardingCompleted(completed: Boolean = true) {
        context.preferencesDataStore.edit { preferences ->
            preferences[KEY_FIRST_LAUNCH_PROMPT_COMPLETED] = completed
        }
    }
}
