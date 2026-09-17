package ru.bank.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.bank.data.entity.EmployeeEntity

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees WHERE branchNumber = :branchNumber ORDER BY fullName")
    fun observeByBranch(branchNumber: Long): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employees WHERE id = :id")
    suspend fun getById(id: Long): EmployeeEntity?

    @Insert
    suspend fun insertAll(employees: List<EmployeeEntity>)
}
