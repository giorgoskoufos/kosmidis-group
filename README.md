# J.A.R.V.I.S. Assistant

**Just A Rather Very Intelligent System** — A sophisticated AI Assistant ecosystem that bridges the gap between Large Language Models and personal productivity. J.A.R.V.I.S. integrates deeply with Google Workspace to manage your professional and personal life through natural language.

---

## 🚀 Key Innovations

### 1. The "Self-Healing" CRM (Google Sheets)
J.A.R.V.I.S. implements a dynamic data management layer using Google Sheets as a lightweight CRM. 
- **Auto-Provisioning**: If the contact spreadsheet is missing, J.A.R.V.I.S. automatically creates, initializes, and persists a `JARVIS_Contacts` sheet to the user's Google Drive.
- **Smart Contact Resolution**: When sending emails, the AI can correlate names (e.g., "Send an email to George") with their corresponding addresses in the Sheet before execution.

### 2. Signature-Aware Communication (Gmail)
Unlike standard AI integrations, J.A.R.V.I.S. maintains your professional identity. 
- **Signature Retrieval**: It automatically fetches your official primary Gmail signature via the Settings API and appends it to all outbound emails.
- **Base64URL Encoding**: Full RFC 2822 compliance for secure and robust mail delivery.

### 3. Contextual Intelligence
- **Sliding Window History**: Maintains a 15-message rolling context to ensure efficiency without losing the current conversation thread.
- **Static System Priority**: Hard-coded personality traits and security boundaries take precedence over message history.
- **Time/Profile Injection**: The AI is aware of the current real-world time (Europe/Athens) and the user's specific professional profile (Age, Profession, Interests) for tailored responses.

---

## 🛠️ Technical Stack

| Component | Technology | Description |
| :--- | :--- | :--- |
| **Backend** | Express (Node.js) | Modular, controller-based API with JWT authentication. |
| **Mobile** | Native Android (Java) | Material Design UI with Markdown support via Markwon. |
| **AI Engine** | OpenAI GPT-4o-mini | Utilizes multi-step Tool Calling (Function Calling). |
| **Database** | PostgreSQL | Stateless persistence for chats, profiles, and Google tokens. |
| **Handshake** | Google OAuth2 | Secure browser-to-app redirection via custom Deep Links. |

---

## 🔒 Security & Performance
- **Stateless Session Management**: Powered by JWT with a custom `auth` middleware that verifies all sensitive operations.
- **API Protection**: `express-rate-limit` prevents abuse and protects against API credit drainage (100 req / 15 min).
- **Hardened Data Access**: Users can only access their own conversations, profiles, and integrations via JWT claims—client-side UserIDs are ignored for security.

---

## 📦 Installation & Deployment

### 🟢 Backend Setup
1. **Directory**: `cd /backend`
2. **Install**: `npm install`
3. **Configure**: Create a `.env` file with the following variables:
   ```bash
   PORT=3000
   DATABASE_URL=postgres://...
   JWT_SECRET=super_secret_key
   AI_API_KEY=sk-proj-...
   GOOGLE_CLIENT_ID=...
   GOOGLE_CLIENT_SECRET=...
   GOOGLE_REDIRECT_URI=http://your-ip:3000/api/auth/google/callback
   ```
4. **Start**: `npm start`

### 🔵 Frontend Setup
1. Open the `/frontend` folder in **Android Studio**.
2. Configure **`NetworkConfig.java`**:
   ```java
   public static final String BASE_URL = "http://YOUR_LOCAL_IP:3000";
   ```
3. Set up **Deep Linking** in `AndroidManifest.xml` (already configured for `jarvisapp://oauth2redirect`).
4. Build and deploy to your Android device.

---

## 📂 Project Structure
```text
├── backend/
│   ├── config/          # AI & Google client initializers
│   ├── controllers/     # Business logic (Auth, Chat, Google, Profile)
│   ├── google_cloud/    # API Wrappers (Calendar, Gmail, Drive, Sheets)
│   └── routes/          # API Endpoint definitions
├── frontend/
│   └── app/src/main/    # Native Android Java source and layout
└── docs/antigravity/    # Detailed Technical Documentation & Logs
```

---

## 🗄️ Database Schema
J.A.R.V.I.S. uses a PostgreSQL database with a normalized structure and full cascading referential integrity.

| Table | Description | Key Fields |
| :--- | :--- | :--- |
| **`users`** | Core account data. | `email`, `password`, `auth_provider` |
| **`user_profiles`** | 1:1 user extension. | `user_id`, `profession`, `contacts_spreadsheet_id` |
| **`user_integrations`** | OAuth tokens. | `user_id`, `provider`, `access_token`, `refresh_token` |
| **`conversations`** | Chat thread metadata. | `user_id`, `title`, `created_at` |
| **`messages`** | Individual chat logs. | `conversation_id`, `sender` (user/ai), `message_text` |

> [!NOTE]
> All user-related tables are linked to `users(id)` with `ON DELETE CASCADE` to ensure clean data removal during account deletion.


---

## ✍️ Documentation
For a deep dive into the high-level architecture and tool-calling sequences, see [PROJECT_ARCHITECTURE.md](docs/antigravity/PROJECT_ARCHITECTURE.md).
