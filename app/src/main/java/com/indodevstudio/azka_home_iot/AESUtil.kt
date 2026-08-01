package com.indodevstudio.azka_home_iot
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import android.util.Base64
import android.util.Log

object AESUtil {
    private const val secretKey = "16CharSecretKey!" // Harus 16 karakter
    private const val initVector = "16CharInitVector" // Harus 16 karakter

    fun encrypt(input: String): String {
        val iv = IvParameterSpec(initVector.toByteArray(Charsets.UTF_8))
        val skeySpec = SecretKeySpec(secretKey.toByteArray(Charsets.UTF_8), "AES")

        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv)

        val encrypted = cipher.doFinal(input.toByteArray())
        Log.d("Encryption", "Encrypted: $encrypted")
        Log.d("Encryption", "Encrypted String: ${Base64.encodeToString(encrypted, Base64.URL_SAFE or Base64.NO_WRAP)}")
        return Base64.encodeToString(encrypted, Base64.URL_SAFE or Base64.NO_WRAP)
    }
}