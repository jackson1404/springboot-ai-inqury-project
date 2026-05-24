import { useEffect, useMemo, useState } from 'react';
import { Database, RefreshCw, Search } from 'lucide-react';
import { fetchCustomers, fetchOrders, fetchProducts } from '../api/dataApi';

const tabs = [
  { key: 'customers', label: 'Customers' },
  { key: 'orders', label: 'Orders' },
  { key: 'products', label: 'Products' },
];

export function DataExplorer() {
  const [tab, setTab] = useState('customers');
  const [query, setQuery] = useState('');
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const columns = useMemo(() => {
    if (tab === 'orders') return ['id', 'customerId', 'productCode', 'quantity', 'totalAmount', 'status', 'orderDate'];
    if (tab === 'products') return ['code', 'name', 'category', 'price', 'stock'];
    return ['id', 'name', 'email', 'tier', 'region'];
  }, [tab]);

  async function loadData(nextTab = tab, nextQuery = query) {
    setLoading(true);
    setError('');
    try {
      const api = nextTab === 'orders' ? fetchOrders : nextTab === 'products' ? fetchProducts : fetchCustomers;
      const data = await api(nextQuery);
      setRows(data);
    } catch (err) {
      setError(err.message || 'Failed to load data');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadData(tab, '');
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tab]);

  function submit(event) {
    event.preventDefault();
    loadData(tab, query);
  }

  return (
    <section className="panel data-panel">
      <div className="panel-header">
        <div>
          <h2>Protected data APIs</h2>
          <p>Directly query the same PostgreSQL data used by the AI tools.</p>
        </div>
        <Database size={22} />
      </div>

      <div className="tabs">
        {tabs.map((item) => (
          <button key={item.key} className={tab === item.key ? 'active' : ''} onClick={() => { setTab(item.key); setQuery(''); }}>
            {item.label}
          </button>
        ))}
      </div>

      <form className="search-row" onSubmit={submit}>
        <div className="search-input-wrap"><Search size={16} /><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder={`Search ${tab}...`} /></div>
        <button type="submit" className="secondary-btn">Search</button>
        <button type="button" className="ghost-btn" onClick={() => loadData(tab, query)}><RefreshCw size={16} /> Refresh</button>
      </form>

      {error && <div className="error-box">{error}</div>}

      <div className="table-wrap">
        <table>
          <thead>
            <tr>{columns.map((column) => <th key={column}>{column}</th>)}</tr>
          </thead>
          <tbody>
            {rows.map((row, index) => (
              <tr key={row.id || row.code || index}>{columns.map((column) => <td key={column}>{String(row[column] ?? '')}</td>)}</tr>
            ))}
          </tbody>
        </table>
        {loading && <div className="table-overlay">Loading...</div>}
        {!loading && rows.length === 0 && <div className="empty-state">No records found.</div>}
      </div>
    </section>
  );
}
