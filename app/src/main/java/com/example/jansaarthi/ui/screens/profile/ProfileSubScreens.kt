package com.example.jansaarthi.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleSubScreen(navController: NavController, title: String, contentText: String) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contentText,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun KycStatusScreen(navController: NavController) = SimpleSubScreen(navController, "KYC Status", "Your Aadhaar and PAN are verified.")

@Composable
fun BusinessDetailsScreen(navController: NavController) = SimpleSubScreen(navController, "Business Details", "Micro Enterprise (Tailoring)\nUdyam Reg: UDYAM-MH-00-1234567")

@Composable
fun SocialCategoryScreen(navController: NavController) = SimpleSubScreen(navController, "Social Category", "SC Category (Verified via Certificate)")

@Composable
fun LanguageSelectionScreen(navController: NavController) = SimpleSubScreen(navController, "Language", "English is currently selected. Hindi and Marathi coming soon.")

@Composable
fun NotificationSettingsScreen(navController: NavController) = SimpleSubScreen(navController, "Notifications", "All push notifications are enabled.")

@Composable
fun HelpSupportScreen(navController: NavController) = SimpleSubScreen(navController, "Help & Support", "Call Nodal Desk at 1800-111-222\nOr email support@jansaarthi.gov.in")
