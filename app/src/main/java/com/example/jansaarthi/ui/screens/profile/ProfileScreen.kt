package com.example.jansaarthi.ui.screens.profile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.jansaarthi.App
import com.example.jansaarthi.R
import com.example.jansaarthi.data.repository.AppLanguage
import com.example.jansaarthi.data.repository.ThemeMode
import com.example.jansaarthi.ui.navigation.Routes
import com.example.jansaarthi.utils.ImageUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val settingsRepo = App.instance.settingsRepository
    val currentTheme by settingsRepo.themeMode.collectAsState()
    val currentLang by settingsRepo.appLanguage.collectAsState()
    
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showPhotoOptions by remember { mutableStateOf(false) }
    var isUploadingPhoto by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val currentUser by App.instance.authRepository.currentUser.collectAsState()
    val scope = rememberCoroutineScope()
    
    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            isUploadingPhoto = true
            scope.launch {
                val part = ImageUtils.getCompressedImagePart(context, uri)
                if (part != null) {
                    val result = App.instance.authRepository.uploadPhoto(part)
                    if (result.isFailure) {
                        Toast.makeText(context, "Upload failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Failed to process image", Toast.LENGTH_SHORT).show()
                }
                isUploadingPhoto = false
            }
        }
    }

    if (showPhotoOptions) {
        AlertDialog(
            onDismissRequest = { showPhotoOptions = false },
            title = { Text("Profile Photo") },
            text = { Text("Choose an option for your profile photo.") },
            confirmButton = {
                TextButton(onClick = {
                    showPhotoOptions = false
                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Text(stringResource(R.string.change_photo))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPhotoOptions = false
                    scope.launch {
                        App.instance.authRepository.deletePhoto()
                    }
                }) {
                    Text(stringResource(R.string.remove_photo), color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(currentValue = currentLang, onDismiss = { showLanguageDialog = false }, onSelect = { settingsRepo.setAppLanguage(it); showLanguageDialog = false })
    }
    if (showThemeDialog) {
        ThemeSelectionDialog(currentValue = currentTheme, onDismiss = { showThemeDialog = false }, onSelect = { settingsRepo.setThemeMode(it); showThemeDialog = false })
    }
    
    LaunchedEffect(Unit) {
        if (currentUser == null && App.instance.authRepository.isLoggedIn()) {
            val result = App.instance.authRepository.fetchMe()
            if (result.isFailure) {
                App.instance.authRepository.logout()
                navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.my_profile), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.PROFILE_BASIC) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.systemBars
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .padding(bottom = 96.dp)
        ) {
            if (currentUser != null) {
                // Header Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha=0.1f), CircleShape)
                                    .clip(CircleShape)
                                    .clickable { showPhotoOptions = true },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isUploadingPhoto) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                } else if (!currentUser!!.profilePhoto.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = currentUser!!.profilePhoto,
                                        contentDescription = "Profile Photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(currentUser!!.name.take(1).uppercase(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(currentUser!!.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text("+91 ${currentUser!!.mobile}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Menu Cards
                Text(stringResource(R.string.profile_details), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(bottom = 8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        ProfileMenuRow(Icons.Default.Person, stringResource(R.string.basic_details), stringResource(R.string.basic_details_desc)) { navController.navigate(Routes.PROFILE_BASIC) }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.5f))
                        ProfileMenuRow(Icons.Default.AccountBalanceWallet, stringResource(R.string.category_income), stringResource(R.string.category_income_desc)) { navController.navigate(Routes.PROFILE_CATEGORY_INCOME) }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.5f))
                        ProfileMenuRow(Icons.Default.BusinessCenter, stringResource(R.string.business_credit), stringResource(R.string.business_credit_desc)) { navController.navigate(Routes.PROFILE_BUSINESS_CREDIT) }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.5f))
                        val verifiedStatus = if (currentUser!!.verificationDocType.isNullOrBlank()) "Pending" else "${currentUser!!.verificationDocType} • Verified"
                        ProfileMenuRow(Icons.Default.VerifiedUser, stringResource(R.string.identity_verification), verifiedStatus) { navController.navigate(Routes.PROFILE_IDENTITY_VERIFICATION) }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }

            Text(stringResource(R.string.app_settings), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(bottom = 8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    PreferenceRow(Icons.Default.Language, stringResource(R.string.language_setting), currentLang.name) { showLanguageDialog = true }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.5f))
                    PreferenceRow(Icons.Default.DarkMode, stringResource(R.string.theme_setting), currentTheme.name) { showThemeDialog = true }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.5f))
                    PreferenceRow(Icons.Default.Notifications, stringResource(R.string.notifications), if(currentUser?.notificationsEnabled == true) "On" else "Off") { }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.5f))
                    PreferenceRow(Icons.AutoMirrored.Filled.HelpOutline, stringResource(R.string.help_support), "") { navController.navigate(Routes.PROFILE_SUPPORT) }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    scope.launch {
                        App.instance.authRepository.logout()
                        navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.logout), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileMenuRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
            Text(subtitle, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun PreferenceRow(icon: ImageVector, title: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Text(value, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun LanguageSelectionDialog(currentValue: AppLanguage, onDismiss: () -> Unit, onSelect: (AppLanguage) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_language)) },
        text = {
            Column {
                AppLanguage.values().forEach { lang ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(lang) }.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = (lang == currentValue), onClick = { onSelect(lang) })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(lang.name)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

@Composable
fun ThemeSelectionDialog(currentValue: ThemeMode, onDismiss: () -> Unit, onSelect: (ThemeMode) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_theme)) },
        text = {
            Column {
                ThemeMode.values().forEach { mode ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(mode) }.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = (mode == currentValue), onClick = { onSelect(mode) })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(mode.name)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
