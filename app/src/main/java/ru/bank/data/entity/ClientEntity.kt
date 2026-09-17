package ru.bank.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(tableName = "clients", indices = [Index("passportData", unique = true)])
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val passportData: String,
    val phoneNumber: String
)
