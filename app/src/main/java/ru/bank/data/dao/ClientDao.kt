package ru.bank.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.bank.data.entity.ClientEntity

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY fullName")
    fun observeAll(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE id = :id")
    suspend fun getById(id: Long): ClientEntity?

    @Query("SELECT * FROM clients WHERE passportData = :passport LIMIT 1")
    suspend fun getByPassport(passport: String): ClientEntity?

    @Query("SELECT COUNT(*) FROM clients")
    fun observeCount(): Flow<Int>

    @Insert
    suspend fun insert(client: ClientEntity): Long

    @Insert
    suspend fun insertAll(clients: List<ClientEntity>): List<Long>

    @Update
    suspend fun update(client: ClientEntity)

    @Delete
    suspend fun delete(client: ClientEntity)
}
