package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
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
import com.example.ui.VpnViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    viewModel: VpnViewModel,
    onNavigateBack: () -> Unit
) {
    val session by viewModel.userSession.collectAsState()
    var selectedPlanId by remember { mutableStateOf("yearly") }

    // Testimonial rotation state
    var currentTestimonialIndex by remember { mutableStateOf(0) }
    val testimonials = listOf(
        "“Incredibly fast connection, I forgot I even had a VPN turned on!” — Rajesh K.",
        "“No logs policy is verified. The best interface on Android hands down.” — Chloe M.",
        "“Unlocked Singapore and Japan instantly. Ping dropped from 120ms to 20ms.” — Tyler S."
    )

    LaunchedEffect(key1 = true) {
        while (true) {
            delay(4000)
            currentTestimonialIndex = (currentTestimonialIndex + 1) % testimonials.size
        }
    }

    Scaffold(
        containerColor = DarkBase,
        topBar = {
            TopAppBar(
                title = { Text("NovaVPN Premium", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("paywall_back")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
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
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Text(
                    text = "Unlock Ultimate Privacy",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Text(
                    text = "No speed limits, unlimited bandwidth, and premium servers in over 50 global cities.",
                    color = TextMuted,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                )
            }

            // Trust Badges
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TrustBadgeWidget(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.VerifiedUser,
                        title = "No-Logs Verified"
                    )
                    TrustBadgeWidget(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.VerifiedUser,
                        title = "256-bit AES"
                    )
                    TrustBadgeWidget(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.VerifiedUser,
                        title = "Cancel Anytime"
                    )
                }
            }

            // Plan Cards
            item {
                PlanCardItem(
                    id = "weekly",
                    title = "Weekly Pass",
                    price = "₹99 / $1.99",
                    subtext = "Includes 3-day free trial",
                    isSelected = selectedPlanId == "weekly",
                    onClick = { selectedPlanId = "weekly" }
                )
                Spacer(modifier = Modifier.height(12.dp))
                PlanCardItem(
                    id = "monthly",
                    title = "Monthly",
                    price = "₹399 / $4.99",
                    subtext = "Most Popular • Auto-renews",
                    badge = "Most Popular",
                    isSelected = selectedPlanId == "monthly",
                    onClick = { selectedPlanId = "monthly" }
                )
                Spacer(modifier = Modifier.height(12.dp))
                PlanCardItem(
                    id = "yearly",
                    title = "Yearly Pass",
                    price = "₹1999 / $24.99",
                    subtext = "Best Value • Save 60%",
                    badge = "Save 60%",
                    isSelected = selectedPlanId == "yearly",
                    onClick = { selectedPlanId = "yearly" }
                )
                Spacer(modifier = Modifier.height(12.dp))
                PlanCardItem(
                    id = "lifetime",
                    title = "Lifetime License",
                    price = "₹4999 / $59.99",
                    subtext = "One-time purchase • Unlimited",
                    isSelected = selectedPlanId == "lifetime",
                    onClick = { selectedPlanId = "lifetime" }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // CTA Button
            item {
                Button(
                    onClick = {
                        viewModel.upgradeToPremium()
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("paywall_cta"),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (selectedPlanId == "weekly") "Start 3-Day Free Trial" else "Unlock Premium Now",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Restore Purchase",
                    color = TextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { viewModel.upgradeToPremium() }
                        .padding(8.dp)
                        .testTag("paywall_restore")
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Dynamic Testimonial Carousel
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    border = BorderStroke(1.dp, BorderDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AnimatedContent(
                            targetState = currentTestimonialIndex,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "Testimonial"
                        ) { targetIndex ->
                            Text(
                                text = testimonials[targetIndex],
                                color = Color.White,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Plan Comparison Table
            item {
                Text(
                    text = "Plan Comparison",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    textAlign = TextAlign.Start
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    border = BorderStroke(1.dp, BorderDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ComparisonRow(feature = "Secure Nodes", freeValue = "3 locations", premiumValue = "50+ global locations")
                        Divider(color = BorderDark, thickness = 1.dp)
                        ComparisonRow(feature = "Speeds", freeValue = "Up to 5 Mbps", premiumValue = "Up to 10 Gbps")
                        Divider(color = BorderDark, thickness = 1.dp)
                        ComparisonRow(feature = "Simultaneous Device", freeValue = "1 device only", premiumValue = "Up to 5 devices")
                        Divider(color = BorderDark, thickness = 1.dp)
                        ComparisonRow(feature = "Built-in Malware Blocker", freeValue = "Not available", premiumValue = "Available")
                        Divider(color = BorderDark, thickness = 1.dp)
                        ComparisonRow(feature = "Kill Switch & Split Tunnel", freeValue = "Not available", premiumValue = "Available")
                    }
                }
            }
        }
    }
}

@Composable
fun PlanCardItem(
    id: String,
    title: String,
    price: String,
    subtext: String,
    badge: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) ElectricBlue else BorderDark
    val borderThickness = if (isSelected) 2.dp else 1.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .border(borderThickness, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("paywall_plan_$id")
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    if (badge != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(ElectricBlue)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(text = badge, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
                Text(text = subtext, color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }
            Text(text = price, fontWeight = FontWeight.Bold, color = ElectricBlue, fontSize = 15.sp)
        }
    }
}

@Composable
fun TrustBadgeWidget(
    modifier: Modifier,
    icon: ImageVector,
    title: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, BorderDark),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ComparisonRow(
    feature: String,
    freeValue: String,
    premiumValue: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = feature, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1.2f))
        Text(text = freeValue, color = TextMuted, fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        Text(text = premiumValue, color = ConnectedGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.3f), textAlign = TextAlign.End)
    }
}
