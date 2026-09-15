package com.pharmachain.ai

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pharmachain.ai.core.common.locale.AppLanguage
import com.pharmachain.ai.core.common.locale.LanguageManager
import com.pharmachain.ai.core.common.preferences.LanguagePreferences
import com.pharmachain.ai.core.designsystem.theme.PharmaChainTheme
import com.pharmachain.ai.navigation.AppNavGraph
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val appLanguage = LanguagePreferences.getStoredLanguageSync(newBase)
        val locale = Locale.forLanguageTag(appLanguage.code)
        Locale.setDefault(locale)

        val config = Configuration(newBase.resources.configuration).apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }
        val localizedContext = newBase.createConfigurationContext(config)
        super.attachBaseContext(localizedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as PharmaChainApp

        setContent {
            val appLanguage by app.languagePreferences.languageFlow.collectAsStateWithLifecycle(
                initialValue = AppLanguage.DEFAULT
            )

            // Keep LanguageManager state synchronized
            LaunchedEffect(appLanguage) {
                LanguageManager.syncCurrentLanguage(appLanguage)
            }

            val layoutDirection = if (appLanguage.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

            val localizedConfiguration = remember(appLanguage) {
                Configuration(resources.configuration).apply {
                    val loc = Locale.forLanguageTag(appLanguage.code)
                    setLocale(loc)
                    setLayoutDirection(loc)
                }
            }

            CompositionLocalProvider(
                LocalContext provides this@MainActivity,
                LocalActivityResultRegistryOwner provides this@MainActivity,
                LocalConfiguration provides localizedConfiguration,
                LocalLayoutDirection provides layoutDirection
            ) {
                PharmaChainTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        AppNavGraph(
                            app = app,
                            currentLanguage = appLanguage
                        )
                    }
                }
            }
        }
    }
}
