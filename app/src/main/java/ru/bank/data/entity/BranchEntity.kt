package ru.bank.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "branches")
data class BranchEntity(
    @PrimaryKey val branchNumber: Long,
    val address: String
)
