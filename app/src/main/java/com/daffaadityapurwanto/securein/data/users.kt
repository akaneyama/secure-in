package com.daffaadityapurwanto.securein.data

data class users (
    val id_user: Int,
    val uid: String,
    val kunci_enkripsi: String,
    val email: String,
    val nama: String,
    val username: String, // <-- Pastikan ini ada
    val password: String,
    val is_verified: Int
)
//agar bisa dipanggil dan ditambahkan
object CurrentUser {
    var user: users? = null
}