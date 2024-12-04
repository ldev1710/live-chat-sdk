package com.mitek.build.live.chat.sdk.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object FileUtils {
    fun openFileStream(context: Context, uri: Uri): String? {
        var fos: FileOutputStream? = null
        val fileName: String? = getFileName(uri, context)
        val path =
            context.cacheDir.absolutePath + "/lc_file_picker/" + System.currentTimeMillis() + "/" + (fileName
                ?: "unamed")

        val file = File(path)

        if (!file.exists()) {
            file.parentFile.mkdirs()
            try {
                fos = FileOutputStream(path)
                try {
                    val out = BufferedOutputStream(fos)
                    val `in` = context.contentResolver.openInputStream(uri)

                    val buffer = ByteArray(8192)
                    var len = 0

                    while ((`in`!!.read(buffer).also { len = it }) >= 0) {
                        out.write(buffer, 0, len)
                    }

                    out.flush()
                } finally {
                    fos.fd.sync()
                }
            } catch (e: Exception) {
                try {
                    fos!!.close()
                } catch (ex: IOException) {
                    LCLog.logE(message = "Failed to close file streams: " + e.message)
                    return null
                } catch (ex: NullPointerException) {
                    LCLog.logE(message = "Failed to close file streams: " + e.message)
                    return null
                }
                LCLog.logE(message = "Failed to retrieve path: " + e.message)
                return null
            }
        }

        LCLog.logE(message = "File loaded and cached at:$path")
        return path
    }

    fun getFileName(uri: Uri, context: Context): String? {
        var result: String? = null

        try {
            if (uri.scheme == "content") {
                val cursor = context.contentResolver.query(
                    uri,
                    arrayOf(OpenableColumns.DISPLAY_NAME),
                    null,
                    null,
                    null
                )
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        result =
                            cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                    }
                } finally {
                    cursor!!.close()
                }
            }
            if (result == null) {
                result = uri.path
                val cut = result!!.lastIndexOf('/')
                if (cut != -1) {
                    result = result!!.substring(cut + 1)
                }
            }
        } catch (ex: Exception) {
        }

        return result
    }
}
