package org.godotengine.plugin.android.mediastore

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.UsedByGodot
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream

class GodotAndroidPlugin(godot: Godot): GodotPlugin(godot) {

    override fun getPluginName() = BuildConfig.GODOT_PLUGIN_NAME

    @UsedByGodot
    fun publishImageToGallery(imagePath: String) {
        val file = File(imagePath)
        if (!file.exists()) {
            return
        }

        val fileName = file.name
        val mimeType = "image/jpeg"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use MediaStore API for Android 10 and above
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }

            val uri: Uri? = godot.getActivity()?.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                copyFileToUri(file, it)                
            }
        } else {
            // For Android 9 and below
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val destFile = File(picturesDir, fileName)
            file.copyTo(destFile, overwrite = true)

            // Trigger media scan
            godot.getActivity()?.sendBroadcast(android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(destFile)))
        }
    }

    @UsedByGodot
    fun publishVideoToGallery(imagePath: String) {
        val file = File(imagePath)
        if (!file.exists()) {
            return
        }

        val fileName = file.name
        val mimeType = "video/mp4"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use MediaStore API for Android 10 and above
            val contentValues = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Video.Media.MIME_TYPE, mimeType)
                put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
            }

            val uri: Uri? = godot.getActivity()?.contentResolver?.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                copyFileToUri(file, it)                
            }
        } else {
            // For Android 9 and below
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            val destFile = File(picturesDir, fileName)
            file.copyTo(destFile, overwrite = true)

            // Trigger media scan
            godot.getActivity()?.sendBroadcast(android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(destFile)))
        }
    }

    private fun copyFileToUri(file: File, uri: Uri) {
        godot.getActivity()?.contentResolver?.openOutputStream(uri).use { outputStream ->
            FileInputStream(file).use { inputStream ->
                inputStream.copyTo(outputStream!!)
            }
        }
    }
}
