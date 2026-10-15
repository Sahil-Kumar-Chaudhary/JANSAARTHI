package com.example.jansaarthi.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.jansaarthi.App
import kotlinx.coroutines.launch

@Composable
fun ProfileRowItem(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailScaffold(
    navController: NavController,
    title: String,
    isEditing: Boolean,
    onEditClick: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isEditing) {
                        IconButton(onClick = onEditClick) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.systemBars,
        content = content
    )
}

@Composable
fun BasicDetailsScreen(navController: NavController) {
    val currentUser by App.instance.authRepository.currentUser.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    if (currentUser == null) return
    
    var editName by remember { mutableStateOf(currentUser!!.name) }
    var editEmail by remember { mutableStateOf(currentUser!!.email ?: "") }
    var editAge by remember { mutableStateOf(currentUser!!.age?.toString() ?: "") }
    var editGender by remember { mutableStateOf(currentUser!!.gender ?: "") }
    var editState by remember { mutableStateOf(currentUser!!.state ?: "") }
    var editDistrict by remember { mutableStateOf(currentUser!!.district ?: "") }

    ProfileDetailScaffold(navController, "Basic Details", isEditing, { isEditing = true }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            if (!isEditing) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ProfileRowItem("Full Name", currentUser!!.name)
                        ProfileRowItem("Mobile Number", "+91 ${currentUser!!.mobile}")
                        ProfileRowItem("Email", if (currentUser!!.email.isNullOrBlank()) "Not provided" else currentUser!!.email!!)
                        ProfileRowItem("Age", currentUser!!.age?.toString() ?: "Not provided")
                        ProfileRowItem("Gender", if (currentUser!!.gender.isNullOrBlank()) "Not provided" else currentUser!!.gender!!)
                        ProfileRowItem("State", if (currentUser!!.state.isNullOrBlank()) "Not provided" else currentUser!!.state!!)
                        ProfileRowItem("District", if (currentUser!!.district.isNullOrBlank()) "Not provided" else currentUser!!.district!!)
                    }
                }
            } else {
                OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
                OutlinedTextField(value = "+91 ${currentUser!!.mobile}", onValueChange = {}, label = { Text("Mobile Number") }, readOnly = true, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
                OutlinedTextField(value = editEmail, onValueChange = { editEmail = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
                OutlinedTextField(value = editAge, onValueChange = { editAge = it }, label = { Text("Age") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
                OutlinedTextField(value = editGender, onValueChange = { editGender = it }, label = { Text("Gender") }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
                OutlinedTextField(value = editState, onValueChange = { editState = it }, label = { Text("State") }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
                OutlinedTextField(value = editDistrict, onValueChange = { editDistrict = it }, label = { Text("District") }, modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp))
                
                Button(
                    onClick = {
                        scope.launch {
                            App.instance.authRepository.updateProfile(currentUser!!.copy(
                                name = editName,
                                email = editEmail.ifBlank { null },
                                age = editAge.toIntOrNull(),
                                gender = editGender.ifBlank { null },
                                state = editState.ifBlank { null },
                                district = editDistrict.ifBlank { null }
                            ))
                            isEditing = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CategoryIncomeScreen(navController: NavController) {
    val currentUser by App.instance.authRepository.currentUser.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    if (currentUser == null) return
    
    var editCategory by remember { mutableStateOf(currentUser!!.scCategory ?: "") }
    var editIncome by remember { mutableStateOf(currentUser!!.annualFamilyIncome?.toString() ?: "") }
    var editOccupation by remember { mutableStateOf(currentUser!!.occupation ?: "") }

    ProfileDetailScaffold(navController, "Category & Income", isEditing, { isEditing = true }) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 24.dp).imePadding().verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            if (!isEditing) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ProfileRowItem("Social Category", if (currentUser!!.scCategory.isNullOrBlank()) "Not provided" else currentUser!!.scCategory!!)
                        ProfileRowItem("Annual Family Income", if (currentUser!!.annualFamilyIncome != null) "? ${currentUser!!.annualFamilyIncome}" else "Not provided")
                        ProfileRowItem("Occupation", if (currentUser!!.occupation.isNullOrBlank()) "Not provided" else currentUser!!.occupation!!)
                    }
                }
            } else {
                OutlinedTextField(value = editCategory, onValueChange = { editCategory = it }, label = { Text("Social Category") }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
                OutlinedTextField(value = editIncome, onValueChange = { editIncome = it }, label = { Text("Annual Family Income") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
                OutlinedTextField(value = editOccupation, onValueChange = { editOccupation = it }, label = { Text("Occupation") }, modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp))
                
                Button(
                    onClick = {
                        scope.launch {
                            App.instance.authRepository.updateProfile(currentUser!!.copy(
                                scCategory = editCategory.ifBlank { null },
                                annualFamilyIncome = editIncome.toIntOrNull(),
                                occupation = editOccupation.ifBlank { null }
                            ))
                            isEditing = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BusinessCreditScreen(navController: NavController) {
    val currentUser by App.instance.authRepository.currentUser.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    if (currentUser == null) return
    
    var editBusiness by remember { mutableStateOf(currentUser!!.businessType ?: "") }
    var editRegStatus by remember { mutableStateOf(currentUser!!.businessRegistrationStatus ?: "") }
    var editPurpose by remember { mutableStateOf(currentUser!!.purpose ?: "") }
    var editReqAmt by remember { mutableStateOf(currentUser!!.requestedAmount?.toString() ?: "") }
    var editProjCost by remember { mutableStateOf(currentUser!!.projectCost?.toString() ?: "") }

    ProfileDetailScaffold(navController, "Business & Credit", isEditing, { isEditing = true }) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 24.dp).imePadding().verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            if (!isEditing) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ProfileRowItem("Business / Enterprise Type", if (currentUser!!.businessType.isNullOrBlank()) "Not provided" else currentUser!!.businessType!!)
                        ProfileRowItem("Business Registration Status", if (currentUser!!.businessRegistrationStatus.isNullOrBlank()) "Not provided" else currentUser!!.businessRegistrationStatus!!)
                        ProfileRowItem("Purpose / Financial Requirement", if (currentUser!!.purpose.isNullOrBlank()) "Not provided" else currentUser!!.purpose!!)
                        ProfileRowItem("Required Loan Amount", if (currentUser!!.requestedAmount != null) "? ${currentUser!!.requestedAmount}" else "Not provided")
                        ProfileRowItem("Estimated Project Cost", if (currentUser!!.projectCost != null) "? ${currentUser!!.projectCost}" else "Not provided")
                    }
                }
            } else {
                OutlinedTextField(value = editBusiness, onValueChange = { editBusiness = it }, label = { Text("Business / Enterprise Type") }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
                OutlinedTextField(value = editRegStatus, onValueChange = { editRegStatus = it }, label = { Text("Business Registration Status") }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
                OutlinedTextField(value = editPurpose, onValueChange = { editPurpose = it }, label = { Text("Purpose / Financial Requirement") }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
                OutlinedTextField(value = editReqAmt, onValueChange = { editReqAmt = it }, label = { Text("Required Loan Amount") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
                OutlinedTextField(value = editProjCost, onValueChange = { editProjCost = it }, label = { Text("Estimated Project Cost") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp))
                
                Button(
                    onClick = {
                        scope.launch {
                            App.instance.authRepository.updateProfile(currentUser!!.copy(
                                businessType = editBusiness.ifBlank { null },
                                businessRegistrationStatus = editRegStatus.ifBlank { null },
                                purpose = editPurpose.ifBlank { null },
                                requestedAmount = editReqAmt.toIntOrNull(),
                                projectCost = editProjCost.toIntOrNull()
                            ))
                            isEditing = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdentityVerificationScreen(navController: NavController) {
    val currentUser by App.instance.authRepository.currentUser.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    if (currentUser == null) return
    
    var editDocType by remember { mutableStateOf(currentUser!!.verificationDocType ?: "Aadhaar Card") }
    var docTypeExpanded by remember { mutableStateOf(false) }
    val docTypes = listOf("Aadhaar Card", "PAN Card", "Voter ID", "Driving Licence", "Other Government ID")

    ProfileDetailScaffold(navController, "Identity Verification", isEditing, { isEditing = true }) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 24.dp).imePadding().verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            if (!isEditing) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ProfileRowItem("Verification Document Type", if (currentUser!!.verificationDocType.isNullOrBlank()) "Not provided" else currentUser!!.verificationDocType!!)
                        ProfileRowItem("Verification Status", if (currentUser!!.verificationDocUrl.isNullOrBlank()) "Not uploaded" else "Document Uploaded • Pending Verification")
                        if (!currentUser!!.verificationDocUrl.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { /* View Document mock */ },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("View Uploaded Document")
                            }
                        }
                    }
                }
            } else {
                ExposedDropdownMenuBox(
                    expanded = docTypeExpanded,
                    onExpandedChange = { docTypeExpanded = !docTypeExpanded }
                ) {
                    OutlinedTextField(
                        value = editDocType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Verification Document Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = docTypeExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor().padding(bottom = 16.dp),
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
                                    editDocType = selectionOption
                                    docTypeExpanded = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedButton(
                    onClick = { /* Replace Document mock */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 24.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Replace / Re-upload Document")
                }
                
                Button(
                    onClick = {
                        scope.launch {
                            App.instance.authRepository.updateProfile(currentUser!!.copy(
                                verificationDocType = editDocType.ifBlank { null }
                            ))
                            isEditing = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
