import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { userService } from '../services/api'
import { useToast } from '../context/ToastContext'
import { mapApiError } from '../utils/errorMapper'
import { Loader, Badge, PageHeader } from '../components/UI'

export default function UserAdminPage() {
  const { error: showError } = useToast()
  const queryClient = useQueryClient()

  const query = useQuery({ queryKey: ['users'], queryFn: () => userService.list({ size: 200 }) })

  const roleMutation = useMutation({
    mutationFn: ({ id, role }) => userService.updateRole(id, role),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['users'] }),
    onError: (err) => showError(mapApiError(err))
  })

  const deactivateMutation = useMutation({
    mutationFn: userService.deactivate,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['users'] }),
    onError: (err) => showError(mapApiError(err))
  })

  if (query.isLoading) return <Loader />
  const items = query.data?.content || []

  return (
    <div>
      <PageHeader title="User Administration" />
      <div className="card-grid">
        {items.map(u => (
          <div key={u.id} className="card">
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 12 }}>
              <div className="user-avatar" style={{ width: 40, height: 40, fontSize: '0.9rem' }}>
                {(u.name || 'U').split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase()}
              </div>
              <div style={{ flex: 1 }}>
                <strong style={{ fontSize: '0.95rem' }}>{u.name}</strong>
                <p style={{ margin: 0, fontSize: '0.8rem' }}>{u.email}</p>
              </div>
              <Badge value={u.isActive ? 'ACTIVE' : 'INACTIVE'} />
            </div>
            <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
              <select
                defaultValue={u.role}
                onChange={(e) => roleMutation.mutate({ id: u.id, role: e.target.value })}
                style={{ flex: 1 }}
              >
                <option value="USER">USER</option>
                <option value="TECHNICIAN">TECHNICIAN</option>
                <option value="ADMIN">ADMIN</option>
              </select>
              {u.isActive && (
                <button className="btn-small btn-danger" onClick={() => deactivateMutation.mutate(u.id)}>
                  Deactivate
                </button>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
