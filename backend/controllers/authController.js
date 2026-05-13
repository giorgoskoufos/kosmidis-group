const db = require('../db/database');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');

exports.register = async (req, res) => {
    const { email, password } = req.body;

    if (!email || !password) {
        return res.status(400).json({ error: 'Please provide email and password.' });
    }

    try {
        // 1. Check if user already exists
        const userExists = await db.query('SELECT * FROM users WHERE email = $1', [email]);
        if (userExists.rows.length > 0) {
            return res.status(400).json({ error: 'This email is already in use.' });
        }

        // 2. Encrypt the password
        const saltRounds = 10;
        const hashedPassword = await bcrypt.hash(password, saltRounds);

        // 3. Save the user to the database
        const newUser = await db.query(
            'INSERT INTO users (email, password) VALUES ($1, $2) RETURNING id, email',
            [email, hashedPassword]
        );

        res.json({ message: 'Registration successful!', user: newUser.rows[0] });
    } catch (error) {
        console.error("Error in registration:", error);
        res.status(500).json({ error: 'Server error' });
    }
};

exports.login = async (req, res) => {
    const { email, password } = req.body;

    if (!email || !password) {
        return res.status(400).json({ error: 'Please provide email and password.' });
    }

    try {
        // 1. Search for the user
        const userResult = await db.query('SELECT * FROM users WHERE email = $1', [email]);
        if (userResult.rows.length === 0) {
            return res.status(401).json({ error: 'Invalid email or password.' });
        }

        const user = userResult.rows[0];

        // 2. Check if the password matches the encrypted one
        const match = await bcrypt.compare(password, user.password);
        if (!match) {
            return res.status(401).json({ error: 'Invalid email or password.' });
        }

        // 3. Create JWT Token
        const token = jwt.sign(
            { userId: user.id, email: user.email },
            process.env.JWT_SECRET,
            { expiresIn: '30d' } // Long duration for mobile app convenience, adjust as needed
        );

        // 4. Send back ID and Token
        res.json({ 
            message: 'Login successful!', 
            token, 
            userId: user.id 
        });
    } catch (error) {
        console.error("Error in login:", error);
        res.status(500).json({ error: 'Server error' });
    }
};
