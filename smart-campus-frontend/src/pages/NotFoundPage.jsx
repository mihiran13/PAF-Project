import { Link } from 'react-router-dom'

export default function NotFoundPage() {
  return (
    <div className="auth-page">
      <div style={{ textAlign: 'center', animation: 'slideUp 0.4s ease' }}>
        <h1 style={{ fontSize: '5rem', marginBottom: 0, background: 'var(--accent-gradient)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>404</h1>
        <h2 style={{ color: 'var(--text-secondary)', marginBottom: 8 }}>Page Not Found</h2>
        <p style={{ color: 'var(--text-tertiary)', marginBottom: 24 }}>The page you're looking for doesn't exist or has been moved.</p>
        <Link to="/dashboard"><button>← Back to Dashboard</button></Link>
      </div>
    </div>
  )
}
