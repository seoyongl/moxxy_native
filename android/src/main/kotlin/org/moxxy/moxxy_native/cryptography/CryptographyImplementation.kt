package org.moxxy.moxxy_native.cryptography

import android.util.Log
import org.moxxy.moxxy_native.BUFFER_SIZE
import org.moxxy.moxxy_native.TAG
import java.io.FileInputStream
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.concurrent.thread

/*
 * Convert the algorithm spec @algorithm to the format that Java/Android understands
 * */
private fun getCipherSpecFromInteger(algorithm: CipherAlgorithm): String {
    return when (algorithm) {
        CipherAlgorithm.AES128GCMNOPADDING -> "AES_128/GCM/NoPadding"
        CipherAlgorithm.AES256GCMNOPADDING -> "AES_256/GCM/NoPadding"
        CipherAlgorithm.AES256CBCPKCS7 -> "AES_256/CBC/PKCS7PADDING"
    }
}

/*
 * Implementation of Moxxy's cryptography API
 * */
class CryptographyImplementation : MoxxyCryptographyApi {
    override fun encryptFile(
        sourcePath: String,
        destPath: String,
        key: ByteArray,
        iv: ByteArray,
        algorithm: CipherAlgorithm,
        hashSpec: String,
        callback: (Result<CryptographyResult?>) -> Unit,
    ) {
        thread(start = true) {
            val cipherSpec = getCipherSpecFromInteger(algorithm)
            val buffer = ByteArray(BUFFER_SIZE)
            val secretKey = SecretKeySpec(key, cipherSpec)
            val inputStream = FileInputStream(sourcePath)
            try {
                val digest = MessageDigest.getInstance(hashSpec)
                val cipher = Cipher.getInstance(cipherSpec).apply {
                    init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
                }
                val fileOutputStream = HashedFileOutputStream(destPath, hashSpec)
                val cipherOutputStream = CipherOutputStream(fileOutputStream, cipher)
                var length: Int
                while (true) {
                    length = inputStream.read(buffer)
                    if (length <= 0) break

                    digest.update(buffer, 0, length)
                    cipherOutputStream.write(buffer, 0, length)
                }

                // Clean up
                cipherOutputStream.apply {
                    flush()
                    close()
                }

                // Success
                callback(
                    Result.success(
                        CryptographyResult(
                            plaintextHash = digest.digest(),
                            ciphertextHash = fileOutputStream.digest(),
                        ),
                    ),
                )
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to encrypt file $sourcePath: ${ex.message}")
                callback(Result.success(null))
            } finally {
                // Clean up
                inputStream.close()
            }
        }
    }

    override fun decryptFile(
        sourcePath: String,
        destPath: String,
        key: ByteArray,
        iv: ByteArray,
        algorithm: CipherAlgorithm,
        hashSpec: String,
        callback: (Result<CryptographyResult?>) -> Unit,
    ) {
        thread(start = true) {
            val cipherSpec = getCipherSpecFromInteger(algorithm)
            val buffer = ByteArray(BUFFER_SIZE)
            val secretKey = SecretKeySpec(key, cipherSpec)
            val inputStream = FileInputStream(sourcePath)
            try {
                val digest = MessageDigest.getInstance(hashSpec)
                val cipher = Cipher.getInstance(cipherSpec).apply {
                    init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
                }
                val fileOutputStream = HashedFileOutputStream(destPath, hashSpec)
                val cipherOutputStream = CipherOutputStream(fileOutputStream, cipher)
                var length: Int
                while (true) {
                    length = inputStream.read(buffer)
                    if (length <= 0) break

                    digest.update(buffer, 0, length)
                    cipherOutputStream.write(buffer, 0, length)
                }

                // Clean up
                cipherOutputStream.apply {
                    flush()
                    close()
                }

                // Success
                callback(
                    Result.success(
                        CryptographyResult(
                            plaintextHash = digest.digest(),
                            ciphertextHash = fileOutputStream.digest(),
                        ),
                    ),
                )
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to decrypt file $sourcePath: ${ex.message}")
                callback(Result.success(null))
            } finally {
                // Clean up
                inputStream.close()
            }
        }
    }

    override fun hashFile(
        sourcePath: String,
        hashSpec: String,
        callback: (Result<ByteArray?>) -> Unit,
    ) {
        thread(start = true) {
            val buffer = ByteArray(BUFFER_SIZE)
            val inputStream = FileInputStream(sourcePath)
            try {
                val digest = MessageDigest.getInstance(hashSpec)
                var length: Int
                while (true) {
                    length = inputStream.read(buffer)
                    if (length <= 0) break

                    // Only update the digest if we read more than 0 bytes
                    digest.update(buffer, 0, length)
                }

                // Return success
                callback(Result.success(digest.digest()))
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to has file $sourcePath with $hashSpec: ${ex.message}")
                callback(Result.success(null))
            } finally {
                // Clean up
                inputStream.close()
            }
        }
    }
}
