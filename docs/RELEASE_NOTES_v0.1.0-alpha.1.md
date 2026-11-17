# Release Notes: v0.1.0-alpha.1 — Initial Public Alpha

**Release Tag:** `v0.1.0-alpha.1`  
**Release Name:** JANSAARTHI AI — Initial Public Alpha  
**Target Release Type:** Pre-release (Developer Preview)  
**Date:** November 17, 2026  
**Problem Statement ID:** SIH26092  
**Repository:** [https://github.com/Sahil-Kumar-Chaudhary/JANSAARTHI](https://github.com/Sahil-Kumar-Chaudhary/JANSAARTHI)

---

## 1. Overview

We are pleased to introduce **JANSAARTHI AI v0.1.0-alpha.1**, the first public developer preview release developed for **Smart India Hackathon 2026**. 

JANSAARTHI AI ("*Aapki Zarurat. Sahi Sarkari Raasta.*") is an intelligent public-assistance platform engineered to help marginalized citizens and aspiring micro-entrepreneurs identify, understand, and apply for government welfare programs and financial assistance.

This initial alpha release establishes the core technical foundation: an end-to-end native Android application built with Jetpack Compose, a secure Node.js/Express authentication and profile backend connected to live MongoDB Atlas, and a fully functional local RAG (Retrieval-Augmented Generation) query engine operating over 100 indexed Indian government schemes with FastEmbed ONNX quantized embeddings.

---

## 2. What's Included in This Release

### 📱 Android Application (Kotlin & Jetpack Compose)
- **Modern User Experience:** Native UI designed using Material 3 with adaptive layouts for light and dark themes.
- **Onboarding & Splash:** Seamless startup flow with automatic session verification via `AuthGuard`.
- **Multi-Step Registration:** 3-step citizen KYC questionnaire with client-side validations and authentic Indian location data covering all 36 States/UTs and their districts.
- **Authentication & Security:** JWT login, secure token management via SharedPreferences, and an interactive 3-step Forgot Password OTP recovery flow.
- **Citizen Profile Management:** Grouped KYC, basic details, and social category sub-screens, along with camera/gallery photo upload with local compression (`ImageUtils`).
- **AI Assistant Screen:** ChatGPT-style conversational composer with multiline auto-expanding text input, action triggers, and expandable verified citation cards.
- **Retrieval Mode Selector:** In-composer pill allowing citizens to toggle between **Hybrid**, **Semantic**, and **Keyword** retrieval.
- **Financial EMI Calculator:** Interactive client-side loan calculator with dynamic sliders for principal amount, interest rate, tenure, and capital subsidy percentage.
- **Bilingual Localization:** Dynamic runtime switching between English and Hindi (`Strings.kt` and `values-hi/strings.xml`).

### ⚙️ Backend & Database Services (Node.js & MongoDB)
- **RESTful API Server:** Built on Express.js with dynamic PORT binding for deployment platforms like Render.
- **MongoDB Atlas Integration:** Live cluster connectivity with asynchronous non-blocking connection retries.
- **Database Resilience:** Custom `dbCheck` middleware ensuring database outages return HTTP 503 instead of request hangs.
- **Authentication Endpoints:** Robust `POST /api/auth/register`, `/login`, `/forgot-password`, `/verify-reset-otp`, and `/reset-password` endpoints.
- **Citizen Profile Endpoints:** Authenticated `GET /api/profile`, `PUT /api/profile`, and photo upload/delete operations.
- **RAG Subprocess Controller:** Secure execution wrapper around the Python query engine with 30-second timeout enforcement.

### 🧠 AI & Scheme Knowledge Engine (Python & FastEmbed)
- **100 Government Schemes Indexed:** Verified central and state schemes indexed from official public records (`myScheme.gov.in`).
- **FastEmbed ONNX Inference:** Normalized 384-dimensional vector embeddings pre-computed and evaluated offline using quantized ONNX runtime (`sentence-transformers/all-MiniLM-L6-v2`) in ~1.1 seconds.
- **Semantic & Lexical Dual Search:** Cosine similarity vector search combined with weighted BM25-style lexical search featuring domain synonym expansion.
- **Out-of-Domain (OOD) Guard:** Threshold guards preventing hallucinations when queries do not match government schemes.
- **Grounded Answer Synthesis:** Integrated with Groq Cloud LLM (`llama-3.3-70b-versatile`) with mobile formatting rules and an offline deterministic grounded fallback synthesizer.

---

## 3. Verified Test & Build Results

All tests have been executed and verified locally prior to release preparation:

- **Android Unit Tests:** **38 Passed, 0 Failed (100% Pass Rate)**
  - Form validation, state preservation across registration steps, password complexity, markdown parsing, and RAG API error handling.
- **Backend Integration Tests:** **23 Passed, 0 Failed (100% Pass Rate)**
  - Password reset OTP lifecycle, RAG input validation, Hybrid/Semantic/Lexical retrieval modes, and database resilience.
- **Android Debug Build:** **BUILD SUCCESSFUL**
  - Artifact created: `app/build/outputs/apk/debug/app-debug.apk` (21.09 MB).

---

## 4. Known Limitations & Alpha Notice

> [!IMPORTANT]
> **JANSAARTHI AI v0.1.0-alpha.1 is an early developer preview and alpha prototype.** It is intended for testing, demonstration, and evaluation purposes.

1. **Decoupled Scheme Catalogue:** The 100 schemes in the RAG vector index are not yet synchronized into a MongoDB collection. The Android Schemes screen currently displays initial sample schemes.
2. **Placeholder Screens:** Several downstream feature routes (`Scheme Details`, `Recommendations`, `Eligibility`, `Partner Details`, `Documents`, and `Action Plan`) route to `PlaceholderScreen.kt` pending implementation in Phase 2.
3. **In-Memory Chat History:** AI assistant conversations are held in memory during the active Compose session and reset upon application restart.
4. **Static Channel Partners & Notifications:** Partner locations and notifications are currently rendered from sample datasets without live GPS distance lookups or push notification services.
5. **Lexical Mode Acronym Handling:** Queries containing standalone abbreviations (e.g. "PMMY") without full titles may return zero results in pure lexical mode (though they resolve accurately in Hybrid mode).

---

## 5. Quick Start Instructions

### Running the Backend
```bash
cd backend
npm install
cp ../.env.example .env
# Configure your MONGODB_URI in .env
npm start
```

### Running the Android App
Open the project root directory in **Android Studio Ladybug** (or newer) with JDK 17 configured, connect an emulator or physical device via ADB, and select **Run 'app'**.
