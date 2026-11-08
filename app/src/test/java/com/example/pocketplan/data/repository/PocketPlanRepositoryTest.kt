package com.example.pocketplan.data.repository

import com.example.pocketplan.data.local.dao.BudgetDao
import com.example.pocketplan.data.local.dao.TransactionDao
import com.example.pocketplan.data.local.entity.Budget
import com.example.pocketplan.data.local.entity.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class PocketPlanRepositoryTest {

    private class FakeTransactionDao : TransactionDao {

        val transactions = mutableListOf<Transaction>()

        override suspend fun insertTransaction(transaction: Transaction): Long {
            transactions.add(transaction)
            return 1L
        }

        override suspend fun updateTransaction(transaction: Transaction) {
            val index = transactions.indexOfFirst { it.id == transaction.id }
            if (index >= 0) transactions[index] = transaction
        }

        override suspend fun deleteTransaction(transaction: Transaction) {
            transactions.remove(transaction)
        }

        override fun getAllTransactions(): Flow<List<Transaction>> =
            flowOf(transactions.toList())

        override fun getMonthlyTransactions(month: String): Flow<List<Transaction>> =
            flowOf(transactions.filter { it.date.startsWith(month) })

        override fun searchAndFilterTransactions(
            searchQuery: String,
            type: String?,
            category: String?
        ): Flow<List<Transaction>> = flowOf(
            transactions.filter {
                (searchQuery.isBlank() ||
                    it.category.contains(searchQuery, true) ||
                    it.description.orEmpty().contains(searchQuery, true) ||
                    it.type.contains(searchQuery, true)) &&
                    (type == null || it.type == type) &&
                    (category == null || it.category == category)
            }
        )

        override fun getIncomeTotal(month: String): Flow<Double> =
            flowOf(
                transactions.filter {
                    it.type == "INCOME" && it.date.startsWith(month)
                }.sumOf { it.amount }
            )

        override fun getExpenseTotal(month: String): Flow<Double> =
            flowOf(
                transactions.filter {
                    it.type == "EXPENSE" && it.date.startsWith(month)
                }.sumOf { it.amount }
            )
    }

    private class FakeBudgetDao : BudgetDao {

        val budgets = mutableListOf<Budget>()

        override fun getBudgetForMonth(month: String): Flow<Budget?> =
            flowOf(budgets.firstOrNull { it.month == month })

        override suspend fun upsertBudget(budget: Budget) {
            val index = budgets.indexOfFirst { it.id == budget.id }

            if (index >= 0) {
                budgets[index] = budget
            } else {
                budgets.add(budget)
            }
        }
    }

    @Test
    fun addTransaction_returnsInsertedId() = runBlocking {
        val dao = FakeTransactionDao()
        val repository = PocketPlanRepository(dao, FakeBudgetDao())

        val transaction = Transaction(
            type = "INCOME",
            amount = 5000.0,
            category = "Salary",
            date = "2026-11-08"
        )

        assertEquals(1L, repository.addTransaction(transaction))
        assertEquals(1, dao.transactions.size)
    }

    @Test
    fun getIncomeTotal_returnsCorrectTotal() = runBlocking {
        val dao = FakeTransactionDao()
        val repository = PocketPlanRepository(dao, FakeBudgetDao())

        dao.transactions.add(
            Transaction(
                type = "INCOME",
                amount = 5000.0,
                category = "Salary",
                date = "2026-11-08"
            )
        )

        assertEquals(
            5000.0,
            repository.getIncomeTotal("2026-11").first(),
            0.001
        )
    }

    @Test
    fun saveMonthlyBudget_createsNewBudget() = runBlocking {
        val budgetDao = FakeBudgetDao()
        val repository = PocketPlanRepository(FakeTransactionDao(), budgetDao)

        repository.saveMonthlyBudget("2026-11", 10000.0)

        assertEquals(1, budgetDao.budgets.size)
        assertEquals(10000.0, budgetDao.budgets[0].amount, 0.001)
    }

    @Test
    fun saveMonthlyBudget_updatesExistingBudget() = runBlocking {
        val budgetDao = FakeBudgetDao()
        budgetDao.budgets.add(
            Budget(id = 1L, month = "2026-11", amount = 10000.0)
        )

        val repository = PocketPlanRepository(FakeTransactionDao(), budgetDao)

        repository.saveMonthlyBudget("2026-11", 15000.0)

        assertEquals(1, budgetDao.budgets.size)
        assertEquals(1L, budgetDao.budgets[0].id)
        assertEquals(15000.0, budgetDao.budgets[0].amount, 0.001)
    }
}
