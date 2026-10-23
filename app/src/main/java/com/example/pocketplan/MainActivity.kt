
package com.example.pocketplan

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.pocketplan.data.local.database.DatabaseProvider
import com.example.pocketplan.data.repository.PocketPlanRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var repository: PocketPlanRepository

    private lateinit var tvBalance: TextView
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpense: TextView
    private lateinit var tvMonthlyBudget: TextView
    private lateinit var tvRemainingBudget: TextView
    private lateinit var tvEmptyState: TextView

    private lateinit var etMonthlyBudget: EditText
    private lateinit var btnSaveBudget: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnAddTransaction)
            .setOnClickListener {
                startActivity(
                    Intent(this, AddTransactionActivity::class.java)
                )
            }

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { view, insets ->

            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        tvBalance = findViewById(R.id.tvBalance)
        tvTotalIncome = findViewById(R.id.tvTotalIncome)
        tvTotalExpense = findViewById(R.id.tvTotalExpense)
        tvMonthlyBudget = findViewById(R.id.tvMonthlyBudget)
        tvRemainingBudget = findViewById(R.id.tvRemainingBudget)
        tvEmptyState = findViewById(R.id.tvEmptyState)

        etMonthlyBudget = findViewById(R.id.etMonthlyBudget)
        btnSaveBudget = findViewById(R.id.btnSaveBudget)

        val database =
            DatabaseProvider.getDatabase(applicationContext)

        repository = PocketPlanRepository(
            transactionDao = database.transactionDao(),
            budgetDao = database.budgetDao()
        )

        btnSaveBudget.setOnClickListener {
            saveMonthlyBudget()
        }

        loadDashboard()
    }

    private fun loadDashboard() {

        val currentMonth = getCurrentMonth()

        lifecycleScope.launch {
            repository.getIncomeTotal(currentMonth).collect { income ->
                tvTotalIncome.text = formatAmount(income)
                updateBalance()
            }
        }

        lifecycleScope.launch {
            repository.getExpenseTotal(currentMonth).collect { expense ->
                tvTotalExpense.text = formatAmount(expense)
                updateBalance()
            }
        }

        lifecycleScope.launch {
            repository.getMonthlyBudget(currentMonth).collect { budget ->

                val budgetAmount = budget?.amount ?: 0.0

                tvMonthlyBudget.text = formatAmount(budgetAmount)

                val expense = repository
                    .getExpenseTotal(currentMonth)
                    .first()

                val remaining = budgetAmount - expense

                tvRemainingBudget.text = formatAmount(remaining)
            }
        }

        lifecycleScope.launch {
            repository.getMonthlyTransactions(currentMonth)
                .collect { transactions ->

                    tvEmptyState.visibility =
                        if (transactions.isEmpty()) {
                            TextView.VISIBLE
                        } else {
                            TextView.GONE
                        }
                }
        }
    }

    private fun saveMonthlyBudget() {

        val input = etMonthlyBudget.text.toString().trim()
        val amount = input.toDoubleOrNull()

        if (
            input.isEmpty() ||
            amount == null ||
            !amount.isFinite() ||
            amount <= 0.0
        ) {
            etMonthlyBudget.error =
                "Enter an amount greater than zero"

            etMonthlyBudget.requestFocus()
            return
        }

        val currentMonth = getCurrentMonth()

        btnSaveBudget.isEnabled = false

        lifecycleScope.launch {
            try {
                repository.saveMonthlyBudget(
                    month = currentMonth,
                    amount = amount
                )

                etMonthlyBudget.text.clear()

                Toast.makeText(
                    this@MainActivity,
                    "Monthly budget saved successfully",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {

                Toast.makeText(
                    this@MainActivity,
                    "Could not save budget. Please try again.",
                    Toast.LENGTH_LONG
                ).show()

            } finally {
                btnSaveBudget.isEnabled = true
            }
        }
    }

    private suspend fun updateBalance() {

        val currentMonth = getCurrentMonth()

        val income = repository
            .getIncomeTotal(currentMonth)
            .first()

        val expense = repository
            .getExpenseTotal(currentMonth)
            .first()

        val balance = income - expense

        tvBalance.text = formatAmount(balance)
    }

    private fun getCurrentMonth(): String {
        return SimpleDateFormat(
            "yyyy-MM",
            Locale.getDefault()
        ).format(Date())
    }

    private fun formatAmount(amount: Double): String {
        return String.format(
            Locale.US,
            "\u20B9%.2f",
            amount
        )
    }
}
