package com.daffaadityapurwanto.securein

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.daffaadityapurwanto.securein.data.CurrentUser
import com.daffaadityapurwanto.securein.data.Kategori
import com.daffaadityapurwanto.securein.data.databaseHelper
import com.daffaadityapurwanto.securein.encryption.Encrypt
import com.daffaadityapurwanto.securein.encryption.keyAES
import kotlin.random.Random

class menutambahdanedit : AppCompatActivity() {

    // --- Deklarasi Variabel ---
    private lateinit var spinnerKategori: Spinner
    private lateinit var etPassword: EditText
    private lateinit var ivShowPassword: ImageView
    private lateinit var kembalikemain: ImageView
    private lateinit var generatePassword: LinearLayout
    private lateinit var passwordStrength: TextView
    private lateinit var simpandatakedb: TextView
    private var isPasswordVisible = false
    private lateinit var kategoriList: List<Kategori>
    private lateinit var emailtext: EditText
    private lateinit var usernametext: EditText
    private lateinit var notestext: EditText

    // Variabel untuk menentukan mode Edit
    private var isEditMode = false
    private var editingPasswordId: String? = null
    private lateinit var dbHelper: databaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menutambahdanedit)
        dbHelper = databaseHelper(this)

        // Inisialisasi komponen UI (seperti kode asli Anda)
        kembalikemain = findViewById(R.id.buttonkembali)
        generatePassword = findViewById(R.id.generatepassword)
        passwordStrength = findViewById(R.id.passwordStrength)
        spinnerKategori = findViewById(R.id.spinnerkategori)
        etPassword = findViewById(R.id.etpassword)
        ivShowPassword = findViewById(R.id.lihatpassword)
        emailtext = findViewById(R.id.ETEmail)
        usernametext = findViewById(R.id.ETUsername)
        notestext = findViewById(R.id.ETNotes)
        simpandatakedb = findViewById(R.id.savedatakedatabase)

        // --- Logika Inti untuk Membedakan Mode Tambah vs Edit ---
        editingPasswordId = intent.getStringExtra("PASSWORD_ID")
        if (editingPasswordId != null) {
            isEditMode = true
        }

        // Setup semua listener
        setupListeners()
        // Muat data untuk spinner
        loadKategoriFromDatabase()
        // Setup adapter untuk spinner
        setupSpinnerAdapter()

        // Jika dalam mode Edit, isi form dengan data yang ada
        if (isEditMode) {
            populateFormForEdit(editingPasswordId!!)
        }
    }

    private fun setupListeners() {
        // Tombol kembali
        kembalikemain.setOnClickListener {
            // Cukup tutup activity ini, tidak perlu memulai activity baru
            finish()
        }

        // Listener untuk kekuatan password
        etPassword.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updatePasswordStrength(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Toggle tampilkan password
        ivShowPassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivShowPassword.setImageResource(R.drawable.eyesclose)
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivShowPassword.setImageResource(R.drawable.eyesopen)
            }
            etPassword.setSelection(etPassword.text.length)
        }

        // Tombol generate password
        generatePassword.setOnClickListener {
            val password = generateRandomPassword()
            etPassword.setText(password)
        }

        // Listener untuk tombol Simpan/Update
        simpandatakedb.setOnClickListener {
            if (isEditMode) {
                // Jika mode edit, jalankan fungsi update
                performUpdate()
            } else {
                // Jika mode tambah, jalankan fungsi insert
                performInsert()
            }
        }
    }

    // Fungsi untuk mengisi data ke form jika dalam mode edit
    private fun populateFormForEdit(passwordId: String) {
        // Ambil detail data dari database
        val itemDetails = dbHelper.getPasswordDetails(passwordId) ?: return
        val user = CurrentUser.user ?: return

        // Ubah teks tombol menjadi "Update"
        simpandatakedb.text = "Update"

        // Dekripsi password untuk ditampilkan
        val kunciAES = keyAES()
        val decryptor = Encrypt(user.kunci_enkripsi, kunciAES.KunciIVKey)
        val decryptedPassword = decryptor.dekripsi(itemDetails.password)

        // Isi semua field dengan data yang ada
        emailtext.setText(itemDetails.emailName)
        usernametext.setText(itemDetails.username)
        notestext.setText(itemDetails.notes)
        etPassword.setText(decryptedPassword)

        // Atur pilihan spinner sesuai data
        val kategoriPosition = kategoriList.indexOfFirst { it.id_service == itemDetails.idservice }
        if (kategoriPosition != -1) {
            spinnerKategori.setSelection(kategoriPosition)
        }
    }

    // Fungsi untuk menyimpan data BARU (logika asli Anda)
    private fun performInsert() {
        val user = CurrentUser.user
        if (user != null) {
            val selectedKategori = spinnerKategori.selectedItem as Kategori
            val email = emailtext.text.toString()
            val username = usernametext.text.toString()
            val notesin = notestext.text.toString()

            if (email.isBlank() || !email.contains("@")) {
                showCustomDialog("harusada_@")
                return
            }

            val kunciAES = keyAES()
            val encryptor = Encrypt(user.kunci_enkripsi, kunciAES.KunciIVKey)
            val passwordsafe = encryptor.enkripsi(etPassword.text.toString())

            dbHelper.insertTopasswordDatabase(user.id_user.toString(), selectedKategori.id_service, email, username, passwordsafe, notesin)
            showCustomDialog("berhasil_daftar")

            // Kosongkan form setelah berhasil
            emailtext.text.clear()
            usernametext.text.clear()
            etPassword.text.clear()
            notestext.text.clear()
        }
    }

    // Fungsi untuk MEMPERBARUI data yang ada
    private fun performUpdate() {
        val user = CurrentUser.user
        if (user != null && editingPasswordId != null) {
            val selectedKategori = spinnerKategori.selectedItem as Kategori
            val email = emailtext.text.toString()
            val username = usernametext.text.toString()
            val notes = notestext.text.toString()

            val kunciAES = keyAES()
            val encryptor = Encrypt(user.kunci_enkripsi, kunciAES.KunciIVKey)
            val newEncryptedPassword = encryptor.enkripsi(etPassword.text.toString())

            dbHelper.updatePassword(editingPasswordId!!, selectedKategori.id_service, email, username, newEncryptedPassword, notes)

            Toast.makeText(this, "Data berhasil diperbarui", Toast.LENGTH_SHORT).show()
            finish() // Kembali ke halaman list setelah update berhasil
        }
    }

    // Fungsi setup adapter spinner (logika asli Anda)
    private fun setupSpinnerAdapter() {
        val adapter = object : ArrayAdapter<Kategori>(this, R.layout.spinner_item, kategoriList) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                return getCustomView(position, parent)
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                return getCustomView(position, parent)
            }

            private fun getCustomView(position: Int, parent: ViewGroup): View {
                val view = LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false)
                val icon = view.findViewById<ImageView>(R.id.iconkategori)
                val text = view.findViewById<TextView>(R.id.textkategori)
                val kategori = getItem(position)
                if(kategori != null){
                    icon.setImageResource(kategori.iconResId)
                    text.text = kategori.nama
                }
                return view
            }
        }
        spinnerKategori.adapter = adapter
        spinnerKategori.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {}
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // Fungsi lain-lain (logika asli Anda)
    private fun generateRandomPassword(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%^&*()"
        return (1..12).map { chars.random() }.joinToString("")
    }

    private fun updatePasswordStrength(password: String) {
        val strength = getPasswordStrength(password)
        passwordStrength.text = "Strength: $strength"
    }

    private fun getPasswordStrength(password: String): String {
        return when {
            password.matches(Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+]).{8,}$")) -> "Strong"
            password.length < 6 -> "Weak"
            password.matches(Regex("^(?=.*[a-zA-Z])(?=.*[0-9]).{6,}$")) -> "Medium"
            else -> "Weak"
        }
    }

    private fun showCustomDialog(status: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.logindialog_status, null)
        val alertDialog = AlertDialog.Builder(this).setView(dialogView).create()
        val imgStatus = dialogView.findViewById<ImageView>(R.id.imgStatus)
        val txtStatus = dialogView.findViewById<TextView>(R.id.txtStatus)
        val btnOk = dialogView.findViewById<Button>(R.id.btnOk)
        when (status) {
            "berhasil_daftar" -> {
                imgStatus.setImageResource(R.drawable.check)
                txtStatus.text = "Berhasil Ditambahkan"
            }
            "harusada_@" -> {
                imgStatus.setImageResource(R.drawable.dangerlogin)
                txtStatus.text = "Email Tidak Valid!"
            }
        }
        btnOk.setOnClickListener { alertDialog.dismiss() }
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun loadKategoriFromDatabase() {
        val db: SQLiteDatabase = dbHelper.openDatabase()
        val tempList = mutableListOf<Kategori>()
        val cursor = db.rawQuery("SELECT id_service, nama_Service, id_kategori FROM services", null)
        if (cursor.moveToFirst()) {
            do {
                val idService = cursor.getString(0)
                val namaService = cursor.getString(1)
                val idKategori = cursor.getString(2)
                val kategori = Kategori(
                    nama = namaService,
                    iconResId = R.drawable.logingoogle,
                    id_service = idService,
                    id_kategori = idKategori
                )
                tempList.add(kategori)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        kategoriList = tempList
    }
}