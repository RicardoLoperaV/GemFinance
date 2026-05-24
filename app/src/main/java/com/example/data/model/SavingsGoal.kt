package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val targetAmount: Double,    // Target amount to save in COP
    val currentAmount: Double,   // Total historically accumulated savings in COP
    val deadline: Long? = null,  // Target completion epoch MS (optional)
    val colorHex: String = "#00E5FF" // Visual identification hex value
)
