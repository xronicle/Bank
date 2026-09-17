package ru.bank.ui.screens.clients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.bank.data.BankRepository
import ru.bank.data.OperationRejectedException
import ru.bank.data.entity.OperationType

data class AddOperationUiState(
    val type: OperationType = OperationType.INCOME,
    val amountText: String = "",
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)

class AddOperationViewModel(private val repository: BankRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AddOperationUiState())
    val uiState: StateFlow<AddOperationUiState> = _uiState.asStateFlow()

    fun onTypeChange(type: OperationType) = _uiState.update { it.copy(type = type, error = null) }
    fun onAmountChange(value: String) = _uiState.update { it.copy(amountText = value, error = null) }

    fun save(accountNumber: String) {
        val state = _uiState.value
        val amount = state.amountText.replace(",", ".").toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(error = "Введите сумму больше нуля") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                repository.addOperation(accountNumber, state.type, amount)
                _uiState.update { it.copy(isSaving = false, saved = true) }
            } catch (e: OperationRejectedException) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = "Не удалось провести операцию: ${e.localizedMessage ?: "неизвестная ошибка"}") }
            }
        }
    }
}
