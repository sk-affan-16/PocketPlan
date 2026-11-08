package com.example.pocketplan

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.pocketplan.data.local.database.DatabaseProvider
import com.example.pocketplan.data.local.entity.Transaction
import com.example.pocketplan.data.repository.PocketPlanRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTransactionFragment : Fragment(R.layout.transaction_form) {

    private var editingTransactionId: Long? = null

    private val dateFormat = SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.US
    )

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val tvTransactionTitle =
            view.findViewById<TextView>(R.id.tvTransactionTitle)

        val spinnerTransactionType =
            view.findViewById<Spinner>(R.id.spinnerTransactionType)

        val spinnerCategory =
            view.findViewById<Spinner>(R.id.spinnerCategory)

        val etAmount =
            view.findViewById<EditText>(R.id.etAmount)

        val etDate =
            view.findViewById<EditText>(R.id.etDate)

        val etDescription =
            view.findViewById<EditText>(R.id.etDescription)

        val btnSaveTransaction =
            view.findViewById<Button>(R.id.btnSaveTransaction)

        val database = DatabaseProvider.getDatabase(
            requireContext().applicationContext
        )

        val repository = PocketPlanRepository(
            transactionDao = database.transactionDao(),
            budgetDao = database.budgetDao()
        )

        editingTransactionId = arguments?.getLong("transactionId", -1L)?.takeIf { it > 0L }

        setupSpinners(
            spinnerTransactionType,
            spinnerCategory
        )

        setupDatePicker(etDate)

        if (editingTransactionId != null) {
            tvTransactionTitle.text = "Edit Transaction"
            btnSaveTransaction.text = "UPDATE TRANSACTION"

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val transaction = repository.getAllTransactions()
                        .first()
                        .find { it.id == editingTransactionId }

                    if (transaction == null) {
                        Toast.makeText(
                            requireContext(),
                            "Transaction not found",
                            Toast.LENGTH_LONG
                        ).show()

                        findNavController().popBackStack()
                        return@launch
                    }

                    spinnerTransactionType.setSelection(
                        if (transaction.type == "EXPENSE") 1 else 0
                    )

                    etAmount.setText(transaction.amount.toString())
                    etDate.setText(transaction.date)
                    etDescription.setText(
                        transaction.description.orEmpty()
                    )

                    val categoryIndex =
                        (0 until spinnerCategory.count)
                            .indexOfFirst {
                                spinnerCategory
                                    .getItemAtPosition(it)
                                    .toString() == transaction.category
                            }

                    if (categoryIndex >= 0) {
                        spinnerCategory.setSelection(categoryIndex)
                    }

                } catch (exception: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "Could not load transaction",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        btnSaveTransaction.setOnClickListener {

            val amountText = etAmount.text.toString().trim()
            val date = etDate.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (amountText.isEmpty()) {
                etAmount.error = "Enter an amount"
                etAmount.requestFocus()
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull()

            if (amount == null || !amount.isFinite() || amount <= 0.0) {
                etAmount.error =
                    "Enter a valid amount greater than zero"
                etAmount.requestFocus()
                return@setOnClickListener
            }

            if (date.isEmpty()) {
                etDate.error = "Select a date"
                return@setOnClickListener
            }

            val parsedDate = try {
                dateFormat.isLenient = false
                dateFormat.parse(date)
            } catch (_: Exception) {
                null
            } finally {
                dateFormat.isLenient = true
            }

            if (parsedDate == null || dateFormat.format(parsedDate) != date) {
                etDate.error = "Select a valid date"
                return@setOnClickListener
            }

            val transaction = Transaction(
                id = editingTransactionId ?: 0L,
                type = spinnerTransactionType.selectedItem.toString(),
                amount = amount,
                category = spinnerCategory.selectedItem.toString(),
                description = description.ifBlank { null },
                date = date
            )

            btnSaveTransaction.isEnabled = false

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    if (editingTransactionId != null) {
                        repository.updateTransaction(transaction)
                    } else {
                        repository.addTransaction(transaction)
                    }

                    Toast.makeText(
                        requireContext(),
                        if (editingTransactionId != null) {
                            "Transaction updated successfully"
                        } else {
                            "Transaction saved successfully"
                        },
                        Toast.LENGTH_SHORT
                    ).show()

                    findNavController().popBackStack()

                } catch (exception: Exception) {
                    btnSaveTransaction.isEnabled = true

                    Toast.makeText(
                        requireContext(),
                        "Could not save transaction",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun setupSpinners(
        spinnerTransactionType: Spinner,
        spinnerCategory: Spinner
    ) {
        val transactionTypes = listOf("INCOME", "EXPENSE")

        val typeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            transactionTypes
        )

        typeAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinnerTransactionType.adapter = typeAdapter

        val categories = listOf(
            "Salary",
            "Food",
            "Transport",
            "Shopping",
            "Bills",
            "Education",
            "Health",
            "Other"
        )

        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )

        categoryAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinnerCategory.adapter = categoryAdapter
    }

    private fun setupDatePicker(etDate: EditText) {

        if (editingTransactionId == null) {
            etDate.setText(
                dateFormat.format(Calendar.getInstance().time)
            )
        }

        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val currentDate = etDate.text.toString().trim()

            try {
                dateFormat.isLenient = false
                val parsedDate = dateFormat.parse(currentDate)

                if (parsedDate != null) {
                    calendar.time = parsedDate
                }
            } catch (_: Exception) {
                // Use today's date.
            } finally {
                dateFormat.isLenient = true
            }

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
    }
}
