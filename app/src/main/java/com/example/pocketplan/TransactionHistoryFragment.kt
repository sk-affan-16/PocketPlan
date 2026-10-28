package com.example.pocketplan

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.pocketplan.data.local.database.DatabaseProvider
import com.example.pocketplan.data.local.entity.Transaction
import com.example.pocketplan.data.repository.PocketPlanRepository
import kotlinx.coroutines.launch
import java.util.Locale

class TransactionHistoryFragment :
    Fragment(R.layout.fragment_transaction_history) {

    private lateinit var transactionListContainer: LinearLayout
    private lateinit var tvEmptyTransactions: TextView

    private val repository: PocketPlanRepository by lazy {
        val database = DatabaseProvider.getDatabase(requireContext())

        PocketPlanRepository(
            database.transactionDao(),
            database.budgetDao()
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        transactionListContainer =
            view.findViewById(R.id.transactionListContainer)

        tvEmptyTransactions =
            view.findViewById(R.id.tvEmptyTransactions)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.getAllTransactions().collect { transactions ->
                    displayTransactions(transactions)
                }
            }
        }
    }

    private fun displayTransactions(
        transactions: List<Transaction>
    ) {
        transactionListContainer.removeAllViews()

        if (transactions.isEmpty()) {
            tvEmptyTransactions.visibility = View.VISIBLE
            return
        }

        tvEmptyTransactions.visibility = View.GONE

        transactions.forEach { transaction ->

            val itemLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
            }

            val typeText = TextView(requireContext()).apply {
                text = transaction.type
                textSize = 18f
            }

            val amountText = TextView(requireContext()).apply {
                text = String.format(
                    Locale.getDefault(),
                    "Amount: %.2f",
                    transaction.amount
                )
                textSize = 16f
            }

            val categoryText = TextView(requireContext()).apply {
                text = "Category: ${transaction.category}"
                textSize = 14f
            }

            val dateText = TextView(requireContext()).apply {
                text = "Date: ${transaction.date}"
                textSize = 14f
            }

            itemLayout.addView(typeText)
            itemLayout.addView(amountText)
            itemLayout.addView(categoryText)
            itemLayout.addView(dateText)

            transaction.description
                ?.takeIf { it.isNotBlank() }
                ?.let { description ->

                    val descriptionText = TextView(requireContext()).apply {
                        text = "Description: $description"
                        textSize = 14f
                    }

                    itemLayout.addView(descriptionText)
                }

            transactionListContainer.addView(itemLayout)

            val divider = View(requireContext()).apply {
                setBackgroundColor(0xFFDDDDDD.toInt())
            }

            transactionListContainer.addView(
                divider,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
                )
            )
        }
    }
}