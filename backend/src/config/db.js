const mongoose = require('mongoose');

let retryTimer = null;
let isConnecting = false;
let listenersAttached = false;

/**
 * Strips password and credentials from any MongoDB connection string or error message for safe logging.
 */
const sanitizeMongoUri = (str) => {
    if (!str) return '';
    return str.replace(/:\/\/([^:]+):([^@]+)@/g, '://***:***@');
};

/**
 * Attach Mongoose connection event listeners once.
 */
const attachEventListeners = () => {
    if (listenersAttached) return;
    listenersAttached = true;

    mongoose.connection.on('connected', () => {
        console.log('[MongoDB] Connected successfully to database: jansaarthi');
    });

    mongoose.connection.on('disconnected', () => {
        console.warn('[MongoDB] Disconnected from database.');
    });

    mongoose.connection.on('error', (err) => {
        console.error(`[MongoDB] Connection error: ${sanitizeMongoUri(err.message)}`);
    });

    mongoose.connection.on('reconnected', () => {
        console.log('[MongoDB] Reconnected to database: jansaarthi');
    });
};

/**
 * Connects to MongoDB Atlas asynchronously with controlled retry mechanism.
 * Does not exit the process on connection failure so HTTP server remains responsive.
 */
const connectDB = async () => {
    attachEventListeners();

    const uri = process.env.MONGODB_URI;

    if (!uri) {
        console.error('[MongoDB] Warning: MONGODB_URI is not defined in environment variables. Database features will remain unavailable.');
        return false;
    }

    if (mongoose.connection.readyState === 1 || isConnecting) {
        return true;
    }

    isConnecting = true;
    try {
        console.log(`[MongoDB] Connecting to database jansaarthi...`);
        await mongoose.connect(uri, {
            dbName: 'jansaarthi',
            serverSelectionTimeoutMS: 5000,
            connectTimeoutMS: 10000,
        });

        if (retryTimer) {
            clearTimeout(retryTimer);
            retryTimer = null;
        }
        return true;
    } catch (error) {
        console.error(`[MongoDB] Connection failed: ${sanitizeMongoUri(error.message)}`);

        // Schedule controlled retry after 5 seconds instead of tight loop
        if (!retryTimer) {
            retryTimer = setTimeout(() => {
                retryTimer = null;
                console.log('[MongoDB] Retrying connection...');
                connectDB();
            }, 5000);
        }
        return false;
    } finally {
        isConnecting = false;
    }
};

module.exports = connectDB;
