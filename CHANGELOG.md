# Changelog

All notable changes to the **JANSAARTHI AI** project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [0.1.0-alpha.1] - 2026-11-17

### Initial Public Alpha Release (Developer Preview)
This is the first public alpha release of JANSAARTHI AI for Smart India Hackathon 2026 (Problem Statement ID: SIH26092). This release showcases the foundational Android client architecture, full-stack JWT authentication, live MongoDB Atlas integration, an offline-capable Python FastEmbed RAG pipeline with ONNX runtime embeddings, and an interactive client-side EMI calculator.

### Added
- **Android Application Foundation:**
  - Modern native Android client built with Kotlin, Jetpack Compose, Material 3, and MVVM architecture.
  - Multi-step citizen registration flow with client-side field validations and dynamic Indian state and district dropdowns covering 36 States/UTs.
  - Secure JWT-based Login and Session Guard (`AuthGuard`) protecting private application routes.
  - Multi-step interactive Forgot Password recovery flow with 6-digit OTP verification and strict password strength validation rules.
  - Citizen Profile and Settings screens supporting profile details editing and profile photo upload with local image compression (`ImageUtils`).
  - Dynamic English and Hindi localization (`Strings.kt` and `values-hi/strings.xml`) with instant runtime language switching.
  - Full Light and Dark theme palette integration with persistent user preference storage in `SettingsRepository`.
  - Client-side financial EMI Calculator with loan amount, interest rate, tenure, and subsidy percentage calculations.
- **AI Assistant & Retrieval-Augmented Generation (RAG):**
  - Interactive ChatGPT-style AI Assistant screen (`RagScreen.kt`) with expanding multiline composer, action triggers, and verified citation cards.
  - In-composer search mode selector supporting **Hybrid**, **Semantic**, and **Keyword (Lexical)** retrieval modes.
  - Local RAG query engine (`query_engine.py`) powered by FastEmbed using quantized ONNX embeddings (`sentence-transformers/all-MiniLM-L6-v2`) against a verified 100-scheme knowledge base.
  - Weighted lexical retrieval engine with domain synonym expansion, concept grouping, and exact title/acronym bonuses.
  - Grounded answer synthesis via Groq Cloud LLM (`llama-3.3-70b-versatile`) with strict mobile-formatting guardrails, plus deterministic structured fallback when API key is unconfigured.
  - Verified source attribution returning official portal links (`myScheme.gov.in`) and relevance scores.
- **Backend & Database Infrastructure:**
  - Node.js and Express.js REST API server with Render dynamic port binding and uncaught exception handlers.
  - MongoDB Atlas database connectivity with asynchronous non-blocking connection retries and database-health check middleware (`dbCheck.js`).
  - Secure password hashing using `bcryptjs` (salt rounds: 10) and stateless token authorization using `jsonwebtoken`.
  - Full automated backend test suite with 23 passing unit and integration tests (`authForgotPassword.test.js`, `ragAndBackend.test.js`).
  - Android unit test suite with 38 passing unit tests covering registration forms, forgot password transitions, markdown parsing, and RAG integration.

### Known Limitations (Alpha Scope)
- The 100-scheme RAG knowledge base is currently decoupled from MongoDB; the Android Schemes screen only displays initial sample schemes.
- The `Scheme Details`, `Recommendations`, `Eligibility`, `Partner Details`, `Documents`, and `Action Plan` routes currently render placeholder destination screens (`PlaceholderScreen.kt`).
- The banking partner locator and notification center render static sample cards without live GPS queries or push notification services.
- Chat history is currently maintained in memory during the active Compose session and is not yet persisted to local SQLite/Room or MongoDB.

---

## [Roadmap Ahead]
- **v0.2.0-alpha.1:** Full scheme catalogue integration in MongoDB and dedicated Scheme Details screen.
- **v0.3.0-alpha.1:** Profile-based scheme matching engine and AI Eligibility Gap Analyzer.
- **v0.4.0-beta.1:** Dynamic document checklist, multi-scheme comparison, and personalized application action plan.
- **v0.5.0-beta.1:** Voice-based regional language assistant (Speech-to-Text & Text-to-Speech) and persistent chat history.
- **v1.0.0:** Production-grade release with verified government source updates and end-to-end partner routing.
