package com.example.util

import java.math.BigInteger
import java.security.MessageDigest

object IpfsEngine {
    private const val ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
    private val BASE = BigInteger.valueOf(58)

    fun generateCidV0(payload: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(payload.toByteArray(Charsets.UTF_8))
            
            // Multihash format: sha2-256 (0x12) and length 32 (0x20)
            val multihash = ByteArray(34)
            multihash[0] = 0x12
            multihash[1] = 0x20
            System.arraycopy(hash, 0, multihash, 2, 32)
            
            "Qm" + encodeBase58(multihash)
        } catch (e: Exception) {
            "QmFallbackCID" + System.currentTimeMillis().toString(16)
        }
    }

    fun generateCidV1(payload: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(payload.toByteArray(Charsets.UTF_8))
            
            // CIDv1 prefix: Version 1 (0x01), dag-pb (0x70), sha2-256 (0x12), length 32 (0x20)
            val prefix = byteArrayOf(0x01, 0x70, 0x12, 0x20)
            val v1Bytes = ByteArray(prefix.size + hash.size)
            System.arraycopy(prefix, 0, v1Bytes, 0, prefix.size)
            System.arraycopy(hash, 0, v1Bytes, prefix.size, hash.size)
            
            "bafybeic" + encodeBase32(v1Bytes).lowercase()
        } catch (e: Exception) {
            "bafybeicFallbackCID" + System.currentTimeMillis().toString(16)
        }
    }

    private fun encodeBase58(input: ByteArray): String {
        var value = BigInteger(1, input)
        var sb = ""
        while (value >= BASE) {
            val mod = value.mod(BASE)
            sb += ALPHABET[mod.toInt()]
            value = value.divide(BASE)
        }
        sb += ALPHABET[value.toInt()]
        
        // Zero byte leading preservation
        for (i in input.indices) {
            if (input[i] == 0.toByte()) {
                sb += ALPHABET[0]
            } else {
                break
            }
        }
        return sb.reversed()
    }

    private fun encodeBase32(bytes: ByteArray): String {
        val base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        var i = 0
        var index = 0
        var digit = 0
        var currByte: Int
        var nextByte: Int
        var result = ""
        
        while (i < bytes.size) {
            currByte = if (bytes[i] >= 0) bytes[i].toInt() else bytes[i].toInt() + 256
            if (index > 3) {
                if (i + 1 < bytes.size) {
                    nextByte = if (bytes[i + 1] >= 0) bytes[i + 1].toInt() else bytes[i + 1].toInt() + 256
                } else {
                    nextByte = 0
                }
                digit = currByte and (0xFF shr index)
                index = (index + 5) % 8
                digit = digit shl index
                digit = digit or (nextByte ushr (8 - index))
                i++
            } else {
                digit = (currByte shr (8 - (index + 5))) and 0x1F
                index = (index + 5) % 8
                if (index == 0) i++
            }
            result += base32Chars[digit]
        }
        return result
    }
}
