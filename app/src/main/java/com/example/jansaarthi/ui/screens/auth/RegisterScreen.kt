package com.example.jansaarthi.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    // Basic Details
    var fullName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }

    // Category & Income
    var scCategory by remember { mutableStateOf("") }
    var annualFamilyIncome by remember { mutableStateOf("") }
    var occupation by remember { mutableStateOf("") }

    // Business / Credit
    var businessType by remember { mutableStateOf("") }
    var businessRegistrationStatus by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    var requestedAmount by remember { mutableStateOf("") }
    var projectCost by remember { mutableStateOf("") }

    // Identity Verification
    var verificationDocType by remember { mutableStateOf("Aadhaar Card") }
    var docTypeExpanded by remember { mutableStateOf(false) }
    val docTypes = listOf("Aadhaar Card", "PAN Card", "Voter ID", "Driving Licence", "Other Government ID")
    var verificationDocUrl by remember { mutableStateOf("Uploaded_Document.pdf") } // Mocked

    // Account Security
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var localError by remember { mutableStateOf<String?>(null) }

    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            viewModel.resetState()
            onNavigateToLogin()
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Your Account", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.systemBars
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .imePadding()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tell us a little about yourself so we can identify suitable government schemes, subsidies, and credit facilities.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            // ================= SECTION: Basic Details =================
            SectionHeading("Basic Details")
            
            AppTextField(value = fullName, onValueChange = { fullName = it }, label = "Full Name *", placeholder = "e.g. Ramesh Kumar")
            AppTextField(value = mobileNumber, onValueChange = { mobileNumber = it }, label = "Mobile Number *", placeholder = "10-digit mobile number", keyboardType = KeyboardType.Phone)
            AppTextField(value = emailAddress, onValueChange = { emailAddress = it }, label = "Email Address (Optional)", placeholder = "name@example.com", keyboardType = KeyboardType.Email)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    AppTextField(value = age, onValueChange = { age = it }, label = "Age *", placeholder = "e.g. 32", keyboardType = KeyboardType.Number)
                }
                Box(modifier = Modifier.weight(1f)) {
                    AppTextField(value = gender, onValueChange = { gender = it }, label = "Gender *", placeholder = "Select")
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    AppTextField(value = state, onValueChange = { state = it }, label = "State *", placeholder = "Select")
                }
                Box(modifier = Modifier.weight(1f)) {
                    AppTextField(value = district, onValueChange = { district = it }, label = "District *", placeholder = "Select")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // ================= SECTION: Category & Income =================
            SectionHeading("Category & Income")
            AppTextField(value = scCategory, onValueChange = { scCategory = it }, label = "Social Category", placeholder = "Select Category")
            AppTextField(value = annualFamilyIncome, onValueChange = { annualFamilyIncome = it }, label = "Annual Family Income", placeholder = "e.g. 1,50,000", keyboardType = KeyboardType.Number)
            AppTextField(value = occupation, onValueChange = { occupation = it }, label = "Occupation", placeholder = "Select Occupation")
            Spacer(modifier = Modifier.height(16.dp))

            // ================= SECTION: Business / Credit =================
            SectionHeading("Business / Credit")
            AppTextField(value = businessType, onValueChange = { businessType = it }, label = "Business / Enterprise Type", placeholder = "Select Business Type")
            AppTextField(value = businessRegistrationStatus, onValueChange = { businessRegistrationStatus = it }, label = "Business Registration Status", placeholder = "Select Status")
            AppTextField(value = purpose, onValueChange = { purpose = it }, label = "Purpose / Financial Requirement", placeholder = "e.g. Working capital & machine purchase")
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    AppTextField(value = requestedAmount, onValueChange = { requestedAmount = it }, label = "Required Loan", placeholder = "e.g. 1,50,000", keyboardType = KeyboardType.Number)
                }
                Box(modifier = Modifier.weight(1f)) {
                    AppTextField(value = projectCost, onValueChange = { projectCost = it }, label = "Est. Project Cost", placeholder = "e.g. 2,00,000", keyboardType = KeyboardType.Number)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // ================= SECTION: Identity Verification =================
            SectionHeading("Identity Verification")
            ExposedDropdownMenuBox(
                expanded = docTypeExpanded,
                onExpandedChange = { docTypeExpanded = !docTypeExpanded }
            ) {
                OutlinedTextField(
                    value = verificationDocType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Verification Document Type *", fontWeight = FontWeight.SemiBold) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = docTypeExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = docTypeExpanded,
                    onDismissRequest = { docTypeExpanded = false }
                ) {
                    docTypes.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                verificationDocType = selectionOption
                                docTypeExpanded = false
                            }
                        )
                    }
                }
            }
            
            OutlinedButton(
                onClick = { verificationDocUrl = "Aadhaar_Upload.pdf" },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (verificationDocUrl.isEmpty()) "Upload Document Proof *" else "Document Attached: $verificationDocUrl")
            }
            Spacer(modifier = Modifier.height(24.dp))

            // ================= SECTION: Account Security =================
            SectionHeading("Account Security")
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password *", fontWeight = FontWeight.SemiBold) },
                placeholder = { Text("At least 8 characters") },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password *", fontWeight = FontWeight.SemiBold) },
                placeholder = { Text("Re-enter password") },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                shape = RoundedCornerShape(12.dp)
            )

            if (localError != null) {
                Text(
                    text = localError!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else if (authState is AuthState.Error) {
                Text(
                    text = (authState as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            val isLoading = authState is AuthState.Loading
            Button(
                onClick = { 
                    if (fullName.isBlank() || mobileNumber.isBlank() || age.isBlank() || gender.isBlank() || 
                        state.isBlank() || district.isBlank() || password.isBlank() || confirmPassword.isBlank() || 
                        verificationDocType.isBlank() || verificationDocUrl.isBlank()) {
                        localError = "Please fill all mandatory fields marked with *"
                    } else if (password != confirmPassword) {
                        localError = "Passwords do not match"
                    } else {
                        localError = null
                        viewModel.registerFull(
                            name = fullName,
                            mobile = mobileNumber,
                            email = emailAddress.ifBlank { null },
                            password = password,
                            age = age.toIntOrNull() ?: 0,
                            gender = gender,
                            state = state,
                            district = district,
                            verificationDocType = verificationDocType,
                            verificationDocUrl = verificationDocUrl,
                            scCategory = scCategory.ifBlank { null },
                            annualFamilyIncome = annualFamilyIncome.toIntOrNull(),
                            occupation = occupation.ifBlank { null },
                            businessType = businessType.ifBlank { null },
                            businessRegistrationStatus = businessRegistrationStatus.ifBlank { null },
                            purpose = purpose.ifBlank { null },
                            requestedAmount = requestedAmount.toIntOrNull(),
                            projectCost = projectCost.toIntOrNull()
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Create Account", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Already have an account? ", color = MaterialTheme.colorScheme.onBackground)
                Text(
                    "Login",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }
    }
}

@Composable
fun SectionHeading(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp, top = 8.dp)
    )
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontWeight = FontWeight.SemiBold) },
        placeholder = { Text(placeholder) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(12.dp)
    )
}


