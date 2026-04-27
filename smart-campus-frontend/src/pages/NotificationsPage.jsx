import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { notificationService } from '../services/api'
import { useNavigate } from 'react-router-dom'
import { useToast } from '../context/ToastContext'
import { POLLING_MS } from '../utils/constants'
import { Loader, PageHeader, Empty } from '../components/UI'

export default function NotificationsPage() {
  const navigate = useNavigate()
  const { error: showError } = useToast()
  const queryClient = useQueryClient()

  const query = useQuery({
    queryKey: ['notifications'],
    queryFn: () => notificationService.list({ size: 100 }),
    refetchInterval: POLLING_MS
  })

  const markMutation = useMutation({
    mutationFn: notificationService.markRead,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['notifications'] })
  })

  const markAllMutation = useMutation({
    mutationFn: notificationService.markAllRead,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['notifications'] })
  })

  if (query.isLoading) return <Loader />
  const items = query.data?.content || []

  const typeIcons = { BOOKING: '📅', TICKET: '🎫' }

  return (
    <div>
      <PageHeader title="Notifications">
        <button className="btn-ghost" onClick={() => markAllMutation.mutate()}>✓ Mark all read</button>
      </PageHeader>

      {!items.length ? (
        <Empty title="No notifications" subtitle="You're all caught up!" />
      ) : (
        <div>
          {items.map(n => (
            <div
              key={n.id}
              className={`notification-item ${!n.isRead ? 'unread' : ''}`}
              onClick={() => {
                if (!n.isRead) markMutation.mutate(n.id)
                if (n.referenceType === 'BOOKING') navigate(`/bookings/${n.referenceId}`)
                if (n.referenceType === 'TICKET') navigate(`/tickets/${n.referenceId}`)
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <h4>{typeIcons[n.referenceType] || '🔔'} {n.title}</h4>
                {!n.isRead && <span className="badge badge-unread">New</span>}
              </div>
              <p>{n.message}</p>
              <small>{n.createdAt}</small>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
