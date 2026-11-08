
package com.example.pocketplan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketplan.data.local.entity.Transaction
import java.util.Locale

class TransactionAdapter(
    private val onTransactionClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    private var transactions: List<Transaction> = emptyList()

    fun submitList(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }

    class TransactionViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val type: TextView = itemView.findViewById(R.id.tvTransactionType)
        val amount: TextView = itemView.findViewById(R.id.tvTransactionAmount)
        val category: TextView = itemView.findViewById(R.id.tvTransactionCategory)
        val date: TextView = itemView.findViewById(R.id.tvTransactionDate)
        val description: TextView =
            itemView.findViewById(R.id.tvTransactionDescription)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)

        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: TransactionViewHolder,
        position: Int
    ) {
        val transaction = transactions[position]

        holder.type.text = transaction.type
        holder.amount.text = String.format(
            Locale.US,
            "%s%.2f",
            "₹",
            transaction.amount
        )
        holder.category.text = transaction.category
        holder.date.text = transaction.date
        holder.description.text = transaction.description.orEmpty()

        holder.amount.setTextColor(
            if (transaction.type == "INCOME") {
                android.graphics.Color.rgb(22, 163, 74)
            } else {
                android.graphics.Color.rgb(220, 38, 38)
            }
        )

        holder.itemView.setOnClickListener {
            onTransactionClick(transaction)
        }
    }

    override fun getItemCount(): Int = transactions.size
}
