package com.example.pocketplan

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.pocketplan.data.local.database.DatabaseProvider
import com.example.pocketplan.data.local.entity.Transaction
import com.example.pocketplan.data.repository.PocketPlanRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Locale

class TransactionHistoryFragment :
    Fragment(R.layout.fragment_transaction_history) {

    private var searchJob: Job? = null
    private lateinit var repository: PocketPlanRepository
    private lateinit var transactionList: LinearLayout
    private lateinit var tvHistoryEmpty: TextView
    private lateinit var etSearchTransactions: EditText
    private lateinit var spinnerTypeFilter: Spinner
    private lateinit var spinnerCategoryFilter: Spinner

    private val categories = listOf(
        "All categories",
        "Salary",
        "Food",
        "Transport",
        "Shopping",
        "Bills",
        "Education",
        "Health",
        "Other"
    )

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        etSearchTransactions =
            view.findViewById(R.id.etSearchTransactions)

        spinnerTypeFilter =
            view.findViewById(R.id.spinnerTypeFilter)

        spinnerCategoryFilter =
            view.findViewById(R.id.spinnerCategoryFilter)

        tvHistoryEmpty =
            view.findViewById(R.id.tvHistoryEmpty)

        transactionList =
            view.findViewById(R.id.transactionList)

        val database = DatabaseProvider.getDatabase(
            requireContext().applicationContext
        )

        repository = PocketPlanRepository(
            transactionDao = database.transactionDao(),
            budgetDao = database.budgetDao()
        )

        setupFilters()
        setupSearch()

        loadTransactions()
    }

    private fun setupFilters() {

        val types = listOf(
            "All types",
            "INCOME",
            "EXPENSE"
        )

        val typeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            types
        )

        typeAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinnerTypeFilter.adapter = typeAdapter

        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )

        categoryAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinnerCategoryFilter.adapter = categoryAdapter

        val filterListener = object :
            android.widget.AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: android.widget.AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                loadTransactions()
            }

            override fun onNothingSelected(
                parent: android.widget.AdapterView<*>?
            ) {
                // No action needed.
            }
        }

        spinnerTypeFilter.onItemSelectedListener = filterListener
        spinnerCategoryFilter.onItemSelectedListener = filterListener
    }

    private fun setupSearch() {

        etSearchTransactions.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    // No action needed.
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    loadTransactions()
                }

                override fun afterTextChanged(s: Editable?) {
                    // No action needed.
                }
            }
        )
    }

    private fun loadTransactions() {

        if (!isAdded) return

        val searchQuery =
            etSearchTransactions.text.toString().trim()

        val selectedType =
            spinnerTypeFilter.selectedItem?.toString()

        val selectedCategory =
            spinnerCategoryFilter.selectedItem?.toString()

        val type = when (selectedType) {
            "INCOME", "EXPENSE" -> selectedType
            else -> null
        }

        val category = when (selectedCategory) {
            null, "All categories" -> null
            else -> selectedCategory
        }

        searchJob?.cancel()

        searchJob = viewLifecycleOwner.lifecycleScope.launch {

            repository.searchAndFilterTransactions(
                searchQuery = searchQuery,
                type = type,
                category = category
            ).collect { transactions ->

                displayTransactions(transactions)
            }
        }
    }

    private fun displayTransactions(
        transactions: List<Transaction>
    ) {

        transactionList.removeAllViews()

        tvHistoryEmpty.visibility =
            if (transactions.isEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }

        transactions.forEach { transaction ->

            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(12), dp(12), dp(12), dp(12))
            }

            val title = TextView(requireContext()).apply {
                text = "${transaction.category} - ${transaction.type}"
                textSize = 18f
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            val amount = TextView(requireContext()).apply {
                text = "Amount: ${formatAmount(transaction.amount)}"
                textSize = 16f
            }

            val date = TextView(requireContext()).apply {
                text = "Date: ${transaction.date}"
                textSize = 14f
            }

            val description = TextView(requireContext()).apply {
                text = "Description: ${
                    transaction.description?.takeIf { it.isNotBlank() }
                        ?: "No description"
                }"
                textSize = 14f
            }

            val buttonRow = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            val editButton = Button(requireContext()).apply {
                text = "EDIT"
                setOnClickListener {
                    editTransaction(transaction)
                }
            }

            val deleteButton = Button(requireContext()).apply {
                text = "DELETE"
                setOnClickListener {
                    confirmDelete(transaction)
                }
            }

            buttonRow.addView(
                editButton,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            buttonRow.addView(
                deleteButton,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            row.addView(title)
            row.addView(amount)
            row.addView(date)
            row.addView(description)
            row.addView(buttonRow)

            transactionList.addView(
                row,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dp(12)
                }
            )

            val divider = View(requireContext()).apply {
                setBackgroundColor(0xFFCCCCCC.toInt())
            }

            transactionList.addView(
                divider,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(1)
                )
            )
        }
    }

    private fun editTransaction(transaction: Transaction) {

        val arguments = Bundle().apply {
            putLong("transactionId", transaction.id)
        }

        findNavController().navigate(
            R.id.addTransactionFragment,
            arguments
        )
    }

    private fun confirmDelete(transaction: Transaction) {

        AlertDialog.Builder(requireContext())
            .setTitle("Delete Transaction")
            .setMessage(
                "Are you sure you want to delete this transaction?"
            )
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("DELETE") { _, _ ->

                viewLifecycleOwner.lifecycleScope.launch {

                    try {
                        repository.deleteTransaction(transaction)

                        android.widget.Toast.makeText(
                            requireContext(),
                            "Transaction deleted successfully",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()

                    } catch (exception: Exception) {

                        android.widget.Toast.makeText(
                            requireContext(),
                            "Could not delete transaction",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .show()
    }

    private fun formatAmount(amount: Double): String {
        return String.format(
            Locale.US,
            "\u20B9%.2f",
            amount
        )
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    override fun onDestroyView() {
        searchJob?.cancel()
        super.onDestroyView()
    }
}
