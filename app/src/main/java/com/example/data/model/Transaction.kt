package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val amount: Double,       // Amount of money in COP
    val categoryId: String,   // Refers to Category.id
    val categoryName: String, // Cached category name for offline presentation & export
    val timestamp: Long = System.currentTimeMillis(), // Time of transaction
    val notes: String = "",
    val isSyncedWithSheets: Boolean = false // Sync status for Google Sheets reconciliation
)
