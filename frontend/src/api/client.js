export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export class ApiError extends Error {
  constructor(message, status, payload) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.payload = payload;
  }
}

export function getStoredToken() {
  return localStorage.getItem('accessToken');
}

export function setStoredAuth(authResponse) {
  localStorage.setItem('accessToken', authResponse.accessToken);
  localStorage.setItem('authUser', JSON.stringify(authResponse.user));
  localStorage.setItem('tokenExpiresAt', authResponse.expiresAt);
}

export function clearStoredAuth() {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('authUser');
  localStorage.removeItem('tokenExpiresAt');
}

export function getStoredUser() {
  const raw = localStorage.getItem('authUser');
  return raw ? JSON.parse(raw) : null;
}

export async function apiRequest(path, options = {}) {
  const token = getStoredToken();
  const headers = {
    Accept: 'application/json',
    ...(options.body ? { 'Content-Type': 'application/json' } : {}),
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  });

  const contentType = response.headers.get('content-type') || '';
  const payload = contentType.includes('application/json') ? await response.json() : await response.text();

  if (!response.ok) {
    const message = typeof payload === 'object' && payload?.message ? payload.message : `Request failed with status ${response.status}`;
    throw new ApiError(message, response.status, payload);
  }

  return payload;
}
