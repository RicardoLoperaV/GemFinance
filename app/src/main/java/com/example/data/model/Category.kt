package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val iconName: String,     // Name of standard Material Icon
    val colorHex: String,     // Hex color code (e.g. "#FF5722")
    val monthlyLimit: Double  // Monthly budget limit in COP (0.0 means unbudgeted)
)
