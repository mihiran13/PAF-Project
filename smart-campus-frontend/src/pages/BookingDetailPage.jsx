import { useParams, Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { bookingService } from '../services/api'
import { Loader, Badge, PageHeader } from '../components/UI'

export default function BookingDetailPage() {
  const { id } = useParams()
  const query = useQuery({ queryKey: ['booking', id], queryFn: () => bookingService.get(id) })

  if (query.isLoading) return <Loader />
  const item = query.data

  return (
    <div>
      <PageHeader title={`Booking #${item.id}`}>
        <Link to="/bookings/my"><button className="btn-ghost">← Back</button></Link>
      </PageHeader>

      <div className="panel">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
          <h3 style={{ margin: 0 }}>{item.resourceName}</h3>
          <Badge value={item.status} />
        </div>

        <div className="detail-grid">
          <div className="detail-item">
            <div className="detail-label">Resource</div>
            <div className="detail-value">{item.resourceName}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Location</div>
            <div className="detail-value">{item.resourceLocation}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Date</div>
            <div className="detail-value">{item.bookingDate}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Time</div>
            <div className="detail-value">{item.startTime} — {item.endTime}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Attendees</div>
            <div className="detail-value">{item.expectedAttendees}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Purpose</div>
            <div className="detail-value">{item.purpose}</div>
          </div>
        </div>

        {item.rejectionReason && (
          <div className="card" style={{ marginTop: 16, borderLeft: '3px solid var(--danger)', background: 'var(--danger-soft)' }}>
            <strong style={{ fontSize: '0.85rem' }}>Rejection Reason</strong>
            <p style={{ margin: '4px 0 0', fontSize: '0.9rem' }}>{item.rejectionReason}</p>
          </div>
        )}
        {item.cancellationReason && (
          <div className="card" style={{ marginTop: 16, borderLeft: '3px solid var(--warning)', background: 'var(--warning-soft)' }}>
            <strong style={{ fontSize: '0.85rem' }}>Cancellation Reason</strong>
            <p style={{ margin: '4px 0 0', fontSize: '0.9rem' }}>{item.cancellationReason}</p>
          </div>
        )}
      </div>
    </div>
  )
}
