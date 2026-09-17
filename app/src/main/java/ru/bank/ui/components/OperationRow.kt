package ru.bank.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.bank.data.entity.OperationEntity
import ru.bank.data.entity.OperationType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val timeFormat = SimpleDateFormat("d MMM, HH:mm", Locale("ru"))

@Composable
fun OperationRow(operation: OperationEntity, currencyCode: String, modifier: Modifier = Modifier) {
    val isIncome = operation.type == OperationType.INCOME
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                if (isIncome) "Пополнение" else "Списание",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(2.dp))
            Text(timeFormat.format(Date(operation.date)), style = MaterialTheme.typography.bodySmall)
        }
        Text(
            "${amountSign(isIncome)}${formatMoney(operation.amount, currencyCode)}",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = amountColor(isIncome)
        )
    }
}
