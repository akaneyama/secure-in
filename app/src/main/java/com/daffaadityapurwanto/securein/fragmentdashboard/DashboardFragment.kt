package com.daffaadityapurwanto.securein.fragmentdashboard

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.daffaadityapurwanto.securein.R
import com.daffaadityapurwanto.securein.data.CurrentUser
import com.daffaadityapurwanto.securein.data.databaseHelper
import java.time.LocalTime

class DashboardFragment : Fragment() {

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
    private lateinit var namauser: TextView
    private lateinit var todayyysinkron: TextView

    private fun setGreeting() {
        val now = LocalTime.now()
        val greeting = when (now.hour) {
            in 5..10 -> "Hello, Good Morning"
            in 11..17 -> "Hello, Good Afternoon"
            in 18..22 -> "Hello, Good Night"
            else -> "Good Night, Don't Forget To Sleep"
        }
        goodmorning.text = greeting
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dbHelper = databaseHelper(requireContext())
        goodmorning = view.findViewById(R.id.ucapanselamat)
        angkapasswordtotal = view.findViewById(R.id.angkatotalpassworddashboard)
        namauser = view.findViewById(R.id.namauser)
        todayyysinkron = view.findViewById(R.id.todayyysinkron)
        setGreeting()

        val user = CurrentUser.user
        if (user != null) {
            namauser.text = user.nama
            todayyysinkron.text = dbHelper.ambildatasinkron(user.id_user.toString())
            angkapasswordtotal.text = dbHelper.hitungJumlahPassword(user.id_user.toString())

            val listView = view.findViewById<ListView>(R.id.listViewNewlyAdded)
            val dataList = getNewlyAddedItems(user.id_user)
            val adapter = NewlyAddedAdapter(requireContext(), dataList)
            listView.adapter = adapter
        } else {
            angkapasswordtotal.text = "User tidak ditemukan"
        }
    }

    fun getNewlyAddedItems(idUser: Int): List<NewlyAddedItem> {
        val db = databaseHelper(requireContext()).readableDatabase
        val itemList = mutableListOf<NewlyAddedItem>()
        val query = "SELECT email_password, dibuat_pada FROM password_view_lengkap WHERE id_user = ? ORDER BY dibuat_pada DESC LIMIT 5"
        //val query = "SELECT nama_service,email_password, dibuat_pada FROM password_view_lengkap WHERE id_user = ? ORDER BY dibuat_pada DESC LIMIT 5"
        val cursor = db.rawQuery(query, arrayOf(idUser.toString()))

        if (cursor.moveToFirst()) {
            do {
                val logoResId = R.drawable.logingoogle
                val email = cursor.getString(0)
                val createdDate = cursor.getString(1)
                itemList.add(NewlyAddedItem(logoResId, email, createdDate))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return itemList
    }

}

