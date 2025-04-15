package com.daffaadityapurwanto.securein

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalTime
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ArrayAdapter
import java.util.ArrayList
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Toast


class Dashboard : AppCompatActivity() {
    data class NewlyAddedItem(
        val logoResId: Int,
        val emailName: String,
        val createdDate: String
    )
    class NewlyAddedAdapter(
        private val context: Context,
        private val dataList: List<NewlyAddedItem>
    ) : BaseAdapter() {

        override fun getCount(): Int = dataList.size
        override fun getItem(position: Int): Any = dataList[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.item_newly_addeddaridashboard, parent, false)

            val item = dataList[position]

            val logo = view.findViewById<ImageView>(R.id.logoItem)
            val email = view.findViewById<TextView>(R.id.emailName)
            val date = view.findViewById<TextView>(R.id.createdDate)

            logo.setImageResource(item.logoResId)
            email.text = item.emailName
            date.text = "Created: ${item.createdDate}"

            return view
        }
    }

    private lateinit var goodmorning: TextView
    private lateinit var angkapasswordtotal: TextView
    private lateinit var tombolkemenumypassword: LinearLayout
    private lateinit var tombolkemenuDashboard: LinearLayout
    private lateinit var tombolkemenuBackup: LinearLayout
    private lateinit var tombolkemenuSetting: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        goodmorning = findViewById(R.id.ucapanselamat)
        setGreeting()
        tombolkemenumypassword = findViewById(R.id.tombolkemenumypassword_dashboard)
        tombolkemenuDashboard = findViewById(R.id.tombolmenukebackup_dashboard)
        tombolkemenuSetting = findViewById(R.id.tombolmenukesetting_dashboard)
        tombolkemenuBackup = findViewById(R.id.tombolmenukebackup_dashboard)
        tombolkemenumypassword.setOnClickListener {
            goToMenupassword()
        }
        tombolkemenuDashboard.setOnClickListener {

        }
        tombolkemenuSetting.setOnClickListener {
            goToSetting()
        }
        tombolkemenuBackup.setOnClickListener {
            goToBackupmenu()
        }

        val listView = findViewById<ListView>(R.id.listViewNewlyAdded)

        val dummyData = listOf(
            NewlyAddedItem(R.drawable.logingoogle, "admin@gmail.com", "2025-04-10"),
            NewlyAddedItem(R.drawable.logingoogle, "user01@yahoo.com", "2025-04-09"),
            NewlyAddedItem(R.drawable.logingoogle, "daffa@securein.id", "2025-04-08"),
            NewlyAddedItem(R.drawable.logingoogle, "user01@yahoo.com", "2025-04-09"),
            NewlyAddedItem(R.drawable.logingoogle, "daffa@securein.id", "2025-04-08"),

        )

        val adapter = NewlyAddedAdapter(this, dummyData)
        listView.adapter = adapter

        val btnAdd = findViewById<ImageView>(R.id.buattambahindata)
        btnAdd.setOnClickListener {
            // Aksi tambah data di sini
            Toast.makeText(this, "Tombol Tambah ditekan", Toast.LENGTH_SHORT).show()
        }
        val totaldata = dummyData.count().toString()
        angkapasswordtotal = findViewById(R.id.angkatotalpassworddashboard)
        angkapasswordtotal.text = totaldata


    }
    private fun setGreeting() {
        val now = LocalTime.now()
        val greeting = when (now.hour) {
            in 5..10 -> "Hello, Good Morning"
            in 11..17 -> "Hello, Good Afternoon"
            in 18..22 -> "Hello, Good Night"
            else -> "Good Night, Dong Forget To Sleep"
        }

        goodmorning.text = greeting
    }
    private fun goToMenupassword() {
        Intent(this, menupassword::class.java).also {
            startActivity(it)
        }
        finish()
    }
    private fun goToDashboard() {
        Intent(this, Dashboard::class.java).also {
            startActivity(it)
        }
        finish()
    }
    private fun goToBackupmenu() {
        Intent(this, menubackupandrestore::class.java).also {
            startActivity(it)
        }
        finish()
    }
    private fun goToSetting() {
        Intent(this, menusetting::class.java).also {
            startActivity(it)
        }
        finish()
    }
}