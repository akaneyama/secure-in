package com.daffaadityapurwanto.securein

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.daffaadityapurwanto.securein.data.databaseHelper
import com.daffaadityapurwanto.securein.encryption.Encrypt
import com.daffaadityapurwanto.securein.encryption.keyAES

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

        val kunciAES = keyAES()
        val encrypt = Encrypt(kunciAES.KunciAES128, kunciAES.KunciIVKey)
        btnLogin.setOnClickListener {
            if(etUsername.text.isNullOrEmpty() || etPassword.text.isNullOrEmpty()){
                //Toast.makeText(this, "username atau password tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                showCustomDialog("empty_password")
                return@setOnClickListener
            }
            val DBhelper = databaseHelper(this)
            val hasilencrypt = encrypt.enkripsi(etPassword.text.toString())
            val user = DBhelper.loginandcheckuser(etUsername.text.toString(),etUsername.text.toString(), hasilencrypt)

            if(user != null){
                //Toast.makeText(this, "Selamat datang, ${user.nama}", Toast.LENGTH_SHORT).show()
                goToDashboard()
            }
            else{
               // Toast.makeText(this, "Password Salah ${hasilencrypt}", Toast.LENGTH_SHORT).show()
                showCustomDialog("wrong_password")
            }


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
    private fun showCustomDialog(status: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.logindialog_status, null)
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val imgStatus = dialogView.findViewById<ImageView>(R.id.imgStatus)
        val txtStatus = dialogView.findViewById<TextView>(R.id.txtStatus)
        val btnOk = dialogView.findViewById<Button>(R.id.btnOk)

        when (status) {
            "wrong_password" -> {
                imgStatus.setImageResource(R.drawable.crosslogin)
                txtStatus.text = "Username atau Password Salah"
            }
            "empty_password" -> {
                imgStatus.setImageResource(R.drawable.dangerlogin)
                txtStatus.text = "Username atau Password Kosong"
            }
        }

        btnOk.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}