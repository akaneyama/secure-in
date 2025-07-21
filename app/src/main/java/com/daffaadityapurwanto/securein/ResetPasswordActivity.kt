package com.daffaadityapurwanto.securein

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.daffaadityapurwanto.securein.encryption.Encrypt
import com.daffaadityapurwanto.securein.encryption.keyAES
import com.daffaadityapurwanto.securein.network.ResetPassPerformRequest
import com.daffaadityapurwanto.securein.network.ResetPassRequest
import com.daffaadityapurwanto.securein.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var btnKirimOtp: Button
    private lateinit var etOtp: EditText
    private lateinit var etNewPass: EditText
    private lateinit var etConfirmPass: EditText
    private lateinit var btnResetPass: Button
    private lateinit var requestOtpLayout: LinearLayout
    private lateinit var resetPasswordLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        etEmail = findViewById(R.id.et_reset_email)
        btnKirimOtp = findViewById(R.id.btn_kirim_otp)
        etOtp = findViewById(R.id.et_reset_otp)
        etNewPass = findViewById(R.id.et_new_password)
        etConfirmPass = findViewById(R.id.et_confirm_new_password)
        btnResetPass = findViewById(R.id.btn_reset_password)
        requestOtpLayout = findViewById(R.id.requestOtpLayout)
        resetPasswordLayout = findViewById(R.id.resetPasswordLayout)

        btnKirimOtp.setOnClickListener { requestOtp() }
        btnResetPass.setOnClickListener { performReset() }
    }

    private fun showResetView(show: Boolean) {
        if (show) {
            requestOtpLayout.visibility = View.GONE
            resetPasswordLayout.visibility = View.VISIBLE
        } else {
            requestOtpLayout.visibility = View.VISIBLE
            resetPasswordLayout.visibility = View.GONE
        }
    }

    private fun requestOtp() {
        val email = etEmail.text.toString().trim()
        if (email.isEmpty() || !email.contains("@")) {
            Toast.makeText(this, "Masukkan alamat email yang valid.", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Mengirim OTP...", Toast.LENGTH_SHORT).show()
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val request = ResetPassRequest(email = email)
                val response = RetrofitClient.instance.requestPasswordReset(request)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ResetPasswordActivity, response.body()?.message, Toast.LENGTH_LONG).show()
                    if (response.isSuccessful) {
                        showResetView(true)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ResetPasswordActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun performReset() {
        val email = etEmail.text.toString().trim()
        val otp = etOtp.text.toString().trim()
        val newPass = etNewPass.text.toString()
        val confirmPass = etConfirmPass.text.toString()

        if (otp.length != 6) {
            Toast.makeText(this, "OTP harus 6 digit.", Toast.LENGTH_SHORT).show()
            return
        }
        if (newPass.isEmpty() || newPass != confirmPass) {
            Toast.makeText(this, "Password baru tidak valid atau tidak cocok.", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Memproses...", Toast.LENGTH_SHORT).show()
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val kunciAES = keyAES()
                val encryptor = Encrypt(kunciAES.KunciAES128, kunciAES.KunciIVKey)
                val encryptedPassword = encryptor.enkripsi(newPass)

                val request = ResetPassPerformRequest(
                    email = email,
                    otp = otp,
                    newPasswordEncrypted = encryptedPassword
                )
                val response = RetrofitClient.instance.performPasswordReset(request)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ResetPasswordActivity, response.body()?.message, Toast.LENGTH_LONG).show()
                    if (response.isSuccessful) {
                        finish() // Tutup halaman reset dan kembali ke login
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ResetPasswordActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}