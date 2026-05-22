# J.A.R.V.I.S. Assistant

**Just A Rather Very Intelligent System** — A full-stack AI assistant platform that bridges Large Language Models with Google Workspace productivity. J.A.R.V.I.S. lets you manage your calendar, inbox, contacts, and files through natural language via a native Android client.

---

## 📌 Reference Repository

> **Source**: [https://github.com/giorgoskoufos/kosmidis-group.git](https://github.com/giorgoskoufos/kosmidis-group.git)

---

## 🚀 Key Features

### 🧠 Contextual Intelligence
- **Sliding Window History**: 15-message rolling context — efficient without losing thread.
- **Profile Injection**: AI is aware of the user's profession, age, and interests for tailored responses.
- **Time Awareness**: Real-world time injected at every request (Europe/Athens timezone).
- **Static System Priority**: Hardcoded personality and security boundaries override any conversation history.

### 📬 Signature-Aware Gmail
- Automatically fetches your primary Gmail signature via the Settings API.
- All outbound emails are RFC 2822–compliant (Base64URL encoded).

### 🗂️ Self-Healing CRM (Google Sheets)
- **Auto-Provisioning**: If the `JARVIS_Contacts` spreadsheet is missing, J.A.R.V.I.S. creates and persists it to your Drive automatically.
- **Smart Contact Resolution**: Resolves names (e.g. "email George") to addresses from the sheet before sending.

### 🛠️ Dynamic Tool Calling
- The AI tool set is dynamically constructed per request based on the user's active Google integration status.
- Supports multi-step tool calling (OpenAI Function Calling).

### 🖼️ Vision & Document Analysis
- Send images (JPEG/PNG) for visual analysis via the OpenAI vision API.
- Upload documents (PDF, TXT, DOCX) for AI-powered content extraction and Q&A.

### 🎙️ Voice Interface (Greek)
- Full speech-to-text and text-to-speech in Greek (`el-GR`).
- Animated listening feedback with pulse effects.
- Automatic normalization of "Jarvis" voice variants (Greek/English) → `J.A.R.V.I.S.`

---

## 🛠️ Technical Stack

| Component | Technology | Version |
| :--- | :--- | :--- |
| **Backend** | Node.js / Express | Express 5.x |
| **AI Engine** | OpenAI SDK | `openai` ^6.33, `gpt-4o-mini` |
| **Database** | PostgreSQL | via `pg` ^8.20 |
| **Auth** | JWT + bcrypt | `jsonwebtoken` ^9, `bcrypt` ^6 |
| **Google APIs** | googleapis | ^171.4 |
| **File Parsing** | pdf-parse + mammoth | PDF, TXT, DOCX |
| **File Uploads** | multer | ^2.1 |
| **Rate Limiting** | express-rate-limit | ^8.3 |
| **Mobile** | Native Android (Java) | Material Design + Markwon |

---

## 🔒 Security & Performance

- **Stateless Sessions**: JWT-based — all protected routes require a Bearer token verified by `middleware/auth.js`.
- **Hardened Data Access**: `req.user` is always derived from the JWT. Client-supplied userIds are ignored.
- **Rate Limiting**: 100 requests / 15 min per IP via `express-rate-limit`.
- **Google Token Safety**: OAuth tokens stored server-side in `user_integrations` — the client never sees them directly.
- **Password Security**: bcrypt hashing on all stored passwords.

---

## 📡 API Reference

### Authentication
```
POST /api/register
POST /api/login
```

### Chat
```
GET  /api/conversations/:userId      → list conversations
POST /api/conversations              → create conversation
GET  /api/messages/:conversationId   → load messages
POST /api/chat                       → text chat (tool-calling enabled)
POST /api/chat/vision                → image chat (multipart)
POST /api/chat/files                 → document chat (multipart)
```

### Profile
```
GET  /api/profile/:userId
POST /api/profile
```

### Google Integration
```
GET  /api/auth/google                            → initiate OAuth
GET  /api/auth/google/callback                   → OAuth callback
GET  /api/users/:userId/integrations/google/status
POST /api/integrations/google/disconnect
```

---

## 🔧 Available AI Tools (Google Workspace)

| Tool | Type | API |
|------|------|-----|
| `getCalendarEvents` | Read | Google Calendar |
| `createCalendarEvent` | Write | Google Calendar |
| `updateCalendarEvent` | Write | Google Calendar |
| `deleteCalendarEvent` | Write | Google Calendar |
| `fetchMails` | Read | Gmail |
| `sendMail` | Write | Gmail |
| `searchDriveFiles` | Read | Google Drive |
| `readDriveFileContent` | Read | Google Drive |
| `readContacts` | Read | Google Sheets |
| `addContact` | Write | Google Sheets |
| `editContact` | Write | Google Sheets |
| `removeContact` | Write | Google Sheets |

> Tools are dynamically injected per request only when the user has an active Google integration.

---

## 📦 Installation & Deployment

### 🟢 Backend Setup
1. `cd backend`
2. `npm install`
3. Create `.env`:
   ```bash
   PORT=3000
   DATABASE_URL=postgres://user:password@host:5432/dbname
   JWT_SECRET=your_secret_key
   AI_API_KEY=sk-proj-...
   GOOGLE_CLIENT_ID=...
   GOOGLE_CLIENT_SECRET=...
   GOOGLE_REDIRECT_URI=http://your-ip:3000/api/auth/google/callback
   ```
4. `npm start` (runs `node server.js`)

### 🔵 Android Frontend Setup
1. Open `/frontend` in **Android Studio**.
2. Set your backend IP in `NetworkConfig.java`:
   ```java
   public static final String BASE_URL = "http://YOUR_LOCAL_IP:3000";
   ```
3. Deep Linking is pre-configured in `AndroidManifest.xml` for `jarvisapp://oauth2redirect`.
4. Build and deploy to device.

---

## 📂 Project Structure

```text
ai-assistant-app/
├── backend/
│   ├── config/           # OpenAI & Google OAuth2 client init
│   ├── controllers/      # authController, chatController, googleController, profileController
│   ├── db/               # PostgreSQL connection pool
│   ├── middleware/        # JWT auth middleware
│   ├── google_cloud/     # Typed wrappers: calendar, gmail, drive, sheets, auth
│   ├── routes/           # Route definitions per domain
│   └── server.js         # Entry point, rate limiting, route mounting
├── frontend/
│   └── app/src/main/
│       └── java/com/kosmidis/jarvis/
│           ├── activities/   # Login, Register, Main, ProfileCustomisation
│           ├── adapters/     # ChatAdapter (Markwon markdown)
│           ├── config/       # NetworkConfig
│           ├── data/         # SessionManager
│           ├── dialogs/      # SettingsMenuDialog, IntegrationsGoogle
│           ├── managers/     # ChatUiManager, VoiceManager, VoiceUiManager, ImageManager, FileManager
│           ├── models/       # MessageModel
│           ├── network/      # AuthApiManager, ChatApiManager, VisionApiManager, FileApiManager, ProfileApiManager, GoogleIntegrationManager
│           └── utils/        # TextNormalizer
└── docs/antigravity/         # Technical documentation & task logs
    ├── FILE_INDEX.md
    ├── PROJECT_ARCHITECTURE.md
    └── antigravity_tasks.md
```

---

## 🗄️ Database Schema

| Table | Description | Key Fields |
| :--- | :--- | :--- |
| `users` | Core accounts | `id`, `email`, `password`, `auth_provider` |
| `user_profiles` | 1:1 user extension | `user_id`, `first_name`, `last_name`, `age`, `profession`, `interests`, `contacts_spreadsheet_id` |
| `user_integrations` | OAuth token store | `user_id`, `provider`, `access_token`, `refresh_token` |
| `conversations` | Chat thread metadata | `user_id`, `title`, `created_at` |
| `messages` | Individual chat logs | `conversation_id`, `sender` (user/ai), `message_text`, `created_at` |

> [!NOTE]
> All user-related tables cascade `ON DELETE` from `users(id)` — account deletion is atomic.

---

## ✍️ Documentation

- **Architecture deep-dive**: [`jarvis_complete_architecture_documentation.md`](jarvis_complete_architecture_documentation.md)
- **Antigravity Docs**: [`docs/antigravity/`](docs/antigravity/)
  - [`PROJECT_ARCHITECTURE.md`](docs/antigravity/PROJECT_ARCHITECTURE.md) — System diagrams & component responsibilities
  - [`FILE_INDEX.md`](docs/antigravity/FILE_INDEX.md) — Full file map
  - [`antigravity_tasks.md`](docs/antigravity/antigravity_tasks.md) — Task log & implementation history
