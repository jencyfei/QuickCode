package com.sms.tagger.util

import android.util.Base64
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.InvalidKeyException
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Fernet 兼容解密器
 * 
 * Fernet 格式（128 字节 token）:
 * - Version (1 byte): 0x80
 * - Timestamp (8 bytes): big-endian uint64
 * - IV (16 bytes): AES-CBC 初始化向量
 * - Ciphertext (variable): AES-128-CBC 加密的数据（PKCS7 填充）
 * - HMAC (32 bytes): SHA256 HMAC 签名
 * 
 * Fernet Key (32 bytes base64 encoded):
 * - Signing key (16 bytes): 用于 HMAC
 * - Encryption key (16 bytes): 用于 AES
 */
object FernetDecryptor {

    private const val VERSION: Byte = 0x80.toByte()
    private const val VERSION_SIZE = 1
    private const val TIMESTAMP_SIZE = 8
    private const val IV_SIZE = 16
    private const val HMAC_SIZE = 32
    private const val KEY_SIZE = 16

    /**
     * 解密 Fernet token
     * 
     * @param fernetKey Base64 编码的 Fernet 密钥（32 字节解码后）
     * @param token Fernet token（双层 urlsafe base64 编码）
     * @return 解密后的明文字符串
     * @throws IllegalArgumentException 如果格式错误或验证失败
     */
    fun decrypt(fernetKey: String, token: String): String {
        // 1. 解码 Fernet 密钥
        val keyBytes = Base64.decode(fernetKey.trim(), Base64.URL_SAFE or Base64.NO_WRAP)
        if (keyBytes.size != 32) {
            throw IllegalArgumentException("Invalid Fernet key length: ${keyBytes.size}, expected 32")
        }
        val signingKey = keyBytes.copyOfRange(0, KEY_SIZE)
        val encryptionKey = keyBytes.copyOfRange(KEY_SIZE, 32)

        // 2. 解码 token（双层 base64 解码）
        // 激活码格式: base64(fernet_token_bytes)
        // fernet_token_bytes 本身也是 base64 编码的 Fernet 结构
        val tokenBytes = try {
            // 第一层解码：得到 Fernet token 字符串的字节形式（如 "gAAAAA..."）
            val fernetTokenBytes = Base64.decode(token.trim(), Base64.URL_SAFE or Base64.NO_WRAP)
            // 第二层解码：将 Fernet token 字符串解码为原始 Fernet 结构
            Base64.decode(fernetTokenBytes, Base64.URL_SAFE or Base64.NO_WRAP)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to decode token: ${e.message}")
        }

        // 3. 验证最小长度
        val minLength = VERSION_SIZE + TIMESTAMP_SIZE + IV_SIZE + HMAC_SIZE
        if (tokenBytes.size < minLength) {
            throw IllegalArgumentException("Token too short: ${tokenBytes.size}, minimum $minLength")
        }

        // 4. 解析 token 结构
        val version = tokenBytes[0]
        if (version != VERSION) {
            throw IllegalArgumentException("Invalid Fernet version: $version, expected $VERSION")
        }

        val timestamp = ByteBuffer.wrap(tokenBytes, VERSION_SIZE, TIMESTAMP_SIZE)
            .order(ByteOrder.BIG_ENDIAN)
            .long

        val iv = tokenBytes.copyOfRange(VERSION_SIZE + TIMESTAMP_SIZE, VERSION_SIZE + TIMESTAMP_SIZE + IV_SIZE)
        
        val ciphertextStart = VERSION_SIZE + TIMESTAMP_SIZE + IV_SIZE
        val ciphertextEnd = tokenBytes.size - HMAC_SIZE
        val ciphertext = tokenBytes.copyOfRange(ciphertextStart, ciphertextEnd)
        
        val hmacReceived = tokenBytes.copyOfRange(ciphertextEnd, tokenBytes.size)

        // 5. 验证 HMAC
        val dataToSign = tokenBytes.copyOfRange(0, ciphertextEnd)
        val hmacExpected = computeHmac(signingKey, dataToSign)
        if (!hmacExpected.contentEquals(hmacReceived)) {
            throw IllegalArgumentException("HMAC verification failed")
        }

        // 6. AES-128-CBC 解密
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKey = SecretKeySpec(encryptionKey, "AES")
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
        
        val plaintext = cipher.doFinal(ciphertext)
        return String(plaintext, Charsets.UTF_8)
    }

    private fun computeHmac(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data)
    }
}

