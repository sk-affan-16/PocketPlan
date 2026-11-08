package com.example.pocketplan

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.pocketplan.data.local.database.DatabaseProvider
import com.example.pocketplan.data.repository.PocketPlanRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTransactionFragment : Fragment(R.layout.fragment_add_transaction) {

    private lateinit var viewModel: TransactionViewModel

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

        val transactionId = arguments
            ?.takeIf { it.containsKey("transactionId") }
            ?.getLong("transactionId", -1L) ?: -1L

        val isEditing = transactionId > 0L

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
            val categories = if (spinnerType.selectedItem?.toString() == "INCOME") {
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
                    selectedView: View?,
                    position: Int,
                    id: Long
                ) {
                    val currentCategory =
                        spinnerCategory.selectedItem?.toString()
                    updateCategories(currentCategory)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        if (!isEditing) {
            etDate.setText(dateFormat.format(Calendar.getInstance().time))
            updateCategories()
        } else {
            btnSave.text = "Update Transaction"
            btnSave.isEnabled = false

            viewModel.getTransactionById(
                transactionId = transactionId,
                onResult = { transaction ->
                    if (isAdded) {
                        spinnerType.setSelection(
                            transactionTypes.indexOf(transaction.type)
                                .takeIf { it >= 0 } ?: 0
                        )
                        updateCategories(transaction.category)
                        etAmount.setText(transaction.amount.toString())
                        etDate.setText(transaction.date)
                        etDescription.setText(transaction.description.orEmpty())
                        btnSave.isEnabled = true
                    }
                },
                onError = { message ->
                    if (isAdded) {
                        btnSave.isEnabled = false
                        Toast.makeText(
                            requireContext(),
                            message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )
        }

        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()

            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    etDate.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnSave.setOnClickListener {
            val amount = etAmount.text.toString().trim().toDoubleOrNull()
            val date = etDate.text.toString().trim()

            if (amount == null || amount <= 0.0) {
                etAmount.error = "Enter an amount greater than zero"
                etAmount.requestFocus()
                return@setOnClickListener
            }

            if (date.isBlank()) {
                etDate.error = "Please select a date"
                return@setOnClickListener
            }

            val type = spinnerType.selectedItem.toString()
            val category = spinnerCategory.selectedItem?.toString().orEmpty()
            val description = etDescription.text.toString()
                .trim()
                .ifBlank { null }

            btnSave.isEnabled = false

            val onSuccess: () -> Unit = {
                if (isAdded) {
                    Toast.makeText(
                        requireContext(),
                        if (isEditing) "Transaction updated successfully!"
                        else "Transaction saved successfully!",
                        Toast.LENGTH_SHORT
                    ).show()

                    if (isEditing) {
                        findNavController().popBackStack()
                    } else {
                        btnSave.isEnabled = true
                        etAmount.text.clear()
                        etDescription.text.clear()
                    }
                }
            }

            val onError: (String) -> Unit = { message ->
                if (isAdded) {
                    btnSave.isEnabled = true
                    Toast.makeText(
                        requireContext(),
                        message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            if (isEditing) {
                viewModel.updateTransaction(
                    transactionId,
                    type,
                    amount,
                    category,
                    date,
                    description,
                    onSuccess,
                    onError
                )
            } else {
                viewModel.addTransaction(
                    type,
                    amount,
                    category,
                    date,
                    description,
                    onSuccess,
                    onError
                )
            }
        }
    }
}
