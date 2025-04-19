package com.daffaadityapurwanto.securein.encryption

import javax.crypto.spec.SecretKeySpec

class Encrypt(Kunci: String) {
    var key: String=Kunci
    var secretKeySpec = SecretKeySpec(key.toByteArray(),"AES")

}