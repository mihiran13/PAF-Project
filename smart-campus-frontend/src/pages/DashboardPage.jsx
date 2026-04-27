import { useQuery } from '@tanstack/react-query'
import { bookingService, notificationService, ticketService } from '../services/api'
import { useAuth } from '../context/AuthContext'
import { Loader, StatCard } from '../components/UI'
import { Link } from 'react-router-dom'
import { ROLES } from '../utils/constants'

export default function DashboardPage() {
  const { user } = useAuth()
  const isAdmin = user?.role === ROLES.ADMIN
  
  const bookings = useQuery({
    queryKey: ['dash-bookings'],
    queryFn: () => (isAdmin ? bookingService.all({ size: 5 }) : bookingService.my({ size: 5 }))
  })
  
  const tickets = useQuery({
    queryKey: ['dash-tickets'],
    queryFn: () => (isAdmin ? ticketService.all({ size: 5 }) : ticketService.my({ size: 5 }))
  })
  
  const unread = useQuery({
    queryKey: ['unread-count'],
    queryFn: notificationService.unreadCount
  })

  if (bookings.isLoading || tickets.isLoading) return <Loader />

  const getStatusColor = (status) => {
    const colors = {
      ACTIVE: '#4caf50',
      PENDING: '#ff9800',
      CONFIRMED: '#2196f3',
      COMPLETED: '#9c27b0',
      CANCELLED: '#f44336',
      RESOLVED: '#4caf50',
      OPEN: '#ff9800',
      IN_PROGRESS: '#2196f3'
    }
    return colors[status] || '#666'
  }

  return (
    <div>
      {/* Welcome Banner */}
      <div className="welcome-banner" style={{ background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', color: 'white', marginBottom: 24 }}>
        <h2 style={{ margin: '0 0 8px' }}>Welcome back, {user?.name?.split(' ')[0]} 👋</h2>
        <p style={{ margin: 0, opacity: 0.9 }}>Here's your campus operations dashboard for today.</p>
        <div className="quick-actions" style={{ marginTop: 16 }}>
          <Link to="/bookings/new"><button style={{ background: 'white', color: '#667eea', fontWeight: 600 }}>➕ New Booking</button></Link>
          <Link to="/tickets/new"><button className="btn-ghost" style={{ color: 'white', borderColor: 'white' }}>🎫 Report Issue</button></Link>
          {isAdmin && <Link to="/analytics"><button className="btn-ghost" style={{ color: 'white', borderColor: 'white' }}>📊 Analytics</button></Link>}
        </div>
      </div>

      {/* Quick Stats */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: 12, marginBottom: 24 }}>
        <div style={{ background: 'white', border: '1px solid #e0e0e0', borderRadius: 8, padding: 16, textAlign: 'center' }}>
          <div style={{ fontSize: 24, fontWeight: 700, color: '#667eea', marginBottom: 4 }}>{unread.data ?? 0}</div>
          <div style={{ fontSize: 12, color: '#666' }}>Unread Notifications</div>
        </div>
        <div style={{ background: 'white', border: '1px solid #e0e0e0', borderRadius: 8, padding: 16, textAlign: 'center' }}>
          <div style={{ fontSize: 24, fontWeight: 700, color: '#2196f3', marginBottom: 4 }}>{bookings.data?.content?.length ?? 0}</div>
          <div style={{ fontSize: 12, color: '#666' }}>Recent Bookings</div>
        </div>
        <div style={{ background: 'white', border: '1px solid #e0e0e0', borderRadius: 8, padding: 16, textAlign: 'center' }}>
          <div style={{ fontSize: 24, fontWeight: 700, color: '#ff9800', marginBottom: 4 }}>{tickets.data?.content?.length ?? 0}</div>
          <div style={{ fontSize: 12, color: '#666' }}>Open Tickets</div>
        </div>
        {isAdmin && (
          <div style={{ background: 'white', border: '1px solid #e0e0e0', borderRadius: 8, padding: 16, textAlign: 'center' }}>
            <div style={{ fontSize: 24, fontWeight: 700, color: '#4caf50', marginBottom: 4 }}>72%</div>
            <div style={{ fontSize: 12, color: '#666' }}>System Health</div>
          </div>
        )}
      </div>

      {/* Main Content Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: 16, width: '100%' }}>
        {/* Recent Bookings */}
        <div className="panel">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
            <h3 style={{ margin: 0 }}>Recent Bookings</h3>
            <Link to="/bookings/my"><span style={{ fontSize: 12, color: '#2196f3', cursor: 'pointer' }}>View All →</span></Link>
          </div>
          {(bookings.data?.content || []).slice(0, 4).map(b => (
            <Link key={b.id} to={`/bookings/${b.id}`} style={{ textDecoration: 'none' }}>
              <div style={{ 
                display: 'flex', 
                justifyContent: 'space-between', 
                alignItems: 'center',
                padding: '10px 12px',
                margin: '8px 0',
                background: '#f9f9f9',
                borderRadius: 4,
                borderLeft: `4px solid ${getStatusColor(b.status)}`,
                transition: 'background 0.2s',
                cursor: 'pointer'
              }}
              onMouseEnter={e => e.currentTarget.style.background = '#f0f0f0'}
              onMouseLeave={e => e.currentTarget.style.background = '#f9f9f9'}
              >
                <div>
                  <strong style={{ fontSize: '0.9rem' }}>{b.resourceName}</strong>
                  <p style={{ fontSize: '0.8rem', margin: '2px 0 0', color: '#999' }}>{b.bookingDate}</p>
                </div>
                <span style={{ fontSize: '11px', fontWeight: 600, color: 'white', background: getStatusColor(b.status), padding: '3px 8px', borderRadius: 3 }}>{b.status}</span>
              </div>
            </Link>
          ))}
          {!(bookings.data?.content?.length) && <p style={{ fontSize: '0.85rem', padding: '12px 0', color: '#999' }}>No recent bookings</p>}
        </div>

        {/* Recent Tickets */}
        <div className="panel">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
            <h3 style={{ margin: 0 }}>Recent Tickets</h3>
            <Link to="/tickets"><span style={{ fontSize: 12, color: '#2196f3', cursor: 'pointer' }}>View All →</span></Link>
          </div>
          {(tickets.data?.content || []).slice(0, 4).map(t => (
            <Link key={t.id} to={`/tickets/${t.id}`} style={{ textDecoration: 'none' }}>
              <div style={{ 
                display: 'flex', 
                justifyContent: 'space-between', 
                alignItems: 'center',
                padding: '10px 12px',
                margin: '8px 0',
                background: '#f9f9f9',
                borderRadius: 4,
                borderLeft: `4px solid ${getStatusColor(t.status)}`,
                transition: 'background 0.2s',
                cursor: 'pointer'
              }}
              onMouseEnter={e => e.currentTarget.style.background = '#f0f0f0'}
              onMouseLeave={e => e.currentTarget.style.background = '#f9f9f9'}
              >
                <div>
                  <strong style={{ fontSize: '0.9rem' }}>{t.title}</strong>
                  <p style={{ fontSize: '0.8rem', margin: '2px 0 0', color: '#999' }}>{t.category}</p>
                </div>
                <span style={{ fontSize: '11px', fontWeight: 600, color: 'white', background: getStatusColor(t.status), padding: '3px 8px', borderRadius: 3 }}>{t.status}</span>
              </div>
            </Link>
          ))}
          {!(tickets.data?.content?.length) && <p style={{ fontSize: '0.85rem', padding: '12px 0', color: '#999' }}>No recent tickets</p>}
        </div>

        {/* Quick Tips / Recommendations */}
        <div className="panel" style={{ background: '#f0f7ff', borderLeft: '4px solid #2196f3' }}>
          <h3 style={{ margin: '0 0 12px', color: '#1565c0' }}>💡 Helpful Tips</h3>
          <ul style={{ margin: 0, paddingLeft: 20, fontSize: 13, color: '#333', lineHeight: 1.6 }}>
            <li>Book resources in advance during peak hours (2-4 PM)</li>
            <li>Check resource availability before creating bookings</li>
            <li>Resolve support tickets promptly for better campus experience</li>
            {isAdmin && <li>Review analytics for resource optimization insights</li>}
          </ul>
        </div>
      </div>
    </div>
  )
}
