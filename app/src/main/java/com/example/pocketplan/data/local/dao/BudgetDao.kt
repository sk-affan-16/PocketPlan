package com.example.pocketplan.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.pocketplan.data.local.entity.Budget
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query(
        """
        SELECT * FROM budgets
        WHERE month = :month
        LIMIT 1
        """
    )
    fun getBudgetForMonth(month: String): Flow<Budget?>

    @Upsert
    suspend fun upsertBudget(budget: Budget)
}