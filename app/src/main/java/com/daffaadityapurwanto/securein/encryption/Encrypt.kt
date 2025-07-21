package com.daffaadityapurwanto.securein.encryption

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class Encrypt(private val key: String, private val IVKey: String) {

    private val charset = Charsets.UTF_8
    private val secretKeySpec = SecretKeySpec(key.toByteArray(charset), "AES")

    private val ivBytes = IVKey.toByteArray(charset)
    private val ivSpec = IvParameterSpec(ivBytes)

    fun enkripsi(plainText: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec)

        val encrypted = cipher.doFinal(plainText.toByteArray(charset))
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    fun dekripsi(cipherText: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec)

        val decrypted = cipher.doFinal(Base64.decode(cipherText, Base64.NO_WRAP))
        return String(decrypted, charset)
    }
    fun enkripsiPassword(plainText: String, key: SecretKey): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")

        // 1. Buat IV BARU dan ACAK setiap kali enkripsi
        val ivBytes = ByteArray(16)
        SecureRandom().nextBytes(ivBytes)
        val ivSpec = IvParameterSpec(ivBytes)

        // 2. Lakukan enkripsi
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)
        val encryptedBytes = cipher.doFinal(plainText.toByteArray())

        // 3. GABUNGKAN IV dan hasil enkripsi
        val combinedBytes = ivBytes + encryptedBytes

        // 4. Encode ke Base64 untuk disimpan sebagai String
        return Base64.encodeToString(combinedBytes, Base64.NO_WRAP)
    }

    fun dekripsi(combinedBase64: String, key: SecretKey): String {
        // 1. Decode Base64 kembali ke byte array
        val combinedBytes = Base64.decode(combinedBase64, Base64.NO_WRAP)

        // 2. PISAHKAN IV dan ciphertext
        // IV untuk AES adalah 16 byte pertama
        val ivBytes = combinedBytes.copyOfRange(0, 16)
        val encryptedBytes = combinedBytes.copyOfRange(16, combinedBytes.size)
        val ivSpec = IvParameterSpec(ivBytes)

        // 3. Lakukan dekripsi dengan IV yang benar
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
        val decryptedBytes = cipher.doFinal(encryptedBytes)

        // 4. Kembalikan hasil dekripsi
        return String(decryptedBytes, Charsets.UTF_8)
    }


}