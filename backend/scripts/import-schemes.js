#!/usr/bin/env node
/**
 * JANSAARTHI AI — Scheme Database Ingestion & Synchronization CLI
 * Milestone 1: Central Government Scheme Database (SIH26092)
 *
 * Imports the 100 verified government schemes from RAG metadata into MongoDB.
 *
 * Features:
 * - Dry-run mode by default.
 * - Requires explicit `--apply` flag to commit changes.
 * - Idempotent upserts keyed on unique `schemeId`.
 * - Validates schema, fields, and detects duplicates.
 * - Verifies database target name matches `MONGODB_DB_NAME` (prevents writing to 'test').
 * - Sanitizes all connection logs to prevent credential leakage.
 * - Preserves existing user documents and collections.
 */

const fs = require('fs');
const path = require('path');
const mongoose = require('mongoose');
require('dotenv').config({ path: path.resolve(__dirname, '../.env') });

const Scheme = require('../src/models/Scheme');

/**
 * Sanitizes MongoDB connection URIs by redacting passwords and user credentials.
 */
function sanitizeUri(uri) {
    if (!uri) return '';
    return uri.replace(/:\/\/([^:]+):([^@]+)@/g, '://***:***@');
}

/**
 * Locates the scheme metadata file across canonical repository locations.
 */
function locateMetadataFile() {
    const candidatePaths = [
        path.resolve(__dirname, '../RAG/semantic_index/scheme_metadata.json'),
        path.resolve(__dirname, '../../RAG/semantic_index/scheme_metadata.json'),
        path.resolve(__dirname, '../../backend/RAG/semantic_index/scheme_metadata.json')
    ];

    for (const p of candidatePaths) {
        if (fs.existsSync(p)) {
            return p;
        }
    }
    throw new Error(`scheme_metadata.json not found in candidate paths: ${candidatePaths.join(', ')}`);
}

/**
 * Parses key-value pairs from the raw LangChain document string.
 */
function parseDocumentText(docText) {
    const result = {};
    if (!docText || typeof docText !== 'string') return result;

    const lines = docText.split('\n');
    for (const line of lines) {
        const colonIdx = line.indexOf(':');
        if (colonIdx !== -1) {
            const key = line.substring(0, colonIdx).trim();
            const value = line.substring(colonIdx + 1).trim();
            result[key] = value;
        }
    }
    return result;
}

/**
 * Derives government level and applicable states from ministry / department name.
 */
function deriveGovernmentContext(ministryOrDept) {
    if (!ministryOrDept || typeof ministryOrDept !== 'string') {
        return {
            governmentLevel: 'Central',
            applicableStates: ['All India'],
            ministry: null
        };
    }

    const trimmed = ministryOrDept.trim();
    if (trimmed.startsWith('Government of ')) {
        const stateName = trimmed.replace('Government of ', '').trim();
        return {
            governmentLevel: 'State',
            applicableStates: [stateName],
            ministry: trimmed
        };
    }

    return {
        governmentLevel: 'Central',
        applicableStates: ['All India'],
        ministry: trimmed
    };
}

/**
 * Transforms a raw scheme metadata record into the MongoDB Scheme document format.
 */
function mapRawRecordToScheme(rawRecord) {
    const parsedDoc = parseDocumentText(rawRecord.document);

    const schemeId = (rawRecord.scheme_id || parsedDoc.scheme_id || '').trim().toUpperCase();
    const schemeName = (rawRecord.scheme_name || parsedDoc.scheme_name || '').trim();
    const category = (rawRecord.category || parsedDoc.category || '').trim();
    const sourcePlatform = (rawRecord.source_platform || parsedDoc.source_platform || 'myScheme.gov.in').trim();
    const ministryOrDept = parsedDoc.ministry_or_department || null;
    const targetGroupOrPurpose = parsedDoc.target_group_or_purpose || null;
    const schemeLaunchDate = parsedDoc.scheme_launch_date || null;
    const dataStatus = parsedDoc.data_status || null;
    const embeddingId = rawRecord.embedding_id || (schemeId ? `scheme-${schemeId}` : null);

    const govContext = deriveGovernmentContext(ministryOrDept);

    return {
        schemeId,
        schemeName,
        shortDescription: targetGroupOrPurpose,
        fullDescription: targetGroupOrPurpose ? `Government welfare and assistance initiative: ${targetGroupOrPurpose}.` : null,
        category,
        subcategory: null,

        // Government Context
        governmentLevel: govContext.governmentLevel,
        ministry: govContext.ministry,
        department: null,
        applicableStates: govContext.applicableStates,
        implementingAgency: null,

        // Beneficiary Context
        targetBeneficiaries: targetGroupOrPurpose ? [targetGroupOrPurpose] : [],
        targetGroups: targetGroupOrPurpose ? [targetGroupOrPurpose] : [],
        occupation: [],
        ageCriteria: { minAge: null, maxAge: null, description: null },
        incomeCriteria: { maxIncome: null, currency: 'INR', description: null },
        socialCategoryCriteria: [],
        geographicalCriteria: govContext.governmentLevel === 'State' ? govContext.applicableStates[0] : 'All India',

        // Benefits (Preserve null where raw data is absent)
        benefitType: null,
        minimumBenefit: null,
        maximumBenefit: null,
        subsidyDetails: null,
        loanDetails: null,
        interestDetails: null,
        otherBenefits: [],

        // Application Details
        requiredDocuments: [],
        eligibilityCriteria: targetGroupOrPurpose ? [`Target: ${targetGroupOrPurpose}`] : [],
        applicationProcess: null,
        officialApplicationUrl: null,
        officialGuidelinesUrl: null,

        // Lifecycle & Verification Status
        schemeStatus: 'Active',
        applicationStatus: 'unknown',
        verificationStatus: 'Unverified',
        sourceName: sourcePlatform,
        sourceUrl: 'https://www.myscheme.gov.in',
        lastUpdatedAt: new Date(),
        lastVerifiedAt: null,

        // Provenance & RAG Compatibility
        schemeLaunchDate,
        dataStatus,
        embeddingId
    };
}

/**
 * Validates a mapped scheme object according to model rules.
 */
function validateScheme(scheme) {
    const errors = [];
    if (!scheme.schemeId || !/^SCH\d{3,}$/i.test(scheme.schemeId)) {
        errors.push(`Invalid schemeId: '${scheme.schemeId}'. Expected format SCH###.`);
    }
    if (!scheme.schemeName || scheme.schemeName.length < 3) {
        errors.push(`Invalid schemeName: '${scheme.schemeName}'. Must be at least 3 characters.`);
    }
    if (!scheme.category) {
        errors.push('Missing category.');
    }
    return errors;
}

/**
 * Main import runner
 */
async function runImport(options = {}) {
    const isApply = options.apply || process.argv.includes('--apply');
    const isQuiet = options.quiet || false;

    const log = (...args) => {
        if (!isQuiet) console.log(...args);
    };

    log('================================================================');
    log('JANSAARTHI AI — Scheme Database Ingestion Pipeline');
    log(`Mode: ${isApply ? 'APPLY (Writing to MongoDB)' : 'DRY-RUN (Inspection & Validation Only)'}`);
    log('================================================================');

    // 1. Locate and read metadata file
    const metadataPath = locateMetadataFile();
    log(`[Source] Reading scheme metadata from: ${metadataPath}`);
    const rawContent = fs.readFileSync(metadataPath, 'utf-8');
    const rawRecords = JSON.parse(rawContent);

    if (!Array.isArray(rawRecords) || rawRecords.length === 0) {
        throw new Error(`Expected a non-empty array of scheme records in ${metadataPath}`);
    }

    log(`[Source] Found ${rawRecords.length} total scheme records in metadata.`);

    // 2. Validate, map, and detect duplicates
    const seenSchemeIds = new Set();
    const seenSchemeNames = new Set();
    const duplicateIds = [];
    const duplicateNames = [];
    const validSchemes = [];
    const validationFailures = [];
    const incompleteSchemes = []; // Schemes missing detailed criteria

    for (let i = 0; i < rawRecords.length; i++) {
        const raw = rawRecords[i];
        const mapped = mapRawRecordToScheme(raw);
        const errors = validateScheme(mapped);

        if (errors.length > 0) {
            validationFailures.push({ index: i, schemeId: mapped.schemeId, errors });
            continue;
        }

        // Duplicate checks
        if (seenSchemeIds.has(mapped.schemeId)) {
            duplicateIds.push(mapped.schemeId);
        } else {
            seenSchemeIds.add(mapped.schemeId);
        }

        if (seenSchemeNames.has(mapped.schemeName.toLowerCase())) {
            duplicateNames.push(mapped.schemeName);
        } else {
            seenSchemeNames.add(mapped.schemeName.toLowerCase());
        }

        // Completeness tracking: check if detailed benefit or document info is present
        if (!mapped.benefitType || mapped.requiredDocuments.length === 0 || mapped.eligibilityCriteria.length <= 1) {
            incompleteSchemes.push(mapped.schemeId);
        }

        validSchemes.push(mapped);
    }

    log(`\n--- Ingestion Analysis ---`);
    log(`Total records processed:     ${rawRecords.length}`);
    log(`Valid records:                ${validSchemes.length}`);
    log(`Validation failures:          ${validationFailures.length}`);
    log(`Duplicate scheme IDs:         ${duplicateIds.length} ${duplicateIds.length > 0 ? JSON.stringify(duplicateIds) : ''}`);
    log(`Duplicate scheme names:       ${duplicateNames.length} ${duplicateNames.length > 0 ? JSON.stringify(duplicateNames) : ''}`);
    log(`Incomplete criteria records:  ${incompleteSchemes.length} (contain catalog metadata but pending detailed eligibility rules)`);

    // Government breakdown
    const centralCount = validSchemes.filter(s => s.governmentLevel === 'Central').length;
    const stateCount = validSchemes.filter(s => s.governmentLevel === 'State').length;
    log(`Central schemes:              ${centralCount}`);
    log(`State schemes:                ${stateCount}`);

    if (validationFailures.length > 0) {
        console.error('\n[Validation Errors]:', JSON.stringify(validationFailures, null, 2));
        throw new Error(`Data validation failed for ${validationFailures.length} records.`);
    }

    if (duplicateIds.length > 0) {
        throw new Error(`Duplicate scheme IDs encountered: ${duplicateIds.join(', ')}`);
    }

    // If dry run, finish safely here
    if (!isApply) {
        log('\n[Dry-Run Notice] Dry-run complete. No database modifications were made.');
        log('To commit these 100 schemes to MongoDB, re-run with: node scripts/import-schemes.js --apply\n');
        return {
            totalFound: rawRecords.length,
            validCount: validSchemes.length,
            incompleteCount: incompleteSchemes.length,
            duplicateIds,
            duplicateNames,
            applied: false
        };
    }

    // 3. Connect to MongoDB and apply updates
    const mongoUri = process.env.MONGODB_URI;
    const targetDbName = process.env.MONGODB_DB_NAME || 'jansaarthi';

    if (!mongoUri) {
        throw new Error('MONGODB_URI is not defined in backend/.env. Cannot proceed with --apply.');
    }

    // Safety guard against accidentally targeting a 'test' database in production mode
    log(`[Database] Connecting to: ${sanitizeUri(mongoUri)}`);
    log(`[Database] Verifying target database name: '${targetDbName}'...`);

    await mongoose.connect(mongoUri, {
        dbName: targetDbName,
        serverSelectionTimeoutMS: 8000
    });

    const activeDbName = mongoose.connection.name;
    if (activeDbName !== targetDbName) {
        throw new Error(`Active database name '${activeDbName}' does not match expected target '${targetDbName}'. Aborting write.`);
    }

    log(`[Database] Connected to target database: '${activeDbName}'. Preserving existing users collection.`);

    // Check users collection is safe
    const userCount = await mongoose.connection.db.collection('users').countDocuments();
    log(`[Database Safety] Confirmed ${userCount} existing records in 'users' collection will remain untouched.`);

    // 4. Idempotent bulk upsert operations
    let inserted = 0;
    let updated = 0;

    const bulkOps = validSchemes.map(scheme => ({
        updateOne: {
            filter: { schemeId: scheme.schemeId },
            update: {
                $set: scheme,
                $setOnInsert: { createdAt: new Date() }
            },
            upsert: true
        }
    }));

    log(`[Database] Executing bulk upsert of ${bulkOps.length} schemes...`);
    const bulkResult = await Scheme.bulkWrite(bulkOps, { ordered: false });

    inserted = bulkResult.upsertedCount || 0;
    updated = bulkResult.modifiedCount || 0;
    const matched = bulkResult.matchedCount || 0;

    const totalSchemesInDb = await Scheme.countDocuments();
    log(`\n[SUCCESS] Scheme Database Synchronization Complete!`);
    log(`  Inserted (New):     ${inserted}`);
    log(`  Updated (Existing): ${updated}`);
    log(`  Matched Unchanged:  ${matched - updated}`);
    log(`  Total in Collection: ${totalSchemesInDb}`);

    await mongoose.disconnect();
    log('[Database] Disconnected cleanly.\n');

    return {
        totalFound: rawRecords.length,
        validCount: validSchemes.length,
        incompleteCount: incompleteSchemes.length,
        duplicateIds,
        duplicateNames,
        applied: true,
        inserted,
        updated,
        totalInDb: totalSchemesInDb
    };
}

// Allow execution both via CLI and programmatically in tests
if (require.main === module) {
    runImport()
        .then(() => process.exit(0))
        .catch(err => {
            console.error('\n[FATAL ERROR]', err.message);
            process.exit(1);
        });
}

module.exports = {
    runImport,
    mapRawRecordToScheme,
    validateScheme,
    deriveGovernmentContext
};
