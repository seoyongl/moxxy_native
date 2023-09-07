// Autogenerated from Pigeon (v11.0.1), do not edit directly.
// See also: https://pub.dev/packages/pigeon

package org.moxxy.moxxy_native.generated

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
      exception.details
    )
  } else {
    return listOf(
      exception.javaClass.simpleName,
      exception.toString(),
      "Cause: " + exception.cause + ", Stacktrace: " + Log.getStackTraceString(exception)
    )
  }
}

/**
 * Error class for passing custom error details to Flutter via a thrown PlatformException.
 * @property code The error code.
 * @property message The error message.
 * @property details The error details. Must be a datatype supported by the api codec.
 */
class FlutterError (
  val code: String,
  override val message: String? = null,
  val details: Any? = null
) : Throwable()

enum class FilePickerType(val raw: Int) {
  /** Pick only image(s) */
  IMAGE(0),
  /** Pick only video(s) */
  VIDEO(1),
  /** Pick image(s) and video(s) */
  IMAGEANDVIDEO(2),
  /** Pick any kind of file(s) */
  GENERIC(3);

  companion object {
    fun ofRaw(raw: Int): FilePickerType? {
      return values().firstOrNull { it.raw == raw }
    }
  }
}

/** Generated interface from Pigeon that represents a handler of messages from Flutter. */
interface MoxxyPickerApi {
  /**
   * Open either the photo picker or the generic file picker to get a list of paths that were
   * selected and are accessable. If the list is empty, then the user dismissed the picker without
   * selecting anything.
   *
   * [type] specifies what kind of file(s) should be picked.
   *
   * [multiple] controls whether multiple files can be picked (true) or just a single file
   * is enough (false).
   */
  fun pickFiles(type: FilePickerType, multiple: Boolean, callback: (Result<List<String>>) -> Unit)
  /** Like [pickFiles] but sets multiple to false and returns the raw binary data from the file. */
  fun pickFileWithData(type: FilePickerType, callback: (Result<ByteArray?>) -> Unit)

  companion object {
    /** The codec used by MoxxyPickerApi. */
    val codec: MessageCodec<Any?> by lazy {
      StandardMessageCodec()
    }
    /** Sets up an instance of `MoxxyPickerApi` to handle messages through the `binaryMessenger`. */
    @Suppress("UNCHECKED_CAST")
    fun setUp(binaryMessenger: BinaryMessenger, api: MoxxyPickerApi?) {
      run {
        val channel = BasicMessageChannel<Any?>(binaryMessenger, "dev.flutter.pigeon.moxxy_native.MoxxyPickerApi.pickFiles", codec)
        if (api != null) {
          channel.setMessageHandler { message, reply ->
            val args = message as List<Any?>
            val typeArg = FilePickerType.ofRaw(args[0] as Int)!!
            val multipleArg = args[1] as Boolean
            api.pickFiles(typeArg, multipleArg) { result: Result<List<String>> ->
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
        val channel = BasicMessageChannel<Any?>(binaryMessenger, "dev.flutter.pigeon.moxxy_native.MoxxyPickerApi.pickFileWithData", codec)
        if (api != null) {
          channel.setMessageHandler { message, reply ->
            val args = message as List<Any?>
            val typeArg = FilePickerType.ofRaw(args[0] as Int)!!
            api.pickFileWithData(typeArg) { result: Result<ByteArray?> ->
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