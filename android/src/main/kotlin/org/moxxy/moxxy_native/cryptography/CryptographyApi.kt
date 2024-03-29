// Autogenerated from Pigeon (v11.0.1), do not edit directly.
// See also: https://pub.dev/packages/pigeon

package org.moxxy.moxxy_native.cryptography

import android.util.Log
import io.flutter.plugin.common.BasicMessageChannel
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MessageCodec
import io.flutter.plugin.common.StandardMessageCodec
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

private fun wrapResult(result: Any?): List<Any?> {
    return listOf(result)
}

private fun wrapError(exception: Throwable): List<Any?> {
    if (exception is FlutterError) {
        return listOf(
            exception.code,
            exception.message,
            exception.details,
        )
    } else {
        return listOf(
            exception.javaClass.simpleName,
            exception.toString(),
            "Cause: " + exception.cause + ", Stacktrace: " + Log.getStackTraceString(exception),
        )
    }
}

/**
 * Error class for passing custom error details to Flutter via a thrown PlatformException.
 * @property code The error code.
 * @property message The error message.
 * @property details The error details. Must be a datatype supported by the api codec.
 */
class FlutterError(
    val code: String,
    override val message: String? = null,
    val details: Any? = null,
) : Throwable()

enum class CipherAlgorithm(val raw: Int) {
    AES128GCMNOPADDING(0),
    AES256GCMNOPADDING(1),
    AES256CBCPKCS7(2),
    ;

    companion object {
        fun ofRaw(raw: Int): CipherAlgorithm? {
            return values().firstOrNull { it.raw == raw }
        }
    }
}

/** Generated class from Pigeon that represents data sent in messages. */
data class CryptographyResult(
    val plaintextHash: ByteArray,
    val ciphertextHash: ByteArray,

) {
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromList(list: List<Any?>): CryptographyResult {
            val plaintextHash = list[0] as ByteArray
            val ciphertextHash = list[1] as ByteArray
            return CryptographyResult(plaintextHash, ciphertextHash)
        }
    }
    fun toList(): List<Any?> {
        return listOf<Any?>(
            plaintextHash,
            ciphertextHash,
        )
    }
}

@Suppress("UNCHECKED_CAST")
private object MoxxyCryptographyApiCodec : StandardMessageCodec() {
    override fun readValueOfType(type: Byte, buffer: ByteBuffer): Any? {
        return when (type) {
            128.toByte() -> {
                return (readValue(buffer) as? List<Any?>)?.let {
                    CryptographyResult.fromList(it)
                }
            }
            else -> super.readValueOfType(type, buffer)
        }
    }
    override fun writeValue(stream: ByteArrayOutputStream, value: Any?) {
        when (value) {
            is CryptographyResult -> {
                stream.write(128)
                writeValue(stream, value.toList())
            }
            else -> super.writeValue(stream, value)
        }
    }
}

/** Generated interface from Pigeon that represents a handler of messages from Flutter. */
interface MoxxyCryptographyApi {
    fun encryptFile(sourcePath: String, destPath: String, key: ByteArray, iv: ByteArray, algorithm: CipherAlgorithm, hashSpec: String, callback: (Result<CryptographyResult?>) -> Unit)
    fun decryptFile(sourcePath: String, destPath: String, key: ByteArray, iv: ByteArray, algorithm: CipherAlgorithm, hashSpec: String, callback: (Result<CryptographyResult?>) -> Unit)
    fun hashFile(sourcePath: String, hashSpec: String, callback: (Result<ByteArray?>) -> Unit)

    companion object {
        /** The codec used by MoxxyCryptographyApi. */
        val codec: MessageCodec<Any?> by lazy {
            MoxxyCryptographyApiCodec
        }

        /** Sets up an instance of `MoxxyCryptographyApi` to handle messages through the `binaryMessenger`. */
        @Suppress("UNCHECKED_CAST")
        fun setUp(binaryMessenger: BinaryMessenger, api: MoxxyCryptographyApi?) {
            run {
                val channel = BasicMessageChannel<Any?>(binaryMessenger, "dev.flutter.pigeon.moxxy_native.MoxxyCryptographyApi.encryptFile", codec)
                if (api != null) {
                    channel.setMessageHandler { message, reply ->
                        val args = message as List<Any?>
                        val sourcePathArg = args[0] as String
                        val destPathArg = args[1] as String
                        val keyArg = args[2] as ByteArray
                        val ivArg = args[3] as ByteArray
                        val algorithmArg = CipherAlgorithm.ofRaw(args[4] as Int)!!
                        val hashSpecArg = args[5] as String
                        api.encryptFile(sourcePathArg, destPathArg, keyArg, ivArg, algorithmArg, hashSpecArg) { result: Result<CryptographyResult?> ->
                            val error = result.exceptionOrNull()
                            if (error != null) {
                                reply.reply(wrapError(error))
                            } else {
                                val data = result.getOrNull()
                                reply.reply(wrapResult(data))
                            }
                        }
                    }
                } else {
                    channel.setMessageHandler(null)
                }
            }
            run {
                val channel = BasicMessageChannel<Any?>(binaryMessenger, "dev.flutter.pigeon.moxxy_native.MoxxyCryptographyApi.decryptFile", codec)
                if (api != null) {
                    channel.setMessageHandler { message, reply ->
                        val args = message as List<Any?>
                        val sourcePathArg = args[0] as String
                        val destPathArg = args[1] as String
                        val keyArg = args[2] as ByteArray
                        val ivArg = args[3] as ByteArray
                        val algorithmArg = CipherAlgorithm.ofRaw(args[4] as Int)!!
                        val hashSpecArg = args[5] as String
                        api.decryptFile(sourcePathArg, destPathArg, keyArg, ivArg, algorithmArg, hashSpecArg) { result: Result<CryptographyResult?> ->
                            val error = result.exceptionOrNull()
                            if (error != null) {
                                reply.reply(wrapError(error))
                            } else {
                                val data = result.getOrNull()
                                reply.reply(wrapResult(data))
                            }
                        }
                    }
                } else {
                    channel.setMessageHandler(null)
                }
            }
            run {
                val channel = BasicMessageChannel<Any?>(binaryMessenger, "dev.flutter.pigeon.moxxy_native.MoxxyCryptographyApi.hashFile", codec)
                if (api != null) {
                    channel.setMessageHandler { message, reply ->
                        val args = message as List<Any?>
                        val sourcePathArg = args[0] as String
                        val hashSpecArg = args[1] as String
                        api.hashFile(sourcePathArg, hashSpecArg) { result: Result<ByteArray?> ->
                            val error = result.exceptionOrNull()
                            if (error != null) {
                                reply.reply(wrapError(error))
                            } else {
                                val data = result.getOrNull()
                                reply.reply(wrapResult(data))
                            }
                        }
                    }
                } else {
                    channel.setMessageHandler(null)
                }
            }
        }
    }
}
