import { useParams, Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { resourceService } from '../services/api'
import { Loader, Badge, PageHeader } from '../components/UI'

export default function ResourceDetailPage() {
  const { id } = useParams()
  const query = useQuery({ queryKey: ['resource', id], queryFn: () => resourceService.get(id) })

  if (query.isLoading) return <Loader />
  const item = query.data

  return (
    <div>
      <PageHeader title={item.name}>
        <Badge value={item.status} />
        <Link to="/resources"><button className="btn-ghost">← Back</button></Link>
        <Link to="/bookings/new"><button>📅 Book Now</button></Link>
      </PageHeader>

      <div className="panel">
        <div className="detail-grid">
          <div className="detail-item">
            <div className="detail-label">Type</div>
            <div className="detail-value">{item.type}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Location</div>
            <div className="detail-value">{item.location}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Capacity</div>
            <div className="detail-value">{item.capacity} people</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Status</div>
            <div className="detail-value"><Badge value={item.status} /></div>
          </div>
        </div>
        {item.description && (
          <div style={{ marginTop: 16 }}>
            <div className="detail-label">Description</div>
            <p style={{ fontSize: '0.9rem' }}>{item.description}</p>
          </div>
        )}
      </div>

      <div className="panel">
        <h3>Availability Windows</h3>
        {(item.availabilityWindows || []).length === 0 ? (
          <p style={{ fontSize: '0.85rem' }}>No availability windows configured.</p>
        ) : (
          <div style={{ display: 'grid', gap: 8, marginTop: 12 }}>
            {item.availabilityWindows.map(w => (
              <div key={w.id} className="card" style={{ padding: 12, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <strong style={{ fontSize: '0.9rem' }}>{w.dayOfWeek}</strong>
                <span style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>{w.startTime} — {w.endTime}</span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
