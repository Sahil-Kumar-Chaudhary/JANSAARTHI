package com.example.jansaarthi.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.InputStream
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MultipartBody


object ImageUtils {
    fun getCompressedImagePart(context: Context, uri: Uri, paramName: String = "photo"): MultipartBody.Part? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) return null

            val maxSize = 800
            val width = bitmap.width
            val height = bitmap.height
            val scale = Math.min(maxSize.toFloat() / width, maxSize.toFloat() / height)
            
            val scaledBitmap = if (scale < 1) {
                Bitmap.createScaledBitmap(bitmap, (width * scale).toInt(), (height * scale).toInt(), true)
            } else {
                bitmap
            }

            val baos = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val byteArray = baos.toByteArray()

            val requestBody = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
            MultipartBody.Part.createFormData(paramName, "profile.jpg", requestBody)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

