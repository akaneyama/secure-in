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
import com.daffaadityapurwanto.securein.Dashboard.NewlyAddedAdapter
import com.daffaadityapurwanto.securein.Dashboard.NewlyAddedItem

class menupassword : AppCompatActivity() {
    data class ItemPassword(
        val logoResId: Int,
        val emailName: String,
        val createdDate: String
    )

    class AdapterPassword(
        private val context: Context,
        private val dataList: List<ItemPassword>
    ) : BaseAdapter() {

        override fun getCount(): Int = dataList.size
        override fun getItem(position: Int): Any = dataList[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.passwordlistdarimenupassword, parent, false)

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
        setContentView(R.layout.activity_menupassword)

        val listView = findViewById<ListView>(R.id.listpasswordtampil)

        val dummyData = listOf(
            ItemPassword(R.drawable.logingoogle, "daffa@gmail.com", "2025-04-10"),
            ItemPassword(R.drawable.logingoogle, "securein@example.com", "2025-04-11"),
            ItemPassword(R.drawable.logingoogle, "daffa@gmail.com", "2025-04-10"),
            ItemPassword(R.drawable.logingoogle, "securein@example.com", "2025-04-11"),
            ItemPassword(R.drawable.logingoogle, "daffa@gmail.com", "2025-04-10"),
            ItemPassword(R.drawable.logingoogle, "securein@example.com", "2025-04-11"),
            ItemPassword(R.drawable.logingoogle, "daffa@gmail.com", "2025-04-10"),
            ItemPassword(R.drawable.logingoogle, "securein@example.com", "2025-04-11"),
            ItemPassword(R.drawable.logingoogle, "daffa@gmail.com", "2025-04-10"),
            ItemPassword(R.drawable.logingoogle, "securein@example.com", "2025-04-11"),
            ItemPassword(R.drawable.logingoogle, "daffa@gmail.com", "2025-04-10"),
            ItemPassword(R.drawable.logingoogle, "securein@example.com", "2025-04-11")
        )

        val adapter = AdapterPassword(this, dummyData)
        listView.adapter = adapter

    }

}