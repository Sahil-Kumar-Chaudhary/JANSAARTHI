console.log('[Server] Initializing JANSAARTHI Backend process...');

/**
 * Fatal error handler: log error details safely and exit process
 * to prevent the server from continuing in an unsafe, corrupted state.
 */
const handleFatalError = (type, error) => {
    console.error(`[Server] Fatal ${type}:`, error ? (error.stack || error.message || error) : 'Unknown error');
    // Ensure the process exits with a non-zero exit code so the process supervisor (e.g. Render) restarts cleanly
    process.exit(1);
};

process.on('uncaughtException', (err) => {
    handleFatalError('Uncaught Exception', err);
});

process.on('unhandledRejection', (reason) => {
    handleFatalError('Unhandled Promise Rejection', reason);
});

require('dotenv').config();
const app = require('./src/app');
const connectDB = require('./src/config/db');

// Render sets process.env.PORT dynamically. Fallback to 10000 for local development.
const PORT = parseInt(process.env.PORT, 10) || 10000;
const HOST = '0.0.0.0';

console.log(`[Server] Configuring HTTP server on host: ${HOST}, port: ${PORT}`);

// Start the HTTP server first so Render detects the open port immediately
const server = app.listen(PORT, HOST, () => {
    console.log(`[Server] JANSAARTHI Backend successfully listening on http://${HOST}:${PORT}`);

    // Connect to MongoDB Atlas asynchronously without blocking HTTP server availability
    connectDB().catch((err) => {
        console.error('[Server] Initial MongoDB connection attempt encountered error:', err.message);
    });
});

// Handle server startup errors (e.g. port already in use)
server.on('error', (err) => {
    console.error(`[Server] Failed to bind to port ${PORT}:`, err.message);
    process.exit(1);
});

module.exports = server;
