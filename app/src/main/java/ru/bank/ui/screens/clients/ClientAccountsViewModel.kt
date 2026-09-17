package ru.bank.ui.screens.clients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.bank.data.BankRepository
import ru.bank.data.entity.AccountEntity
import ru.bank.data.entity.ClientEntity

data class ClientAccountsUiState(
    val client: ClientEntity? = null,
    val accounts: List<AccountEntity> = emptyList()
)

class ClientAccountsViewModel(private val repository: BankRepository) : ViewModel() {
    private val clientId = MutableStateFlow<Long?>(null)

    fun load(id: Long) {
        clientId.value = id
        viewModelScope.launch {
            val client = repository.getClient(id)
            _uiState.update { it.copy(client = client) }
        }
    }

    private val _uiState = MutableStateFlow(ClientAccountsUiState())
    val uiState: StateFlow<ClientAccountsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            clientId.filterNotNull().flatMapLatest { repository.observeAccounts(it) }
                .collect { accounts -> _uiState.update { it.copy(accounts = accounts) } }
        }
    }

    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch { repository.deleteAccount(account) }
    }
}
