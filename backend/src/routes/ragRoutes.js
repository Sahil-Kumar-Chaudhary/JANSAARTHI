const express = require('express');
const router = express.Router();
const ragController = require('../controllers/ragController');

/**
 * @route   POST /api/rag/ask
 * @desc    Ask a question about government schemes via RAG
 * @access  Public (can also be called by authenticated citizens)
 */
router.post('/ask', ragController.askQuestion);

module.exports = router;
