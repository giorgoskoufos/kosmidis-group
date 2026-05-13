const db = require('../db/database');

// 1. Fetch user profile
exports.getProfile = async (req, res) => {
    const userId = req.user.userId; 
    try {
        const result = await db.query('SELECT * FROM user_profiles WHERE user_id = $1', [userId]);
        if (result.rows.length === 0) {
            return res.json({}); 
        }
        res.json(result.rows[0]);
    } catch (error) {
        console.error("Profile fetch error:", error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

// 2. Save or Update user profile (UPSERT)
exports.updateProfile = async (req, res) => {
    const userId = req.user.userId;
    const { firstName, lastName, age, profession, interests } = req.body;

    try {
        const result = await db.query(
            `INSERT INTO user_profiles (user_id, first_name, last_name, age, profession, interests) 
             VALUES ($1, $2, $3, $4, $5, $6)
             ON CONFLICT (user_id) 
             DO UPDATE SET 
                first_name = EXCLUDED.first_name,
                last_name = EXCLUDED.last_name,
                age = EXCLUDED.age,
                profession = EXCLUDED.profession,
                interests = EXCLUDED.interests,
                updated_at = CURRENT_TIMESTAMP
             RETURNING *`,
            [userId, firstName, lastName, age, profession, interests]
        );
        res.json({ message: "Profile updated successfully!", profile: result.rows[0] });
    } catch (error) {
        console.error("Profile update error:", error);
        res.status(500).json({ error: 'Internal server error' });
    }
};
