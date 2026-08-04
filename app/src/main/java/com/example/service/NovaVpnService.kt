package com.example.service

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log

class NovaVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("NovaVpnService", "VpnService started")
        val action = intent?.action
        if (action == "CONNECT") {
            establishVpn()
        } else if (action == "DISCONNECT") {
            disconnectVpn()
        }
        return START_STICKY
    }

    private fun establishVpn() {
        try {
            if (vpnInterface != null) return

            val builder = Builder()
                .setSession("NovaVPN Session")
                .addAddress("10.0.0.2", 24)
                .addDnsServer("1.1.1.1")
                .addRoute("0.0.0.0", 0)

            vpnInterface = builder.establish()
            Log.d("NovaVpnService", "VPN established successfully")
        } catch (e: Exception) {
            Log.e("NovaVpnService", "Failed to establish VPN: ${e.message}", e)
        }
    }

    private fun disconnectVpn() {
        try {
            vpnInterface?.close()
            vpnInterface = null
            Log.d("NovaVpnService", "VPN disconnected")
            stopSelf()
        } catch (e: Exception) {
            Log.e("NovaVpnService", "Error disconnecting: ${e.message}", e)
        }
    }

    override fun onDestroy() {
        disconnectVpn()
        super.onDestroy()
    }
}
