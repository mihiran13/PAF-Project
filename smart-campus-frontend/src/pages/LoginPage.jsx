import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useNavigate, useLocation } from 'react-router-dom'
import { GoogleLogin } from '@react-oauth/google'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import { authService } from '../services/api'
import { mapApiError } from '../utils/errorMapper'
import { z } from 'zod'
import { useState } from 'react'

const schema = z.object({
  email: z.string().email('Invalid email'),
  password: z.string().min(1, 'Password required')
})

export default function LoginPage() {
  const { login } = useAuth()
  const { error: showError, success: showSuccess } = useToast()
  const navigate = useNavigate()
  const location = useLocation()
  const [isGoogleLoading, setIsGoogleLoading] = useState(false)
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm({ resolver: zodResolver(schema) })

  const onSubmit = async (data) => {
    try {
      await login(data)
      navigate(location.state?.from?.pathname || '/dashboard', { replace: true })
    } catch (err) {
      showError(mapApiError(err))
    }
  }

  const handleGoogleSuccess = async (credentialResponse) => {
    setIsGoogleLoading(true)
    try {
      const auth = await authService.googleLogin(credentialResponse.credential)
      await login(auth)
      showSuccess('Signed in with Google!')
      navigate(location.state?.from?.pathname || '/dashboard', { replace: true })
    } catch (err) {
      showError(mapApiError(err))
    } finally {
      setIsGoogleLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={handleSubmit(onSubmit)}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 4 }}>
          <div className="sidebar-brand-icon" style={{ width: 40, height: 40, fontSize: '1rem' }}>SC</div>
          <h1>Smart Campus</h1>
        </div>
        <p className="auth-sub">Sign in to your account to continue</p>
        
        <div className="form-group">
          <label>Email Address</label>
          <input type="email" placeholder="you@example.com" {...register('email')} />
          {errors.email && <p className="error-text">{errors.email.message}</p>}
        </div>
        
        <div className="form-group">
          <label>Password</label>
          <input type="password" placeholder="••••••••" {...register('password')} />
          {errors.password && <p className="error-text">{errors.password.message}</p>}
        </div>

        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Signing in…' : 'Sign In'}
        </button>

        <div className="auth-divider">
          <span>or continue with</span>
        </div>

        <div style={{ display: 'flex', justifyContent: 'center' }}>
          <GoogleLogin
            onSuccess={handleGoogleSuccess}
            onError={() => showError('Google sign-in failed')}
            disabled={isGoogleLoading}
            theme="filled_black"
            shape="pill"
            size="large"
          />
        </div>
      </form>
    </div>
  )
}
