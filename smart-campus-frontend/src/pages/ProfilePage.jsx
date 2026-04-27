import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation } from '@tanstack/react-query'
import { userService } from '../services/api'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import { mapApiError } from '../utils/errorMapper'
import { PageHeader } from '../components/UI'

const schema = z.object({
  name: z.string().trim().min(2, 'Name must be at least 2 characters').max(255, 'Name is too long'),
  email: z.string().trim().email('Invalid email address').max(255, 'Email is too long'),
  profileImageUrl: z
    .string()
    .trim()
    .max(500, 'Profile image URL is too long')
    .optional()
    .or(z.literal(''))
    .refine((value) => !value || /^https?:\/\//i.test(value), 'Profile image URL must start with http:// or https://')
})

export default function ProfilePage() {
  const { user, setUser } = useAuth()
  const { success: showSuccess, error: showError } = useToast()

  const { register, handleSubmit, reset, formState: { errors, isSubmitting, isDirty } } = useForm({
    resolver: zodResolver(schema),
    defaultValues: {
      name: user?.name || '',
      email: user?.email || '',
      profileImageUrl: user?.profileImageUrl || ''
    }
  })

  useEffect(() => {
    reset({
      name: user?.name || '',
      email: user?.email || '',
      profileImageUrl: user?.profileImageUrl || ''
    })
  }, [user, reset])

  const mutation = useMutation({
    mutationFn: (payload) => userService.updateMyProfile(payload),
    onSuccess: (updatedUser) => {
      setUser(updatedUser)
      showSuccess('Profile updated successfully')
      reset({
        name: updatedUser?.name || '',
        email: updatedUser?.email || '',
        profileImageUrl: updatedUser?.profileImageUrl || ''
      })
    },
    onError: (err) => showError(mapApiError(err))
  })

  const onSubmit = async (values) => {
    await mutation.mutateAsync({
      name: values.name,
      email: values.email,
      profileImageUrl: values.profileImageUrl || null
    })
  }

  return (
    <div>
      <PageHeader title="My Profile" />

      <div className="panel" style={{ maxWidth: 760, width: '100%' }}>
        <form onSubmit={handleSubmit(onSubmit)} className="form">
          <div className="form-group">
            <label>Full Name</label>
            <input placeholder="Your full name" {...register('name')} />
            {errors.name && <p className="error-text">{errors.name.message}</p>}
          </div>

          <div className="form-group">
            <label>Email</label>
            <input type="email" placeholder="you@example.com" {...register('email')} />
            {errors.email && <p className="error-text">{errors.email.message}</p>}
          </div>

          <div className="form-group">
            <label>Profile Image URL (optional)</label>
            <input placeholder="https://example.com/avatar.png" {...register('profileImageUrl')} />
            {errors.profileImageUrl && <p className="error-text">{errors.profileImageUrl.message}</p>}
          </div>

          <div className="form-group">
            <label>Role</label>
            <input value={user?.role || ''} disabled />
          </div>

          <button type="submit" disabled={isSubmitting || mutation.isPending || !isDirty}>
            {mutation.isPending ? 'Saving...' : 'Save Profile'}
          </button>
        </form>
      </div>
    </div>
  )
}
