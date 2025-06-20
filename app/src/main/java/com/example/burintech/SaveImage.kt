package com.example.burintech

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore

fun saveImage(context: Context, filename:String, uri: Uri) : Uri?{
    return try {
        // 1. Получаем Bitmap из временного Uri
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        if (bitmap == null) {
            null
        } else {
            // 2. Сохраняем в постоянное хранилище
            // Для Android 10+ используем MediaStore
            saveImageMediaStore(context, filename, bitmap)

        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

}
private fun saveImageMediaStore(context: Context, filename: String, bitmap: Bitmap): Uri? {

    val folderName = "BURINTECH"

    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "$filename.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$folderName/")
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }
    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    return uri?.also {
        try {
            resolver.openOutputStream(it)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            }
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        } catch (e: Exception) {
            resolver.delete(uri, null, null)

        }
    }
}