package ru.bank.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.bank.data.entity.BranchEntity

@Dao
interface BranchDao {
    @Query("SELECT * FROM branches ORDER BY branchNumber")
    fun observeAll(): Flow<List<BranchEntity>>

    @Query("SELECT * FROM branches WHERE branchNumber = :branchNumber")
    suspend fun getById(branchNumber: Long): BranchEntity?

    @Query("SELECT * FROM branches ORDER BY branchNumber")
    suspend fun getAllOnce(): List<BranchEntity>

    @Insert
    suspend fun insertAll(branches: List<BranchEntity>)
}
