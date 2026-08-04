package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VpnDao {
    // Servers
    @Query("SELECT * FROM vpn_servers ORDER BY isPremium ASC, country ASC, name ASC")
    fun getAllServers(): Flow<List<VpnServer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServers(servers: List<VpnServer>)

    @Update
    suspend fun updateServer(server: VpnServer)

    @Query("UPDATE vpn_servers SET isFavorite = :isFav WHERE serverId = :id")
    suspend fun toggleFavorite(id: String, isFav: Boolean)

    // User Session
    @Query("SELECT * FROM user_sessions WHERE uid = :uid LIMIT 1")
    fun getUserSession(uid: String = "local_user"): Flow<UserSession?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserSession(session: UserSession)

    // Usage Logs
    @Query("SELECT * FROM usage_logs ORDER BY date DESC LIMIT 7")
    fun getRecentUsageLogs(): Flow<List<UsageLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUsageLog(log: UsageLog)
}
