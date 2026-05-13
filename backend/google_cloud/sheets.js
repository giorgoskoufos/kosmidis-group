const { google } = require('googleapis');
const { getGoogleOAuthClient } = require('./auth');

async function getOrSyncContactsSheet(db, userId) {
    const auth = await getGoogleOAuthClient(db, userId);
    const sheets = google.sheets({ version: 'v4', auth });
    const drive = google.drive({ version: 'v3', auth });

    // 1. First, check if we have it in our DB
    const dbRes = await db.query('SELECT contacts_spreadsheet_id FROM user_profiles WHERE user_id = $1', [userId]);
    let sheetId = dbRes.rows[0]?.contacts_spreadsheet_id;

    if (sheetId) {
        console.log(`[Database] Found existing Sheet ID: ${sheetId}`);
        return sheetId;
    }

    // 2. If not in DB, search Google Drive by title
    console.log(`[Drive] Searching for 'JARVIS_Contacts'...`);
    const driveRes = await drive.files.list({
        q: "name = 'JARVIS_Contacts' and mimeType = 'application/vnd.google-apps.spreadsheet' and trashed = false",
        fields: 'files(id, name)',
    });

    if (driveRes.data.files.length > 0) {
        sheetId = driveRes.data.files[0].id;
        console.log(`[Drive] Found existing file on Drive: ${sheetId}`);
    } else {
        // 3. If not found on Drive, create a new one
        console.log(`[Drive] No file found. Creating new 'JARVIS_Contacts' spreadsheet...`);
        const resource = {
            properties: { title: 'JARVIS_Contacts' },
            sheets: [{ properties: { title: 'Contacts' } }]
        };
        const spreadsheet = await sheets.spreadsheets.create({
            resource,
            fields: 'spreadsheetId',
        });
        sheetId = spreadsheet.data.spreadsheetId;

        // Initialize Headers
        await sheets.spreadsheets.values.update({
            spreadsheetId: sheetId,
            range: 'Contacts!A1:D1',
            valueInputOption: 'RAW',
            resource: { values: [['Full Name', 'Email', 'Phone', 'Category']] }
        });
    }

    // 4. CRITICAL: Update the database (Insert if not exists, Update if exists)
    try { // Update the database
        const updateRes = await db.query(
            `INSERT INTO user_profiles (user_id, contacts_spreadsheet_id) 
             VALUES ($2, $1) 
             ON CONFLICT (user_id) 
             DO UPDATE SET contacts_spreadsheet_id = EXCLUDED.contacts_spreadsheet_id`,
            [sheetId, userId]
        );
        console.log(`[Database] Successfully saved Sheet ID to user ${userId}.`);
    } catch (dbErr) {
        console.error(`[Database] Failed to save Sheet ID:`, dbErr);
    }

    return sheetId;
}

// Tool to read contacts
async function readContacts(db, userId) {
    const auth = await getGoogleOAuthClient(db, userId);
    const sheets = google.sheets({ version: 'v4', auth });
    const spreadsheetId = await getOrSyncContactsSheet(db, userId);

    try {
        const response = await sheets.spreadsheets.values.get({
            spreadsheetId,
            range: 'Contacts!A2:D100', // Use the explicit tab name
        });

        const rows = response.data.values;
        if (!rows || rows.length === 0) return "The contact agenda is empty.";

        return JSON.stringify(rows.map(row => ({
            name: row[0],
            email: row[1],
            phone: row[2],
            category: row[3]
        })));
    } catch (error) {
        console.error('Google Sheets Error:', error);
        return "Error fetching contacts from Google Sheets.";
    }
}

// Tool to add a new contact
async function addContact(db, userId, name, email, phone = "", category = "") {
    const auth = await getGoogleOAuthClient(db, userId);
    const sheets = google.sheets({ version: 'v4', auth });
    const spreadsheetId = await getOrSyncContactsSheet(db, userId);

    try {
        await sheets.spreadsheets.values.append({
            spreadsheetId,
            range: 'Contacts!A:D', // Use the explicit tab name
            valueInputOption: 'USER_ENTERED', 
            resource: {
                values: [[name, email, phone, category]]
            }
        });
        
        return `Successfully added contact: ${name} (${email}) to the agenda.`;
    } catch (error) {
        console.error('Google Sheets Append Error:', error);
        return "Error adding contact to Google Sheets.";
    }
}

// Tool to edit an existing contact
async function editContact(db, userId, searchName, newName, newEmail, newPhone = "", newCategory = "") {
    const auth = await getGoogleOAuthClient(db, userId);
    const sheets = google.sheets({ version: 'v4', auth });
    const spreadsheetId = await getOrSyncContactsSheet(db, userId);

    try {
        // 1. Read all rows to find the target contact
        const response = await sheets.spreadsheets.values.get({
            spreadsheetId,
            range: 'Contacts!A:D',
        });
        const rows = response.data.values;
        if (!rows || rows.length === 0) return "No contacts found in the agenda.";

        // 2. Find the row index (case-insensitive search)
        const rowIndex = rows.findIndex(row => row[0] && row[0].toLowerCase() === searchName.toLowerCase());
        if (rowIndex === -1) return `Contact with name '${searchName}' was not found.`;

        // 3. Update the specific row (Google Sheets ranges are 1-based, so array index 1 is row 2)
        const rowNumber = rowIndex + 1;
        
        // If the AI didn't provide a new value for a field, keep the old one
        const updatedRow = [
            newName || rows[rowIndex][0],
            newEmail || rows[rowIndex][1] || "",
            newPhone || rows[rowIndex][2] || "",
            newCategory || rows[rowIndex][3] || ""
        ];

        await sheets.spreadsheets.values.update({
            spreadsheetId,
            range: `Contacts!A${rowNumber}:D${rowNumber}`,
            valueInputOption: 'USER_ENTERED',
            resource: { values: [updatedRow] }
        });
        
        return `Successfully updated contact: ${searchName}.`;
    } catch (error) {
        console.error('Google Sheets Edit Error:', error);
        return "Error editing contact in Google Sheets.";
    }
}

// Tool to completely remove a contact
async function removeContact(db, userId, searchName) {
    const auth = await getGoogleOAuthClient(db, userId);
    const sheets = google.sheets({ version: 'v4', auth });
    const spreadsheetId = await getOrSyncContactsSheet(db, userId);

    try {
        // 1. Find the row index first
        const response = await sheets.spreadsheets.values.get({
            spreadsheetId,
            range: 'Contacts!A:D',
        });
        const rows = response.data.values;
        if (!rows || rows.length === 0) return "No contacts found in the agenda.";

        const rowIndex = rows.findIndex(row => row[0] && row[0].toLowerCase() === searchName.toLowerCase());
        if (rowIndex === -1) return `Contact with name '${searchName}' was not found.`;

        // 2. To DELETE a row, we need the specific sheetId of the "Contacts" tab
        const sheetRes = await sheets.spreadsheets.get({ spreadsheetId });
        const sheet = sheetRes.data.sheets.find(s => s.properties.title === 'Contacts');
        if (!sheet) return "Error: Contacts tab not found in the spreadsheet.";
        const tabId = sheet.properties.sheetId;

        // 3. Delete the dimension (row)
        await sheets.spreadsheets.batchUpdate({
            spreadsheetId,
            resource: {
                requests: [{
                    deleteDimension: {
                        range: {
                            sheetId: tabId,
                            dimension: "ROWS",
                            startIndex: rowIndex,     // 0-based index, inclusive
                            endIndex: rowIndex + 1    // 0-based index, exclusive
                        }
                    }
                }]
            }
        });
        
        return `Successfully deleted contact: ${searchName} from the agenda.`;
    } catch (error) {
        console.error('Google Sheets Delete Error:', error);
        return "Error removing contact from Google Sheets.";
    }
}

// IMPORTANT: Update your exports at the very bottom!
module.exports = { readContacts, addContact, editContact, removeContact };