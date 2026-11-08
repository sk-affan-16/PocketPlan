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
        transactionId: Long,
        onResult: (Transaction) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val transaction = repository.getTransactionById(transactionId)

                if (transaction != null) {
                    onResult(transaction)
                } else {
                    onError("Transaction not found.")
                }
            } catch (exception: Exception) {
                onError(exception.message ?: "Unable to load transaction.")
            }
        }
    }

    fun updateTransaction(
        transactionId: Long,
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
                val existingTransaction =
                    repository.getTransactionById(transactionId)

                if (existingTransaction == null) {
                    onError("Transaction not found.")
                    return@launch
                }

                val updatedTransaction = existingTransaction.copy(
                    type = type,
                    amount = amount,
                    category = category,
                    date = date,
                    description = description
                )

                repository.updateTransaction(updatedTransaction)
                onSuccess()
            } catch (exception: Exception) {
                onError(exception.message ?: "Unable to update transaction.")
            }
        }
    }
}
