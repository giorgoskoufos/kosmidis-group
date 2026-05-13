const jwt = require('jsonwebtoken');

module.exports = (req, res, next) => {
    try {
        const authHeader = req.headers.authorization;
        if (!authHeader || !authHeader.startsWith('Bearer ')) {
            return res.status(401).json({ error: 'Authorization token not found.' });
        }

        const token = authHeader.split(' ')[1];
        const decoded = jwt.verify(token, process.env.JWT_SECRET);
        
        // decoded expected to be { userId: number, ... }
        req.user = decoded; 
        next();
    } catch (err) {
        console.error("JWT Verification Error:", err.message);
        res.status(401).json({ error: 'Invalid token.' });
    }
};
