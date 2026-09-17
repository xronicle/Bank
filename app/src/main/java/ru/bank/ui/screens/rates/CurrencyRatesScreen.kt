package ru.bank.ui.screens.rates

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.bank.data.entity.CurrencyEntity
import ru.bank.data.entity.CurrencyHistoryEntity
import ru.bank.ui.components.SectionLabel
import ru.bank.ui.components.ThinDivider
import ru.bank.ui.repositoryViewModel
import ru.bank.ui.theme.Accent
import ru.bank.ui.theme.AccentSoft
import ru.bank.ui.theme.Amber
import ru.bank.ui.theme.AmberSoft
import ru.bank.ui.theme.Negative
import ru.bank.ui.theme.Positive


@Composable
fun CurrencyRatesScreen() {
    val viewModel: RatesViewModel = repositoryViewModel()
    val currencies by viewModel.currencies.collectAsState()
    val selectedCode by viewModel.selectedCodeState.collectAsState()
    val selectedPeriod by viewModel.selectedPeriodState.collectAsState()
    val history by viewModel.history.collectAsState()
    val latest by viewModel.latest.collectAsState()
    val allLatest by viewModel.allLatestRates.collectAsState()

    val selectedCurrency = currencies.find { it.code == selectedCode }
    val nonRubCurrencies = currencies.filter { it.code != "RUB" }
    val latestByCode = allLatest.associateBy { it.currencyCode }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()


    LaunchedEffect(selectedCode) {
        coroutineScope.launch { scrollState.animateScrollTo(0) }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(20.dp))
        Text("Курсы валют", style = MaterialTheme.typography.displaySmall)
        Text(
            "Первичные данные из истории курсов отделения",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(20.dp))
        Text(
            "Сегодня = ${formatRate(latest?.rate)}",
            style = MaterialTheme.typography.displaySmall
        )
        Text(
            selectedCurrency?.let { "${it.name} · ${it.issuerCountry}" } ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(14.dp))
        CurrencyStatsRow(currency = selectedCurrency, history = history)

        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RatePeriod.entries.forEach { period ->
                PeriodChip(
                    label = period.label,
                    selected = period == selectedPeriod,
                    onClick = { viewModel.selectPeriod(period) }
                )
            }
        }

        RatesChartWebView(
            history = history,
            accentColorHex = "#3E9A64",
            modifier = Modifier.padding(top = 16.dp)
        )

        SectionLabel("Все валюты отделения · нажмите, чтобы выбрать")
        nonRubCurrencies.forEach { currency ->
            CurrencyOverviewRow(
                currency = currency,
                rate = latestByCode[currency.code]?.rate,
                selected = currency.code == selectedCode,
                onClick = { viewModel.selectCurrency(currency.code) }
            )
            ThinDivider()
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CurrencyStatsRow(currency: CurrencyEntity?, history: List<CurrencyHistoryEntity>) {
    if (currency == null || history.size < 2) return

    val first = history.first().rate
    val last = history.last().rate
    val changePercent = if (first != 0.0) (last - first) / first * 100 else 0.0
    val min = history.minOf { it.rate }
    val max = history.maxOf { it.rate }
    val isUp = changePercent >= 0

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatChip(
            label = "За период",
            value = "${if (isUp) "▲" else "▼"} ${"%.2f".format(kotlin.math.abs(changePercent))}%",
            valueColor = if (isUp) Positive else Negative
        )
        StatChip(label = "Мин.", value = "%.2f ₽".format(min))
        StatChip(label = "Макс.", value = "%.2f ₽".format(max))
        if (currency.unit > 1) {
            StatChip(label = "Котировка", value = "за ${currency.unit}")
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, valueColor: Color = Color.Unspecified) {
    Column(
        Modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = if (valueColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else valueColor
        )
    }
}

@Composable
private fun CurrencyOverviewRow(currency: CurrencyEntity, rate: Double?, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .background(if (selected) Accent else AmberSoft, RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                currency.code,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else Amber
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(currency.name, style = MaterialTheme.typography.titleMedium)
            Text(
                "${currency.issuerCountry}${if (currency.unit > 1) " · за ${currency.unit}" else ""}",
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            formatRate(rate),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = if (selected) Accent else MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatRate(rate: Double?): String {
    if (rate == null) return "—"
    return "%.2f ₽".format(rate)
}

@Composable
private fun PeriodChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) Accent else MaterialTheme.colorScheme.surface
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        Modifier
            .background(bg, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = fg, style = MaterialTheme.typography.bodySmall)
    }
}
