package com.daffaadityapurwanto.securein

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.daffaadityapurwanto.securein.data.databaseHelper
import com.daffaadityapurwanto.securein.encryption.Encrypt
import com.daffaadityapurwanto.securein.encryption.keyAES
import com.daffaadityapurwanto.securein.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class halamandaftar : AppCompatActivity() {

    // --- Deklarasi Variabel ---
    private lateinit var registrationLayout: LinearLayout
    private lateinit var verificationLayout: LinearLayout
    private lateinit var etPassword: EditText
    private lateinit var etRetypePassword: EditText
    private lateinit var togglePassword: ImageView
    private lateinit var toggleRetype: ImageView
    private lateinit var passwordStrength: TextView
    private lateinit var passwordMatch: TextView
    private lateinit var username: EditText
    private lateinit var name: EditText
    private lateinit var email: EditText
    private lateinit var daftar: Button
    private lateinit var etOtp: EditText
    private lateinit var btnVerifikasi: Button
    private lateinit var loginkembali: TextView

    private var currentPasswordStrength = ""
    private var isPasswordVisible = false
    private var isRetypeVisible = false
    private lateinit var dbHelper: databaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_halamandaftar)

        dbHelper = databaseHelper(this)
        initializeViews()
        setupListeners()
        val emailToVerify = intent.getStringExtra("VERIFY_EMAIL")
        if (emailToVerify != null) {
            // Langsung tampilkan form OTP
            showVerificationView(true)
            // Isi dan kunci kolom email
            email.setText(emailToVerify)
            email.isEnabled = false
            // Sembunyikan tombol login kembali agar tidak membingungkan
            loginkembali.visibility = View.GONE
        }
    }

    private fun initializeViews() {
        registrationLayout = findViewById(R.id.registrationLayout)
        verificationLayout = findViewById(R.id.verificationLayout)
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
        etOtp = findViewById(R.id.et_otp)
        btnVerifikasi = findViewById(R.id.btn_verifikasi)
    }

    private fun setupListeners() {
        daftar.setOnClickListener { requestOtp() }
        btnVerifikasi.setOnClickListener { verifyOtp() }
        loginkembali.setOnClickListener { goToLogin() }

        togglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            toggleVisibility(etPassword, togglePassword, isPasswordVisible)
        }

        toggleRetype.setOnClickListener {
            isRetypeVisible = !isRetypeVisible
            toggleVisibility(etRetypePassword, toggleRetype, isRetypeVisible)
        }

        etPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                currentPasswordStrength = getPasswordStrength(password)
                updatePasswordStrengthUI(currentPasswordStrength)
                checkPasswordMatch()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        etRetypePassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkPasswordMatch()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun showVerificationView(show: Boolean) {
        if (show) {
            registrationLayout.visibility = View.GONE
            verificationLayout.visibility = View.VISIBLE
        } else {
            registrationLayout.visibility = View.VISIBLE
            verificationLayout.visibility = View.GONE
        }
    }

    private fun requestOtp() {
        val emailStr = email.text.toString().trim()
        val nameStr = name.text.toString().trim()
        val usernameStr = username.text.toString().trim()
        val passwordStr = etPassword.text.toString()
        val retypePasswordStr = etRetypePassword.text.toString()

        // Validasi input
        if (emailStr.isBlank() || nameStr.isBlank() || usernameStr.isBlank() || passwordStr.isBlank()) {
            showCustomDialog("field_kosong")
            return
        }
        if (passwordStr != retypePasswordStr) {
            showCustomDialog("wrong_password")
            return
        }
        if (!emailStr.contains("@")) {
            showCustomDialog("harusada_@")
            return
        }
        if (currentPasswordStrength == "Weak") {
            showCustomDialog("password_lemah")
            return
        }

        Toast.makeText(this, "Mengirim kode verifikasi...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val kunciAES = keyAES()
                val encryptor = Encrypt(kunciAES.KunciAES128, kunciAES.KunciIVKey)
                val encryptedPassword = encryptor.enkripsi(passwordStr)
                val randomEncryptionKey = dbHelper.generateRandomKeyString()

                val request = OtpRequest(
                    email = emailStr,
                    nama = nameStr,
                    username = usernameStr,
                    passwordEncrypted = encryptedPassword,
                    kunciEnkripsi = randomEncryptionKey
                )
                val response = RetrofitClient.instance.requestOtp(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@halamandaftar, "Kode OTP telah dikirim ke email Anda.", Toast.LENGTH_LONG).show()
                        showVerificationView(true)
                    } else {
                        val errorMsg = "Email/Username mungkin sudah terdaftar."
                        Toast.makeText(this@halamandaftar, "Gagal: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@halamandaftar, "Error: Tidak bisa terhubung ke server.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun verifyOtp() {
        val emailStr = email.text.toString().trim()
        val otpStr = etOtp.text.toString().trim()

        if (otpStr.length != 6) {
            Toast.makeText(this, "Kode OTP harus 6 digit.", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Memverifikasi kode...", Toast.LENGTH_SHORT).show()
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val request = OtpVerificationRequest(email = emailStr, otp = otpStr)
                val response = RetrofitClient.instance.verifyOtp(request)
                val body = response.body()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && body?.status == "success") {
                        val userData = body.userData
                        if (userData != null) {
                            dbHelper.insertOrUpdateUser(userData)
                            showCustomDialog("berhasil_daftar")
                        } else {
                            Toast.makeText(this@halamandaftar, "Gagal: Respons server tidak valid.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        val errorMsg = body?.message ?: "Verifikasi gagal."
                        Toast.makeText(this@halamandaftar, "Gagal: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@halamandaftar, "Error: Tidak bisa terhubung ke server.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun goToLogin() {
        Intent(this, halamanlogin::class.java).also {
            startActivity(it)
        }
        finish()
    }

    private fun toggleVisibility(editText: EditText, imageView: ImageView, isVisible: Boolean) {
        if (isVisible) {
            editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            imageView.setImageResource(R.drawable.eyesclose)
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            imageView.setImageResource(R.drawable.eyesopen)
        }
        editText.setSelection(editText.text.length)
    }

    private fun updatePasswordStrengthUI(strength: String) {
        passwordStrength.text = "Strength: $strength"
        passwordStrength.setTextColor(
            when (strength) {
                "Weak" -> Color.RED
                "Medium" -> Color.parseColor("#FFA500")
                "Strong" -> Color.GREEN
                else -> Color.GRAY
            }
        )
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
        val alertDialog = AlertDialog.Builder(this).setView(dialogView).create()
        val imgStatus = dialogView.findViewById<ImageView>(R.id.imgStatus)
        val txtStatus = dialogView.findViewById<TextView>(R.id.txtStatus)
        val btnOk = dialogView.findViewById<Button>(R.id.btnOk)

        when (status) {
            "wrong_password" -> {
                txtStatus.text = "Password Tidak Cocok"
            }
            "field_kosong" -> {
                txtStatus.text = "Semua field harus diisi"
            }
            "berhasil_daftar" -> {
                imgStatus.setImageResource(R.drawable.check)
                txtStatus.text = "Registrasi Berhasil! Silakan Login."
            }
            "password_lemah" -> {
                txtStatus.text = "Password Anda masih lemah"
            }
            "harusada_@" -> {
                txtStatus.text = "Email Tidak Valid!"
            }
        }

        btnOk.setOnClickListener {
            if (status == "berhasil_daftar") {
                goToLogin()
            }
            alertDialog.dismiss()
        }
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}