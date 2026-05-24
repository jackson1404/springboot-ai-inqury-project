import { apiRequest } from './client';

export function fetchCustomers(query = '') {
  const params = query ? `?q=${encodeURIComponent(query)}` : '';
  return apiRequest(`/api/data/customers${params}`);
}

export function fetchOrders(query = '') {
  const params = query ? `?q=${encodeURIComponent(query)}` : '';
  return apiRequest(`/api/data/orders${params}`);
}

export function fetchProducts(query = '') {
  const params = query ? `?q=${encodeURIComponent(query)}` : '';
  return apiRequest(`/api/data/products${params}`);
}
