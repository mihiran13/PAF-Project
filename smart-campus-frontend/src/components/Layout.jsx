import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { ROLES } from '../utils/constants'
import { useState } from 'react'
import { useToast } from '../context/ToastContext'

export default function Layout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const { error: showError } = useToast()
  const [sidebarOpen, setSidebarOpen] = useState(false)

  const onLogout = async () => {
    await logout()
    navigate('/login', { replace: true })
  }

  const goToProfile = () => {
    try {
      navigate('/profile')
    } catch (err) {
      showError('Unable to navigate to profile')
    }
  }

  const initials = (user?.name || 'U').split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase()

  const navSections = [
    {
      label: 'Overview',
      links: [
        { to: '/dashboard', label: 'Dashboard', icon: '📊', roles: [ROLES.USER, ROLES.ADMIN, ROLES.TECHNICIAN] },
        { to: '/profile', label: 'My Profile', icon: '👤', roles: [ROLES.USER, ROLES.ADMIN, ROLES.TECHNICIAN] },
      ]
    },
    {
      label: 'Facilities',
      links: [
        { to: '/resources', label: 'Resources', icon: '🏢', roles: [ROLES.USER, ROLES.ADMIN, ROLES.TECHNICIAN] },
        { to: '/bookings/new', label: 'New Booking', icon: '➕', roles: [ROLES.USER, ROLES.ADMIN, ROLES.TECHNICIAN] },
        { to: '/bookings/my', label: 'My Bookings', icon: '📅', roles: [ROLES.USER, ROLES.ADMIN, ROLES.TECHNICIAN] },
      ]
    },
    {
      label: 'Support',
      links: [
        { to: '/tickets/new', label: 'Report Issue', icon: '🎫', roles: [ROLES.USER, ROLES.ADMIN, ROLES.TECHNICIAN] },
        { to: '/tickets', label: 'Tickets', icon: '📋', roles: [ROLES.USER, ROLES.ADMIN, ROLES.TECHNICIAN] },
        { to: '/notifications', label: 'Notifications', icon: '🔔', roles: [ROLES.USER, ROLES.ADMIN, ROLES.TECHNICIAN] },
      ]
    },
    {
      label: 'Administration',
      links: [
        { to: '/analytics', label: 'Analytics', icon: '📈', roles: [ROLES.ADMIN] },
        { to: '/resources/manage', label: 'Manage Resources', icon: '🏢', roles: [ROLES.ADMIN] },
        { to: '/bookings/manage', label: 'Manage Bookings', icon: '⚙️', roles: [ROLES.ADMIN] },
        { to: '/tickets/manage', label: 'Manage Tickets', icon: '🛠️', roles: [ROLES.ADMIN] },
        { to: '/users', label: 'Users', icon: '👥', roles: [ROLES.ADMIN] },
      ]
    }
  ]

  return (
    <div className="layout">
      {/* Mobile overlay */}
      <div className={`mobile-overlay ${sidebarOpen ? 'open' : ''}`} onClick={() => setSidebarOpen(false)} />

      {/* Sidebar */}
      <aside className={`sidebar ${sidebarOpen ? 'open' : ''}`}>
        <div className="sidebar-brand">
          <div className="sidebar-brand-icon">SC</div>
          <h1>Smart Campus</h1>
        </div>

        <nav>
          {navSections.map(section => {
            const visibleLinks = section.links.filter(l => l.roles.includes(user?.role))
            if (!visibleLinks.length) return null
            return (
              <div key={section.label}>
                <div className="sidebar-label">{section.label}</div>
                {visibleLinks.map(l => (
                  <NavLink
                    key={l.to}
                    to={l.to}
                    className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}
                    onClick={() => setSidebarOpen(false)}
                  >
                    <span className="nav-icon">{l.icon}</span>
                    {l.label}
                  </NavLink>
                ))}
              </div>
            )
          })}
        </nav>

        <div className="sidebar-footer">
          <button className="btn-ghost" onClick={onLogout} style={{ width: '100%', justifyContent: 'center' }}>
            Logout
          </button>
        </div>
      </aside>

      {/* Main area */}
      <main className="main">
        <header className="topbar">
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <div className="mobile-toggle" onClick={() => setSidebarOpen(!sidebarOpen)}>☰</div>
            <div className="topbar-left">
              <h2>Smart Campus Operations Hub</h2>
            </div>
          </div>
          <div className="topbar-right">
            <span className="role-tag">{user?.role}</span>
            <button 
              onClick={goToProfile}
              style={{ 
                background: 'none', 
                border: 'none', 
                cursor: 'pointer', 
                padding: 0,
                display: 'flex',
                alignItems: 'center',
                gap: 8
              }}
            >
              <div className="user-pill">
                <div className="user-avatar">{initials}</div>
                <span>{user?.name}</span>
              </div>
            </button>
          </div>
        </header>

        <section className="content">
          <Outlet />
        </section>
      </main>
    </div>
  )
}
