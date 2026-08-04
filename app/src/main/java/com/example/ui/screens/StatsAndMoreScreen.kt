package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UsageLog
import com.example.ui.VpnViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsAndMoreScreen(
    viewModel: VpnViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPaywall: () -> Unit,
    onLogoutSuccess: () -> Unit
) {
    val session by viewModel.userSession.collectAsState()
    val usageLogs by viewModel.usageLogs.collectAsState()
    val isAdmin = session?.email?.contains("admin", ignoreCase = true) == true || session?.email == "samim8gk@gmail.com"
    var activeTab by remember { mutableStateOf("STATS") } // STATS, PROFILE, SUPPORT, ADMIN

    val tabs = remember(isAdmin) {
        val list = mutableListOf("STATS", "PROFILE", "SUPPORT")
        if (isAdmin) list.add("ADMIN")
        list
    }

    Scaffold(
        containerColor = DarkBase,
        topBar = {
            TopAppBar(
                title = { Text("Stats & More", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("stats_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            // Main Top Tabs
            TabRow(
                selectedTabIndex = tabs.indexOf(activeTab).coerceAtLeast(0),
                containerColor = Color.Transparent,
                contentColor = ElectricBlue,
                divider = { Divider(color = BorderDark, thickness = 1.dp) }
            ) {
                tabs.forEach { tab ->
                    Tab(
                        selected = activeTab == tab,
                        onClick = { activeTab = tab },
                        modifier = Modifier.testTag("tab_${tab.lowercase()}")
                    ) {
                        Text(
                            text = tab,
                            color = if (activeTab == tab) Color.White else TextMuted,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic Tab Contents
            Box(modifier = Modifier.weight(1f)) {
                when (activeTab) {
                    "STATS" -> StatsTabContent(viewModel, usageLogs, session?.plan == "FREE", onNavigateToPaywall)
                    "PROFILE" -> ProfileTabContent(viewModel, session, onNavigateToPaywall, onLogoutSuccess)
                    "SUPPORT" -> SupportTabContent()
                    "ADMIN" -> AdminTabContent(viewModel)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STATS TAB: Custom Usage Charts + Rewarded Ad for +200MB
// -------------------------------------------------------------
@Composable
fun StatsTabContent(
    viewModel: VpnViewModel,
    usageLogs: List<UsageLog>,
    isFreeUser: Boolean,
    onNavigateToPaywall: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isAdPlaying by remember { mutableStateOf(false) }
    var showBonusEarnedDialog by remember { mutableStateOf(false) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Daily Data usage simulated charts/bars
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, BorderDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Data Bandwidth Chart (Weekly)",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Simulated bar drawings
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        usageLogs.forEach { log ->
                            // Normalize bar height
                            val maxMB = 600f
                            val normalizedHeight = (log.dataUsedMB / maxMB).coerceIn(0.1f, 1f)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "${log.dataUsedMB.toInt()}MB",
                                    fontSize = 9.sp,
                                    color = ElectricBlue,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .fillMaxHeight(normalizedHeight)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(ElectricBlue, NeonPurple)
                                            )
                                        )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = log.date, fontSize = 10.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }
        }

        // REWARDED AD BONUS SECTION (Free Users Only)
        if (isFreeUser) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    border = BorderStroke(1.dp, BorderDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.PlayCircleFilled, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Need more bandwidth?",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Watch a short rewarded ad to instantly get an extra +200MB bonus data limit reset!",
                            color = TextMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Button(
                            onClick = {
                                isAdPlaying = true
                                coroutineScope.launch {
                                    delay(4000) // Simulate ad duration
                                    isAdPlaying = false
                                    viewModel.addBonusData()
                                    showBonusEarnedDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("watch_rewarded_ad"),
                            enabled = !isAdPlaying
                        ) {
                            if (isAdPlaying) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Watching ad (0:04)...", color = Color.White)
                                }
                            } else {
                                Text("Watch Ad for +200MB", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Ads dialog
        item {
            if (showBonusEarnedDialog) {
                AlertDialog(
                    onDismissRequest = { showBonusEarnedDialog = false },
                    confirmButton = {
                        TextButton(onClick = { showBonusEarnedDialog = false }) {
                            Text("Awesome!", color = ConnectedGreen)
                        }
                    },
                    title = { Text("Reward Earned! 🎁", color = Color.White) },
                    text = { Text("You successfully watched the video! Extra 200MB safe bandwidth has been added to your daily allowance.", color = TextMuted) },
                    containerColor = CardDark
                )
            }
        }
    }
}

// -------------------------------------------------------------
// PROFILE TAB: Plan active badge, connected devices, referrals
// -------------------------------------------------------------
@Composable
fun ProfileTabContent(
    viewModel: VpnViewModel,
    session: com.example.data.UserSession?,
    onNavigateToPaywall: () -> Unit,
    onLogoutSuccess: () -> Unit
) {
    val isPremium = session?.plan == "PREMIUM"

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // User primary identity Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, BorderDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(colors = listOf(ElectricBlue, NeonPurple))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = session?.email?.firstOrNull()?.uppercase() ?: "G",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = session?.email ?: "guest@novavpn.com",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isPremium) NeonPurple.copy(alpha = 0.15f) else BorderDark)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isPremium) "PRO PLAN" else "FREE TIER",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPremium) NeonPurple else TextMuted
                                )
                            }
                            if (!isPremium) {
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Upgrade",
                                    color = ElectricBlue,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable { onNavigateToPaywall() }
                                        .testTag("profile_upgrade_link")
                                )
                            }
                        }
                    }
                }
            }
        }

        // Simulated Connected Devices List
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, BorderDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Connected Devices List",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isPremium) "Premium active: 1 of 5 devices used" else "Free tier: 1 device active",
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhoneAndroid, null, tint = ElectricBlue, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("This Android Phone (Active)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("IP: 10.0.0.2 • Connected via WireGuard", color = TextMuted, fontSize = 11.sp)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ConnectedGreen)
                        )
                    }
                }
            }
        }

        // REFERRAL SCREEN COMPONENT
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, BorderDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Share, null, tint = ElectricBlue, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Refer Friends, Get Premium!",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Invite friends to NovaVPN. Each friend who signs up gives you +7 Days of full Pro Premium access for free!",
                        color = TextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                            .background(DarkBase)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = session?.referralCode ?: "NOVA_5X89", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Copy Code",
                            color = ElectricBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .clickable { /* Copy referral code simulation */ }
                                .testTag("copy_referral_code")
                        )
                    }
                }
            }
        }

        // Log Out Button
        item {
            Button(
                onClick = {
                    viewModel.logout {
                        onLogoutSuccess()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("profile_logout_button")
            ) {
                Icon(Icons.Default.ExitToApp, null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out Securely", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------------------------------------------------
// SUPPORT TAB: FAQs accordions, Live Chat simulation trigger, notify
// -------------------------------------------------------------
@Composable
fun SupportTabContent() {
    var expandedFaqIndex by remember { mutableStateOf<Int?>(null) }
    var chatSimulated by remember { mutableStateOf(false) }

    val faqs = listOf(
        "Does NovaVPN log my history?" to "Absolutely not. We enforce a strict no-logs policy verified by third party audits.",
        "How do I activate Split Tunneling?" to "Head over to Settings. Split Tunneling is a Premium feature allowing select apps to bypass the VPN.",
        "Why is my ping high?" to "Ping depends on distance. Use our Japan or Germany Premium servers to achieve lower latency."
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Notifications section
        item {
            Text(
                text = "SECURITY ALERTS & NOTIFICATIONS",
                fontSize = 11.sp,
                color = TextMuted,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, BorderDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    NotificationRow(
                        title = "US Server Reconnection",
                        desc = "Your tunnel was safely restored without any packet leaks.",
                        time = "12 mins ago"
                    )
                    Divider(color = BorderDark, thickness = 1.dp)
                    NotificationRow(
                        title = "Pro Plan Promotion",
                        desc = "Get 60% off our yearly pass today. Unlocked worldwide.",
                        time = "1 day ago"
                    )
                }
            }
        }

        // FAQs accordion
        item {
            Text(
                text = "FREQUENTLY ASKED QUESTIONS",
                fontSize = 11.sp,
                color = TextMuted,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                faqs.forEachIndexed { index, pair ->
                    val isExpanded = expandedFaqIndex == index
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        border = BorderStroke(1.dp, BorderDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedFaqIndex = if (isExpanded) null else index }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = pair.first, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = TextMuted
                                )
                            }
                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = pair.second, color = TextMuted, fontSize = 12.sp, lineHeight = 18.sp)
                            }
                        }
                    }
                }
            }
        }

        // Live Chat Simulation Button
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, BorderDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.AutoMirrored.Filled.HelpOutline, null, tint = ElectricBlue, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Need Direct Support?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Our support team is online 24/7. Open a live chat now.", color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(vertical = 4.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { chatSimulated = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_live_chat")
                    ) {
                        Text("Start Live Chat Simulation", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            if (chatSimulated) {
                AlertDialog(
                    onDismissRequest = { chatSimulated = false },
                    confirmButton = {
                        TextButton(onClick = { chatSimulated = false }) {
                            Text("Ok", color = ElectricBlue)
                        }
                    },
                    title = { Text("Live Agent Connected 💬", color = Color.White) },
                    text = { Text("Agent: 'Hello! Welcome to NovaVPN. How can I assist you with your subscription or protocol settings today?'", color = TextMuted) },
                    containerColor = CardDark
                )
            }
        }
    }
}

@Composable
fun NotificationRow(
    title: String,
    desc: String,
    time: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(ElectricBlue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Notifications, null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(text = desc, color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
        }
        Text(text = time, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

// -------------------------------------------------------------
// ADMIN TAB: Query all user session documents from Firestore
// -------------------------------------------------------------
@Composable
fun AdminTabContent(viewModel: VpnViewModel) {
    val adminUsersList by viewModel.adminUsersList.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchAllUsersForAdmin()
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, BorderDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Global Admin Operations Center",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Real-time access to all registered secure user accounts, connection parameters, and database collections.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.fetchAllUsersForAdmin() },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Refresh Firestore Users List", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (adminUsersList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = ElectricBlue)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Querying Firestore users...", color = TextMuted, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(adminUsersList) { user ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    border = BorderStroke(1.dp, if (user.plan == "PREMIUM") NeonPurple else BorderDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.email,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "UID: ${user.uid.take(12)}...",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (user.plan == "PREMIUM") NeonPurple.copy(alpha = 0.15f) else BorderDark)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = user.plan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (user.plan == "PREMIUM") NeonPurple else TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Usage info
                        val limitMB = user.dataLimit / (1024 * 1024L)
                        val usedMB = user.dataUsedToday / (1024 * 1024L)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Usage: ${usedMB}MB / ${if (limitMB > 100000000) "Unlimited" else "${limitMB}MB"}", color = TextMuted, fontSize = 12.sp)
                            Text("Protocol: ${user.preferredProtocol}", color = TextMuted, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Actions for admin to control the user subscription plan and data limit
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val updated = user.copy(
                                        plan = "PREMIUM",
                                        dataLimit = Long.MAX_VALUE,
                                        planExpiry = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000L)
                                    )
                                    viewModel.syncSessionToFirestore(updated)
                                    viewModel.fetchAllUsersForAdmin()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Set PREMIUM", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val updated = user.copy(
                                        plan = "FREE",
                                        dataLimit = 500 * 1024 * 1024L,
                                        dataUsedToday = 0L,
                                        planExpiry = 0L
                                    )
                                    viewModel.syncSessionToFirestore(updated)
                                    viewModel.fetchAllUsersForAdmin()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BorderDark),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reset FREE", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
