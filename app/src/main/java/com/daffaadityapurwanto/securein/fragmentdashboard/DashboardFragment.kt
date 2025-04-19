package com.daffaadityapurwanto.securein.fragmentdashboard

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.daffaadityapurwanto.securein.R
import com.daffaadityapurwanto.securein.data.CurrentUser
import com.daffaadityapurwanto.securein.data.users
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        goodmorning = view.findViewById(R.id.ucapanselamat)
        angkapasswordtotal = view.findViewById(R.id.angkatotalpassworddashboard)

        setGreeting()
        namauser = view.findViewById(R.id.namauser)
        //panggil dari class
        namauser.text = CurrentUser.user?.nama.toString()

        val listView = view.findViewById<ListView>(R.id.listViewNewlyAdded)


        val dummyData = listOf(
            NewlyAddedItem(R.drawable.logingoogle, "admin@gmail.com", "2025-04-10"),
            NewlyAddedItem(R.drawable.logingoogle, "user01@yahoo.com", "2025-04-09"),
            NewlyAddedItem(R.drawable.logingoogle, "daffa@securein.id", "2025-04-08"),
            NewlyAddedItem(R.drawable.logingoogle, "user01@yahoo.com", "2025-04-09"),
            NewlyAddedItem(R.drawable.logingoogle, "daffa@securein.id", "2025-04-08")
        )

        val adapter = NewlyAddedAdapter(requireContext(), dummyData)
        listView.adapter = adapter

        angkapasswordtotal.text = dummyData.size.toString()




    }
}
