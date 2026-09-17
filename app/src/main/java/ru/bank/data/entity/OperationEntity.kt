package ru.bank.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class OperationType { INCOME, EXPENSE }

@Entity(
    tableName = "operations",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["accountNumber"],
            childColumns = ["accountNumber"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("accountNumber")]
)
data class OperationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountNumber: String,
    val type: OperationType,
    val amount: Double,
    val date: Long
)
