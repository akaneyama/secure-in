package com.daffaadityapurwanto.securein

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.daffaadityapurwanto.securein.menupassword.AdapterPassword
import com.daffaadityapurwanto.securein.menupassword.ItemPassword

class menubackupandrestore : AppCompatActivity() {
    data class logbackupandrestore(
        val logoResId: Int,
        val emailName: String,
        val createdDate: String
    )

    class Adapterlog(
        private val context: Context,
        private val dataList: List<logbackupandrestore>
    ) : BaseAdapter() {

        override fun getCount(): Int = dataList.size
        override fun getItem(position: Int): Any = dataList[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.loglistdaribackupandrestore, parent, false)

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

    private lateinit var tombolkemenumypassword: LinearLayout
    private lateinit var tombolkemenuDashboard: LinearLayout
    private lateinit var tombolkemenuBackup: LinearLayout
    private lateinit var tombolkemenuSetting: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_menubackupandrestore)
        val listView = findViewById<ListView>(R.id.listlog)

        val dummyData = listOf(
            logbackupandrestore(R.drawable.logingoogle, "daffa@gmail.com", "2025-04-10"),
            logbackupandrestore(R.drawable.logingoogle, "securein@example.com", "2025-04-11"),
            logbackupandrestore(R.drawable.logingoogle, "daffa@gmail.com", "2025-04-10"),
            logbackupandrestore(R.drawable.logingoogle, "securein@example.com", "2025-04-11"),
            logbackupandrestore(R.drawable.logingoogle, "daffa@gmail.com", "2025-04-10"),
            logbackupandrestore(R.drawable.logingoogle, "securein@example.com", "2025-04-11"),
            logbackupandrestore(R.drawable.logingoogle, "daffa@gmail.com", "2025-04-10"),
            logbackupandrestore(R.drawable.logingoogle, "securein@example.com", "2025-04-11"),
            logbackupandrestore(R.drawable.logingoogle, "daffa@gmail.com", "2025-04-10"),
            logbackupandrestore(R.drawable.logingoogle, "securein@example.com", "2025-04-11"),
            logbackupandrestore(R.drawable.logingoogle, "daffa@gmail.com", "2025-04-10"),
            logbackupandrestore(R.drawable.logingoogle, "securein@example.com", "2025-04-11")
        )

        val adapter = Adapterlog(this, dummyData)
        listView.adapter = adapter
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