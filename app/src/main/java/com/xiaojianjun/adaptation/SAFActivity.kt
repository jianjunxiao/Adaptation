package com.xiaojianjun.adaptation

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.net.UriCompat
import androidx.core.os.EnvironmentCompat
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import com.xiaojianjun.adaptation.util.launchActivityForResult
import com.xiaojianjun.adaptation.util.suspendLaunchActivityForResult
import kotlinx.android.synthetic.main.activity_saf.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SAFActivity : AppCompatActivity() {

    companion object {
        const val TAG = "分区存储(SAF)"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saf)
        title = getString(R.string.app_name) + " :   SAF"
        init()
        Environment.isExternalStorageManager()
    }

    private fun init() {
        // 创建文档
        btnCreateDocument.setOnClickListener {
            createNewDocument()
        }
        // 读取文档
        btnReadDocument.setOnClickListener {
            openReadDocument()
        }
        // 删除文档
        btnDeleteDocument.setOnClickListener {
            deleteDocument()
        }
        // 授予选定录内容的访问权限
        btnOpenDocumentTree.setOnClickListener {
            grantedDirAccessPermission()
        }
    }

    /**
     * 创建新文件
     */
    private fun createNewDocument() {
        lifecycleScope.launch {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/plain"
                putExtra(Intent.EXTRA_TITLE, "invoice.txt")
            }
            val activityResult = suspendLaunchActivityForResult(intent)
            val uri = activityResult.data?.data ?: return@launch
            val result = writeContentIntoDocumentUri(uri, "这是文档的内容")
            if (result) {
                showToastAndLog("写入文档内容成功")
            } else {
                showToastAndLog("写入文档内容失败")
            }
        }
    }

    /**
     * 向指定Uri的文档中写入内容
     */
    private suspend fun writeContentIntoDocumentUri(uri: Uri, content: String): Boolean {
        return withContext(Dispatchers.IO) {
            contentResolver.openOutputStream(uri)?.use {
                it.write(content.toByteArray())
                return@withContext true
            }
            return@withContext false
        }
    }

    /**
     * 打开文档
     */
    private fun openReadDocument() {
        lifecycleScope.launch {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/plain"
            }
            val activityResult = suspendLaunchActivityForResult(intent)
            val uri = activityResult.data?.data ?: return@launch
            val content = readContentFromDocumentUri(uri)
            if (content.isNullOrEmpty().not()) {
                showToastAndLog("读取文档内容成功：$content")
            } else {
                showToastAndLog("读取文档内容失败")
            }
        }
    }

    /**
     * 从文档Uri读取内容，读取失败则为null
     */
    private suspend fun readContentFromDocumentUri(uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            contentResolver.openInputStream(uri)?.use {
                return@withContext String(it.readBytes())
            }
            return@withContext null
        }
    }

    /**
     * 删除文件
     */
    private fun deleteDocument() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        launchActivityForResult(intent) {
            val uri = it.data?.data ?: return@launchActivityForResult
            val result = DocumentFile.fromSingleUri(this, uri)?.delete()
            if (result == true) {
                showToastAndLog("文档删除成功")
            } else {
                showToastAndLog("文档删除失败")
            }
        }
    }

    /**
     * 授予APP对某个目录内容的访问权限
     */
    private fun grantedDirAccessPermission() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        }
        launchActivityForResult(intent) {
            val uri = it.data?.data ?: return@launchActivityForResult
            val urlStr = UriCompat.toSafeString(uri)
            showToastAndLog(urlStr)
            // 保留权限（可选）
            takePersistableUriPermission(uri)
            // 后续操作，借助androidx DocumentFile库方便对目录及内容进行读、写、编辑、删除等操作
            val documentFile = DocumentFile.fromTreeUri(this@SAFActivity, uri)
            val documentFiles = documentFile?.listFiles()
            documentFiles?.forEach {
                val childUri = it.uri
                // 子文件读、写、删除，或者创建新的目录、新的文件
            }
        }
    }

    /**
     * 保留权限
     */
    private fun takePersistableUriPermission(uri: Uri) {
        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        contentResolver.takePersistableUriPermission(uri, takeFlags)
    }


    /**
     * 弹出吐司并打印Log
     */
    private fun showToastAndLog(msg: String) {
        Log.d(TAG, msg)
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}