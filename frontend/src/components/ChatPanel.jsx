import { useEffect, useMemo, useState } from 'react';
import { MessageSquarePlus, RefreshCcw, Send, Trash2 } from 'lucide-react';
import { sendChatMessage } from '../api/chatApi';
import { deleteConversation, getConversation, listConversations } from '../api/conversationApi';

const starterPrompts = [
  'My name is Jack and I am learning Spring AI advisors.',
  'What am I learning? Use memory from this conversation.',
  'Find orders for customer CUST-1001 and calculate total spend.',
];

export function ChatPanel() {
  const [conversationId, setConversationId] = useState('');
  const [conversations, setConversations] = useState([]);
  const [message, setMessage] = useState('Find orders for customer CUST-1001 and calculate total spend.');
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(false);
  const [loadingHistory, setLoadingHistory] = useState(false);
  const [error, setError] = useState('');

  const activeConversation = useMemo(
    () => conversations.find((item) => item.id === conversationId),
    [conversations, conversationId]
  );

  useEffect(() => {
    refreshConversations();
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
    setConversationId('');
    setMessages([]);
    setMessage('');
    setError('');
  }

  async function removeConversation(id) {
    if (!id) return;
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
    if (!message.trim()) return;

    const userMessage = message.trim();
    setMessages((current) => [...current, { role: 'user', content: userMessage }]);
    setMessage('');
    setLoading(true);
    setError('');

    try {
      const response = await sendChatMessage({ conversationId: conversationId || undefined, message: userMessage });
      setConversationId(response.conversationId);
      setMessages((current) => [...current, { role: 'assistant', content: response.answer }]);
      await refreshConversations();
    } catch (err) {
      setError(err.message || 'Chat request failed');
    } finally {
      setLoading(false);
    }
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
              <button onClick={() => loadConversation(item.id)}>
                <strong>{item.title}</strong>
                <span>{item.messageCount} messages</span>
              </button>
              <button className="delete-conversation" onClick={() => removeConversation(item.id)} title="Delete conversation"><Trash2 size={14} /></button>
            </div>
          ))}
        </div>
      </aside>

      <div className="chat-main">
        <div className="panel-header">
          <div>
            <h2>AI chat memory + advisors</h2>
            <p>
              Uses Spring AI MessageChatMemoryAdvisor with JDBC memory, plus app-owned PostgreSQL chat history for UI.
            </p>
            {activeConversation && <span className="active-chat-label">Current: {activeConversation.title}</span>}
          </div>
        </div>

        <div className="prompt-row">
          {starterPrompts.map((prompt) => <button key={prompt} onClick={() => usePrompt(prompt)}>{prompt}</button>)}
        </div>

        <div className="messages">
          {messages.length === 0 && <div className="empty-state">Start a new chat or load a previous conversation. Follow-up questions will use chat memory.</div>}
          {messages.map((item, index) => (
            <div key={`${item.role}-${index}`} className={`message ${item.role}`}>
              <strong>{item.role === 'user' ? 'You' : 'AI'}</strong>
              <p>{item.content}</p>
            </div>
          ))}
          {(loading || loadingHistory) && <div className="message assistant"><strong>AI</strong><p>{loadingHistory ? 'Loading conversation...' : 'Thinking...'}</p></div>}
        </div>

        {error && <div className="error-box">{error}</div>}

        <form onSubmit={submit} className="chat-input-row">
          <input value={message} onChange={(event) => setMessage(event.target.value)} placeholder="Ask AI about your database or continue the conversation..." />
          <button className="icon-btn" disabled={loading} aria-label="Send message"><Send size={18} /></button>
        </form>
      </div>
    </section>
  );
}
