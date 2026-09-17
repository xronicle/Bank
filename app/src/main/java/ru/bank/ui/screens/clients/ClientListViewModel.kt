package ru.bank.ui.screens.clients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.bank.data.BankRepository
import ru.bank.data.entity.ClientEntity

class ClientListViewModel(private val repository: BankRepository) : ViewModel() {
    val clients: StateFlow<List<ClientEntity>> = repository.observeClients()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteClient(client: ClientEntity) {
        viewModelScope.launch { repository.deleteClient(client) }
    }
}
