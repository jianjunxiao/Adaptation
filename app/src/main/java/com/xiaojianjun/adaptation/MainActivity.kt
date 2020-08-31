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
        // 访问共享存储MediaStore
        btnAccessMediaStore.setOnClickListener {
            startActivity(Intent(this, AccessMediaStoreActivity::class.java))
        }
        // 通过SAF访问共享存储空间
    }
}