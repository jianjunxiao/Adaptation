package com.xiaojianjun.adaptation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.getSystemService
import com.xiaojianjun.adaptation.util.doRequestPermission
import kotlinx.android.synthetic.main.activity_permission_change.*

class PermissionChangeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission_change)

        // 单次权限
        btnSinglePermission.setOnClickListener {
            requestCameraPermission()
        }
        // 读取电话号码
        btnReadPhoneNumber.setOnClickListener {
            readPhoneNumber()
        }
    }

    /**
     * 请求相机权限
     */
    private fun requestCameraPermission() {
        doRequestPermission(Manifest.permission.CAMERA) {
            if (it) {
                showToastAndLog("获得相机权限")
            } else {
                showToastAndLog("拒绝了相机权限")
            }
        }
    }

    private fun readPhoneNumber() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            doRequestPermission(Manifest.permission.READ_PHONE_NUMBERS) {
                if (it) {
                    getPhoneNumber()
                } else {
                    showToastAndLog("权限拒绝")
                }
            }
        } else {
            doRequestPermission(Manifest.permission.READ_PHONE_STATE) {
                if (it) {
                    getPhoneNumber()
                } else {
                    showToastAndLog("权限拒绝")
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun getPhoneNumber() {
       val ac = createAttributionContext("电话号码权限变更")
        val telephonyManager = ac.getSystemService<TelephonyManager>()!!
        val line1Number = telephonyManager.line1Number
        showToastAndLog(line1Number)
//        telephonyManager.getMsisdn()
    }


    /**
     * 弹出吐司并打印Log
     */
    private fun showToastAndLog(msg: String) {
        Log.d(TAG, msg)
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val TAG = "权限变更"
    }
}