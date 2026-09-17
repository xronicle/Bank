package ru.bank.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.bank.data.entity.CurrencyEntity

@Dao
interface CurrencyDao {
    @Query("SELECT * FROM currencies ORDER BY code")
    fun observeAll(): Flow<List<CurrencyEntity>>

    @Insert
    suspend fun insertAll(currencies: List<CurrencyEntity>)
}
