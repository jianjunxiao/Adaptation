package com.xiaojianjun.adaptation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        // 访问应用私有存储
        btnAccessPrivateStorage.setOnClickListener {
            startActivity(Intent(this, AccessPrivateStorageActivity::class.java))
        }
        // 通过MediaStore访问共享存储的媒体文件
        btnAccessMediaStore.setOnClickListener {
            startActivity(Intent(this, AccessMediaStoreActivity::class.java))
        }
        // 通过SAF访问共享存储空间中的文档或其他文件
        btnAccessSAF.setOnClickListener {
            startActivity(Intent(this, SAFActivity::class.java))
        }
        // 位置(定位）
        btnLocation.setOnClickListener {
            startActivity(Intent(this, LocationActivity::class.java))
        }
        // 权限变更
        btnPermissionChange.setOnClickListener {
            startActivity(Intent(this, PermissionChangeActivity::class.java))
        }
    }
}