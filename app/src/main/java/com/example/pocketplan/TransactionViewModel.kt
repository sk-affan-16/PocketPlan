
package com.example.pocketplan

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pocketplan.data.local.database.DatabaseProvider
import com.example.pocketplan.data.local.entity.Transaction
import com.example.pocketplan.data.repository.PocketPlanRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTransactionFragment : Fragment(R.layout.fragment_add_transaction) {

    private lateinit var viewModel: TransactionViewModel
    private var editingTransaction: Transaction? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        val spinnerType = view.findViewById<Spinner>(R.id.spinnerTransactionType)
        val etAmount = view.findViewById<EditText>(R.id.etAmount)
        val spinnerCategory = view.findViewById<Spinner>(R.id.spinnerCategory)
        val etDate = view.findViewById<EditText>(R.id.etDate)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val btnSave = view.findViewById<Button>(R.id.btnSaveTransaction)
        val tvTitle = view.findViewById<TextView>(R.id.tvTransactionTitle)

        val transactionTypes = listOf("INCOME", "EXPENSE")

        val incomeCategories = listOf(
            "Salary", "Business", "Investment", "Gift", "Other"
        )

        val expenseCategories = listOf(
            "Food", "Transport", "Shopping", "Bills",
            "Education", "Health", "Entertainment", "Other"
        )

        spinnerType.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            transactionTypes
        )

        fun updateCategories(selectedCategory: String? = null) {
            val categories =
                if (spinnerType.selectedItem.toString() == "INCOME") {
                    incomeCategories
                } else {
                    expenseCategories
                }

            spinnerCategory.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                categories
            )

            val selectedIndex = categories.indexOf(selectedCategory)
            if (selectedIndex >= 0) {
                spinnerCategory.setSelection(selectedIndex)
            }
        }

        spinnerType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    updateCategories()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        etDate.setText(dateFormat.format(Calendar.getInstance().time))

        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()

            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, month, day)
                    etDate.setText(dateFormat.format(selectedDate.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val transactionId = arguments?.get("transactionId") as? Long

        if (transactionId != null && transactionId > 0L) {
            tvTitle.text = "Edit Transaction"
            btnSave.text = "Update Transaction"
            btnSave.isEnabled = false

            viewModel.getTransactionById(
                id = transactionId,
                onResult = { transaction ->
                    if (!isAdded) return@getTransactionById

                    if (transaction == null) {
                        btnSave.isEnabled = true
                        Toast.makeText(
                            requireContext(),
                            "Transaction not found",
                            Toast.LENGTH_LONG
                        ).show()
                        return@getTransactionById
                    }

                    editingTransaction = transaction

                    spinnerType.setSelection(
                        transactionTypes.indexOf(transaction.type).coerceAtLeast(0)
                    )

                    updateCategories(transaction.category)

                    etAmount.setText(transaction.amount.toString())
                    etDate.setText(transaction.date)
                    etDescription.setText(transaction.description ?: "")

                    btnSave.isEnabled = true
                },
                onError = { message ->
                    if (!isAdded) return@getTransactionById

                    btnSave.isEnabled = true
                    Toast.makeText(
                        requireContext(),
                        message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        } else {
            updateCategories()
        }

        btnSave.setOnClickListener {
            val amountText = etAmount.text.toString().trim()
            val amount = amountText.toDoubleOrNull()
            val date = etDate.text.toString().trim()

            if (amount == null || amount <= 0.0 || !amount.isFinite()) {
                etAmount.error = "Enter a valid amount greater than zero"
                etAmount.requestFocus()
                return@setOnClickListener
            }

            if (date.isBlank()) {
                etDate.error = "Please select a date"
                return@setOnClickListener
            }

            val type = spinnerType.selectedItem.toString()
            val category = spinnerCategory.selectedItem.toString()
            val description = etDescription.text.toString().trim()
                .ifBlank { null }

            btnSave.isEnabled = false

            val existing = editingTransaction

            if (existing != null) {
                val updatedTransaction = existing.copy(
                    type = type,
                    amount = amount,
                    category = category,
                    date = date,
                    description = description
                )

                viewModel.updateTransaction(
                    transaction = updatedTransaction,
                    onSuccess = {
                        if (!isAdded) return@updateTransaction

                        Toast.makeText(
                            requireContext(),
                            "Transaction updated successfully!",
                            Toast.LENGTH_SHORT
                        ).show()

                        btnSave.isEnabled = true
                    },
                    onError = { message ->
                        if (!isAdded) return@updateTransaction

                        btnSave.isEnabled = true
                        Toast.makeText(
                            requireContext(),
                            message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            } else {
                viewModel.addTransaction(
                    type = type,
                    amount = amount,
                    category = category,
                    date = date,
                    description = description,
                    onSuccess = {
                        if (!isAdded) return@addTransaction

                        btnSave.isEnabled = true

                        Toast.makeText(
                            requireContext(),
                            "Transaction saved successfully!",
                            Toast.LENGTH_SHORT
                        ).show()

                        etAmount.text.clear()
                        etDescription.text.clear()
                    },
                    onError = { message ->
                        if (!isAdded) return@addTransaction

                        btnSave.isEnabled = true
                        Toast.makeText(
                            requireContext(),
                            message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
        }
    }
}
