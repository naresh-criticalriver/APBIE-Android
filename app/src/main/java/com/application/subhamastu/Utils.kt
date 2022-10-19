package com.application.subhamastu

import android.content.Context
import androidx.annotation.RequiresApi
import android.os.Build
import android.graphics.Bitmap
import android.graphics.Matrix
import android.provider.MediaStore
import android.media.ExifInterface
import android.net.Uri
import java.lang.Exception

object Utils {
    @RequiresApi(api = Build.VERSION_CODES.N)
    fun getRotatedBitmap(context: Context, uri: Uri?): Bitmap? {
        return try {
            var bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            val width = bitmap.width.toFloat()
            val height = bitmap.height.toFloat()
            val max = Math.max(width / 1280.0f, height / 1280.0f)
            if (max > 1.0f) {
                bitmap = Bitmap.createScaledBitmap(
                    bitmap,
                    (width / max).toInt(),
                    (height / max).toInt(),
                    false
                )
            }
            val rotateBitmap = rotateBitmap(
                bitmap,
                ExifInterface(context.contentResolver.openInputStream(uri!!)!!)
                    .getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
            )
            if (rotateBitmap != bitmap) {
                bitmap.recycle()
            }
            rotateBitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, i: Int): Bitmap? {
        val matrix = Matrix()
        when (i) {
            2 -> matrix.setScale(-1.0f, 1.0f)
            3 -> matrix.setRotate(180.0f)
            4 -> {
                matrix.setRotate(180.0f)
                matrix.postScale(-1.0f, 1.0f)
            }
            5 -> {
                matrix.setRotate(90.0f)
                matrix.postScale(-1.0f, 1.0f)
            }
            6 -> matrix.setRotate(90.0f)
            7 -> {
                matrix.setRotate(-90.0f)
                matrix.postScale(-1.0f, 1.0f)
            }
            8 -> matrix.setRotate(-90.0f)
            else -> return bitmap
        }
        return try {
            val createBitmap =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
            bitmap.recycle()
            createBitmap
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            null
        }
    }
}