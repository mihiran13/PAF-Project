import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { ticketService, userService } from '../services/api'
import { useToast } from '../context/ToastContext'
import { mapApiError } from '../utils/errorMapper'
import { Loader, Badge, Empty, Error, PageHeader } from '../components/UI'
import { useState } from 'react'

export default function TicketAdminPage() {
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const { success: showSuccess, error: showError } = useToast()
  const [statusFilter, setStatusFilter] = useState('all')

  const { data: ticketsData, isLoading: ticketsLoading, error: ticketsError } = useQuery({
    queryKey: ['tickets_admin', statusFilter],
    queryFn: async () => {
      const params = statusFilter !== 'all' ? { status: statusFilter } : {}
      const res = await ticketService.all(params)
      return res.content || res
    }
  })

  const { data: technicians } = useQuery({
    queryKey: ['technicians'],
    queryFn: async () => {
      const res = await userService.list({ role: 'TECHNICIAN' })
      return res.content || res
    }
  })

  const assignMutation = useMutation({
    mutationFn: (data) => ticketService.assign(data.ticketId, data.technicianId),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['tickets_admin'] }); showSuccess('Ticket assigned') },
    onError: (err) => showError(mapApiError(err))
  })

  const statusMutation = useMutation({
    mutationFn: (data) => ticketService.updateStatus(data.ticketId, data.status),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['tickets_admin'] }); showSuccess('Status updated') },
    onError: (err) => showError(mapApiError(err))
  })

  const resolveMutation = useMutation({
    mutationFn: (data) => ticketService.resolve(data.ticketId, data.notes),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['tickets_admin'] }); showSuccess('Ticket resolved') },
    onError: (err) => showError(mapApiError(err))
  })

  const rejectMutation = useMutation({
    mutationFn: (data) => ticketService.reject(data.ticketId, data.reason),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['tickets_admin'] }); showSuccess('Ticket rejected') },
    onError: (err) => showError(mapApiError(err))
  })

  const deleteMutation = useMutation({
    mutationFn: (ticketId) => ticketService.delete(ticketId),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['tickets_admin'] }); showSuccess('Ticket deleted') },
    onError: (err) => showError(mapApiError(err))
  })

  if (ticketsLoading) return <Loader />
  if (ticketsError) return <Error msg={mapApiError(ticketsError)} onRetry={() => window.location.reload()} />
  if (!ticketsData || ticketsData.length === 0) return <Empty title="No tickets" subtitle="No tickets found" />

  return (
    <div>
      <PageHeader title="Ticket Management" />

      <div className="filter-buttons">
        {['all', 'OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'REJECTED'].map(s => (
          <button key={s} className={statusFilter === s ? 'active' : ''} onClick={() => setStatusFilter(s)}>
            {s === 'all' ? 'All' : s.replace(/_/g, ' ')}
          </button>
        ))}
      </div>

      <div className="table-wrap">
        <table>
          <thead>
            <tr><th>Title</th><th>Category</th><th>Priority</th><th>Status</th><th>Assigned To</th><th>Created</th><th>Actions</th></tr>
          </thead>
          <tbody>
            {ticketsData.map(ticket => (
              <tr key={ticket.id}>
                <td><strong>{ticket.title}</strong></td>
                <td>{ticket.category}</td>
                <td><Badge value={ticket.priority} /></td>
                <td><Badge value={ticket.status} /></td>
                <td>
                  <select
                    value={ticket.assignedTechnicianId || ''}
                    onChange={(e) => { if (e.target.value) assignMutation.mutate({ ticketId: ticket.id, technicianId: e.target.value }) }}
                    disabled={assignMutation.isPending || ['CLOSED', 'REJECTED'].includes(ticket.status)}
                    style={{ minWidth: 130 }}
                  >
                    <option value="">Unassigned</option>
                    {technicians?.map(tech => (
                      <option key={tech.id} value={tech.id}>{tech.name}</option>
                    ))}
                  </select>
                </td>
                <td style={{ fontSize: '0.82rem', color: 'var(--text-tertiary)' }}>{new Date(ticket.createdAt).toLocaleDateString()}</td>
                <td>
                  <div className="actions-inline">
                    {ticket.status === 'OPEN' && (
                      <button
                        className="btn-small btn-ghost"
                        onClick={() => statusMutation.mutate({ ticketId: ticket.id, status: 'IN_PROGRESS' })}
                        disabled={statusMutation.isPending || resolveMutation.isPending || rejectMutation.isPending}
                      >
                        Start
                      </button>
                    )}

                    {ticket.status === 'IN_PROGRESS' && (
                      <button
                        className="btn-small btn-success"
                        onClick={() => {
                          const notes = window.prompt('Resolution notes:') || ''
                          if (notes.trim()) resolveMutation.mutate({ ticketId: ticket.id, notes: notes.trim() })
                        }}
                        disabled={statusMutation.isPending || resolveMutation.isPending || rejectMutation.isPending}
                      >
                        Resolve
                      </button>
                    )}

                    {ticket.status === 'RESOLVED' && (
                      <button
                        className="btn-small btn-ghost"
                        onClick={() => statusMutation.mutate({ ticketId: ticket.id, status: 'CLOSED' })}
                        disabled={statusMutation.isPending || resolveMutation.isPending || rejectMutation.isPending}
                      >
                        Close
                      </button>
                    )}

                    {ticket.status === 'OPEN' && (
                      <button
                        className="btn-small btn-danger"
                        onClick={() => {
                          const reason = window.prompt('Rejection reason:') || ''
                          if (reason.trim()) rejectMutation.mutate({ ticketId: ticket.id, reason: reason.trim() })
                        }}
                        disabled={statusMutation.isPending || resolveMutation.isPending || rejectMutation.isPending}
                      >
                        Reject
                      </button>
                    )}

                    <button className="btn-small btn-ghost" onClick={() => navigate(`/tickets/${ticket.id}`)}>View</button>
                    <button className="btn-small btn-danger" onClick={() => { if (confirm('Delete this ticket?')) deleteMutation.mutate(ticket.id) }} disabled={deleteMutation.isPending}>Delete</button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
