package com.example.pocketplan.data.local.database

import androidx.room.Room
import com.example.pocketplan.data.local.entity.Budget
import com.example.pocketplan.data.local.entity.Transaction
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PocketPlanDatabaseTest {

    private val context = RuntimeEnvironment.getApplication()
    private val databaseName = "pocketplan-room-test.db"

    private lateinit var database: PocketPlanDatabase

    @Before
    fun createDatabase() {
        context.deleteDatabase(databaseName)

        database = Room.databaseBuilder(
            context,
            PocketPlanDatabase::class.java,
            databaseName
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDatabase() {
        if (::database.isInitialized) {
            database.close()
        }
        context.deleteDatabase(databaseName)
    }

    @Test
    fun transactionCanBeInsertedReadUpdatedAndDeleted() = runBlocking {
        val dao = database.transactionDao()

        val transaction = Transaction(
            type = "EXPENSE",
            amount = 250.0,
            category = "Food",
            description = "Lunch",
            date = "2026-11-08"
        )

        val id = dao.insertTransaction(transaction)
        assertTrue(id > 0)

        val inserted = dao.getTransactionById(id)
        assertNotNull(inserted)
        assertEquals(250.0, inserted!!.amount, 0.0)

        val updated = inserted.copy(
            amount = 300.0,
            description = "Dinner"
        )
        dao.updateTransaction(updated)

        val afterUpdate = dao.getTransactionById(id)
        assertNotNull(afterUpdate)
        assertEquals(300.0, afterUpdate!!.amount, 0.0)
        assertEquals("Dinner", afterUpdate.description)

        dao.deleteTransaction(afterUpdate)

        assertNull(dao.getTransactionById(id))
    }

    @Test
    fun monthlyTransactionsAndSearchFiltersReturnMatchingRows() = runBlocking {
        val dao = database.transactionDao()

        dao.insertTransaction(
            Transaction(
                type = "EXPENSE",
                amount = 150.0,
                category = "Food",
                description = "Lunch",
                date = "2026-11-08"
            )
        )

        dao.insertTransaction(
            Transaction(
                type = "INCOME",
                amount = 5000.0,
                category = "Salary",
                description = "November salary",
                date = "2026-11-01"
            )
        )

        dao.insertTransaction(
            Transaction(
                type = "EXPENSE",
                amount = 80.0,
                category = "Travel",
                description = "Bus ticket",
                date = "2026-10-25"
            )
        )

        val november = dao.getMonthlyTransactions("2026-11").first()
        assertEquals(2, november.size)

        val foodExpenses = dao.searchAndFilterTransactions(
            searchQuery = "",
            type = "EXPENSE",
            category = "Food"
        ).first()

        assertEquals(1, foodExpenses.size)
        assertEquals("Food", foodExpenses.first().category)

        val lunchSearch = dao.searchAndFilterTransactions(
            searchQuery = "Lunch",
            type = null,
            category = null
        ).first()

        assertEquals(1, lunchSearch.size)
        assertEquals("Lunch", lunchSearch.first().description)
    }

    @Test
    fun monthlyIncomeAndExpenseTotalsAreCalculatedCorrectly() = runBlocking {
        val dao = database.transactionDao()

        dao.insertTransaction(
            Transaction(
                type = "INCOME",
                amount = 5000.0,
                category = "Salary",
                date = "2026-11-01"
            )
        )

        dao.insertTransaction(
            Transaction(
                type = "INCOME",
                amount = 1000.0,
                category = "Other",
                date = "2026-11-05"
            )
        )

        dao.insertTransaction(
            Transaction(
                type = "EXPENSE",
                amount = 1200.0,
                category = "Food",
                date = "2026-11-06"
            )
        )

        dao.insertTransaction(
            Transaction(
                type = "EXPENSE",
                amount = 300.0,
                category = "Travel",
                date = "2026-10-20"
            )
        )

        assertEquals(6000.0, dao.getIncomeTotal("2026-11").first(), 0.01)
        assertEquals(1200.0, dao.getExpenseTotal("2026-11").first(), 0.01)
    }

    @Test
    fun budgetCanBeInsertedUpdatedAndRetrieved() = runBlocking {
        val dao = database.budgetDao()

        dao.upsertBudget(
            Budget(
                month = "2026-11",
                amount = 10000.0
            )
        )

        val inserted = dao.getBudgetForMonth("2026-11").first()
        assertNotNull(inserted)
        assertEquals(10000.0, inserted!!.amount, 0.0)

        dao.upsertBudget(
            inserted.copy(amount = 12000.0)
        )

        val updated = dao.getBudgetForMonth("2026-11").first()
        assertNotNull(updated)
        assertEquals(12000.0, updated!!.amount, 0.0)
    }

    @Test
    fun transactionSurvivesDatabaseCloseAndReopen() = runBlocking {
        val transaction = Transaction(
            type = "EXPENSE",
            amount = 450.0,
            category = "Shopping",
            description = "Test persistence",
            date = "2026-11-08"
        )

        val id = database.transactionDao().insertTransaction(transaction)

        database.close()

        database = Room.databaseBuilder(
            context,
            PocketPlanDatabase::class.java,
            databaseName
        ).allowMainThreadQueries().build()

        val restored = database.transactionDao().getTransactionById(id)

        assertNotNull(restored)
        assertEquals(450.0, restored!!.amount, 0.0)
        assertEquals("Test persistence", restored.description)
    }
}
