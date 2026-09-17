package ru.bank.ui.screens.clients

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.bank.data.entity.AccountEntity
import ru.bank.ui.components.LongPressRow
import ru.bank.ui.components.SectionLabel
import ru.bank.ui.components.ThinDivider
import ru.bank.ui.components.formatMoney
import ru.bank.ui.repositoryViewModel
import ru.bank.ui.theme.Amber
import ru.bank.ui.theme.AmberSoft

@Composable
fun ClientAccountsScreen(
    clientId: Long,
    onBack: () -> Unit,
    onOpenAccount: (String) -> Unit,
    onAddAccount: () -> Unit,
    onEditClient: () -> Unit
) {
    val viewModel: ClientAccountsViewModel = repositoryViewModel()
    LaunchedEffect(clientId) { viewModel.load(clientId) }
    val state by viewModel.uiState.collectAsState()
    var accountToDelete by remember { mutableStateOf<AccountEntity?>(null) }

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(onClick = onAddAccount) {
                Icon(Icons.Filled.Add, contentDescription = "Открыть счёт")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Назад")
                }
                Spacer(Modifier.width(4.dp))
                Column(Modifier.weight(1f)) {
                    Text(state.client?.fullName ?: "…", style = MaterialTheme.typography.headlineSmall)
                    Text(state.client?.passportData ?: "", style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = onEditClient) {
                    Icon(Icons.Outlined.EditNote, contentDescription = "Редактировать клиента")
                }
            }

            Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
                SectionLabel("Счета клиента · долгое нажатие — удалить")
                if (state.accounts.isEmpty()) {
                    Text("У клиента пока нет открытых счетов.", style = MaterialTheme.typography.bodySmall)
                } else {
                    LazyColumn {
                        items(state.accounts, key = { it.accountNumber }) { account ->
                            LongPressRow(
                                onClick = { onOpenAccount(account.accountNumber) },
                                onLongPress = { accountToDelete = account }
                            ) {
                                AccountRow(account)
                            }
                            ThinDivider()
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }

    accountToDelete?.let { account ->
        AlertDialog(
            onDismissRequest = { accountToDelete = null },
            title = { Text("Удалить счёт?") },
            text = {
                Text("Счёт ${account.currencyCode} ${maskAccountNumber(account.accountNumber)} и все операции по нему будут удалены безвозвратно.")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAccount(account)
                    accountToDelete = null
                }) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { accountToDelete = null }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun AccountRow(account: AccountEntity) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            Modifier
                .background(AmberSoft, RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                account.currencyCode,
                style = MaterialTheme.typography.labelSmall,
                color = Amber
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(maskAccountNumber(account.accountNumber), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(2.dp))
            Text(
                "${account.accountType} · ${formatMoney(account.balance, account.currencyCode)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun maskAccountNumber(number: String): String {
    if (number.length <= 4) return number
    return number.take(4) + "****"
}
