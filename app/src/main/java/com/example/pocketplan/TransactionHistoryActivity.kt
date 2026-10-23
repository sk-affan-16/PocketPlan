
package com.example.pocketplan

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketplan.data.local.database.DatabaseProvider
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

        transactionAdapter = TransactionAdapter()

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
}
