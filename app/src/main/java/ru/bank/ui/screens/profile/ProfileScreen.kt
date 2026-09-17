package ru.bank.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.bank.data.BranchFundsGroup
import ru.bank.data.CurrencyFundsRow
import ru.bank.ui.components.SectionLabel
import ru.bank.ui.components.ThinDivider
import ru.bank.ui.components.amountColor
import ru.bank.ui.components.amountSign
import ru.bank.ui.components.formatMoney
import ru.bank.ui.repositoryViewModel
import ru.bank.ui.theme.Accent
import ru.bank.ui.theme.AccentSoft
import ru.bank.ui.theme.Amber

@Composable
fun ProfileScreen(onLoggedOut: () -> Unit) {
    val viewModel: ProfileViewModel = repositoryViewModel()
    val state by viewModel.uiState.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(28.dp))
        Box(
            Modifier.size(72.dp).background(AccentSoft, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Person, contentDescription = null, tint = Accent, modifier = Modifier.size(34.dp))
        }
        Spacer(Modifier.height(14.dp))
        Text(state.employee?.fullName ?: "—", style = MaterialTheme.typography.displaySmall)
        Text(
            state.employee?.position ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SectionLabel("Показатели")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ru.bank.ui.components.StatTile(
                value = state.clientsCount.toString(),
                label = "Клиентов в базе",
                modifier = Modifier.weight(1f),
                accentColor = Accent
            )
            ru.bank.ui.components.StatTile(
                value = state.accountsCount.toString(),
                label = "Открытых счетов",
                modifier = Modifier.weight(1f),
                accentColor = Amber
            )
        }

        SectionLabel("Отделение")
        InfoRow("Номер отделения", state.branch?.branchNumber?.toString() ?: "—")
        ThinDivider()
        InfoRow("Адрес", state.branch?.address ?: "—")

        SectionLabel("О денежных средствах")
        if (state.reportYears.isEmpty()) {
            Text("Недостаточно данных для отчёта.", style = MaterialTheme.typography.bodySmall)
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.reportYears.forEach { year ->
                    YearChip(
                        year = year,
                        selected = year == state.selectedYear,
                        onClick = { viewModel.selectYear(year) }
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            if (state.reportGroups.isEmpty() && !state.isReportLoading) {
                Text(
                    "За ${state.selectedYear} год данных нет.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 12.dp)
                )
            } else {
                state.reportGroups.forEach { group ->
                    BranchFundsSection(group)
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        OutlinedButton(
            onClick = { viewModel.logout(); onLoggedOut() },
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
            Text("Выйти")
        }
    }
}

@Composable
private fun YearChip(year: Int, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) Accent else MaterialTheme.colorScheme.surface
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        Modifier
            .background(bg, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(year.toString(), color = fg, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun BranchFundsSection(group: BranchFundsGroup) {
    Column(Modifier.fillMaxWidth().padding(top = 14.dp)) {
        Text("Отделение №${group.branchNumber}", style = MaterialTheme.typography.titleMedium)
        Text(group.branchAddress, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(6.dp))
        group.rows.forEach { row ->
            CurrencyFundsLine(row)
        }
    }
}

@Composable
private fun CurrencyFundsLine(row: CurrencyFundsRow) {
    Column(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Box(
            Modifier
                .background(AccentSoft, RoundedCornerShape(8.dp))
                .padding(horizontal = 9.dp, vertical = 5.dp)
        ) {
            Text(row.currencyCode, style = MaterialTheme.typography.labelSmall, color = Accent)
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MiniStat("Хранится", formatMoney(row.stored, row.currencyCode), modifier = Modifier.weight(1f))
            MiniStat(
                "Положено",
                "${amountSign(true)}${formatMoney(row.deposited, row.currencyCode)}",
                amountColor(true),
                modifier = Modifier.weight(1f)
            )
            MiniStat(
                "Снято",
                "${amountSign(false)}${formatMoney(row.withdrawn, row.currencyCode)}",
                amountColor(false),
                modifier = Modifier.weight(1f)
            )
        }
    }
    ThinDivider()
}

@Composable
private fun MiniStat(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = if (valueColor == androidx.compose.ui.graphics.Color.Unspecified) MaterialTheme.colorScheme.onSurface else valueColor
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}
