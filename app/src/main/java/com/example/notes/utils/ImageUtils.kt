package com.example.notes.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

object ImageUtils {
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int, reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val heightRatio =
                Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio =
                Math.round(width.toFloat() / reqWidth.toFloat())

            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        return inSampleSize
    }

    fun getSmallBitmap(filePath: String?, newWidth: Int, newHeight: Int): Bitmap? {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, options)
        options.inSampleSize = calculateInSampleSize(options, newWidth, newHeight)
        options.inJustDecodeBounds = false
        val bitmap = BitmapFactory.decodeFile(filePath, options)
        val newBitmap = compressImage(bitmap, 500)
        bitmap?.recycle()
        return newBitmap
    }

    private fun compressImage(image: Bitmap?, maxSize: Int): Bitmap? {
        val os = ByteArrayOutputStream()
        var options = 80
        image!!.compress(Bitmap.CompressFormat.JPEG, options, os)
        while (os.toByteArray().size / 1024 > maxSize) {
            os.reset()
            options -= 10
            image.compress(Bitmap.CompressFormat.JPEG, options, os)
        }
        var bitmap: Bitmap? = null
        val b = os.toByteArray()
        if (b.size != 0) {
            bitmap = BitmapFactory.decodeByteArray(b, 0, b.size)
        }
        return bitmap
    }
}