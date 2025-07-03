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
import com.daffaadityapurwanto.securein.fragmentdashboard.mypasswordFragment


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
        db.close()
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
        db.close()
        return item
    }

    fun updatePassword(idPassword: String, idService: String, email: String, username: String, newEncryptedPass: String, notes: String): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("id_service", idService)
            put("email", email)
            put("username", username)
            put("password", newEncryptedPass)
            put("notes", notes)
        }
        val result = db.update("password", values, "id_password = ?", arrayOf(idPassword))
        db.close()
        return result
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
        db.close()
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

    fun loginandcheckuser(username: String,email: String, password: String): users? {
        val db = openDatabase()
        val query = "SELECT id_user, uid, kunci_enkripsi, email, nama FROM users WHERE username = ? OR email = ? AND password = ?"
        val cursor = db.rawQuery(query, arrayOf(username, email,password))

        var user: users? = null
        if (cursor.moveToFirst()) {
            val id_user = cursor.getInt(cursor.getColumnIndexOrThrow("id_user"))
            val uid = cursor.getString(cursor.getColumnIndexOrThrow("uid"))
            val kunci_enkripsi = cursor.getString(cursor.getColumnIndexOrThrow("kunci_enkripsi"))
            val email = cursor.getString(cursor.getColumnIndexOrThrow("email"))
            val nama = cursor.getString(cursor.getColumnIndexOrThrow("nama"))

            user = users(id_user, uid,kunci_enkripsi, email,nama)

            //tambahkan user saat ini
            CurrentUser.user = users(
                id_user = id_user,
                uid = uid,
                kunci_enkripsi = kunci_enkripsi,
                email = email,
                nama = nama
            )
        }
        cursor.close()
        db.close()
        return user
    }
    fun generateRandomKeyString(length: Int = 16): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { allowedChars[SecureRandom().nextInt(allowedChars.length)] }
            .joinToString("")
    }
    fun tambahkanUser(email: String, nama: String,  username: String, password: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        val kunciAES = keyAES()
        val encrypt = Encrypt(kunciAES.KunciAES128,kunciAES.KunciIVKey)
        val kunciBase64 = generateRandomKeyString()
        values.put("uid","0")
        values.put("kunci_enkripsi",kunciBase64)
        values.put("email", email)
        values.put("nama", nama)
        values.put("username", username)
        values.put("password", encrypt.enkripsi(password))

        val result = db.insert("users", null, values)
        db.close()
        return result != -1L
    }
    fun insertTopasswordDatabase(idUser:String, idService: String, email: String ,username: String, password: String, notes:String ) {
        val db = this.writableDatabase

        val query = "INSERT INTO password (id_user, id_service, email, username, password, notes) VALUES (?, ?, ?, ?, ?, ?)"
        val statement = db.compileStatement(query)
        statement.bindString(1, idUser)
        statement.bindString(2, idService)
        statement.bindString(3, email)
        statement.bindString(4, username)
        statement.bindString(5, password)
        statement.bindString(6, notes)

        try {
            statement.executeInsert()

        } catch (e: Exception) {

        } finally {
            db.close()
        }
    }

    fun cekUsernameAtauEmailSudahAda(username: String, email: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM users WHERE username = ? OR email = ?"
        val cursor = db.rawQuery(query, arrayOf(username, email))

        val sudahAda = cursor.count > 0
        cursor.close()
        db.close()

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
        db.close()

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
        db.close()

        return jumlah
    }
}