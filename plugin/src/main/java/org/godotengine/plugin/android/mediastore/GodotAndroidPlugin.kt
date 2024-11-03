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
import org.godotengine.godot.plugin.SignalInfo
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
            emitSignal("on_image_publish_failed", "File does not exist")
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

            val context = godot as Context  // Cast godot to Context to access contentResolver
            val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                copyFileToUri(file, it)
                emitSignal("on_image_published", "Image successfully published to gallery")
            } ?: emitSignal("on_image_publish_failed", "Failed to publish image")
        } else {
            // For Android 9 and below
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val destFile = File(picturesDir, fileName)
            file.copyTo(destFile, overwrite = true)

            // Trigger media scan
            val context = godot as Context  // Cast godot to Context to access contentResolver
            context.sendBroadcast(android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(destFile)))
            emitSignal("on_image_published", "Image successfully published to gallery")
        }
    }

    @UsedByGodot
    fun publishVideoToGallery(imagePath: String) {
        val file = File(imagePath)
        if (!file.exists()) {
            emitSignal("on_image_publish_failed", "File does not exist")
            return
        }

        val fileName = file.name
        val mimeType = "video/mp4"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use MediaStore API for Android 10 and above
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }

            val context = godot as Context  // Cast godot to Context to access contentResolver
            val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                copyFileToUri(file, it)
                emitSignal("on_image_published", "Image successfully published to gallery")
            } ?: emitSignal("on_image_publish_failed", "Failed to publish image")
        } else {
            // For Android 9 and below
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val destFile = File(picturesDir, fileName)
            file.copyTo(destFile, overwrite = true)

            // Trigger media scan
            val context = godot as Context  // Cast godot to Context to access contentResolver
            context.sendBroadcast(android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(destFile)))
            emitSignal("on_image_published", "Image successfully published to gallery")
        }
    }

    private fun copyFileToUri(file: File, uri: Uri) {
        val context = godot as Context
        context.contentResolver.openOutputStream(uri).use { outputStream ->
            FileInputStream(file).use { inputStream ->
                inputStream.copyTo(outputStream!!)
            }
        }
    }

    override fun getPluginSignals(): Set<SignalInfo> {
        return setOf(
            SignalInfo("on_image_published", String::class.java),
            SignalInfo("on_image_publish_failed", String::class.java)
        )
    }
}
