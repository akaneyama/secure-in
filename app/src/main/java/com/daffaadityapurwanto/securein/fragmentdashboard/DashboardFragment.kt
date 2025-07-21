package com.daffaadityapurwanto.securein.fragmentdashboard

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.daffaadityapurwanto.securein.R
import com.daffaadityapurwanto.securein.data.CurrentUser
import com.daffaadityapurwanto.securein.data.NotificationHelper
import com.daffaadityapurwanto.securein.data.databaseHelper
import java.time.LocalTime
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class DashboardFragment : Fragment() {

    // ... (Data Class NewlyAddedItem ada di sini) ...
    data class NewlyAddedItem(
        val logoResId: Int,
        val namaakundashboard: String,
        val emailName: String,
        val createdDate: String
    )

    // ... (Class NewlyAddedAdapter yang sudah dimodifikasi ada di sini) ...
    class NewlyAddedAdapter(
        private val context: Context,
        private var dataList: MutableList<NewlyAddedItem>
    ) : BaseAdapter() {
        override fun getCount(): Int = dataList.size
        override fun getItem(position: Int): Any = dataList[position]
        override fun getItemId(position: Int): Long = position.toLong()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.item_newly_addeddaridashboard, parent, false)
            val item = dataList[position]
            val logo = view.findViewById<ImageView>(R.id.logoItem)
            val akunnamanyaapa = view.findViewById<TextView>(R.id.akunname)
            val email = view.findViewById<TextView>(R.id.emailName)
            val date = view.findViewById<TextView>(R.id.createdDate)
            logo.setImageResource(item.logoResId)
            akunnamanyaapa.text = item.namaakundashboard
            email.text = item.emailName
            date.text = "Created: ${item.createdDate}"
            return view
        }

        fun updateData(newList: List<NewlyAddedItem>) {
            dataList.clear()
            dataList.addAll(newList)
            notifyDataSetChanged()
        }
    }


    // --- Deklarasi Properti Kelas ---
    private lateinit var goodmorning: TextView
    private lateinit var angkapasswordtotal: TextView
    private lateinit var namauser: TextView
    private lateinit var todayyysinkron: TextView
    private lateinit var listView: ListView
    private lateinit var adapter: NewlyAddedAdapter
    private lateinit var dbHelper: databaseHelper

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            // Handle hasil izin di sini jika diperlukan
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inisialisasi helper sekali saja
        dbHelper = databaseHelper(requireContext())
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- Setup satu kali (inisialisasi view dan listener) ---
        goodmorning = view.findViewById(R.id.ucapanselamat)
        angkapasswordtotal = view.findViewById(R.id.angkatotalpassworddashboard)
        namauser = view.findViewById(R.id.namauser)
        todayyysinkron = view.findViewById(R.id.todayyysinkron)
        listView = view.findViewById(R.id.listViewNewlyAdded)

        // Inisialisasi adapter dengan list kosong, data akan diisi nanti
        adapter = NewlyAddedAdapter(requireContext(), mutableListOf())
        listView.adapter = adapter

        askForNotificationPermission()

        val notificationIcon = view.findViewById<ImageView>(R.id.notification)
        notificationIcon.setOnClickListener {
            NotificationHelper.showExpirationWarningNotification(requireContext(), 12)
        }
    }

    override fun onResume() {
        super.onResume()
        // Panggil fungsi refresh data setiap kali fragment ini kembali aktif
        refreshDashboardData()
    }

    private fun refreshDashboardData() {
        // --- Logika untuk memuat ulang semua data dashboard ---
        setGreeting()

        // Ambil ID user dari SharedPreferences sebagai sumber kebenaran utama
        val sharedPref = requireActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val loggedInUserId = sharedPref.getInt("userId", -1)

        if (loggedInUserId != -1) {
            // Gunakan Coroutine untuk menjalankan operasi database di background thread
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                // Ambil semua data dari DB di sini
                val user = dbHelper.getUserDetails(loggedInUserId) // Anda perlu membuat fungsi ini di dbHelper
                val syncData = dbHelper.getLastSuccessfulSyncTimestamp()
                val passwordCount = dbHelper.hitungJumlahPassword(loggedInUserId.toString())
                val newlyAddedItems = getNewlyAddedItems(loggedInUserId)

                // Setelah data siap, kembali ke UI Thread untuk memperbarui tampilan
                launch(Dispatchers.Main) {
                    if (user != null) {
                        namauser.text = user.nama
                    } else {
                        namauser.text = "Guest"
                    }
                    todayyysinkron.text = syncData
                    angkapasswordtotal.text = passwordCount
                    adapter.updateData(newlyAddedItems)
                }
            }
        } else {
            // Kondisi jika user tidak ditemukan di SharedPreferences
            angkapasswordtotal.text = "User tidak ditemukan"
            namauser.text = "Guest"
            adapter.updateData(emptyList()) // Kosongkan list
        }
    }

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

    private fun askForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    fun getNewlyAddedItems(idUser: Int): List<NewlyAddedItem> {
        val itemList = mutableListOf<NewlyAddedItem>()
        val query = "SELECT notes, email_password, dibuat_pada FROM password_view_lengkap WHERE id_user = ? ORDER BY dibuat_pada DESC LIMIT 5 "
        val cursor = dbHelper.readableDatabase.rawQuery(query, arrayOf(idUser.toString()))
        if (cursor.moveToFirst()) {
            do {
                itemList.add(NewlyAddedItem(R.drawable.privacy, cursor.getString(0), cursor.getString(1), cursor.getString(2)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        // Tidak perlu db.close() jika menggunakan instance dbHelper dari kelas
        return itemList
    }
}