import { useState } from 'react';
import { Send } from 'lucide-react';
import { sendChatMessage } from '../api/chatApi';

const starterPrompts = [
  'Find customer Jack and show his orders.',
  'Find orders for customer CUST-1001 and calculate total spend.',
  'Which products are in the AI Platform category?',
];

export function ChatPanel() {
  const [conversationId, setConversationId] = useState('');
  const [message, setMessage] = useState('Find orders for customer CUST-1001 and calculate total spend.');
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

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
    <section className="panel chat-panel">
      <div className="panel-header">
        <div>
          <h2>AI chat + database tools</h2>
          <p>Protected endpoint. The AI can call backend tools that query PostgreSQL.</p>
        </div>
      </div>

      <div className="prompt-row">
        {starterPrompts.map((prompt) => <button key={prompt} onClick={() => usePrompt(prompt)}>{prompt}</button>)}
      </div>

      <div className="messages">
        {messages.length === 0 && <div className="empty-state">Ask about customers, orders, products, or normal Spring Boot questions.</div>}
        {messages.map((item, index) => (
          <div key={`${item.role}-${index}`} className={`message ${item.role}`}>
            <strong>{item.role === 'user' ? 'You' : 'AI'}</strong>
            <p>{item.content}</p>
          </div>
        ))}
        {loading && <div className="message assistant"><strong>AI</strong><p>Thinking...</p></div>}
      </div>

      {error && <div className="error-box">{error}</div>}

      <form onSubmit={submit} className="chat-input-row">
        <input value={message} onChange={(event) => setMessage(event.target.value)} placeholder="Ask AI about your database..." />
        <button className="icon-btn" disabled={loading} aria-label="Send message"><Send size={18} /></button>
      </form>
    </section>
  );
}
