const AUTH_KEY = 'sc-auth'

export function saveAuth(data) { localStorage.setItem(AUTH_KEY, JSON.stringify(data)) }
export function loadAuth() {
  try { return JSON.parse(localStorage.getItem(AUTH_KEY)) } catch { return null }
}
export function clearAuth() { localStorage.removeItem(AUTH_KEY) }
