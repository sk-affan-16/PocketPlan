package com.example.pocketplan

import android.os.Bundle
import android.widget.TextView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
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

        val database = DatabaseProvider.getDatabase(applicationContext)

        repository = PocketPlanRepository(
            transactionDao = database.transactionDao(),
            budgetDao = database.budgetDao()
        )

        loadDashboard()
    }

    private fun loadDashboard() {

        val currentMonth = SimpleDateFormat(
            "yyyy-MM",
            Locale.getDefault()
        ).format(Date())

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

            repository.getMonthlyTransactions(currentMonth).collect { transactions ->

                tvEmptyState.visibility =
                    if (transactions.isEmpty()) {
                        TextView.VISIBLE
                    } else {
                        TextView.GONE
                    }
            }
        }
    }

    private suspend fun updateBalance() {

        val currentMonth = SimpleDateFormat(
            "yyyy-MM",
            Locale.getDefault()
        ).format(Date())

        val income = repository
            .getIncomeTotal(currentMonth)
            .first()

        val expense = repository
            .getExpenseTotal(currentMonth)
            .first()

        val balance = income - expense

        tvBalance.text = formatAmount(balance)
    }

    private fun formatAmount(amount: Double): String {

        return String.format(
            Locale.getDefault(),
            "₹%.2f",
            amount
        )
    }
}