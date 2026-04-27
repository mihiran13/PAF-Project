import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useNavigate } from 'react-router-dom'
import { useQuery, useMutation } from '@tanstack/react-query'
import { bookingService, resourceService } from '../services/api'
import { useToast } from '../context/ToastContext'
import { mapApiError } from '../utils/errorMapper'
import { z } from 'zod'
import { PageHeader } from '../components/UI'

const schema = z.object({
  resourceId: z.coerce.number().positive('Resource required'),
  bookingDate: z.string().min(1, 'Date required'),
  startTime: z.string().min(1, 'Start time required'),
  endTime: z.string().min(1, 'End time required'),
  purpose: z.string().min(5, 'Purpose required (min 5 chars)'),
  expectedAttendees: z.coerce.number().min(1, 'At least 1 attendee')
}).refine(d => d.endTime > d.startTime, { path: ['endTime'], message: 'End time must be after start' })

export default function BookingCreatePage() {
  const navigate = useNavigate()
  const { error: showError } = useToast()

  const toMinutes = (value) => {
    if (!value || typeof value !== 'string' || !value.includes(':')) return null
    const [h, m] = value.split(':').map(Number)
    if (Number.isNaN(h) || Number.isNaN(m)) return null
    return (h * 60) + m
  }
  
  const resources = useQuery({
    queryKey: ['bookable-resources'],
    queryFn: () => resourceService.list({ status: 'ACTIVE', size: 100 })
  })

  const { register, handleSubmit, watch, formState: { errors, isSubmitting } } = useForm({ resolver: zodResolver(schema) })
  
  const mutation = useMutation({
    mutationFn: bookingService.create,
    onSuccess: (data) => navigate(`/bookings/${data.id}`),
    onError: (err) => showError(mapApiError(err))
  })

  const onSubmit = async (data) => {
    if (availabilityIssue) {
      showError(availabilityIssue)
      return
    }
    await mutation.mutateAsync(data)
  }
  const resourceId = watch('resourceId')
  const bookingDate = watch('bookingDate')
  const startTime = watch('startTime')
  const endTime = watch('endTime')
  const resource = resources.data?.content?.find(r => r.id === Number(resourceId))

  const availabilityIssue = (() => {
    if (!resource || !bookingDate || !startTime || !endTime) return null

    const windows = resource.availabilityWindows || []
    if (!windows.length) return null

    const selectedDate = new Date(`${bookingDate}T00:00:00`)
    if (Number.isNaN(selectedDate.getTime())) return null

    const dayNames = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY']
    const selectedDay = dayNames[selectedDate.getDay()]
    const dayWindows = windows.filter(w => w.dayOfWeek === selectedDay)

    if (!dayWindows.length) {
      return `This resource is not available on ${selectedDay.toLowerCase()}.`
    }

    const startMins = toMinutes(startTime)
    const endMins = toMinutes(endTime)
    if (startMins == null || endMins == null) return null

    const inAnyWindow = dayWindows.some(w => {
      const windowStart = toMinutes(w.startTime)
      const windowEnd = toMinutes(w.endTime)
      if (windowStart == null || windowEnd == null) return false
      return startMins >= windowStart && endMins <= windowEnd
    })

    if (!inAnyWindow) {
      return `Selected time is outside allowed hours for ${selectedDay.toLowerCase()}.`
    }

    return null
  })()

  return (
    <div>
      <PageHeader title="Create Booking" />
      <div className="panel" style={{ maxWidth: 760, width: '100%' }}>
        <form onSubmit={handleSubmit(onSubmit)} className="form">
          <div className="form-group">
            <label>Resource</label>
            <select {...register('resourceId')}>
              <option value="">Select a resource…</option>
              {resources.data?.content?.map(r => (
                <option key={r.id} value={r.id}>{r.name} — {r.location}</option>
              ))}
            </select>
            {errors.resourceId && <p className="error-text">{errors.resourceId.message}</p>}
          </div>

          {resource && (
            <div className="card" style={{ padding: 14, background: 'var(--bg-tertiary)' }}>
              <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                📍 {resource.location} · 👥 Capacity: {resource.capacity} · 🏷️ {resource.type}
              </div>
              {!!resource.availabilityWindows?.length && (
                <div style={{ fontSize: '0.8rem', color: 'var(--text-tertiary)', marginTop: 6 }}>
                  Availability: {resource.availabilityWindows.map(w => `${w.dayOfWeek} ${w.startTime}-${w.endTime}`).join(' | ')}
                </div>
              )}
              {availabilityIssue && <p className="error-text" style={{ marginTop: 8 }}>{availabilityIssue}</p>}
            </div>
          )}

          <div className="form-group">
            <label>Date</label>
            <input type="date" {...register('bookingDate')} />
            {errors.bookingDate && <p className="error-text">{errors.bookingDate.message}</p>}
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>Start Time</label>
              <input type="time" {...register('startTime')} />
            </div>
            <div className="form-group">
              <label>End Time</label>
              <input type="time" {...register('endTime')} />
              {errors.endTime && <p className="error-text">{errors.endTime.message}</p>}
            </div>
          </div>

          <div className="form-group">
            <label>Purpose</label>
            <textarea placeholder="Describe the purpose of this booking…" {...register('purpose')} />
            {errors.purpose && <p className="error-text">{errors.purpose.message}</p>}
          </div>

          <div className="form-group">
            <label>Expected Attendees</label>
            <input type="number" min="1" placeholder="e.g. 25" {...register('expectedAttendees')} />
            {errors.expectedAttendees && <p className="error-text">{errors.expectedAttendees.message}</p>}
          </div>

          <button type="submit" disabled={isSubmitting || !!availabilityIssue}>
            {isSubmitting ? 'Submitting…' : '📅 Submit Booking'}
          </button>
        </form>
      </div>
    </div>
  )
}
