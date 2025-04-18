package com.daffaadityapurwanto.securein

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class halamanlogin : AppCompatActivity() {
    private lateinit var etPassword: EditText
    private lateinit var ivShowPassword: ImageView
    private lateinit var btnLogin: Button
    private lateinit var btnDaftar: TextView
    private lateinit var etUsername: EditText
    private lateinit var lihatstatus: TextView
    private var isPasswordVisible = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_halamanlogin)
        lihatstatus = findViewById(R.id.statuslogin)
        etUsername = findViewById(R.id.et_username)
        etPassword = findViewById(R.id.et_password)
        ivShowPassword = findViewById(R.id.lihatpassword)
        btnLogin = findViewById(R.id.btn_login)
        btnDaftar = findViewById(R.id.btndaftar)
        ivShowPassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivShowPassword.setImageResource(R.drawable.eyesclose)
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivShowPassword.setImageResource(R.drawable.eyesopen)
            }
            etPassword.setSelection(etPassword.text.length)
        }
        btnDaftar.setOnClickListener {
            goToDaftar()
        }


        btnLogin.setOnClickListener {
            goToDashboard()
        }
    }
    private fun goToDashboard() {
        Intent(this, MainDashboard::class.java).also {
            startActivity(it)
        }
        finish()
    }
    private fun goToDaftar() {
        Intent(this, halamandaftar::class.java).also {
            startActivity(it)
        }
        finish()
    }
}