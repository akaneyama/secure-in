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
import com.daffaadityapurwanto.securein.fragmentdashboard.DashboardFragment.NewlyAddedItem


class mypasswordFragment : Fragment() {
    data class ItemPassword(
        val logoResId: Int,
        val iduserpassword: String,
        val idservice: String,
        val idpassword: String,
        val notes: String,
        val emailName: String,
        val username: String,
        val password: String,
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
            val akunnyanama = view.findViewById<TextView>(R.id.akunname)
            val email = view.findViewById<TextView>(R.id.emailName)
            val date = view.findViewById<TextView>(R.id.createdDate)


            logo.setImageResource(item.logoResId)
            akunnyanama.text = item.notes
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
//    val logoResId: Int,
//    val iduserpassword: Int,
//    val idservice: Int,
//    val idpassword: Int,
//    val notes: String,
//    val emailName: String,
//    val username: String,
//    val password: String,
//    val createdDate: String
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = CurrentUser.user
        if (user != null) {
            val listView = view.findViewById<ListView>(R.id.listpasswordtampil)
            val datalist = ambildatapassword(user.id_user)
            val adapter = AdapterPassword(requireContext(),datalist)
            listView.adapter = adapter
        }


//        val adapter = AdapterPassword(requireContext(), dummyData)
//        listView.adapter = adapter
    }

    fun ambildatapassword(idUser: Int): List<ItemPassword> {
        val db = databaseHelper(requireContext()).readableDatabase
        val itemList = mutableListOf<ItemPassword>()
        val query = "SELECT id_user, id_service, id_password, notes, email_password, username_password, password_password, dibuat_pada FROM password_view_lengkap WHERE id_user = ? "
        val cursor = db.rawQuery(query, arrayOf(idUser.toString()))

        if (cursor.moveToFirst()) { do{
            val logoResId = R.drawable.logingoogle
            val id_user = cursor.getString(0)
            val id_service = cursor.getString(1)
            val id_password = cursor.getString(2)
            val notes = cursor.getString(3)
            val email_password = cursor.getString(4)
            var username_password = cursor.getString(5)
            val password_password = cursor.getString(6)
            val tangggaldibuat = cursor.getString(7)
                itemList.add(ItemPassword(
                    logoResId,
                    id_user,
                    id_service,
                    id_password,
                    notes,
                    email_password,
                    username_password,
                    password_password,
                    tangggaldibuat
                ))
            }
            while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return itemList
    }

}