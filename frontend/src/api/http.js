const ADMIN_TOKEN_KEY = 'kids_admin_token'

export function getAdminToken() {
  return localStorage.getItem(ADMIN_TOKEN_KEY) || ''
}

export function setAdminToken(token) {
  localStorage.setItem(ADMIN_TOKEN_KEY, token)
}

export function clearAdminToken() {
  localStorage.removeItem(ADMIN_TOKEN_KEY)
}

export async function request(url, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {}),
  }
  if (options.admin) {
    headers['X-Admin-Token'] = getAdminToken()
  }
  const res = await fetch(url, {
    ...options,
    headers,
  })
  const data = await res.json().catch(() => ({ code: -1, message: '网络异常' }))
  if (res.status === 401 || data.code === 401) {
    clearAdminToken()
    throw new Error(data.message || '未登录')
  }
  if (data.code !== 0) {
    throw new Error(data.message || '请求失败')
  }
  return data.data
}

export const api = {
  getCharacters: () => request('/api/characters'),
  getPoems: () => request('/api/poems'),
  getPoem: (id) => request(`/api/poems/${id}`),
  getEnglish: (category) => request(category ? `/api/english?category=${category}` : '/api/english'),

  adminLogin: (username, password) =>
    request('/api/admin/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    }),
  adminLogout: () =>
    request('/api/admin/logout', { method: 'POST', admin: true }),

  adminListCharacters: () => request('/api/admin/characters', { admin: true }),
  adminSaveCharacter: (payload, id) =>
    request(id ? `/api/admin/characters/${id}` : '/api/admin/characters', {
      method: id ? 'PUT' : 'POST',
      body: JSON.stringify(payload),
      admin: true,
    }),
  adminDeleteCharacter: (id) =>
    request(`/api/admin/characters/${id}`, { method: 'DELETE', admin: true }),

  adminListPoems: () => request('/api/admin/poems', { admin: true }),
  adminSavePoem: (payload, id) =>
    request(id ? `/api/admin/poems/${id}` : '/api/admin/poems', {
      method: id ? 'PUT' : 'POST',
      body: JSON.stringify(payload),
      admin: true,
    }),
  adminDeletePoem: (id) =>
    request(`/api/admin/poems/${id}`, { method: 'DELETE', admin: true }),

  adminListEnglish: (category) =>
    request(category ? `/api/admin/english?category=${category}` : '/api/admin/english', { admin: true }),
  adminSaveEnglish: (payload, id) =>
    request(id ? `/api/admin/english/${id}` : '/api/admin/english', {
      method: id ? 'PUT' : 'POST',
      body: JSON.stringify(payload),
      admin: true,
    }),
  adminDeleteEnglish: (id) =>
    request(`/api/admin/english/${id}`, { method: 'DELETE', admin: true }),
}
