import { AuthCard } from './components/AuthCard';
import { useAuth } from './context/AuthContext';
import { Dashboard } from './pages/Dashboard';
import './styles.css';

export default function App() {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? <Dashboard /> : <AuthCard />;
}
