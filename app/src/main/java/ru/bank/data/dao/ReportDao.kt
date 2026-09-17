package ru.bank.data.dao

import androidx.room.Dao
import androidx.room.Query

data class StoredSumRow(
    val branchNumber: Long,
    val currencyCode: String,
    val stored: Double
)


data class FutureReversalRow(
    val branchNumber: Long,
    val currencyCode: String,
    val futureIncome: Double,
    val futureExpense: Double
)


data class OperationSumRow(
    val branchNumber: Long,
    val currencyCode: String,
    val deposited: Double,
    val withdrawn: Double
)


@Dao
interface ReportDao {

    @Query("""
        SELECT DISTINCT year FROM (
            SELECT CAST(strftime('%Y', openedAt / 1000, 'unixepoch') AS INTEGER) AS year FROM accounts
            UNION
            SELECT CAST(strftime('%Y', date / 1000, 'unixepoch') AS INTEGER) AS year FROM operations
        )
        ORDER BY year DESC
    """)
    suspend fun getAvailableYears(): List<Int>


    @Query("""
        SELECT branchNumber, currencyCode, SUM(balance) AS stored
        FROM accounts
        WHERE CAST(strftime('%Y', openedAt / 1000, 'unixepoch') AS INTEGER) <= :year
        GROUP BY branchNumber, currencyCode
    """)
    suspend fun getBaseStoredSums(year: Int): List<StoredSumRow>


    @Query("""
        SELECT a.branchNumber AS branchNumber, a.currencyCode AS currencyCode,
               SUM(CASE WHEN o.type = 'INCOME' THEN o.amount ELSE 0 END) AS futureIncome,
               SUM(CASE WHEN o.type = 'EXPENSE' THEN o.amount ELSE 0 END) AS futureExpense
        FROM operations o
        INNER JOIN accounts a ON a.accountNumber = o.accountNumber
        WHERE o.date >= :yearEndExclusiveMillis
          AND CAST(strftime('%Y', a.openedAt / 1000, 'unixepoch') AS INTEGER) <= :year
        GROUP BY a.branchNumber, a.currencyCode
    """)
    suspend fun getFutureReversalSums(year: Int, yearEndExclusiveMillis: Long): List<FutureReversalRow>

    @Query("""
        SELECT a.branchNumber AS branchNumber, a.currencyCode AS currencyCode,
               SUM(CASE WHEN o.type = 'INCOME' THEN o.amount ELSE 0 END) AS deposited,
               SUM(CASE WHEN o.type = 'EXPENSE' THEN o.amount ELSE 0 END) AS withdrawn
        FROM operations o
        INNER JOIN accounts a ON a.accountNumber = o.accountNumber
        WHERE CAST(strftime('%Y', o.date / 1000, 'unixepoch') AS INTEGER) = :year
        GROUP BY a.branchNumber, a.currencyCode
    """)
    suspend fun getOperationSums(year: Int): List<OperationSumRow>
}
