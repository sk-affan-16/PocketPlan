
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.pocketplan.data.local.database.DatabaseProvider
import com.example.pocketplan.data.local.entity.Transaction
import com.example.pocketplan.data.repository.PocketPlanRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
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

    private val filterState =
        MutableStateFlow(Triple("", null as String?, null as String?))

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

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
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
                updateFilterState()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                updateFilterState()
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
                    updateFilterState()
                }

                override fun afterTextChanged(
                    s: android.text.Editable?
                ) = Unit
            }
        )

        btnClearFilters.setOnClickListener {
            etSearchTransactions.text.clear()
            spinnerTypeFilter.setSelection(0)
            spinnerCategoryFilter.setSelection(0)
            updateFilterState()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                filterState
                    .debounce(300)
                    .distinctUntilChanged()
                    .flatMapLatest { filters ->
                        repository.searchAndFilterTransactions(
                            searchQuery = filters.first,
                            type = filters.second,
                            category = filters.third
                        )
                    }
                    .collectLatest { transactions ->
                        displayTransactions(transactions)
                    }
            }
        }

        updateFilterState()
    }

    private fun updateFilterState() {

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

        filterState.value = Triple(
            searchQuery,
            selectedType,
            selectedCategory
        )
    }

    private fun displayTransactions(transactions: List<Transaction>) {
        transactionListContainer.removeAllViews()
        if (transactions.isEmpty()) {
            tvEmptyTransactions.visibility = View.VISIBLE
            return
        }
        tvEmptyTransactions.visibility = View.GONE
        transactions.forEach {
            transactionListContainer.addView(createTransactionCard(it))
        }
    }

    private fun createTransactionCard(transaction: Transaction): View {
        val income = transaction.type.equals("INCOME", true)
        val accent = if (income) 0xFF25A875.toInt() else 0xFFE66C76.toInt()
        val soft = if (income) 0xFFE5F6EE.toInt() else 0xFFFCEBED.toInt()

        val card = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(15), dp(16), dp(14))
            background = roundedBackground(0xFFFFFFFF.toInt(), 20)
            elevation = dp(2).toFloat()
        }
        card.layoutParams = LinearLayout.LayoutParams(-1, -2).apply {
            bottomMargin = dp(12)
        }

        val header = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }
        val badge = TextView(requireContext()).apply {
            text = if (income) "INCOME" else "EXPENSE"
            textSize = 10f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(accent)
            setPadding(dp(10), dp(6), dp(10), dp(6))
            background = roundedBackground(soft, 30)
        }
        val amount = TextView(requireContext()).apply {
            text = (if (income) "+" else "\u2212") + "\u20B9" + String.format(Locale.getDefault(), "%.2f", transaction.amount)
            textSize = 20f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(accent)
        }
        header.addView(badge, LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(amount)
        card.addView(header)

        val category = TextView(requireContext()).apply {
            text = transaction.category
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(0xFF242044.toInt())
            setPadding(0, dp(12), 0, dp(3))
        }
        card.addView(category)

        val date = TextView(requireContext()).apply {
            text = transaction.date
            textSize = 12f
            setTextColor(0xFF777487.toInt())
        }
        card.addView(date)

        transaction.description?.takeIf { it.isNotBlank() }?.let {
            val note = TextView(requireContext()).apply {
                text = it
                textSize = 13f
                setTextColor(0xFF777487.toInt())
                setPadding(0, dp(7), 0, 0)
            }
            card.addView(note)
        }

        val divider = View(requireContext()).apply {
            setBackgroundColor(0xFFEAE8F0.toInt())
        }
        card.addView(divider, LinearLayout.LayoutParams(-1, dp(1)).apply {
            topMargin = dp(13)
            bottomMargin = dp(10)
        })

        val actions = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        val edit = createActionButton("Edit", 0xFF7252B5.toInt(), 0xFFEDE7F8.toInt()) {
            val args = Bundle().apply { putLong("transactionId", transaction.id) }
            findNavController().navigate(R.id.addTransactionFragment, args)
        }
        val delete = createActionButton("Delete", 0xFFCD4A55.toInt(), 0xFFFCEBED.toInt()) {
            showDeleteConfirmation(transaction)
        }
        actions.addView(edit, LinearLayout.LayoutParams(0, dp(40), 1f).apply {
            marginEnd = dp(8)
        })
        actions.addView(delete, LinearLayout.LayoutParams(0, dp(40), 1f))
        card.addView(actions)
        return card
    }

    private fun createActionButton(
        label: String, textColor: Int, fillColor: Int, action: () -> Unit
    ): TextView {
        return TextView(requireContext()).apply {
            text = label
            textSize = 13f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(textColor)
            gravity = android.view.Gravity.CENTER
            background = roundedBackground(fillColor, 12)
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }
    }

    private fun roundedBackground(
        color: Int, radiusDp: Int
    ): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radiusDp).toFloat()
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
    private fun showDeleteConfirmation(transaction: Transaction) {

        AlertDialog.Builder(requireContext())
            .setTitle("Delete Transaction")
            .setMessage(
                "Are you sure you want to delete this transaction?"
            )
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


