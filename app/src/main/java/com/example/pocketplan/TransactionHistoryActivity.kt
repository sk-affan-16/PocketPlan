
package com.example.pocketplan

import android.content.Intent
import android.os.Bundle
import android.view.View
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
import kotlinx.coroutines.launch

class TransactionHistoryActivity : AppCompatActivity() {

    private lateinit var repository: PocketPlanRepository
    private lateinit var transactionAdapter: TransactionAdapter

    private lateinit var rvTransactions: RecyclerView
    private lateinit var tvHistoryCount: TextView
    private lateinit var tvHistoryEmpty: TextView

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

        transactionAdapter = TransactionAdapter { transaction ->
            showTransactionOptions(transaction)
        }

        rvTransactions.layoutManager = LinearLayoutManager(this)
        rvTransactions.adapter = transactionAdapter

        observeTransactions()
    }

    private fun observeTransactions() {
        lifecycleScope.launch {
            repository.getAllTransactions().collect { transactions ->
                transactionAdapter.submitList(transactions)

                val isEmpty = transactions.isEmpty()

                tvHistoryEmpty.visibility =
                    if (isEmpty) View.VISIBLE else View.GONE

                rvTransactions.visibility =
                    if (isEmpty) View.GONE else View.VISIBLE

                tvHistoryCount.text = when (transactions.size) {
                    0 -> "No saved transactions"
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
