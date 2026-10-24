
package com.example.pocketplan

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketplan.data.local.database.DatabaseProvider
import com.example.pocketplan.data.local.entity.Transaction
import com.example.pocketplan.data.repository.PocketPlanRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class TransactionHistoryActivity : AppCompatActivity() {

    private lateinit var repository: PocketPlanRepository
    private lateinit var transactionAdapter: TransactionAdapter

    private lateinit var rvTransactions: RecyclerView
    private lateinit var tvHistoryCount: TextView
    private lateinit var tvHistoryEmpty: TextView
    private lateinit var etHistorySearch: EditText
    private lateinit var spinnerHistoryType: Spinner

    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_history)

        val database = DatabaseProvider.getDatabase(applicationContext)

        repository = PocketPlanRepository(
            transactionDao = database.transactionDao(),
            budgetDao = database.budgetDao()
        )

        rvTransactions = findViewById(R.id.rvTransactions)
        tvHistoryCount = findViewById(R.id.tvHistoryCount)
        tvHistoryEmpty = findViewById(R.id.tvHistoryEmpty)
        etHistorySearch = findViewById(R.id.etHistorySearch)
        spinnerHistoryType = findViewById(R.id.spinnerHistoryType)

        transactionAdapter = TransactionAdapter { transaction ->
            showTransactionOptions(transaction)
        }

        rvTransactions.layoutManager = LinearLayoutManager(this)
        rvTransactions.adapter = transactionAdapter

        setupTypeFilter()
        setupSearch()

        observeTransactions()
    }

    private fun setupTypeFilter() {
        val types = listOf("All types", "INCOME", "EXPENSE")

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            types
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinnerHistoryType.adapter = adapter

        spinnerHistoryType.setOnItemSelectedListener(
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    observeTransactions()
                }

                override fun onNothingSelected(
                    parent: android.widget.AdapterView<*>?
                ) = Unit
            }
        )
    }

    private fun setupSearch() {
        etHistorySearch.addTextChangedListener(
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
                    scheduleSearch()
                }

                override fun afterTextChanged(
                    s: android.text.Editable?
                ) = Unit
            }
        )
    }

    private fun scheduleSearch() {
        searchJob?.cancel()

        searchJob = lifecycleScope.launch {
            delay(250)
            observeTransactions()
        }
    }

    private fun observeTransactions() {
        searchJob?.cancel()

        searchJob = lifecycleScope.launch {
            val query = etHistorySearch.text.toString().trim()

            val selectedType = spinnerHistoryType.selectedItem?.toString()
            val type = when (selectedType) {
                "INCOME" -> "INCOME"
                "EXPENSE" -> "EXPENSE"
                else -> null
            }

            repository.searchAndFilterTransactions(
                searchQuery = query,
                type = type,
                category = null
            ).collect { transactions ->
                transactionAdapter.submitList(transactions)

                val isEmpty = transactions.isEmpty()

                tvHistoryEmpty.visibility =
                    if (isEmpty) View.VISIBLE else View.GONE

                rvTransactions.visibility =
                    if (isEmpty) View.GONE else View.VISIBLE

                tvHistoryEmpty.text = if (query.isNotEmpty() || type != null) {
                    "No matching transactions found."
                } else {
                    "No transactions yet.\nAdd income or an expense to see it here."
                }

                tvHistoryCount.text = when (transactions.size) {
                    0 -> "No matching transactions"
                    1 -> "1 saved transaction"
                    else -> "${transactions.size} saved transactions"
                }
            }
        }
    }

    private fun showTransactionOptions(transaction: Transaction) {
        AlertDialog.Builder(this)
            .setTitle("Transaction Options")
            .setItems(arrayOf("Edit", "Delete")) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(
                            this,
                            AddTransactionActivity::class.java
                        )
                        intent.putExtra("transaction_id", transaction.id)
                        startActivity(intent)
                    }

                    1 -> confirmDelete(transaction)
                }
            }
            .show()
    }

    private fun confirmDelete(transaction: Transaction) {
        AlertDialog.Builder(this)
            .setTitle("Delete Transaction")
            .setMessage(
                "Are you sure you want to delete this transaction?"
            )
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    try {
                        repository.deleteTransaction(transaction)

                        Toast.makeText(
                            this@TransactionHistoryActivity,
                            "Transaction deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (exception: Exception) {
                        Toast.makeText(
                            this@TransactionHistoryActivity,
                            "Could not delete transaction",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            .show()
    }
}
