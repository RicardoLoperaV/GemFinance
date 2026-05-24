package com.example.data.sheets

import com.squareup.moshi.JsonClass

/**
 * Model specifically mapped for external Google Sheets transactions.
 * Designed to hold clean, readable, primitive representations of transactions.
 */
@JsonClass(generateAdapter = true)
data class SheetsTransaction(
    val id: String,
    val amount: Double,
    val categoryName: String,
    val timestamp: Long,
    val notes: String
)

/**
 * Common response payload structure for Sheet web requests.
 */
@JsonClass(generateAdapter = true)
data class SheetsResponse(
    val status: String, // "success" or "error"
    val message: String? = null
)
