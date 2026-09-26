const express = require('express');
const cors = require('cors');

const authRoutes = require('./routes/authRoutes');
const profileRoutes = require('./routes/profileRoutes');

const app = express();

app.use(cors());
app.use(express.json());

app.get('/api/health', (req, res) => {
    res.status(200).json({ status: 'ok', message: 'JANSAARTHI Backend is running' });
});

app.use('/api/auth', authRoutes);
app.use('/api/profile', profileRoutes);

const recommendationRoutes = require('./routes/recommendationRoutes');

// Mock endpoints for the rest of the flow (to be expanded later)
app.use('/api/schemes', (req, res) => res.json([]));
app.use('/api/requirements', (req, res) => res.json([]));
app.use('/api/recommendations', recommendationRoutes);
app.use('/api/eligibility', (req, res) => res.json({}));
app.use('/api/calculator', (req, res) => res.json({}));
app.use('/api/partners', (req, res) => res.json([]));
app.use('/api/documents', (req, res) => res.json([]));
app.use('/api/action-plans', (req, res) => res.json([]));

module.exports = app;
