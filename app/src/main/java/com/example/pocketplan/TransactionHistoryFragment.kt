
package com.example.pocketplan

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import com.example.pocketplan.data.local.database.DatabaseProvider
import com.example.pocketplan.data.local.entity.Transaction
import com.example.pocketplan.data.repository.PocketPlanRepository
import kotlinx.coroutines.launch
import java.util.Locale

class TransactionHistoryFragment :
    Fragment(R.layout.fragment_transaction_history) {

    private lateinit var transactionListContainer: LinearLayout
    private lateinit var tvEmptyTransactions: TextView
    private lateinit var viewModel: TransactionViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        transactionListContainer =
            view.findViewById(R.id.transactionListContainer)

        tvEmptyTransactions =
            view.findViewById(R.id.tvEmptyTransactions)

        val database = DatabaseProvider.getDatabase(requireContext())

        val repository = PocketPlanRepository(
            database.transactionDao(),
            database.budgetDao()
        )

        viewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
                        return TransactionViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        )[TransactionViewModel::class.java]

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.getAllTransactions().collect { transactions ->
                    displayTransactions(transactions)
                }
            }
        }
    }

    private fun displayTransactions(transactions: List<Transaction>) {
        transactionListContainer.removeAllViews()

        if (transactions.isEmpty()) {
            tvEmptyTransactions.visibility = View.VISIBLE
            return
        }

        tvEmptyTransactions.visibility = View.GONE

        transactions.forEach { transaction ->

            val itemLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
            }

            val typeText = TextView(requireContext()).apply {
                text = transaction.type
                textSize = 18f
            }

            val amountText = TextView(requireContext()).apply {
                text = String.format(
                    Locale.getDefault(),
                    "Amount: %.2f",
                    transaction.amount
                )
                textSize = 16f
            }

            val categoryText = TextView(requireContext()).apply {
                text = "Category: ${transaction.category}"
                textSize = 14f
            }

            val dateText = TextView(requireContext()).apply {
                text = "Date: ${transaction.date}"
                textSize = 14f
            }

            itemLayout.addView(typeText)
            itemLayout.addView(amountText)
            itemLayout.addView(categoryText)
            itemLayout.addView(dateText)

            transaction.description
                ?.takeIf { it.isNotBlank() }
                ?.let {
                    itemLayout.addView(
                        TextView(requireContext()).apply {
                            text = "Description: $it"
                            textSize = 14f
                        }
                    )
                }

            val buttonLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            val editButton = Button(requireContext()).apply {
                text = "Edit"
                setOnClickListener {
                    showEditDialog(transaction)
                }
            }

            val deleteButton = Button(requireContext()).apply {
                text = "Delete"
                setOnClickListener {
                    showDeleteDialog(transaction)
                }
            }

            buttonLayout.addView(
                editButton,
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            )

            buttonLayout.addView(
                deleteButton,
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            )

            itemLayout.addView(buttonLayout)
            transactionListContainer.addView(itemLayout)
        }
    }

    private fun showEditDialog(transaction: Transaction) {
        val amountInput = EditText(requireContext()).apply {
            setText(transaction.amount.toString())
            hint = "Enter amount"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Transaction Amount")
            .setMessage("Update the amount for this transaction.")
            .setView(amountInput)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Update") { _, _ ->
                val amount = amountInput.text.toString().toDoubleOrNull()

                if (amount == null || amount <= 0.0) {
                    Toast.makeText(
                        requireContext(),
                        "Enter a valid amount",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                val updatedTransaction = transaction.copy(amount = amount)

                viewModel.updateTransaction(
                    updatedTransaction,
                    onSuccess = {
                        if (!isAdded) return@updateTransaction
                        Toast.makeText(
                            requireContext(),
                            "Transaction updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onError = { message ->
                        if (!isAdded) return@updateTransaction
                        Toast.makeText(
                            requireContext(),
                            message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
            .show()
    }

    private fun showDeleteDialog(transaction: Transaction) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteTransaction(
                    transaction,
                    onSuccess = {
                        if (!isAdded) return@deleteTransaction
                        Toast.makeText(
                            requireContext(),
                            "Transaction deleted successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onError = { message ->
                        if (!isAdded) return@deleteTransaction
                        Toast.makeText(
                            requireContext(),
                            message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
            .show()
    }
}
