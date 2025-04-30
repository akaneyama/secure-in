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


class databaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        companion object {
            private const val DATABASE_NAME = "secureindb.db"
            private const val DATABASE_VERSION = 1
        }

        private val dbPath: String = context.getDatabasePath(DATABASE_NAME).path

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

    fun loginandcheckuser(username: String, password: String): users? {
        val db = openDatabase()
        val query = "SELECT id_user, uid, kunci_enkripsi, email, nama FROM users WHERE username = ? AND password = ?"
        val cursor = db.rawQuery(query, arrayOf(username, password))

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

    fun cekUsernameAtauEmailSudahAda(username: String, email: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM users WHERE username = ? OR email = ?"
        val cursor = db.rawQuery(query, arrayOf(username, email))

        val sudahAda = cursor.count > 0
        cursor.close()
        db.close()

        return sudahAda
    }
}