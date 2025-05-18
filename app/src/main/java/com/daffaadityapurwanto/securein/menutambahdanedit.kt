package com.daffaadityapurwanto.securein

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.daffaadityapurwanto.securein.data.Kategori
import kotlin.random.Random

class menutambahdanedit : AppCompatActivity() {
    private lateinit var spinnerKategori: Spinner
    private lateinit var etPassword: EditText
    private lateinit var ivShowPassword: ImageView
    private lateinit var kembalikemain: ImageView
    private lateinit var generatePassword: LinearLayout
    private lateinit var passwordStrength: TextView
    private val kategoriList = listOf(
        Kategori("Gmail", R.drawable.logingoogle),
        Kategori("Reddit", R.drawable.logingoogle),
        Kategori("Whatsapp", R.drawable.logingoogle),
        Kategori("Free fire", R.drawable.logingoogle)
    )
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menutambahdanedit)

        kembalikemain = findViewById(R.id.buttonkembali)
        generatePassword = findViewById(R.id.generatepassword)
        passwordStrength = findViewById(R.id.passwordStrength)
        kembalikemain.setOnClickListener {
            Intent(this, MainDashboard::class.java).also {
                startActivity(it)
            }
            finish()
        }

        spinnerKategori = findViewById(R.id.spinnerkategori)
        etPassword = findViewById(R.id.etpassword)
        ivShowPassword = findViewById(R.id.lihatpassword)
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

        generatePassword.setOnClickListener {
            val password = generateRandomPassword()
            etPassword.setText(password)
            updatePasswordStrength(password)
            Toast.makeText(this, "Password Generated!", Toast.LENGTH_SHORT).show()
        }

        val adapter = object : ArrayAdapter<Kategori>(
            this,
            R.layout.spinner_item,
            kategoriList
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                return getCustomView(position, convertView, parent)
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                return getCustomView(position, convertView, parent)
            }

            private fun getCustomView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false)
                val icon = view.findViewById<ImageView>(R.id.iconkategori)
                val text = view.findViewById<TextView>(R.id.textkategori)

                val kategori = kategoriList[position]
                icon.setImageResource(kategori.iconResId)
                text.text = kategori.nama

                return view
            }
        }

        spinnerKategori.adapter = adapter

        spinnerKategori.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                val kategori = kategoriList[position]
                Toast.makeText(this@menutambahdanedit, "Kategori: $kategori.nama}", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun generateRandomPassword(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#\$%^&*()"
        return (1..12)
            .map { Random.nextInt(chars.length) }
            .map(chars::get)
            .joinToString("")
    }

    private fun updatePasswordStrength(password: String) {
        val strength = getPasswordStrength(password)
        passwordStrength.text = "Strength: " + strength
    }

    private fun getPasswordStrength(password: String): String {
        return when {
            password.matches(Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#\$%^&*()_+]).{8,}\$")) -> "Strong"
            password.length < 6 -> "Weak"
            password.matches(Regex("^(?=.*[a-zA-Z])(?=.*[0-9]).{6,}\$")) -> "Medium"
            else -> "Weak"
        }
    }
}
