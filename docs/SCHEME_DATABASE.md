# 🏛️ JANSAARTHI AI — Central Government Scheme Database Specification

> **Milestone 1:** Centralized Government Scheme Database Architecture & Ingestion  
> **Problem Statement:** SIH26092 | Smart India Hackathon 2026  
> **Component:** Node.js Backend, MongoDB Atlas, and RAG Provenance Layer  

---

## 1. Overview & Architecture

Prior to Milestone 1, the JANSAARTHI project maintained 100 government welfare schemes exclusively within a static file-based RAG vector index (`scheme_metadata.json` and `scheme_embeddings.npy`). MongoDB contained only the `users` collection, `GET /api/schemes` returned an empty array, and the Android scheme screens relied on static sample placeholders.

Milestone 1 establishes a **production-grade, centralized MongoDB database** for government welfare schemes while preserving 100% interoperability with the existing ONNX FastEmbed RAG engine.

```
       +-------------------------------------------------------------+
       |   Source Dataset: schemes.xlsx / scheme_metadata.json       |
       +-------------------------------------------------------------+
                                      |
                         (Safe Ingestion Pipeline)
                       node scripts/import-schemes.js
                                      |
                     +----------------+----------------+
                     |                                 |
                     v                                 v
        +--------------------------+     +--------------------------+
        |   MongoDB Atlas Database |     |   FastEmbed ONNX Index   |
        |   Collection: `schemes`  |     |   `scheme_embeddings.npy`|
        |   100 Structured Records |     |   384-dim Dense Vectors  |
        +--------------------------+     +--------------------------+
                     |                                 |
                     v                                 v
        +--------------------------+     +--------------------------+
        |  REST APIs: /api/schemes |     |  RAG Engine: /api/rag/ask|
        |  Search, Filter, Paging  |     |  Hybrid & Semantic Query |
        +--------------------------+     +--------------------------+
                     \                                 /
                      +---------------+---------------+
                                      |
                                      v
                       +-------------------------------+
                       |  Unified Android Application  |
                       |  (Consistent Scheme IDs)      |
                       +-------------------------------+
```

---

## 2. MongoDB Scheme Model (`backend/src/models/Scheme.js`)

The `Scheme` model is organized into 6 semantic domains:

```javascript
const schemeSchema = new mongoose.Schema({
    // Basic Information
    schemeId: { type: String, required: true, unique: true, index: true }, // e.g. "SCH001"
    schemeName: { type: String, required: true, index: true },
    shortDescription: { type: String, default: null },
    fullDescription: { type: String, default: null },
    category: { type: String, required: true, index: true },
    subcategory: { type: String, default: null },

    // Government Information
    governmentLevel: { type: String, enum: ['Central', 'State', 'Union Territory', 'unknown'], default: 'Central', index: true },
    ministry: { type: String, default: null, index: true },
    department: { type: String, default: null },
    applicableStates: { type: [String], default: ['All India'], index: true },
    implementingAgency: { type: String, default: null },

    // Beneficiary Information
    targetBeneficiaries: { type: [String], default: [] },
    targetGroups: { type: [String], default: [] },
    occupation: { type: [String], default: [] },
    ageCriteria: {
        minAge: { type: Number, default: null },
        maxAge: { type: Number, default: null },
        description: { type: String, default: null }
    },
    incomeCriteria: {
        maxIncome: { type: Number, default: null },
        currency: { type: String, default: 'INR' },
        description: { type: String, default: null }
    },
    socialCategoryCriteria: { type: [String], default: [] },
    geographicalCriteria: { type: String, default: null },

    // Benefits
    benefitType: { type: String, default: null },
    minimumBenefit: { type: Number, default: null },
    maximumBenefit: { type: Number, default: null },
    subsidyDetails: { type: String, default: null },
    loanDetails: { type: String, default: null },
    interestDetails: { type: String, default: null },
    otherBenefits: { type: [String], default: [] },

    // Application Details
    requiredDocuments: { type: [String], default: [] },
    eligibilityCriteria: { type: [String], default: [] },
    applicationProcess: { type: String, default: null },
    officialApplicationUrl: { type: String, default: null },
    officialGuidelinesUrl: { type: String, default: null },

    // Lifecycle & Verification
    schemeStatus: { type: String, enum: ['Active', 'Discontinued', 'Proposed', 'unknown'], default: 'Active', index: true },
    applicationStatus: { type: String, enum: ['Open', 'Closed', 'Upcoming', 'unknown'], default: 'unknown', index: true },
    verificationStatus: { type: String, enum: ['Verified', 'Unverified', 'Pending Review'], default: 'Unverified', index: true },
    sourceName: { type: String, default: 'myScheme.gov.in' },
    sourceUrl: { type: String, default: 'https://www.myscheme.gov.in' },
    lastUpdatedAt: { type: Date, default: null },
    lastVerifiedAt: { type: Date, default: null },

    // Provenance & RAG Interoperability
    schemeLaunchDate: { type: String, default: null },
    dataStatus: { type: String, default: null },
    embeddingId: { type: String, default: null, index: true } // "scheme-SCH001"
}, {
    timestamps: true,
    collection: 'schemes'
});
```

### Database Indexes
* **Full-Text Index (`SchemeTextIndex`):** Covers `schemeName` (weight 10), `category` (weight 5), `shortDescription` (weight 3), `ministry` (weight 2), and `targetBeneficiaries` (weight 2).
* **Compound Filter Indexes:**
  * `{ category: 1, governmentLevel: 1, schemeStatus: 1 }`
  * `{ applicableStates: 1, schemeStatus: 1 }`
  * `{ governmentLevel: 1, schemeStatus: 1 }`
* **Unique Key:** `schemeId: 1` enforces idempotency and rapid lookups.

---

## 3. Data Ingestion Pipeline (`backend/scripts/import-schemes.js`)

The import CLI transforms raw scheme documents into the structured schema.

### Execution Modes

#### 1. Dry-Run Mode (Default — Safe Inspection)
```powershell
node backend/scripts/import-schemes.js
# Or from backend/:
npm run import-schemes
```
* Reads and validates all records.
* Detects duplicate IDs or names.
* Generates an audit summary.
* **Writes zero records to MongoDB.**

#### 2. Apply Mode (Commits Changes)
```powershell
node backend/scripts/import-schemes.js --apply
```
* Verifies target database name matches `MONGODB_DB_NAME` (prevents writing into `test`).
* Sanitizes URIs in logs (credentials are never leaked).
* Performs bulk idempotent upserts keyed on `schemeId`.
* Preserves existing `users` documents without data loss.

---

## 4. Ingestion Audit Results

| Metric | Result | Description |
| :--- | :--- | :--- |
| **Total Source Records** | **100** | Read from `backend/RAG/semantic_index/scheme_metadata.json` |
| **Valid Records** | **100** | 100% passed strict schema validation |
| **Validation Failures** | **0** | Zero malformed records |
| **Duplicate `schemeId`s** | **0** | IDs are unique from `SCH001` through `SCH100` |
| **Duplicate Scheme Names** | **0** | No conflicting or duplicated scheme titles |
| **Central Level Schemes** | **83** | Central Ministries (Finance, MSME, Agriculture, etc.) |
| **State Level Schemes** | **17** | State initiatives across 8 states (Gujarat, Assam, Tamil Nadu, etc.) |
| **Incomplete Criteria Records**| **100** | Records contain catalog metadata, launch dates, and target groups, but pending detailed numerical criteria |

### Distribution by Category
* **Business & Entrepreneurship:** 27 schemes
* **Agriculture, Rural & Environment:** 24 schemes
* **Education & Learning:** 13 schemes
* **Skills & Employment:** 8 schemes
* **Social Welfare & Empowerment:** 8 schemes
* **Banking, Financial Services and Insurance:** 5 schemes
* **Utility & Sanitation:** 4 schemes
* **Women and Child Development:** 4 schemes
* **Housing & Shelter:** 3 schemes
* **Health & Wellness:** 2 schemes
* **Travel & Tourism:** 2 schemes

---

## 5. REST API Documentation

### 1. `GET /api/schemes`
Retrieves a paginated list of schemes with support for multi-attribute search and filtering.

#### Query Parameters
| Parameter | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `page` | Integer | `1` | Page number (min: 1) |
| `limit` | Integer | `10` | Page size (min: 1, max: 100) |
| `search` / `q` | String | - | Case-insensitive search across name, category, ministry, purpose |
| `category` | String | - | Filter by category (e.g. `Business & Entrepreneurship`) |
| `governmentLevel`| String | - | `Central` or `State` |
| `state` | String | - | State name (e.g. `Gujarat`, `Assam`) |
| `includeNational`| Boolean| `false` | When true with `state`, includes Central/All-India schemes |
| `schemeStatus` | String | - | `Active`, `Discontinued`, or `unknown` |
| `applicationStatus`| String| - | `Open`, `Closed`, or `unknown` |
| `sort` | String | `id_asc` | `id_asc`, `id_desc`, `name_asc`, `name_desc`, `newest` |

#### Example Response (HTTP 200 OK)
```json
{
  "status": "success",
  "total": 100,
  "page": 1,
  "limit": 2,
  "totalPages": 50,
  "data": [
    {
      "schemeId": "SCH001",
      "schemeName": "Pradhan Mantri Jan Dhan Yojana",
      "shortDescription": "Financial inclusion",
      "category": "Banking, Financial Services and Insurance",
      "governmentLevel": "Central",
      "ministry": "Ministry of Finance",
      "applicableStates": ["All India"],
      "targetBeneficiaries": ["Financial inclusion"],
      "schemeStatus": "Active",
      "applicationStatus": "unknown",
      "verificationStatus": "Unverified",
      "sourceName": "myScheme.gov.in",
      "sourceUrl": "https://www.myscheme.gov.in",
      "schemeLaunchDate": "2014-08-28",
      "embeddingId": "scheme-SCH001",
      "id": "SCH001",
      "name": "Pradhan Mantri Jan Dhan Yojana",
      "purpose": "Financial inclusion"
    },
    {
      "schemeId": "SCH002",
      "schemeName": "Pradhan Mantri Mudra Yojana",
      "shortDescription": "Micro-enterprise credit",
      "category": "Business & Entrepreneurship",
      "governmentLevel": "Central",
      "ministry": "Ministry of Finance",
      "applicableStates": ["All India"],
      "targetBeneficiaries": ["Micro-enterprise credit"],
      "schemeStatus": "Active",
      "applicationStatus": "unknown",
      "verificationStatus": "Unverified",
      "sourceName": "myScheme.gov.in",
      "sourceUrl": "https://www.myscheme.gov.in",
      "schemeLaunchDate": "2015-04-08",
      "embeddingId": "scheme-SCH002",
      "id": "SCH002",
      "name": "Pradhan Mantri Mudra Yojana",
      "purpose": "Micro-enterprise credit"
    }
  ]
}
```

---

### 2. `GET /api/schemes/:id`
Retrieves a single scheme by either its canonical `schemeId` (e.g., `SCH001`) or its MongoDB `_id`.

#### Success Response (HTTP 200 OK)
```json
{
  "status": "success",
  "data": {
    "schemeId": "SCH001",
    "schemeName": "Pradhan Mantri Jan Dhan Yojana",
    "shortDescription": "Financial inclusion",
    "category": "Banking, Financial Services and Insurance",
    "governmentLevel": "Central",
    "ministry": "Ministry of Finance",
    "applicableStates": ["All India"],
    "targetBeneficiaries": ["Financial inclusion"],
    "schemeStatus": "Active",
    "applicationStatus": "unknown",
    "verificationStatus": "Unverified",
    "sourceName": "myScheme.gov.in",
    "sourceUrl": "https://www.myscheme.gov.in",
    "schemeLaunchDate": "2014-08-28",
    "embeddingId": "scheme-SCH001",
    "id": "SCH001",
    "name": "Pradhan Mantri Jan Dhan Yojana",
    "purpose": "Financial inclusion"
  }
}
```

#### Not Found Response (HTTP 404)
```json
{
  "status": "error",
  "code": "SCHEME_NOT_FOUND",
  "message": "Government scheme with identifier 'SCH999' was not found in the database."
}
```

---

## 6. RAG Interoperability & Synchronization Strategy

To ensure zero drift between the AI RAG Assistant and the Scheme Catalogue:

1. **Identifier Parity:**
   * In MongoDB: `schemeId` is `SCH001`, `embeddingId` is `scheme-SCH001`.
   * In FastEmbed ONNX: Embeddings are keyed with `scheme-SCH001`, and `query_engine.py` outputs `schemeId: "SCH001"`.
   * Android client can directly resolve any citation returned by `POST /api/rag/ask` by fetching `GET /api/schemes/SCH001`.
2. **Synchronization Policy:**
   * Raw changes to government schemes are staged in `RAG/knowledge_base/schemes.xlsx` or `scheme_metadata.json`.
   * Running `npm run import-schemes -- --apply` applies the changes to MongoDB.
   * Because Milestone 1 retains the identical 100 scheme records, the pre-built `scheme_embeddings.npy` remains valid and does not need to be recomputed.

---

## 7. Automated Test Suite

Run the complete backend test suite:
```powershell
npm --prefix backend test
```

### Test Coverage Summary (41 Tests Passed, 0 Failed):
* **Auth & Password Reset Suite (`authForgotPassword.test.js`):** 10 tests
* **Backend & RAG Query Suite (`ragAndBackend.test.js`):** 13 tests
* **Scheme Database & Ingestion Suite (`schemeDatabase.test.js`):** 18 tests
  * Model validation (missing required fields, enum enforcement)
  * Government context derivation
  * Document text parser
  * Dry-run non-destructive execution
  * Paginated listing and total count
  * Search by keyword ("Mudra")
  * Category filtering ("Business & Entrepreneurship")
  * GovernmentLevel filtering (Central vs State)
  * State-specific filtering ("Gujarat")
  * Canonical `schemeId` lookup (`SCH001`)
  * MongoDB `_id` lookup
  * HTTP 404 for unknown schemes
  * Disconnected database resilience (HTTP 503 via `dbCheck`)
