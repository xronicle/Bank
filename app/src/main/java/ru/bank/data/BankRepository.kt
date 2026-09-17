package ru.bank.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import ru.bank.data.entity.*
import java.time.Instant
import java.time.ZoneOffset


data class CurrencyFundsRow(
    val currencyCode: String,
    val stored: Double,
    val deposited: Double,
    val withdrawn: Double
)

data class BranchFundsGroup(
    val branchNumber: Long,
    val branchAddress: String,
    val rows: List<CurrencyFundsRow>
)

class OperationRejectedException(message: String) : Exception(message)

class BankRepository(private val db: AppDatabase) {

    fun observeBranches(): Flow<List<BranchEntity>> = db.branchDao().observeAll()
    suspend fun getBranch(branchNumber: Long): BranchEntity? = db.branchDao().getById(branchNumber)
    fun observeEmployeesByBranch(branchNumber: Long): Flow<List<EmployeeEntity>> = db.employeeDao().observeByBranch(branchNumber)
    suspend fun getEmployee(id: Long): EmployeeEntity? = db.employeeDao().getById(id)

    fun observeClients(): Flow<List<ClientEntity>> = db.clientDao().observeAll()
    fun observeClientsCount(): Flow<Int> = db.clientDao().observeCount()
    suspend fun getClient(id: Long): ClientEntity? = db.clientDao().getById(id)
    suspend fun addClient(client: ClientEntity): Long = db.clientDao().insert(client)
    suspend fun updateClient(client: ClientEntity) = db.clientDao().update(client)
    suspend fun deleteClient(client: ClientEntity) = db.clientDao().delete(client)

    suspend fun findClientByPassport(passport: String): ClientEntity? = db.clientDao().getByPassport(passport)

    fun observeCurrencies(): Flow<List<CurrencyEntity>> = db.currencyDao().observeAll()

    fun observeAccounts(clientId: Long): Flow<List<AccountEntity>> = db.accountDao().observeByClient(clientId)
    fun observeAccount(accountNumber: String): Flow<AccountEntity?> = db.accountDao().observeById(accountNumber)
    fun observeOpenAccountsCount(): Flow<Int> = db.accountDao().observeCount()
    suspend fun getAccount(accountNumber: String): AccountEntity? = db.accountDao().getById(accountNumber)
    suspend fun addAccount(account: AccountEntity) = db.accountDao().insert(account)
    suspend fun deleteAccount(account: AccountEntity) = db.accountDao().delete(account)

    fun observeOperations(accountNumber: String): Flow<List<OperationEntity>> = db.operationDao().observeByAccount(accountNumber)
    suspend fun getOperationsForPeriod(accountNumber: String, from: Long, to: Long) =
        db.operationDao().getForPeriod(accountNumber, from, to)

    suspend fun addOperation(accountNumber: String, type: OperationType, amount: Double) {
        require(amount > 0) { "Сумма операции должна быть положительной" }
        db.withTransaction {
            val account = db.accountDao().getById(accountNumber)
                ?: throw OperationRejectedException("Счёт не найден")
            val newBalance = when (type) {
                OperationType.INCOME -> account.balance + amount
                OperationType.EXPENSE -> account.balance - amount
            }
            if (newBalance < 0) {
                throw OperationRejectedException(
                    "Недостаточно средств: на счёте ${"%.2f".format(account.balance)} ${account.currencyCode}"
                )
            }
            db.operationDao().insert(
                OperationEntity(
                    accountNumber = accountNumber,
                    type = type,
                    amount = amount,
                    date = System.currentTimeMillis()
                )
            )
            db.accountDao().update(account.copy(balance = newBalance))
        }
    }

    fun observeCurrencyHistory(code: String, sinceMillis: Long): Flow<List<CurrencyHistoryEntity>> =
        db.currencyHistoryDao().observeSince(code, sinceMillis)
    fun observeLatestRate(code: String): Flow<CurrencyHistoryEntity?> = db.currencyHistoryDao().observeLatest(code)
    fun observeAllLatestRates(): Flow<List<CurrencyHistoryEntity>> = db.currencyHistoryDao().observeAllLatest()

    fun generateAccountNumber(): String = "40817810" + (100000000..999999999).random()


    fun observeAvailableReportYears(): Flow<List<Int>> =
        combine(db.accountDao().observeAllAccounts(), db.operationDao().observeAllOperations()) { accounts, operations ->
            val years = accounts.map { yearOf(it.openedAt) } + operations.map { yearOf(it.date) }
            years.toSortedSet(compareByDescending { it }).toList()
        }


    fun observeYearlyFundsReport(year: Int): Flow<List<BranchFundsGroup>> =
        combine(
            db.branchDao().observeAll(),
            db.accountDao().observeAllAccounts(),
            db.operationDao().observeAllOperations()
        ) { branches, accounts, operations ->
            computeYearlyFundsReport(year, branches, accounts, operations)
        }

    private fun computeYearlyFundsReport(
        year: Int,
        branches: List<BranchEntity>,
        accounts: List<AccountEntity>,
        operations: List<OperationEntity>
    ): List<BranchFundsGroup> {
        val yearEndExclusive = java.time.LocalDate.of(year + 1, 1, 1)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()

        val branchByNumber = branches.associateBy { it.branchNumber }
        val accountsByNumber = accounts.associateBy { it.accountNumber }
        val accountsOpenedByYear = accounts.filter { yearOf(it.openedAt) <= year }

        data class Key(val branchNumber: Long, val currencyCode: String)

        val storedAtYearEnd = mutableMapOf<Key, Double>()
        accountsOpenedByYear.forEach { acc ->
            val key = Key(acc.branchNumber, acc.currencyCode)
            storedAtYearEnd[key] = (storedAtYearEnd[key] ?: 0.0) + acc.balance
        }
        operations.forEach { op ->
            if (op.date < yearEndExclusive) return@forEach
            val acc = accountsByNumber[op.accountNumber] ?: return@forEach
            if (yearOf(acc.openedAt) > year) return@forEach
            val key = Key(acc.branchNumber, acc.currencyCode)
            val delta = if (op.type == OperationType.INCOME) -op.amount else op.amount
            storedAtYearEnd[key] = (storedAtYearEnd[key] ?: 0.0) + delta
        }

        val deposited = mutableMapOf<Key, Double>()
        val withdrawn = mutableMapOf<Key, Double>()
        operations.forEach { op ->
            if (yearOf(op.date) != year) return@forEach
            val acc = accountsByNumber[op.accountNumber] ?: return@forEach
            val key = Key(acc.branchNumber, acc.currencyCode)
            when (op.type) {
                OperationType.INCOME -> deposited[key] = (deposited[key] ?: 0.0) + op.amount
                OperationType.EXPENSE -> withdrawn[key] = (withdrawn[key] ?: 0.0) + op.amount
            }
        }

        val keys = (storedAtYearEnd.keys + deposited.keys + withdrawn.keys).toSet()

        return keys
            .groupBy { it.branchNumber }
            .toSortedMap()
            .map { (branchNumber, keysForBranch) ->
                BranchFundsGroup(
                    branchNumber = branchNumber,
                    branchAddress = branchByNumber[branchNumber]?.address ?: "—",
                    rows = keysForBranch
                        .sortedBy { it.currencyCode }
                        .map { key ->
                            CurrencyFundsRow(
                                currencyCode = key.currencyCode,
                                stored = storedAtYearEnd[key] ?: 0.0,
                                deposited = deposited[key] ?: 0.0,
                                withdrawn = withdrawn[key] ?: 0.0
                            )
                        }
                )
            }
    }

    private fun yearOf(millis: Long): Int =
        Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).year
}
