const { google } = require('googleapis');
const { getGoogleOAuthClient } = require('./auth');

async function getCalendarEvents(db, userId, timeMin, timeMax, query, maxResults = 10) {
    const auth = await getGoogleOAuthClient(db, userId);
    if (!auth) return "Error: User has not connected their Google account yet.";

    const calendar = google.calendar({ version: 'v3', auth });

    try {
        const res = await calendar.events.list({
            calendarId: 'primary',
            // If the AI doesn't provide a timeMin, default to right now
            timeMin: timeMin || new Date().toISOString(),
            timeMax: timeMax,
            q: query, // Search keyword (e.g., "Meeting", "Dentist")
            maxResults: maxResults,
            singleEvents: true,
            orderBy: 'startTime',
        });
        
        const events = res.data.items;
        if (!events || events.length === 0) {
            return "No events found for the specified criteria.";
        }
        
        // Return a clean JSON array for the AI to read easily
        return JSON.stringify(events.map(e => ({
            id: e.id,
            title: e.summary,
            start: e.start.dateTime || e.start.date, // handles all-day events too
            end: e.end.dateTime || e.end.date,
            location: e.location || 'No location'
        })));
        
    } catch (error) {
        console.error('Google Calendar Error:', error);
        return "Error fetching calendar data from Google.";
    }
}


async function createCalendarEvent(db, userId, summary, start, end, location = "", description = "") {
    const auth = await getGoogleOAuthClient(db, userId);
    if (!auth) return "Error: User has not connected their Google account yet.";
    const calendar = google.calendar({ version: 'v3', auth });

    try {
        const event = {
            summary,
            location,
            description,
            start: { dateTime: start, timeZone: 'Europe/Athens' },
            end: { dateTime: end, timeZone: 'Europe/Athens' },
        };

        const res = await calendar.events.insert({
            calendarId: 'primary',
            resource: event,
        });

        return `Event created successfully: ${res.data.htmlLink}`;
    } catch (error) {
        console.error('Calendar Create Error:', error);
        return "Error creating the calendar event.";
    }
}

async function deleteCalendarEvent(db, userId, eventId) {
    const auth = await getGoogleOAuthClient(db, userId);
    if (!auth) return "Error: User has not connected their Google account yet.";
    const calendar = google.calendar({ version: 'v3', auth });

    try {
        await calendar.events.delete({
            calendarId: 'primary',
            eventId: eventId,
        });
        return "Event deleted successfully.";
    } catch (error) {
        console.error('Calendar Delete Error:', error);
        return "Error deleting the event. Make sure the event ID is correct.";
    }
}

async function updateCalendarEvent(db, userId, eventId, updates) {
    const auth = await getGoogleOAuthClient(db, userId);
    if (!auth) return "Error: User has not connected their Google account yet.";
    const calendar = google.calendar({ version: 'v3', auth });

    try {
        // updates is an object that can contain: summary, start, end, location, etc.
        const res = await calendar.events.patch({
            calendarId: 'primary',
            eventId: eventId,
            resource: updates,
        });
        return `Event updated successfully: ${res.data.summary}`;
    } catch (error) {
        console.error('Calendar Update Error:', error);
        return "Error updating the calendar event.";
    }
}

// Update your exports!
module.exports = { getCalendarEvents, createCalendarEvent, deleteCalendarEvent, updateCalendarEvent };