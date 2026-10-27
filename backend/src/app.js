const express = require('express');
const cors = require('cors');
const mongoose = require('mongoose');

const authRoutes = require('./routes/authRoutes');
const profileRoutes = require('./routes/profileRoutes');
const recommendationRoutes = require('./routes/recommendationRoutes');
const ragRoutes = require('./routes/ragRoutes');
const dbCheck = require('./middleware/dbCheck');

const app = express();

app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

/**
 * Root health check / info endpoint
 */
app.get('/', (req, res) => {
    res.status(200).json({
        name: 'JANSAARTHI Backend API',
        status: 'running',
        healthCheck: '/api/health'
    });
});

/**
 * Detailed Health Check handler
 * Distinguishes between HTTP server availability and MongoDB connection status.
 * Returns HTTP 200 when database is connected, or HTTP 503 when degraded.
 */
const healthCheckHandler = (req, res) => {
    // 0 = disconnected, 1 = connected, 2 = connecting, 3 = disconnecting
    const state = mongoose.connection.readyState;
    const isDbConnected = state === 1;

    let dbStatusText = 'disconnected';
    if (state === 1) dbStatusText = 'connected';
    else if (state === 2) dbStatusText = 'connecting';
    else if (state === 3) dbStatusText = 'disconnecting';

    const payload = {
        status: isDbConnected ? 'ok' : 'degraded',
        server: 'healthy',
        database: {
            status: dbStatusText,
            name: isDbConnected ? (mongoose.connection.name || 'jansaarthi') : undefined
        },
        timestamp: new Date().toISOString()
    };

    const statusCode = isDbConnected ? 200 : 503;
    res.status(statusCode).json(payload);
};

// Register health check endpoints
app.get('/api/health', healthCheckHandler);
app.get('/health', healthCheckHandler);

// RAG Knowledge Base and Scheme Inquiry Endpoint
app.use('/api/rag', ragRoutes);

// Database-dependent routes guarded by dbCheck middleware
// When MongoDB is disconnected, these return HTTP 503 with DATABASE_UNAVAILABLE
app.use('/api/auth', dbCheck, authRoutes);
app.use('/api/profile', dbCheck, profileRoutes);
app.use('/api/recommendations', dbCheck, recommendationRoutes);
app.use('/api/schemes', dbCheck, (req, res) => res.json([]));
app.use('/api/requirements', dbCheck, (req, res) => res.json([]));
app.use('/api/eligibility', dbCheck, (req, res) => res.json({}));
app.use('/api/calculator', dbCheck, (req, res) => res.json({}));
app.use('/api/partners', dbCheck, (req, res) => res.json([]));
app.use('/api/documents', dbCheck, (req, res) => res.json([]));
app.use('/api/action-plans', dbCheck, (req, res) => res.json([]));

module.exports = app;
