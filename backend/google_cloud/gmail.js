const { google } = require('googleapis');
const { getGoogleOAuthClient } = require('./auth');

async function fetchMails(db, userId, query = "", maxResults = 5) {
    const auth = await getGoogleOAuthClient(db, userId);
    if (!auth) return "Error: User has not connected their Google account yet.";

    const gmail = google.gmail({ version: 'v1', auth });

    try {
        // 1. Get the list of email IDs based on the AI's search query
        const listRes = await gmail.users.messages.list({
            userId: 'me',
            q: query, // e.g., "is:unread", "from:boss@example.com", or just "" for latest
            maxResults: maxResults
        });

        const messages = listRes.data.messages;
        if (!messages || messages.length === 0) {
            return "No emails found for the specified query.";
        }

        // 2. Fetch the actual content (metadata & snippet) for each email ID
        const emailDetails = [];
        for (const msg of messages) {
            const msgRes = await gmail.users.messages.get({
                userId: 'me',
                id: msg.id,
                format: 'metadata', // Metadata is lightweight and gives us the headers we need
                metadataHeaders: ['Subject', 'From', 'Date']
            });

            const headers = msgRes.data.payload.headers;
            const subject = headers.find(h => h.name === 'Subject')?.value || 'No Subject';
            const from = headers.find(h => h.name === 'From')?.value || 'Unknown Sender';
            const date = headers.find(h => h.name === 'Date')?.value || '';

            emailDetails.push({
                id: msg.id,
                from: from,
                subject: subject,
                snippet: msgRes.data.snippet, // A short preview of the email body text
                date: date
            });
        }

        // Return a clean JSON string for the AI to parse
        return JSON.stringify(emailDetails);

    } catch (error) {
        console.error('Gmail API Error:', error);
        return "Error fetching emails from Google.";
    }
}

// Utility functions for Gmail features

// Helper function to fetch the user's Gmail signature
async function getUserSignature(gmail) {
    try {
        const res = await gmail.users.settings.sendAs.list({ userId: 'me' });
        // Find the primary email alias
        const primaryAlias = res.data.sendAs.find(alias => alias.isPrimary);
        if (primaryAlias && primaryAlias.signature) {
            return primaryAlias.signature; // Returns the signature in HTML format
        }
        return "";
    } catch (error) {
        console.error('Error fetching signature:', error);
        return "";
    }
}

// Tool for sending emails
async function sendMail(db, userId, to, subject, body) {
    const auth = await getGoogleOAuthClient(db, userId);
    if (!auth) return "Error: User has not connected their Google account yet.";

    const gmail = google.gmail({ version: 'v1', auth });

    try {
        // 0. Sanitize inputs
        const cleanTo = to.trim();
        const cleanSubject = subject.trim();
        const cleanBody = body.trim();

        // 1. Get user signature
        const signature = await getUserSignature(gmail);

        // 2. Combine AI text with user signature (if exists)
        const htmlBody = cleanBody.replace(/\n/g, '<br>');
        const finalBody = signature ? `${htmlBody}<br><br>${signature}` : htmlBody;

        // 3. Create email format (RFC 2822)
        const utf8Subject = `=?utf-8?B?${Buffer.from(cleanSubject).toString('base64')}?=`;
        
        const messageParts = [
            `From: me`,
            `To: ${cleanTo}`,
            `Subject: ${utf8Subject}`,
            'MIME-Version: 1.0',
            'Content-Type: text/html; charset=utf-8',
            '',
            finalBody
        ];
        const message = messageParts.join('\r\n');

        // 4. Base64URL encoding (required by Gmail API)
        const encodedMessage = Buffer.from(message).toString('base64url');

        // 5. Send!
        const res = await gmail.users.messages.send({
            userId: 'me',
            requestBody: {
                raw: encodedMessage
            }
        });

        return `Email successfully sent to ${to}.`;

    } catch (error) {
        if (error.response && error.response.data && error.response.data.error) {
            console.error('Gmail API detail:', JSON.stringify(error.response.data.error, null, 2));
        }
        console.error('Gmail Send Error:', error.message);
        return `Error sending email: ${error.message}`;
    }
}


module.exports = { fetchMails, sendMail };