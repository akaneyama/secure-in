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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.daffaadityapurwanto.securein.R
import com.daffaadityapurwanto.securein.data.databaseHelper
import com.daffaadityapurwanto.securein.network.BackupRequest
import com.daffaadityapurwanto.securein.network.PasswordData
import com.daffaadityapurwanto.securein.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


class BackuprestoreFragment : Fragment() {

    // Model data untuk log histori
    data class HistoryLog(
        val type: String, // "Backup" atau "Restore"
        val timestamp: String,
        val status: String // "Success" atau "Failed"
    )
    private fun getCurrentWibTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
        return sdf.format(Date())
    }
    // Adapter untuk menampilkan log histori
    class HistoryAdapter(
        private val context: Context,
        private var dataList: List<HistoryLog>
    ) : BaseAdapter() {

        override fun getCount(): Int = dataList.size
        override fun getItem(position: Int): Any = dataList[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.loglistdaribackupandrestore, parent, false)
            val item = dataList[position]

            val logo = view.findViewById<ImageView>(R.id.logoItem)
            val title = view.findViewById<TextView>(R.id.emailName) // Kita gunakan ulang view ini
            val date = view.findViewById<TextView>(R.id.createdDate)

            title.text = "${item.type} - ${item.status}"
            date.text = "At: ${item.timestamp}"
            logo.setImageResource(if (item.type == "Backup") R.drawable.backupiconbackup else R.drawable.restoreiconbackup)

            return view
        }

        fun updateData(newLogs: List<HistoryLog>) {
            this.dataList = newLogs
            notifyDataSetChanged()
        }
    }

    // Deklarasi properti Fragment
    private lateinit var historyListView: ListView
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var dbHelper: databaseHelper
    private lateinit var tvLastSync: TextView // <-- TAMBAHKAN INI
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dbHelper = databaseHelper(requireContext())
        return inflater.inflate(R.layout.fragment_backuprestore, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi View
        historyListView = view.findViewById(R.id.listlog)
        val backupButton = view.findViewById<CardView>(R.id.backup)
        val restoreButton = view.findViewById<CardView>(R.id.restore)
        tvLastSync = view.findViewById(R.id.terakhirsingkronisasi) // <-- TAMBAHKAN INI
        historyListView = view.findViewById(R.id.listlog)
        // Setup adapter dengan data kosong, akan diisi nanti
        historyAdapter = HistoryAdapter(requireContext(), emptyList())
        historyListView.adapter = historyAdapter

        // Setup listener untuk tombol
        backupButton.setOnClickListener {
            showConfirmationDialog("Backup") { performBackup() }
        }

        restoreButton.setOnClickListener {
            showConfirmationDialog("Restore") { performRestore() }
        }
    }

    override fun onResume() {
        super.onResume()
        // Muat histori setiap kali fragment ditampilkan
        loadHistory()
    }

    private fun showConfirmationDialog(action: String, onConfirm: () -> Unit) {
        val message = if (action == "Backup") {
            "Data lokal Anda akan menimpa data di server. Lanjutkan?"
        } else {
            "Data di server akan menimpa data lokal Anda. Lanjutkan?"
        }
        AlertDialog.Builder(requireContext())
            .setTitle("$action Data")
            .setMessage(message)
            .setPositiveButton("Ya, Lanjutkan") { _, _ -> onConfirm() }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun loadHistory() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try { // <- BLOK 'TRY' DIMULAI: Mencoba menjalankan kode yang berisiko

                val logs = dbHelper.getHistoryLogs()
                val lastSyncTimestamp = dbHelper.getLastSuccessfulSyncTimestamp()
                withContext(Dispatchers.Main) {
                    historyAdapter.updateData(logs)
                    // Update TextView untuk sinkronisasi terakhir
                    if (lastSyncTimestamp != null) {
                        tvLastSync.text = lastSyncTimestamp
                    } else {
                        tvLastSync.text = "Belum pernah"
                    }
                }

            } catch (e: Exception) { // <- BLOK 'CATCH': Akan menangkap error jika terjadi

                // Jika terjadi error, kita akan mencetaknya ke Logcat dengan tag khusus
                // Pesan ini tidak akan membuat aplikasi force close
                Log.e("BackupRestoreError", "Error saat memuat histori: ", e)

                // Tampilkan pesan singkat ke pengguna di UI thread
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Gagal memuat histori, silakan cek Logcat.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun performBackup() {
        val sharedPref = requireActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getInt("userId", -1)
        if (currentUserId == -1) {
            Toast.makeText(requireContext(), "Sesi tidak valid.", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(requireContext(), "Memulai proses backup...", Toast.LENGTH_SHORT).show()
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val localPasswords = dbHelper.getAllPasswordsForBackup(currentUserId)
                val passwordsForApi = localPasswords.map { PasswordData(it.idService, it.notes, it.emailName, it.username, it.passwordEncrypted) }
                val request = BackupRequest(userId = currentUserId, passwords = passwordsForApi)
                val response = RetrofitClient.instance.backupPasswords(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Backup berhasil!", Toast.LENGTH_LONG).show()
                        dbHelper.addHistoryLog("Backup", "Success", getCurrentWibTimestamp()) // Tambah log sukses
                    } else {
                        Toast.makeText(requireContext(), "Backup gagal: ${response.message()}", Toast.LENGTH_LONG).show()
                        dbHelper.addHistoryLog("Backup", "Failed", getCurrentWibTimestamp()) // Tambah log gagal
                    }
                    loadHistory() // Refresh list histori
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    dbHelper.addHistoryLog("Backup", "Failed", getCurrentWibTimestamp())
                    loadHistory()
                }
            }
        }
    }

    private fun performRestore() {
        val sharedPref = requireActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getInt("userId", -1)
        if (currentUserId == -1) {
            Toast.makeText(requireContext(), "Sesi tidak valid.", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(requireContext(), "Memulai proses restore...", Toast.LENGTH_SHORT).show()
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.restorePasswords(currentUserId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val passwordsFromServer = response.body()!!
                        dbHelper.overwriteLocalPasswords(currentUserId, passwordsFromServer)
                        Toast.makeText(requireContext(), "Restore berhasil!", Toast.LENGTH_LONG).show()
                        dbHelper.addHistoryLog("Restore", "Success", getCurrentWibTimestamp())
                    } else {
                        Toast.makeText(requireContext(), "Restore gagal: ${response.message()}", Toast.LENGTH_LONG).show()
                        dbHelper.addHistoryLog("Restore", "Failed", getCurrentWibTimestamp())
                    }
                    loadHistory()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    dbHelper.addHistoryLog("Restore", "Failed", getCurrentWibTimestamp())
                    loadHistory()
                }
            }
        }
    }
}