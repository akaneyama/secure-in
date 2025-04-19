package com.daffaadityapurwanto.securein

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.daffaadityapurwanto.securein.data.databaseHelper

class halamandaftar : AppCompatActivity() {

    private lateinit var etPassword: EditText
    private lateinit var etRetypePassword: EditText
    private lateinit var togglePassword: ImageView
    private lateinit var toggleRetype: ImageView
    private lateinit var passwordStrength: TextView
    private lateinit var passwordMatch: TextView
    private lateinit var loginkembali: TextView
    private lateinit var username: EditText
    private lateinit var name: EditText
    private lateinit var email: EditText
    private lateinit var daftar: Button
    private var currentPasswordStrength = ""


    private var isPasswordVisible = false
    private var isRetypeVisible = false
    private fun goToLogin() {
        Intent(this, halamanlogin::class.java).also {
            startActivity(it)
        }
        finish()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_halamandaftar)
        loginkembali = findViewById(R.id.btnloginkembali)
        etPassword = findViewById(R.id.et_password)
        etRetypePassword = findViewById(R.id.et_passwordretype)
        togglePassword = findViewById(R.id.lihatpassword)
        toggleRetype = findViewById(R.id.lihatpasswordretype)
        passwordStrength = findViewById(R.id.passwordStrength)
        passwordMatch = findViewById(R.id.passwordMatch)
        username = findViewById(R.id.et_username)
        name = findViewById(R.id.et_nama)
        email = findViewById(R.id.et_email)
        daftar = findViewById(R.id.btn_daftarin)


        //tombol buat daftar
        val DBhelper  = databaseHelper(this)
        daftar.setOnClickListener {
            if(email.text.isNullOrEmpty() || name.text.isNullOrEmpty() || username.text.isNullOrEmpty() || etPassword.text.isNullOrEmpty() || etRetypePassword.text.isNullOrEmpty()  ){
                showCustomDialog("field_kosong")
                return@setOnClickListener
            }
            if(etPassword.text.toString() != etRetypePassword.text.toString()){
                showCustomDialog("wrong_password")
                return@setOnClickListener
            }
            if(DBhelper.cekUsernameAtauEmailSudahAda(username.text.toString(), email.text.toString())){
                showCustomDialog("userataupasswordada")
                return@setOnClickListener
            }
            if(!email.text.toString().contains("@")){
                showCustomDialog("harusada_@")
                return@setOnClickListener
            }
           if(currentPasswordStrength == "Medium" || currentPasswordStrength == "Strong"){
               DBhelper.tambahkanUser(email.text.toString(), name.text.toString(),username.text.toString(), etPassword.text.toString() )
               showCustomDialog("berhasil_daftar")

           }
            else{
               showCustomDialog("password_lemah")
               return@setOnClickListener
           }

        }
        loginkembali.setOnClickListener {
            goToLogin()
        }

        // Toggle visibility untuk password
        togglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                togglePassword.setImageResource(R.drawable.eyesclose)
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePassword.setImageResource(R.drawable.eyesopen)
            }
            etPassword.setSelection(etPassword.text.length)
        }

        // Toggle visibility untuk retype password
        toggleRetype.setOnClickListener {
            isRetypeVisible = !isRetypeVisible
            if (isRetypeVisible) {
                etRetypePassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toggleRetype.setImageResource(R.drawable.eyesclose)
            } else {
                etRetypePassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleRetype.setImageResource(R.drawable.eyesopen)
            }
            etRetypePassword.setSelection(etRetypePassword.text.length)
        }

        // Validasi kekuatan password
        etPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                val strength = getPasswordStrength(password)
                passwordStrength.text = "Strength: $strength"
                currentPasswordStrength = "$strength"
                passwordStrength.setTextColor(
                    when (strength) {
                        "Weak" -> Color.RED
                        "Medium" -> Color.parseColor("#FFA500")
                        "Strong" -> Color.GREEN
                        else -> Color.GRAY
                    }
                )

                checkPasswordMatch()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Validasi kecocokan password dan retype
        etRetypePassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkPasswordMatch()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun getPasswordStrength(password: String): String {
        return when {
            password.matches(Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#\$%^&*()_+]).{8,}$")) -> "Strong"
            password.length < 6 -> "Weak"
            password.matches(Regex("^(?=.*[a-zA-Z])(?=.*[0-9]).{6,}\$")) -> "Medium"
            else -> "Weak"
        }
    }

    private fun checkPasswordMatch() {
        val password = etPassword.text.toString()
        val retype = etRetypePassword.text.toString()

        if (retype.isEmpty()) {
            passwordMatch.text = ""
        } else if (password == retype) {
            passwordMatch.text = "Password cocok"
            passwordMatch.setTextColor(Color.GREEN)
        } else {
            passwordMatch.text = "Password tidak cocok"
            passwordMatch.setTextColor(Color.RED)
        }
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
                txtStatus.text = "Password Tidak Cocok"
            }
            "field_kosong" -> {
                imgStatus.setImageResource(R.drawable.dangerlogin)
                txtStatus.text = "Semua field harus diisi"
            }
            "berhasil_daftar" -> {
                imgStatus.setImageResource(R.drawable.check)
                txtStatus.text = "Berhasil Daftar"
            }
            "password_lemah" -> {
                imgStatus.setImageResource(R.drawable.dangerlogin)
                txtStatus.text = "Password Minimal 8 Kata dan harus ada angkanya"
            }
            "userataupasswordada" -> {
                imgStatus.setImageResource(R.drawable.dangerlogin)
                txtStatus.text = "Daftar Gagal. ada data yang sudah digunakan!"
            }
            "harusada_@" -> {
                imgStatus.setImageResource(R.drawable.dangerlogin)
                txtStatus.text = "Email Tidak Valid!"
            }
        }

        btnOk.setOnClickListener {
            if (status == "berhasil_daftar"){
                goToLogin()
            }
            alertDialog.dismiss()
        }

        alertDialog.setCancelable(false)
        alertDialog.show()
    }

}
