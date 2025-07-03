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
import com.daffaadityapurwanto.securein.R
import com.daffaadityapurwanto.securein.data.CurrentUser
import com.daffaadityapurwanto.securein.data.databaseHelper
import com.daffaadityapurwanto.securein.data.users
import com.daffaadityapurwanto.securein.encryption.Encrypt
import com.daffaadityapurwanto.securein.encryption.keyAES
import com.daffaadityapurwanto.securein.menutambahdanedit

class mypasswordFragment : Fragment() {

    // Data class untuk menampung data setiap item password
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

    // Adapter untuk ListView
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

            // Inisialisasi semua view di dalam item list
            val logo = view.findViewById<ImageView>(R.id.logoItem)
            val akunnyanama = view.findViewById<TextView>(R.id.akunname)
            val email = view.findViewById<TextView>(R.id.emailName)
            val date = view.findViewById<TextView>(R.id.createdDate)
            val btnSalin = view.findViewById<ImageView>(R.id.salin)
            val btnMenu = view.findViewById<ImageView>(R.id.titik3menu)

            // Set data ke view
            logo.setImageResource(item.logoResId)
            akunnyanama.text = item.notes
            email.text = item.emailName
            date.text = "Created: ${item.createdDate}"

            // Listener untuk tombol salin password
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

            // Listener untuk tombol menu (titik tiga)
            btnMenu.setOnClickListener { anchorView ->
                showPopupMenu(anchorView, item, position)
            }

            return view
        }

        // Fungsi untuk menampilkan menu popup Edit/Delete
        private fun showPopupMenu(anchorView: View, item: ItemPassword, position: Int) {
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

        // Fungsi untuk menampilkan dialog konfirmasi sebelum menghapus
        private fun showDeleteConfirmationDialog(item: ItemPassword, position: Int) {
            AlertDialog.Builder(context)
                .setTitle("Hapus Password")
                .setMessage("Apakah Anda yakin ingin menghapus data untuk '${item.notes}'?")
                .setPositiveButton("Hapus") { _, _ ->
                    val dbHelper = databaseHelper(context)
                    dbHelper.deletePasswordById(item.idpassword)
                    dataList.removeAt(position)
                    notifyDataSetChanged()
                    Toast.makeText(context, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        // Fungsi untuk memperbarui data list (digunakan oleh fitur search)
        fun updateData(newList: List<ItemPassword>) {
            dataList.clear()
            dataList.addAll(newList)
            notifyDataSetChanged()
        }
    }

    // Properti untuk Fragment
    private lateinit var adapter: AdapterPassword
    private lateinit var dbHelper: databaseHelper
    private var currentUser: users? = null
    private lateinit var listView: ListView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dbHelper = databaseHelper(requireContext())
        currentUser = CurrentUser.user
        return inflater.inflate(R.layout.fragment_mypassword, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView = view.findViewById(R.id.listpasswordtampil)
        val searchEditText = view.findViewById<EditText>(R.id.searchingtext)

        // Hanya jalankan jika user sudah login
        if (currentUser != null) {
            val currentUserId = currentUser!!.id_user
            val initialData = ambildatapassword(currentUserId).toMutableList()

            // Inisialisasi adapter dengan data awal
            adapter = AdapterPassword(requireContext(), initialData)
            listView.adapter = adapter

            // Listener untuk search
            searchEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val searchQuery = s.toString()
                    val searchResult = dbHelper.searchMyPasswords(currentUserId.toString(), searchQuery)
                    adapter.updateData(searchResult)
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data saat fragment kembali aktif (misalnya setelah selesai mengedit)
        if (currentUser != null) {
            val freshData = ambildatapassword(currentUser!!.id_user)
            if (::adapter.isInitialized) {
                adapter.updateData(freshData)
            }
        }
    }

    // Fungsi untuk mengambil semua data password dari database
    fun ambildatapassword(idUser: Int): List<ItemPassword> {
        val db = databaseHelper(requireContext()).readableDatabase
        val itemList = mutableListOf<ItemPassword>()
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
        db.close()
        return itemList
    }
}