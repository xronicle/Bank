package ru.bank.ui.screens.clients

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.bank.data.entity.CurrencyEntity
import ru.bank.ui.components.SectionLabel
import ru.bank.ui.repositoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountScreen(clientId: Long, onBack: () -> Unit, onSaved: () -> Unit) {
    val viewModel: AddAccountViewModel = repositoryViewModel()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Назад")
            }
            Spacer(Modifier.width(4.dp))
            Text("Новый счёт", style = MaterialTheme.typography.headlineSmall)
        }

        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            SectionLabel("Параметры счёта")

            var currencyExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = currencyExpanded, onExpandedChange = { currencyExpanded = it }) {
                OutlinedTextField(
                    value = state.selectedCurrency?.let { "${it.code} · ${it.name}" } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Валюта") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = currencyExpanded, onDismissRequest = { currencyExpanded = false }) {
                    state.currencies.forEach { currency: CurrencyEntity ->
                        DropdownMenuItem(
                            text = { Text("${currency.code} · ${currency.name}") },
                            onClick = { viewModel.onCurrencySelected(currency); currencyExpanded = false }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            var typeExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
                OutlinedTextField(
                    value = state.selectedAccountType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Вид счёта") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                    ACCOUNT_TYPES.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = { viewModel.onAccountTypeSelected(type); typeExpanded = false }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.initialBalance,
                onValueChange = viewModel::onBalanceChange,
                label = { Text("Начальный баланс") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            state.error?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(20.dp))
            Button(onClick = { viewModel.save(clientId) }, modifier = Modifier.fillMaxWidth()) {
                Text("Создать")
            }
        }
    }
}
