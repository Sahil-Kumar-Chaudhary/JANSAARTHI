const mongoose = require('mongoose');
const Scheme = require('../models/Scheme');

/**
 * Escapes special regex characters in a user search string to prevent ReDoS / injection.
 */
function escapeRegex(str) {
    if (!str || typeof str !== 'string') return '';
    return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

/**
 * GET /api/schemes
 * Retrieves paginated, filterable, and searchable government schemes from MongoDB.
 *
 * Supported Query Parameters:
 * - page: Integer (default 1)
 * - limit: Integer (default 10, max 100)
 * - search / q: String (searches across name, category, ministry, and shortDescription)
 * - name: String (filters by name match)
 * - category: String (e.g. "Business & Entrepreneurship")
 * - governmentLevel: "Central" | "State" | "Union Territory"
 * - state: String (e.g. "Gujarat", "Assam", "All India")
 * - schemeStatus / status: "Active" | "Discontinued" | "Proposed" | "unknown"
 * - applicationStatus: "Open" | "Closed" | "Upcoming" | "unknown"
 * - sort: "name_asc" | "name_desc" | "newest" | "id_asc" (default "id_asc")
 */
exports.getAllSchemes = async (req, res) => {
    try {
        const query = {};

        // 1. Pagination parameters
        let page = parseInt(req.query.page, 10);
        let limit = parseInt(req.query.limit, 10);

        if (isNaN(page) || page < 1) page = 1;
        if (isNaN(limit) || limit < 1) limit = 10;
        if (limit > 100) limit = 100;

        const skip = (page - 1) * limit;

        // 2. Search / Keyword filtering
        const searchTerm = (req.query.search || req.query.q || '').trim();
        const nameFilter = (req.query.name || '').trim();

        if (searchTerm) {
            const escaped = escapeRegex(searchTerm);
            const regex = new RegExp(escaped, 'i');
            query.$or = [
                { schemeName: regex },
                { schemeId: regex },
                { category: regex },
                { ministry: regex },
                { shortDescription: regex },
                { targetBeneficiaries: regex }
            ];
        } else if (nameFilter) {
            query.schemeName = new RegExp(escapeRegex(nameFilter), 'i');
        }

        // 3. Category filtering
        const category = (req.query.category || '').trim();
        if (category && category.toLowerCase() !== 'all') {
            query.category = new RegExp(`^${escapeRegex(category)}$`, 'i');
        }

        // 4. Central / State government level filtering
        const govLevel = (req.query.governmentLevel || '').trim();
        if (govLevel && govLevel.toLowerCase() !== 'all') {
            query.governmentLevel = new RegExp(`^${escapeRegex(govLevel)}$`, 'i');
        }

        // 5. State filtering
        const state = (req.query.state || '').trim();
        const includeNational = req.query.includeNational === 'true';
        if (state && state.toLowerCase() !== 'all') {
            const escapedState = escapeRegex(state);
            if (includeNational) {
                const stateCondition = [
                    { applicableStates: new RegExp(escapedState, 'i') },
                    { applicableStates: 'All India' }
                ];
                if (query.$or) {
                    query.$and = [{ $or: query.$or }, { $or: stateCondition }];
                    delete query.$or;
                } else {
                    query.$or = stateCondition;
                }
            } else {
                query.applicableStates = new RegExp(escapedState, 'i');
            }
        }

        // 6. Scheme Lifecycle status filtering
        const status = (req.query.schemeStatus || req.query.status || '').trim();
        if (status && status.toLowerCase() !== 'all') {
            query.schemeStatus = new RegExp(`^${escapeRegex(status)}$`, 'i');
        }

        // 7. Application status filtering
        const appStatus = (req.query.applicationStatus || '').trim();
        if (appStatus && appStatus.toLowerCase() !== 'all') {
            query.applicationStatus = new RegExp(`^${escapeRegex(appStatus)}$`, 'i');
        }

        // 8. Sorting options
        let sortOption = { schemeId: 1 };
        const sortParam = (req.query.sort || '').toLowerCase();
        if (sortParam === 'name_asc') sortOption = { schemeName: 1 };
        else if (sortParam === 'name_desc') sortOption = { schemeName: -1 };
        else if (sortParam === 'newest') sortOption = { createdAt: -1 };
        else if (sortParam === 'id_desc') sortOption = { schemeId: -1 };

        // 9. Execute count and find queries in parallel
        const [total, schemes] = await Promise.all([
            Scheme.countDocuments(query),
            Scheme.find(query)
                .sort(sortOption)
                .skip(skip)
                .limit(limit)
                .lean({ virtuals: true })
        ]);

        const totalPages = Math.ceil(total / limit) || 1;

        // Ensure backwards compatibility with client models expecting alias fields
        const formattedSchemes = schemes.map(s => ({
            ...s,
            id: s.schemeId,
            name: s.schemeName,
            purpose: s.shortDescription || (s.targetBeneficiaries && s.targetBeneficiaries[0]) || ''
        }));

        res.status(200).json({
            status: 'success',
            total,
            page,
            limit,
            totalPages,
            data: formattedSchemes
        });
    } catch (error) {
        console.error('[SchemeController] Error in getAllSchemes:', error);
        res.status(500).json({
            status: 'error',
            code: 'SCHEME_FETCH_FAILED',
            message: 'Failed to retrieve government schemes from database.'
        });
    }
};

/**
 * GET /api/schemes/:id
 * Retrieves a single government scheme by its unique `schemeId` (e.g. "SCH001")
 * or MongoDB `_id`.
 */
exports.getSchemeById = async (req, res) => {
    try {
        const idParam = (req.params.id || '').trim();

        if (!idParam) {
            return res.status(400).json({
                status: 'error',
                code: 'INVALID_SCHEME_ID',
                message: 'A valid scheme identifier is required.'
            });
        }

        let scheme = null;

        // 1. First attempt lookup by canonical schemeId (e.g. "SCH001")
        scheme = await Scheme.findOne({ schemeId: idParam.toUpperCase() }).lean({ virtuals: true });

        // 2. Fallback: If not found and param is a valid MongoDB ObjectId, search by _id
        if (!scheme && mongoose.Types.ObjectId.isValid(idParam)) {
            scheme = await Scheme.findById(idParam).lean({ virtuals: true });
        }

        if (!scheme) {
            return res.status(404).json({
                status: 'error',
                code: 'SCHEME_NOT_FOUND',
                message: `Government scheme with identifier '${idParam}' was not found in the database.`
            });
        }

        // Add backwards-compatible alias fields
        const formattedScheme = {
            ...scheme,
            id: scheme.schemeId,
            name: scheme.schemeName,
            purpose: scheme.shortDescription || (scheme.targetBeneficiaries && scheme.targetBeneficiaries[0]) || ''
        };

        res.status(200).json({
            status: 'success',
            data: formattedScheme
        });
    } catch (error) {
        console.error(`[SchemeController] Error in getSchemeById (${req.params.id}):`, error);
        res.status(500).json({
            status: 'error',
            code: 'SCHEME_LOOKUP_FAILED',
            message: 'Failed to retrieve scheme details due to an internal server error.'
        });
    }
};
