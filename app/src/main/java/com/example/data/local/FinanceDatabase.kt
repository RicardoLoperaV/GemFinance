package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Category
import com.example.data.model.Transaction
import com.example.data.model.SavingsGoal

@Database(
    entities = [Category::class, Transaction::class, SavingsGoal::class],
    version = 1,
    exportSchema = false
)
abstract class FinanceDatabase : RoomDatabase() {

    abstract fun financeDao(): FinanceDao

    companion object {
        @Volatile
        private var INSTANCE: FinanceDatabase? = null

        fun getDatabase(context: Context): FinanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinanceDatabase::class.java,
                    "finance_database"
                )
                .fallbackToDestructiveMigration() // Quick reset utility for dev iteration
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
