import { apiRequest } from './client';

export function listConversations() {
  return apiRequest('/api/conversations');
}

export function getConversation(conversationId) {
  return apiRequest(`/api/conversations/${conversationId}`);
}

export function renameConversation(conversationId, title) {
  return apiRequest(`/api/conversations/${conversationId}`, {
    method: 'PATCH',
    body: JSON.stringify({ title }),
  });
}

export function deleteConversation(conversationId) {
  return apiRequest(`/api/conversations/${conversationId}`, {
    method: 'DELETE',
  });
}
