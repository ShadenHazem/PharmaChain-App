package com.pharmachain.ai.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * PharmaCard provides a clean, pure-white card surface that visually lifts
 * off the softer off-white background with a subtle border and soft elevation.
 */
@Composable
fun PharmaCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ),
    border: BorderStroke? = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    elevation: CardElevation = CardDefaults.cardElevation(
        defaultElevation = 1.dp,
        pressedElevation = 2.dp,
        focusedElevation = 2.dp,
        hoveredElevation = 2.dp
    ),
    testTag: String = "pharma_card",
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.testTag(testTag),
        shape = shape,
        colors = colors,
        border = border,
        elevation = elevation,
        content = content
    )
}

/**
 * PharmaOutlinedCard provides an outlined card variant with a crisp 1dp border
 * and pure white surface container.
 */
@Composable
fun PharmaOutlinedCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    colors: CardColors = CardDefaults.outlinedCardColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ),
    border: BorderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    testTag: String = "pharma_outlined_card",
    content: @Composable ColumnScope.() -> Unit
) {
    OutlinedCard(
        modifier = modifier.testTag(testTag),
        shape = shape,
        colors = colors,
        border = border,
        content = content
    )
}
