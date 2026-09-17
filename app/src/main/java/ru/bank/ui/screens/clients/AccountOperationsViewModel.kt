package ru.bank.ui.screens.clients

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.bank.data.BankRepository
import ru.bank.data.entity.AccountEntity
import ru.bank.data.entity.OperationEntity
import ru.bank.pdf.StatementPdfGenerator
import java.io.File

data class AccountOperationsUiState(
    val account: AccountEntity? = null,
    val operations: List<OperationEntity> = emptyList(),
    val generatedFile: File? = null,
    val isGenerating: Boolean = false,
    val showResultDialog: Boolean = false
)

class AccountOperationsViewModel(private val repository: BankRepository) : ViewModel() {

    private val accountNumber = MutableStateFlow<String?>(null)
    private val _uiState = MutableStateFlow(AccountOperationsUiState())
    val uiState: StateFlow<AccountOperationsUiState> = _uiState.asStateFlow()

    fun load(number: String) {
        accountNumber.value = number
        viewModelScope.launch {
            repository.observeAccount(number).collect { account ->
                _uiState.update { it.copy(account = account) }
            }
        }
        viewModelScope.launch {
            repository.observeOperations(number).collect { ops ->
                _uiState.update { it.copy(operations = ops) }
            }
        }
    }

    fun generateStatement(context: Context, branchName: String) {
        val account = _uiState.value.account ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true) }
            val client = repository.getClient(account.clientId)
            if (client == null) {
                _uiState.update { it.copy(isGenerating = false) }
                return@launch
            }
            val operations = _uiState.value.operations
            val file = StatementPdfGenerator(context).generate(
                client = client,
                account = account,
                branchName = branchName,
                operations = operations
            )
            _uiState.update { it.copy(generatedFile = file, isGenerating = false, showResultDialog = true) }
        }
    }

    fun dismissResultDialog() {
        _uiState.update { it.copy(showResultDialog = false) }
    }
}
