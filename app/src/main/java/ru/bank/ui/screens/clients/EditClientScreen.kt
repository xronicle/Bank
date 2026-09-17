package ru.bank.ui.screens.clients

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.bank.ui.components.SectionLabel
import ru.bank.ui.repositoryViewModel

@Composable
fun EditClientScreen(clientId: Long, onBack: () -> Unit, onSaved: () -> Unit) {
    val viewModel: EditClientViewModel = repositoryViewModel()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(clientId) { viewModel.load(clientId) }
    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Назад")
            }
            Spacer(Modifier.width(4.dp))
            Text("Редактировать клиента", style = MaterialTheme.typography.headlineSmall)
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            SectionLabel("Данные клиента")
            OutlinedTextField(
                value = state.fullName,
                onValueChange = viewModel::onFullNameChange,
                label = { Text("ФИО") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.passportData,
                onValueChange = viewModel::onPassportChange,
                label = { Text("Паспортные данные") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.phoneNumber,
                onValueChange = viewModel::onPhoneChange,
                label = { Text("Телефон") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            state.error?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(20.dp))
            Button(onClick = viewModel::save, modifier = Modifier.fillMaxWidth()) {
                Text("Сохранить изменения")
            }
        }
    }
}
