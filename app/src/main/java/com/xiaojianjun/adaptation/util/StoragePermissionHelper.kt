package com.xiaojianjun.adaptation.util

import android.Manifest
import androidx.appcompat.app.AppCompatActivity

/**
 * Created by xiaojianjun on 2020/8/30.
 */
/**
 * AppCompatActivity扩展方法，请求存储权限
 * @param onGranted 同意
 * @param onDenied 拒绝
 */
fun AppCompatActivity.requestStoragePermission(onGranted: () -> Unit, onDenied: () -> Unit) {
    doRequestPermission(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    ) {
        if (it) {
            onGranted()
        } else {
            onDenied()
        }
    }
}

/**
 * AppCompatActivity扩展方法，协程中请求存储权限
 * @return true-同意，false-拒绝
 */
suspend fun AppCompatActivity.suspendRequestStoragePermission(): Boolean {
    return suspendRequestPermission(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
}

