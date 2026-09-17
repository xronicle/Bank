package ru.bank.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.bank.data.BankRepository
import ru.bank.data.entity.BranchEntity
import ru.bank.data.entity.EmployeeEntity
import ru.bank.data.session.SessionManager

data class LoginUiState(
    val branches: List<BranchEntity> = emptyList(),
    val employees: List<EmployeeEntity> = emptyList(),
    val selectedBranch: BranchEntity? = null,
    val selectedEmployee: EmployeeEntity? = null,
    val password: String = "",
    val error: String? = null,
    val loggedIn: Boolean = false
)

class LoginViewModel(
    private val repository: BankRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeBranches().collect { branches ->
                _uiState.update { it.copy(branches = branches) }
            }
        }
    }

    fun onBranchSelected(branch: BranchEntity) {
        _uiState.update { it.copy(selectedBranch = branch, selectedEmployee = null, error = null) }
        viewModelScope.launch {
            repository.observeEmployeesByBranch(branch.branchNumber).collect { employees ->
                _uiState.update { it.copy(employees = employees) }
            }
        }
    }

    fun onEmployeeSelected(employee: EmployeeEntity) {
        _uiState.update { it.copy(selectedEmployee = employee, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun login() {
        val state = _uiState.value
        val employee = state.selectedEmployee
        when {
            state.selectedBranch == null -> _uiState.update { it.copy(error = "Выберите отделение") }
            employee == null -> _uiState.update { it.copy(error = "Выберите сотрудника") }
            employee.password != state.password -> _uiState.update { it.copy(error = "Неверный пароль") }
            else -> {
                sessionManager.employeeId = employee.id
                _uiState.update { it.copy(loggedIn = true) }
            }
        }
    }
}
