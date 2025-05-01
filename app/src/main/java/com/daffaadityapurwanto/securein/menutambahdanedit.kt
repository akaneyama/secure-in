package com.daffaadityapurwanto.securein

import android.os.Bundle
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

    private val kategoriList = listOf(
        Kategori("Pribadi", R.drawable.logingoogle),
        Kategori("Pekerjaan", R.drawable.logingoogle),
        Kategori("Keuangan", R.drawable.logingoogle),
        Kategori("Sosial", R.drawable.logingoogle)
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menutambahdanedit)
        spinnerKategori = findViewById(R.id.spinnerkategori)

        val adapter = object : ArrayAdapter<Kategori>(
            this,
            R.layout.spinner_item,  // custom item layout
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