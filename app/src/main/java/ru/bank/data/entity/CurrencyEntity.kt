package ru.bank.data.entity

import androidx.room.Entity

@Entity(tableName = "currencies", primaryKeys = ["code"])
data class CurrencyEntity(
    val code: String,
    val name: String,
    val issuerCountry: String,
    val unit: Int = 1
)
