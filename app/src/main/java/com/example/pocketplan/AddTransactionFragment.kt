
package com.example.pocketplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocketplan.data.local.entity.Transaction
import com.example.pocketplan.data.repository.PocketPlanRepository
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val repository: PocketPlanRepository
) : ViewModel() {

    fun addTransaction(
        type: String,
        amount: Double,
        category: String,
        date: String,
        description: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val transaction = Transaction(
                    type = type,
                    amount = amount,
                    category = category,
                    date = date,
                    description = description
                )

                repository.addTransaction(transaction)
                onSuccess()
            } catch (exception: Exception) {
                onError(exception.message ?: "Unable to save transaction.")
            }
        }
    }

    fun getTransactionById(
        id: Long,
        onResult: (Transaction?) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                onResult(repository.getTransactionById(id))
            } catch (exception: Exception) {
                onError(exception.message ?: "Unable to load transaction.")
            }
        }
    }

    fun updateTransaction(
        transaction: Transaction,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.updateTransaction(transaction)
                onSuccess()
            } catch (exception: Exception) {
                onError(exception.message ?: "Unable to update transaction.")
            }
        }
    }

    fun deleteTransaction(
        transaction: Transaction,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.deleteTransaction(transaction)
                onSuccess()
            } catch (exception: Exception) {
                onError(exception.message ?: "Unable to delete transaction.")
            }
        }
    }
}
