package com.daffaadityapurwanto.securein.encryption

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

class Encrypt(private val key: String) {

    private val secretKeySpec: SecretKeySpec = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "AES")

    fun enkripsi(plainText: String): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)

    }
    fun dekripsi(cipherText: String): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
        val decryptedBytes = Base64.decode(cipherText, Base64.DEFAULT)
        return decryptedBytes.toString(Charsets.UTF_8)
    }

}