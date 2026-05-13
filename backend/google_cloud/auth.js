const { google } = require('googleapis');
require('dotenv').config();

async function getGoogleOAuthClient(db, userId) {
    const result = await db.query(
        'SELECT * FROM user_integrations WHERE user_id = $1 AND provider = $2', 
        [userId, 'google']
    );
    
    if (result.rows.length === 0) return null;

    const tokens = result.rows[0];
    const auth = new google.auth.OAuth2(
        process.env.GOOGLE_CLIENT_ID, 
        process.env.GOOGLE_CLIENT_SECRET, 
        process.env.GOOGLE_REDIRECT_URI
    );
    
    auth.setCredentials({
        access_token: tokens.access_token,
        refresh_token: tokens.refresh_token,
        expiry_date: tokens.expiry_date
    });

    auth.on('tokens', async (newTokens) => {
        await db.query(
            `UPDATE user_integrations SET 
             access_token = $1, 
             expiry_date = $2, 
             refresh_token = COALESCE($3, refresh_token)
             WHERE user_id = $4 AND provider = 'google'`,
            [newTokens.access_token, newTokens.expiry_date, newTokens.refresh_token, userId]
        );
    });

    return auth;
}

module.exports = { getGoogleOAuthClient };