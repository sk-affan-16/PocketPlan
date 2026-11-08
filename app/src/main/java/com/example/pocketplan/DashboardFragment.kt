
package com.example.pocketplan

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.pocketplan.data.local.database.DatabaseProvider
import com.example.pocketplan.data.repository.PocketPlanRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private lateinit var repository: PocketPlanRepository

    private lateinit var tvBalance: TextView
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpense: TextView
    private lateinit var tvMonthlyBudget: TextView
    private lateinit var tvRemainingBudget: TextView
    private lateinit var tvEmptyState: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = DatabaseProvider.getDatabase(
            requireContext().applicationContext
        )

        repository = PocketPlanRepository(
            transactionDao = database.transactionDao(),
            budgetDao = database.budgetDao()
        )

        tvBalance = view.findViewById(R.id.tvBalance)
        tvTotalIncome = view.findViewById(R.id.tvTotalIncome)
        tvTotalExpense = view.findViewById(R.id.tvTotalExpense)
        tvMonthlyBudget = view.findViewById(R.id.tvMonthlyBudget)
        tvRemainingBudget = view.findViewById(R.id.tvRemainingBudget)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)

        view.findViewById<Button>(R.id.btnAddTransaction)
            .setOnClickListener {
                findNavController().navigate(
                    R.id.action_dashboardFragment_to_addTransactionFragment
                )
            }

        view.findViewById<Button>(R.id.btnTransactionHistory)
            .setOnClickListener {
                findNavController().navigate(
                    R.id.action_dashboardFragment_to_transactionHistoryFragment
                )
            }

        view.findViewById<Button>(R.id.btnOpenBudget)
            .setOnClickListener {
                findNavController().navigate(
                    R.id.action_dashboardFragment_to_budgetFragment
                )
            }

        observeDashboard()
    }

    private fun observeDashboard() {
        val currentMonth = getCurrentMonth()

        viewLifecycleOwner.lifecycleScope.launch {
            combine(
                repository.getIncomeTotal(currentMonth),
                repository.getExpenseTotal(currentMonth),
                repository.getMonthlyBudget(currentMonth)
            ) { income, expense, budget ->

                Triple(income, expense, budget?.amount ?: 0.0)

            }.collect { (income, expense, budget) ->

                tvTotalIncome.text = formatAmount(income)
                tvTotalExpense.text = formatAmount(expense)
                tvBalance.text = formatAmount(income - expense)

                tvMonthlyBudget.text =
                    "Monthly Budget: ${formatAmount(budget)}"

                tvRemainingBudget.text =
                    "Remaining Budget: ${formatAmount(budget - expense)}"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repository.getMonthlyTransactions(currentMonth)
                .collect { transactions ->

                    tvEmptyState.visibility =
                        if (transactions.isEmpty()) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
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
