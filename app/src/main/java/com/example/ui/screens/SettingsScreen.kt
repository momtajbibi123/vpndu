package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.VpnViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: VpnViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPaywall: () -> Unit
) {
    val session by viewModel.userSession.collectAsState()

    Scaffold(
        containerColor = DarkBase,
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("settings_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // SECURITY Section
            item {
                Text(
                    text = "CONNECTION & SECURITY",
                    fontSize = 12.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    border = BorderStroke(1.dp, BorderDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column {
                        // Kill Switch
                        SettingToggleRow(
                            icon = Icons.Default.Shield,
                            title = "Kill Switch",
                            description = "Block traffic immediately if connection drops.",
                            checked = session?.isKillSwitchEnabled ?: false,
                            onCheckedChange = { checked ->
                                if (session?.plan != "PREMIUM") {
                                    onNavigateToPaywall()
                                } else {
                                    viewModel.updateSettings(killSwitch = checked)
                                }
                            },
                            isLocked = session?.plan != "PREMIUM",
                            tag = "kill_switch_toggle"
                        )

                        Divider(color = BorderDark, thickness = 1.dp)

                        // Split Tunneling
                        SettingToggleRow(
                            icon = Icons.Default.Info,
                            title = "Split Tunneling",
                            description = "Select which apps bypass VPN tunnel.",
                            checked = session?.isSplitTunnelEnabled ?: false,
                            onCheckedChange = { checked ->
                                if (session?.plan != "PREMIUM") {
                                    onNavigateToPaywall()
                                } else {
                                    viewModel.updateSettings(splitTunnel = checked)
                                }
                            },
                            isLocked = session?.plan != "PREMIUM",
                            tag = "split_tunnel_toggle"
                        )

                        Divider(color = BorderDark, thickness = 1.dp)

                        // Auto-Connect WiFi
                        SettingToggleRow(
                            icon = Icons.Default.Wifi,
                            title = "Auto-Connect on Wi-Fi",
                            description = "Connect automatically on untrusted public Wi-Fi.",
                            checked = session?.autoConnectWifi ?: false,
                            onCheckedChange = { checked ->
                                viewModel.updateSettings(autoConnect = checked)
                            },
                            tag = "autoconnect_wifi_toggle"
                        )
                    }
                }
            }

            // TUNNELING PROTOCOL Section
            item {
                Text(
                    text = "VPN PROTOCOL",
                    fontSize = 12.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    border = BorderStroke(1.dp, BorderDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val protocols = listOf("WireGuard", "OpenVPN (TCP)", "OpenVPN (UDP)", "Nova Stealth")
                        protocols.forEach { proto ->
                            val isSelected = session?.preferredProtocol == proto
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) ElectricBlue.copy(alpha = 0.1f) else Color.Transparent)
                                    .clickable { viewModel.updateSettings(protocol = proto) }
                                    .padding(horizontal = 12.dp, vertical = 12.dp)
                                    .testTag("settings_proto_$proto"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = proto,
                                    color = if (isSelected) Color.White else TextMuted,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.Shield, contentDescription = "Active", tint = ElectricBlue, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            // APP DETAILS Section
            item {
                Text(
                    text = "APPLICATION DETAILS",
                    fontSize = 12.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    border = BorderStroke(1.dp, BorderDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Language", color = Color.White, fontSize = 14.sp)
                            Text("English (Default)", color = TextMuted, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Divider(color = BorderDark, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Current Version", color = Color.White, fontSize = 14.sp)
                            Text("v1.0.0 (Stable)", color = TextMuted, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isLocked: Boolean = false,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BorderDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = ElectricBlue)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    if (isLocked) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Lock, contentDescription = "Locked", tint = WarningAmber, modifier = Modifier.size(14.dp))
                    }
                }
                Text(text = description, color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ElectricBlue,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = BorderDark
            ),
            modifier = Modifier.testTag(tag)
        )
    }
}
