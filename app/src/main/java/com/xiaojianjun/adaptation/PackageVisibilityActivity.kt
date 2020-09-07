package com.xiaojianjun.adaptation

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION
import kotlinx.android.synthetic.main.activity_package_visibility.*

class PackageVisibilityActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_package_visibility)

        init()
    }

    private fun init() {
        // 浏览器应用打开百度
        btnOpenBaiduByBrowserApps.setOnClickListener {
            openBaiduByBrowserApps()
        }
        // 查询浏览器应用的包名
        btnQueryAvailableBrowserApps.setOnClickListener {
            queryBrowserAppPackageNames()
        }
        // 在自定义标签页中打开百度
        btnOpenBaiduByCustomTabsClient.setOnClickListener {
            openBaiduByCustomTabsClient()
        }
        // 查看支持自定义标签页的应用包名(queries配置和不配置两种情况)
        btnQueryAvailableCustomTabs.setOnClickListener {
            queryAvailableCustomTabs()
        }
    }


    /**
     * 浏览器应用打开百度
     * 不管是否配置queries，都能通过系统浏览器打开
     */
    private fun openBaiduByBrowserApps() {
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            addCategory(Intent.CATEGORY_BROWSABLE)
            data = Uri.parse("https://www.baidu.com/")
            flags = FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    /**
     * 先不在清单文件中配置queries，查询浏览器应用的包名，结果只能查询本应用
     * 然后在清单文件中配置好queries在查询浏览器应用的包名，结果能查询到所有配置的浏览器应用
     * 可以通过此方式检查是否有可用的浏览器应用
     */
    private fun queryBrowserAppPackageNames() {
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            addCategory(Intent.CATEGORY_BROWSABLE)
            data = Uri.parse("https://www.baidu.com/")
            flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_REQUIRE_NON_BROWSER
        }
        packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL).forEach {
            val targetPackageName = it.activityInfo.packageName
            Log.d("软件包可见性", "包名：$targetPackageName")
        }
    }

    /**
     * 在自定义标签页中打开百度
     * 不管是否配置queries，都能在默认的自定义标签页中打开百度
     */
    private fun openBaiduByCustomTabsClient() {
        CustomTabsIntent.Builder()
            .addDefaultShareMenuItem()
            .build()
            .launchUrl(this, Uri.parse("https://www.baidu.com/"))

    }

    private fun queryAvailableCustomTabs() {
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            addCategory(Intent.CATEGORY_BROWSABLE)
            data = Uri.parse("https://www.baidu.com/")
            flags = FLAG_ACTIVITY_NEW_TASK
        }
        packageManager.queryIntentActivities(intent, 0).forEach {
            val serviceIntent = Intent().apply {
                action = ACTION_CUSTOM_TABS_CONNECTION
                `package` = it.activityInfo.packageName
            }
            val supported = packageManager.resolveService(serviceIntent, 0) != null
            if (supported) {
                Log.d("软件包可见性", "支持自定义标签的应用包名：${it.activityInfo.packageName}")
            }
        }
    }
}