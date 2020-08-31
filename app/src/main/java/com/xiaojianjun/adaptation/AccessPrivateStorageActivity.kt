package com.xiaojianjun.adaptation

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.xiaojianjun.adaptation.util.requestStoragePermission
import kotlinx.android.synthetic.main.activity_access_private_storage.*
import java.io.FileInputStream
import java.io.FileOutputStream

class AccessPrivateStorageActivity : AppCompatActivity() {

    companion object {
        const val TAG = "应用专属存储空间(私有存储)"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_access_private_storage)
        init()
    }

    private fun init() {
        // 访问专属存储空间的内部存储
        btnPrivateInternalStorage.setOnClickListener { accessPrivateInternalStorage() }
        // 访问专属存储空间的外部存储
        btnPrivateExternalStorage.setOnClickListener { accessPrivateExternalStorage() }
        // 访问其他App的专属存储空间的外部存储
        btnOtherAppPrivateExternalStorage.setOnClickListener { accessOtherAppPrivateExternalStorage() }
    }

    /**
     * 访问本应用的专属内部存储
     */
    private fun accessPrivateInternalStorage() {
        val file = "${this.filesDir.path}/test.txt"
        FileOutputStream(file).use { it.write("应用专属内部存储".toByteArray()) }
        FileInputStream(file).use { showToastAndLog(String(it.readBytes())) }
    }

    /**
     * 访问本应用的专属外部存储
     */
    private fun accessPrivateExternalStorage() {
        val file = "${this.getExternalFilesDir(null)?.path}/test.txt"
        FileOutputStream(file).use { it.write("应用专属外部存储".toByteArray()) }
        FileInputStream(file).use { showToastAndLog(String(it.readBytes())) }
    }

    /**
     * 访问其他应用的专属外部存储
     * TargetApi 29及以上不允许，会直接抛出异常崩溃。
     * TargetApi 28及以下需有存储权限才可以访问，无权限会抛异常崩溃。
     */
    private fun accessOtherAppPrivateExternalStorage() {
        // 如果应用TargetApi<29
        requestStoragePermission(
            onGranted = {
                // 读取com.demo.test这个应用的专属外部存储空间(提前准备好com.demo.test/files/test.txt文件)
                val dir = this.getExternalFilesDir(null)?.parentFile?.parentFile?.path
                val other = if (packageName == "com.ztzh.adaptation.a") {
                    "com.ztzh.adaptation.b"
                } else {
                    "com.ztzh.adaptation.a"
                }
                val filePath = "$dir/$other/files/test.txt"
                FileInputStream(filePath).use { showToastAndLog(String(it.readBytes())) }
            },
            onDenied = {
                showToastAndLog("存储权限拒绝")
            }
        )
    }

    /**
     * 弹出吐司并打印Log
     */
    private fun showToastAndLog(msg: String) {
        Log.d(TAG, msg)
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}