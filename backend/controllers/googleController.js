const db = require('../db/database');
const oauth2Client = require('../config/google');

// 1. Generate Google Auth URL
exports.getAuthUrl = (req, res) => {
    const { userId, email, read, write } = req.query;
    
    if (!userId) return res.status(400).send('Missing userId parameter.');

    let scopes = [
        'https://www.googleapis.com/auth/userinfo.email',
        'https://www.googleapis.com/auth/userinfo.profile'
    ];

    if (write === 'true') {
        scopes.push(
            'https://www.googleapis.com/auth/calendar.events',
            'https://www.googleapis.com/auth/gmail.modify',
            'https://www.googleapis.com/auth/gmail.settings.basic',
            'https://www.googleapis.com/auth/drive',
            'https://www.googleapis.com/auth/spreadsheets'
        );
    } else {
        scopes.push(
            'https://www.googleapis.com/auth/calendar.events.readonly',
            'https://www.googleapis.com/auth/gmail.readonly',
            'https://www.googleapis.com/auth/drive.metadata.readonly',
            'https://www.googleapis.com/auth/spreadsheets.readonly'
        );
    }

    const authorizationUrl = oauth2Client.generateAuthUrl({
        access_type: 'offline',
        scope: scopes,
        include_granted_scopes: true,
        login_hint: email,
        prompt: 'consent',
        state: JSON.stringify({ 
            userId, 
            email, 
            canRead: read === 'true', 
            canWrite: write === 'true' 
        })
    });

    res.redirect(authorizationUrl);
};

// 2. Handle Google OAuth Callback
exports.handleCallback = async (req, res) => {
    const { code, state } = req.query;

    if (!code || !state) {
        return res.redirect('jarvisapp://oauth2redirect?status=error');
    }

    try {
        const stateData = JSON.parse(state);
        const { userId, canRead, canWrite } = stateData;

        const { tokens } = await oauth2Client.getToken(code);
        
        await db.query(
            `INSERT INTO user_integrations 
                (user_id, provider, access_token, refresh_token, expiry_date, can_read, can_write) 
             VALUES ($1, $2, $3, $4, $5, $6, $7)
             ON CONFLICT (user_id, provider) 
             DO UPDATE SET 
                access_token = EXCLUDED.access_token,
                refresh_token = COALESCE(EXCLUDED.refresh_token, user_integrations.refresh_token),
                expiry_date = EXCLUDED.expiry_date,
                can_read = EXCLUDED.can_read,
                can_write = EXCLUDED.can_write`,
            [userId, 'google', tokens.access_token, tokens.refresh_token, tokens.expiry_date, canRead, canWrite]
        );

        res.redirect(`jarvisapp://oauth2redirect?status=success&write=${canWrite}`);

    } catch (error) {
        if (error.message && error.message.includes('invalid_grant')) {
            console.error('CRITICAL: Redirect URI Mismatch! Check GOOGLE_REDIRECT_URI in your .env');
        }
        console.error('Google OAuth callback error:', error.message || error);
        res.redirect('jarvisapp://oauth2redirect?status=error');
    }
};

// 3. Get Integration Status
exports.getStatus = async (req, res) => {
    const userId = req.user.userId;
    try {
        const result = await db.query(
            'SELECT id FROM user_integrations WHERE user_id = $1 AND provider = $2',
            [userId, 'google']
        );
        res.json({ isConnected: result.rowCount > 0 });
    } catch (error) {
        console.error('Integration status error:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};
// 4. Disconnect Google Integration
exports.disconnect = async (req, res) => {
    const userId = req.user.userId;

    try {
        // Fetch tokens to revoke them from Google
        const result = await db.query(
            'SELECT refresh_token, access_token FROM user_integrations WHERE user_id = $1 AND provider = $2',
            [userId, 'google']
        );

        if (result.rowCount > 0) {
            const { refresh_token, access_token } = result.rows[0];
            
            // Attempt to revoke the token if it exists
            try {
                const tokenToRevoke = refresh_token || access_token;
                if (tokenToRevoke) {
                    await oauth2Client.revokeToken(tokenToRevoke);
                }
            } catch (revokeError) {
                console.error('Google token revocation warning (might already be invalid):', revokeError.message);
                // We proceed anyway to clean up the DB
            }

            // Remove from local database
            await db.query(
                'DELETE FROM user_integrations WHERE user_id = $1 AND provider = $2',
                [userId, 'google']
            );
        }

        res.json({ success: true, message: 'Google account disconnected successfully.' });
    } catch (error) {
        console.error('Disconnect error:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};
