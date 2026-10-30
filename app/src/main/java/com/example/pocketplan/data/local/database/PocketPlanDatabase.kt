package com.example.pocketplan.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.pocketplan.data.local.dao.BudgetDao
import com.example.pocketplan.data.local.dao.TransactionDao
import com.example.pocketplan.data.local.entity.Budget
import com.example.pocketplan.data.local.entity.Transaction

@Database(
    entities = [
        Transaction::class,
        Budget::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PocketPlanDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    abstract fun budgetDao(): BudgetDao
}