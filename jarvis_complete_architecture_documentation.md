# architecture-explaining.md

# J.A.R.V.I.S. — Complete System Architecture Documentation

# 1. Introduction

J.A.R.V.I.S. is a full-stack AI assistant platform consisting of:

- Android mobile application
- Node.js / Express backend server
- PostgreSQL database
- OpenAI integration
- Google Workspace integrations
- Voice interaction system
- Vision analysis system
- File analysis system

The system is designed around modular components where each layer has a clearly defined responsibility.

---

# 2. System Overview

```text
Android Client
    ↓
REST API Requests (JSON / Multipart)
    ↓
Node.js Express Backend
    ↓
PostgreSQL Database
    ↓
External APIs
    ├── OpenAI
    └── Google APIs
```

The Android application acts as the client interface.

The backend handles:

- authentication
- conversations
- AI communication
- Google integrations
- file processing
- image processing
- persistent storage

---

# 3. Android Application Architecture

## Android Package Structure

```text
com.kosmidis.jarvis
│
├── activities
├── adapters
├── config
├── data
├── dialogs
├── managers
├── models
├── network
└── utils
```

---

# 4. Android Layers

## 4.1 Activities Layer

The Activities layer represents the visible screens of the application.

Responsibilities:

- UI initialization
- user interaction handling
- lifecycle management
- navigation
- communication with managers

### Activities

```text
LoginActivity
RegisterActivity
MainActivity
ProfileCustomisationActivity
```

---

## LoginActivity

Handles:

- user login UI
- email/password submission
- authentication requests
- session initialization

Flow:

```text
LoginActivity
    ↓
AuthApiManager
    ↓
Backend Authentication
    ↓
SessionManager
    ↓
MainActivity
```

---

## RegisterActivity

Handles:

- user registration UI
- registration requests
- account creation flow

---

## ProfileCustomisationActivity

Handles:

- profile editing
- loading user profile
- saving user profile

Profile data includes:

- first name
- last name
- age
- profession
- interests

---

## MainActivity

The MainActivity is the central coordinator of the Android application.

Responsibilities:

- chat interaction
- drawer navigation
- voice interaction
- attachment handling
- conversation handling
- Google integration UI
- RecyclerView coordination
- deep link handling

MainActivity communicates with:

- ChatUiManager
- VoiceManager
- VoiceUiManager
- ImageManager
- FileManager
- Conversation managers
- API managers

---

# 5. Android Managers Layer

Managers encapsulate reusable business/UI coordination logic.

---

## 5.1 ChatUiManager

Responsible for:

- managing message list UI
- adding messages
- removing typing indicators
- updating RecyclerView
- managing welcome layout visibility

Works with:

```text
RecyclerView
ChatAdapter
MessageModel
```

---

## 5.2 VoiceManager

Responsible for:

- speech recognition
- speech-to-text conversion
- text-to-speech synthesis
- microphone permission handling

Features:

- Greek speech recognition
- Greek voice synthesis
- callback-based architecture

Flow:

```text
User Voice
    ↓
SpeechRecognizer
    ↓
VoiceManager
    ↓
MainActivity
```

---

## 5.3 VoiceUiManager

Responsible for:

- voice overlay
- voice animations
- microphone listening feedback
- pulse animation effects

---

## 5.4 ImageManager

Responsible for:

- opening image picker
- storing selected image URIs
- clearing selected images
- image preview management

---

## 5.5 FileManager

Responsible for:

- opening document picker
- storing selected document URIs
- document selection handling
- file clearing

Supported files:

- PDF
- TXT
- DOCX

---

# 6. Android Models Layer

## MessageModel

Represents a chat message.

Properties:

```text
message
isUser
isTyping
imageUris
fileNames
```

Supports:

- text messages
- typing indicators
- image messages
- document messages

---

# 7. Android Adapter Layer

## ChatAdapter

Responsible for rendering chat messages inside RecyclerView.

Supports:

- user messages
- AI messages
- markdown rendering
- typing indicators
- image previews
- attachment previews

Uses:

```text
RecyclerView
Markwon
MessageModel
```

---

# 8. Android Dialog Layer

## SettingsMenuDialog

Provides:

- settings access
- logout access
- integrations access

---

## IntegrationsGoogle

Handles:

- Google OAuth connection
- permission selection
- integration UI
- disconnect actions

---

# 9. Android Network Layer

The network layer handles all communication with the backend.

## Network Managers

```text
AuthApiManager
ChatApiManager
VisionApiManager
FileApiManager
ProfileApiManager
GoogleIntegrationManager
```

---

## 9.1 AuthApiManager

Handles:

- login requests
- registration requests
- authentication response parsing

Endpoints:

```text
POST /api/login
POST /api/register
```

---

## 9.2 ChatApiManager

Handles:

- conversation retrieval
- conversation creation
- message retrieval
- text message sending

Endpoints:

```text
GET  /api/conversations
POST /api/conversations
GET  /api/messages/:conversationId
POST /api/chat
```

---

## 9.3 VisionApiManager

Handles:

- multipart image uploads
- AI vision requests

Endpoint:

```text
POST /api/chat/vision
```

---

## 9.4 FileApiManager

Handles:

- multipart document uploads
- AI file analysis requests

Endpoint:

```text
POST /api/chat/files
```

---

## 9.5 ProfileApiManager

Handles:

- loading profile data
- saving profile data

Endpoints:

```text
GET  /api/profile/:userId
POST /api/profile
```

---

## 9.6 GoogleIntegrationManager

Handles:

- Google integration status
- integration verification

---

# 10. Android Data Layer

## SessionManager

Responsible for local session persistence.

Stores:

```text
JWT token
userId
userEmail
Google connection status
```

Used by:

- authentication flow
- protected API calls
- session restoration

---

# 11. Android Utilities

## TextNormalizer

Responsible for:

- normalizing variations of “Jarvis”
- converting Greek pronunciation variants
- improving speech recognition consistency

Examples:

```text
jarvis
τζαρβις
τζάρβις
ζαρβις
```

All normalize to:

```text
J.A.R.V.I.S.
```

---

# 12. Backend Architecture

## Backend Structure

```text
backend
│
├── config
├── controllers
├── db
├── google_cloud
├── middleware
└── routes
```

---

# 13. Backend Configuration Layer

## 13.1 ai.js

Creates the OpenAI client.

Uses:

```text
AI_API_KEY
```

Provides:

```text
OpenAI API access
```

---

## 13.2 google.js

Creates the Google OAuth2 client.

Uses:

```text
GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET
GOOGLE_REDIRECT_URI
```

Provides:

```text
Google OAuth access
```

---

# 14. Database Layer

## database.js

Creates PostgreSQL connection pool.

Provides:

```js
query(text, params)
```

Used throughout backend services and controllers.

---

# 15. Middleware Layer

## auth.js

JWT authentication middleware.

Responsibilities:

- reading Bearer token
- verifying JWT
- attaching decoded user to request

Flow:

```text
Request
    ↓
JWT Verification
    ↓
req.user
    ↓
Protected Route
```

---

# 16. Backend Routes Layer

## 16.1 Authentication Routes

```text
POST /api/register
POST /api/login
```

Controller:

```text
authController
```

---

## 16.2 Chat Routes

```text
GET  /api/conversations/:userId
POST /api/conversations
GET  /api/messages/:conversationId
POST /api/chat
POST /api/chat/vision
POST /api/chat/files
```

Controller:

```text
chatController
```

---

## 16.3 Google Routes

```text
GET  /api/auth/google
GET  /api/auth/google/callback
GET  /api/users/:userId/integrations/google/status
POST /api/integrations/google/disconnect
```

Controller:

```text
googleController
```

---

## 16.4 Profile Routes

```text
GET  /api/profile/:userId
POST /api/profile
```

Controller:

```text
profileController
```

---

# 17. Backend Controllers

## 17.1 authController

Responsible for:

- registration
- login
- password hashing
- password verification
- JWT creation

Security:

- bcrypt password hashing
- JWT authentication

---

## 17.2 profileController

Responsible for:

- retrieving profile data
- updating profile data
- UPSERT operations on user_profiles

---

## 17.3 googleController

Responsible for:

- generating Google OAuth URLs
- handling OAuth callback
- storing Google tokens
- checking integration status
- disconnecting Google accounts

Flow:

```text
Android App
    ↓
Google OAuth
    ↓
Backend Callback
    ↓
Token Storage
    ↓
Deep Link Redirect
```

---

## 17.4 chatController

The central AI orchestration controller.

Responsible for:

- saving user messages
- loading conversation history
- generating AI responses
- handling OpenAI tool calls
- managing Google integrations
- handling image analysis
- handling document analysis
- saving AI responses

Supports:

- text chat
- image chat
- document chat
- tool-based AI actions

---

# 18. Google Cloud Integration Layer

## 18.1 auth.js

Responsible for:

- creating authenticated Google clients
- loading stored OAuth tokens
- refreshing expired tokens
- updating tokens automatically

---

## 18.2 calendar.js

Provides:

```text
getCalendarEvents
createCalendarEvent
deleteCalendarEvent
updateCalendarEvent
```

Supports:

- event retrieval
- event creation
- event deletion
- event updates

---

## 18.3 gmail.js

Provides:

```text
fetchMails
sendMail
```

Supports:

- reading Gmail messages
- retrieving metadata/snippets
- sending emails
- using Gmail signatures

---

## 18.4 drive.js

Provides:

```text
searchDriveFiles
readDriveFileContent
```

Supports:

- Google Drive search
- Google Docs reading
- Google Sheets export
- text file reading

---

## 18.5 sheets.js

Provides:

```text
readContacts
addContact
editContact
removeContact
```

Uses Google Sheets as:

```text
JARVIS_Contacts
```

Supports:

- contact management
- persistent cloud contacts

---

# 19. OpenAI Integration

The backend integrates with OpenAI using:

```text
gpt-4o-mini
```

Capabilities:

- natural conversation
- vision analysis
- document analysis
- tool calling
- multilingual responses
- contextual conversation history

The AI receives:

- system instructions
- conversation history
- user profile context
- current date/time context
- Google tool availability

---

# 20. Tool Calling Architecture

The backend dynamically exposes tools to OpenAI depending on Google permissions.

## Read Tools

```text
getCalendarEvents
fetchMails
readContacts
searchDriveFiles
readDriveFileContent
```

## Write Tools

```text
sendMail
createCalendarEvent
updateCalendarEvent
deleteCalendarEvent
addContact
```

Flow:

```text
User Request
    ↓
OpenAI Tool Selection
    ↓
Google Module Execution
    ↓
Tool Result
    ↓
Final AI Response
```

---

# 21. Vision Processing Architecture

Image messages are handled using multipart uploads.

Flow:

```text
Android Image Selection
    ↓
Multipart Upload
    ↓
Express + Multer
    ↓
Image Buffer Processing
    ↓
OpenAI Vision Request
    ↓
AI Response
```

Supported:

- multiple images
- image comparison
- image description
- image understanding

---

# 22. File Processing Architecture

Supported document types:

```text
PDF
TXT
DOCX
```

Libraries used:

```text
pdf-parse
mammoth
```

Flow:

```text
Android File Selection
    ↓
Multipart Upload
    ↓
File Parsing
    ↓
Extracted Text
    ↓
OpenAI Analysis
    ↓
AI Response
```

---

# 23. Voice System Architecture

The Android voice system supports:

- speech recognition
- speech-to-text
- text-to-speech
- animated listening feedback

Language:

```text
Greek (el-GR)
```

Flow:

```text
User Voice
    ↓
SpeechRecognizer
    ↓
Recognized Text
    ↓
AI Processing
    ↓
TextToSpeech
    ↓
Spoken AI Response
```

---

# 24. Database Architecture

## Core Tables

### users

Stores:

```text
id
email
password
```

---

### conversations

Stores:

```text
id
user_id
title
created_at
```

---

### messages

Stores:

```text
id
conversation_id
sender
message_text
created_at
```

---

### user_profiles

Stores:

```text
user_id
first_name
last_name
age
profession
interests
contacts_spreadsheet_id
updated_at
```

---

### user_integrations

Stores:

```text
user_id
provider
access_token
refresh_token
expiry_date
can_read
can_write
```

---

# 25. Security Architecture

## Authentication

Uses:

```text
JWT Authentication
```

Passwords are secured using:

```text
bcrypt hashing
```

---

## Google Security

Google OAuth tokens are stored server-side.

The Android app never directly accesses Google APIs.

All Google operations pass through the backend.

---

## Protected Routes

Protected routes use:

```text
JWT Middleware
```

Ensuring only authenticated users can access private data.

---

# 26. Conversation Architecture

Each conversation contains:

- unique conversation ID
- user ownership
- message history
- AI replies
- generated title

Conversation titles are automatically generated by OpenAI based on the first message.

---

# 27. Markdown Rendering

The Android app uses:

```text
Markwon
```

to render:

- markdown text
- code blocks
- formatting
- lists
- AI-generated structured responses

---

# 28. Deep Link Architecture

Google OAuth redirects back into Android using:

```text
jarvisapp://oauth2redirect
```

Handled by:

```text
MainActivity.handleDeepLink()
```

This enables seamless OAuth integration between browser and app.

---

# 29. System Characteristics

The architecture provides:

- modularity
- reusable components
- scalable backend structure
- AI extensibility
- Google Workspace integration
- multimedia support
- persistent conversations
- cloud-based user context
- voice interaction
- secure authentication

---

# 30. End-to-End Request Lifecycle

## Example: Standard AI Message

```text
User Message
    ↓
MainActivity
    ↓
ChatApiManager
    ↓
Express Backend
    ↓
JWT Verification
    ↓
chatController
    ↓
Conversation History
    ↓
OpenAI
    ↓
Optional Tool Calls
    ↓
AI Response
    ↓
Database Storage
    ↓
Android UI Update
```

---

# 31. Complete Technology Stack

## Android

```text
Java
Android SDK
RecyclerView
Markwon
OkHttp
SpeechRecognizer
TextToSpeech
```

---

## Backend

```text
Node.js
Express.js
PostgreSQL
JWT
bcrypt
multer
```

---

## AI

```text
OpenAI GPT-4o-mini
```

---

## Google APIs

```text
Google OAuth2
Google Calendar API
Gmail API
Google Drive API
Google Sheets API
Google Docs API
```

---

# 32. Final Architecture Summary

J.A.R.V.I.S. is a modular AI assistant platform built around:

- Android frontend
- Node.js backend
- OpenAI intelligence layer
- Google Workspace integration layer
- PostgreSQL persistence layer

The system supports:

- conversational AI
- multimodal AI
- cloud integrations
- voice interaction
- persistent memory
- document understanding
- calendar/email productivity workflows

The architecture separates responsibilities across:

- UI
- managers
- networking
- backend controllers
- cloud integrations
- AI orchestration
- persistent storage

resulting in a scalable and maintainable full-stack AI assistant platform.

