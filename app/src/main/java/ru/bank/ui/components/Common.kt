package ru.bank.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.bank.ui.theme.AvatarPalette
import ru.bank.ui.theme.InkSecondary
import ru.bank.ui.theme.Negative
import ru.bank.ui.theme.Positive
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = InkSecondary,
        modifier = modifier.padding(top = 24.dp, bottom = 10.dp)
    )
}

@Composable
fun StatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        tonalElevation = 1.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(
                Modifier
                    .width(22.dp)
                    .height(3.dp)
                    .background(accentColor, RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.height(10.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = InkSecondary)
        }
    }
}

@Composable
fun AvatarInitials(name: String, size: androidx.compose.ui.unit.Dp = 42.dp, modifier: Modifier = Modifier) {
    val initials = name.trim().split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
    val color = AvatarPalette[abs(name.hashCode()) % AvatarPalette.size]
    Box(
        modifier
            .size(size)
            .background(color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            initials,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LongPressRow(onClick: () -> Unit, onLongPress: () -> Unit, content: @Composable () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Box(
        Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onLongPress()
            }
        )
    ) {
        content()
    }
}

fun formatMoney(amount: Double, currencyCode: String): String {
    val symbol = when (currencyCode) {
        "RUB" -> "₽"
        "USD" -> "$"
        "EUR" -> "€"
        "CNY" -> "¥"
        "GBP" -> "£"
        "KZT" -> "₸"
        "JPY" -> "¥"
        else -> currencyCode
    }
    val fmt = NumberFormat.getNumberInstance(Locale("ru", "RU"))
    fmt.maximumFractionDigits = 2
    fmt.minimumFractionDigits = 0
    return "${fmt.format(amount)} $symbol"
}

fun amountColor(isIncome: Boolean) = if (isIncome) Positive else Negative
fun amountSign(isIncome: Boolean) = if (isIncome) "+" else "−"

@Composable
fun ThinDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
}
