# Contributing to JANSAARTHI AI

Thank you for your interest in contributing to **JANSAARTHI AI** (Smart India Hackathon 2026, Problem Statement ID: SIH26092)!

We welcome contributions from open-source developers, academic researchers, and domain experts in public policy and e-governance.

---

## 1. Code of Conduct

Please help us maintain a collaborative, respectful, and constructive development environment. Be polite in code reviews, issues, and discussions.

---

## 2. Getting Started

### Prerequisites
- **Android Development:** Android Studio Ladybug (or newer), JDK 17, Android SDK Platform 34+.
- **Backend Development:** Node.js (v18+ recommended, v24 verified), npm.
- **Database:** MongoDB (local Community Server or MongoDB Atlas free tier).
- **Python / AI:** Python 3.10+ (Python 3.12 verified).

### Fork & Clone
1. Fork the repository on GitHub: `https://github.com/Sahil-Kumar-Chaudhary/JANSAARTHI`
2. Clone your fork locally:
   ```bash
   git clone https://github.com/<your-username>/JANSAARTHI.git
   cd JANSAARTHI
   ```
3. Create a descriptive feature branch:
   ```bash
   git checkout -b feature/scheme-details-screen
   ```

---

## 3. Development Workflow

### Backend & RAG Setup
1. Navigate to `backend/` and install dependencies:
   ```bash
   cd backend
   npm install
   ```
2. Configure local environment variables:
   ```bash
   cp ../.env.example .env
   # Edit .env with your local MongoDB connection string
   ```
3. Run the backend tests:
   ```bash
   npm test
   ```
4. Start the development server:
   ```bash
   npm start
   ```

### Android Setup
1. Open the project root in **Android Studio**.
2. Let Gradle sync and download required dependencies.
3. Run unit tests:
   ```powershell
   .\gradlew testDebugUnitTest
   ```
4. Build and install the debug APK onto an emulator or connected device:
   ```powershell
   .\gradlew assembleDebug
   ```

---

## 4. Coding Standards

### Android (Kotlin & Jetpack Compose)
- Follow standard Kotlin naming conventions and Android Jetpack Compose best practices.
- Use unidirectional data flow: UI emits events $\rightarrow$ ViewModel updates StateFlow $\rightarrow$ UI observes state.
- Keep composables modular, reusable, and preview-compatible where feasible.
- Maintain light and dark theme compatibility for all new UI components.

### Backend (Node.js & Express)
- Use standard RESTful API conventions (`GET`, `POST`, `PUT`, `DELETE`).
- Place route definitions in `src/routes/`, business logic in `src/controllers/`, and middleware in `src/middleware/`.
- Ensure all database-dependent routes pass through `dbCheck` middleware.
- Never hardcode credentials, secrets, or connection strings.

### Python RAG Engine
- Follow PEP 8 guidelines.
- Ground all generated scheme responses strictly in official metadata.
- Avoid external calls during retrieval unit tests; keep ONNX models cached locally.

---

## 5. Submitting Pull Requests

1. **Keep Pull Requests Focused:** One feature or bug fix per pull request.
2. **Run Tests:** Ensure both Android and backend tests pass before opening a PR:
   ```bash
   # In root
   ./gradlew testDebugUnitTest
   cd backend && npm test
   ```
3. **Commit Messages:** Follow conventional commit formatting:
   - `feat(scope): add new capability`
   - `fix(scope): resolve issue description`
   - `docs(scope): update documentation`
   - `test(scope): add unit or integration tests`
4. **Push & Open PR:** Push to your fork and submit a Pull Request against `main`. Provide a clear description and screenshots for UI changes.
