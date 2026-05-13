const { google } = require('googleapis');
const { getGoogleOAuthClient } = require('./auth');

/**
 * Searches for files in Google Drive.
 */
async function searchDriveFiles(db, userId, query) {
    const auth = await getGoogleOAuthClient(db, userId);
    const drive = google.drive({ version: 'v3', auth });

    try {
        const res = await drive.files.list({
            q: `name contains '${query}' or fullText contains '${query}' and trashed = false`,
            fields: 'files(id, name, mimeType, modifiedTime)',
            pageSize: 5
        });

        if (res.data.files.length === 0) return "No files found matching your request.";

        return JSON.stringify(res.data.files.map(f => ({
            name: f.name,
            id: f.id,
            type: f.mimeType,
            lastModified: f.modifiedTime
        })));
    } catch (error) {
        console.error('Drive Search Error:', error);
        return "Error searching for files in Google Drive.";
    }
}

/**
 * Reads the content of a Google Doc or a text file.
 */
async function readDriveFileContent(db, userId, fileId, mimeType) {
    const auth = await getGoogleOAuthClient(db, userId);
    const drive = google.drive({ version: 'v3', auth });
    const docs = google.docs({ version: 'v1', auth });

    try {
        if (mimeType === 'application/vnd.google-apps.document') {
            // GOOGLE DOCS: Use Docs API
            const doc = await docs.documents.get({ documentId: fileId });
            let content = "";
            doc.data.body.content.forEach(el => {
                if (el.paragraph) {
                    el.paragraph.elements.forEach(run => {
                        if (run.textRun) content += run.textRun.content;
                    });
                }
            });
            return content;

        } else if (mimeType === 'application/vnd.google-apps.spreadsheet') {
            // GOOGLE SHEETS: Export to CSV so the AI can read it as structured text
            const res = await drive.files.export({
                fileId: fileId,
                mimeType: 'text/csv'
            });
            return res.data;

        } else if (mimeType.includes('pdf')) {
            // PDF: For now, we return a message (unless we add OCR later)
            return "This is a PDF file. I can see it exists, but I need OCR capabilities to read its content.";

        } else {
            // BINARY FILES (txt, json, code): Download as media
            const res = await drive.files.get({ 
                fileId: fileId, 
                alt: 'media' 
            });
            
            if (typeof res.data === 'object') {
                return JSON.stringify(res.data);
            }
            return res.data;
        }
    } catch (error) {
        console.error('Drive Read Error:', error.message);
        return `Error reading file content: ${error.message}`;
    }
}

module.exports = { searchDriveFiles, readDriveFileContent };