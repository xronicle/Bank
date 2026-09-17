package ru.bank.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index


@Entity(
    tableName = "currency_history",
    primaryKeys = ["currencyCode", "date"],
    foreignKeys = [
        ForeignKey(
            entity = CurrencyEntity::class,
            parentColumns = ["code"],
            childColumns = ["currencyCode"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("currencyCode")]
)
data class CurrencyHistoryEntity(
    val currencyCode: String,
    val date: Long,
    val rate: Double
)
