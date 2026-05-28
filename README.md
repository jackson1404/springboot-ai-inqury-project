# Spring AI OpenRouter Demo - Secure Full Stack with Chat Memory + Advisors

This project is a production-style learning project for:

- Spring Boot 3.5.x
- Spring Security + JWT
- PostgreSQL + Spring Data JPA
- Spring AI + OpenRouter
- Spring AI `MessageChatMemoryAdvisor`
- Spring AI JDBC chat memory repository
- App-owned persistent chat history for the UI
- React + Vite frontend

## What changed in this version

This version adds two memory layers:

1. **Spring AI Chat Memory**
   - Uses `MessageChatMemoryAdvisor`.
   - Uses `ChatMemory.CONVERSATION_ID` on every chat request.
   - Uses JDBC-backed memory through `spring-ai-starter-model-chat-memory-repository-jdbc`.
   - Stores the short memory window used by the model in `SPRING_AI_CHAT_MEMORY`.

2. **Application Chat History**
   - Uses your own JPA entities: `ChatConversationEntity` and `ChatMessageEntity`.
   - Stores full conversation history for the frontend UI.
   - Supports listing, loading, renaming, and deleting user-owned conversations.

Spring AI memory is for model context. App chat history is for UI, audit, and long-term records.

## Main backend endpoints

Public:

```http
GET  /api/ping
POST /api/auth/register
POST /api/auth/login
GET  /actuator/health
```

Protected by JWT:

```http
POST   /api/chat
POST   /api/chat/stream
GET    /api/conversations
GET    /api/conversations/{conversationId}
PATCH  /api/conversations/{conversationId}
DELETE /api/conversations/{conversationId}
GET    /api/data/customers
GET    /api/data/orders
GET    /api/data/products
```

## Run PostgreSQL

```bash
docker compose up -d
```

Adminer runs on:

```text
http://localhost:8081
```

## Backend environment variables

Set these in IntelliJ Run Configuration or your shell:

```text
OPENROUTER_API_KEY=your_key
OPENROUTER_BASE_URL=https://openrouter.ai/api
OPENROUTER_MODEL=openrouter/free
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=spring_ai_demo
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
APP_DB_SEED_ENABLED=true
JWT_SECRET=replace-with-a-long-random-secret-at-least-32-characters
CORS_ALLOWED_ORIGINS=http://localhost:5173
SPRING_AI_CHAT_MEMORY_SCHEMA=always
APP_AI_MAX_MEMORY_MESSAGES=20
APP_AI_MAX_TITLE_LENGTH=80
```

Run backend:

```bash
mvn spring-boot:run
```

## Frontend

```bash
cd frontend
npm install
npm run dev
```

Open:

```text
http://localhost:5173
```

Seed users:

```text
jack@example.com / Password123

demo@example.com / Password123
```

## How chat memory works

When the user sends a message:

```text
React ChatPanel
  -> POST /api/chat with JWT
  -> ChatController
  -> ChatService
  -> ChatHistoryService creates or validates conversation ownership
  -> ChatClient call uses MessageChatMemoryAdvisor
  -> advisor gets ChatMemory.CONVERSATION_ID
  -> Spring AI loads prior memory messages from JDBC
  -> OpenRouter receives current prompt + typed history messages
  -> Spring AI stores new user/assistant messages in JDBC memory
  -> ChatHistoryService stores the same visible exchange in app chat history tables
```

The important line is in `ChatService`:

```java
.advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversation.getId()))
```

Without that parameter, Spring AI memory advisors do not know which conversation to load or update.

## Important classes

```text
AiChatConfig
= Creates MessageWindowChatMemory using the JDBC ChatMemoryRepository.

ChatService
= Uses ChatClient + MessageChatMemoryAdvisor + DatabaseBusinessTools.

ChatHistoryService
= Owns application conversation records and user ownership checks.

ConversationController
= REST API for conversation list/load/rename/delete.

ChatConversationEntity
= JPA table for one chat thread.

ChatMessageEntity
= JPA table for visible user/assistant messages.

ChatConversationRepository / ChatMessageRepository
= JPA repositories for persistent chat history.
```

## Notes

- `.env` files are not included because they may contain secrets.
- Spring Boot does not automatically load `.env`; use IntelliJ environment variables, shell variables, Docker Compose, or production secrets.
- The Spring AI JDBC memory table is separate from the app-owned `chat_conversations` and `chat_messages` tables.

## Streaming chat reliability update

This version includes a safer streaming implementation for `POST /api/chat/stream`.

### What changed

- Adds `spring-boot-starter-webflux` so the backend can return `Flux<StreamChatEvent>`.
- Streams newline-delimited JSON (`application/x-ndjson`) events.
- Buffers tiny provider token chunks and flushes when a sentence ends or the buffer reaches `APP_AI_STREAM_MIN_BUFFER_CHARS` characters.
- Default stream buffer threshold is `160`, which is in the recommended 120-180 range.
- Permits `/error` and `ERROR` / `ASYNC` / `FORWARD` dispatcher types in Spring Security so streaming errors do not cause `AccessDeniedException` after the response is already committed.
- Adds `response.isCommitted()` protection in `RestAuthenticationEntryPoint`.
- Frontend now uses `fetch()` streaming and handles interrupted streams gracefully.

### Optional streaming environment variable

```bash
APP_AI_STREAM_MIN_BUFFER_CHARS=160
```

Lower values stream faster but produce more frontend updates. Higher values reduce frontend updates but feel less realtime.

### Test streaming with curl

```bash
curl -N -X POST http://localhost:8080/api/chat/stream \
  -H "Content-Type: application/json" \
  -H "Accept: application/x-ndjson" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"message":"Explain Spring AI streaming simply"}'
```

## Frontend typewriter rendering update

This version uses the recommended real-world streaming UI design:

```text
Backend: real streaming + medium chunks
Frontend: typewriter display
```

Backend still streams real data from the model and flushes medium-sized chunks when a sentence ends or the buffer reaches `APP_AI_STREAM_MIN_BUFFER_CHARS`.

The frontend no longer appends a whole backend chunk immediately. Instead, `ChatPanel.jsx` queues each incoming `token` event and reveals it gradually with:

```text
TYPEWRITER_CHARS_PER_STEP = 3
TYPEWRITER_DELAY_MS = 18
```

So the backend remains efficient, while the UI feels like a real typing stream.

To tune the visual speed, edit these constants in:

```text
frontend/src/components/ChatPanel.jsx
```

Recommended values:

```text
Slower typing: 1-2 chars every 20-30ms
Balanced typing: 3 chars every 18ms
Faster typing: 4-5 chars every 10-15ms
```


## Streaming UX tuning

This version uses real backend streaming with medium chunks and frontend typewriter display.

Optional environment variables:

```text
APP_AI_STREAM_MIN_BUFFER_CHARS=120
APP_AI_STREAM_MAX_WAIT_MILLIS=400
```

Meaning:

- `APP_AI_STREAM_MIN_BUFFER_CHARS`: flush a chunk when the backend buffer reaches this many characters.
- `APP_AI_STREAM_MAX_WAIT_MILLIS`: flush partial text after this delay even if no sentence has ended yet. This reduces the first visible delay and reduces pauses between chunks.

Frontend typewriter speed is in `frontend/src/components/ChatPanel.jsx`:

```javascript
const TYPEWRITER_CHARS_PER_STEP = 4;
const TYPEWRITER_DELAY_MS = 14;
```
