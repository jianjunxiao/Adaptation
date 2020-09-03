package com.xiaojianjun.adaptation.util

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun AppCompatActivity.launchActivityForResult(intent: Intent, callback: (ActivityResult) -> Unit) {
    var launcher: ActivityResultLauncher<Intent>? = null
    launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        callback.invoke(it)
        launcher?.unregister()
    }
    launcher.launch(intent)
}

suspend fun AppCompatActivity.suspendLaunchActivityForResult(intent: Intent): ActivityResult {
    return suspendCoroutine { continuation ->
        var launcher: ActivityResultLauncher<Intent>? = null
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            continuation.resume(it)
            launcher?.unregister()
        }
        launcher.launch(intent)
    }
}

fun AppCompatActivity.doRequestPermission(
    vararg permissions: String,
    callback: (Boolean) -> Unit
) {
    var launcher: ActivityResultLauncher<Array<out String>>? = null
    launcher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val allGranted = result.all { it.value == true }
            callback.invoke(allGranted)
            launcher?.unregister()
        }
    launcher.launch(permissions)
}

suspend fun AppCompatActivity.suspendRequestPermission(vararg permissions: String): Boolean {
    return suspendCoroutine { continuation ->
        var launcher: ActivityResultLauncher<Array<out String>>? = null
        launcher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
                val allGranted = result.all { it.value == true }
                continuation.resume(allGranted)
                launcher?.unregister()
            }
        launcher.launch(permissions)
    }
}

fun AppCompatActivity.launchSenderIntentForResult(
    intentSender: IntentSender,
    callback: (Boolean) -> Unit
) {
    var launcher: ActivityResultLauncher<IntentSenderRequest>? = null
    launcher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
        callback.invoke(it.resultCode == Activity.RESULT_OK)
        launcher?.unregister()
    }
    launcher.launch(
        IntentSenderRequest
            .Builder(intentSender)
            .build()
    )
}

suspend fun AppCompatActivity.suspendLaunchSenderIntentForResult(intentSender: IntentSender): Boolean {
    return suspendCoroutine { continuation ->
        var launcher: ActivityResultLauncher<IntentSenderRequest>? = null
        launcher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            continuation.resume(it.resultCode == Activity.RESULT_OK)
            launcher?.unregister()
        }
        launcher.launch(
            IntentSenderRequest
                .Builder(intentSender)
                .build()
        )
    }
}