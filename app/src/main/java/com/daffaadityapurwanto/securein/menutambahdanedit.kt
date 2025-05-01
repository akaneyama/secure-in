package com.daffaadityapurwanto.securein

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.daffaadityapurwanto.securein.data.Kategori


class menutambahdanedit : AppCompatActivity() {
    private lateinit var spinnerKategori: Spinner
    private lateinit var etPassword: EditText
    private lateinit var ivShowPassword: ImageView
    private lateinit var kembalikemain: ImageView
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
        kembalikemain.setOnClickListener{
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
                Toast.makeText(this@menutambahdanedit, "Kategori: ${kategori.nama}", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}