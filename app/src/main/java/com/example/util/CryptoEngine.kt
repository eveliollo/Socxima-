package com.example.util

import android.content.Context
import android.util.Base64
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

object CryptoEngine {
    private const val PREFS_NAME = "socxima_crypto_keys"
    private const val KEY_PRIVATE = "private_key_pem"
    private const val KEY_PUBLIC = "public_key_pem"

    private var activePublicKey: PublicKey? = null
    private var activePrivateKey: PrivateKey? = null

    fun getMondayOfWeek(offsetWeeks: Int = 0): String {
        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        cal.firstDayOfWeek = java.util.Calendar.MONDAY
        
        // Find Monday of the current week (or adjust for offsetWeeks)
        val dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK)
        val daysToSubtract = if (dayOfWeek == java.util.Calendar.SUNDAY) 6 else dayOfWeek - java.util.Calendar.MONDAY
        cal.add(java.util.Calendar.DAY_OF_YEAR, -daysToSubtract)
        
        // Apply weekly offset for history
        if (offsetWeeks != 0) {
            cal.add(java.util.Calendar.WEEK_OF_YEAR, -offsetWeeks)
        }
        
        val year = cal.get(java.util.Calendar.YEAR)
        val month = cal.get(java.util.Calendar.MONTH) + 1 // 1-based
        val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
        return String.format("%04d-%02d-%02d", year, month, day)
    }

    fun getPasscodeForWeek(context: Context, offsetWeeks: Int = 0): String {
        val mondayStr = getMondayOfWeek(offsetWeeks)
        val input = "Evelio Llovera_$mondayStr"
        return try {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
            
            // 1. [LETRA]: A-Z from byte 0
            val letterIndex = (hashBytes[0].toInt() and 0xFF) % 26
            val letter = ('A' + letterIndex)
            
            // 2. [3NÚMEROS]: 3 digits from bytes 1 and 2
            val numValue = (((hashBytes[1].toInt() and 0xFF) shl 8) or (hashBytes[2].toInt() and 0xFF)) % 1000
            val numbers = String.format("%03d", numValue)
            
            // 3. [SÍMBOLO]: a symbol from the selected strong list
            val symbols = listOf("$", "@", "!", "&", "%", "*", "?", "+")
            val symbolIndex = (hashBytes[3].toInt() and 0xFF) % symbols.size
            val symbol = symbols[symbolIndex]
            
            // 4. [4LETRAS]: 4 alphanumeric letters
            val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
            val sb = java.lang.StringBuilder()
            for (i in 0 until 4) {
                val charIndex = (hashBytes[4 + i].toInt() and 0xFF) % alphabet.length
                sb.append(alphabet[charIndex])
            }
            val letters4 = sb.toString()
            
            "SX-$letter-$numbers#$symbol-$letters4"
        } catch (e: Exception) {
            "SX-K-741#$-PwM9"
        }
    }

    fun getPasscodeHistory(context: Context, count: Int = 5): List<Pair<String, String>> {
        val history = java.util.ArrayList<Pair<String, String>>()
        for (i in 0 until count) {
            val dateStr = getMondayOfWeek(i)
            val code = getPasscodeForWeek(context, i)
            history.add(Pair(dateStr, code))
        }
        return history
    }

    @Synchronized
    fun getOrGenerateKeys(context: Context): Pair<String, String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedPrivate = prefs.getString(KEY_PRIVATE, null)
        val savedPublic = prefs.getString(KEY_PUBLIC, null)

        return if (savedPrivate != null && savedPublic != null) {
            try {
                val privateBytes = Base64.decode(savedPrivate, Base64.DEFAULT)
                val publicBytes = Base64.decode(savedPublic, Base64.DEFAULT)

                val keyFactory = KeyFactory.getInstance("EC")
                activePrivateKey = keyFactory.generatePrivate(PKCS8EncodedKeySpec(privateBytes))
                activePublicKey = keyFactory.generatePublic(X509EncodedKeySpec(publicBytes))

                val pubHex = bytesToHex(publicBytes).take(64)
                val privHex = bytesToHex(privateBytes).take(64)
                Pair(pubHex, privHex)
            } catch (e: Exception) {
                // Fail-safe recreate
                generateNewKeys(context)
            }
        } else {
            generateNewKeys(context)
        }
    }

    private fun generateNewKeys(context: Context): Pair<String, String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return try {
            val keyGen = KeyPairGenerator.getInstance("EC")
            keyGen.initialize(256)
            val keyPair = keyGen.generateKeyPair()

            activePrivateKey = keyPair.private
            activePublicKey = keyPair.public

            val privateBytes = activePrivateKey!!.encoded
            val publicBytes = activePublicKey!!.encoded

            val privateBase64 = Base64.encodeToString(privateBytes, Base64.DEFAULT)
            val publicBase64 = Base64.encodeToString(publicBytes, Base64.DEFAULT)

            prefs.edit()
                .putString(KEY_PRIVATE, privateBase64)
                .putString(KEY_PUBLIC, publicBase64)
                .apply()

            val pubHex = bytesToHex(publicBytes).take(64)
            val privHex = bytesToHex(privateBytes).take(64)
            Pair(pubHex, privHex)
        } catch (e: Exception) {
            Pair("failed_to_generate_public_key_hex", "failed_to_generate_private_key_hex")
        }
    }

    fun signMessage(message: String): String {
        val privateKey = activePrivateKey ?: return "no_private_key_active_to_sign"
        return try {
            val dsa = Signature.getInstance("SHA256withECDSA")
            dsa.initSign(privateKey)
            dsa.update(message.toByteArray(Charsets.UTF_8))
            val signatureBytes = dsa.sign()
            bytesToHex(signatureBytes)
        } catch (e: Exception) {
            "signature_generation_error: ${e.localizedMessage}"
        }
    }

    fun verifySignature(message: String, signatureHex: String): Boolean {
        val publicKey = activePublicKey ?: return false
        return try {
            val dsa = Signature.getInstance("SHA256withECDSA")
            dsa.initVerify(publicKey)
            dsa.update(message.toByteArray(Charsets.UTF_8))
            dsa.verify(hexToBytes(signatureHex))
        } catch (e: Exception) {
            false
        }
    }

    fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[i * 2] = "0123456789ABCDEF"[v ushr 4]
            hexChars[i * 2 + 1] = "0123456789ABCDEF"[v and 0x0F]
        }
        return String(hexChars)
    }

    fun hexToBytes(hexString: String): ByteArray {
        val len = hexString.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(hexString[i], 16) shl 4) + Character.digit(hexString[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}
