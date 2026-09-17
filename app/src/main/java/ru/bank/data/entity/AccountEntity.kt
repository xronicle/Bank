package ru.bank.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "accounts",
    foreignKeys = [
        ForeignKey(entity = ClientEntity::class, parentColumns = ["id"], childColumns = ["clientId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = EmployeeEntity::class, parentColumns = ["id"], childColumns = ["employeeId"]),
        ForeignKey(entity = BranchEntity::class, parentColumns = ["branchNumber"], childColumns = ["branchNumber"]),
        ForeignKey(entity = CurrencyEntity::class, parentColumns = ["code"], childColumns = ["currencyCode"])
    ],
    indices = [Index("clientId"), Index("employeeId"), Index("branchNumber"), Index("currencyCode")]
)
data class AccountEntity(
    @PrimaryKey val accountNumber: String,
    val clientId: Long,
    val employeeId: Long,
    val branchNumber: Long,
    val currencyCode: String,
    val accountType: String,
    val contractNumber: String,
    val openedAt: Long,
    val balance: Double
)