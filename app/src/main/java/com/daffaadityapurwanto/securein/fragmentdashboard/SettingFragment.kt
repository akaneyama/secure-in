package com.daffaadityapurwanto.securein.fragmentdashboard

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.util.Log
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.daffaadityapurwanto.securein.R
import com.daffaadityapurwanto.securein.aboutusmenu
import com.daffaadityapurwanto.securein.data.databaseHelper
import com.daffaadityapurwanto.securein.encryption.Encrypt
import com.daffaadityapurwanto.securein.encryption.keyAES
import com.daffaadityapurwanto.securein.halamanlogin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import android.text.InputType
import android.widget.EditText
import androidx.core.content.FileProvider
import com.daffaadityapurwanto.securein.network.RetrofitClient
import com.daffaadityapurwanto.securein.network.UpdateUserRequest
import java.io.File

class SettingFragment : Fragment() {

    private lateinit var dbHelper: databaseHelper

    // --- Launcher untuk Ekspor/Impor File (Cara Modern Android) ---

    // Launcher untuk memilih lokasi simpan file (Ekspor)
    private val createFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val type = result.data?.getStringExtra("export_type")
                if (type == "encrypted_db") {
                    copyDbToUri(uri)
                } else if (type == "clear_csv") {
                    writePasswordsToCsv(uri)
                }
            }
        }
    }

    // Launcher untuk memilih file yang akan diimpor
    private val openFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                confirmAndImportDb(uri)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dbHelper = databaseHelper(requireContext())
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi semua tombol dan listener
        setupClickListeners(view)
//        setupFingerprintSwitch(view)
    }

    private fun setupClickListeners(view: View) {
        view.findViewById<LinearLayout>(R.id.exporttofile).setOnClickListener { exportEncryptedDb() }
//        view.findViewById<LinearLayout>(R.id.exporttofileunencrypted).setOnClickListener { exportClearCsv() }
//        view.findViewById<LinearLayout>(R.id.importfromfile).setOnClickListener { importDb() }
        view.findViewById<LinearLayout>(R.id.clearLocalData).setOnClickListener { showClearDataConfirmation() }
        view.findViewById<LinearLayout>(R.id.keluarsetting).setOnClickListener { logout() }
        view.findViewById<LinearLayout>(R.id.aboutus).setOnClickListener { openAboutUs() }
        view.findViewById<LinearLayout>(R.id.ubahNama).setOnClickListener { showChangeDialog("nama") } // <-- TAMBAHKAN INI
        view.findViewById<LinearLayout>(R.id.ubahUsername).setOnClickListener { showChangeDialog("username") }
        view.findViewById<LinearLayout>(R.id.ubahEmail).setOnClickListener { showChangeDialog("email") }
        view.findViewById<LinearLayout>(R.id.ubahPassword).setOnClickListener { showChangePasswordDialog() }
    }
    private fun showChangeDialog(field: String) {
        val dialogTitle = "Ubah ${field.capitalize()}"
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(dialogTitle)

        // Buat layout untuk dialog secara dinamis
        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 50, 50, 50)

        val inputNewValue = EditText(requireContext()).apply {
            hint = "${field.capitalize()} Baru"
        }
        val inputCurrentPassword = EditText(requireContext()).apply {
            hint = "Password Saat Ini (untuk verifikasi)"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        layout.addView(inputNewValue)
        layout.addView(inputCurrentPassword)
        builder.setView(layout)

        builder.setPositiveButton("Simpan") { dialog, _ ->
            val newValue = inputNewValue.text.toString().trim()
            val currentPassword = inputCurrentPassword.text.toString().trim()
            if (newValue.isNotEmpty() && currentPassword.isNotEmpty()) {
                updateUserDetails(field, newValue, currentPassword)
            } else {
                Toast.makeText(requireContext(), "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Batal") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun showChangePasswordDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Ubah Password")

        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 50, 50, 50)

        val inputCurrentPassword = EditText(requireContext()).apply {
            hint = "Password Saat Ini"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        val inputNewPassword = EditText(requireContext()).apply {
            hint = "Password Baru"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        val inputConfirmPassword = EditText(requireContext()).apply {
            hint = "Konfirmasi Password Baru"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        layout.addView(inputCurrentPassword)
        layout.addView(inputNewPassword)
        layout.addView(inputConfirmPassword)
        builder.setView(layout)

        builder.setPositiveButton("Simpan") { dialog, _ ->
            val currentPass = inputCurrentPassword.text.toString().trim()
            val newPass = inputNewPassword.text.toString().trim()
            val confirmPass = inputConfirmPassword.text.toString().trim()

            if (newPass != confirmPass) {
                Toast.makeText(requireContext(), "Password baru tidak cocok!", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            if (currentPass.isEmpty() || newPass.isEmpty()) {
                Toast.makeText(requireContext(), "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            updateUserDetails("password", newPass, currentPass)
        }
        builder.setNegativeButton("Batal") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun updateUserDetails(fieldToUpdate: String, newValue: String, currentPasswordPlain: String) {
        val sharedPref = requireActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getInt("userId", -1)
        if (currentUserId == -1) {
            Toast.makeText(requireContext(), "Sesi tidak valid.", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            // Enkripsi password untuk verifikasi dan update
            val kunciAES = keyAES()
            val encryptor = Encrypt(kunciAES.KunciAES128, kunciAES.KunciIVKey)
            val currentPasswordEncrypted = encryptor.enkripsi(currentPasswordPlain)
            val newValueForDb = if (fieldToUpdate == "password") encryptor.enkripsi(newValue) else newValue

            // 1. Verifikasi password saat ini secara lokal dulu
            val isPasswordCorrect = dbHelper.verifyUserPassword(currentUserId, currentPasswordEncrypted)

            if (!isPasswordCorrect) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Password saat ini salah!", Toast.LENGTH_LONG).show()
                }
                return@launch
            }

            // 2. Jika password lokal benar, kirim request ke server
            try {
                val request = UpdateUserRequest(currentUserId, currentPasswordEncrypted, fieldToUpdate, newValueForDb)
                val response = RetrofitClient.instance.updateUser(request)

                if (response.isSuccessful) {
                    // 3. Jika server sukses, update juga database lokal
                    dbHelper.updateUserField(currentUserId, fieldToUpdate, newValueForDb)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "${fieldToUpdate.capitalize()} berhasil diperbarui.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Gagal update di server: ${response.message()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    // --- Implementasi Fitur ---

    private fun exportEncryptedDb() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 1. Tentukan file sumber (database) dan file tujuan (di cache)
                val sourceDbFile = requireContext().getDatabasePath(databaseHelper.DATABASE_NAME)
                val destinationFile = File(requireContext().cacheDir, "securein_backup.db")

                // Pastikan database helper ditutup untuk menyalin file yang stabil
                dbHelper.close()

                // 2. Salin file database ke cache, timpa jika sudah ada
                sourceDbFile.copyTo(destinationFile, overwrite = true)
                Log.d("ExportDB", "Database disalin ke cache: ${destinationFile.absolutePath}")

                // 3. Dapatkan Uri yang aman menggunakan FileProvider
                val contentUri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.provider", // Harus sama dengan 'authorities' di AndroidManifest
                    destinationFile
                )
                Log.d("ExportDB", "Content Uri dibuat: $contentUri")

                // 4. Buat Intent untuk "Bagikan" (ACTION_SEND)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/vnd.sqlite3" // Tipe MIME untuk file .db atau .sqlite3
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Beri izin baca sementara
                    putExtra(Intent.EXTRA_SUBJECT, "SecureIn Database Backup")
                }

                // 5. Jalankan Intent di Main Thread
                withContext(Dispatchers.Main) {
                    startActivity(Intent.createChooser(shareIntent, "Ekspor Database Via..."))
                }

            } catch (e: Exception) {
                Log.e("ExportDB", "Gagal mengekspor database", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Gagal Ekspor: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                // Penting: Buka kembali koneksi dbHelper
                dbHelper = databaseHelper(requireContext())
            }
        }
    }




    private fun importDb() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*" // Izinkan semua tipe file
        }
        openFileLauncher.launch(intent)
    }

    private fun showClearDataConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Semua Data?")
            .setMessage("Apakah Anda yakin ingin menghapus semua data password lokal? Aksi ini tidak bisa dibatalkan.")
            .setIcon(R.drawable.dangerlogin)
            .setPositiveButton("Ya, Hapus") { _, _ -> clearLocalData() }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun clearLocalData() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val sharedPref = requireActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
            val currentUserId = sharedPref.getInt("userId", -1)
            if (currentUserId != -1) {
                dbHelper.clearAllPasswords(currentUserId)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Semua data password lokal berhasil dihapus.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

//    private fun setupFingerprintSwitch(view: View) {
//        val fingerprintSwitch = view.findViewById<Switch>(R.id.fingerprintSwitch)
//        val sharedPrefs = requireActivity().getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
//
//        // Muat pengaturan yang tersimpan
//        fingerprintSwitch.isChecked = sharedPrefs.getBoolean("fingerprint_enabled", false)
//
//        // Simpan pengaturan saat diubah
//        fingerprintSwitch.setOnCheckedChangeListener { _, isChecked ->
//            sharedPrefs.edit().putBoolean("fingerprint_enabled", isChecked).apply()
//            val status = if (isChecked) "diaktifkan" else "dinonaktifkan"
//            Toast.makeText(requireContext(), "Login sidik jari $status", Toast.LENGTH_SHORT).show()
//        }
//    }

    private fun logout() {
        val activity = requireActivity()
        val sharedPref = activity.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()

        val intent = Intent(activity, halamanlogin::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        activity.finish()
    }

    private fun openAboutUs() {
        // Gunakan requireContext() agar lebih aman
        Intent(requireContext(), aboutusmenu::class.java).also {
            startActivity(it)
        }
    }

    // --- Fungsi Bantuan untuk File ---

    // Di dalam SettingFragment.kt

    private fun copyDbToUri(destinationUri: Uri) {
        // Jalankan di luar lifecycleScope utama agar tidak terikat langsung pada view
        // Gunakan Dispatchers.IO untuk operasi file yang berat
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            var success = false
            var errorMessage = ""
            var dbFile: File? = null

            try {
                // --- BAGIAN PENTING DIMULAI ---
                // 1. Dapatkan path SEBELUM menutup database
                val dbPath = requireContext().getDatabasePath(databaseHelper.DATABASE_NAME).absolutePath
                if (dbPath.isNullOrEmpty()) {
                    throw IllegalStateException("Path database tidak valid.")
                }
                dbFile = File(dbPath)

                // 2. Tutup helper untuk melepaskan semua koneksi ke file DB
                Log.d("BackupDB", "Menutup database helper...")
                dbHelper.close()
                // --- BAGIAN PENTING SELESAI ---

                Log.d("BackupDB", "Lokasi file DB: ${dbFile.absolutePath}, exists: ${dbFile.exists()}")

                if (dbFile.exists()) {
                    FileInputStream(dbFile).use { inputStream ->
                        requireActivity().contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                            val totalBytes = inputStream.copyTo(outputStream)
                            if (totalBytes > 0) {
                                success = true
                                Log.d("BackupDB", "Copy berhasil: $totalBytes bytes ditulis.")
                            } else {
                                errorMessage = "Tidak ada data yang ditulis (file 0 byte)."
                                Log.e("BackupDB", errorMessage)
                            }
                        } ?: run {
                            errorMessage = "Gagal membuat OutputStream dari Uri."
                            Log.e("BackupDB", errorMessage)
                        }
                    }
                } else {
                    errorMessage = "File database tidak ditemukan di path: ${dbFile.absolutePath}"
                    Log.e("BackupDB", errorMessage)
                }

            } catch (e: Exception) {
                errorMessage = e.message ?: "Terjadi error tidak dikenal"
                Log.e("BackupDB", "Exception saat backup DB", e)
            } finally {
                // --- BAGIAN PENTING LAINNYA ---
                // 3. Buka kembali koneksi database agar aplikasi bisa berfungsi normal lagi
                Log.d("BackupDB", "Membuka kembali koneksi database...")
                dbHelper = databaseHelper(requireContext())
                // --- SELESAI ---

                // Pindahkan pembaruan UI ke Main thread
                withContext(Dispatchers.Main) {
                    if (success) {
                        Toast.makeText(requireContext(), "Database berhasil diekspor!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(requireContext(), "Gagal ekspor DB: $errorMessage", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }



    private fun writePasswordsToCsv(destinationUri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val sharedPref = requireActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
                val currentUserId = sharedPref.getInt("userId", -1)
                if (currentUserId == -1) return@launch

                val user = dbHelper.getUserDetails(currentUserId) ?: return@launch
                val passwords = dbHelper.getAllPasswordsForBackup(currentUserId)

                val kunciAES = keyAES()
                val decryptor = Encrypt(user.kunci_enkripsi, kunciAES.KunciIVKey)

                requireActivity().contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                    OutputStreamWriter(outputStream).use { writer ->
                        // Header CSV
                        writer.append("Notes,Email,Username,Password\n")
                        // Data
                        passwords.forEach {
                            val decryptedPass = decryptor.dekripsi(it.passwordEncrypted)
                            writer.append("\"${it.notes}\",\"${it.emailName}\",\"${it.username}\",\"${decryptedPass}\"\n")
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Data berhasil diekspor ke file CSV!", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Gagal ekspor CSV: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun confirmAndImportDb(sourceUri: Uri) {
        AlertDialog.Builder(requireContext())
            .setTitle("Impor Database?")
            .setMessage("Ini akan menimpa semua data lokal Anda saat ini dengan data dari file yang dipilih. Lanjutkan?")
            .setPositiveButton("Ya, Impor") { _, _ ->
                try {
                    val dbFile = requireContext().getDatabasePath("securein.db")
                    requireActivity().contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                        FileOutputStream(dbFile).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    Toast.makeText(requireContext(), "Database berhasil diimpor! Silakan restart aplikasi.", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Gagal impor DB: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}