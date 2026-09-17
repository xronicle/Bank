package ru.bank.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.bank.data.entity.AccountEntity

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE clientId = :clientId ORDER BY openedAt")
    fun observeByClient(clientId: Long): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE accountNumber = :accountNumber")
    suspend fun getById(accountNumber: String): AccountEntity?

    @Query("SELECT * FROM accounts WHERE accountNumber = :accountNumber")
    fun observeById(accountNumber: String): Flow<AccountEntity?>

    @Query("SELECT * FROM accounts")
    fun observeAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT COUNT(*) FROM accounts")
    fun observeCount(): Flow<Int>

    @Insert
    suspend fun insert(account: AccountEntity)

    @Insert
    suspend fun insertAll(accounts: List<AccountEntity>)

    @Update
    suspend fun update(account: AccountEntity)

    @Delete
    suspend fun delete(account: AccountEntity)
}
