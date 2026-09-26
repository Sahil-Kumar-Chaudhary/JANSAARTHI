# ?? JANSAARTHI AI

### AI-Driven Scheme Matching for Marginalized Entrepreneurs
**Smart India Hackathon 2026**
**Problem Statement: SIH26092**

---

## ?? Problem
Government financial-assistance information can be difficult to find and understand.

Users may struggle to know:
- which scheme fits their requirement
- whether they may satisfy eligibility conditions
- what documents are needed
- how much they may borrow
- where they should approach for the application

---

## ?? Solution
User Information
?
Requirement Understanding
?
Scheme Matching
?
Eligibility
?
Financial Calculation
?
Channel Partner
?
Documents
?
Application Guidance

---

## ? Core Objectives
1. Collect user/business information and identify relevant government schemes.
2. Implement AI/rule-based scheme matching using eligibility rules, NLP and RAG.
3. Provide personalized scheme details, eligibility information, required documents and application guidance.

---

## ?? Core Features
### ?? User & Authentication
- ?? Registration
- ? Login
- ?? Profile management

### ?? Scheme Matching
- ?? Requirement input
- ?? Scheme recommendations
- ?? Match reasons
- ?? Eligibility status

### ?? Financial Assistance
- ?? Scheme financial details
- ?? EMI calculator

### ?? Channel Partners
- ?? Authorized partner information
- ?? Nearby partner discovery

### ?? Documents & Guidance
- ?? Required documents
- ?? Personalized action plan

### ?? Android Experience
- ? Jetpack Compose
- ? Material 3
- ? Adaptive UI
- ? Light/Dark theme
- ? Responsive layouts

---

## ?? User Flow
Register
? Login
? Profile
? Describe Requirement
? Scheme Recommendations
? Eligibility
? Scheme Details
? EMI
? Channel Partner
? Documents
? Action Plan

---

## ??? Technology Stack
### Android
- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose

### Backend
- Node.js
- Express.js

---

## ??? Architecture
Android App
?
Retrofit
?
Node.js + Express
?
Services / Business Logic
?
MongoDB

---

## ?? Project Structure
JANSAARTHI/
+-- app/
+-- backend/
+-- gradle/
+-- README.md

---

## ?? Setup
### Android App
Open the project in Android Studio, sync Gradle, and run the app.

### Backend
```sh
cd backend
npm install
node server.js
```

### Environment Variables
1. Copy `backend/.env.example` to `backend/.env`
2. Add MongoDB Atlas connection string
3. Add JWT secret
4. Add port

---

## ?? API
### Authentication
?? POST /api/auth/register
?? POST /api/auth/login
?? GET /api/auth/me

### Profile
?? GET /api/profile
?? PUT /api/profile

---

## ?? Security
- Environment variables
- No secrets in Git

---

## ?? Testing
- Android emulator
- Gradle build

---

## ?? Current Status
| Module | Status |
|---|---|
| Android Compose UI | ? |
| Authentication | ?? |
| MongoDB | ?? |
| Profile | ?? |
| Scheme Matching | ?? |
| Eligibility | ?? |
| EMI Calculator | ?? |
| Partner Locator | ?? |
| Documents | ?? |
| Action Plan | ?? |
| AI/RAG | ?? |

---

## ??? Future Scope
- More government schemes
- More Indian languages
- Voice interaction improvements
- Government API integration

---

## ?? Disclaimer
JANSAARTHI provides informational guidance and potential eligibility assessment. Final eligibility, sanction and approval are determined by the concerned government authority/channel partner.

---

## ?? Team
| Member | Responsibility |
|---|---|
| Member 1 | Android Development + Jetpack Compose UI |
| Member 2 | Android Architecture + Local Data |
| Member 3 | Scheme + Eligibility + Recommendation |
| Member 4 | Finance + Channel Partner |
| Member 5 | Backend + AI/RAG |
| Member 6 | Testing + Security + Integration |

---

## ?? Project Information
Problem Statement: SIH26092
Organization: Ministry of Social Justice and Empowerment
Category: Software
Theme: Smart Automation

---

## ?? License
License: To be decided

