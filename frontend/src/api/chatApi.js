import { apiRequest } from './client';

export function sendChatMessage(payload) {
  return apiRequest('/api/chat', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}
