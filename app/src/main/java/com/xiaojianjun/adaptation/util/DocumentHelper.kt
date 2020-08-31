package com.xiaojianjun.adaptation.util

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android 10及以上设备保存txt文档到媒体下载目录
 * 重复保存同名文档的话，会在文件名会变化，比如：test.txt、test(1).txt、test(1).txt
 */
@RequiresApi(Build.VERSION_CODES.Q)
suspend fun doSaveDocumentToMediaDownload(
    context: Context,
    content: String,
    documentName: String,
    mineType: String
): Boolean {
    return withContext(Dispatchers.IO) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, documentName)
            put(MediaStore.Downloads.MIME_TYPE, mineType)
            // 文件待处理状态，本应用独占，其他应用暂时无法访问
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        // 在download表中插入记录
        val uri = context.contentResolver
            .insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: return@withContext false
        try {
            // 写入文档内容
            context.contentResolver.openOutputStream(uri)?.use { it.write(content.toByteArray()) }
        } catch (e: Exception) {
            return@withContext false
        }
        // 切换文件待处理状态
        contentValues.clear()
        contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
        val row = context.contentResolver.update(uri, contentValues, null, null)
        return@withContext row != -1
    }
}

/**
 * 从下载目录获取文档的内容
 */
@RequiresApi(Build.VERSION_CODES.Q)
suspend fun getDocumentContentFromMediaDownload(
    context: Context,
    documentName: String,
    mineType: String
): String? {
    return withContext(Dispatchers.IO) {
        val uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Downloads._ID,
            MediaStore.Downloads.DISPLAY_NAME
        )
        val selection =
            "${MediaStore.Downloads.DISPLAY_NAME}=? and ${MediaStore.Downloads.MIME_TYPE}=?"
        val selectionArgs = arrayOf(documentName, mineType)
        val sortOrder = "${MediaStore.Downloads.DISPLAY_NAME} ASC"
        context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Downloads._ID)
            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val contentUri =
                    ContentUris.withAppendedId(uri, id)
                context.contentResolver.openInputStream(contentUri)?.use { inputStream ->
                    return@withContext String(inputStream.readBytes())
                }
            }
        }
        return@withContext null
    }
}

