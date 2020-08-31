package com.xiaojianjun.adaptation.util

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 协程下载图片，返回file为null则下载失败，不为null则下载成功
 */
suspend fun downloadImage(context: Context, url: String): File? {
    return withContext(Dispatchers.IO) {
        return@withContext try {
            Glide.with(context)
                .downloadOnly()
                .load(url)
                .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .get()
        } catch (e: Exception) {
            null
        }
    }
}