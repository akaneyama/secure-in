package com.daffaadityapurwanto.securein

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.*
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.daffaadityapurwanto.securein.data.databaseHelper

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val dbHelper = databaseHelper(this)
        dbHelper.copyDatabaseIfNeeded(this)
        Handler(Looper.getMainLooper()).postDelayed({
            runOnUiThread {
                goToMain()
            }
        }, 3000)
    }

    private fun goToMain() {
        Intent(this, halamanlogin::class.java).also {
            startActivity(it)
        }
        finish()
    }
}
