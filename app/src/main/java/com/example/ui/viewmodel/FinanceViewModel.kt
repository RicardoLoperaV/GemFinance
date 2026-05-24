package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.FinanceDatabase
import com.example.data.model.Category
import com.example.data.model.SavingsGoal
import com.example.data.model.Transaction
import com.example.data.repository.FinanceRepository
import com.example.data.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "FinanceViewModel"
    private val repository: FinanceRepository

    // Base Flows of real-time offline-first persistent states
    val categories: StateFlow<List<Category>>
    val transactions: StateFlow<List<Transaction>>
    val savingsGoals: StateFlow<List<SavingsGoal>>

    // General Balance Ingestion States
    val totalInvestmentBalance = MutableStateFlow(2500000.0) // Sample investment balance COP
    val totalLiquidSavings = MutableStateFlow(1200000.0)    // Balance in savings account

    init {
        val database = FinanceDatabase.getDatabase(application)
        repository = FinanceRepository(database.financeDao())

        categories = repository.categories.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        transactions = repository.transactions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        savingsGoals = repository.savingsGoals.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Prep notification channel
        NotificationHelper.initNotificationChannel(application)

        // Check if database needs original seed data
        viewModelScope.launch {
            seedInitialDataIfNecessary()
            // Auto back-sync any offline transactions to sheets
            repository.syncPendingTransactions()
        }
    }

    // Dynamic month-by-month filtering calculations (Defaulting to the calendar month)
    val monthlySpendingSummary: StateFlow<MonthlySummary> = combine(
        transactions,
        categories
    ) { txs, _cats ->
        val currentMonthTxs = filterCurrentMonthTransactions(txs)
        val totalSpent = currentMonthTxs.sumOf { it.amount }

        // Compile spending by category
        val categoryProgressMap = mutableMapOf<String, Double>()
        currentMonthTxs.forEach { tx ->
            val prev = categoryProgressMap[tx.categoryId] ?: 0.0
            categoryProgressMap[tx.categoryId] = prev + tx.amount
        }

        MonthlySummary(
            totalSpent = totalSpent,
            spendingByCategoryId = categoryProgressMap
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MonthlySummary()
    )

    // --- OPERATIONS ---

    fun logTransaction(amount: Double, category: Category, notes: String, timestamp: Long) {
        viewModelScope.launch {
            val transaction = Transaction(
                amount = amount,
                categoryId = category.id,
                categoryName = category.name,
                timestamp = timestamp,
                notes = notes
            )
            
            // Check limits dynamically BEFORE insertion
            val currentSpent = monthlySpendingSummary.value.spendingByCategoryId[category.id] ?: 0.0
            val newTotalSpent = currentSpent + amount
            val limit = category.monthlyLimit

            if (limit > 0.0) {
                val prePercent = (currentSpent / limit * 100).toInt()
                val postPercent = (newTotalSpent / limit * 100).toInt()

                // Trigger warnings contextually
                if (prePercent < 90 && postPercent in 90..99) {
                    NotificationHelper.showLimitWarning(getApplication(), category.name, 90, newTotalSpent, limit)
                } else if (prePercent < 100 && postPercent >= 100) {
                    NotificationHelper.showLimitWarning(getApplication(), category.name, postPercent, newTotalSpent, limit)
                }
            }

            repository.insertTransaction(transaction)
        }
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            repository.deleteTransaction(transactionId)
        }
    }

    fun addCategory(name: String, iconName: String, colorHex: String, monthlyLimit: Double) {
        viewModelScope.launch {
            val newCategory = Category(
                name = name,
                iconName = iconName,
                colorHex = colorHex,
                monthlyLimit = monthlyLimit
            )
            repository.insertCategory(newCategory)
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            repository.deleteCategory(id)
        }
    }

    fun addSavingsGoal(name: String, targetAmount: Double, currentAmount: Double, colorHex: String) {
        viewModelScope.launch {
            val goal = SavingsGoal(
                name = name,
                targetAmount = targetAmount,
                currentAmount = currentAmount,
                colorHex = colorHex
            )
            repository.insertSavingsGoal(goal)
        }
    }

    fun deleteSavingsGoal(id: String) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(id)
        }
    }

    fun incrementSavingsGoalProgress(goal: SavingsGoal, increment: Double) {
        viewModelScope.launch {
            val newAmount = (goal.currentAmount + increment).coerceAtMost(goal.targetAmount)
            repository.insertSavingsGoal(goal.copy(currentAmount = newAmount))
            // deduct from liquid savings if specified
            val currentSavings = totalLiquidSavings.value
            totalLiquidSavings.value = (currentSavings - increment).coerceAtLeast(0.0)
        }
    }

    fun triggerManualSync() {
        viewModelScope.launch {
            repository.syncPendingTransactions()
        }
    }

    // --- SEED SECTOR ---

    private suspend fun seedInitialDataIfNecessary() {
        // Query to check if categories are pristine
        val currentCats = repository.categories.first()
        if (currentCats.isEmpty()) {
            val seedCategories = listOf(
                Category(name = "Alimentación", iconName = "restaurant", colorHex = "#F2B8B5", monthlyLimit = 1200000.0),
                Category(name = "Transporte", iconName = "directions_car", colorHex = "#7FCFFF", monthlyLimit = 500000.0),
                Category(name = "Suscripciones & Ocio", iconName = "sports_esports", colorHex = "#B4E495", monthlyLimit = 300000.0),
                Category(name = "Servicios", iconName = "bolt", colorHex = "#00E5FF", monthlyLimit = 600000.0)
            )
            repository.insertCategories(seedCategories)

            // Seed sample savings goals
            repository.insertSavingsGoal(
                SavingsGoal(name = "Fondo de Emergencias", targetAmount = 5000000.0, currentAmount = 2500000.0, colorHex = "#00E5FF")
            )
            repository.insertSavingsGoal(
                SavingsGoal(name = "Vacaciones", targetAmount = 3000000.0, currentAmount = 900000.0, colorHex = "#B4E495")
            )

            // Feed pre-populated historical transactions to make chart visualize beautifully on first run
            val baseTime = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 10)
            }.timeInMillis

            val restaurantId = seedCategories[0].id
            val rName = seedCategories[0].name
            val transportId = seedCategories[1].id
            val tName = seedCategories[1].name

            repository.insertTransaction(
                Transaction(amount = 45000.0, categoryId = restaurantId, categoryName = rName, timestamp = baseTime - 86400000 * 2, notes = "Almuerzo de trabajo"),
                syncToSheets = false
            )
            repository.insertTransaction(
                Transaction(amount = 120000.0, categoryId = restaurantId, categoryName = rName, timestamp = baseTime - 86400000 * 1, notes = "Mercado Semanal"),
                syncToSheets = false
            )
            repository.insertTransaction(
                Transaction(amount = 25000.0, categoryId = transportId, categoryName = tName, timestamp = baseTime, notes = "Combustible"),
                syncToSheets = false
            )
        }
    }

    private fun filterCurrentMonthTransactions(txs: List<Transaction>): List<Transaction> {
        val calCurrent = Calendar.getInstance()
        val m = calCurrent.get(Calendar.MONTH)
        val y = calCurrent.get(Calendar.YEAR)

        val itemCal = Calendar.getInstance()
        return txs.filter {
            itemCal.timeInMillis = it.timestamp
            itemCal.get(Calendar.MONTH) == m && itemCal.get(Calendar.YEAR) == y
        }
    }
}

data class MonthlySummary(
    val totalSpent: Double = 0.0,
    val spendingByCategoryId: Map<String, Double> = emptyMap()
)
