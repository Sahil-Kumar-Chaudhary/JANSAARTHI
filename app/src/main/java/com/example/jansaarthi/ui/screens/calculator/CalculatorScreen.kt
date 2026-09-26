package com.example.jansaarthi.ui.screens.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(navController: NavController) {
    var loanAmount by remember { mutableFloatStateOf(500000f) }
    var interestRate by remember { mutableFloatStateOf(8.5f) }
    var tenureMonths by remember { mutableFloatStateOf(60f) }
    var subsidyPercentage by remember { mutableFloatStateOf(0f) }

    val effectiveLoanAmount = loanAmount - (loanAmount * subsidyPercentage / 100)
    
    val r = (interestRate / 12) / 100
    val n = tenureMonths
    val emi = if (r > 0 && n > 0) {
        (effectiveLoanAmount * r * (1 + r).pow(n)) / ((1 + r).pow(n) - 1)
    } else {
        effectiveLoanAmount / n
    }
    
    val totalPayment = emi * n
    val totalInterest = totalPayment - effectiveLoanAmount

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EMI Calculator", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
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
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .padding(bottom = 96.dp) // Avoid floating bottom nav overlap
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Estimated EMI", color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f), fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "₹${emi.toInt()}",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("per month", color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f), fontSize = 14.sp)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Total Interest", color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f), fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("₹${totalInterest.toInt()}", color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Repayment", color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f), fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("₹${totalPayment.toInt()}", color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Text("Adjust Parameters", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(24.dp))
            
            SliderInput(
                label = "Loan Amount",
                value = loanAmount,
                onValueChange = { loanAmount = it },
                range = 50000f..5000000f,
                formatValue = { "₹${it.toInt()}" }
            )
            
            SliderInput(
                label = "Tenure",
                value = tenureMonths,
                onValueChange = { tenureMonths = it },
                range = 12f..120f,
                formatValue = { "${it.toInt()} Months" }
            )
            
            SliderInput(
                label = "Interest Rate (p.a.)",
                value = interestRate,
                onValueChange = { interestRate = it },
                range = 4f..15f,
                formatValue = { "%.1f%%".format(it) }
            )
            
            SliderInput(
                label = "Subsidy / Grant",
                value = subsidyPercentage,
                onValueChange = { subsidyPercentage = it },
                range = 0f..35f,
                formatValue = { "${it.toInt()}%" }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SliderInput(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    formatValue: (Float) -> String
) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(formatValue(value), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.secondary,
                activeTrackColor = MaterialTheme.colorScheme.secondary,
                inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
    }
}