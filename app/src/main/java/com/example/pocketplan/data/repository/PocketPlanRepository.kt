package com.example.pocketplan.data.repository

import com.example.pocketplan.data.local.dao.BudgetDao
import com.example.pocketplan.data.local.dao.TransactionDao
import com.example.pocketplan.data.local.entity.Budget
import com.example.pocketplan.data.local.entity.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class PocketPlanRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao
) {

    // -------------------------
    // Transaction operations
    // -------------------------

    suspend fun addTransaction(transaction: Transaction): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions()
    }

    fun getMonthlyTransactions(month: String): Flow<List<Transaction>> {
        return transactionDao.getMonthlyTransactions(month)
    }

    fun searchAndFilterTransactions(
        searchQuery: String,
        type: String?,
        category: String?
    ): Flow<List<Transaction>> {
        return transactionDao.searchAndFilterTransactions(
            searchQuery = searchQuery,
            type = type,
            category = category
        )
    }

    fun getIncomeTotal(month: String): Flow<Double> {
        return transactionDao.getIncomeTotal(month)
    }

    fun getExpenseTotal(month: String): Flow<Double> {
        return transactionDao.getExpenseTotal(month)
    }

    // -------------------------
    // Budget operations
    // -------------------------

    fun getMonthlyBudget(month: String): Flow<Budget?> {
        return budgetDao.getBudgetForMonth(month)
    }

    suspend fun saveMonthlyBudget(
        month: String,
        amount: Double
    ) {
        val existingBudget = budgetDao.getBudgetForMonth(month).first()

        val budget = if (existingBudget != null) {
            existingBudget.copy(
                amount = amount
            )
        } else {
            Budget(
                month = month,
                amount = amount
            )
        }

        budgetDao.upsertBudget(budget)
    }
}