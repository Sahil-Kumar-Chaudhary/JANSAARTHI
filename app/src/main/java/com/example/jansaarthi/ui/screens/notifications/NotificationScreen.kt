package com.example.jansaarthi.ui.screens.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.jansaarthi.R
import com.example.jansaarthi.ui.navigation.Routes
import com.example.jansaarthi.ui.theme.DarkNavy
import com.example.jansaarthi.ui.theme.PrimaryGold

enum class NotificationCategory {
    ALL, UNREAD, SCHEMES
}

enum class NotificationType {
    SCHEME_MATCH,
    PROFILE_UPDATE,
    ASSISTANCE,
    SYSTEM
}

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val type: NotificationType,
    val isRead: Boolean = false,
    val targetRoute: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavController) {
    // Initial sample notifications personalized for JANSAARTHI user
    var notifications by remember {
        mutableStateOf(
            listOf(
                NotificationItem(
                    id = "1",
                    title = "98% Match: PM Mudra Yojana",
                    message = "Based on your ST category and business requirements in Bihar, you are eligible for up to ₹10 Lakh collateral-free loan.",
                    timestamp = "10 mins ago",
                    type = NotificationType.SCHEME_MATCH,
                    isRead = false,
                    targetRoute = Routes.SCHEMES
                ),
                NotificationItem(
                    id = "2",
                    title = "Welcome to JANSAARTHI",
                    message = "Your registration is complete. Discover subsidized central & state government schemes tailored to your profile.",
                    timestamp = "1 hour ago",
                    type = NotificationType.SYSTEM,
                    isRead = false,
                    targetRoute = Routes.HOME
                ),
                NotificationItem(
                    id = "3",
                    title = "Complete Business & Credit Profile",
                    message = "Add your enterprise category and credit details to receive instant scheme eligibility reports.",
                    timestamp = "Yesterday",
                    type = NotificationType.PROFILE_UPDATE,
                    isRead = true,
                    targetRoute = Routes.PROFILE_BUSINESS_CREDIT
                ),
                NotificationItem(
                    id = "4",
                    title = "JANSAARTHI AI Assistant Ready",
                    message = "Need help applying? Chat with JANSAARTHI AI for step-by-step guidance in Hindi or English.",
                    timestamp = "2 days ago",
                    type = NotificationType.ASSISTANCE,
                    isRead = true,
                    targetRoute = Routes.RAG_QUERY
                )
            )
        )
    }

    var selectedCategory by remember { mutableStateOf(NotificationCategory.ALL) }

    val filteredNotifications = remember(notifications, selectedCategory) {
        when (selectedCategory) {
            NotificationCategory.ALL -> notifications
            NotificationCategory.UNREAD -> notifications.filter { !it.isRead }
            NotificationCategory.SCHEMES -> notifications.filter { it.type == NotificationType.SCHEME_MATCH }
        }
    }

    val unreadCount = remember(notifications) { notifications.count { !it.isRead } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.notifications),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = CircleShape,
                                color = PrimaryGold,
                                modifier = Modifier.size(22.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = unreadCount.toString(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    if (notifications.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                notifications = notifications.map { it.copy(isRead = true) }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Mark all read",
                                tint = PrimaryGold
                            )
                        }
                        IconButton(
                            onClick = {
                                notifications = emptyList()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Clear all",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NotificationFilterChip(
                    label = "All (${notifications.size})",
                    selected = selectedCategory == NotificationCategory.ALL,
                    onClick = { selectedCategory = NotificationCategory.ALL }
                )
                NotificationFilterChip(
                    label = "Unread ($unreadCount)",
                    selected = selectedCategory == NotificationCategory.UNREAD,
                    onClick = { selectedCategory = NotificationCategory.UNREAD }
                )
                NotificationFilterChip(
                    label = "Schemes",
                    selected = selectedCategory == NotificationCategory.SCHEMES,
                    onClick = { selectedCategory = NotificationCategory.SCHEMES }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (filteredNotifications.isEmpty()) {
                EmptyNotificationsView(
                    category = selectedCategory,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredNotifications, key = { it.id }) { item ->
                        NotificationCard(
                            item = item,
                            onClick = {
                                // Mark as read
                                notifications = notifications.map {
                                    if (it.id == item.id) it.copy(isRead = true) else it
                                }
                                // Navigate if target exists
                                if (item.targetRoute != null) {
                                    navController.navigate(item.targetRoute)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) PrimaryGold else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (selected) PrimaryGold else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
private fun NotificationCard(
    item: NotificationItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = BorderStroke(
            width = if (item.isRead) 1.dp else 1.5.dp,
            color = if (item.isRead) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f) else PrimaryGold.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isRead) 0.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Leading Type Icon Container
            val (icon, iconBg, iconTint) = getNotificationIconConfig(item.type)
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (item.isRead) FontWeight.Medium else FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (!item.isRead) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(PrimaryGold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 19.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.timestamp,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    )

                    if (item.targetRoute != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "View",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = PrimaryGold,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = PrimaryGold,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun getNotificationIconConfig(type: NotificationType): Triple<ImageVector, Color, Color> {
    return when (type) {
        NotificationType.SCHEME_MATCH -> Triple(
            Icons.Default.AccountBalance,
            PrimaryGold.copy(alpha = 0.15f),
            PrimaryGold
        )
        NotificationType.PROFILE_UPDATE -> Triple(
            Icons.Default.VerifiedUser,
            Color(0xFF16845B).copy(alpha = 0.15f),
            Color(0xFF16845B)
        )
        NotificationType.ASSISTANCE -> Triple(
            Icons.Default.AutoAwesome,
            DarkNavy.copy(alpha = 0.12f),
            DarkNavy
        )
        NotificationType.SYSTEM -> Triple(
            Icons.Default.Notifications,
            PrimaryGold.copy(alpha = 0.15f),
            PrimaryGold
        )
    }
}

@Composable
private fun EmptyNotificationsView(
    category: NotificationCategory,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = null,
                        tint = PrimaryGold,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = when (category) {
                    NotificationCategory.ALL -> "No Notifications"
                    NotificationCategory.UNREAD -> "No Unread Notifications"
                    NotificationCategory.SCHEMES -> "No Scheme Alerts"
                },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (category) {
                    NotificationCategory.ALL -> "You're all caught up! Relevant scheme alerts, application updates, and announcements will appear here."
                    NotificationCategory.UNREAD -> "All notifications have been marked as read."
                    NotificationCategory.SCHEMES -> "New matching schemes and loan eligibility alerts will appear here."
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
