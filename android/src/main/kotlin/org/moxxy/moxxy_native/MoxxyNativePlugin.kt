package org.moxxy.moxxy_native

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import org.moxxy.moxxy_native.generated.FilePickerType
import org.moxxy.moxxy_native.generated.MoxxyPickerApi
import org.moxxy.moxxy_native.picker.PickerResultListener

class MoxxyNativePlugin: FlutterPlugin, ActivityAware, MoxxyPickerApi {
  private var context: Context? = null
  private var activity: Activity? = null
  private lateinit var pickerListener: PickerResultListener

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    context = flutterPluginBinding.applicationContext
    MoxxyPickerApi.setUp(flutterPluginBinding.binaryMessenger, this)
    pickerListener = PickerResultListener(context!!)
    Log.d(TAG, "Attached to engine")
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    Log.d(TAG, "Detached from engine")
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activity = binding.activity
    binding.addActivityResultListener(pickerListener)
    Log.d(TAG, "Attached to activity")
  }

  override fun onDetachedFromActivityForConfigChanges() {
    activity = null
    Log.d(TAG, "Detached from activity")
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    activity = binding.activity
  }

  override fun onDetachedFromActivity() {
    activity = null
    Log.d(TAG, "Detached from activity")
  }

  override fun pickFiles(
    type: FilePickerType,
    multiple: Boolean,
    callback: (Result<List<String>>) -> Unit
  ) {
    val requestCode = if (multiple) PICK_FILES_REQUEST else PICK_FILE_REQUEST
    AsyncRequestTracker.requestTracker[requestCode] = callback as (Result<Any>) -> Unit
    if (type == FilePickerType.GENERIC) {
      val pickIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        this.type = "*/*"

        // Allow/disallow picking multiple files
        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, multiple)
      }
      activity?.startActivityForResult(pickIntent, requestCode)
      return
    }

    val contract = when (multiple) {
      false -> ActivityResultContracts.PickVisualMedia()
      true -> ActivityResultContracts.PickMultipleVisualMedia()
    }
    val pickType = when (type) {
      // We keep FilePickerType.GENERIC here, even though we know that @type will never be
      // GENERIC to make Kotlin happy.
      FilePickerType.GENERIC, FilePickerType.IMAGE -> ActivityResultContracts.PickVisualMedia.ImageOnly
      FilePickerType.VIDEO -> ActivityResultContracts.PickVisualMedia.VideoOnly
      FilePickerType.IMAGEANDVIDEO -> ActivityResultContracts.PickVisualMedia.ImageAndVideo
    }
    val pickIntent = contract.createIntent(context!!, PickVisualMediaRequest(pickType))
    activity?.startActivityForResult(pickIntent, requestCode)
  }

  override fun pickFileWithData(type: FilePickerType, callback: (Result<ByteArray?>) -> Unit) {
    AsyncRequestTracker.requestTracker[PICK_FILE_WITH_DATA_REQUEST] = callback as (Result<Any>) -> Unit
    if (type == FilePickerType.GENERIC) {
      val pickIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        this.type = "*/*"
      }
      activity?.startActivityForResult(pickIntent, PICK_FILE_WITH_DATA_REQUEST)
      return
    }

    val pickType = when (type) {
      // We keep FilePickerType.GENERIC here, even though we know that @type will never be
      // GENERIC to make Kotlin happy.
      FilePickerType.GENERIC, FilePickerType.IMAGE -> ActivityResultContracts.PickVisualMedia.ImageOnly
      FilePickerType.VIDEO -> ActivityResultContracts.PickVisualMedia.VideoOnly
      FilePickerType.IMAGEANDVIDEO -> ActivityResultContracts.PickVisualMedia.ImageAndVideo
    }
    val contract = ActivityResultContracts.PickVisualMedia()
    val pickIntent = contract.createIntent(context!!, PickVisualMediaRequest(pickType))
    activity?.startActivityForResult(pickIntent, PICK_FILE_WITH_DATA_REQUEST)
  }
}
