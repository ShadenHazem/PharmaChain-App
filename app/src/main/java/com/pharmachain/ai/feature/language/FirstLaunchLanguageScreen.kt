package com.pharmachain.ai.feature.language

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pharmachain.ai.core.common.locale.AppLanguage
import com.pharmachain.ai.core.common.locale.LanguageManager
import com.pharmachain.ai.core.common.preferences.LanguagePreferences
import com.pharmachain.ai.core.designsystem.components.PharmaChainBrandDots
import com.pharmachain.ai.core.designsystem.components.PharmaChainLogoMark
import com.pharmachain.ai.core.designsystem.components.PharmaPrimaryButton
import kotlinx.coroutines.launch

@Composable
fun FirstLaunchLanguageScreen(
    languagePreferences: LanguagePreferences,
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    // Default to ARABIC on first launch (Egypt market requirement)
    var selectedLanguage by remember { mutableStateOf(AppLanguage.ARABIC) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val activity = context as? Activity

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(36.dp))

                // Brand Emblem
                PharmaChainLogoMark(size = 80.dp)

                Spacer(modifier = Modifier.height(16.dp))

                PharmaChainBrandDots()

                Spacer(modifier = Modifier.height(24.dp))

                // Dual-language Title
                Text(
                    text = "اختر لغتك المفضلة\nChoose Your Language",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Dual-language Subtitle
                Text(
                    text = "سوق الأدوية الذكي في مصر • B2B Pharma Platform",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Option 1: Arabic (Recommended for Egypt)
                LanguageOptionCard(
                    primaryLabel = "العربية",
                    secondaryLabel = "Arabic (Default for Egypt)",
                    badge = "موصى به في مصر • Recommended",
                    icon = Icons.Default.Language,
                    isSelected = selectedLanguage == AppLanguage.ARABIC,
                    onClick = { selectedLanguage = AppLanguage.ARABIC },
                    testTag = "language_option_arabic"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Option 2: English
                LanguageOptionCard(
                    primaryLabel = "English",
                    secondaryLabel = "الإنجليزية (International)",
                    badge = "Global Interface",
                    icon = Icons.Default.Public,
                    isSelected = selectedLanguage == AppLanguage.ENGLISH,
                    onClick = { selectedLanguage = AppLanguage.ENGLISH },
                    testTag = "language_option_english"
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, bottom = 16.dp)
            ) {
                PharmaPrimaryButton(
                    onClick = {
                        coroutineScope.launch {
                            languagePreferences.setLanguage(selectedLanguage)
                            languagePreferences.setLanguageOnboardingCompleted(true)
                            LanguageManager.applyLanguage(selectedLanguage, activity)
                            onLanguageSelected(selectedLanguage)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "confirm_language_button"
                ) {
                    Text(
                        text = if (selectedLanguage == AppLanguage.ARABIC) "متابعة • Continue" else "Continue • متابعة",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageOptionCard(
    primaryLabel: String,
    secondaryLabel: String,
    badge: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    }

    val cardBg = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Surface(
        shape = MaterialTheme.shapes.large,
        color = cardBg,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = primaryLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    RadioButton(
                        selected = isSelected,
                        onClick = onClick,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = secondaryLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
