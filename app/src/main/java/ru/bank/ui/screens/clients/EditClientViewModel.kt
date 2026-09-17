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

data class EditClientUiState(
    val clientId: Long = 0,
    val fullName: String = "",
    val passportData: String = "",
    val phoneNumber: String = "",
    val isLoading: Boolean = true,
    val saved: Boolean = false,
    val error: String? = null
)

class EditClientViewModel(private val repository: BankRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(EditClientUiState())
    val uiState: StateFlow<EditClientUiState> = _uiState.asStateFlow()

    fun load(clientId: Long) {
        viewModelScope.launch {
            val client = repository.getClient(clientId)
            if (client == null) {
                _uiState.update { it.copy(isLoading = false, error = "Клиент не найден") }
                return@launch
            }
            _uiState.update {
                it.copy(
                    clientId = client.id,
                    fullName = client.fullName,
                    passportData = client.passportData,
                    phoneNumber = client.phoneNumber,
                    isLoading = false
                )
            }
        }
    }

    fun onFullNameChange(v: String) = _uiState.update { it.copy(fullName = v, error = null) }
    fun onPassportChange(v: String) = _uiState.update { it.copy(passportData = v, error = null) }
    fun onPhoneChange(v: String) = _uiState.update { it.copy(phoneNumber = v, error = null) }

    fun save() {
        val state = _uiState.value

        ClientValidation.validateFullName(state.fullName)?.let { _uiState.update { s -> s.copy(error = it) }; return }
        ClientValidation.validatePassport(state.passportData)?.let { _uiState.update { s -> s.copy(error = it) }; return }
        ClientValidation.validatePhone(state.phoneNumber)?.let { _uiState.update { s -> s.copy(error = it) }; return }

        viewModelScope.launch {
            // Уникальность паспорта — как и при добавлении, но с исключением
            // самого редактируемого клиента (иначе он «конфликтовал» бы сам с собой).
            val existing = repository.findClientByPassport(state.passportData.trim())
            if (existing != null && existing.id != state.clientId) {
                _uiState.update { it.copy(error = "Этот паспорт уже принадлежит клиенту: ${existing.fullName}") }
                return@launch
            }
            try {
                repository.updateClient(
                    ClientEntity(
                        id = state.clientId,
                        fullName = state.fullName.trim(),
                        passportData = state.passportData.trim(),
                        phoneNumber = state.phoneNumber.trim()
                    )
                )
                _uiState.update { it.copy(saved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Не удалось сохранить изменения: ${e.localizedMessage ?: "неизвестная ошибка"}") }
            }
        }
    }
}
