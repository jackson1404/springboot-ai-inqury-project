import { useState } from 'react';
import { useAuth } from '../context/AuthContext';

export function AuthCard() {
  const { login, register, loading } = useAuth();
  const [mode, setMode] = useState('login');
  const [form, setForm] = useState({ displayName: 'Jack Son', email: 'jack@example.com', password: 'Password123' });
  const [error, setError] = useState('');

  function updateField(event) {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  }

  async function submit(event) {
    event.preventDefault();
    setError('');
    try {
      if (mode === 'login') {
        await login({ email: form.email, password: form.password });
      } else {
        await register(form);
      }
    } catch (err) {
      setError(err.message || 'Authentication failed');
    }
  }

  return (
    <div className="auth-shell">
      <div className="auth-card">
        <div className="brand-badge">Spring AI</div>
        <h1>{mode === 'login' ? 'Welcome back' : 'Create account'}</h1>
        <p className="muted">Sign in to access protected chat, database inquiry APIs, and AI tool calling.</p>

        <div className="mode-switch">
          <button className={mode === 'login' ? 'active' : ''} onClick={() => setMode('login')}>Login</button>
          <button className={mode === 'register' ? 'active' : ''} onClick={() => setMode('register')}>Register</button>
        </div>

        <form onSubmit={submit} className="form-stack">
          {mode === 'register' && (
            <label>
              Display name
              <input name="displayName" value={form.displayName} onChange={updateField} />
            </label>
          )}
          <label>
            Email
            <input name="email" type="email" value={form.email} onChange={updateField} />
          </label>
          <label>
            Password
            <input name="password" type="password" value={form.password} onChange={updateField} />
          </label>

          {error && <div className="error-box">{error}</div>}
          <button className="primary-btn" disabled={loading}>{loading ? 'Please wait...' : mode === 'login' ? 'Login' : 'Register'}</button>
        </form>

        <p className="hint">Seed user: jack@example.com / Password123</p>
      </div>
    </div>
  );
}
