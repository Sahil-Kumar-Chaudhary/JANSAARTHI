package com.example.jansaarthi.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.jansaarthi.ui.navigation.Routes
import com.example.jansaarthi.ui.theme.AppStrings
import com.example.jansaarthi.ui.theme.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val strings = LocalAppStrings.current
    val currentUser by com.example.jansaarthi.App.instance.authRepository.currentUser.collectAsState()
    
    // Fetch profile on mount if null
    LaunchedEffect(Unit) {
        if (currentUser == null && com.example.jansaarthi.App.instance.authRepository.isLoggedIn()) {
            com.example.jansaarthi.App.instance.authRepository.fetchMe()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp)
            .padding(bottom = 80.dp) // Extra padding to avoid content being hidden under FAB/BottomNav
    ) {
        HomeHeader(
            strings = strings,
            userName = currentUser?.name,
            onProfileClick = { navController.navigate(Routes.PROFILE) }
        )
        Spacer(modifier = Modifier.height(32.dp))
        VoiceAssistanceCard(
            onSearchClick = { navController.navigate(Routes.REQUIREMENT) }
        )
        Spacer(modifier = Modifier.height(32.dp))
        ExploreServicesGrid(navController, strings)
        Spacer(modifier = Modifier.height(32.dp))
        RecommendedSchemeCard(navController, strings)
        Spacer(modifier = Modifier.height(32.dp))
        WhyJansaarthiSection()
    }
}

@Composable
fun HomeHeader(strings: AppStrings, userName: String?, onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (userName != null) "Welcome, $userName!" else strings.homeGreeting,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = strings.homeSubtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable { onProfileClick() }
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = "Profile",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
fun VoiceAssistanceCard(onSearchClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSearchClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Mic,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    "Tell us your requirement",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Tap to speak or type",
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

data class ServiceItem(val title: String, val icon: ImageVector, val route: String)

@Composable
fun ExploreServicesGrid(navController: NavController, strings: AppStrings) {
    val services = listOf(
        ServiceItem(strings.businessLoan, Icons.Default.Store, Routes.REQUIREMENT),
        ServiceItem(strings.educationLoan, Icons.Default.School, Routes.REQUIREMENT),
        ServiceItem(strings.checkEligibility, Icons.AutoMirrored.Filled.FactCheck, Routes.SCHEMES),
        ServiceItem(strings.calculateEmi, Icons.Default.Calculate, Routes.CALCULATOR)
    )

    Text(
        strings.quickServices,
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    )
    Spacer(modifier = Modifier.height(16.dp))
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ServiceCard(services[0], Modifier.weight(1f)) { navController.navigate(services[0].route) }
            ServiceCard(services[1], Modifier.weight(1f)) { navController.navigate(services[1].route) }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ServiceCard(services[2], Modifier.weight(1f)) { navController.navigate(services[2].route) }
            ServiceCard(services[3], Modifier.weight(1f)) { navController.navigate(services[3].route) }
        }
    }
}

@Composable
fun ServiceCard(item: ServiceItem, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .wrapContentHeight()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(item.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                item.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun RecommendedSchemeCard(navController: NavController, strings: AppStrings) {
    Text(
        strings.recommendedForYou,
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    )
    Spacer(modifier = Modifier.height(16.dp))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(
                    "PM Mudra Yojana",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "98% Match",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Business expansion funding.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Max Loan: ₹10 Lakh", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { navController.navigate(Routes.SCHEMES) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(strings.viewScheme, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun WhyJansaarthiSection() {
    Text(
        "Why JANSAARTHI?",
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    )
    Spacer(modifier = Modifier.height(16.dp))
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        WhyItem(Icons.Default.Verified, "Verified Government Schemes")
        WhyItem(Icons.Default.Speed, "Fast AI-driven Eligibility")
        WhyItem(Icons.Default.SupportAgent, "Dedicated Nodal Support")
    }
}

@Composable
fun WhyItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
    }
}
