package com.lyc.sstcontrol.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max
import kotlin.math.min

object PhotoUtils {

    fun createTempImageFile(context: Context): File {
        val dir = File(context.cacheDir, "photos").apply { mkdirs() }
        return File.createTempFile("evidence_", ".jpg", dir)
    }

    fun compressToBase64(file: File, maxSide: Int = 1024, quality: Int = 60): String? {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

            var sample = 1
            while (bounds.outWidth / (sample * 2) >= maxSide && bounds.outHeight / (sample * 2) >= maxSide) {
                sample *= 2
            }
            val options = BitmapFactory.Options().apply { inSampleSize = sample }
            val decoded = BitmapFactory.decodeFile(file.absolutePath, options) ?: return null

            val orientation = ExifInterface(file.absolutePath)
                .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            val rotation = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
            val scale = min(1f, maxSide.toFloat() / max(decoded.width, decoded.height))
            val matrix = Matrix().apply {
                postRotate(rotation)
                postScale(scale, scale)
            }
            val result = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)

            val output = ByteArrayOutputStream()
            result.compress(Bitmap.CompressFormat.JPEG, quality, output)
            Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        } finally {
            file.delete()
        }
    }

    fun base64ToBitmap(data: String, sampleSize: Int = 1): Bitmap? = try {
        val bytes = Base64.decode(data, Base64.DEFAULT)
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    } catch (e: Exception) {
        null
    }
}