import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { resourceService } from '../services/api'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import { mapApiError } from '../utils/errorMapper'
import { PageHeader, Badge, Loader, Empty } from '../components/UI'
import { RESOURCE_TYPES, RESOURCE_STATUSES, ROLES } from '../utils/constants'

const schema = z.object({
  name: z.string().min(2, 'Name must be at least 2 characters').max(255, 'Name is too long'),
  type: z.string().refine(v => Object.values(RESOURCE_TYPES).includes(v), 'Invalid resource type'),
  capacity: z.number().int().positive('Capacity must be greater than 0'),
  location: z.string().min(2, 'Location required').max(255),
  description: z.string().max(1000, 'Description is too long').optional().or(z.literal('')),
  status: z.string().refine(v => Object.values(RESOURCE_STATUSES).includes(v), 'Invalid status')
})

export default function ResourceAdminPage() {
  const { user } = useAuth()
  const { success: showSuccess, error: showError } = useToast()
  const queryClient = useQueryClient()
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [editingId, setEditingId] = useState(null)
  const [searchTerm, setSearchTerm] = useState('')
  const [filterStatus, setFilterStatus] = useState('')

  // Fetch all resources
  const { data: response, isLoading } = useQuery({
    queryKey: ['resources', searchTerm, filterStatus],
    queryFn: () => resourceService.list({
      keyword: searchTerm || undefined,
      status: filterStatus || undefined,
      page: 0,
      size: 50
    }),
    enabled: user?.role === ROLES.ADMIN
  })

  const resources = response?.data || []

  // Form setup
  const { register, handleSubmit, reset, formState: { errors, isSubmitting }, watch } = useForm({
    resolver: zodResolver(schema),
    defaultValues: {
      name: '',
      type: RESOURCE_TYPES.LECTURE_HALL,
      capacity: 1,
      location: '',
      description: '',
      status: RESOURCE_STATUSES.ACTIVE
    }
  })

  // Create/Update mutation
  const mutation = useMutation({
    mutationFn: (data) => {
      if (editingId) {
        return resourceService.update(editingId, data)
      } else {
        return resourceService.create(data)
      }
    },
    onSuccess: (result) => {
      showSuccess(editingId ? 'Resource updated successfully' : 'Resource created successfully')
      queryClient.invalidateQueries({ queryKey: ['resources'] })
      setIsModalOpen(false)
      setEditingId(null)
      reset()
    },
    onError: (err) => showError(mapApiError(err))
  })

  // Delete mutation
  const deleteMutation = useMutation({
    mutationFn: (id) => resourceService.delete(id),
    onSuccess: () => {
      showSuccess('Resource deleted successfully')
      queryClient.invalidateQueries({ queryKey: ['resources'] })
    },
    onError: (err) => showError(mapApiError(err))
  })

  // Status change mutation
  const statusMutation = useMutation({
    mutationFn: ({ id, status }) => resourceService.updateStatus(id, status),
    onSuccess: () => {
      showSuccess('Resource status updated successfully')
      queryClient.invalidateQueries({ queryKey: ['resources'] })
    },
    onError: (err) => showError(mapApiError(err))
  })

  const onSubmit = async (data) => {
    await mutation.mutateAsync(data)
  }

  const handleEdit = (resource) => {
    reset({
      name: resource.name,
      type: resource.type,
      capacity: resource.capacity,
      location: resource.location,
      description: resource.description || '',
      status: resource.status
    })
    setEditingId(resource.id)
    setIsModalOpen(true)
  }

  const handleDelete = (id) => {
    if (window.confirm('Are you sure you want to delete this resource?')) {
      deleteMutation.mutate(id)
    }
  }

  const handleStatusChange = (id, currentStatus) => {
    const newStatus = currentStatus === RESOURCE_STATUSES.ACTIVE ? RESOURCE_STATUSES.OUT_OF_SERVICE : RESOURCE_STATUSES.ACTIVE
    statusMutation.mutate({ id, status: newStatus })
  }

  const handleCloseModal = () => {
    setIsModalOpen(false)
    setEditingId(null)
    reset()
  }

  if (isLoading) return <Loader />

  const typeLabel = (type) => {
    const labels = {
      [RESOURCE_TYPES.LECTURE_HALL]: '📚 Lecture Hall',
      [RESOURCE_TYPES.LAB]: '🔬 Lab',
      [RESOURCE_TYPES.MEETING_ROOM]: '🏢 Meeting Room',
      [RESOURCE_TYPES.EQUIPMENT]: '🎛️ Equipment'
    }
    return labels[type] || type
  }

  return (
    <div>
      <PageHeader title="Resource Management" />

      {/* Controls */}
      <div className="panel" style={{ marginBottom: 24, padding: 16 }}>
        <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', alignItems: 'center' }}>
          <input
            type="text"
            placeholder="Search resources..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{ flex: 1, minWidth: 200 }}
          />
          <select
            value={filterStatus}
            onChange={(e) => setFilterStatus(e.target.value)}
            style={{ padding: '8px 12px' }}
          >
            <option value="">All Status</option>
            <option value={RESOURCE_STATUSES.ACTIVE}>Active</option>
            <option value={RESOURCE_STATUSES.OUT_OF_SERVICE}>Out of Service</option>
          </select>
          <button onClick={() => { setEditingId(null); setIsModalOpen(true) }} style={{ background: '#0066cc', color: 'white', border: 'none', padding: '8px 16px', borderRadius: 4, cursor: 'pointer' }}>
            + New Resource
          </button>
        </div>
      </div>

      {/* Resources Table */}
      {resources.length === 0 ? (
        <Empty message="No resources found" />
      ) : (
        <div className="panel">
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ borderBottom: '2px solid #e0e0e0' }}>
                <th style={{ padding: 12, textAlign: 'left', fontWeight: 600 }}>Name</th>
                <th style={{ padding: 12, textAlign: 'left', fontWeight: 600 }}>Type</th>
                <th style={{ padding: 12, textAlign: 'left', fontWeight: 600 }}>Capacity</th>
                <th style={{ padding: 12, textAlign: 'left', fontWeight: 600 }}>Location</th>
                <th style={{ padding: 12, textAlign: 'left', fontWeight: 600 }}>Status</th>
                <th style={{ padding: 12, textAlign: 'left', fontWeight: 600 }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {resources.map(resource => (
                <tr key={resource.id} style={{ borderBottom: '1px solid #f0f0f0', '&:hover': { backgroundColor: '#f9f9f9' } }}>
                  <td style={{ padding: 12 }}>{resource.name}</td>
                  <td style={{ padding: 12 }}>{typeLabel(resource.type)}</td>
                  <td style={{ padding: 12 }}>{resource.capacity}</td>
                  <td style={{ padding: 12 }}>{resource.location}</td>
                  <td style={{ padding: 12 }}>
                    <Badge 
                      status={resource.status} 
                      colors={{
                        [RESOURCE_STATUSES.ACTIVE]: { bg: '#e8f5e9', fg: '#2e7d32' },
                        [RESOURCE_STATUSES.OUT_OF_SERVICE]: { bg: '#ffebee', fg: '#c62828' }
                      }}
                    />
                  </td>
                  <td style={{ padding: 12, display: 'flex', gap: 8 }}>
                    <button
                      onClick={() => handleEdit(resource)}
                      style={{ padding: '4px 8px', background: '#f5f5f5', border: '1px solid #ddd', borderRadius: 3, cursor: 'pointer', fontSize: 12 }}
                    >
                      Edit
                    </button>
                    <button
                      onClick={() => handleStatusChange(resource.id, resource.status)}
                      disabled={statusMutation.isPending}
                      style={{ padding: '4px 8px', background: resource.status === RESOURCE_STATUSES.ACTIVE ? '#fff3e0' : '#e8f5e9', border: '1px solid #ddd', borderRadius: 3, cursor: 'pointer', fontSize: 12 }}
                    >
                      {resource.status === RESOURCE_STATUSES.ACTIVE ? 'Deactivate' : 'Activate'}
                    </button>
                    <button
                      onClick={() => handleDelete(resource.id)}
                      disabled={deleteMutation.isPending}
                      style={{ padding: '4px 8px', background: '#ffebee', border: '1px solid #ddd', borderRadius: 3, cursor: 'pointer', fontSize: 12, color: '#c62828' }}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Modal */}
      {isModalOpen && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div style={{ background: 'white', borderRadius: 8, padding: 24, maxWidth: 500, width: '90%', maxHeight: '90vh', overflowY: 'auto' }}>
            <h2 style={{ marginTop: 0, marginBottom: 16 }}>{editingId ? 'Edit Resource' : 'Create Resource'}</h2>
            
            <form onSubmit={handleSubmit(onSubmit)} style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              <div>
                <label style={{ display: 'block', marginBottom: 4, fontWeight: 500 }}>Name *</label>
                <input {...register('name')} placeholder="e.g., Main Lecture Hall" />
                {errors.name && <p style={{ color: '#c62828', fontSize: 12, margin: '4px 0 0' }}>{errors.name.message}</p>}
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: 4, fontWeight: 500 }}>Type *</label>
                <select {...register('type')}>
                  <option value={RESOURCE_TYPES.LECTURE_HALL}>Lecture Hall</option>
                  <option value={RESOURCE_TYPES.LAB}>Lab</option>
                  <option value={RESOURCE_TYPES.MEETING_ROOM}>Meeting Room</option>
                  <option value={RESOURCE_TYPES.EQUIPMENT}>Equipment</option>
                </select>
                {errors.type && <p style={{ color: '#c62828', fontSize: 12, margin: '4px 0 0' }}>{errors.type.message}</p>}
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: 4, fontWeight: 500 }}>Capacity *</label>
                <input {...register('capacity', { valueAsNumber: true })} type="number" min="1" placeholder="e.g., 50" />
                {errors.capacity && <p style={{ color: '#c62828', fontSize: 12, margin: '4px 0 0' }}>{errors.capacity.message}</p>}
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: 4, fontWeight: 500 }}>Location *</label>
                <input {...register('location')} placeholder="e.g., Building A, Floor 2" />
                {errors.location && <p style={{ color: '#c62828', fontSize: 12, margin: '4px 0 0' }}>{errors.location.message}</p>}
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: 4, fontWeight: 500 }}>Description</label>
                <textarea {...register('description')} placeholder="Optional description" style={{ minHeight: 80 }} />
                {errors.description && <p style={{ color: '#c62828', fontSize: 12, margin: '4px 0 0' }}>{errors.description.message}</p>}
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: 4, fontWeight: 500 }}>Status *</label>
                <select {...register('status')}>
                  <option value={RESOURCE_STATUSES.ACTIVE}>Active</option>
                  <option value={RESOURCE_STATUSES.OUT_OF_SERVICE}>Out of Service</option>
                </select>
                {errors.status && <p style={{ color: '#c62828', fontSize: 12, margin: '4px 0 0' }}>{errors.status.message}</p>}
              </div>

              <div style={{ display: 'flex', gap: 12, justifyContent: 'flex-end', marginTop: 12 }}>
                <button
                  type="button"
                  onClick={handleCloseModal}
                  style={{ padding: '8px 16px', background: '#f5f5f5', border: '1px solid #ddd', borderRadius: 4, cursor: 'pointer' }}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={isSubmitting || mutation.isPending}
                  style={{ padding: '8px 16px', background: '#0066cc', color: 'white', border: 'none', borderRadius: 4, cursor: 'pointer' }}
                >
                  {mutation.isPending ? 'Saving...' : editingId ? 'Update' : 'Create'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
