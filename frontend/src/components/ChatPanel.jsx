import { useEffect, useMemo, useRef, useState } from 'react';
import { MessageSquarePlus, RefreshCcw, Send, Square, Trash2 } from 'lucide-react';
import { streamChatMessage } from '../api/chatApi';
import { deleteConversation, getConversation, listConversations } from '../api/conversationApi';

const starterPrompts = [
  'My name is Jack and I am learning Spring AI advisors.',
  'What am I learning? Use memory from this conversation.',
  'Find orders for customer CUST-1001 and calculate total spend.',
];

const TYPEWRITER_CHARS_PER_STEP = 4;
const TYPEWRITER_DELAY_MS = 14;

export function ChatPanel() {
  const [conversationId, setConversationId] = useState('');
  const [conversations, setConversations] = useState([]);
  const [message, setMessage] = useState('Find orders for customer CUST-1001 and calculate total spend.');
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(false);
  const [loadingHistory, setLoadingHistory] = useState(false);
  const [error, setError] = useState('');
  const abortRef = useRef(null);
  const typingQueueRef = useRef([]);
  const typingTimerRef = useRef(null);
  const isTypingRef = useRef(false);


  const activeConversation = useMemo(
    () => conversations.find((item) => item.id === conversationId),
    [conversations, conversationId]
  );

  useEffect(() => {
    refreshConversations();

    return () => {
      abortRef.current?.abort();
      clearTypewriter();
    };
  }, []);

  async function refreshConversations() {
    try {
      const data = await listConversations();
      setConversations(data);
    } catch (err) {
      setError(err.message || 'Unable to load conversations');
    }
  }

  async function loadConversation(id) {
    if (loading) return;

    setLoadingHistory(true);
    setError('');
    try {
      const data = await getConversation(id);
      setConversationId(id);
      setMessages(data.messages.map((item) => ({ role: item.role.toLowerCase(), content: item.content })));
    } catch (err) {
      setError(err.message || 'Unable to load conversation');
    } finally {
      setLoadingHistory(false);
    }
  }

  function newConversation() {
    if (loading) return;

    setConversationId('');
    setMessages([]);
    setMessage('');
    setError('');
  }

  async function removeConversation(id) {
    if (!id || loading) return;
    try {
      await deleteConversation(id);
      if (conversationId === id) {
        newConversation();
      }
      await refreshConversations();
    } catch (err) {
      setError(err.message || 'Unable to delete conversation');
    }
  }

  async function submit(event) {
    event?.preventDefault();
    if (!message.trim() || loading) return;

    const userMessage = message.trim();
    setMessages((current) => [
      ...current,
      { role: 'user', content: userMessage },
      { role: 'assistant', content: '' },
    ]);
    setMessage('');
    setLoading(true);
    setError('');

    const controller = new AbortController();
    abortRef.current = controller;

    try {
      await streamChatMessage({
        conversationId: conversationId || undefined,
        message: userMessage,
        signal: controller.signal,
        onEvent: (event) => {
          if (event.type === 'conversation') {
            setConversationId(event.conversationId);
            return;
          }

          if (event.type === 'token') {
            enqueueAssistantText(event.content);
            return;
          }

          if (event.type === 'error') {
            clearTypewriter();
            setMessages((current) => {
              const next = [...current];
              const lastIndex = next.length - 1;
              const errorText = `Error: ${event.content}`;

              if (lastIndex >= 0 && next[lastIndex].role === 'assistant') {
                next[lastIndex] = { ...next[lastIndex], content: errorText };
                return next;
              }

              return [...next, { role: 'assistant', content: errorText }];
            });
          }
        },
      });

      await waitForTypewriterToFinish();
      await refreshConversations();
    } catch (err) {
      if (err.name !== 'AbortError') {
        setError(err.message || 'Streaming chat request failed');
      }
    } finally {
      setLoading(false);
      abortRef.current = null;
    }
  }

  function stopStreaming() {
    abortRef.current?.abort();
    clearTypewriter();
    setLoading(false);
  }

  function enqueueAssistantText(text) {
    if (!text) return;

    typingQueueRef.current.push(text);

    if (!isTypingRef.current) {
      runTypewriter();
    }
  }

  function runTypewriter() {
    isTypingRef.current = true;

    const typeNext = () => {
      const currentChunk = typingQueueRef.current[0];

      if (!currentChunk || currentChunk.length === 0) {
        typingQueueRef.current.shift();

        if (typingQueueRef.current.length === 0) {
          isTypingRef.current = false;
          typingTimerRef.current = null;
          return;
        }

        typeNext();
        return;
      }

      const nextPiece = currentChunk.slice(0, TYPEWRITER_CHARS_PER_STEP); // 4 char per step
      typingQueueRef.current[0] = currentChunk.slice(TYPEWRITER_CHARS_PER_STEP);

      setMessages((current) => {
        const next = [...current];
        const lastIndex = next.length - 1;

        if (lastIndex >= 0 && next[lastIndex].role === 'assistant') {
          next[lastIndex] = {
            ...next[lastIndex],
            content: next[lastIndex].content + nextPiece,
          };
        }

        return next;
      });

      typingTimerRef.current = window.setTimeout(typeNext, TYPEWRITER_DELAY_MS);
    };

    typeNext();
  }

  function clearTypewriter() {
    typingQueueRef.current = [];
    isTypingRef.current = false;

    if (typingTimerRef.current) {
      window.clearTimeout(typingTimerRef.current);
      typingTimerRef.current = null;
    }
  }

  function waitForTypewriterToFinish() {
    return new Promise((resolve) => {
      const check = () => {
        if (!isTypingRef.current && typingQueueRef.current.length === 0) {
          resolve();
          return;
        }

        window.setTimeout(check, 40);
      };

      check();
    });
  }

  function usePrompt(prompt) {
    setMessage(prompt);
  }

  return (
    <section className="panel chat-panel memory-layout">
      <aside className="conversation-sidebar">
        <div className="sidebar-title-row">
          <h3>Conversations</h3>
          <button className="small-icon-btn" onClick={newConversation} title="New chat"><MessageSquarePlus size={16} /></button>
        </div>
        <button className="refresh-btn" onClick={refreshConversations}><RefreshCcw size={14} /> Refresh</button>
        <div className="conversation-list">
          {conversations.length === 0 && <div className="empty-mini">No saved chats yet.</div>}
          {conversations.map((item) => (
            <div key={item.id} className={`conversation-item ${item.id === conversationId ? 'active' : ''}`}>
              <button onClick={() => loadConversation(item.id)} disabled={loading}>
                <strong>{item.title}</strong>
                <span>{item.messageCount} messages</span>
              </button>
              <button className="delete-conversation" onClick={() => removeConversation(item.id)} title="Delete conversation" disabled={loading}><Trash2 size={14} /></button>
            </div>
          ))}
        </div>
      </aside>

      <div className="chat-main">
        <div className="panel-header">
          <div>
            <h2>AI chat memory + advisors</h2>
            <p>
              Uses Spring AI MessageChatMemoryAdvisor with JDBC memory, app-owned PostgreSQL chat history, and NDJSON streaming.
            </p>
            {activeConversation && <span className="active-chat-label">Current: {activeConversation.title}</span>}
          </div>
        </div>

        <div className="prompt-row">
          {starterPrompts.map((prompt) => <button key={prompt} onClick={() => usePrompt(prompt)} disabled={loading}>{prompt}</button>)}
        </div>

        <div className="messages">
          {messages.length === 0 && <div className="empty-state">Start a new chat or load a previous conversation. Follow-up questions will use chat memory.</div>}
          {messages.map((item, index) => (
            <div key={`${item.role}-${index}`} className={`message ${item.role}`}>
              <strong>{item.role === 'user' ? 'You' : 'AI'}</strong>
              <p>{item.content || (item.role === 'assistant' && loading ? 'Streaming...' : '')}</p>
            </div>
          ))}
          {loadingHistory && <div className="message assistant"><strong>AI</strong><p>Loading conversation...</p></div>}
        </div>

        {error && <div className="error-box">{error}</div>}

        <form onSubmit={submit} className="chat-input-row">
          <input
            value={message}
            onChange={(event) => setMessage(event.target.value)}
            placeholder="Ask AI about your database or continue the conversation..."
            disabled={loading}
          />
          {loading ? (
            <button type="button" className="icon-btn stop-btn" onClick={stopStreaming} aria-label="Stop streaming"><Square size={18} /></button>
          ) : (
            <button className="icon-btn" disabled={!message.trim()} aria-label="Send message"><Send size={18} /></button>
          )}
        </form>
      </div>
    </section>
  );
}
