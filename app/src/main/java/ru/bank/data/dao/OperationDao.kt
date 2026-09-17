package ru.bank.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.bank.data.entity.OperationEntity

@Dao
interface OperationDao {
    @Query("SELECT * FROM operations WHERE accountNumber = :accountNumber ORDER BY date DESC")
    fun observeByAccount(accountNumber: String): Flow<List<OperationEntity>>

    @Query("""
        SELECT * FROM operations WHERE accountNumber = :accountNumber AND date BETWEEN :from AND :to
        ORDER BY date
    """)
    suspend fun getForPeriod(accountNumber: String, from: Long, to: Long): List<OperationEntity>

    @Query("SELECT * FROM operations")
    fun observeAllOperations(): Flow<List<OperationEntity>>

    @Insert
    suspend fun insert(operation: OperationEntity): Long

    @Insert
    suspend fun insertAll(operations: List<OperationEntity>)
}
