package ru.bank.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.bank.data.BankRepository
import ru.bank.data.BranchFundsGroup
import ru.bank.data.entity.BranchEntity
import ru.bank.data.entity.EmployeeEntity
import ru.bank.data.session.SessionManager

data class ProfileUiState(
    val employee: EmployeeEntity? = null,
    val branch: BranchEntity? = null,
    val clientsCount: Int = 0,
    val accountsCount: Int = 0,
    val reportYears: List<Int> = emptyList(),
    val selectedYear: Int? = null,
    val reportGroups: List<BranchFundsGroup> = emptyList(),
    val isReportLoading: Boolean = true
)

class ProfileViewModel(
    private val repository: BankRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val selectedYear = MutableStateFlow<Int?>(null)

    init {
        viewModelScope.launch {
            val employee = repository.getEmployee(sessionManager.employeeId)
            val branch = employee?.let { repository.getBranch(it.branchNumber) }
            _uiState.update { it.copy(employee = employee, branch = branch) }
        }
        viewModelScope.launch {
            combine(
                repository.observeClientsCount(),
                repository.observeOpenAccountsCount()
            ) { clients, accounts -> clients to accounts }
                .collect { (clients, accounts) ->
                    _uiState.update { it.copy(clientsCount = clients, accountsCount = accounts) }
                }
        }

        viewModelScope.launch {
            repository.observeAvailableReportYears().collect { years ->
                val defaultYear = years.firstOrNull() ?: java.time.LocalDate.now().year
                _uiState.update { it.copy(reportYears = years.ifEmpty { listOf(defaultYear) }) }
                if (selectedYear.value == null || (years.isNotEmpty() && selectedYear.value !in years)) {
                    selectedYear.value = defaultYear
                }
            }
        }
        viewModelScope.launch {
            selectedYear
                .filterNotNull()
                .onEach { year -> _uiState.update { it.copy(selectedYear = year, isReportLoading = true) } }
                .flatMapLatest { year -> repository.observeYearlyFundsReport(year) }
                .collect { groups ->
                    _uiState.update { it.copy(reportGroups = groups, isReportLoading = false) }
                }
        }
    }

    fun selectYear(year: Int) {
        selectedYear.value = year
    }

    fun logout() {
        sessionManager.logout()
    }
}
