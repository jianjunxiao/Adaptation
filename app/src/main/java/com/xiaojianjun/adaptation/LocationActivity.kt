package com.xiaojianjun.adaptation

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.pm.PermissionInfoCompat
import androidx.lifecycle.lifecycleScope
import com.xiaojianjun.adaptation.util.suspendRequestPermission
import kotlinx.android.synthetic.main.activity_location.*
import kotlinx.coroutines.launch

class LocationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        init()
    }

    private fun init() {
        btnForegroundLocation.setOnClickListener {
            requestForegroundLocationPermission()
        }
        btnBackgroundLocation.setOnClickListener {
            requestBackgroundLocationPermission()
        }
    }

    /**
     * 获取前台位置权限
     */
    private fun requestForegroundLocationPermission() {
        lifecycleScope.launch {
            val granted = suspendRequestPermission(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
            if (granted) {
                showToastAndLog("获得前台位置权限")
            } else {
                showToastAndLog("拒绝前台位置权限")
            }
        }
    }

    /**
     * 先后获取前后台位置权限
     */
    private fun requestBackgroundLocationPermission() {
        lifecycleScope.launch {
            // 先获取前台位置权限
            val foregroundGranted = suspendRequestPermission(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (!foregroundGranted) {
                showToastAndLog("拒绝了前台位置权限")
                return@launch
            }
            // 有了前台位置权限，在获取后台位置权限
            val backgroundGranted = suspendRequestPermission(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
            if (backgroundGranted) {
                showToastAndLog("获得后台位置权限")
            } else {
                showToastAndLog("拒绝后台位置权限")
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

    companion object {
        const val TAG = "分区存储(SAF)"
    }
}