package com.example.jansaarthi.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import com.example.jansaarthi.data.repository.AppLanguage

data class AppStrings(
    val language: AppLanguage,
    val homeGreeting: String,
    val homeSubtitle: String,
    val quickServices: String,
    val businessLoan: String,
    val educationLoan: String,
    val checkEligibility: String,
    val calculateEmi: String,
    val recommendedForYou: String,
    val viewScheme: String,
    val profile: String,
    val kycStatus: String,
    val businessDetails: String,
    val socialCategory: String,
    val appSettings: String,
    val languageSetting: String,
    val themeSetting: String,
    val notifications: String,
    val helpSupport: String
)

val EnglishStrings = AppStrings(
    language = AppLanguage.EN,
    homeGreeting = "Welcome!",
    homeSubtitle = "Let's find the right support for you.",
    quickServices = "Quick Services",
    businessLoan = "Business Loan",
    educationLoan = "Education Loan",
    checkEligibility = "Check Eligibility",
    calculateEmi = "Calculate EMI",
    recommendedForYou = "Recommended for you",
    viewScheme = "View Scheme",
    profile = "My Profile",
    kycStatus = "KYC Status",
    businessDetails = "Business Details",
    socialCategory = "Social Category",
    appSettings = "App Settings",
    languageSetting = "Language",
    themeSetting = "Theme",
    notifications = "Notifications",
    helpSupport = "Help & Support"
)

val HindiStrings = AppStrings(
    language = AppLanguage.HI,
    homeGreeting = "नमस्ते!",
    homeSubtitle = "आइए आपके लिए सही सहायता खोजें।",
    quickServices = "त्वरित सेवाएँ",
    businessLoan = "व्यापार ऋण",
    educationLoan = "शिक्षा ऋण",
    checkEligibility = "पात्रता जांचें",
    calculateEmi = "EMI की गणना करें",
    recommendedForYou = "आपके लिए अनुशंसित",
    viewScheme = "योजना देखें",
    profile = "मेरी प्रोफ़ाइल",
    kycStatus = "KYC स्थिति",
    businessDetails = "व्यापार विवरण",
    socialCategory = "सामाजिक श्रेणी",
    appSettings = "ऐप सेटिंग्स",
    languageSetting = "भाषा (Language)",
    themeSetting = "थीम (Theme)",
    notifications = "सूचनाएं",
    helpSupport = "सहायता और समर्थन"
)

val LocalAppStrings = staticCompositionLocalOf { EnglishStrings }
