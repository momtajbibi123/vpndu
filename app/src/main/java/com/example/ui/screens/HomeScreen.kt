package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.VpnViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: VpnViewModel,
    onNavigateToServerList: () -> Unit,
    onNavigateToPaywall: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val activeServer by viewModel.activeServer.collectAsState()
    val session by viewModel.userSession.collectAsState()
    val connTime by viewModel.connectionTime.collectAsState()
    val downSpeed by viewModel.downloadSpeed.collectAsState()
    val upSpeed by viewModel.uploadSpeed.collectAsState()

    // Pulse animation logic for connected/connecting state
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val pulseRadius1 by infiniteTransition.animateFloat(
        initialValue = 100f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseRadius1"
    )
    val pulseAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseAlpha1"
    )

    val radarRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarRotation"
    )

    fun formatDuration(seconds: Long): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hrs, mins, secs)
    }

    fun formatSpeed(bytesPerSec: Long): String {
        if (bytesPerSec <= 0) return "0.0 B/s"
        val kb = bytesPerSec / 1024.0
        val mb = kb / 1024.0
        return if (mb >= 1.0) {
            String.format(Locale.getDefault(), "%.1f Mbps", mb)
        } else {
            String.format(Locale.getDefault(), "%.1f Kbps", kb)
        }
    }

    Scaffold(
        containerColor = DarkBase,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.linearGradient(colors = listOf(ElectricBlue, NeonPurple))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Security, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("NovaVPN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                },
                actions = {
                    // Quick Stats & Settings Triggers
                    IconButton(onClick = onNavigateToStats, modifier = Modifier.testTag("home_to_stats")) {
                        Icon(Icons.Default.BarChart, contentDescription = "Stats", tint = Color.White)
                    }
                    IconButton(onClick = onNavigateToSettings, modifier = Modifier.testTag("home_to_settings")) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Animated Globe Radar Background Map Simulation
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
            ) {
                val centerOffset = Offset(size.width / 2, size.height * 0.42f)
                // Draw latitude/longitude grid rings
                drawCircle(
                    color = ElectricBlue.copy(alpha = 0.05f),
                    radius = 350f,
                    center = centerOffset,
                    style = Stroke(width = 1.5f)
                )
                drawCircle(
                    color = ElectricBlue.copy(alpha = 0.03f),
                    radius = 550f,
                    center = centerOffset,
                    style = Stroke(width = 1.5f)
                )
                drawCircle(
                    color = ElectricBlue.copy(alpha = 0.02f),
                    radius = 750f,
                    center = centerOffset,
                    style = Stroke(width = 1.5f)
                )

                // Radar scanning ray when connected or connecting
                if (connectionState == "CONNECTED" || connectionState == "CONNECTING") {
                    val angleRad = Math.toRadians(radarRotation.toDouble())
                    val endX = centerOffset.x + 450f * Math.cos(angleRad).toFloat()
                    val endY = centerOffset.y + 450f * Math.sin(angleRad).toFloat()
                    drawLine(
                        color = ElectricBlue.copy(alpha = 0.15f),
                        start = centerOffset,
                        end = Offset(endX, endY),
                        strokeWidth = 3f
                    )
                }

                // Node pins (simulated servers scattered)
                val serverPins = listOf(
                    Offset(centerOffset.x - 220f, centerOffset.y - 140f), // US
                    Offset(centerOffset.x + 180f, centerOffset.y - 210f), // UK
                    Offset(centerOffset.x + 310f, centerOffset.y + 120f), // SG
                    Offset(centerOffset.x - 140f, centerOffset.y + 280f)  // BR
                )
                for (pin in serverPins) {
                    drawCircle(
                        color = ElectricBlue.copy(alpha = 0.4f),
                        radius = 4f,
                        center = pin
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                // PLAN BADGE / DATA USED METER
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(CardDark)
                            .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
                            .clickable { onNavigateToPaywall() }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("home_plan_badge"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (session?.plan == "PREMIUM") Icons.Default.VpnLock else Icons.Default.Star,
                            contentDescription = null,
                            tint = if (session?.plan == "PREMIUM") NeonPurple else WarningAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (session?.plan == "PREMIUM") "PREMIUM ACTIVE" else "FREE PLAN • GO PRO",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Remaining Data cap or Connection info
                    if (session?.plan == "PREMIUM") {
                        Text(
                            text = "UNLIMITED SECURE BANDWIDTH",
                            fontSize = 11.sp,
                            color = ConnectedGreen,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    } else {
                        session?.let { s ->
                            val usedMB = s.dataUsedToday / (1024 * 1024)
                            val limitMB = s.dataLimit / (1024 * 1024)
                            val progress = (s.dataUsedToday.toFloat() / s.dataLimit).coerceIn(0f, 1f)

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "DATA USED: $usedMB MB / $limitMB MB",
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .width(180.dp)
                                        .height(4.dp)
                                        .clip(CircleShape),
                                    color = if (progress > 0.8f) WarningAmber else ElectricBlue,
                                    trackColor = BorderDark,
                                )
                            }
                        }
                    }
                }

                // CENTRAL CONNECT POWER BUTTON
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    // Pulsing Glow Rings when Connected
                    if (connectionState == "CONNECTED" || connectionState == "CONNECTING") {
                        Box(
                            modifier = Modifier
                                .size(pulseRadius1.dp)
                                .clip(CircleShape)
                                .background(
                                    ElectricBlue.copy(
                                        alpha = pulseAlpha1
                                    )
                                )
                        )
                    }

                    // Main outer ring card
                    Card(
                        modifier = Modifier
                            .size(150.dp)
                            .clickable {
                                if (connectionState == "CONNECTED") {
                                    viewModel.disconnectVpn()
                                } else {
                                    viewModel.connectVpn()
                                }
                            }
                            .testTag("home_connect_button"),
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = when (connectionState) {
                                "CONNECTED" -> ConnectedGreen.copy(alpha = 0.1f)
                                "CONNECTING" -> ElectricBlue.copy(alpha = 0.1f)
                                else -> CardDark
                            }
                        ),
                        border = borderStrokeForState(connectionState)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PowerSettingsNew,
                                    contentDescription = "Connect",
                                    tint = when (connectionState) {
                                        "CONNECTED" -> ConnectedGreen
                                        "CONNECTING" -> ElectricBlue
                                        else -> TextMuted
                                    },
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = when (connectionState) {
                                        "CONNECTED" -> "CONNECTED"
                                        "CONNECTING" -> "CONNECTING..."
                                        else -> "CONNECT"
                                    },
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }

                // STATS PANEL (IP, ACTIVE SERVER, SPEED GRAPH METER)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Current Connection Timer
                    if (connectionState == "CONNECTED") {
                        Text(
                            text = formatDuration(connTime),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Active Server Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        border = BorderStroke(1.dp, BorderDark),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToServerList() }
                            .testTag("home_server_selector")
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (activeServer?.isPremium == true) NeonPurple.copy(alpha = 0.15f)
                                            else ElectricBlue.copy(alpha = 0.15f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = null,
                                        tint = if (activeServer?.isPremium == true) NeonPurple else ElectricBlue
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = activeServer?.name ?: "No Server Selected",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = if (activeServer?.isPremium == true) "Premium • High Speed" else "Free Server • Throttled Speed",
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.SignalCellularAlt,
                                    contentDescription = null,
                                    tint = when {
                                        (activeServer?.ping ?: 100) < 30 -> ConnectedGreen
                                        (activeServer?.ping ?: 100) < 70 -> WarningAmber
                                        else -> MaterialTheme.colorScheme.error
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${activeServer?.ping ?: 0} ms",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(Icons.Default.KeyboardArrowRight, null, tint = TextMuted)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Speed stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SpeedMeterWidget(
                            modifier = Modifier.weight(1f),
                            title = "DOWNLOAD",
                            speedText = formatSpeed(downSpeed),
                            icon = Icons.Default.ArrowDownward,
                            iconColor = ElectricBlue
                        )
                        SpeedMeterWidget(
                            modifier = Modifier.weight(1f),
                            title = "UPLOAD",
                            speedText = formatSpeed(upSpeed),
                            icon = Icons.Default.ArrowUpward,
                            iconColor = NeonPurple
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Simulated IP display
                    Text(
                        text = "YOUR IP: ${if (connectionState == "CONNECTED") activeServer?.ip ?: "192.168.1.15" else "172.56.21.90"}",
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SpeedMeterWidget(
    modifier: Modifier,
    title: String,
    speedText: String,
    icon: ImageVector,
    iconColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, BorderDark),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(14.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = title, fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                Text(text = speedText, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun borderStrokeForState(state: String): BorderStroke {
    return when (state) {
        "CONNECTED" -> BorderStroke(2.dp, ConnectedGreen)
        "CONNECTING" -> BorderStroke(2.dp, ElectricBlue)
        else -> BorderStroke(1.dp, BorderDark)
    }
}
