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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menutambahdanedit)

        // Inisialisasi komponen UI
        kembalikemain = findViewById(R.id.buttonkembali)
        generatePassword = findViewById(R.id.generatepassword)
        passwordStrength = findViewById(R.id.passwordStrength)
        spinnerKategori = findViewById(R.id.spinnerkategori)
        etPassword = findViewById(R.id.etpassword)
        ivShowPassword = findViewById(R.id.lihatpassword)
        emailtext = findViewById(R.id.ETEmail)
        usernametext = findViewById(R.id.ETUsername)
        notestext = findViewById(R.id.ETNotes)

        // Tombol kembali
        kembalikemain.setOnClickListener {
            startActivity(Intent(this, MainDashboard::class.java))
            finish()
        }
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
            updatePasswordStrength(password)

        }


        simpandatakedb = findViewById<TextView>(R.id.savedatakedatabase)
        simpandatakedb.setOnClickListener {
            val user = CurrentUser.user
            if (user != null) {
                val selectedKategori = spinnerKategori.selectedItem as Kategori
                val email = emailtext.text
                val username = usernametext.text
                val kunciAES = keyAES()
                val buatkunci = Encrypt(user.kunci_enkripsi,kunciAES.KunciIVKey)
                val passwordsafe = buatkunci.enkripsi(etPassword.text.toString())
                val notesin = notestext.text
                val dbhelper = databaseHelper(this)
                if (!email.toString().contains("@")){
                    showCustomDialog( "harusada_@" )
                    return@setOnClickListener
                }
                dbhelper.insertTopasswordDatabase(user.id_user.toString(),selectedKategori.id_service,email.toString(),username.toString(),passwordsafe,notesin.toString())
                showCustomDialog( "berhasil_daftar" )
                emailtext.text = null
                usernametext.text  = null
                etPassword.text = null
                notestext.text = null

            }


        }
        // Load data kategori dari database
        loadKategoriFromDatabase()

        // Adapter Spinner
        val adapter = object : ArrayAdapter<Kategori>(
            this,
            R.layout.spinner_item,
            kategoriList
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                return getCustomView(position, convertView, parent)
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                return getCustomView(position, convertView, parent)
            }

            private fun getCustomView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false)
                val icon = view.findViewById<ImageView>(R.id.iconkategori)
                val text = view.findViewById<TextView>(R.id.textkategori)

                val kategori = kategoriList[position]
                icon.setImageResource(kategori.iconResId)
                text.text = kategori.nama

                return view
            }
        }

        spinnerKategori.adapter = adapter

        spinnerKategori.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                val kategori = kategoriList[position]
                //Toast.makeText(this@menutambahdanedit, "Kategori: ${kategori.nama}", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun generateRandomPassword(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#\$%^&*()"
        return (1..12)
            .map { Random.nextInt(chars.length) }
            .map(chars::get)
            .joinToString("")
    }

    private fun updatePasswordStrength(password: String) {
        val strength = getPasswordStrength(password)
        passwordStrength.text = "Strength: $strength"
    }

    private fun getPasswordStrength(password: String): String {
        return when {
            password.matches(Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#\$%^&*()_+]).{8,}$")) -> "Strong"
            password.length < 6 -> "Weak"
            password.matches(Regex("^(?=.*[a-zA-Z])(?=.*[0-9]).{6,}$")) -> "Medium"
            else -> "Weak"
        }
    }
    private fun showCustomDialog(status: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.logindialog_status, null)
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

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

        btnOk.setOnClickListener {

            alertDialog.dismiss()
        }



        alertDialog.setCancelable(false)
        alertDialog.show()
    }
    private fun loadKategoriFromDatabase() {
        val dbHelper = databaseHelper(this)
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
                    iconResId = R.drawable.logingoogle, // Bisa disesuaikan nanti berdasarkan kategori
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
