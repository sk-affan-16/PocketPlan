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

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val tvBalance = view.findViewById<TextView>(R.id.tvBalance)
        val tvTotalIncome = view.findViewById<TextView>(R.id.tvTotalIncome)
        val tvTotalExpense = view.findViewById<TextView>(R.id.tvTotalExpense)
        val tvEmptyState = view.findViewById<TextView>(R.id.tvEmptyState)

        val btnAddTransaction =
            view.findViewById<Button>(R.id.btnAddTransaction)

        val btnTransactionHistory =
            view.findViewById<Button>(R.id.btnTransactionHistory)

        val btnOpenBudget =
            view.findViewById<Button>(R.id.btnOpenBudget)

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

        btnAddTransaction.setOnClickListener {
            findNavController().navigate(
                R.id.action_dashboardFragment_to_addTransactionFragment
            )
        }

        btnTransactionHistory.setOnClickListener {
            findNavController().navigate(
                R.id.action_dashboardFragment_to_transactionHistoryFragment
            )
        }

        btnOpenBudget.setOnClickListener {
            findNavController().navigate(
                R.id.action_dashboardFragment_to_budgetFragment
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            combine(
                repository.getIncomeTotal(currentMonth),
                repository.getExpenseTotal(currentMonth)
            ) { income, expense ->
                income to expense
            }.collect { (income, expense) ->

                tvTotalIncome.text = formatAmount(income)
                tvTotalExpense.text = formatAmount(expense)
                tvBalance.text = formatAmount(income - expense)
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

    private fun formatAmount(amount: Double): String {
        return String.format(
            Locale.US,
            "\u20B9%.2f",
            amount
        )
    }
}
