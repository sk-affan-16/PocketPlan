
package com.example.pocketplan

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.pocketplan.data.local.database.DatabaseProvider
import com.example.pocketplan.data.repository.PocketPlanRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BudgetFragment : Fragment(R.layout.fragment_budget) {

    private lateinit var repository: PocketPlanRepository

    private lateinit var etMonthlyBudget: EditText
    private lateinit var btnSaveBudget: Button
    private lateinit var tvBudgetAmount: TextView
    private lateinit var tvBudgetExpense: TextView
    private lateinit var tvBudgetRemaining: TextView
    private lateinit var tvBudgetMessage: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = DatabaseProvider.getDatabase(
            requireContext().applicationContext
        )

        repository = PocketPlanRepository(
            transactionDao = database.transactionDao(),
            budgetDao = database.budgetDao()
        )

        etMonthlyBudget = view.findViewById(R.id.etMonthlyBudget)
        btnSaveBudget = view.findViewById(R.id.btnSaveBudget)
        tvBudgetAmount = view.findViewById(R.id.tvBudgetAmount)
        tvBudgetExpense = view.findViewById(R.id.tvBudgetExpense)
        tvBudgetRemaining = view.findViewById(R.id.tvBudgetRemaining)
        tvBudgetMessage = view.findViewById(R.id.tvBudgetMessage)

        btnSaveBudget.setOnClickListener {
            saveBudget()
        }

        observeBudget()
    }

    private fun observeBudget() {
        val currentMonth = getCurrentMonth()

        viewLifecycleOwner.lifecycleScope.launch {
            combine(
                repository.getMonthlyBudget(currentMonth),
                repository.getExpenseTotal(currentMonth)
            ) { budget, expense ->
                (budget?.amount ?: 0.0) to expense
            }.collect { (budget, expense) ->

                val remaining = budget - expense

                tvBudgetAmount.text =
                    "Monthly Budget: ${formatAmount(budget)}"

                tvBudgetExpense.text =
                    "Total Expense: ${formatAmount(expense)}"

                tvBudgetRemaining.text =
                    "Remaining Budget: ${formatAmount(remaining)}"

                tvBudgetMessage.text = when {
                    budget <= 0.0 ->
                        "Please set your monthly budget."

                    remaining < 0.0 ->
                        "You have exceeded your monthly budget!"

                    else ->
                        "You are within your monthly budget."
                }
            }
        }
    }

    private fun saveBudget() {
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

        btnSaveBudget.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                repository.saveMonthlyBudget(
                    month = getCurrentMonth(),
                    amount = amount
                )

                etMonthlyBudget.text.clear()

                Toast.makeText(
                    requireContext(),
                    "Monthly budget saved successfully",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (exception: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Could not save budget. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                btnSaveBudget.isEnabled = true
            }
        }
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
