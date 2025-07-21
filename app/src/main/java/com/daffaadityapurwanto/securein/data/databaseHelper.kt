package com.daffaadityapurwanto.securein.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import com.daffaadityapurwanto.securein.data.users
import com.daffaadityapurwanto.securein.encryption.Encrypt
import com.daffaadityapurwanto.securein.encryption.keyAES
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import android.util.Base64
import com.daffaadityapurwanto.securein.R
import com.daffaadityapurwanto.securein.fragmentdashboard.BackuprestoreFragment
import com.daffaadityapurwanto.securein.fragmentdashboard.mypasswordFragment
import com.daffaadityapurwanto.securein.network.LoginResponse
import com.daffaadityapurwanto.securein.network.PasswordData


class databaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        companion object {
            private const val DATABASE_NAME = "secureindb.db"
            private const val DATABASE_VERSION = 1
        }

        private val dbPath: String = context.getDatabasePath(DATABASE_NAME).path

    // Letakkan fungsi ini di dalam class databaseHelper

    // Tambahkan 3 fungsi ini di dalam class databaseHelper

    fun deletePasswordById(idPassword: String): Int {
        val db = this.writableDatabase
        val result = db.delete("password", "id_password = ?", arrayOf(idPassword))
        
        return result
    }

    // Tambahkan 2 fungsi ini di dalam class databaseHelper

    fun getPasswordDetails(idPassword: String): com.daffaadityapurwanto.securein.fragmentdashboard.mypasswordFragment.ItemPassword? {
        val db = this.readableDatabase
        var item: com.daffaadityapurwanto.securein.fragmentdashboard.mypasswordFragment.ItemPassword? = null
        val query = "SELECT id_user, id_service, id_password, notes, email_password, username_password, password_password, dibuat_pada FROM password_view_lengkap WHERE id_password = ?"
        val cursor = db.rawQuery(query, arrayOf(idPassword))

        if (cursor.moveToFirst()) {
            item = com.daffaadityapurwanto.securein.fragmentdashboard.mypasswordFragment.ItemPassword(
                logoResId = R.drawable.privacy, // Ikon default
                iduserpassword = cursor.getString(0),
                idservice = cursor.getString(1),
                idpassword = cursor.getString(2),
                notes = cursor.getString(3),
                emailName = cursor.getString(4),
                username = cursor.getString(5),
                password = cursor.getString(6),
                createdDate = cursor.getString(7)
            )
        }
        cursor.close()
        
        return item
    }
    // Contoh fungsi yang perlu Anda tambahkan di databaseHelper.kt

    fun getUserDetails(userId: Int): users? {
        val db = this.readableDatabase
        // 1. UBAH QUERY: Ambil semua kolom yang dibutuhkan, termasuk username dan password
        val cursor = db.rawQuery("SELECT id_user, uid, kunci_enkripsi, email, nama, username, password FROM users WHERE id_user = ?", arrayOf(userId.toString()))

        var user: users? = null

        if (cursor.moveToFirst()) {
            val idUser = cursor.getInt(cursor.getColumnIndexOrThrow("id_user"))
            val uid = cursor.getString(cursor.getColumnIndexOrThrow("uid"))
            val kunciEnkripsi = cursor.getString(cursor.getColumnIndexOrThrow("kunci_enkripsi"))
            val email = cursor.getString(cursor.getColumnIndexOrThrow("email"))
            val nama = cursor.getString(cursor.getColumnIndexOrThrow("nama"))

            // 2. TAMBAHKAN: Ambil username dan password dari cursor
            val username = cursor.getString(cursor.getColumnIndexOrThrow("username"))
            val password = cursor.getString(cursor.getColumnIndexOrThrow("password"))

            // 3. LENGKAPI: Masukkan semua parameter saat membuat objek users
            user = users(idUser, uid, kunciEnkripsi, email, nama, username, password)
        }

        cursor.close()
        return user
    }
    fun updatePassword(idPassword: String, email: String, username: String, newEncryptedPass: String, notes: String): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("id_service", 3) // Tambahkan baris ini
            put("email", email)
            put("username", username)
            put("password", newEncryptedPass)
            put("notes", notes)
        }
        val result = db.update("password", values, "id_password = ?", arrayOf(idPassword))
        // Tidak perlu () di sini
        return result
    }

    // Tambahkan data class sederhana ini di dalam file databaseHelper.kt atau di file model terpisah
    data class PasswordForBackup(
        val idService: String,
        val notes: String,
        val emailName: String,
        val username: String,
        val passwordEncrypted: String
    )

    // Tambahkan fungsi ini di dalam class databaseHelper
    fun getAllPasswordsForBackup(idUser: Int): List<PasswordForBackup> {
        val itemList = mutableListOf<PasswordForBackup>()
        val db = this.readableDatabase
        // Query ini mengambil data dari tabel 'password'
        val query = "SELECT id_service, notes, email, username, password FROM password WHERE id_user = ?"
        val cursor = db.rawQuery(query, arrayOf(idUser.toString()))

        if (cursor.moveToFirst()) {
            do {
                // Pastikan indeks kolomnya benar
                itemList.add(PasswordForBackup(
                    idService = cursor.getString(0),
                    notes = cursor.getString(1),       // Kolom 'notes'
                    emailName = cursor.getString(2),   // Kolom 'email'
                    username = cursor.getString(3),    // Kolom 'username'
                    passwordEncrypted = cursor.getString(4) // Kolom 'password'
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return itemList
    }
// Di dalam class databaseHelper

    // Panggil ini di dalam onCreate(db: SQLiteDatabase) jika Anda membuat tabel dari awal
    fun createHistoryTable(db: SQLiteDatabase) {
        val CREATE_HISTORY_TABLE = """
        CREATE TABLE history_log (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            log_type TEXT,
            status TEXT,
            timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
        )
    """.trimIndent()
        db.execSQL(CREATE_HISTORY_TABLE)
    }
    fun getLastSuccessfulSyncTimestamp(): String? {
        val db = this.readableDatabase
        var timestamp: String? = null

        // Query untuk mengambil timestamp dari log terakhir yang statusnya 'Success'
        val cursor = db.rawQuery(
            "SELECT timestamp FROM history_log WHERE status = 'Success' ORDER BY timestamp DESC LIMIT 1",
            null
        )

        if (cursor.moveToFirst()) {
            timestamp = cursor.getString(0)
        }

        cursor.close()
        return timestamp
    }
    fun addHistoryLog(type: String, status: String, timestamp: String) { // <-- 1. Tambahkan parameter ketiga
        val db = this.writableDatabase
        val values = android.content.ContentValues().apply {
            put("log_type", type)
            put("status", status)
            put("timestamp", timestamp) // <-- 2. Masukkan timestamp ke database
        }
        db.insert("history_log", null, values)
    }
    fun getUserByEmail(email: String): users? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users WHERE email = ?", arrayOf(email))
        var user: users? = null
        if (cursor.moveToFirst()) {
            user = users(
                id_user = cursor.getInt(cursor.getColumnIndexOrThrow("id_user")),
                uid = cursor.getString(cursor.getColumnIndexOrThrow("uid")),
                kunci_enkripsi = cursor.getString(cursor.getColumnIndexOrThrow("kunci_enkripsi")),
                email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                nama = cursor.getString(cursor.getColumnIndexOrThrow("nama")),
                username = cursor.getString(cursor.getColumnIndexOrThrow("username")),
                password = cursor.getString(cursor.getColumnIndexOrThrow("password")) // Password terenkripsi
            )
        }
        cursor.close()
        return user
    }
    fun getHistoryLogs(): List<BackuprestoreFragment.HistoryLog> {
        val logList = mutableListOf<BackuprestoreFragment.HistoryLog>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT log_type, status, timestamp FROM history_log ORDER BY timestamp DESC", null)
        if (cursor.moveToFirst()) {
            do {
                logList.add(BackuprestoreFragment.HistoryLog(
                    type = cursor.getString(0),
                    status = cursor.getString(1),
                    timestamp = cursor.getString(2)
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return logList
    }

    // Di dalam file databaseHelper.kt

    fun overwriteLocalPasswords(userId: Int, passwordsFromServer: List<PasswordData>) {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            db.delete("password", "id_user = ?", arrayOf(userId.toString()))
            val insertQuery = "INSERT INTO password (id_user, id_service, email, username, password, notes) VALUES (?, ?, ?, ?, ?, ?)"
            val statement = db.compileStatement(insertQuery)

            for (item in passwordsFromServer) {
                statement.clearBindings()
                statement.bindLong(1, userId.toLong())

                // TAMBAHKAN PENGECEKAN INI:
                // Jika idService dari server null, gunakan nilai default "3"
                val serviceId = item.idService ?: "3"
                statement.bindString(2, serviceId)

                statement.bindString(3, item.emailName ?: "") // Tambahkan juga untuk field lain yg mungkin null
                statement.bindString(4, item.username ?: "")
                statement.bindString(5, item.passwordEncrypted ?: "")
                statement.bindString(6, item.notes ?: "")
                statement.executeInsert()
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
    fun searchMyPasswords(idUser: String, searchText: String): List<mypasswordFragment.ItemPassword> {
        val db = this.readableDatabase
        val itemList = mutableListOf<mypasswordFragment.ItemPassword>()

        // Query untuk mencari di kolom 'notes' (nama layanan) dan 'email_password'
        val query = """
        SELECT id_user, id_service, id_password, notes, email_password, username_password, password_password, dibuat_pada 
        FROM password_view_lengkap 
        WHERE id_user = ? AND (notes LIKE ? OR email_password LIKE ?)
    """.trimIndent()

        // Tanda '%' adalah wildcard untuk pencarian teks
        val cursor = db.rawQuery(query, arrayOf(idUser, "%$searchText%", "%$searchText%"))

        if (cursor.moveToFirst()) {
            do {
                val logoResId = R.drawable.privacy
                val id_user = cursor.getString(0)
                val id_service = cursor.getString(1)
                val id_password = cursor.getString(2)
                val notes = cursor.getString(3)
                val email_password = cursor.getString(4)
                val username_password = cursor.getString(5)
                val password_password = cursor.getString(6)
                val tangggaldibuat = cursor.getString(7)
                itemList.add(
                    mypasswordFragment.ItemPassword(
                        logoResId, id_user, id_service, id_password, notes,
                        email_password, username_password, password_password, tangggaldibuat
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        
        return itemList
    }

    fun copyDatabaseIfNeeded(context: Context) {
        val databaseFile = context.getDatabasePath("secureindb.db")

        if (!databaseFile.exists()) {
            try {
                databaseFile.parentFile?.mkdirs()
                copyDatabaseFromAssets(context)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
//    else{
//        Toast.makeText(context, "Database sudah ada di folder databases", Toast.LENGTH_SHORT).show()
//    }
    }

    private fun copyDatabaseFromAssets(context: Context) {
        val databaseFile = context.getDatabasePath("secureindb.db")
        val inputStream: InputStream = context.assets.open("secureindb.db")
        val outputStream: OutputStream = FileOutputStream(databaseFile)

        try {
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }

            outputStream.flush()
        } catch (e: IOException) {
            e.printStackTrace()
            throw IOException("Error copying database")
            // Toast.makeText(context, "Gagal Copy databases", Toast.LENGTH_SHORT).show()
        } finally {
            inputStream.close()
            outputStream.close()
        }
    }
    override fun onCreate(db: SQLiteDatabase) {
        // ndak usah buat table
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Kosongkan soale gak perlu migrasi
    }
    fun openDatabase(): SQLiteDatabase {
        return SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE)
    }


    fun loginandcheckuser(username: String, email: String, password: String): users? {
        // 1. Gunakan readableDatabase standar untuk konsistensi
        val db = this.readableDatabase

        // 2. PERBAIKI QUERY: Tambahkan kurung untuk logika yang benar dan ambil semua kolom
        val query = "SELECT id_user, uid, kunci_enkripsi, email, nama, username, password FROM users WHERE (username = ? OR email = ?) AND password = ?"
        val cursor = db.rawQuery(query, arrayOf(username, email, password))

        var user: users? = null
        if (cursor.moveToFirst()) {
            val id_user = cursor.getInt(cursor.getColumnIndexOrThrow("id_user"))
            val uid = cursor.getString(cursor.getColumnIndexOrThrow("uid"))
            val kunci_enkripsi = cursor.getString(cursor.getColumnIndexOrThrow("kunci_enkripsi"))
            val emailResult = cursor.getString(cursor.getColumnIndexOrThrow("email"))
            val nama = cursor.getString(cursor.getColumnIndexOrThrow("nama"))
            // 3. TAMBAHKAN: Ambil username dan password dari cursor
            val usernameResult = cursor.getString(cursor.getColumnIndexOrThrow("username"))
            val passwordResult = cursor.getString(cursor.getColumnIndexOrThrow("password"))

            // 4. LENGKAPI: Panggil constructor dengan semua parameter
            user = users(id_user, uid, kunci_enkripsi, emailResult, nama, usernameResult, passwordResult)

            // Lakukan hal yang sama untuk CurrentUser
            CurrentUser.user = user
        }
        cursor.close()
        return user
    }
    fun generateRandomKeyString(length: Int = 16): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { allowedChars[SecureRandom().nextInt(allowedChars.length)] }
            .joinToString("")
    }

    fun insertOrUpdateUser(user: LoginResponse) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("id_user", user.id_user)
            put("uid", user.uid)
            put("kunci_enkripsi", user.kunci_enkripsi)
            put("email", user.email)
            put("nama", user.nama)
            put("username", user.username)
            put("password", user.password) // Password dari server sudah terenkripsi
        }
        // "replace" akan melakukan INSERT jika id_user belum ada, atau UPDATE jika sudah ada.
        db.replace("users", null, values)
    }

    fun tambahkanUser(idUser: Int, email: String, nama: String, username: String, passwordNonEnkripsi: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        val kunciAES = keyAES()
        val encryptor = Encrypt(kunciAES.KunciAES128, kunciAES.KunciIVKey)
        val kunciBase64 = generateRandomKeyString()

        // Sekarang kita masukkan id_user yang didapat dari server
        values.put("id_user", idUser)
        values.put("uid", "0")
        values.put("kunci_enkripsi", kunciBase64)
        values.put("email", email)
        values.put("nama", nama)
        values.put("username", username)
        values.put("password", encryptor.enkripsi(passwordNonEnkripsi))

        val result = db.insert("users", null, values)
        return result != -1L
    }
    fun insertTopasswordDatabase(idUser: String, email: String, username: String, password: String, notes: String) {
        val db = this.writableDatabase

        // 1. Tambahkan kolom "id_service" ke dalam query
        val query = "INSERT INTO password (id_user, id_service, email, username, password, notes) VALUES (?, ?, ?, ?, ?, ?)"
        val statement = db.compileStatement(query)

        // 2. Sesuaikan urutan binding dan tambahkan nilai "3" untuk id_service
        statement.bindString(1, idUser)
        statement.bindString(2, "3") // id_service diatur statis ke 3
        statement.bindString(3, email)
        statement.bindString(4, username)
        statement.bindString(5, password)
        statement.bindString(6, notes)

        try {
            statement.executeInsert()
        } catch (e: Exception) {
            // Handle exception jika perlu
        } finally {
            // Tidak perlu () di sini
        }
    }

    fun cekUsernameAtauEmailSudahAda(username: String, email: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM users WHERE username = ? OR email = ?"
        val cursor = db.rawQuery(query, arrayOf(username, email))

        val sudahAda = cursor.count > 0
        cursor.close()
        

        return sudahAda
    }

    fun hitungJumlahPassword(idUser: String): String {
        val db = this.readableDatabase
        val query = "SELECT COUNT(*) FROM password_view_lengkap WHERE id_user = ?"
        val cursor = db.rawQuery(query, arrayOf(idUser))

        var jumlah = "0"
        if (cursor.moveToFirst()) {
            jumlah = cursor.getString(0)  // Mengambil nilai hasil COUNT
        }
        cursor.close()
        

        return jumlah
    }
    fun ambildatasinkron(idUser: String): String {
        val db = this.readableDatabase
        val query = "SELECT sinkronisasi.tanggal_sinkron FROM password_view_lengkap JOIN sinkronisasi ON password_view_lengkap.id_sinkronisasi = sinkronisasi.id_sinkronisasi WHERE password_view_lengkap.id_user = ? AND password_view_lengkap.status_sinkron = '1' ORDER BY sinkronisasi.tanggal_sinkron LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(idUser))

        var jumlah = "0"
        if (cursor.moveToFirst()) {
            jumlah = cursor.getString(0)  // Mengambil nilai hasil COUNT
        }
        cursor.close()
        

        return jumlah
    }
}