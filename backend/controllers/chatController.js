const db = require('../db/database');
const openai = require('../config/ai');
const pdfParse = require('pdf-parse');
const mammoth = require('mammoth');
const { getCalendarEvents, createCalendarEvent, deleteCalendarEvent, updateCalendarEvent } = require('../google_cloud/calendar');
const { fetchMails, sendMail } = require('../google_cloud/gmail');
const { readContacts, addContact, editContact, removeContact } = require('../google_cloud/sheets');
const { searchDriveFiles, readDriveFileContent } = require('../google_cloud/drive');

// 1. Primary Chat Logic (The Brain)
exports.sendMessage = async (req, res) => {
    const { conversationId, message } = req.body;

    if (!conversationId || !message) {
        return res.status(400).json({ error: 'Missing conversationId or message' });
    }

    try {
        await db.query(
            'INSERT INTO messages (conversation_id, sender, message_text) VALUES ($1, $2, $3)',
            [conversationId, 'user', message]
        );

        const historyQuery = await db.query(
            `SELECT sender, message_text FROM 
               (SELECT * FROM messages WHERE conversation_id = $1 ORDER BY created_at DESC LIMIT 15) sub
             ORDER BY created_at ASC`,
            [conversationId]
        );

        if (historyQuery.rows.length === 1) {
            openai.chat.completions.create({
                model: "gpt-4o-mini",
                messages: [
                    {
                        role: "system",
                        content: "Generate a very short title (maximum 3-4 words, without quotes or trailing periods) for a conversation that starts with the following message. The title must be in English."
                    },
                    { role: "user", content: message }
                ]
            }).then(async (titleRes) => {
                let newTitle = titleRes.choices[0].message.content.trim().replace(/^["']|["']$/g, '');
                await db.query('UPDATE conversations SET title = $1 WHERE id = $2', [newTitle, conversationId]);
            }).catch(err => console.error("Error generating title:", err));
        }

        const userQuery = await db.query('SELECT user_id FROM conversations WHERE id = $1', [conversationId]);
        if (!userQuery.rows.length) {
            return res.status(404).json({ error: 'Conversation not found.' });
        }
        const userId = userQuery.rows[0].user_id;
        if (userId !== req.user.userId) {
            return res.status(403).json({ error: 'Forbidden.' });
        }

        const integrationQuery = await db.query(
            'SELECT can_read, can_write FROM user_integrations WHERE user_id = $1 AND provider = $2',
            [userId, 'google']
        );

        const perms = integrationQuery.rows[0] || { can_read: false, can_write: false };

        const profileQuery = await db.query('SELECT * FROM user_profiles WHERE user_id = $1', [userId]);

        let userContext = "";

        if (profileQuery.rows.length > 0) {
            const p = profileQuery.rows[0];

            userContext = `\n\nUser Profile Information:\n`;

            if (p.first_name) userContext += `- Name: ${p.first_name} ${p.last_name || ''}\n`;
            if (p.age) userContext += `- Age: ${p.age}\n`;
            if (p.profession) userContext += `- Profession: ${p.profession}\n`;
            if (p.interests) userContext += `- Interests: ${p.interests}\n`;

            userContext += `\nPlease personalize your responses based on the user's profile.`;
        }

        const currentDateTime = new Date().toLocaleString('en-US', { timeZone: 'Europe/Athens' });

        const timeContext = `\n\nCRITICAL DATA: The current real-world date and time is ${currentDateTime}. Use this as the baseline for relative dates (today, tomorrow, etc.).`;

        const aiMessages = [
            {
                role: "system",
                content:
                    "You are J.A.R.V.I.S., an advanced AI assistant integrated with external APIs. " +
                    "CRITICAL: Always be 100% transparent about the results of your actions. " +
                    "If a tool (like sendMail) returns an error, do NOT claim success. Instead, explain the error to the user and ask for clarification. " +
                    "Do not make assumptions about the outcome of an operation before the tool actually returns a result. " +
                    "Always respond in the exact same language as the user input." +
                    userContext +
                    timeContext
            }
        ];

        historyQuery.rows.forEach(row => {
            aiMessages.push({
                role: row.sender === 'ai' || row.sender === 'assistant' ? 'assistant' : 'user',
                content: row.message_text
            });
        });

        let availableTools = [];

        if (perms.can_read) {
            availableTools.push(
                {
                    type: "function",
                    function: {
                        name: "getCalendarEvents",
                        description: "Retrieves the user's Google Calendar events for a specific time range.",
                        parameters: {
                            type: "object",
                            properties: {
                                timeMin: { type: "string", description: "Start time in ISO format." },
                                timeMax: { type: "string", description: "End time in ISO format." },
                                query: { type: "string", description: "Optional search keyword." }
                            }
                        }
                    }
                },
                {
                    type: "function",
                    function: {
                        name: "fetchMails",
                        description: "Fetch recent emails from the user's Gmail account.",
                        parameters: {
                            type: "object",
                            properties: {
                                query: { type: "string" },
                                maxResults: { type: "integer" }
                            }
                        }
                    }
                },
                {
                    type: "function",
                    function: {
                        name: "readContacts",
                        description: "Reads the user's contact agenda from their dedicated Google Sheet."
                    }
                },
                {
                    type: "function",
                    function: {
                        name: "searchDriveFiles",
                        description: "Searches for files in the user's Google Drive.",
                        parameters: {
                            type: "object",
                            properties: {
                                query: { type: "string" }
                            },
                            required: ["query"]
                        }
                    }
                },
                {
                    type: "function",
                    function: {
                        name: "readDriveFileContent",
                        description: "Reads content of a Google Doc or text file.",
                        parameters: {
                            type: "object",
                            properties: {
                                fileId: { type: "string" },
                                mimeType: { type: "string" }
                            },
                            required: ["fileId", "mimeType"]
                        }
                    }
                }
            );
        }

        if (perms.can_write) {
            availableTools.push(
                {
                    type: "function",
                    function: {
                        name: "sendMail",
                        description: "Sends an email from the user's Gmail account. The 'to' field MUST be a valid email address. If the user provides a name instead of an email, use 'readContacts' first to find the correct email address.",
                        parameters: {
                            type: "object",
                            properties: {
                                to: { type: "string", description: "The recipient's email address (e.g., 'name@example.com')." },
                                subject: { type: "string" },
                                body: { type: "string" }
                            },
                            required: ["to", "subject", "body"]
                        }
                    }
                },
                {
                    type: "function",
                    function: {
                        name: "createCalendarEvent",
                        description: "Creates a new event in the user's Google Calendar.",
                        parameters: {
                            type: "object",
                            properties: {
                                summary: { type: "string" },
                                start: { type: "string" },
                                end: { type: "string" }
                            },
                            required: ["summary", "start", "end"]
                        }
                    }
                },
                {
                    type: "function",
                    function: {
                        name: "updateCalendarEvent",
                        description: "Updates an existing event.",
                        parameters: {
                            type: "object",
                            properties: {
                                eventId: { type: "string" },
                                updates: { type: "object" }
                            },
                            required: ["eventId", "updates"]
                        }
                    }
                },
                {
                    type: "function",
                    function: {
                        name: "deleteCalendarEvent",
                        description: "Deletes a specific event.",
                        parameters: {
                            type: "object",
                            properties: {
                                eventId: { type: "string" }
                            },
                            required: ["eventId"]
                        }
                    }
                },
                {
                    type: "function",
                    function: {
                        name: "addContact",
                        description: "Adds a new contact to Google Sheets.",
                        parameters: {
                            type: "object",
                            properties: {
                                name: { type: "string" },
                                email: { type: "string" }
                            },
                            required: ["name", "email"]
                        }
                    }
                },
                {
                    type: "function",
                    function: {
                        name: "editContact",
                        description: "Edits an existing contact in the user's Google Sheets contact list by searching for their current name.",
                        parameters: {
                            type: "object",
                            properties: {
                                searchName: { type: "string", description: "The current full name of the contact to find." },
                                newName: { type: "string", description: "The new name (optional)." },
                                newEmail: { type: "string", description: "The new email (optional)." },
                                newPhone: { type: "string", description: "The new phone number (optional)." },
                                newCategory: { type: "string", description: "The new category (optional)." }
                            },
                            required: ["searchName"]
                        }
                    }
                },
                {
                    type: "function",
                    function: {
                        name: "removeContact",
                        description: "Permanently removes a contact from the user's Google Sheets contact list by name.",
                        parameters: {
                            type: "object",
                            properties: {
                                searchName: { type: "string", description: "The full name of the contact to remove." }
                            },
                            required: ["searchName"]
                        }
                    }
                }
            );
        }

        const chatOptions = {
            model: "gpt-4o-mini",
            messages: aiMessages,
            ...(availableTools.length > 0 && {
                tools: availableTools,
                tool_choice: "auto"
            })
        };

        let completion = await openai.chat.completions.create(chatOptions);
        let responseMessage = completion.choices[0].message;

        if (responseMessage.tool_calls) {
            aiMessages.push(responseMessage);

            for (const toolCall of responseMessage.tool_calls) {
                const functionName = toolCall.function.name;
                const args = JSON.parse(toolCall.function.arguments);

                let toolResult = "";

                try {
                    switch (functionName) {
                        case "getCalendarEvents":
                            toolResult = await getCalendarEvents(db, userId, args.timeMin, args.timeMax, args.query);
                            break;

                        case "fetchMails":
                            toolResult = await fetchMails(db, userId, args.query, args.maxResults || 5);
                            break;

                        case "readContacts":
                            toolResult = await readContacts(db, userId);
                            break;

                        case "searchDriveFiles":
                            toolResult = await searchDriveFiles(db, userId, args.query);
                            break;

                        case "readDriveFileContent":
                            toolResult = await readDriveFileContent(db, userId, args.fileId, args.mimeType);
                            break;

                        case "sendMail":
                            toolResult = await sendMail(db, userId, args.to, args.subject, args.body);
                            break;

                        case "createCalendarEvent":
                            toolResult = await createCalendarEvent(db, userId, args.summary, args.start, args.end);
                            break;

                        case "deleteCalendarEvent":
                            toolResult = await deleteCalendarEvent(db, userId, args.eventId);
                            break;

                        case "updateCalendarEvent":
                            toolResult = await updateCalendarEvent(db, userId, args.eventId, args.updates);
                            break;

                        case "addContact":
                            toolResult = await addContact(db, userId, args.name, args.email);
                            break;

                        case "editContact":
                            toolResult = await editContact(db, userId, args.searchName, args.newName, args.newEmail, args.newPhone, args.newCategory);
                            break;

                        case "removeContact":
                            toolResult = await removeContact(db, userId, args.searchName);
                            break;

                        default:
                            toolResult = `Error: Tool not implemented.`;
                    }
                } catch (err) {
                    toolResult = `Execution error: ${err.message}`;
                }

                aiMessages.push({
                    role: "tool",
                    tool_call_id: toolCall.id,
                    content: typeof toolResult === 'string' ? toolResult : JSON.stringify(toolResult)
                });
            }

            completion = await openai.chat.completions.create({
                model: "gpt-4o-mini",
                messages: aiMessages
            });

            responseMessage = completion.choices[0].message;
        }

        const aiResponseText = responseMessage.content;

        await db.query(
            'INSERT INTO messages (conversation_id, sender, message_text) VALUES ($1, $2, $3)',
            [conversationId, 'ai', aiResponseText]
        );

        res.json({ reply: aiResponseText });

    } catch (error) {
        console.error("Error in sendMessage:", error);
        res.status(500).json({ error: 'Internal Server Error' });
    }
};

// 2. Vision Chat Logic - Handles uploaded images
exports.sendVisionMessage = async (req, res) => {
    const { conversationId, message } = req.body;

    if (!conversationId || !req.files || req.files.length === 0) {
        return res.status(400).json({
            error: 'Missing images or conversationId'
        });
    }

    try {
        const userText = message || "Περιέγραψε αυτές τις εικόνες.";

        await db.query(
            'INSERT INTO messages (conversation_id, sender, message_text) VALUES ($1, $2, $3)',
            [conversationId, 'user', userText]
        );

        const imageContents = req.files.slice(0, 5).map(file => ({
            type: "image_url",
            image_url: {
                url: `data:${file.mimetype};base64,${file.buffer.toString('base64')}`
            }
        }));

        const completion = await openai.chat.completions.create({
            model: "gpt-4o-mini",
            messages: [
                {
                    role: "system",
                    content:
                        "You are J.A.R.V.I.S., an advanced AI assistant. " +
                        "Analyze all uploaded images carefully. " +
                        "If there are multiple images, compare or describe each one clearly. " +
                        "Always answer in the same language as the user."
                },
                {
                    role: "user",
                    content: [
                        {
                            type: "text",
                            text: userText
                        },
                        ...imageContents
                    ]
                }
            ],
            max_tokens: 900
        });

        const aiReply = completion.choices[0].message.content;

        await db.query(
            'INSERT INTO messages (conversation_id, sender, message_text) VALUES ($1, $2, $3)',
            [conversationId, 'ai', aiReply]
        );

        res.json({ reply: aiReply });

    } catch (error) {
        console.error("Vision Error:", error);
        res.status(500).json({
            error: 'Vision processing failed'
        });
    }
};

// 3. File Chat Logic - Handles uploaded documents
exports.sendFileMessage = async (req, res) => {
    const { conversationId, message } = req.body;

    if (!conversationId || !req.files || req.files.length === 0) {
        return res.status(400).json({ error: 'Missing conversationId or files' });
    }

    try {
        const userText = message && message.trim()
            ? message.trim()
            : "Ανάλυσε αυτά τα αρχεία.";

        await db.query(
            'INSERT INTO messages (conversation_id, sender, message_text) VALUES ($1, $2, $3)',
            [conversationId, 'user', userText]
        );

        let extractedText = "";

        for (const file of req.files.slice(0, 2)) {
            let content = "";
            const fileName = file.originalname.toLowerCase();
            if (
                file.mimetype === "application/pdf" ||
                fileName.endsWith(".pdf")
            ) {
                try {
                    const pdfData = await pdfParse(file.buffer);
                    content = pdfData.text || "";
                } catch (err) {
                    console.error("PDF Parse Error:", err);
                    content = "[Failed to read PDF file]";
                }
            }
            else if (
                file.mimetype === "text/plain" ||
                fileName.endsWith(".txt")
            ) {
                content = file.buffer.toString("utf8");
            }
            else if (
                file.mimetype === "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ||
                fileName.endsWith(".docx")
            ) {
                try {
                    const docxData = await mammoth.extractRawText({
                        buffer: file.buffer
                    });
                    content = docxData.value || "";
                } catch (err) {
                    console.error("DOCX Parse Error:", err);
                    content = "[Failed to read DOCX file]";
                }
            }
            else {
                content = `[Unsupported file type: ${file.originalname}]`;

            }
            extractedText +=
                `\n\n--- FILE: ${file.originalname} ---\n${content}`;
        }

        const completion = await openai.chat.completions.create({
            model: "gpt-4o-mini",
            messages: [
                {
                    role: "system",
                    content:
                        "You are J.A.R.V.I.S. Analyze uploaded documents carefully. " +
                        "Answer in the same language as the user. " +
                        "If multiple documents are uploaded, clearly separate findings per document."
                },
                {
                    role: "user",
                    content:
                        `${userText}\n\nUploaded document contents:\n${extractedText}`
                }
            ],
            max_tokens: 1200
        });

        const aiReply = completion.choices[0].message.content;

        await db.query(
            'INSERT INTO messages (conversation_id, sender, message_text) VALUES ($1, $2, $3)',
            [conversationId, 'ai', aiReply]
        );

        res.json({ reply: aiReply });

    } catch (error) {
        console.error("File Analysis Error:", error);
        res.status(500).json({ error: 'File analysis failed' });
    }
};

// 4. Helper Getters
exports.getConversations = async (req, res) => {
    const userId = req.user.userId;

    try {
        const result = await db.query(
            'SELECT id, title FROM conversations WHERE user_id = $1 ORDER BY created_at DESC',
            [userId]
        );

        res.json(result.rows);

    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.createConversation = async (req, res) => {
    const userId = req.user.userId;
    const { title } = req.body;

    try {
        const result = await db.query(
            'INSERT INTO conversations (user_id, title) VALUES ($1, $2) RETURNING id, title',
            [userId, title || 'New Conversation']
        );

        res.json(result.rows[0]);

    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.getMessages = async (req, res) => {
    const { conversationId } = req.params;

    try {
        // Ownership check — ensure the conversation belongs to the requesting user
        const ownerCheck = await db.query(
            'SELECT user_id FROM conversations WHERE id = $1',
            [conversationId]
        );
        if (!ownerCheck.rows.length) {
            return res.status(404).json({ error: 'Conversation not found.' });
        }
        if (ownerCheck.rows[0].user_id !== req.user.userId) {
            return res.status(403).json({ error: 'Forbidden.' });
        }

        const result = await db.query(
            'SELECT sender, message_text, created_at FROM messages WHERE conversation_id = $1 ORDER BY created_at ASC',
            [conversationId]
        );

        res.json(result.rows);

    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};