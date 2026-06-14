package com.example.hoctiengtrung2.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
val TimDam    = Color(0xFF26215C)
val TimTrung  = Color(0xFF534AB7)
val TimNhat   = Color(0xFF7F77DD)
val TimPastel = Color(0xFFEEEDFE)
val TimMo     = Color(0xFFCECBF6)
val NenTrang  = Color(0xFFFFFFFF)
val NenTim    = Color(0xFFEEEDFE)
val HongNhat  = Color(0xFFFBEAF0)
val HongDam   = Color(0xFF993556)

// Dark Mode Colors
val DarkBackground = Color(0xFF1A1C1E)
val DarkSurface = Color(0xFF24292E)
val DarkSurfaceVariant = Color(0xFF323940)
val DarkOnSurface = Color(0xFFE2E2E6)
val DarkOnSurfaceVariant = Color(0xFFC2C7CF)

fun mauTheoHSK(tenCapDo: String): Triple<Color, Color, String> = when {
    tenCapDo.contains("1") -> Triple(TimPastel, TimDam, "✨")
    tenCapDo.contains("2") -> Triple(TimPastel, TimTrung, "💫")
    tenCapDo.contains("3") -> Triple(TimMo, TimDam, "💎")
    tenCapDo.contains("4") -> Triple(TimPastel, TimTrung, "⭐")
    tenCapDo.contains("5") -> Triple(HongNhat, HongDam, "🔥")
    else -> Triple(HongNhat, HongDam, "👑")
}
