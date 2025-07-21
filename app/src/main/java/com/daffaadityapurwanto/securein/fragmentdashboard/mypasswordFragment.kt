package com.daffaadityapurwanto.securein.fragmentdashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.daffaadityapurwanto.securein.R
import com.daffaadityapurwanto.securein.data.databaseHelper
import com.daffaadityapurwanto.securein.data.users
import com.daffaadityapurwanto.securein.encryption.Encrypt
import com.daffaadityapurwanto.securein.encryption.keyAES
import com.daffaadityapurwanto.securein.menutambahdanedit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class mypasswordFragment : Fragment() {

    // ... (Data class ItemPassword tidak berubah)
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

    // ... (AdapterPassword tidak banyak berubah, hanya penyesuaian kecil pada delete)
    class AdapterPassword(
        private val context: Context,
        private var dataList: MutableList<ItemPassword>,
        private val fragment: mypasswordFragment // Tambahkan referensi ke fragment
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
            val btnSalin = view.findViewById<ImageView>(R.id.salin)
            val btnMenu = view.findViewById<ImageView>(R.id.titik3menu)

            logo.setImageResource(item.logoResId)
            akunnyanama.text = item.notes
            email.text = item.emailName
            date.text = "Created: ${item.createdDate}"

            btnSalin.setOnClickListener {
                fragment.lifecycleScope.launch(Dispatchers.IO) {
                    val user = databaseHelper(context).getUserDetails(item.iduserpassword.toInt())
                    if (user != null) {
                        val kunciAES = keyAES()
                        val buatkunci = Encrypt(user.kunci_enkripsi, kunciAES.KunciIVKey)
                        val passwordasli = buatkunci.dekripsi(item.password)
                        withContext(Dispatchers.Main) {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Password", passwordasli)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Password disalin ke clipboard", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            btnMenu.setOnClickListener { anchorView -> showPopupMenu(anchorView, item, position) }
            return view
        }

        private fun showPopupMenu(anchorView: View, item: ItemPassword, position: Int) {
            // ... (Fungsi ini tidak berubah)
            val popup = PopupMenu(context, anchorView)
            popup.inflate(R.menu.password_options_menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_edit -> {
                        val intent = Intent(context, menutambahdanedit::class.java).apply {
                            putExtra("PASSWORD_ID", item.idpassword)
                        }
                        context.startActivity(intent)
                        true
                    }
                    R.id.action_delete -> {
                        showDeleteConfirmationDialog(item, position)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        private fun showDeleteConfirmationDialog(item: ItemPassword, position: Int) {
            AlertDialog.Builder(context)
                .setTitle("Hapus Password")
                .setMessage("Apakah Anda yakin ingin menghapus data untuk '${item.notes}'?")
                .setPositiveButton("Hapus") { _, _ ->
                    fragment.lifecycleScope.launch(Dispatchers.IO) {
                        databaseHelper(context).deletePasswordById(item.idpassword)
                        withContext(Dispatchers.Main) {
                            dataList.removeAt(position)
                            notifyDataSetChanged()
                            Toast.makeText(context, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        fun updateData(newList: List<ItemPassword>) {
            dataList.clear()
            dataList.addAll(newList)
            notifyDataSetChanged()
        }
    }

    private lateinit var adapter: AdapterPassword
    private lateinit var dbHelper: databaseHelper
    private lateinit var listView: ListView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dbHelper = databaseHelper(requireContext())
        return inflater.inflate(R.layout.fragment_mypassword, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView = view.findViewById(R.id.listpasswordtampil)
        val searchEditText = view.findViewById<EditText>(R.id.searchingtext)

        // Inisialisasi adapter dengan list kosong, data akan di-load di onResume
        adapter = AdapterPassword(requireContext(), mutableListOf(), this)
        listView.adapter = adapter

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loadPasswordData(s.toString()) // Panggil fungsi load dengan query pencarian
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onResume() {
        super.onResume()
        loadPasswordData() // Muat semua data saat fragment kembali aktif
    }

    private fun loadPasswordData(searchQuery: String = "") {
        val sharedPref = requireActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getInt("userId", -1)

        if (currentUserId != -1) {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                // Operasi DB berjalan di background
                val data = if (searchQuery.isBlank()) {
                    ambildatapassword(currentUserId)
                } else {
                    dbHelper.searchMyPasswords(currentUserId.toString(), searchQuery)
                }

                // Update UI kembali di Main thread
                withContext(Dispatchers.Main) {
                    if (::adapter.isInitialized) {
                        adapter.updateData(data)
                    }
                }
            }
        }
    }

    fun ambildatapassword(idUser: Int): List<ItemPassword> {
        val itemList = mutableListOf<ItemPassword>()
        // Gunakan dbHelper instance yang sudah ada, jangan buat baru
        val db = dbHelper.readableDatabase
        val query = "SELECT id_user, id_service, id_password, notes, email_password, username_password, password_password, dibuat_pada FROM password_view_lengkap WHERE id_user = ?"
        val cursor = db.rawQuery(query, arrayOf(idUser.toString()))

        if (cursor.moveToFirst()) {
            do {
                itemList.add(ItemPassword(
                    logoResId = R.drawable.privacy,
                    iduserpassword = cursor.getString(0),
                    idservice = cursor.getString(1),
                    idpassword = cursor.getString(2),
                    notes = cursor.getString(3),
                    emailName = cursor.getString(4),
                    username = cursor.getString(5),
                    password = cursor.getString(6),
                    createdDate = cursor.getString(7)
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        // JANGAN PANGGIL db.close() DI SINI
        return itemList
    }
}