package com.example.pocketplan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val type: String,

    val amount: Double,

    val category: String,

    val description: String? = null,

    val date: String
)