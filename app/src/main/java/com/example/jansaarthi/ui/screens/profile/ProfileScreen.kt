
 package com.example.jansaarthi.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.jansaarthi.App
import com.example.jansaarthi.data.repository.AppLanguage
import com.example.jansaarthi.data.repository.ThemeMode
import kotlinx.coroutines.launch
import com.example.jansaarthi.ui.navigation.Routes
import com.example.jansaarthi.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val settingsRepo = App.instance.settingsRepository
    val currentTheme by settingsRepo.themeMode.collectAsState()
    val currentLang by settingsRepo.appLanguage.collectAsState()
    val strings = LocalAppStrings.current
    
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentValue = currentLang,
            onDismiss = { showLanguageDialog = false },
            onSelect = { 
                settingsRepo.setAppLanguage(it)
                showLanguageDialog = false 
            }
        )
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentValue = currentTheme,
            onDismiss = { showThemeDialog = false },
            onSelect = { 
                settingsRepo.setThemeMode(it)
                showThemeDialog = false 
            }
        )
    }
    val currentUser by App.instance.authRepository.currentUser.collectAsState()
    val scope = rememberCoroutineScope()
    var showEditProfile by remember { mutableStateOf(false) }
    
    // Fetch profile on mount if null
    LaunchedEffect(Unit) {
        if (currentUser == null && App.instance.authRepository.isLoggedIn()) {
            App.instance.authRepository.fetchMe()
        }
    }

    if (showEditProfile && currentUser != null) {
        var editName by remember { mutableStateOf(currentUser!!.name) }
        var editAge by remember { mutableStateOf(currentUser!!.age?.toString() ?: "") }
        var editIncome by remember { mutableStateOf(currentUser!!.annualFamilyIncome?.toString() ?: "") }
        var editBusiness by remember { mutableStateOf(currentUser!!.businessType ?: "") }
        var editCategory by remember { mutableStateOf(currentUser!!.scCategory ?: "") }
        
        AlertDialog(
            onDismissRequest = { showEditProfile = false },
            title = { Text("Edit Profile") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = editAge, onValueChange = { editAge = it }, label = { Text("Age") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = editIncome, onValueChange = { editIncome = it }, label = { Text("Annual Income") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = editBusiness, onValueChange = { editBusiness = it }, label = { Text("Business Type") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = editCategory, onValueChange = { editCategory = it }, label = { Text("Social Category") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        App.instance.authRepository.updateProfile(currentUser!!.copy(
                            name = editName,
                            age = editAge.toIntOrNull(),
                            annualFamilyIncome = editIncome.toIntOrNull(),
                            businessType = editBusiness,
                            scCategory = editCategory
                        ))
                        showEditProfile = false
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfile = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.profile, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                actions = {
                    IconButton(onClick = { showEditProfile = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .padding(bottom = 96.dp) // Avoid overlap with floating nav if any
        ) {
        
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(60.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(currentUser?.name ?: "Loading...", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("+91 ${currentUser?.mobile ?: ""}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "✓ Aadhaar Verified",
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("Verification & Details", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(16.dp))
        
        ProfileOption(icon = Icons.Default.VerifiedUser, title = strings.kycStatus, subtitle = "Aadhaar & PAN", onClick = { navController.navigate(Routes.PROFILE_KYC) })
        ProfileOption(icon = Icons.Default.Business, title = strings.businessDetails, subtitle = currentUser?.businessType ?: "Not added", onClick = { navController.navigate(Routes.PROFILE_BUSINESS) })
        ProfileOption(icon = Icons.Default.Category, title = strings.socialCategory, subtitle = currentUser?.scCategory ?: "Not added", onClick = { navController.navigate(Routes.PROFILE_CATEGORY) })
        
        Spacer(modifier = Modifier.height(32.dp))
        Text(strings.appSettings, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(16.dp))
        
        val langLabel = if (currentLang == AppLanguage.EN) "English" else "हिन्दी"
        ProfileOption(icon = Icons.Default.Language, title = strings.languageSetting, subtitle = langLabel, onClick = { showLanguageDialog = true })
        
        val themeLabel = when (currentTheme) { ThemeMode.LIGHT -> "Light"; ThemeMode.DARK -> "Dark"; ThemeMode.SYSTEM -> "System" }
        ProfileOption(icon = Icons.Default.DarkMode, title = strings.themeSetting, subtitle = themeLabel, onClick = { showThemeDialog = true })
        
        ProfileOption(icon = Icons.Default.Notifications, title = strings.notifications, subtitle = "On", onClick = { navController.navigate(Routes.PROFILE_NOTIFICATIONS) })
        ProfileOption(icon = Icons.Default.SupportAgent, title = strings.helpSupport, subtitle = "Contact Nodal Desk", onClick = { navController.navigate(Routes.PROFILE_SUPPORT) })
        
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = { 
                com.example.jansaarthi.App.instance.authRepository.logout()
                navController.navigate(com.example.jansaarthi.ui.navigation.Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
        ) {
            Text("Log Out", color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(vertical = 4.dp), fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}
}

@Composable
fun ProfileOption(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            }
            Text(text = subtitle, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, contentDescription = "Next", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun LanguageSelectionDialog(currentValue: AppLanguage, onDismiss: () -> Unit, onSelect: (AppLanguage) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Language", color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onSelect(AppLanguage.EN) }.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = currentValue == AppLanguage.EN, onClick = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("English", color = MaterialTheme.colorScheme.onSurface)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onSelect(AppLanguage.HI) }.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = currentValue == AppLanguage.HI, onClick = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Hindi (हिन्दी)", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun ThemeSelectionDialog(currentValue: ThemeMode, onDismiss: () -> Unit, onSelect: (ThemeMode) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Theme", color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column {
                val options = listOf(ThemeMode.LIGHT to "Light", ThemeMode.DARK to "Dark", ThemeMode.SYSTEM to "System Default")
                options.forEach { (mode, label) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(mode) }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = currentValue == mode, onClick = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(label, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}