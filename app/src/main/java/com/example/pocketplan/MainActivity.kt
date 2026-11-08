package com.example.pocketplan

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load the main layout containing the NavHostFragment.
        setContentView(R.layout.activity_main)
    }
}