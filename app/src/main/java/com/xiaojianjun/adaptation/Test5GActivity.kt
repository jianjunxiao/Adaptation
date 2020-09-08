package com.xiaojianjun.adaptation

import android.Manifest
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.NET_CAPABILITY_NOT_METERED
import android.net.NetworkCapabilities.NET_CAPABILITY_TEMPORARILY_NOT_METERED
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.PhoneStateListener
import android.telephony.PhoneStateListener.LISTEN_DISPLAY_INFO_CHANGED
import android.telephony.TelephonyDisplayInfo
import android.telephony.TelephonyDisplayInfo.*
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import androidx.lifecycle.lifecycleScope
import com.xiaojianjun.adaptation.util.suspendRequestPermission
import com.xiaojianjun.adaptation.util.suspendRequestStoragePermission
import kotlinx.android.synthetic.main.activity_5_g.*
import kotlinx.coroutines.launch

class Test5GActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_5_g)

        btnMeteredness.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                check5GMeteredness()
            }
        }
        btnCheckConnect5G.setOnClickListener {
            check5GConnection()
        }

        btnBandwidth.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                checkBandwidth()
            }
        }
    }

    /**
     * 检查是否按流量计费
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun check5GMeteredness() {
        val connectivityManager = getSystemService<ConnectivityManager>()
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                val notMeteredness =
                    networkCapabilities.hasCapability(NET_CAPABILITY_NOT_METERED) ||
                            networkCapabilities.hasCapability(NET_CAPABILITY_TEMPORARILY_NOT_METERED)
                val meteredness = notMeteredness.not()
                Toast.makeText(this@Test5GActivity, "5G是否按流量计费:$meteredness", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        connectivityManager?.registerDefaultNetworkCallback(networkCallback)
    }

    /**
     * 检查用户是否连接到了5G网络
     */
    private fun check5GConnection() {
        lifecycleScope.launch {
            if (suspendRequestPermission(Manifest.permission.READ_PHONE_STATE)) {
                val telephonyManager = getSystemService<TelephonyManager>()!!
                val phoneStateListener = object : PhoneStateListener() {
                    override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
                        val typeStr = when (telephonyDisplayInfo.overrideNetworkType) {
                            OVERRIDE_NETWORK_TYPE_LTE_ADVANCED_PRO -> "高级专业版 LTE (5Ge)"
                            OVERRIDE_NETWORK_TYPE_NR_NSA -> "NR (5G) - 5G Sub-6 网络"
                            OVERRIDE_NETWORK_TYPE_NR_NSA_MMWAVE -> "5G+/5G UW - 5G mmWave 网络"
                            OVERRIDE_NETWORK_TYPE_LTE_CA -> "其他"
                            OVERRIDE_NETWORK_TYPE_NONE -> "没有连接到5G网络"
                            else -> ""
                        }
                        Toast.makeText(this@Test5GActivity, typeStr, Toast.LENGTH_SHORT).show()
                    }
                }

                telephonyManager.listen(phoneStateListener, LISTEN_DISPLAY_INFO_CHANGED)
            }
        }
    }

    /**
     * 检测带宽
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun checkBandwidth() {
        val connectivityManager = getSystemService<ConnectivityManager>()
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                val bandwidthDown = networkCapabilities.linkDownstreamBandwidthKbps
                val bandwidthUp = networkCapabilities.linkUpstreamBandwidthKbps
                Toast.makeText(
                    this@Test5GActivity,
                    "上行带宽:$bandwidthUp kbps，下行带宽:$bandwidthDown kbps",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
        connectivityManager?.registerDefaultNetworkCallback(networkCallback)
    }
}