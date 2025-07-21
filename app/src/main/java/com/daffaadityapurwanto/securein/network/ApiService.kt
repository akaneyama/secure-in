package com.daffaadityapurwanto.securein.network

import com.daffaadityapurwanto.securein.fragmentdashboard.mypasswordFragment
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
// di dalam interface ApiService
import retrofit2.http.GET
import retrofit2.http.Query


// --- Model Data untuk dikirim ke API ---
data class BackupRequest(
    @SerializedName("user_id") // Sesuaikan dengan nama field di JSON API
    val userId: Int,
    @SerializedName("passwords")
    val passwords: List<PasswordData>
)

data class PasswordData(
    // Hanya kirim data yang dibutuhkan oleh server
    @SerializedName("id_service")
    val idService: String,
    @SerializedName("notes")
    val notes: String,
    @SerializedName("email")
    val emailName: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val passwordEncrypted: String // Kirim password yang sudah terenkripsi
)
data class UpdateUserRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("current_password") val currentPasswordEncrypted: String,
    @SerializedName("field_to_update") val fieldToUpdate: String,
    @SerializedName("new_value") val newValue: String
)
// --- Model Data untuk Response dari API ---
data class BackupResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String
)
data class OtpRequest(
    @SerializedName("email") val email: String,
    @SerializedName("nama") val nama: String,
    @SerializedName("username") val username: String,
    @SerializedName("password") val passwordEncrypted: String,
    @SerializedName("kunci_enkripsi") val kunciEnkripsi: String
)

data class OtpVerificationRequest(
    @SerializedName("email") val email: String,
    @SerializedName("otp") val otp: String
)

data class OtpVerificationResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("user_data") val userData: LoginResponse? // Kita bisa pakai ulang LoginResponse
)


// --- Interface untuk Retrofit ---
interface ApiService {

    @POST("request-otp")
    suspend fun requestOtp(@Body request: OtpRequest): Response<BackupResponse>

    @POST("verify-otp")
    suspend fun verifyOtp(@Body request: OtpVerificationRequest): Response<OtpVerificationResponse>
    @POST("backup") // Sesuaikan dengan endpoint di Flask ('/backup')
    suspend fun backupPasswords(@Body request: BackupRequest): Response<BackupResponse>
    @GET("restore")
    suspend fun restorePasswords(@Query("user_id") userId: Int): Response<List<PasswordData>>
    // Tambahkan fungsi ini di dalam interface ApiService
    @POST("register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<RegisterResponse>
    @POST("login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>
    @POST("update_user")
    suspend fun updateUser(@Body request: UpdateUserRequest): Response<BackupResponse> // Bisa pakai ulang BackupResponse
}

// Model data untuk dikirim saat registrasi
data class RegisterRequest(
    @SerializedName("uid") val uid: String = "0",
    @SerializedName("kunci_enkripsi") val kunciEnkripsi: String,
    @SerializedName("email") val email: String,
    @SerializedName("nama") val nama: String,
    @SerializedName("username") val username: String,
    @SerializedName("password") val passwordEncrypted: String
)

// Model Response: sekarang menerima userId
data class RegisterResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("user_id") val userId: Int?
)
data class LoginRequest(
    @SerializedName("username") val username: String, // Bisa berisi username atau email
    @SerializedName("password") val passwordEncrypted: String
)

// Model untuk response login (mirip dengan data class 'users' Anda)
data class LoginResponse(
    @SerializedName("id_user") val id_user: Int,
    @SerializedName("uid") val uid: String,
    @SerializedName("kunci_enkripsi") val kunci_enkripsi: String,
    @SerializedName("email") val email: String,
    @SerializedName("nama") val nama: String,
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("is_verified") val is_verified: Int // <-- TAMBAHKAN BARIS INI
)

// --- Objek untuk membuat instance Retrofit ---
object RetrofitClient {
    private const val BASE_URL = "http://192.168.1.2:5000/" // URL API Anda

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ApiService::class.java)
    }
}