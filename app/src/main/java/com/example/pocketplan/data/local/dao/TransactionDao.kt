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
        SELECT * FROM transactions
        WHERE (
            category LIKE '%' || :searchQuery || '%'
            OR description LIKE '%' || :searchQuery || '%'
            OR type LIKE '%' || :searchQuery || '%'
        )
        AND (:type IS NULL OR type = :type)
        AND (:category IS NULL OR category = :category)
        ORDER BY date DESC, id DESC
        """
    )
    fun searchAndFilterTransactions(
        searchQuery: String,
        type: String?,
        category: String?
    ): Flow<List<Transaction>>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0.0)
        FROM transactions
        WHERE type = 'INCOME'
        AND date LIKE :month || '%'
        """
    )
    fun getIncomeTotal(month: String): Flow<Double>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0.0)
        FROM transactions
        WHERE type = 'EXPENSE'
        AND date LIKE :month || '%'
        """
    )
    fun getExpenseTotal(month: String): Flow<Double>
}