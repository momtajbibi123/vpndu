package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vpn_servers")
data class VpnServer(
    @PrimaryKey val serverId: String,
    val name: String,
    val country: String,
    val city: String,
    val ip: String,
    val load: Int, // 0-100%
    val ping: Int, // ms
    val isPremium: Boolean,
    val protocol: String = "WireGuard",
    val isFavorite: Boolean = false
)

@Entity(tableName = "user_sessions")
data class UserSession(
    @PrimaryKey val uid: String = "local_user",
    val email: String = "guest@novavpn.com",
    val plan: String = "FREE", // "FREE" or "PREMIUM"
    val planExpiry: Long = 0L,
    val deviceIds: String = "Device-1",
    val dataUsedToday: Long = 120 * 1024 * 1024L, // in bytes
    val dataLimit: Long = 500 * 1024 * 1024L, // 500MB free cap
    val referralCode: String = "NOVA_5X89",
    val isAdBlockEnabled: Boolean = false,
    val isKillSwitchEnabled: Boolean = false,
    val isSplitTunnelEnabled: Boolean = false,
    val autoConnectWifi: Boolean = false,
    val preferredProtocol: String = "WireGuard"
)

@Entity(tableName = "usage_logs")
data class UsageLog(
    @PrimaryKey val date: String, // e.g. "2026-08-03"
    val dataUsedMB: Float,
    val connectedMinutes: Int,
    val serversUsedCount: Int
)
