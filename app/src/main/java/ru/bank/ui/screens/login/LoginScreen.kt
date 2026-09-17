package ru.bank.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ru.bank.data.entity.BranchEntity
import ru.bank.data.entity.EmployeeEntity
import ru.bank.ui.repositoryViewModel
import ru.bank.ui.theme.Accent
import ru.bank.ui.theme.AccentSoft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val viewModel: LoginViewModel = repositoryViewModel()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.loggedIn) {
        if (state.loggedIn) onLoginSuccess()
    }

    Column(
        Modifier.fillMaxSize().padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(72.dp))
        Box(
            Modifier.size(84.dp).background(AccentSoft, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Person, contentDescription = null, tint = Accent, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text("Банк", style = MaterialTheme.typography.displaySmall)
        Text(
            "Вход для сотрудников отделения",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(36.dp))
        LoginDropdown(
            label = "Отделение",
            items = state.branches,
            selected = state.selectedBranch,
            itemLabel = { "№${it.branchNumber} · ${it.address}" },
            onSelect = viewModel::onBranchSelected
        )
        if (state.branches.isEmpty()) {
            Spacer(Modifier.height(6.dp))
            Text(
                "Список отделений пуст. Переустановите приложение (полностью удалите старую версию перед установкой новой) — это заново заполнит базу тестовыми данными.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
        Spacer(Modifier.height(14.dp))
        LoginDropdown(
            label = "Сотрудник",
            items = state.employees,
            selected = state.selectedEmployee,
            itemLabel = { it.fullName },
            enabled = state.selectedBranch != null,
            onSelect = viewModel::onEmployeeSelected
        )
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text("Пароль") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        state.error?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(22.dp))
        Button(onClick = viewModel::login, modifier = Modifier.fillMaxWidth().height(50.dp)) {
            Text("Вход")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> LoginDropdown(
    label: String,
    items: List<T>,
    selected: T?,
    itemLabel: (T) -> String,
    enabled: Boolean = true,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = it }
    ) {
        OutlinedTextField(
            value = selected?.let(itemLabel) ?: "",
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded && enabled, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(text = { Text(itemLabel(item)) }, onClick = { onSelect(item); expanded = false })
            }
        }
    }
}
