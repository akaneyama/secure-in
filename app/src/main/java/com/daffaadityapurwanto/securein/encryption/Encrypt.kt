package com.daffaadityapurwanto.securein.encryption

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import android.util.Base64
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



}