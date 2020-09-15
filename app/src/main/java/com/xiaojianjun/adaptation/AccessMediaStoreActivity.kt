package com.xiaojianjun.adaptation

import android.app.RecoverableSecurityException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.xiaojianjun.adaptation.util.*
import kotlinx.android.synthetic.main.activity_access_media_store.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream

class AccessMediaStoreActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "分区存储(MediaStore)"
        private const val IMAGE_URL_A =
            "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/04/2017-04-26_10-00-28.jpg"
        private const val IMAGE_URL_B =
            "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-17_17-30-43.jpg"
        private const val IMAGE_NAME_A = "搞笑图片(a).jpg"
        private const val IMAGE_NAME_B = "搞笑图片(b).jpg"
        private const val DOCUMENT_CONTENT_A = "这是搞笑段子(a)的内容"
        private const val DOCUMENT_CONTENT_B = "这是搞笑段子(b)的内容"
        private const val DOCUMENT_NAME_A = "搞笑段子(a).txt"
        private const val DOCUMENT_NAME_B = "搞笑段子(b).txt"
        private const val TXT = "text/plain"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_access_media_store)
        title = getString(R.string.app_name) + " :   MediaStore"
        init()
    }

    private fun isPackageA(): Boolean {
        return this.packageName == "com.ztzh.adaptation.a"
    }

    private fun init() {
        // 下载图片到系统相册
        btnDownloadImageToAlbum.setOnClickListener {
            if (isPackageA()) {
                downloadImageIntoAlbum(IMAGE_URL_A, IMAGE_NAME_A)
            } else {
                downloadImageIntoAlbum(IMAGE_URL_B, IMAGE_NAME_B)
            }
        }
        // 读取本应用下载到系统相册的图片
        btnReadOwnAlbumImage.setOnClickListener {
            readOwnDownloadAlbumImage(if (isPackageA()) IMAGE_NAME_A else IMAGE_NAME_B)
        }
        // 读取其他应用下载到系统相册的图片
        btnReadOtherAppAlbumImage.setOnClickListener {
            readOtherAppAlbumImage(if (isPackageA()) IMAGE_NAME_B else IMAGE_NAME_A)
        }
        // 删除本应用下载到系统相册中的图片
        btnDeleteOwnAlbumImage.setOnClickListener {
            deleteOwnImageFromAlbum(if (isPackageA()) IMAGE_NAME_A else IMAGE_NAME_B)
        }
        // 删除其他应用下载到系统相册中的图片
        btnDeleteOtherAlbumImage.setOnClickListener {
            deleteOtherImageFromAlbum(if (isPackageA()) IMAGE_NAME_B else IMAGE_NAME_A)
        }

        // 保存文档到媒体下载目录
        btnSaveDocumentToMediaDownload.setOnClickListener {
            if (isPackageA()) {
                saveDocumentToMediaDownload(DOCUMENT_CONTENT_A, DOCUMENT_NAME_A, TXT)
            } else {
                saveDocumentToMediaDownload(DOCUMENT_CONTENT_B, DOCUMENT_NAME_B, TXT)
            }
        }
        // 读取本应用保存到媒体下载目录中的文档
        btnReadOwnMediaDownloadDocument.setOnClickListener {
            readOwnMediaDownloadDocument(
                if (isPackageA()) DOCUMENT_NAME_A else DOCUMENT_NAME_B, TXT
            )
        }
        // 删除本应用保存到媒体下载目录中的文档
        btnDeleteOwnMediaDownloadDocument.setOnClickListener {
            deleteOwnDownloadDocument(if (isPackageA()) DOCUMENT_NAME_A else DOCUMENT_NAME_B, TXT)
        }
        // TODO 读无法取其他应用保存到媒体下载目录中的文档，不能使用这次方式，要用SAF才能访问
        btnReadOtherAppMediaDownloadDocument.let {
            it.paintFlags = (it.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG)
            it.setOnClickListener {
                readOtherAppMediaDownloadDocument(
                    if (isPackageA()) DOCUMENT_NAME_B else DOCUMENT_NAME_A, TXT
                )
            }
        }
        // 媒体文件执行批量操作，Android11新增
        btnMediaStoreBatchOperation.setOnClickListener {
            doBatchOperationMediaStore()
        }
        // 通过FileAPI和路径读取本应用保存的相册图片
        btnReadOwnAlbumImageByFileApi.setOnClickListener {
            readOwnAlbumImageByFileApi(true)
        }
        // 通过FileAPI和路径读取其他应用保存的相册图片
        btnReadOtherAlbumImageByFileApi.setOnClickListener {
            readOwnAlbumImageByFileApi(false)
        }
        // 通过FileAPI和路径删除本应用保存的相册图片
        btnDeleteOwnAlbumImageByFileApi.setOnClickListener {
            deleteAlbumImageByFileApi(true)
        }
        // TODO 无法通过FileAPI和路径删除其他应用保存的相册图片，只有通过MediaStore抛出异常后用户二次确认才能删除
        btnDeleteOtherAlbumImageByFileApi.let {
            it.paintFlags = (it.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG)
            it.setOnClickListener {
                deleteAlbumImageByFileApi(false)
            }
        }
    }

    /**
     * 下载图片到相册
     * Android 10、11系统版本的设备，不需要存储权限，可直接操作。
     * Android 9及以下系统版本的设备，需要申请存储权限才能操作。
     * 谷歌官方给的适配建议是不要使用MediaStore API 不要在Android 10以上系统版本的设备申请存储权限，只在Android 9及以下申请。
     * 注意：MediaStore API 需不需要存储权限与targetApi无关，只与设备系统版本(高于Android10不需要权限)有关
     * @param url 图片链接
     * @param imageName 图片名称
     */
    private fun downloadImageIntoAlbum(url: String, imageName: String) {
        lifecycleScope.launch {
            // 下载得到imageFile
            val imageFile = downloadImage(this@AccessMediaStoreActivity, url)
            if (imageFile == null) {
                showToastAndLog("图片下载失败")
                return@launch
            }
            // 开启了分区存储
            // 根据设备系统版本判断是否需要存储权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10 及以上设备
                val result = saveImageToAlbum(this@AccessMediaStoreActivity, imageFile, imageName)
                if (result) {
                    showToastAndLog("Android10及以上设备:图片保存到相册成功")
                } else {
                    showToastAndLog("Android10及以上设备:图片保存到相册失败")
                }
            } else {
                // Android 9 及以下设备
                if (suspendRequestStoragePermission()) {
                    val result =
                        saveImageToAlbum(this@AccessMediaStoreActivity, imageFile, imageName)
                    if (result) {
                        showToastAndLog("Android9及以下设备:图片保存到相册成功")
                    } else {
                        showToastAndLog("Android9及以下设备:图片保存到相册失败")
                    }
                } else {
                    showToastAndLog("Android9及以下设备:存储权限拒绝")
                }
            }
        }
    }

    /**
     * 访问本应用下载到相册的图片
     * 在Android 10 及以上不需要存储权限，Android 9及以下需要存储权限。
     * 在Android 10 及以上的版本，如果应用卸载了，再重新安装，那么之前下载的图片也不能读取了(不会抛出异常，只是查询不到)；
     * 在Android 10 及以上的设备系统版本，卸载重装，但是如果申请了存储权限的话，还是能读取到之前下载的图片。
     */
    private fun readOwnDownloadAlbumImage(imageName: String) {
        lifecycleScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android10及以上系统
                val bitmap = getImageFileFromAlbum(this@AccessMediaStoreActivity, imageName)
                if (bitmap == null) {
                    showToastAndLog("Android10及以上系统读取本应用下载到相册的图片失败")
                } else {
                    showToastAndLog("Android10及以上系统读取本应用下载到相册的图片成功")
                    ImageFragment().show(supportFragmentManager, bitmap)
                }
            } else {
                // Android9及以下系统
                // 申请存储权限
                if (suspendRequestStoragePermission()) {
                    val bitmap = getImageFileFromAlbum(this@AccessMediaStoreActivity, imageName)
                    if (bitmap == null) {
                        showToastAndLog("Android9及以下系统读取本应用下载到相册的图片失败")
                    } else {
                        showToastAndLog("Android9及以下系统读取本应用下载到相册的图片成功")
                        ImageFragment().show(supportFragmentManager, bitmap)
                    }
                } else {
                    // 存储权限拒绝
                    showToastAndLog("Android9及以下存储权限拒绝")
                }
            }
        }
    }

    /**
     * 访问其他App存储到系统相册的图片
     */
    private fun readOtherAppAlbumImage(fileName: String) {
        lifecycleScope.launch {
            // 申请存储权限
            if (suspendRequestStoragePermission()) {
                val bitmap = getImageFileFromAlbum(this@AccessMediaStoreActivity, fileName)
                if (bitmap == null) {
                    showToastAndLog("读取其他应用下载到相册的图片失败")
                } else {
                    showToastAndLog("读取其他应用下载到相册的图片成功")
                    ImageFragment().show(supportFragmentManager, bitmap)
                }
            } else {
                // 存储权限拒绝
                showToastAndLog("存储权限拒绝")
            }
        }
    }

    /**
     * 删除系统相册中的图片
     * 本应用保存到相册中的图片，可直接删除，无需存储权限
     */
    private fun deleteOwnImageFromAlbum(fileName: String) {
        lifecycleScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val result = deleteOwnImageFromAlbum(this@AccessMediaStoreActivity, fileName)
                if (!result) {
                    showToastAndLog("删除图片失败：可能没有该图片")
                } else {
                    showToastAndLog("删除图片成功")
                }
            } else {
                if (suspendRequestStoragePermission()) {
                    val result = deleteOwnImageFromAlbum(this@AccessMediaStoreActivity, fileName)
                    if (!result) {
                        showToastAndLog("删除图片失败：可能没有该图片")
                    } else {
                        showToastAndLog("删除图片成功")
                    }
                }
            }
        }
    }

    /**
     * 删除系统相册中的图片
     * 其他应用保存到相册的图片，删除时，有存储权限会抛出SecurityException，没有存储权限查找不到其他应用的图片
     * 抛出安全异常，捕获后判断版本和权限
     */
    private fun deleteOtherImageFromAlbum(fileName: String) {
        lifecycleScope.launch {
            if (suspendRequestStoragePermission()) {
                try {
                    val result = deleteOwnImageFromAlbum(this@AccessMediaStoreActivity, fileName)
                    if (!result) {
                        showToastAndLog("删除其他应用的相册图片失败：可能该图片不存在着")
                    } else {
                        showToastAndLog("删除其他应用的相册图片成功")
                    }
                } catch (se: SecurityException) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val recoverableSecurityException = se as? RecoverableSecurityException
                            ?: return@launch showToastAndLog(se.message.toString())
                        val intentSender =
                            recoverableSecurityException.userAction.actionIntent.intentSender
                        if (suspendLaunchSenderIntentForResult(intentSender)) {
                            // 用户同意后，再去执行删除，如果不执行，只要在App重启前执行都不会再次抛出se异常
                            val result =
                                deleteOwnImageFromAlbum(this@AccessMediaStoreActivity, fileName)
                            if (!result) {
                                showToastAndLog("删除其他应用的相册图片失败")
                            } else {
                                showToastAndLog("删除其他应用的相册图片成功")
                            }
                        } else {
                            showToastAndLog("二次确认被拒绝")
                        }
                    }
                }
            } else {
                // 没有存储权限，无法访问其他应用保存的相册图片，也就无法删除
                showToastAndLog("没有存储权限，无法访问，无法删除")
            }
        }
    }

    /**
     * 保存文档到媒体下载目录
     * 只有Android10及以上设备可用，且需要存储权限
     * Android9及以下设备不可用。
     */
    private fun saveDocumentToMediaDownload(
        content: String,
        documentName: String,
        mineType: String
    ) {
        lifecycleScope.launch {
            // Android 10及以上设备
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val result = doSaveDocumentToMediaDownload(
                    this@AccessMediaStoreActivity,
                    content,
                    documentName,
                    mineType
                )
                if (result) {
                    showToastAndLog("保存文档到媒体下载目录成功")
                } else {
                    showToastAndLog("保存文档到媒体下载目录失败")
                }
            } else {
                showToastAndLog("Android 9及以下不支持")
            }
        }
    }

    /**
     * 读取本应用保存到媒体下载目录中的文档，只有Android10及以上设备可用，Android9及以下设备不可用。
     * Android 10 及以上的设备不需要存储权限。
     * Android 10 及以上的版本，如果应用卸载了，再重新安装，那么之前保存到媒体下载目录的文件不能读取了(不会抛出异常，只是查询不到)；
     * Android 10 及以上的设备系统版本，卸载重装，即使申请了存储权限，也不能读取到之前下载的图片，只有通过SAF才能读取到。
     */
    private fun readOwnMediaDownloadDocument(documentName: String, mineType: String) {
        lifecycleScope.launch {
            // Android 10及以上设备
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val result = getDocumentContentFromMediaDownload(
                    context = this@AccessMediaStoreActivity,
                    documentName = documentName,
                    mineType = mineType
                )
                if (result.isNullOrEmpty()) {
                    showToastAndLog("访问本应用媒体下载目录中的文档内容失败")
                } else {
                    showToastAndLog("访问本应用媒体下载目录中的文档内容成功:\n$result")
                }
            } else {
                showToastAndLog("Android 9及以下不支持")
            }
        }
    }

    /**
     * 删除本应用保存到媒体下载目录中的文档，只有Android10及以上设备可用，Android9及以下设备不可用。
     * Android 10 及以上的设备不需要存储权限。
     * Android 10 及以上的版本，如果应用卸载了，再重新安装，那么之前保存到媒体下载目录的文件不能删除了(不会抛出异常，只是查询不到)；
     * Android 10 及以上的设备系统版本，卸载重装，即使申请了存储权限，也不能删除之前下载的图片，只有通过SAF才能操作。
     */
    private fun deleteOwnDownloadDocument(documentName: String, mineType: String) {
        lifecycleScope.launch {
            // Android 10及以上设备
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val result = deleteDocumentFromMediaDownload(
                    context = this@AccessMediaStoreActivity,
                    documentName = documentName,
                    mineType = mineType
                )
                if (result) {
                    showToastAndLog("删除本应用媒体下载目录中的文档内容成功")
                } else {
                    showToastAndLog("删除本应用媒体下载目录中的文档内容失败")
                }
            } else {
                showToastAndLog("Android 9及以下不支持")
                // 通过File API去删
            }
        }
    }

    /**
     * TODO 不能使用！不能使用！不能使用！此处只是为了测试！
     * 不能使用MediaStore API访问其他应用保存到媒体下载目录中的文档
     * 必须使用存储访问框架才能访问其他应用保存到媒体下载目录的文档
     */
    @Deprecated("不能使用这种方式访问，不敢哪个版本，是否有存储权限，结果肯定是失败！！！")
    private fun readOtherAppMediaDownloadDocument(documentName: String, mineType: String) {
        lifecycleScope.launch {
            // Android 10及以上设备
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // 申请存储权限
                if (suspendRequestStoragePermission()) {
                    val result = getDocumentContentFromMediaDownload(
                        context = this@AccessMediaStoreActivity,
                        documentName = documentName,
                        mineType = mineType
                    )
                    if (result.isNullOrEmpty()) {
                        showToastAndLog("访问其他应用媒体下载目录中的文档内容失败")
                    } else {
                        showToastAndLog("访问媒其他应用媒体下载目录中的文档内容成功:\n$result")
                    }
                } else {
                    showToastAndLog("存储权限拒绝")
                }
            } else {
                showToastAndLog("Android 9及以下不支持")
            }
        }
    }

    /**
     * MediaStore执行批量操作确认
     * 只在Android11及以上可用
     * 猜想批量操作需要弹出询问框，可能是因为操作的批量Uri可能含有其他应用创建的文件
     */
    private fun doBatchOperationMediaStore() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val urisToModify = queryAllImageUris(this)
            if (urisToModify.isEmpty()) {
                return showToastAndLog("没有可操作的图片")
            }
            val editPendingIntent = MediaStore.createWriteRequest(contentResolver, urisToModify)
            launchSenderIntentForResult(editPendingIntent.intentSender) {
                if (it) {
                    /* Edit request granted; proceed. */
                } else {
                    /* Edit request not granted; explain to the user. */
                }
            }
            // or 收藏、放入垃圾箱、删除
            // MediaStore.createFavoriteRequest(contentResolver, urisToModify)
            // MediaStore.createTrashRequest(contentResolver, urisToModify, true)
            // MediaStore.createDeleteRequest(contentResolver, urisToModify)
        } else {
            showToastAndLog("Android 10及以下设备不支持")
        }
    }

    /**
     * 使用直接路径通过File APi来访问系统相册中的照片
     * 在Android 11 及以上，本应用创建的的无需存储权限，直接访问，其他应用创建的需要权限
     * Android 10，不支持，除非禁用分区存储
     * Android 9 及以下，有存储权限才可访问
     *
     * 图片、适配、音频、下载目录文件，不论归属是本应用还是其他应用，
     * 不管是读取、编辑、删除等访问操作的是否需要存储权限的限制条件是和MediaStore操作是一样的。
     *
     * @param isOwner 是否是自己应用保存的图片
     */
    private fun readOwnAlbumImageByFileApi(isOwner: Boolean) {
        val fileName = if (isOwner) {
            if (isPackageA()) IMAGE_NAME_A else IMAGE_NAME_B
        } else {
            if (isPackageA()) IMAGE_NAME_B else IMAGE_NAME_A
        }
        lifecycleScope.launch {
            when {
                // Android 11 及以上
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                    if (isOwner || (isOwner.not() && suspendRequestStoragePermission())) {
                        val bmp = doGetImageByFileApi(fileName)
                        ImageFragment().show(supportFragmentManager, bmp)
                        showToastAndLog("在Android11通过FileAPI使用直接路径访问成功")
                    }
                }
                // Android 10
                Build.VERSION.SDK_INT == Build.VERSION_CODES.Q -> {
                    showToastAndLog("Android 10 不支持FileAPI访问")
                }
                // Android 9及以下
                else -> {
                    if (suspendRequestStoragePermission()) {
                        val bmp = doGetImageByFileApi(fileName)
                        ImageFragment().show(supportFragmentManager, bmp)
                        showToastAndLog("在Android9及以下通过FileAPI使用直接路径访问成功")
                    } else {
                        showToastAndLog("存储权限拒绝")
                    }
                }
            }
        }
    }

    /**
     * File Api 读取图片
     */
    private suspend fun doGetImageByFileApi(fileName: String): Bitmap {
        return withContext(Dispatchers.IO) {
            val path =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path + "/" + fileName
            FileInputStream(path).use {
                return@withContext BitmapFactory.decodeStream(it)
            }
        }
    }

    /**
     * 删除系统相册中的照片
     * 只能删除本应用保存到系统相册中的照片
     * 其他应用保存到系统相册中的照片，无法通过File API 删除，就算有存储权限也不行
     * 不管有没有存储权限都不会抛出异常。
     * @param isOwner 是否是本应用保存到系统相册中的
     */
    private fun deleteAlbumImageByFileApi(isOwner: Boolean) {
        val fileName = if (isOwner) {
            if (isPackageA()) IMAGE_NAME_A else IMAGE_NAME_B
        } else {
            if (isPackageA()) IMAGE_NAME_B else IMAGE_NAME_A
        }
        lifecycleScope.launch {
            when {
                // Android 11 及以上
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                    if (isOwner) {
                        val result = doDeleteImageByFileApi(fileName)
                        if (result) {
                            showToastAndLog("Android11以上通过FileApi删除图片成功")
                        } else {
                            showToastAndLog("Android11以上通过FileApi删除图片失败")
                        }
                    } else {
                        showToastAndLog("Android11不支持通过FileApi删其他应用的图片，即使有权限也不行")
                    }
                }
                // Android 10
                Build.VERSION.SDK_INT == Build.VERSION_CODES.Q -> {
                    showToastAndLog("Android 10 不支持FileAPI访问")
                }
                // Android 9及以下
                else -> {
                    if (suspendRequestStoragePermission()) {
                        val result = doDeleteImageByFileApi(fileName)
                        if (result) {
                            showToastAndLog("Android9及以下通过FileApi删除图片成功")
                        } else {
                            showToastAndLog("Android9及以下通过FileApi删除图片失败")
                        }
                    } else {
                        showToastAndLog("存储权限拒绝")
                    }
                }
            }
        }
    }

    /**
     * 通过FileApi删除系统相册中照片
     */
    private suspend fun doDeleteImageByFileApi(fileName: String): Boolean {
        return withContext(Dispatchers.IO) {
            val path =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path + "/" + fileName
            val file = File(path)
            return@withContext if (file.exists()) {
                file.delete()
            } else {
                true
            }
        }
    }

    /**
     * 弹出吐司并打印Log
     */
    private fun showToastAndLog(msg: String) {
        Log.d(TAG, msg)
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}