import { API_BASE_URL, apiRequest, getStoredToken } from './client';

export function sendChatMessage(payload) {
  return apiRequest('/api/chat', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export async function streamChatMessage({ conversationId, message, onEvent, signal }) {
  const token = getStoredToken();

  const response = await fetch(`${API_BASE_URL}/api/chat/stream`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/x-ndjson',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify({
      conversationId: conversationId || null,
      message,
    }),
    signal,
  });

  if (!response.ok) {
    const error = await safeJson(response);
    throw new Error(error?.message || `Streaming chat request failed with status ${response.status}`);
  }

  if (!response.body) {
    throw new Error('Streaming is not supported by this browser.');
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = '';

  try {
    while (true) {
      const { value, done } = await reader.read();

      if (done) {
        break;
      }

      buffer += decoder.decode(value, { stream: true });

      const lines = buffer.split('\n');
      buffer = lines.pop() || '';

      for (const line of lines) {
        const trimmed = line.trim();

        if (!trimmed) {
          continue;
        }

        try {
          onEvent(JSON.parse(trimmed));
        } catch (error) {
          console.warn('Failed to parse stream event:', trimmed, error);
        }
      }
    }

    const finalLine = buffer.trim();

    if (finalLine) {
      try {
        onEvent(JSON.parse(finalLine));
      } catch (error) {
        console.warn('Failed to parse final stream event:', finalLine, error);
      }
    }
  } catch (error) {
    if (error.name === 'AbortError') {
      throw error;
    }

    onEvent({
      type: 'error',
      conversationId: conversationId || null,
      content: 'The streaming response was interrupted before completion. Please retry.',
      timestamp: new Date().toISOString(),
    });
  } finally {
    reader.releaseLock();
  }
}

async function safeJson(response) {
  try {
    return await response.json();
  } catch {
    return null;
  }
}
