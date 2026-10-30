
package com.example.pocketplan

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.pocketplan.data.local.database.DatabaseProvider
import com.example.pocketplan.data.local.entity.Transaction
import com.example.pocketplan.data.repository.PocketPlanRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Locale

class TransactionHistoryFragment :
    Fragment(R.layout.fragment_transaction_history) {

    private lateinit var transactionListContainer: LinearLayout
    private lateinit var tvEmptyTransactions: TextView
    private lateinit var etSearchTransactions: EditText
    private lateinit var spinnerTypeFilter: Spinner
    private lateinit var spinnerCategoryFilter: Spinner
    private lateinit var btnClearFilters: Button

    private var filterJob: Job? = null

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

        etSearchTransactions =
            view.findViewById(R.id.etSearchTransactions)

        spinnerTypeFilter =
            view.findViewById(R.id.spinnerTypeFilter)

        spinnerCategoryFilter =
            view.findViewById(R.id.spinnerCategoryFilter)

        btnClearFilters =
            view.findViewById(R.id.btnClearFilters)

        setupFilters()
    }

    private fun setupFilters() {

        val typeOptions = listOf(
            "All Types",
            "INCOME",
            "EXPENSE"
        )

        val categoryOptions = listOf(
            "All Categories",
            "Salary",
            "Business",
            "Investment",
            "Gift",
            "Food",
            "Transport",
            "Shopping",
            "Bills",
            "Education",
            "Health",
            "Entertainment",
            "Other"
        )

        spinnerTypeFilter.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            typeOptions
        ).also {
            it.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
            )
        }

        spinnerCategoryFilter.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categoryOptions
        ).also {
            it.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
            )
        }

        val filterListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                loadFilteredTransactions()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                loadFilteredTransactions()
            }
        }

        spinnerTypeFilter.onItemSelectedListener = filterListener
        spinnerCategoryFilter.onItemSelectedListener = filterListener

        etSearchTransactions.addTextChangedListener(
            object : android.text.TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) = Unit

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    loadFilteredTransactions()
                }

                override fun afterTextChanged(s: android.text.Editable?) = Unit
            }
        )

        btnClearFilters.setOnClickListener {
            etSearchTransactions.text.clear()
            spinnerTypeFilter.setSelection(0)
            spinnerCategoryFilter.setSelection(0)
            loadFilteredTransactions()
        }

        loadFilteredTransactions()
    }

    private fun loadFilteredTransactions() {

        val searchQuery =
            etSearchTransactions.text.toString().trim()

        val selectedType =
            when (spinnerTypeFilter.selectedItemPosition) {
                1 -> "INCOME"
                2 -> "EXPENSE"
                else -> null
            }

        val selectedCategory =
            spinnerCategoryFilter.selectedItem
                ?.toString()
                ?.takeUnless { it == "All Categories" }

        filterJob?.cancel()

        filterJob = viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.searchAndFilterTransactions(
                    searchQuery = searchQuery,
                    type = selectedType,
                    category = selectedCategory
                ).collect { transactions ->
                    displayTransactions(transactions)
                }
            }
        }
    }

    private fun displayTransactions(transactions: List<Transaction>) {
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

            val buttonLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            val editButton = Button(requireContext()).apply {
                text = "Edit"
                isAllCaps = false
                setOnClickListener {
                    val arguments = bundleOf(
                        "transactionId" to transaction.id
                    )
                    findNavController().navigate(
                        R.id.addTransactionFragment,
                        arguments
                    )
                }
            }

            val deleteButton = Button(requireContext()).apply {
                text = "Delete"
                isAllCaps = false
                setOnClickListener {
                    showDeleteConfirmation(transaction)
                }
            }

            buttonLayout.addView(
                editButton,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            buttonLayout.addView(
                deleteButton,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            itemLayout.addView(buttonLayout)
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

    private fun showDeleteConfirmation(transaction: Transaction) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        repository.deleteTransaction(transaction)

                        if (isAdded) {
                            Toast.makeText(
                                requireContext(),
                                "Transaction deleted successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (exception: Exception) {
                        if (isAdded) {
                            Toast.makeText(
                                requireContext(),
                                exception.message
                                    ?: "Unable to delete transaction",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
            .show()
    }
}
