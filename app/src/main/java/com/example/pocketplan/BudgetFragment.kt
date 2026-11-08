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

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val etMonthlyBudget =
            view.findViewById<EditText>(R.id.etMonthlyBudget)

        val tvCurrentBudget =
            view.findViewById<TextView>(R.id.tvCurrentBudget)

        val tvRemainingBudget =
            view.findViewById<TextView>(R.id.tvRemainingBudget)

        val btnSaveBudget =
            view.findViewById<Button>(R.id.btnSaveBudget)

        val database = DatabaseProvider.getDatabase(
            requireContext().applicationContext
        )

        val repository = PocketPlanRepository(
            transactionDao = database.transactionDao(),
            budgetDao = database.budgetDao()
        )

        val currentMonth = SimpleDateFormat(
            "yyyy-MM",
            Locale.getDefault()
        ).format(Date())

        viewLifecycleOwner.lifecycleScope.launch {
            combine(
                repository.getMonthlyBudget(currentMonth),
                repository.getExpenseTotal(currentMonth)
            ) { budget, expenses ->
                budget to expenses
            }.collect { (budget, expenses) ->

                val budgetAmount = budget?.amount ?: 0.0
                val remaining = budgetAmount - expenses

                tvCurrentBudget.text =
                    "Monthly Budget: ${formatAmount(budgetAmount)}"

                tvRemainingBudget.text =
                    "Remaining Budget: ${formatAmount(remaining)}"

                if (budget != null) {
                    etMonthlyBudget.setText(
                        budget.amount.toString()
                    )
                }
            }
        }

        btnSaveBudget.setOnClickListener {

            val amountText = etMonthlyBudget.text.toString().trim()
            val amount = amountText.toDoubleOrNull()

            if (amount == null || !amount.isFinite() || amount <= 0) {
                etMonthlyBudget.error =
                    "Enter a valid budget greater than zero"
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                repository.saveMonthlyBudget(
                    month = currentMonth,
                    amount = amount
                )

                Toast.makeText(
                    requireContext(),
                    "Monthly budget saved successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun formatAmount(amount: Double): String {
        return String.format(
            Locale.US,
            "\u20B9%.2f",
            amount
        )
    }
}
