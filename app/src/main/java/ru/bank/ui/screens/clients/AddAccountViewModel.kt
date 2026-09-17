package ru.bank.ui.screens.clients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.bank.data.BankRepository
import ru.bank.data.entity.AccountEntity
import ru.bank.data.entity.CurrencyEntity
import ru.bank.data.session.SessionManager

val ACCOUNT_TYPES = listOf("Текущий", "Накопительный", "Депозитный")

data class AddAccountUiState(
    val currencies: List<CurrencyEntity> = emptyList(),
    val selectedCurrency: CurrencyEntity? = null,
    val selectedAccountType: String = ACCOUNT_TYPES.first(),
    val initialBalance: String = "",
    val saved: Boolean = false,
    val error: String? = null
)

class AddAccountViewModel(
    private val repository: BankRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddAccountUiState())
    val uiState: StateFlow<AddAccountUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeCurrencies().collect { list ->
                _uiState.update { it.copy(currencies = list, selectedCurrency = it.selectedCurrency ?: list.firstOrNull()) }
            }
        }
    }

    fun onCurrencySelected(currency: CurrencyEntity) = _uiState.update { it.copy(selectedCurrency = currency, error = null) }
    fun onAccountTypeSelected(type: String) = _uiState.update { it.copy(selectedAccountType = type, error = null) }
    fun onBalanceChange(value: String) = _uiState.update { it.copy(initialBalance = value, error = null) }

    fun save(clientId: Long) {
        val state = _uiState.value
        val currency = state.selectedCurrency
        val balance = state.initialBalance.replace(",", ".").toDoubleOrNull()
        if (currency == null) {
            _uiState.update { it.copy(error = "Выберите валюту") }
            return
        }
        if (balance == null || balance < 0) {
            _uiState.update { it.copy(error = "Введите корректный начальный баланс") }
            return
        }
        viewModelScope.launch {

            val employee = repository.getEmployee(sessionManager.employeeId)
            if (employee == null) {
                _uiState.update { it.copy(error = "Не удалось определить сотрудника сессии") }
                return@launch
            }
            repository.addAccount(
                AccountEntity(
                    accountNumber = repository.generateAccountNumber(),
                    clientId = clientId,
                    employeeId = employee.id,
                    branchNumber = employee.branchNumber,
                    currencyCode = currency.code,
                    accountType = state.selectedAccountType,
                    contractNumber = generateContractNumber(),
                    openedAt = System.currentTimeMillis(),
                    balance = balance
                )
            )
            _uiState.update { it.copy(saved = true) }
        }
    }

    private fun generateContractNumber(): String {
        val year = java.time.LocalDate.now().year
        return "Д-$year-" + (10000..99999).random()
    }
}
