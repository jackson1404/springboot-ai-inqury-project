import { LogOut, ShieldCheck } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { ChatPanel } from '../components/ChatPanel';
import { DataExplorer } from '../components/DataExplorer';

export function Dashboard() {
  const { user, logout } = useAuth();

  return (
    <div className="app-shell">
      <header className="topbar">
        <div>
          <div className="brand-row"><ShieldCheck size={24} /><span>Secure Spring AI</span></div>
          <p>Spring Boot + Spring Security JWT + PostgreSQL + OpenRouter</p>
        </div>
        <div className="user-box">
          <div>
            <strong>{user?.displayName}</strong>
            <span>{user?.email} · {user?.role}</span>
          </div>
          <button className="ghost-btn" onClick={logout}><LogOut size={16} /> Logout</button>
        </div>
      </header>

      <main className="content-grid">
        <ChatPanel />
        <DataExplorer />
      </main>
    </div>
  );
}
