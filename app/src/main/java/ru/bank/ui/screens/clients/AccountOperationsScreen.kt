package ru.bank.ui.screens.clients

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import ru.bank.pdf.FileDownloader
import ru.bank.ui.components.OperationRow
import ru.bank.ui.components.SectionLabel
import ru.bank.ui.components.StatementActionSheet
import ru.bank.ui.components.ThinDivider
import ru.bank.ui.components.formatMoney
import ru.bank.ui.rememberRepository
import ru.bank.ui.rememberSessionManager
import ru.bank.ui.repositoryViewModel
import java.io.File

@Composable
fun AccountOperationsScreen(accountNumber: String, onBack: () -> Unit) {
    val viewModel: AccountOperationsViewModel = repositoryViewModel()
    val repository = rememberRepository()
    val session = rememberSessionManager()
    val context = LocalContext.current

    LaunchedEffect(accountNumber) { viewModel.load(accountNumber) }
    val state by viewModel.uiState.collectAsState()

    var branchName by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val employee = repository.getEmployee(session.employeeId)
        val branch = employee?.let { repository.getBranch(it.branchNumber) }
        branchName = branch?.let { "Отделение №${it.branchNumber} · ${it.address}" } ?: "Банк"
    }

    val account = state.account
    var showAddOperation by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Назад")
            }
            Spacer(Modifier.width(4.dp))
            Column {
                Text(
                    account?.let { "${it.currencyCode} · ${it.accountNumber.take(4)}****" } ?: "…",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    account?.let { formatMoney(it.balance, it.currencyCode) } ?: "",
                    style = MaterialTheme.typography.bodySmall
                )
                if (account != null) {
                    Text(
                        "${account.accountType} · договор ${account.contractNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            SectionLabel("Операции по счёту")
            if (state.operations.isEmpty()) {
                Text("По счёту пока нет операций.", style = MaterialTheme.typography.bodySmall)
            } else {
                LazyColumn(Modifier.weight(1f)) {
                    items(state.operations, key = { it.id }) { op ->
                        OperationRow(op, currencyCode = account?.currencyCode ?: "RUB")
                        ThinDivider()
                    }
                }
            }

            OutlinedButton(
                onClick = { showAddOperation = true },
                enabled = account != null,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Новая операция")
            }
            Button(
                onClick = { viewModel.generateStatement(context, branchName) },
                enabled = !state.isGenerating,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 16.dp)
            ) {
                Text(if (state.isGenerating) "Формируется…" else "Сделать выписку по счёту")
            }
        }
    }

    if (showAddOperation && account != null) {
        AddOperationDialog(
            accountNumber = account.accountNumber,
            currencyCode = account.currencyCode,
            onDismiss = { showAddOperation = false },
            onSaved = { showAddOperation = false }
        )
    }

    if (state.showResultDialog && state.generatedFile != null) {
        val file = state.generatedFile!!

        fun shareIntentFor(mimeType: String): Intent {
            val uri = FileProvider.getUriForFile(context, "ru.bank.fileprovider", file)
            return Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Выписка по счёту")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }

        StatementActionSheet(
            fileName = file.name,
            onDismiss = viewModel::dismissResultDialog,
            onDownload = {
                val saved = FileDownloader.saveToDownloads(context, file)
                val message = if (saved) "Файл сохранён в «Загрузки»" else "Сохранено во внутреннем хранилище: ${file.absolutePath}"
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                viewModel.dismissResultDialog()
            },
            onEmail = {
                context.startActivity(
                    Intent.createChooser(shareIntentFor("message/rfc822"), "Отправить по почте")
                )
                viewModel.dismissResultDialog()
            },
            onMessenger = {
                context.startActivity(
                    Intent.createChooser(shareIntentFor("application/pdf"), "Отправить в мессенджер")
                )
                viewModel.dismissResultDialog()
            },
            onMoreOptions = {
                context.startActivity(
                    Intent.createChooser(shareIntentFor("application/pdf"), "Отправить выписку")
                )
                viewModel.dismissResultDialog()
            }
        )
    }
}
