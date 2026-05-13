const express = require('express');
const cors = require('cors');
require('dotenv').config();

// Configs & DB
const db = require('./db/database');

// Routes
const authRoutes = require('./routes/auth');
const chatRoutes = require('./routes/chat');
const profileRoutes = require('./routes/profile');
const googleRoutes = require('./routes/google');

const app = express();
const rateLimit = require('express-rate-limit');

// Rate Limiter: Prevent API spam (100 requests per 15 minutes)
const apiLimiter = rateLimit({
    windowMs: 15 * 60 * 1000, // 15 minutes
    max: 100, 
    message: { error: 'Too many requests from this IP, please try again after 15 minutes' },
    standardHeaders: true,
    legacyHeaders: false,
});

const port = process.env.PORT || 3000;

// --- GLOBAL MIDDLEWARE ---
app.use(cors()); 
app.use(express.json());

// Request Logger
app.use((req, res, next) => {
    console.log(`[${new Date().toISOString()}] ${req.method} ${req.url}`);
    next();
});

// --- MOUNT ROUTES ---
app.use('/api', apiLimiter);     // Apply rate limiting to all /api endpoints
app.use('/api', authRoutes);    // Auth (Login, Register)
app.use('/api', chatRoutes);    // Chat (Messages, AI Logic)
app.use('/api', profileRoutes); // Profile Management
app.use('/api', googleRoutes);  // Google OAuth & Status

// --- START SERVER ---
app.listen(port, () => {
    console.log(`🚀 J.A.R.V.I.S. Server running at http://localhost:${port}`);
});