# JANSAARTHI AI — Implementation Status & Known Limitations

**Version:** `v0.1.0-alpha.1`  
**Current Development Phase:** Alpha Prototype (Developer Preview)  
**Estimated Overall Implementation:** ~51% (Rigorous weighted audit across 53 features)

---

## 1. Executive Summary

JANSAARTHI AI is an AI-powered welfare scheme discovery and assistance platform for marginalized entrepreneurs developed for **Smart India Hackathon 2026** (Problem Statement ID: **SIH26092**).

This document provides a technical audit of what is currently operational in the codebase versus what is planned for subsequent releases.

---

## 2. Status by Module

### A. Android Client Application

| Feature | Status | Notes |
| :--- | :--- | :--- |
| **Splash & Onboarding** | **COMPLETED** | Auto-redirection based on auth state. Intro slides. |
| **Multi-Step Registration** | **COMPLETED** | 3-step KYC form with 36 authentic Indian states and districts, client-side validation. |
| **JWT Login & Session Guard** | **COMPLETED** | Encrypted token storage in `TokenManager`; `AuthGuard` route interceptor. |
| **Forgot Password Recovery** | **COMPLETED** | 3-step flow with 6-digit OTP, countdown timer, password complexity checks. |
| **Citizen Profile & Settings** | **COMPLETED** | KYC, basic details, and category sub-screens connected to `PUT /api/profile`. |
| **Profile Photo Upload** | **COMPLETED** | Gallery picker, compression via `ImageUtils`, Base64 upload to MongoDB. |
| **Home Dashboard** | **PARTIALLY COMPLETED** | Dynamic user greeting; static quick-services and sample recommended scheme. |
| **Scheme Catalogue** | **PARTIALLY COMPLETED** | Category chips & local search work, but displays only 3 sample schemes from backend. |
| **Scheme Details Screen** | **NOT IMPLEMENTED** | Route `Routes.SCHEME_DETAILS` renders `PlaceholderScreen.kt`. |
| **AI Assistant (RAG Chat UI)** | **COMPLETED** | Modern ChatGPT-style composer, expanding input, formatted markdown, verified citations. |
| **In-Composer Search Modes** | **COMPLETED** | UI selector for Hybrid, Semantic, and Keyword modes. |
| **Chat History Persistence** | **UI ONLY** | Modal dialog inspects in-memory StateFlow only; wiped on screen exit. |
| **Financial EMI Calculator** | **COMPLETED** | Client-side formula with sliders for amount, interest, tenure, and subsidy. |
| **Banking Partner Locator** | **STATIC / MOCK** | 3 static branch cards; phone and maps intents work; no live GPS API. |
| **Notifications Center** | **STATIC / MOCK** | 2 sample notifications; no backend feed or FCM push service. |
| **English / Hindi Localization** | **COMPLETED** | Dynamic runtime toggle via Compose `LocalAppStrings` and `values-hi/strings.xml`. |
| **Light & Dark Themes** | **COMPLETED** | System, Light, and Dark modes persisted in SharedPreferences via `SettingsRepository`. |

---

### B. Backend & Database (Node.js & MongoDB Atlas)

| Endpoint / Component | Status | Notes |
| :--- | :--- | :--- |
| **Express Server Architecture** | **COMPLETED** | Modular routing, Render dynamic PORT binding, uncaught exception handlers. |
| **MongoDB Atlas Connection** | **COMPLETED** | Live cluster connectivity, asynchronous non-blocking retries, sanitization. |
| **Database Resilience Guard** | **COMPLETED** | Middleware `dbCheck.js` returns HTTP 503 `DATABASE_UNAVAILABLE` when offline. |
| **POST /api/auth/register** | **COMPLETED** | Bcrypt password hashing (10 rounds), checks mobile/email duplicates, returns JWT. |
| **POST /api/auth/login** | **COMPLETED** | Validates credentials, issues 7-day stateless JWT token. |
| **POST /api/auth/forgot-password** | **COMPLETED** | Generates 6-digit random OTP with 10-minute expiry timestamp. |
| **POST /api/auth/verify-reset-otp** | **COMPLETED** | Verifies OTP against user document, issues 15-minute reset token. |
| **POST /api/auth/reset-password** | **COMPLETED** | Validates password complexity and updates hashed password in database. |
| **GET /api/profile & PUT /api/profile** | **COMPLETED** | Authenticated citizen profile retrieval and update endpoints. |
| **POST & DELETE /api/profile/photo** | **COMPLETED** | Handles multipart image upload, converts to Base64 URI, atomic `$unset` for delete. |
| **GET /api/schemes** | **NOT IMPLEMENTED** | Endpoint currently returns empty array `[]`. |
| **GET /api/recommendations** | **STATIC / MOCK** | Returns 3 hardcoded schemes with static fields and trivial heuristic adjustments. |
| **POST /api/rag/ask** | **COMPLETED** | Non-blocking execution of Python query engine, input validation, 30s timeout guard. |
| **GET /api/eligibility** | **NOT IMPLEMENTED** | Endpoint currently returns empty object `{}`. |
| **GET /api/documents** | **NOT IMPLEMENTED** | Endpoint currently returns empty array `[]`. |
| **GET /api/action-plans** | **NOT IMPLEMENTED** | Endpoint currently returns empty array `[]`. |

---

### C. AI & Retrieval-Augmented Generation (RAG)

| Component | Status | Notes |
| :--- | :--- | :--- |
| **Scheme Knowledge Base** | **COMPLETED** | 100 central and state government schemes indexed with official metadata. |
| **Semantic Vector Index** | **COMPLETED** | 384-dimensional normalized vector matrix stored in `scheme_embeddings.npy` (150 KB). |
| **ONNX Runtime Inference** | **COMPLETED** | Powered by FastEmbed (`sentence-transformers/all-MiniLM-L6-v2`); runs in ~1.1s on CPU. |
| **Lexical Scoring Engine** | **NEEDS FIX** | BM25-style scoring with synonyms; suffers from token boundary drop on abbreviations. |
| **Hybrid Retrieval Fusion** | **COMPLETED** | Combines normalized semantic and lexical scores with concordance bonuses and pruning. |
| **LLM Grounding & Synthesis** | **COMPLETED** | Groq Cloud API integration (`llama-3.3-70b-versatile`) with strict mobile guidelines. |
| **Deterministic Fallback** | **COMPLETED** | Safe structured answer synthesized directly from context when API key is omitted. |
| **Verified Citations** | **COMPLETED** | Returns `schemeId`, `schemeName`, `category`, `sourcePlatform`, snippet, and score. |
| **Profile-Based Matching** | **NOT IMPLEMENTED** | User profile attributes (income, caste, purpose) are not yet linked to RAG engine. |

---

## 3. Automated Test Verification Summary

*Audit Execution: November 17, 2026*

### Android Unit Tests: 38 Passed, 0 Failed (100%)
- `RegistrationFlowUnitTest` (12 tests): Multi-step state transitions, income parsing, underage validation, location data.
- `ForgotPasswordUnitTest` (10 tests): OTP validation, password complexity (uppercase, lowercase, number, min length).
- `RagIntegrationTest` (9 tests): Response parsing, timeout error handling, URL verification, staging URLs.
- `FormattedMessageTextTest` (6 tests): Markdown table card conversion, bold/italic, bullet points, clean sanitization.
- `ExampleUnitTest` (1 test): Environment validation.

### Backend Tests: 23 Passed, 0 Failed (100%)
- `authForgotPassword.test.js` (10 tests): OTP generation, validation, expiry, and reset password constraints.
- `ragAndBackend.test.js` (13 tests): RAG input validation, Hybrid search, Semantic search, Lexical search, Out-of-domain rejection, fallback generation, and database resilience.

**Total Verified Automated Tests:** 61 tests passing across both stacks.
