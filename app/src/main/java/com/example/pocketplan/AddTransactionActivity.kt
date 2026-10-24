
package com.example.pocketplan

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.pocketplan.data.local.database.DatabaseProvider
import com.example.pocketplan.data.local.entity.Transaction
import com.example.pocketplan.data.repository.PocketPlanRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var repository: PocketPlanRepository

    private var editingTransactionId: Long? = null

    private lateinit var tvTransactionTitle: TextView
    private lateinit var spinnerTransactionType: Spinner
    private lateinit var spinnerCategory: Spinner
    private lateinit var etAmount: EditText
    private lateinit var etDate: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnSaveTransaction: Button

    private val dateFormat = SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.US
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.transaction_form)

        editingTransactionId = if (intent.hasExtra("transaction_id")) {
            intent.getLongExtra("transaction_id", -1L)
                .takeIf { it > 0L }
        } else {
            null
        }

        tvTransactionTitle = findViewById(R.id.tvTransactionTitle)

        val database = DatabaseProvider.getDatabase(applicationContext)

        repository = PocketPlanRepository(
            transactionDao = database.transactionDao(),
            budgetDao = database.budgetDao()
        )

        spinnerTransactionType =
            findViewById(R.id.spinnerTransactionType)

        spinnerCategory =
            findViewById(R.id.spinnerCategory)

        etAmount = findViewById(R.id.etAmount)
        etDate = findViewById(R.id.etDate)
        etDescription = findViewById(R.id.etDescription)

        btnSaveTransaction =
            findViewById(R.id.btnSaveTransaction)

        setupSpinners()
        setupDatePicker()

        if (editingTransactionId != null) {
            tvTransactionTitle.text = "Edit Transaction"
            btnSaveTransaction.text = "UPDATE TRANSACTION"
            loadTransactionForEditing()
        }

        btnSaveTransaction.setOnClickListener {
            saveTransaction()
        }
    }

    private fun setupSpinners() {
        val transactionTypes = listOf(
            "INCOME",
            "EXPENSE"
        )

        val typeAdapter = ArrayAdapter(
            this,
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
            this,
            android.R.layout.simple_spinner_item,
            categories
        )

        categoryAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinnerCategory.adapter = categoryAdapter
    }

    private fun setupDatePicker() {
        if (editingTransactionId == null) {
            etDate.setText(dateFormat.format(Calendar.getInstance().time))
        }

        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()

            val currentDate = etDate.text.toString().trim()

            if (currentDate.isNotEmpty()) {
                try {
                    dateFormat.isLenient = false
                    val parsedDate = dateFormat.parse(currentDate)

                    if (parsedDate != null) {
                        calendar.time = parsedDate
                    }
                } catch (_: Exception) {
                    // Use today's date if the existing date is invalid.
                } finally {
                    dateFormat.isLenient = true
                }
            }

            DatePickerDialog(
                this,
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

    private fun loadTransactionForEditing() {
        lifecycleScope.launch {
            try {
                val id = editingTransactionId ?: return@launch

                val transaction = repository.getAllTransactions()
                    .first()
                    .find { item -> item.id == id }

                if (transaction == null) {
                    Toast.makeText(
                        this@AddTransactionActivity,
                        "Transaction not found",
                        Toast.LENGTH_LONG
                    ).show()

                    finish()
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

                val categoryIndex = (0 until spinnerCategory.count)
                    .indexOfFirst { index ->
                        spinnerCategory.getItemAtPosition(index).toString() ==
                                transaction.category
                    }

                if (categoryIndex >= 0) {
                    spinnerCategory.setSelection(categoryIndex)
                }

            } catch (exception: Exception) {
                Toast.makeText(
                    this@AddTransactionActivity,
                    "Could not load transaction",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun saveTransaction() {
        val amountText = etAmount.text.toString().trim()
        val date = etDate.text.toString().trim()
        val description = etDescription.text.toString().trim()

        if (amountText.isEmpty()) {
            etAmount.error = "Enter an amount"
            etAmount.requestFocus()
            return
        }

        val amount = amountText.toDoubleOrNull()

        if (amount == null || !amount.isFinite() || amount <= 0.0) {
            etAmount.error = "Enter a valid amount greater than zero"
            etAmount.requestFocus()
            return
        }

        if (date.isEmpty()) {
            etDate.error = "Select a date"
            return
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
            return
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

        lifecycleScope.launch {
            try {
                if (editingTransactionId != null) {
                    repository.updateTransaction(transaction)
                } else {
                    repository.addTransaction(transaction)
                }

                Toast.makeText(
                    this@AddTransactionActivity,
                    if (editingTransactionId != null) {
                        "Transaction updated successfully"
                    } else {
                        "Transaction saved successfully"
                    },
                    Toast.LENGTH_SHORT
                ).show()

                finish()

            } catch (exception: Exception) {
                btnSaveTransaction.isEnabled = true

                Toast.makeText(
                    this@AddTransactionActivity,
                    if (editingTransactionId != null) {
                        "Could not update transaction"
                    } else {
                        "Could not save transaction"
                    },
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
