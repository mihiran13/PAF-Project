import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { bookingService } from '../services/api'
import { useToast } from '../context/ToastContext'
import { mapApiError } from '../utils/errorMapper'
import { Loader, Empty, Badge, PageHeader } from '../components/UI'

export default function MyBookingsPage() {
  const { error: showError } = useToast()
  const queryClient = useQueryClient()

  const query = useQuery({
    queryKey: ['my-bookings'],
    queryFn: () => bookingService.my({ size: 50 })
  })

  const cancelMutation = useMutation({
    mutationFn: (id) => bookingService.cancel(id, ''),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['my-bookings'] }),
    onError: (err) => showError(mapApiError(err))
  })

  if (query.isLoading) return <Loader />
  const items = query.data?.content || []

  return (
    <div>
      <PageHeader title="My Bookings">
        <Link to="/bookings/new"><button>➕ New Booking</button></Link>
      </PageHeader>

      {!items.length ? (
        <Empty title="No bookings yet" subtitle="Create a booking to get started" />
      ) : (
        <div className="card-grid">
          {items.map(b => (
            <div key={b.id} className="card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 10 }}>
                <h3 style={{ margin: 0, fontSize: '0.95rem' }}>{b.resourceName}</h3>
                <Badge value={b.status} />
              </div>
              <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: 12 }}>
                <div>📅 {b.bookingDate}</div>
                <div>🕐 {b.startTime} — {b.endTime}</div>
                {(b.rejectionReason || b.cancellationReason) && (
                  <div style={{ marginTop: 4, color: 'var(--danger)', fontSize: '0.8rem' }}>
                    {b.rejectionReason || b.cancellationReason}
                  </div>
                )}
              </div>
              <div className="actions-inline">
                <Link to={`/bookings/${b.id}`}><button className="btn-small btn-ghost">View</button></Link>
                {b.status === 'APPROVED' && (
                  <button className="btn-small btn-danger" onClick={() => cancelMutation.mutate(b.id)}>Cancel</button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
