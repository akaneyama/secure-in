package com.daffaadityapurwanto.securein

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.daffaadityapurwanto.securein.data.databaseHelper
import com.daffaadityapurwanto.securein.encryption.Encrypt
import com.daffaadityapurwanto.securein.encryption.keyAES
import androidx.lifecycle.lifecycleScope
import com.daffaadityapurwanto.securein.network.LoginRequest
import com.daffaadityapurwanto.securein.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.Toast


class halamanlogin : AppCompatActivity() {
    private lateinit var etPassword: EditText
    private lateinit var ivShowPassword: ImageView
    private lateinit var btnLogin: Button
    private lateinit var btnDaftar: TextView
    private lateinit var etUsername: EditText
    private lateinit var lihatstatus: TextView
    private var isPasswordVisible = false

    // Nama dan Key untuk SharedPreferences
    private val PREFS_NAME = "LoginPrefs"
    private val KEY_IS_LOGGED_IN = "isLoggedIn"
    private val KEY_USER_ID = "userId"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cek dulu status login, jika sudah, langsung ke dashboard
        if (isUserLoggedIn()) {
            goToDashboard()
            return // Hentikan eksekusi lebih lanjut
        }

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
            val usernameStr = etUsername.text.toString()
            val passwordStr = etPassword.text.toString()

            if (usernameStr.isBlank() || passwordStr.isBlank()) {
                showCustomDialog("empty_password")
                return@setOnClickListener
            }

            // Tampilkan loading (opsional, tapi baik untuk UX)
            Toast.makeText(this, "Mencoba login...", Toast.LENGTH_SHORT).show()

            lifecycleScope.launch(Dispatchers.IO) {
                val kunciAES = keyAES()
                val encrypt = Encrypt(kunciAES.KunciAES128, kunciAES.KunciIVKey)
                val hasilencrypt = encrypt.enkripsi(passwordStr)
                val dbHelper = databaseHelper(this@halamanlogin)

                // 1. Coba login secara lokal
                var localUser = dbHelper.loginandcheckuser(usernameStr, usernameStr, hasilencrypt)

                if (localUser != null) {
                    // 2. Jika login lokal berhasil, langsung masuk
                    withContext(Dispatchers.Main) {
                        saveLoginStatus(true, localUser.id_user)
                        goToDashboard()
                    }
                } else {
                    // 3. Jika login lokal gagal, coba via server
                    try {
                        val request = LoginRequest(username = usernameStr, passwordEncrypted = hasilencrypt)
                        val response = RetrofitClient.instance.loginUser(request)
                        val userFromServer = response.body()

                        if (response.isSuccessful && userFromServer != null) {
                            // 4. Jika di server berhasil, simpan data ke lokal
                            dbHelper.insertOrUpdateUser(userFromServer)

                            // 5. Masuk ke dashboard
                            withContext(Dispatchers.Main) {
                                saveLoginStatus(true, userFromServer.id_user)
                                goToDashboard()
                            }
                        } else {
                            // Jika di server juga gagal, tampilkan error
                            withContext(Dispatchers.Main) {
                                showCustomDialog("wrong_password")
                            }
                        }
                    } catch (e: Exception) {
                        // Tangani jika tidak ada koneksi internet atau server error
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@halamanlogin, "Gagal terhubung ke server. Periksa koneksi internet Anda.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    /**
     * Menyimpan status login dan ID pengguna ke SharedPreferences.
     * @param isLoggedIn Boolean yang menandakan status login.
     * @param userId ID pengguna yang akan disimpan. Beri nilai -1 saat logout.
     */
    private fun saveLoginStatus(isLoggedIn: Boolean, userId: Int) {
        val sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
        editor.putInt(KEY_USER_ID, userId)
        editor.apply() // .apply() menyimpan data secara asynchronous
    }

    /**
     * Memeriksa status login dari SharedPreferences.
     * @return Boolean true jika sudah login, false jika belum.
     */
    private fun isUserLoggedIn(): Boolean {
        val sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPref.getBoolean(KEY_IS_LOGGED_IN, false) // `false` adalah nilai default
    }

    /**
     * Mengambil ID pengguna yang sedang login dari SharedPreferences.
     * Fungsi ini mungkin tidak digunakan di sini, tapi akan sangat berguna di activity lain.
     * @return Int ID pengguna, atau -1 jika tidak ada yang login.
     */
    private fun getLoggedInUserId(): Int {
        val sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPref.getInt(KEY_USER_ID, -1)
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