import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { bookingService } from '../services/api'
import { useToast } from '../context/ToastContext'
import { mapApiError } from '../utils/errorMapper'
import { Loader, Badge, PageHeader, Empty } from '../components/UI'

export default function BookingAdminPage() {
  const { error: showError } = useToast()
  const queryClient = useQueryClient()

  const query = useQuery({
    queryKey: ['all-bookings'],
    queryFn: () => bookingService.all({ size: 100 })
  })

  const approveMutation = useMutation({
    mutationFn: bookingService.approve,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['all-bookings'] }),
    onError: (err) => showError(mapApiError(err))
  })

  const rejectMutation = useMutation({
    mutationFn: (id) => bookingService.reject(id, window.prompt('Rejection reason:') || ''),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['all-bookings'] }),
    onError: (err) => showError(mapApiError(err))
  })

  if (query.isLoading) return <Loader />
  const items = query.data?.content || []

  return (
    <div>
      <PageHeader title="Booking Management" />

      {!items.length ? (
        <Empty title="No bookings" subtitle="No bookings to manage yet." />
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr><th>User</th><th>Resource</th><th>Date</th><th>Status</th><th>Actions</th></tr>
            </thead>
            <tbody>
              {items.map(b => (
                <tr key={b.id}>
                  <td>{b.userName}</td>
                  <td>{b.resourceName}</td>
                  <td>{b.bookingDate}</td>
                  <td><Badge value={b.status} /></td>
                  <td>
                    <div className="actions-inline">
                      <Link to={`/bookings/${b.id}`}><button className="btn-small btn-ghost">View</button></Link>
                      {b.status === 'PENDING' && (
                        <>
                          <button className="btn-small btn-success" onClick={() => approveMutation.mutate(b.id)}>Approve</button>
                          <button className="btn-small btn-danger" onClick={() => rejectMutation.mutate(b.id)}>Reject</button>
                        </>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
