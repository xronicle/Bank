package ru.bank.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightScheme = lightColorScheme(
    primary = Accent,
    onPrimary = AccentOnAccent,
    primaryContainer = AccentSoft,
    onPrimaryContainer = Accent,
    background = Background,
    onBackground = InkText,
    surface = Surface,
    onSurface = InkText,
    surfaceVariant = Surface,
    onSurfaceVariant = InkSecondary,
    outline = OutlineSoft,
    error = Negative,
    errorContainer = NegativeSoft
)

@Composable
fun BankTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightScheme, typography = BankType, content = content)
}
