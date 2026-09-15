package com.pharmachain.ai.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// ==============================================================================
// PharmaChain AI — Light-First Clinical & Trustworthy Color System
// Healthcare/Pharma B2B Design: High legibility, low glare, clear hierarchy.
// ==============================================================================

// Primary Brand — Trustworthy Clinical Sapphire Blue (M3 Tonal Palette)
val PharmaPrimary = Color(0xFF026AA7) // Calm, professional, high-contrast healthcare blue
val PharmaOnPrimary = Color(0xFFFFFFFF)
val PharmaPrimaryContainer = Color(0xFFE0F2FE) // Soft sky/clinical blue tint
val PharmaOnPrimaryContainer = Color(0xFF003554)
val PharmaInversePrimary = Color(0xFF7DD3FC)

// Secondary Brand — Clinical Medical Cyan / Teal
val PharmaSecondary = Color(0xFF088395)
val PharmaOnSecondary = Color(0xFFFFFFFF)
val PharmaSecondaryContainer = Color(0xFFE0F7FA)
val PharmaOnSecondaryContainer = Color(0xFF00363A)

// Tertiary Brand — Warm Amber Accent
val PharmaTertiary = Color(0xFFC05621)
val PharmaOnTertiary = Color(0xFFFFFFFF)
val PharmaTertiaryContainer = Color(0xFFFEF3C7)
val PharmaOnTertiaryContainer = Color(0xFF78350F)

// Background & Surfaces (Light-First: soft off-white background with pure white elevated cards)
val PharmaBackgroundLight = Color(0xFFF8F9FA) // Soft off-white to eliminate glare under pharmacy lights
val PharmaOnBackgroundLight = Color(0xFF191C1E) // Rich dark charcoal (not harsh #000000)

val PharmaSurfaceLight = Color(0xFFFFFFFF) // Pure white card surface to lift off the background
val PharmaOnSurfaceLight = Color(0xFF191C1E)
val PharmaSurfaceVariantLight = Color(0xFFF1F5F9) // Subtle slate/grey tint for insets, chips, and headers
val PharmaOnSurfaceVariantLight = Color(0xFF475569) // Mid-slate grey with WCAG AA 4.5:1+ contrast

// Outlines & Borders (Clean 1dp subtle borders)
val PharmaOutlineLight = Color(0xFFCBD5E1)
val PharmaOutlineVariantLight = Color(0xFFE2E8F0)

// Semantic / Status Colors (Distinguishable by color, text, and icons)
// Success: Delivered, Confirmed, Available, Active
val StatusSuccess = Color(0xFF15803D)
val StatusSuccessBg = Color(0xFFDCFCE7)
val StatusSuccessText = Color(0xFF14532D)

// Warning: Pending, Low Stock, Requires Review (Amber/Orange, never low-contrast yellow)
val StatusWarning = Color(0xFFB45309)
val StatusWarningBg = Color(0xFFFEF3C7)
val StatusWarningText = Color(0xFF78350F)

// Error: Cancelled, Rejected, Expired, Critical (Clinical Crimson Red)
val StatusError = Color(0xFFB91C1C)
val StatusErrorBg = Color(0xFFFEE2E2)
val StatusErrorText = Color(0xFF7F1D1D)

// Info: Shipped, In-Transit, Informational
val StatusInfo = Color(0xFF0284C7)
val StatusInfoBg = Color(0xFFE0F2FE)
val StatusInfoText = Color(0xFF0369A1)

// Backward-compatible semantic bindings for existing screens
val StatusPendingBg = StatusWarningBg
val StatusPendingText = StatusWarningText
val StatusConfirmedBg = StatusInfoBg
val StatusConfirmedText = StatusInfoText
val StatusShippedBg = StatusSuccessBg
val StatusShippedText = StatusSuccessText
val StatusDeliveredBg = StatusSuccessBg
val StatusDeliveredText = StatusSuccessText
val StatusCancelledBg = StatusErrorBg
val StatusCancelledText = StatusErrorText

// Logo Node Molecular Accents for Badges & Branding
val LogoNodeAmber = Color(0xFFE67E22)
val LogoNodeOrange = Color(0xFFEA580C)
val LogoNodeGold = Color(0xFFF59E0B)
val LogoNodeLime = Color(0xFF16A34A)
val LogoNodeGreen = Color(0xFF10B981)
val LogoNodeTeal = Color(0xFF00A896)
val LogoNodeCyan = Color(0xFF06B6D4)
val LogoNodeBlue = Color(0xFF0284C7)
val LogoNodeNavy = Color(0xFF026AA7)
val PharmaNavyPrimary = PharmaPrimary

// Dark Theme Palette (Retained as secondary option, not default)
val PharmaBackgroundDark = Color(0xFF0F172A)
val PharmaOnBackgroundDark = Color(0xFFF8FAFC)
val PharmaSurfaceDark = Color(0xFF1E293B)
val PharmaOnSurfaceDark = Color(0xFFF8FAFC)
val PharmaSurfaceVariantDark = Color(0xFF334155)
val PharmaOnSurfaceVariantDark = Color(0xFF94A3B8)
val PharmaOutlineDark = Color(0xFF475569)
