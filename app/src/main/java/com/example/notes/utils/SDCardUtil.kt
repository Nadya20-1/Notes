package com.example.notes.utils

import android.graphics.Bitmap
import android.os.Environment
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

object SDCardUtil {
    private var SDCardRoot = Environment.getExternalStorageDirectory().absolutePath + File.separator
    private var APP_NAME = "Notes"
    private val pictureDir: String
        get() {
            val imageCacheUrl =
                SDCardRoot + APP_NAME + File.separator
            val file = File(imageCacheUrl)
            if (!file.exists()) file.mkdir()
            return imageCacheUrl
        }

    fun saveToSdCard(bitmap: Bitmap): String {
        val imageUrl =
            pictureDir + System.currentTimeMillis() + "-"
        val file = File(imageUrl)
        try {
            val out = FileOutputStream(file)
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)) {
                out.flush()
                out.close()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file.absolutePath
    }

    fun deleteFile(filePath: String?): Boolean {
        val file = File(filePath)
        var isOk = false
        if (file.isFile && file.exists()) isOk = file.delete()
        return isOk
    }
}