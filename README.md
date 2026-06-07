# Spring AI OpenRouter Demo - Secure Full Stack with Chat Memory + Advisors

This project is a production-style learning project for:

- Spring Boot 3.5.x
- Spring Security + JWT
- PostgreSQL + Spring Data JPA
- Spring AI + OpenRouter
- Spring AI MCP client/server over STDIO
- Spring AI `MessageChatMemoryAdvisor`
- Spring AI JDBC chat memory repository
- App-owned persistent chat history for the UI
- React + Vite frontend

## Module layout

The backend is now a Maven multi-module project:

```text
business-data-core
= Shared customer/order/product JPA entities, repositories, records, DataInquiryService, and business data seeding.

business-data-mcp-server
= Spring AI MCP STDIO server that exposes business data tools.

spring-ai-openrouter-app
= Web/chat/JWT backend, Spring AI chat client, MCP client, REST data API, and conversation history.
```

Chat no longer attaches local `@Tool` beans. Business-data routes attach Spring AI MCP tool callbacks discovered from the STDIO MCP server.

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
APP_MCP_CLIENT_ENABLED=true
APP_MCP_REQUEST_TIMEOUT=20s
MCP_DATA_SERVER_COMMAND=java
MCP_DATA_SERVER_JAR=business-data-mcp-server/target/business-data-mcp-server-0.0.1-SNAPSHOT.jar
```

Run backend:

```bash
mvn clean package
mvn -pl spring-ai-openrouter-app spring-boot:run
```

If your IDE runs `spring-ai-openrouter-app` with a different working directory, set `MCP_DATA_SERVER_JAR` to an absolute path.

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
= Uses ChatClient + MessageChatMemoryAdvisor + Spring AI MCP ToolCallbackProvider.

BusinessDataMcpTools
= Exposes search_customers, search_orders, search_products, and calculate_customer_total_spend as MCP tools.

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

---

## Structured-output intent routing layer

This version adds a Spring AI structured-output routing layer without removing the existing streaming, JWT, frontend, PostgreSQL, chat memory, advisor, and typewriter flows.

### Why this layer exists

The chat service no longer blindly attaches business database tools to every request. Instead, it first creates a route decision:

```text
User message
  -> ChatService
  -> ChatRoutePolicy
  -> RuleBasedIntentDetector
  -> if rule is confident, use rule route
  -> if unclear and enabled, AiIntentDetectionService uses Spring AI structured output
  -> ChatRouteDecision
  -> ChatService chooses normal chat or tool-enabled chat
```

This improves maintainability because `ChatService` does orchestration, while routing logic is isolated in `ai/intent`.

### New package

```text
spring-ai-openrouter-app/src/main/java/com/jack/springaiopenrouter/ai/intent/
  ChatIntent.java
  IntentSource.java
  IntentResult.java
  ChatRouteDecision.java
  RuleBasedIntentDetector.java
  AiIntentDetectionService.java
  ChatRoutePolicy.java
```

### New API

```text
POST /api/intent/detect
```

Example:

```bash
curl -X POST http://localhost:8080/api/intent/detect \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"message":"Find orders for CUST-1001"}'
```

Example response:

```json
{
  "intent": "ORDER_SEARCH",
  "source": "RULE",
  "query": "Find orders for CUST-1001",
  "confidence": 0.9,
  "attachBusinessTools": true,
  "useDocumentRetrieval": false,
  "requiresClarification": false,
  "clarificationQuestion": "",
  "reason": "Matched order or sales keyword"
}
```

### Routing behavior

```text
NORMAL_CHAT
  -> no database tools attached

CUSTOMER_SEARCH / ORDER_SEARCH / PRODUCT_SEARCH / CUSTOMER_SPEND
  -> databaseBusinessTools attached

DOCUMENT_QA
  -> prepared route for future RAG integration

UNKNOWN with clarification
  -> chat returns clarification instead of forcing a bad tool call
```

### Config

```yaml
app:
  ai:
    intent-routing-enabled: ${APP_AI_INTENT_ROUTING_ENABLED:true}
    intent-min-confidence: ${APP_AI_INTENT_MIN_CONFIDENCE:0.65}
```

Recommended local defaults:

```text
APP_AI_INTENT_ROUTING_ENABLED=true
APP_AI_INTENT_MIN_CONFIDENCE=0.65
```

### Important design note

The routing flow is hybrid:

```text
Fast obvious cases -> backend rule-based detector -> no extra AI classifier call
Unclear cases -> AI structured-output classifier -> route decision
Main answer -> streaming ChatClient call
```

This avoids making every request do two model calls. Normal technical questions and obvious business queries usually use only the main chat model call.

### Current full chat flow

```text
React ChatPanel
  -> POST /api/chat/stream with JWT
  -> JwtAuthenticationFilter authenticates request
  -> ChatController
  -> ChatService
  -> ChatHistoryService resolves conversation
  -> ChatRoutePolicy creates route decision
  -> ChatClient + MessageChatMemoryAdvisor
  -> optional MCP ToolCallbackProvider only if route requires business tools
  -> Spring AI MCP client sends tool calls over STDIO
  -> business-data-mcp-server handles MCP protocol and calls DataInquiryService
  -> PostgreSQL returns customer/order/product data
  -> OpenRouter streams raw chunks
  -> backend buffers chunks by sentence / 120 chars / 400ms
  -> frontend receives NDJSON stream events
  -> frontend typewriter queue displays smooth output
  -> ChatHistoryService saves final visible message history
```

## MCP transport migration note

The first MCP transport is STDIO. To migrate the same tools to Streamable HTTP later, keep the tool names and return DTOs stable, replace the server starter with `spring-ai-starter-mcp-server-webmvc`, set `spring.ai.mcp.server.protocol=STREAMABLE`, and switch the app config from `spring.ai.mcp.client.stdio.connections.business-data` to `spring.ai.mcp.client.streamable-http.connections.business-data.url`.
