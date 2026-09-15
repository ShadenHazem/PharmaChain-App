package com.pharmachain.ai.feature.forecasting

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pharmachain.ai.R
import com.pharmachain.ai.core.designsystem.components.PharmaCard
import com.pharmachain.ai.core.designsystem.components.PharmaPrimaryButton
import com.pharmachain.ai.core.model.ColumnMapping
import com.pharmachain.ai.core.model.MappingField

@Composable
fun ColumnMappingScreen(
    mappings: List<ColumnMapping>,
    isProcessing: Boolean,
    onMappingChanged: (String, MappingField) -> Unit,
    onConfirmMappings: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasLowConfidenceMatches = mappings.any { it.serverConfidence < 0.85 && it.mappedField != MappingField.IGNORE }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onNavigateBack,
                enabled = !isProcessing,
                modifier = Modifier.testTag("column_mapping_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_to_preview),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = stringResource(R.string.step_3_of_6),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.column_mapping_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.column_mapping_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Server-Side Data Pre-cleaning Notice
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.mapping_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (hasLowConfidenceMatches) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.missing_fields_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Mappings List
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            mappings.forEach { mapping ->
                ColumnMappingRow(
                    mapping = mapping,
                    onMappingChanged = { field -> onMappingChanged(mapping.sourceColumnName, field) }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        PharmaPrimaryButton(
            onClick = onConfirmMappings,
            enabled = !isProcessing,
            modifier = Modifier.fillMaxWidth(),
            testTag = "confirm_mappings_btn"
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = stringResource(R.string.validate_dataset_btn),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ColumnMappingRow(
    mapping: ColumnMapping,
    onMappingChanged: (MappingField) -> Unit
) {
    var isDropdownOpen by remember { mutableStateOf(false) }

    val confidence = mapping.serverConfidence
    val isHighConfidence = confidence >= 0.85
    val isModerateConfidence = confidence in 0.60..0.84

    val statusColor = when {
        mapping.mappedField == MappingField.IGNORE -> MaterialTheme.colorScheme.outline
        isHighConfidence -> MaterialTheme.colorScheme.secondary
        isModerateConfidence -> Color(0xFFE65100) // Amber / Dark Orange warning
        else -> MaterialTheme.colorScheme.error
    }

    val statusIcon = when {
        mapping.mappedField == MappingField.IGNORE -> Icons.Default.HelpOutline
        isHighConfidence -> Icons.Default.CheckCircle
        else -> Icons.Default.WarningAmber
    }

    val confidenceLabel = when {
        mapping.mappedField == MappingField.IGNORE -> "Unmapped Column (Ignored)"
        isHighConfidence -> "${(confidence * 100).toInt()}% Match Confidence"
        isModerateConfidence -> "${(confidence * 100).toInt()}% Moderate Match — Verify"
        else -> "${(confidence * 100).toInt()}% Low Confidence — Check Field"
    }

    PharmaCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mapping_row_${mapping.sourceColumnName}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mapping.sourceColumnName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = confidenceLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor,
                        fontWeight = if (isModerateConfidence || !isHighConfidence) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            // Dropdown Selector
            Box {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (isModerateConfidence) Color(0xFFFFF3E0) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clickable { isDropdownOpen = true }
                        .testTag("dropdown_trigger_${mapping.sourceColumnName}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = mapping.mappedField.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isModerateConfidence) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = if (isModerateConfidence) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                DropdownMenu(
                    expanded = isDropdownOpen,
                    onDismissRequest = { isDropdownOpen = false }
                ) {
                    MappingField.values().forEach { field ->
                        DropdownMenuItem(
                            text = { Text(field.displayName) },
                            onClick = {
                                onMappingChanged(field)
                                isDropdownOpen = false
                            }
                        )
                    }
                }
            }
        }
    }
}
