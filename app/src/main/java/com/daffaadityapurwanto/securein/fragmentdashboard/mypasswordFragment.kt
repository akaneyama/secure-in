package com.daffaadityapurwanto.securein.fragmentdashboard

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.*
import com.daffaadityapurwanto.securein.R
import com.daffaadityapurwanto.securein.data.CurrentUser
import com.daffaadityapurwanto.securein.data.databaseHelper
import com.daffaadityapurwanto.securein.data.users
import com.daffaadityapurwanto.securein.encryption.Encrypt
import com.daffaadityapurwanto.securein.encryption.keyAES

class mypasswordFragment : Fragment() {

    // ... Data Class ItemPassword ada di sini ...
    data class ItemPassword(
        val logoResId: Int, val iduserpassword: String, val idservice: String,
        val idpassword: String, val notes: String, val emailName: String,
        val username: String, val password: String, val createdDate: String
    )

    // ... Class AdapterPassword yang sudah dimodifikasi ada di sini ...
    class AdapterPassword(
        private val context: Context,
        private var dataList: MutableList<ItemPassword>
    ) : BaseAdapter() {
        override fun getCount(): Int = dataList.size
        override fun getItem(position: Int): Any = dataList[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.passwordlistdarimenupassword, parent, false)
            val item = dataList[position]
            val user = CurrentUser.user
            val logo = view.findViewById<ImageView>(R.id.logoItem)
            val akunnyanama = view.findViewById<TextView>(R.id.akunname)
            val email = view.findViewById<TextView>(R.id.emailName)
            val date = view.findViewById<TextView>(R.id.createdDate)
            val btnSalin = view.findViewById<ImageView>(R.id.salin)

            logo.setImageResource(item.logoResId)
            akunnyanama.text = item.notes
            email.text = item.emailName
            date.text = "Created: ${item.createdDate}"
            btnSalin.setOnClickListener {
                if (user != null) {
                    val kunciAES = keyAES()
                    val buatkunci = Encrypt(user.kunci_enkripsi, kunciAES.KunciIVKey)
                    val passwordasli = buatkunci.dekripsi(item.password)
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("Password", passwordasli)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Password disalin ke clipboard", Toast.LENGTH_SHORT).show()
                }
            }
            return view
        }

        fun updateData(newList: List<ItemPassword>) {
            dataList.clear()
            dataList.addAll(newList)
            notifyDataSetChanged()
        }
    }

    // Definisikan properti kelas untuk adapter dan helper
    private lateinit var adapter: AdapterPassword
    private lateinit var dbHelper: databaseHelper
    private var currentUser: users? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inisialisasi helper dan user saat view dibuat
        dbHelper = databaseHelper(requireContext())
        currentUser = CurrentUser.user
        return inflater.inflate(R.layout.fragment_mypassword, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ambil referensi ke view
        val listView = view.findViewById<ListView>(R.id.listpasswordtampil)
        val searchEditText = view.findViewById<EditText>(R.id.searchingtext)

        if (currentUser != null) {
            val currentUserId = currentUser!!.id_user

            // 1. Tampilkan semua data saat pertama kali dibuka
            val initialData = ambildatapassword(currentUserId).toMutableList()
            adapter = AdapterPassword(requireContext(), initialData)
            listView.adapter = adapter

            // 2. Pasang listener untuk memantau input di search box
            searchEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // Setiap kali teks berubah, panggil fungsi search
                    val searchQuery = s.toString()
                    val searchResult = dbHelper.searchMyPasswords(currentUserId.toString(), searchQuery)
                    // Update adapter dengan hasil pencarian
                    adapter.updateData(searchResult)
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    // Fungsi ambildatapassword Anda tetap sama
    fun ambildatapassword(idUser: Int): List<ItemPassword> {
        val db = databaseHelper(requireContext()).readableDatabase
        val itemList = mutableListOf<ItemPassword>()
        val query = "SELECT id_user, id_service, id_password, notes, email_password, username_password, password_password, dibuat_pada FROM password_view_lengkap WHERE id_user = ? "
        val cursor = db.rawQuery(query, arrayOf(idUser.toString()))

        if (cursor.moveToFirst()) {
            do {
                val logoResId = R.drawable.privacy
                val id_user = cursor.getString(0)
                val id_service = cursor.getString(1)
                val id_password = cursor.getString(2)
                val notes = cursor.getString(3)
                val email_password = cursor.getString(4)
                var username_password = cursor.getString(5)
                val password_password = cursor.getString(6)
                val tangggaldibuat = cursor.getString(7)
                itemList.add(
                    ItemPassword(
                        logoResId, id_user, id_service, id_password, notes,
                        email_password, username_password, password_password, tangggaldibuat
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return itemList
    }
}