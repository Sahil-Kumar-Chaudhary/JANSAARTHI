const mongoose = require('mongoose');

/**
 * Middleware to ensure MongoDB connection is active before processing database-dependent requests.
 * Prevents requests from hanging on connection buffering and returns immediate 503 status.
 */
const dbCheck = (req, res, next) => {
    // mongoose.connection.readyState:
    // 0 = disconnected, 1 = connected, 2 = connecting, 3 = disconnecting
    if (mongoose.connection.readyState !== 1) {
        return res.status(503).json({
            status: 'error',
            code: 'DATABASE_UNAVAILABLE',
            message: 'Database service is currently unavailable. Please try again shortly.'
        });
    }
    next();
};

module.exports = dbCheck;
