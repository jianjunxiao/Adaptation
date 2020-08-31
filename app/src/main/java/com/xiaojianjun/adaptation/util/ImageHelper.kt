package com.xiaojianjun.adaptation.util

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 将图片文件保存到系统相册
 * 重复保存同名文件的话，会在文件名会变化，比如：test.jpg、test.jpg(1)、test.jpg
 */
suspend fun saveImageToAlbum(context: Context, imageFile: File, imageName: String): Boolean {
    return withContext(Dispatchers.IO) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, imageName)
            put(MediaStore.Images.Media.DESCRIPTION, "图片描述")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
            // 文件待处理状态，本应用独占，其他应用暂时无法访问
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        // 插入数据库，获得uri
        val uri = context.contentResolver
            .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: return@withContext false
        // 根据uri打开输出流，写入imageFile文件
        try {
            context.contentResolver.openOutputStream(uri)?.use { it.write(imageFile.readBytes()) }
        } catch (e: Exception) {
            return@withContext false
        }
        // 切换文件待处理状态
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            context.contentResolver.update(uri, contentValues, null, null)
        }
        return@withContext true
    }
}

/**
 * 从系统相册获取图片
 */
suspend fun getImageFileFromAlbum(context: Context, fileName: String): Bitmap? {
    return withContext(Dispatchers.IO) {
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DESCRIPTION
        )
        val selection = "${MediaStore.Images.Media.DISPLAY_NAME} >= ?"
        val selectionArgs = arrayOf(fileName)
        val sortOrder = "${MediaStore.Images.Media.DISPLAY_NAME} ASC"
        context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val contentUri = ContentUris.withAppendedId(uri, id)
                context.contentResolver.openInputStream(contentUri)?.use { inputStream ->
                    return@withContext BitmapFactory.decodeStream(inputStream)
                    // 如果是gif图，通过Bitmap来读取会失去动效，可以先保存到专属存储空间，再访问
                    // 或者直接将contentUri给Glide来进行加载
                }
            }
        }
        return@withContext null
    }
}

suspend fun deleteImageFromAlbum(context: Context, fileName: String): Boolean {
    return withContext(Dispatchers.IO) {
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val selection = "${MediaStore.Images.Media.DISPLAY_NAME} >= ?"
        val selectionArgs = arrayOf(fileName)
        val r = context.contentResolver.delete(uri, selection, selectionArgs)
        return@withContext r != -1
    }
}

fun queryAllImageUris(context: Context): MutableList<Uri> {
    val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val contentUriList = mutableListOf<Uri>()
    context.contentResolver.query(uri, null, null, null, null)?.use {
        val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        while (it.moveToNext()) {
            val id = it.getLong(idColumn)
            val contentUri = ContentUris.withAppendedId(uri, id)
            contentUriList.add(contentUri)
        }
    }
    return contentUriList
}