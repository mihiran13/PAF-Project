import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useNavigate } from 'react-router-dom'
import { useQuery, useMutation } from '@tanstack/react-query'
import { ticketService, resourceService } from '../services/api'
import { useToast } from '../context/ToastContext'
import { mapApiError } from '../utils/errorMapper'
import { z } from 'zod'
import { PageHeader } from '../components/UI'

const categories = ['ELECTRICAL', 'PLUMBING', 'IT_EQUIPMENT', 'FURNITURE', 'HVAC', 'CLEANING', 'SAFETY', 'OTHER']
const priorities = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']

const schema = z.object({
  resourceId: z.coerce.number().optional(),
  locationDescription: z.string().optional(),
  category: z.string().min(1, 'Category required'),
  title: z.string().min(5, 'Title required (min 5 chars)'),
  description: z.string().min(10, 'Description required (min 10 chars)'),
  priority: z.string().min(1, 'Priority required'),
  preferredContact: z.string().min(5, 'Contact info required')
})

export default function TicketCreatePage() {
  const navigate = useNavigate()
  const { error: showError } = useToast()

  const resources = useQuery({
    queryKey: ['ticket-resources'],
    queryFn: () => resourceService.list({ size: 100 })
  })

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm({ resolver: zodResolver(schema) })

  const mutation = useMutation({
    mutationFn: (data) => ticketService.create(data, []),
    onSuccess: (result) => navigate(`/tickets/${result.id}`),
    onError: (err) => showError(mapApiError(err))
  })

  return (
    <div>
      <PageHeader title="Report an Issue" />
      <div className="panel" style={{ maxWidth: 760, width: '100%' }}>
        <form onSubmit={handleSubmit(data => mutation.mutate(data))} className="form">
          <div className="form-row">
            <div className="form-group">
              <label>Category</label>
              <select {...register('category')}>
                <option value="">Select category…</option>
                {categories.map(c => <option key={c} value={c}>{c.replace(/_/g, ' ')}</option>)}
              </select>
              {errors.category && <p className="error-text">{errors.category.message}</p>}
            </div>
            <div className="form-group">
              <label>Priority</label>
              <select {...register('priority')}>
                <option value="">Select priority…</option>
                {priorities.map(p => <option key={p} value={p}>{p}</option>)}
              </select>
              {errors.priority && <p className="error-text">{errors.priority.message}</p>}
            </div>
          </div>

          <div className="form-group">
            <label>Title</label>
            <input placeholder="Brief summary of the issue" {...register('title')} />
            {errors.title && <p className="error-text">{errors.title.message}</p>}
          </div>

          <div className="form-group">
            <label>Description</label>
            <textarea placeholder="Describe the issue in detail…" {...register('description')} />
            {errors.description && <p className="error-text">{errors.description.message}</p>}
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>Related Resource (optional)</label>
              <select {...register('resourceId')}>
                <option value="">None</option>
                {resources.data?.content?.map(r => (
                  <option key={r.id} value={r.id}>{r.name}</option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label>Location Description</label>
              <input placeholder="e.g. Room 204, 2nd floor" {...register('locationDescription')} />
            </div>
          </div>

          <div className="form-group">
            <label>Preferred Contact</label>
            <input placeholder="Email or phone number" {...register('preferredContact')} />
            {errors.preferredContact && <p className="error-text">{errors.preferredContact.message}</p>}
          </div>

          <button type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Submitting…' : '🎫 Submit Ticket'}
          </button>
        </form>
      </div>
    </div>
  )
}
