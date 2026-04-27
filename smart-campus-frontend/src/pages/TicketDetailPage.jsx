import { useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { ticketService, commentService, userService } from '../services/api'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import { mapApiError } from '../utils/errorMapper'
import { Loader, Badge, PageHeader } from '../components/UI'

export default function TicketDetailPage() {
  const { id } = useParams()
  const { user } = useAuth()
  const { error: showError } = useToast()
  const queryClient = useQueryClient()
  const [comment, setComment] = useState('')

  const ticket = useQuery({ queryKey: ['ticket', id], queryFn: () => ticketService.get(id) })

  const technicians = useQuery({
    queryKey: ['technicians'],
    queryFn: () => userService.list({ role: 'TECHNICIAN', size: 100 }),
    enabled: user?.role === 'ADMIN'
  })

  const statusMutation = useMutation({
    mutationFn: (status) => ticketService.updateStatus(id, status),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['ticket', id] }),
    onError: (err) => showError(mapApiError(err))
  })

  const assignMutation = useMutation({
    mutationFn: (techId) => ticketService.assign(id, Number(techId)),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['ticket', id] }),
    onError: (err) => showError(mapApiError(err))
  })

  const resolveMutation = useMutation({
    mutationFn: (notes) => ticketService.resolve(id, notes),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['ticket', id] }),
    onError: (err) => showError(mapApiError(err))
  })

  const commentMutation = useMutation({
    mutationFn: (content) => commentService.create(id, content),
    onSuccess: () => { setComment(''); queryClient.invalidateQueries({ queryKey: ['ticket', id] }) },
    onError: (err) => showError(mapApiError(err))
  })

  const deleteCommentMutation = useMutation({
    mutationFn: commentService.delete,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['ticket', id] }),
    onError: (err) => showError(mapApiError(err))
  })

  if (ticket.isLoading) return <Loader />
  const item = ticket.data
  const priorityColors = { CRITICAL: 'var(--danger)', HIGH: 'var(--warning)', MEDIUM: 'var(--info)', LOW: 'var(--text-tertiary)' }
  const isAdmin = user?.role === 'ADMIN'
  const isAssignedTechnician = user?.role === 'TECHNICIAN' && item.assignedTechnicianId === user?.id
  const canOperateTicket = isAdmin || isAssignedTechnician

  const canMoveToInProgress = item.status === 'OPEN' && canOperateTicket
  const canResolve = item.status === 'IN_PROGRESS' && canOperateTicket
  const canClose = item.status === 'RESOLVED' && isAdmin
  const canAssignTechnician = isAdmin

  return (
    <div>
      <PageHeader title={item.title}>
        <Link to="/tickets"><button className="btn-ghost">← Back</button></Link>
      </PageHeader>

      {/* Info Panel */}
      <div className="panel" style={{ borderLeft: `3px solid ${priorityColors[item.priority] || 'var(--border)'}` }}>
        <div style={{ display: 'flex', gap: 8, marginBottom: 16, flexWrap: 'wrap' }}>
          <Badge value={item.status} />
          <Badge value={item.priority} />
          <span className="badge" style={{ background: 'var(--bg-tertiary)', color: 'var(--text-secondary)' }}>{item.category}</span>
        </div>
        <div className="detail-grid">
          <div className="detail-item">
            <div className="detail-label">Description</div>
            <div className="detail-value">{item.description}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Assigned To</div>
            <div className="detail-value">{item.assignedTechnicianName || 'Unassigned'}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Contact</div>
            <div className="detail-value">{item.preferredContact || '-'}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Location</div>
            <div className="detail-value">{item.locationDescription || item.resourceName || '-'}</div>
          </div>
        </div>
      </div>

      {/* Admin/Tech Actions */}
      {(user?.role === 'ADMIN' || user?.role === 'TECHNICIAN') && (
        <div className="panel">
          <h3>Actions</h3>
          <div className="actions" style={{ flexWrap: 'wrap' }}>
            {canMoveToInProgress && (
              <button
                className="btn-small btn-ghost"
                onClick={() => statusMutation.mutate('IN_PROGRESS')}
                disabled={statusMutation.isPending || resolveMutation.isPending}
              >
                ▶ In Progress
              </button>
            )}
            {canClose && (
              <button
                className="btn-small btn-ghost"
                onClick={() => statusMutation.mutate('CLOSED')}
                disabled={statusMutation.isPending || resolveMutation.isPending}
              >
                ✓ Close
              </button>
            )}
            {canResolve && (
              <button
                className="btn-small btn-success"
                onClick={() => {
                  const notes = window.prompt('Resolution notes:') || ''
                  if (notes) resolveMutation.mutate(notes)
                }}
                disabled={resolveMutation.isPending || statusMutation.isPending}
              >
                ✅ Resolve
              </button>
            )}
            {canAssignTechnician && (
              <select
                onChange={(e) => e.target.value && assignMutation.mutate(e.target.value)}
                defaultValue=""
                disabled={assignMutation.isPending}
                style={{ maxWidth: 200 }}
              >
                <option value="">Assign technician…</option>
                {technicians.data?.content?.map(t => (
                  <option key={t.id} value={t.id}>{t.name}</option>
                ))}
              </select>
            )}
          </div>
          {!canMoveToInProgress && !canResolve && !canClose && (
            <p style={{ fontSize: '0.85rem', color: 'var(--text-tertiary)', marginTop: 10 }}>
              No available status actions for this ticket in its current state.
            </p>
          )}
        </div>
      )}

      {/* Comments */}
      <div className="panel">
        <h3>Comments</h3>
        <form onSubmit={(e) => { e.preventDefault(); if (comment.trim()) commentMutation.mutate(comment) }} style={{ display: 'flex', gap: 8, marginBottom: 16 }}>
          <input value={comment} onChange={(e) => setComment(e.target.value)} placeholder="Write a comment…" style={{ flex: 1 }} />
          <button type="submit" className="btn-small" disabled={!comment.trim()}>Post</button>
        </form>
        <div className="comments">
          {(item.comments || []).length === 0 && <p style={{ fontSize: '0.85rem', color: 'var(--text-tertiary)' }}>No comments yet.</p>}
          {(item.comments || []).map(c => (
            <div key={c.id} className="comment-item">
              <strong>{c.authorName}</strong>
              <p>{c.content}</p>
              <small>{c.createdAt}</small>
              {c.canDelete && (
                <div className="comment-actions">
                  <button className="btn-small btn-danger" onClick={() => deleteCommentMutation.mutate(c.id)}>Delete</button>
                </div>
              )}
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
