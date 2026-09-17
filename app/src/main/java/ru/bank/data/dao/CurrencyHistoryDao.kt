package ru.bank.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.bank.data.entity.CurrencyHistoryEntity

@Dao
interface CurrencyHistoryDao {
    @Query("""
        SELECT * FROM currency_history
        WHERE currencyCode = :code AND date >= :sinceMillis
        ORDER BY date
    """)
    fun observeSince(code: String, sinceMillis: Long): Flow<List<CurrencyHistoryEntity>>

    @Query("""
        SELECT * FROM currency_history WHERE currencyCode = :code
        ORDER BY date DESC LIMIT 1
    """)
    fun observeLatest(code: String): Flow<CurrencyHistoryEntity?>

    @Query("""
        SELECT ch.* FROM currency_history ch
        INNER JOIN (
            SELECT currencyCode, MAX(date) AS maxDate
            FROM currency_history
            GROUP BY currencyCode
        ) latest ON ch.currencyCode = latest.currencyCode AND ch.date = latest.maxDate
    """)
    fun observeAllLatest(): Flow<List<CurrencyHistoryEntity>>

    @Insert
    suspend fun insertAll(history: List<CurrencyHistoryEntity>)
}
