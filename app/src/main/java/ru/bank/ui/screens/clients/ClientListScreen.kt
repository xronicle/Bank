package ru.bank.ui.screens.clients

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.bank.data.entity.ClientEntity
import ru.bank.ui.components.AvatarInitials
import ru.bank.ui.components.LongPressRow
import ru.bank.ui.components.SectionLabel
import ru.bank.ui.components.ThinDivider
import ru.bank.ui.repositoryViewModel

@Composable
fun ClientListScreen(onOpenClient: (Long) -> Unit, onAddClient: () -> Unit) {
    val viewModel: ClientListViewModel = repositoryViewModel()
    val clients by viewModel.clients.collectAsState()
    var clientToDelete by remember { mutableStateOf<ClientEntity?>(null) }

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClient) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить клиента")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(20.dp))
            Text("Клиенты", style = MaterialTheme.typography.displaySmall)
            Text(
                "${clients.size} записей · долгое нажатие — удалить",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SectionLabel("Все клиенты")
            LazyColumn {
                items(clients, key = { it.id }) { client ->
                    LongPressRow(
                        onClick = { onOpenClient(client.id) },
                        onLongPress = { clientToDelete = client }
                    ) {
                        ClientRow(client)
                    }
                    ThinDivider()
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    clientToDelete?.let { client ->
        AlertDialog(
            onDismissRequest = { clientToDelete = null },
            title = { Text("Удалить клиента?") },
            text = {
                Text("«${client.fullName}» будет удалён вместе со всеми его счетами и операциями по ним. Действие необратимо.")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteClient(client)
                    clientToDelete = null
                }) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { clientToDelete = null }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun ClientRow(client: ClientEntity) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarInitials(client.fullName)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(client.fullName, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(2.dp))
            Text(client.passportData, style = MaterialTheme.typography.bodySmall)
        }
        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
