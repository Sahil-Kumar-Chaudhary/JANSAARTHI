const express = require('express');
const router = express.Router();
const schemeController = require('../controllers/schemeController');

/**
 * Public Scheme Discovery Endpoints
 * All routes are guarded by dbCheck in app.js.
 */

// GET /api/schemes - Paginated listing, full-text search, and multi-facet filtering
router.get('/', schemeController.getAllSchemes);

// GET /api/schemes/:id - Detailed lookup by schemeId (e.g. SCH001) or MongoDB _id
router.get('/:id', schemeController.getSchemeById);

module.exports = router;
