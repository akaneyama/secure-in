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



class mypasswordFragment : Fragment() {
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mypassword, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listView = view.findViewById<ListView>(R.id.listpasswordtampil)

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

        val adapter = AdapterPassword(requireContext(), dummyData)
        listView.adapter = adapter
    }


}