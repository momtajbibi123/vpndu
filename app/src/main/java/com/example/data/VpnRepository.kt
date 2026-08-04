package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class VpnRepository(private val vpnDao: VpnDao) {

    val allServers: Flow<List<VpnServer>> = vpnDao.getAllServers()
    val userSession: Flow<UserSession?> = vpnDao.getUserSession()
    val usageLogs: Flow<List<UsageLog>> = vpnDao.getRecentUsageLogs()

    suspend fun saveSession(session: UserSession) {
        vpnDao.saveUserSession(session)
    }

    suspend fun toggleFavorite(id: String, isFav: Boolean) {
        vpnDao.toggleFavorite(id, isFav)
    }

    suspend fun saveUsageLog(log: UsageLog) {
        vpnDao.saveUsageLog(log)
    }

    suspend fun populateInitialData() {
        // Pre-populate servers if empty
        val existingServers = vpnDao.getAllServers().firstOrNull() ?: emptyList()
        if (existingServers.isEmpty()) {
            val servers = listOf(
                // Free Servers
                VpnServer("us_free", "United States", "USA", "New York", "192.168.1.1", 42, 45, false),
                VpnServer("uk_free", "United Kingdom", "UK", "London", "192.168.1.2", 31, 72, false),
                VpnServer("sg_free", "Singapore", "Singapore", "Marina Bay", "192.168.1.3", 56, 110, false),

                // Premium Servers
                VpnServer("jp_prem", "Japan", "Japan", "Tokyo", "203.0.113.1", 15, 12, true),
                VpnServer("de_prem", "Germany", "Germany", "Frankfurt", "203.0.113.2", 22, 18, true),
                VpnServer("us_prem", "USA - Silicon Valley", "USA", "San Jose", "203.0.113.3", 19, 25, true),
                VpnServer("ca_prem", "Canada", "Canada", "Toronto", "203.0.113.4", 28, 32, true),
                VpnServer("au_prem", "Australia", "Australia", "Sydney", "203.0.113.5", 12, 68, true),
                VpnServer("fr_prem", "France", "France", "Paris", "203.0.113.6", 35, 24, true),
                VpnServer("in_prem", "India", "India", "Mumbai", "203.0.113.7", 51, 42, true),
                VpnServer("br_prem", "Brazil", "Brazil", "Sao Paulo", "203.0.113.8", 44, 98, true)
            )
            vpnDao.insertServers(servers)
        }

        // Pre-populate session if empty
        val existingSession = vpnDao.getUserSession().firstOrNull()
        if (existingSession == null) {
            vpnDao.saveUserSession(UserSession())
        }

        // Pre-populate 7 days of usage logs if empty
        val existingLogs = vpnDao.getRecentUsageLogs().firstOrNull() ?: emptyList()
        if (existingLogs.isEmpty()) {
            val logs = listOf(
                UsageLog("Jul 28", 120.5f, 45, 1),
                UsageLog("Jul 29", 350.2f, 110, 2),
                UsageLog("Jul 30", 98.4f, 30, 1),
                UsageLog("Jul 31", 450.1f, 180, 3),
                UsageLog("Aug 01", 210.0f, 75, 1),
                UsageLog("Aug 02", 520.3f, 220, 2),
                UsageLog("Aug 03", 120.0f, 40, 1) // Today
            )
            for (log in logs) {
                vpnDao.saveUsageLog(log)
            }
        }
    }
}
