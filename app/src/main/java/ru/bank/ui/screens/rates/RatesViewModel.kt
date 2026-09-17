package ru.bank.ui.screens.rates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import ru.bank.data.BankRepository
import ru.bank.data.entity.CurrencyEntity
import ru.bank.data.entity.CurrencyHistoryEntity
import java.util.concurrent.TimeUnit

enum class RatePeriod(val days: Long, val label: String) {
    WEEK(7, "1W"),
    MONTH(30, "1M")
}

class RatesViewModel(private val repository: BankRepository) : ViewModel() {

    private val selectedCode = MutableStateFlow<String?>(null)
    private val selectedPeriod = MutableStateFlow(RatePeriod.WEEK)

    val currencies: StateFlow<List<CurrencyEntity>> = repository.observeCurrencies()
        .onEach { list -> if (selectedCode.value == null) list.firstOrNull { it.code != "RUB" }?.let { selectedCode.value = it.code } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedCodeState: StateFlow<String?> = selectedCode
    val selectedPeriodState: StateFlow<RatePeriod> = selectedPeriod

    fun selectCurrency(code: String) {
        selectedCode.value = code
    }

    fun selectPeriod(period: RatePeriod) {
        selectedPeriod.value = period
    }

    val history: StateFlow<List<CurrencyHistoryEntity>> = combine(selectedCode, selectedPeriod) { code, period -> code to period }
        .filter { it.first != null }
        .flatMapLatest { (code, period) ->
            val since = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(period.days)
            repository.observeCurrencyHistory(code!!, since)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latest: StateFlow<CurrencyHistoryEntity?> = selectedCode
        .filterNotNull()
        .flatMapLatest { repository.observeLatestRate(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allLatestRates: StateFlow<List<CurrencyHistoryEntity>> = repository.observeAllLatestRates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
