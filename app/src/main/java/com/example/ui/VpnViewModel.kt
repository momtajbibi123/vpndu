package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.service.NovaVpnService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class VpnViewModel(application: Application) : AndroidViewModel(application) {

    private val db = VpnDatabase.getDatabase(application)
    private val repository = VpnRepository(db.vpnDao())

    // UI States
    val allServers: StateFlow<List<VpnServer>> = repository.allServers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userSession: StateFlow<UserSession?> = repository.userSession
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val usageLogs: StateFlow<List<UsageLog>> = repository.usageLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Connection States
    private val _connectionState = MutableStateFlow("DISCONNECTED") // DISCONNECTED, CONNECTING, CONNECTED
    val connectionState: StateFlow<String> = _connectionState.asStateFlow()

    private val _activeServer = MutableStateFlow<VpnServer?>(null)
    val activeServer: StateFlow<VpnServer?> = _activeServer.asStateFlow()

    private val _connectionTime = MutableStateFlow(0L) // in seconds
    val connectionTime: StateFlow<Long> = _connectionTime.asStateFlow()

    private val _downloadSpeed = MutableStateFlow(0L) // bytes/sec
    val downloadSpeed: StateFlow<Long> = _downloadSpeed.asStateFlow()

    private val _uploadSpeed = MutableStateFlow(0L) // bytes/sec
    val uploadSpeed: StateFlow<Long> = _uploadSpeed.asStateFlow()

    // Screen navigation helpers
    private val _onboardingCompleted = MutableStateFlow(false)
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    private val _isUserLoggedIn = MutableStateFlow(false)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    // Admin Panel States
    private val _adminUsersList = MutableStateFlow<List<UserSession>>(emptyList())
    val adminUsersList: StateFlow<List<UserSession>> = _adminUsersList.asStateFlow()

    private var timerJob: Job? = null
    private var speedJob: Job? = null

    // Search & Filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("ALL") // "ALL", "FREE", "PREMIUM", "FAVORITE"
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    init {
        viewModelScope.launch {
            repository.populateInitialData()
            // Set first available free server as default active
            val servers = repository.allServers.first()
            _activeServer.value = servers.firstOrNull { !it.isPremium } ?: servers.firstOrNull()

            // Firebase Current User state checking on startup
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    _isUserLoggedIn.value = true
                    fetchSessionFromFirestore(currentUser.uid)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun toggleFavorite(server: VpnServer) {
        viewModelScope.launch {
            repository.toggleFavorite(server.serverId, !server.isFavorite)
        }
    }

    fun selectServer(server: VpnServer) {
        val session = userSession.value ?: return
        if (server.isPremium && session.plan != "PREMIUM") {
            // Screen will handle navigation to Paywall
            return
        }
        _activeServer.value = server
        if (_connectionState.value == "CONNECTED") {
            // Reconnect to new server
            disconnectVpn()
            connectVpn()
        }
    }

    fun connectVpn() {
        if (_connectionState.value != "DISCONNECTED") return

        viewModelScope.launch {
            val session = userSession.value ?: return@launch
            val server = _activeServer.value ?: return@launch

            // Check Free Tier Data Cap
            if (session.plan == "FREE" && session.dataUsedToday >= session.dataLimit) {
                // Free limit exceeded
                return@launch
            }

            _connectionState.value = "CONNECTING"
            delay(1500) // Aesthetic delay for progress ring

            // Start native service
            val context = getApplication<Application>().applicationContext
            val intent = Intent(context, NovaVpnService::class.java).apply {
                action = "CONNECT"
            }
            context.startService(intent)

            _connectionState.value = "CONNECTED"

            // Start counters
            startStatsJobs()
        }
    }

    fun disconnectVpn() {
        if (_connectionState.value != "CONNECTED" && _connectionState.value != "CONNECTING") return

        viewModelScope.launch {
            _connectionState.value = "DISCONNECTED"
            stopStatsJobs()

            // Stop native service
            val context = getApplication<Application>().applicationContext
            val intent = Intent(context, NovaVpnService::class.java).apply {
                action = "DISCONNECT"
            }
            context.startService(intent)

            // Update local DB logs
            val session = userSession.value
            if (session != null) {
                val dataIncrement = (_connectionTime.value * (1.5 * 1024 * 1024)).toLong() // Simulated 1.5MB per sec
                val newUsage = (session.dataUsedToday + dataIncrement).coerceAtMost(
                    if (session.plan == "PREMIUM") Long.MAX_VALUE else session.dataLimit
                )
                val updatedSession = session.copy(dataUsedToday = newUsage)
                repository.saveSession(updatedSession)
                syncSessionToFirestore(updatedSession)

                // Save usage statistics
                val date = "Aug 03" // Standardize today key
                val incrementMB = (dataIncrement / (1024 * 1024).toFloat())
                val minsUsed = (_connectionTime.value / 60).toInt().coerceAtLeast(1)
                repository.saveUsageLog(
                    UsageLog(
                        date = date,
                        dataUsedMB = 120.0f + incrementMB,
                        connectedMinutes = 40 + minsUsed,
                        serversUsedCount = 1
                    )
                )
            }
            _connectionTime.value = 0L
            _downloadSpeed.value = 0L
            _uploadSpeed.value = 0L
        }
    }

    private fun startStatsJobs() {
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _connectionTime.value += 1
                
                // Simulated consumption
                val session = userSession.value
                if (session != null && session.plan == "FREE") {
                    val increment = 250 * 1024L // 250KB per second
                    val updatedUsed = (session.dataUsedToday + increment).coerceAtMost(session.dataLimit)
                    val updatedSession = session.copy(dataUsedToday = updatedUsed)
                    repository.saveSession(updatedSession)
                    // Sync periodically
                    if (_connectionTime.value % 10 == 0L) {
                        syncSessionToFirestore(updatedSession)
                    }
                    if (updatedUsed >= session.dataLimit) {
                        disconnectVpn()
                        break
                    }
                }
            }
        }

        speedJob = viewModelScope.launch {
            val session = userSession.value
            while (true) {
                val isPremium = session?.plan == "PREMIUM"
                val baseDown = if (isPremium) 25_000_000L else 4_500_000L // Premium is much faster
                val baseUp = if (isPremium) 12_000_000L else 1_800_000L

                _downloadSpeed.value = baseDown + Random.nextLong(-2_000_000L, 2_000_000L)
                _uploadSpeed.value = baseUp + Random.nextLong(-800_000L, 800_000L)
                delay(2000)
            }
        }
    }

    private fun stopStatsJobs() {
        timerJob?.cancel()
        speedJob?.cancel()
        timerJob = null
        speedJob = null
    }

    // Interactive simulator actions
    fun upgradeToPremium() {
        viewModelScope.launch {
            val session = userSession.value ?: return@launch
            val updated = session.copy(
                plan = "PREMIUM",
                dataLimit = Long.MAX_VALUE, // Unlimited
                planExpiry = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000L) // 30 Days
            )
            repository.saveSession(updated)
            syncSessionToFirestore(updated)
        }
    }

    fun downgradeToFree() {
        viewModelScope.launch {
            val session = userSession.value ?: return@launch
            val updated = session.copy(
                plan = "FREE",
                dataLimit = 500 * 1024 * 1024L,
                dataUsedToday = 120 * 1024 * 1024L,
                planExpiry = 0L
            )
            repository.saveSession(updated)
            syncSessionToFirestore(updated)
        }
    }

    fun addBonusData() {
        viewModelScope.launch {
            val session = userSession.value ?: return@launch
            if (session.plan == "FREE") {
                val currentLimit = session.dataLimit
                val newLimit = currentLimit + (200 * 1024 * 1024L) // +200MB
                val updated = session.copy(dataLimit = newLimit)
                repository.saveSession(updated)
                syncSessionToFirestore(updated)
            }
        }
    }

    fun completeOnboarding() {
        _onboardingCompleted.value = true
    }

    // Firebase Auth login/signup functions
    fun signUpWithEmailAndPassword(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { authResult ->
                        val firebaseUser = authResult.user
                        if (firebaseUser != null) {
                            val newSession = UserSession(
                                uid = firebaseUser.uid,
                                email = email,
                                plan = "FREE",
                                dataLimit = 500 * 1024 * 1024L,
                                dataUsedToday = 0L,
                                referralCode = "NOVA_" + Random.nextInt(1000, 9999)
                            )
                            viewModelScope.launch {
                                repository.saveSession(newSession)
                                syncSessionToFirestore(newSession)
                                _isUserLoggedIn.value = true
                                onSuccess()
                            }
                        } else {
                            onError("User creation failed.")
                        }
                    }
                    .addOnFailureListener { exception ->
                        onError(exception.localizedMessage ?: "Sign up failed.")
                    }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Unknown error occurred.")
            }
        }
    }

    fun loginWithEmailAndPassword(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener { authResult ->
                        val firebaseUser = authResult.user
                        if (firebaseUser != null) {
                            _isUserLoggedIn.value = true
                            fetchSessionFromFirestore(firebaseUser.uid, onComplete = {
                                onSuccess()
                            })
                        } else {
                            onError("User authentication failed.")
                        }
                    }
                    .addOnFailureListener { exception ->
                        onError(exception.localizedMessage ?: "Sign in failed.")
                    }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Unknown error occurred.")
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                FirebaseAuth.getInstance().signOut()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _isUserLoggedIn.value = false
            repository.saveSession(UserSession()) // Reset local to default guest template
            onComplete()
        }
    }

    // Firestore Sync Helpers
    fun syncSessionToFirestore(session: UserSession) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        try {
            val db = FirebaseFirestore.getInstance()
            val data = hashMapOf(
                "uid" to currentUser.uid,
                "email" to session.email,
                "plan" to session.plan,
                "planExpiry" to session.planExpiry,
                "deviceIds" to session.deviceIds,
                "dataUsedToday" to session.dataUsedToday,
                "dataLimit" to session.dataLimit,
                "referralCode" to session.referralCode,
                "isAdBlockEnabled" to session.isAdBlockEnabled,
                "isKillSwitchEnabled" to session.isKillSwitchEnabled,
                "isSplitTunnelEnabled" to session.isSplitTunnelEnabled,
                "autoConnectWifi" to session.autoConnectWifi,
                "preferredProtocol" to session.preferredProtocol
            )
            db.collection("users").document(currentUser.uid)
                .set(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun fetchSessionFromFirestore(uid: String, onComplete: () -> Unit = {}) {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val session = UserSession(
                            uid = uid,
                            email = document.getString("email") ?: "guest@novavpn.com",
                            plan = document.getString("plan") ?: "FREE",
                            planExpiry = document.getLong("planExpiry") ?: 0L,
                            deviceIds = document.getString("deviceIds") ?: "Device-1",
                            dataUsedToday = document.getLong("dataUsedToday") ?: 0L,
                            dataLimit = document.getLong("dataLimit") ?: 500 * 1024 * 1024L,
                            referralCode = document.getString("referralCode") ?: "NOVA_5X89",
                            isAdBlockEnabled = document.getBoolean("isAdBlockEnabled") ?: false,
                            isKillSwitchEnabled = document.getBoolean("isKillSwitchEnabled") ?: false,
                            isSplitTunnelEnabled = document.getBoolean("isSplitTunnelEnabled") ?: false,
                            autoConnectWifi = document.getBoolean("autoConnectWifi") ?: false,
                            preferredProtocol = document.getString("preferredProtocol") ?: "WireGuard"
                        )
                        viewModelScope.launch {
                            repository.saveSession(session)
                            onComplete()
                        }
                    } else {
                        val currentEmail = FirebaseAuth.getInstance().currentUser?.email ?: "guest@novavpn.com"
                        val newSession = UserSession(
                            uid = uid,
                            email = currentEmail,
                            plan = "FREE",
                            dataLimit = 500 * 1024 * 1024L,
                            dataUsedToday = 0L,
                            referralCode = "NOVA_" + Random.nextInt(1000, 9999)
                        )
                        viewModelScope.launch {
                            repository.saveSession(newSession)
                            syncSessionToFirestore(newSession)
                            onComplete()
                        }
                    }
                }
                .addOnFailureListener {
                    onComplete()
                }
        } catch (e: Exception) {
            e.printStackTrace()
            onComplete()
        }
    }

    // Admin Panel fetch
    fun fetchAllUsersForAdmin() {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("users")
                .get()
                .addOnSuccessListener { result ->
                    val list = mutableListOf<UserSession>()
                    for (document in result) {
                        try {
                            val uid = document.getString("uid") ?: document.id
                            val email = document.getString("email") ?: ""
                            val plan = document.getString("plan") ?: "FREE"
                            val planExpiry = document.getLong("planExpiry") ?: 0L
                            val deviceIds = document.getString("deviceIds") ?: ""
                            val dataUsedToday = document.getLong("dataUsedToday") ?: 0L
                            val dataLimit = document.getLong("dataLimit") ?: 500 * 1024 * 1024L
                            val referralCode = document.getString("referralCode") ?: ""
                            val isAdBlockEnabled = document.getBoolean("isAdBlockEnabled") ?: false
                            val isKillSwitchEnabled = document.getBoolean("isKillSwitchEnabled") ?: false
                            val isSplitTunnelEnabled = document.getBoolean("isSplitTunnelEnabled") ?: false
                            val autoConnectWifi = document.getBoolean("autoConnectWifi") ?: false
                            val preferredProtocol = document.getString("preferredProtocol") ?: "WireGuard"

                            list.add(
                                UserSession(
                                    uid = uid,
                                    email = email,
                                    plan = plan,
                                    planExpiry = planExpiry,
                                    deviceIds = deviceIds,
                                    dataUsedToday = dataUsedToday,
                                    dataLimit = dataLimit,
                                    referralCode = referralCode,
                                    isAdBlockEnabled = isAdBlockEnabled,
                                    isKillSwitchEnabled = isKillSwitchEnabled,
                                    isSplitTunnelEnabled = isSplitTunnelEnabled,
                                    autoConnectWifi = autoConnectWifi,
                                    preferredProtocol = preferredProtocol
                                )
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    _adminUsersList.value = list
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateSettings(
        killSwitch: Boolean? = null,
        adBlock: Boolean? = null,
        splitTunnel: Boolean? = null,
        autoConnect: Boolean? = null,
        protocol: String? = null
    ) {
        viewModelScope.launch {
            val session = userSession.value ?: return@launch
            val updated = session.copy(
                isKillSwitchEnabled = killSwitch ?: session.isKillSwitchEnabled,
                isAdBlockEnabled = adBlock ?: session.isAdBlockEnabled,
                isSplitTunnelEnabled = splitTunnel ?: session.isSplitTunnelEnabled,
                autoConnectWifi = autoConnect ?: session.autoConnectWifi,
                preferredProtocol = protocol ?: session.preferredProtocol
            )
            repository.saveSession(updated)
            syncSessionToFirestore(updated)
        }
    }

    override fun onCleared() {
        stopStatsJobs()
        super.onCleared()
    }
}
