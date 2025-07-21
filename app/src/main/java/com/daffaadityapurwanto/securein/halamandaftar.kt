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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.daffaadityapurwanto.securein.data.databaseHelper
import com.daffaadityapurwanto.securein.encryption.Encrypt
import com.daffaadityapurwanto.securein.encryption.keyAES
import com.daffaadityapurwanto.securein.network.RegisterRequest
import com.daffaadityapurwanto.securein.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class halamandaftar : AppCompatActivity() {

    // --- Deklarasi Variabel ---
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
    private lateinit var dbHelper: databaseHelper // Gunakan satu instance saja

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_halamandaftar)

        // Inisialisasi properti kelas
        dbHelper = databaseHelper(this)
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

        // Setup semua listener
        setupListeners()
    }

    private fun setupListeners() {
        daftar.setOnClickListener {
            performRegistration()
        }

        loginkembali.setOnClickListener {
            goToLogin()
        }

        // ... (Listener lain tidak berubah) ...
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
                currentPasswordStrength = strength
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

    private fun performRegistration() {
        // ... (kode validasi input Anda tetap di sini) ...
        val emailStr = email.text.toString()
        val nameStr = name.text.toString()
        val usernameStr = username.text.toString()
        val passwordStr = etPassword.text.toString()

        // Proses registrasi sekarang terjadi di background
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Kita mengenkripsi password sekali di sini
                val kunciAES = keyAES()
                val encryptor = Encrypt(kunciAES.KunciAES128, kunciAES.KunciIVKey)
                val encryptedPassword = encryptor.enkripsi(passwordStr)
                val randomEncryptionKey = dbHelper.generateRandomKeyString()

                // 1. Siapkan data untuk dikirim ke server (TANPA userId)
                val request = RegisterRequest(
                    kunciEnkripsi = randomEncryptionKey,
                    email = emailStr,
                    nama = nameStr,
                    username = usernameStr,
                    passwordEncrypted = encryptedPassword
                )

                // 2. Panggil API untuk mendaftar di server
                val response = RetrofitClient.instance.registerUser(request)
                val responseBody = response.body()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && responseBody != null && responseBody.status == "success") {
                        // 3. Jika di server sukses, ambil ID baru
                        val newUserId = responseBody.userId
                        if (newUserId != null) {
                            // 4. BARU SIMPAN KE DATABASE LOKAL dengan ID dari server
                            // Kita kirim password yang belum di-enkripsi agar helper bisa mengenkripsi ulang dengan kunci unik user yang baru dibuat
                            dbHelper.tambahkanUser(newUserId, emailStr, nameStr, usernameStr, passwordStr)
                            showCustomDialog("berhasil_daftar")
                        } else {
                            // Error dari server: tidak ada user_id yang dikembalikan
                            showCustomDialog("error_server_id") // Anda bisa buat dialog error baru untuk ini
                        }
                    } else {
                        // Tampilkan pesan error dari server jika ada
                        val errorMessage = responseBody?.message ?: "Registrasi server gagal"
                        Toast.makeText(this@halamandaftar, "Gagal: $errorMessage", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@halamandaftar, "Error: Tidak bisa terhubung ke server. ${e.message}", Toast.LENGTH_LONG).show()
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

    // ... (Fungsi getPasswordStrength dan checkPasswordMatch tidak berubah) ...
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
                imgStatus.setImageResource(R.drawable.crosslogin)
                txtStatus.text = "Password Tidak Cocok"
            }
            "field_kosong" -> {
                imgStatus.setImageResource(R.drawable.dangerlogin)
                txtStatus.text = "Semua field harus diisi"
            }
            "berhasil_daftar" -> {
                imgStatus.setImageResource(R.drawable.check)
                txtStatus.text = "Berhasil Daftar & Sinkronisasi"
            }
            "berhasil_daftar_tapi_gagal_sinkron" -> {
                imgStatus.setImageResource(R.drawable.check)
                txtStatus.text = "Berhasil Daftar! Sinkronisasi Gagal, silakan Backup manual nanti."
            }
            "gagal_daftar_db" -> {
                imgStatus.setImageResource(R.drawable.crosslogin)
                txtStatus.text = "Gagal menyimpan ke database lokal."
            }
            "password_lemah" -> {
                imgStatus.setImageResource(R.drawable.dangerlogin)
                txtStatus.text = "Password Anda masih lemah"
            }
            "userataupasswordada" -> {
                imgStatus.setImageResource(R.drawable.dangerlogin)
                txtStatus.text = "Email atau Username sudah digunakan!"
            }
            "harusada_@" -> {
                imgStatus.setImageResource(R.drawable.dangerlogin)
                txtStatus.text = "Email Tidak Valid!"
            }
        }

        btnOk.setOnClickListener {
            // Pindah ke halaman login HANYA jika registrasi berhasil
            if (status.startsWith("berhasil_daftar")) {
                goToLogin()
            }
            alertDialog.dismiss()
        }

        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}