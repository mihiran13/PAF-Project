import { useState, useMemo } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { ticketService } from '../services/api'
import { useAuth } from '../context/AuthContext'
import { Loader, Badge, PageHeader, Empty } from '../components/UI'

export default function TicketListPage() {
  const { user } = useAuth()
  const [scope, setScope] = useState('my')

  const fn = useMemo(() => {
    if (scope === 'all') return () => ticketService.all({ size: 100 })
    if (scope === 'assigned') return () => ticketService.assigned({ size: 100 })
    return () => ticketService.my({ size: 100 })
  }, [scope])

  const query = useQuery({ queryKey: ['tickets', scope], queryFn: fn })
  if (query.isLoading) return <Loader />
  const items = query.data?.content || []

  const priorityColors = { CRITICAL: 'var(--danger)', HIGH: 'var(--warning)', MEDIUM: 'var(--info)', LOW: 'var(--text-tertiary)' }

  return (
    <div>
      <PageHeader title="Tickets">
        <Link to="/tickets/new"><button>🎫 Report Issue</button></Link>
      </PageHeader>

      <div className="filter-buttons">
        <button onClick={() => setScope('my')} className={scope === 'my' ? 'active' : ''}>My Tickets</button>
        {user?.role !== 'USER' && (
          <button onClick={() => setScope('assigned')} className={scope === 'assigned' ? 'active' : ''}>Assigned</button>
        )}
        {user?.role === 'ADMIN' && (
          <button onClick={() => setScope('all')} className={scope === 'all' ? 'active' : ''}>All Tickets</button>
        )}
      </div>

      {!items.length ? (
        <Empty title="No tickets" subtitle="All clear! No tickets to show." />
      ) : (
        <div className="card-grid">
          {items.map(t => (
            <Link key={t.id} to={`/tickets/${t.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
              <div className="card card-clickable" style={{ borderLeft: `3px solid ${priorityColors[t.priority] || 'var(--border)'}` }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 }}>
                  <h3 style={{ margin: 0, fontSize: '0.95rem', flex: 1, marginRight: 8 }}>{t.title}</h3>
                  <Badge value={t.status} />
                </div>
                <div style={{ display: 'flex', gap: 16, fontSize: '0.82rem', color: 'var(--text-secondary)', flexWrap: 'wrap' }}>
                  <span>{t.category}</span>
                  <Badge value={t.priority} />
                  {t.assignedTechnicianName && <span>👤 {t.assignedTechnicianName}</span>}
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}
