// Autogenerated from Pigeon (v11.0.1), do not edit directly.
// See also: https://pub.dev/packages/pigeon

package org.moxxy.moxxy_native.service

import android.util.Log
import io.flutter.plugin.common.BasicMessageChannel
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MessageCodec
import io.flutter.plugin.common.StandardMessageCodec

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

/** Generated interface from Pigeon that represents a handler of messages from Flutter. */
interface MoxxyServiceApi {
    fun configure(handle: Long, extraData: String)
    fun isRunning(): Boolean
    fun start()
    fun sendData(data: String)

    companion object {
        /** The codec used by MoxxyServiceApi. */
        val codec: MessageCodec<Any?> by lazy {
            StandardMessageCodec()
        }

        /** Sets up an instance of `MoxxyServiceApi` to handle messages through the `binaryMessenger`. */
        @Suppress("UNCHECKED_CAST")
        fun setUp(binaryMessenger: BinaryMessenger, api: MoxxyServiceApi?) {
            run {
                val channel = BasicMessageChannel<Any?>(binaryMessenger, "dev.flutter.pigeon.moxxy_native.MoxxyServiceApi.configure", codec)
                if (api != null) {
                    channel.setMessageHandler { message, reply ->
                        val args = message as List<Any?>
                        val handleArg = args[0].let { if (it is Int) it.toLong() else it as Long }
                        val extraDataArg = args[1] as String
                        var wrapped: List<Any?>
                        try {
                            api.configure(handleArg, extraDataArg)
                            wrapped = listOf<Any?>(null)
                        } catch (exception: Throwable) {
                            wrapped = wrapError(exception)
                        }
                        reply.reply(wrapped)
                    }
                } else {
                    channel.setMessageHandler(null)
                }
            }
            run {
                val channel = BasicMessageChannel<Any?>(binaryMessenger, "dev.flutter.pigeon.moxxy_native.MoxxyServiceApi.isRunning", codec)
                if (api != null) {
                    channel.setMessageHandler { _, reply ->
                        var wrapped: List<Any?>
                        try {
                            wrapped = listOf<Any?>(api.isRunning())
                        } catch (exception: Throwable) {
                            wrapped = wrapError(exception)
                        }
                        reply.reply(wrapped)
                    }
                } else {
                    channel.setMessageHandler(null)
                }
            }
            run {
                val channel = BasicMessageChannel<Any?>(binaryMessenger, "dev.flutter.pigeon.moxxy_native.MoxxyServiceApi.start", codec)
                if (api != null) {
                    channel.setMessageHandler { _, reply ->
                        var wrapped: List<Any?>
                        try {
                            api.start()
                            wrapped = listOf<Any?>(null)
                        } catch (exception: Throwable) {
                            wrapped = wrapError(exception)
                        }
                        reply.reply(wrapped)
                    }
                } else {
                    channel.setMessageHandler(null)
                }
            }
            run {
                val channel = BasicMessageChannel<Any?>(binaryMessenger, "dev.flutter.pigeon.moxxy_native.MoxxyServiceApi.sendData", codec)
                if (api != null) {
                    channel.setMessageHandler { message, reply ->
                        val args = message as List<Any?>
                        val dataArg = args[0] as String
                        var wrapped: List<Any?>
                        try {
                            api.sendData(dataArg)
                            wrapped = listOf<Any?>(null)
                        } catch (exception: Throwable) {
                            wrapped = wrapError(exception)
                        }
                        reply.reply(wrapped)
                    }
                } else {
                    channel.setMessageHandler(null)
                }
            }
        }
    }
}