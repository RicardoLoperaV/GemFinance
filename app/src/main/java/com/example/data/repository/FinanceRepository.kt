package com.example.data.repository

import android.util.Log
import com.example.data.local.FinanceDao
import com.example.data.model.Category
import com.example.data.model.Transaction
import com.example.data.model.SavingsGoal
import com.example.data.sheets.GoogleSheetsClient
import com.example.data.sheets.SheetsTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FinanceRepository(private val financeDao: FinanceDao) {
    private val TAG = "FinanceRepository"

    val categories: Flow<List<Category>> = financeDao.getAllCategories()
    val transactions: Flow<List<Transaction>> = financeDao.getAllTransactions()
    val savingsGoals: Flow<List<SavingsGoal>> = financeDao.getAllSavingsGoals()

    // --- CATEGORIES ---
    suspend fun insertCategory(category: Category) = financeDao.insertCategory(category)
    suspend fun insertCategories(categories: List<Category>) = financeDao.insertCategories(categories)
    suspend fun deleteCategory(id: String) = financeDao.deleteCategoryById(id)

    // --- TRANSACTIONS WITH OFFLINE-FIRST GOOGLE SHEETS SYNC ---
    suspend fun insertTransaction(transaction: Transaction, syncToSheets: Boolean = true) {
        // Save locally to local Room DB instantly for instantaneous UI reaction and offline functionality
        financeDao.insertTransaction(transaction)

        if (syncToSheets && GoogleSheetsClient.isConfigured) {
            withContext(Dispatchers.IO) {
                try {
                    val sheetsPayload = SheetsTransaction(
                        id = transaction.id,
                        amount = transaction.amount,
                        categoryName = transaction.categoryName,
                        timestamp = transaction.timestamp,
                        notes = transaction.notes
                    )
                    val response = GoogleSheetsClient.apiService.appendTransaction(sheetsPayload)
                    if (response.isSuccessful && response.body()?.status == "success") {
                        Log.d(TAG, "Google Sheet sync succeeded for transaction: ${transaction.id}")
                        // Mark as synced with remote Sheets database
                        financeDao.updateTransaction(transaction.copy(isSyncedWithSheets = true))
                    } else {
                        Log.w(TAG, "Google Sheet sync declined by endpoint: ${response.message()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Network connection error. Postponing transaction sheets upload: ${e.message}")
                }
            }
        }
    }

    suspend fun deleteTransaction(id: String) {
        financeDao.deleteTransactionById(id)
    }

    /**
     * Batch Reconciler: Auto-syncs any transactions logged offline when connectivity is established
     */
    suspend fun syncPendingTransactions() = withContext(Dispatchers.IO) {
        if (!GoogleSheetsClient.isConfigured) return@withContext

        val unsynced = financeDao.getUnsyncedTransactions()
        if (unsynced.isEmpty()) return@withContext

        Log.d(TAG, "Batch Synchronization initiated. Syncing ${unsynced.size} transactions to Google Sheets...")
        for (transaction in unsynced) {
            try {
                val payload = SheetsTransaction(
                    id = transaction.id,
                    amount = transaction.amount,
                    categoryName = transaction.categoryName,
                    timestamp = transaction.timestamp,
                    notes = transaction.notes
                )
                val response = GoogleSheetsClient.apiService.appendTransaction(payload)
                if (response.isSuccessful && response.body()?.status == "success") {
                    financeDao.updateTransaction(transaction.copy(isSyncedWithSheets = true))
                    Log.d(TAG, "Reconciled local transaction ${transaction.id} to Google Sheets.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Transient network interruption during reconciliation: ${e.message}")
                break // Postpone the rest
            }
        }
    }

    // --- SAVINGS GOALS ---
    suspend fun insertSavingsGoal(goal: SavingsGoal) = financeDao.insertSavingsGoal(goal)
    suspend fun deleteSavingsGoal(id: String) = financeDao.deleteSavingsGoalById(id)
}
