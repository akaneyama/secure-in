package com.daffaadityapurwanto.securein

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class aboutusmenu : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aboutusmenu)
        var tombolkedashboard = findViewById<CardView>(R.id.backtohome)
        tombolkedashboard.setOnClickListener{
            Intent(this, MainDashboard::class.java).also {
                startActivity(it)

            }
        }


    }
}