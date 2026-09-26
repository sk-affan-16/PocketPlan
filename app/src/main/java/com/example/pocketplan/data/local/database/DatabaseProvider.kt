package com.example.pocketplan.data.local.database

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    @Volatile
    private var INSTANCE: PocketPlanDatabase? = null

    fun getDatabase(context: Context): PocketPlanDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                PocketPlanDatabase::class.java,
                "pocketplan_database"
            ).build().also { INSTANCE = it }
        }
    }
}