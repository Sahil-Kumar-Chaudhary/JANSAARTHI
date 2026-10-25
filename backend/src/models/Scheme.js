const mongoose = require('mongoose');

/**
 * Scheme Schema for JANSAARTHI AI
 * Centralized Government Scheme Database Model (SIH26092)
 *
 * Designed to represent central and state welfare programs, credit initiatives,
 * subsidies, and social security schemes with full provenance and RAG compatibility.
 */
const schemeSchema = new mongoose.Schema({
    // =========================================================================
    // Basic Information
    // =========================================================================
    schemeId: {
        type: String,
        required: [true, 'schemeId is required'],
        unique: true,
        trim: true,
        uppercase: true,
        index: true
    },
    schemeName: {
        type: String,
        required: [true, 'schemeName is required'],
        trim: true,
        index: true
    },
    shortDescription: {
        type: String,
        default: null,
        trim: true
    },
    fullDescription: {
        type: String,
        default: null,
        trim: true
    },
    category: {
        type: String,
        required: [true, 'category is required'],
        trim: true,
        index: true
    },
    subcategory: {
        type: String,
        default: null,
        trim: true
    },

    // =========================================================================
    // Government Information
    // =========================================================================
    governmentLevel: {
        type: String,
        enum: ['Central', 'State', 'Union Territory', 'unknown'],
        default: 'Central',
        index: true
    },
    ministry: {
        type: String,
        default: null,
        trim: true,
        index: true
    },
    department: {
        type: String,
        default: null,
        trim: true
    },
    applicableStates: {
        type: [String],
        default: ['All India'],
        index: true
    },
    implementingAgency: {
        type: String,
        default: null,
        trim: true
    },

    // =========================================================================
    // Beneficiary Information
    // =========================================================================
    targetBeneficiaries: {
        type: [String],
        default: []
    },
    targetGroups: {
        type: [String],
        default: []
    },
    occupation: {
        type: [String],
        default: []
    },
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
    socialCategoryCriteria: {
        type: [String],
        default: []
    },
    geographicalCriteria: {
        type: String,
        default: null,
        trim: true
    },

    // =========================================================================
    // Benefits
    // =========================================================================
    benefitType: {
        type: String,
        default: null,
        trim: true
    },
    minimumBenefit: {
        type: Number,
        default: null
    },
    maximumBenefit: {
        type: Number,
        default: null
    },
    subsidyDetails: {
        type: String,
        default: null,
        trim: true
    },
    loanDetails: {
        type: String,
        default: null,
        trim: true
    },
    interestDetails: {
        type: String,
        default: null,
        trim: true
    },
    otherBenefits: {
        type: [String],
        default: []
    },

    // =========================================================================
    // Application Information
    // =========================================================================
    requiredDocuments: {
        type: [String],
        default: []
    },
    eligibilityCriteria: {
        type: [String],
        default: []
    },
    applicationProcess: {
        type: String,
        default: null,
        trim: true
    },
    officialApplicationUrl: {
        type: String,
        default: null,
        trim: true
    },
    officialGuidelinesUrl: {
        type: String,
        default: null,
        trim: true
    },

    // =========================================================================
    // Verification & Lifecycle
    // =========================================================================
    schemeStatus: {
        type: String,
        enum: ['Active', 'Discontinued', 'Proposed', 'unknown'],
        default: 'Active',
        index: true
    },
    applicationStatus: {
        type: String,
        enum: ['Open', 'Closed', 'Upcoming', 'unknown'],
        default: 'unknown',
        index: true
    },
    verificationStatus: {
        type: String,
        enum: ['Verified', 'Unverified', 'Pending Review'],
        default: 'Unverified',
        index: true
    },
    sourceName: {
        type: String,
        default: 'myScheme.gov.in',
        trim: true
    },
    sourceUrl: {
        type: String,
        default: 'https://www.myscheme.gov.in',
        trim: true
    },
    lastUpdatedAt: {
        type: Date,
        default: null
    },
    lastVerifiedAt: {
        type: Date,
        default: null
    },

    // =========================================================================
    // Historical / Provenance & RAG Compatibility
    // =========================================================================
    schemeLaunchDate: {
        type: String,
        default: null,
        trim: true
    },
    dataStatus: {
        type: String,
        default: null,
        trim: true
    },
    embeddingId: {
        type: String,
        default: null,
        index: true
    }
}, {
    timestamps: true,
    collection: 'schemes',
    toJSON: {
        virtuals: true,
        transform: (doc, ret) => {
            // Provide backwards-compatible and convenient top-level alias fields
            ret.id = ret.schemeId;
            return ret;
        }
    },
    toObject: {
        virtuals: true
    }
});

// Full-text search index across name, category, short description, ministry, and target beneficiaries
schemeSchema.index({
    schemeName: 'text',
    shortDescription: 'text',
    category: 'text',
    ministry: 'text',
    targetBeneficiaries: 'text'
}, {
    name: 'SchemeTextIndex',
    weights: {
        schemeName: 10,
        category: 5,
        shortDescription: 3,
        ministry: 2,
        targetBeneficiaries: 2
    }
});

// Compound indexes for frequent catalogue queries
schemeSchema.index({ category: 1, governmentLevel: 1, schemeStatus: 1 });
schemeSchema.index({ applicableStates: 1, schemeStatus: 1 });
schemeSchema.index({ governmentLevel: 1, schemeStatus: 1 });

const Scheme = mongoose.model('Scheme', schemeSchema);
module.exports = Scheme;
