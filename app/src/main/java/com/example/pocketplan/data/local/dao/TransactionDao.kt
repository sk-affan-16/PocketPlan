<<<<<<< HEAD

=======
>>>>>>> origin/feature/rabin
package com.example.pocketplan.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.pocketplan.data.local.entity.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

<<<<<<< HEAD
    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :transactionId LIMIT 1")
    suspend fun getTransactionById(transactionId: Long): Transaction?

    @Query("SELECT * FROM transactions WHERE date LIKE :month || '%' ORDER BY date DESC, id DESC")
    fun getMonthlyTransactions(month: String): Flow<List<Transaction>>

    @Query("""
=======
    @Query(
        """
        SELECT * FROM transactions
        ORDER BY date DESC, id DESC
        """
    )
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE date LIKE :month || '%'
        ORDER BY date DESC, id DESC
        """
    )
    fun getMonthlyTransactions(month: String): Flow<List<Transaction>>

    @Query(
        """
>>>>>>> origin/feature/rabin
        SELECT * FROM transactions
        WHERE (
            category LIKE '%' || :searchQuery || '%'
            OR description LIKE '%' || :searchQuery || '%'
            OR type LIKE '%' || :searchQuery || '%'
        )
        AND (:type IS NULL OR type = :type)
        AND (:category IS NULL OR category = :category)
        ORDER BY date DESC, id DESC
<<<<<<< HEAD
    """)
=======
        """
    )
>>>>>>> origin/feature/rabin
    fun searchAndFilterTransactions(
        searchQuery: String,
        type: String?,
        category: String?
    ): Flow<List<Transaction>>

<<<<<<< HEAD
    @Query("""
=======
    @Query(
        """
>>>>>>> origin/feature/rabin
        SELECT COALESCE(SUM(amount), 0.0)
        FROM transactions
        WHERE type = 'INCOME'
        AND date LIKE :month || '%'
<<<<<<< HEAD
    """)
    fun getIncomeTotal(month: String): Flow<Double>

    @Query("""
=======
        """
    )
    fun getIncomeTotal(month: String): Flow<Double>

    @Query(
        """
>>>>>>> origin/feature/rabin
        SELECT COALESCE(SUM(amount), 0.0)
        FROM transactions
        WHERE type = 'EXPENSE'
        AND date LIKE :month || '%'
<<<<<<< HEAD
    """)
    fun getExpenseTotal(month: String): Flow<Double>
}
=======
        """
    )
    fun getExpenseTotal(month: String): Flow<Double>
}
>>>>>>> origin/feature/rabin
