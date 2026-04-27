import { createContext, useContext, useMemo, useState } from 'react'
import { authService } from '../services/api'
import { clearAuth, loadAuth, saveAuth } from '../utils/storage'

const AuthContext = createContext()

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(loadAuth())

  const login = async (payload) => {
    let data
    // Check if payload is already an auth object (from Google login) or email/password
    if (payload.accessToken) {
      data = payload
    } else {
      data = await authService.login(payload)
    }
    saveAuth(data)
    setAuth(data)
  }

  const hydrate = async () => {
    if (!auth?.accessToken) return null
    try {
      const user = await authService.me()
      const next = { ...auth, user }
      saveAuth(next)
      setAuth(next)
      return user
    } catch {
      clearAuth()
      setAuth(null)
    }
  }

  const logout = async () => {
    try { await authService.logout() } catch (e) { /* ignore logout errors */ }
    clearAuth()
    setAuth(null)
  }

  const setUser = (updatedUser) => {
    const next = { ...auth, user: updatedUser }
    saveAuth(next)
    setAuth(next)
  }

  const value = useMemo(() => ({
    auth,
    user: auth?.user,
    isAuthenticated: !!auth?.accessToken,
    login,
    logout,
    hydrate,
    setUser
  }), [auth])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth outside AuthProvider')
  return ctx
}
