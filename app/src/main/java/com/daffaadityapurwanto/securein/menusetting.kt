package com.daffaadityapurwanto.securein

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

private lateinit var tombolkemenumypassword: LinearLayout
private lateinit var tombolkemenuDashboard: LinearLayout
private lateinit var tombolkemenuBackup: LinearLayout
private lateinit var tombolkemenuSetting: LinearLayout
class menusetting : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menusetting)

    }
    private fun goToMenupassword() {
        Intent(this, menupassword::class.java).also {
            startActivity(it)
        }
        finish()
    }
    private fun goToDashboard() {
        Intent(this, Dashboard::class.java).also {
            startActivity(it)
        }
        finish()
    }
    private fun goToBackupmenu() {
        Intent(this, menubackupandrestore::class.java).also {
            startActivity(it)
        }
        finish()
    }
    private fun goToSetting() {
        Intent(this, menusetting::class.java).also {
            startActivity(it)
        }
        finish()
    }
}