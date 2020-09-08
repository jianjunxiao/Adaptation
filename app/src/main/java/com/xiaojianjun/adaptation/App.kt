package com.xiaojianjun.adaptation

import android.app.AppOpsManager
import android.app.Application
import android.app.AsyncNotedAppOp
import android.app.SyncNotedAppOp
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            initAppOps()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun initAppOps() {
        val appOpsCallbacks = object : AppOpsManager.OnOpNotedCallback() {
            override fun onNoted(syncNotedAppOp: SyncNotedAppOp) {
                val op = syncNotedAppOp.op
                val attributionTag = syncNotedAppOp.attributionTag
                val stackTrace = Throwable().stackTraceToString()
                Log.d("数据访问审核", "onNoted----> op:$op, attributionTag:$attributionTag, stackTrace:$stackTrace")
            }

            override fun onSelfNoted(syncNotedAppOp: SyncNotedAppOp) {
                val op = syncNotedAppOp.op
                val attributionTag = syncNotedAppOp.attributionTag
                Log.d("数据访问审核", "onSelfNoted----> op:$op, attributionTag:$attributionTag")
            }

            override fun onAsyncNoted(asyncNotedAppOp: AsyncNotedAppOp) {
                val op = asyncNotedAppOp.op
                val attributionTag = asyncNotedAppOp.attributionTag
                val msg = asyncNotedAppOp.message
                Log.d("数据访问审核", "onAsyncNoted----> op:$op, attributionTag:$attributionTag, msg:$msg")
            }
        }
        val appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        appOpsManager.setOnOpNotedCallback(mainExecutor, appOpsCallbacks)
    }
}