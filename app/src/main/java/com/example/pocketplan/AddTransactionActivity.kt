
package com.example.pocketplan

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.pocketplan.data.local.database.DatabaseProvider
import com.example.pocketplan.data.local.entity.Transaction
import com.example.pocketplan.data.repository.PocketPlanRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var repository: PocketPlanRepository

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

        val database =
            DatabaseProvider.getDatabase(applicationContext)

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

        etDate.setText(dateFormat.format(Calendar.getInstance().time))

        etDate.setOnClickListener {

            val calendar = Calendar.getInstance()

            DatePickerDialog(
                this,
                { _, year, month, day ->

                    calendar.set(year, month, day)

                    etDate.setText(
                        dateFormat.format(calendar.time)
                    )
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun saveTransaction() {

        val amountText = etAmount.text.toString().trim()
        val date = etDate.text.toString().trim()
        val description =
            etDescription.text.toString().trim()

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

        val transaction = Transaction(
            type = spinnerTransactionType.selectedItem.toString(),
            amount = amount,
            category = spinnerCategory.selectedItem.toString(),
            description = description.ifBlank { null },
            date = date
        )

        btnSaveTransaction.isEnabled = false

        lifecycleScope.launch {

            try {
                repository.addTransaction(transaction)

                Toast.makeText(
                    this@AddTransactionActivity,
                    "Transaction saved successfully",
                    Toast.LENGTH_SHORT
                ).show()

                finish()

            } catch (exception: Exception) {

                btnSaveTransaction.isEnabled = true

                Toast.makeText(
                    this@AddTransactionActivity,
                    "Could not save transaction",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
