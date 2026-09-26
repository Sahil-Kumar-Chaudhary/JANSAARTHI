package com.example.jansaarthi.data.model

data class Scheme(
    val id: String,
    val name: String,
    val ministry: String,
    val purpose: String,
    val maxAmountText: String,
    val matchPercentage: Int,
    val interestRate: String,
    val subsidyText: String,
    val reasons: List<String>? = null,
    val eligibilityStatus: String? = null
)
