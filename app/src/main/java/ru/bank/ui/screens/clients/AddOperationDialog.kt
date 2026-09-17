package ru.bank.ui.screens.clients

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.bank.data.entity.OperationType
import ru.bank.ui.repositoryViewModel
import ru.bank.ui.theme.Accent


@Composable
fun AddOperationDialog(
    accountNumber: String,
    currencyCode: String,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val viewModel: AddOperationViewModel = repositoryViewModel()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая операция") },
        text = {
            Column {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                        .padding(4.dp)
                ) {
                    TypeSegment(
                        label = "Пополнение",
                        selected = state.type == OperationType.INCOME,
                        onClick = { viewModel.onTypeChange(OperationType.INCOME) },
                        modifier = Modifier.weight(1f)
                    )
                    TypeSegment(
                        label = "Списание",
                        selected = state.type == OperationType.EXPENSE,
                        onClick = { viewModel.onTypeChange(OperationType.EXPENSE) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = state.amountText,
                    onValueChange = viewModel::onAmountChange,
                    label = { Text("Сумма, $currencyCode") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                state.error?.let {
                    Spacer(Modifier.height(10.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !state.isSaving,
                onClick = { viewModel.save(accountNumber) }
            ) {
                Text(if (state.isSaving) "Проведение…" else "Провести")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@Composable
private fun TypeSegment(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bg = if (selected) Accent else androidx.compose.ui.graphics.Color.Transparent
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier
            .background(bg, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = fg, style = MaterialTheme.typography.bodySmall)
    }
}
