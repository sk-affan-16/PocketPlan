
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
                onError(
                    exception.message ?: "Unable to save transaction."
                )
            }
        }
    }
}
