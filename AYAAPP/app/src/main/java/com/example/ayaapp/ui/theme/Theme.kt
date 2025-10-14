package com.example.ayaapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF22C55E),
    secondary = Color(0xFF0EA5E9),
    background = Color(0xFF0F1115),
    surface = Color(0xFF101418),
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
)

@Composable
fun AyaTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}