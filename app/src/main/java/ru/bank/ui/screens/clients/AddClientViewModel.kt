package ru.bank.ui.screens.clients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.bank.data.BankRepository
import ru.bank.data.entity.ClientEntity

data class AddClientUiState(
    val fullName: String = "",
    val passportData: String = "",
    val phoneNumber: String = "",
    val saved: Boolean = false,
    val error: String? = null
)

class AddClientViewModel(private val repository: BankRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AddClientUiState())
    val uiState: StateFlow<AddClientUiState> = _uiState.asStateFlow()

    fun onFullNameChange(v: String) = _uiState.update { it.copy(fullName = v, error = null) }
    fun onPassportChange(v: String) = _uiState.update { it.copy(passportData = v, error = null) }
    fun onPhoneChange(v: String) = _uiState.update { it.copy(phoneNumber = v, error = null) }

    fun save() {
        val state = _uiState.value

        ClientValidation.validateFullName(state.fullName)?.let { _uiState.update { s -> s.copy(error = it) }; return }
        ClientValidation.validatePassport(state.passportData)?.let { _uiState.update { s -> s.copy(error = it) }; return }
        ClientValidation.validatePhone(state.phoneNumber)?.let { _uiState.update { s -> s.copy(error = it) }; return }

        viewModelScope.launch {
            val existing = repository.findClientByPassport(state.passportData.trim())
            if (existing != null) {
                _uiState.update { it.copy(error = "Клиент с таким паспортом уже зарегистрирован: ${existing.fullName}") }
                return@launch
            }
            try {
                repository.addClient(
                    ClientEntity(
                        fullName = state.fullName.trim(),
                        passportData = state.passportData.trim(),
                        phoneNumber = state.phoneNumber.trim()
                    )
                )
                _uiState.update { it.copy(saved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Не удалось сохранить клиента: ${e.localizedMessage ?: "неизвестная ошибка"}") }
            }
        }
    }
}
