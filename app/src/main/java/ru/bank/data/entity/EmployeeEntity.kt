package ru.bank.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "employees",
    foreignKeys = [
        ForeignKey(
            entity = BranchEntity::class,
            parentColumns = ["branchNumber"],
            childColumns = ["branchNumber"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("branchNumber")]
)
data class EmployeeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val position: String,
    val branchNumber: Long,
    val password: String
)
