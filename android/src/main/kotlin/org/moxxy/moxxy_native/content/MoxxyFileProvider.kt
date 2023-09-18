package org.moxxy.moxxy_native.content

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import org.moxxy.moxxy_native.MOXXY_FILEPROVIDER_ID
import org.moxxy.moxxy_native.R
import java.io.File

class MoxxyFileProvider : FileProvider(R.xml.file_paths) {
    companion object {
        /*
         * Convert a path @path inside a sharable storage directory into a content URI, given
         * the application's context @context.
         * */
        fun getUriForPath(context: Context, path: String): Uri {
            return getUriForFile(
                context,
                MOXXY_FILEPROVIDER_ID,
                File(path),
            )
        }
    }
}
