import clsx from 'clsx'

export function Badge({ value, status, colors }) {
  // Support both 'value' and 'status' for backwards compatibility
  const displayValue = status || value
  const label = String(displayValue).replace(/_/g, ' ')
  
  // If custom colors are provided, use inline styles
  if (colors && colors[displayValue]) {
    const { bg, fg } = colors[displayValue]
    return (
      <span style={{
        backgroundColor: bg,
        color: fg,
        padding: '4px 8px',
        borderRadius: '4px',
        fontSize: '12px',
        fontWeight: 600
      }}>
        {label}
      </span>
    )
  }
  
  // Otherwise use CSS classes
  return <span className={clsx('badge', `badge-${String(displayValue).toLowerCase()}`)}>{label}</span>
}

export function Loader() {
  return (
    <div className="center">
      <div className="spinner" />
      <p style={{ marginTop: 12, fontSize: '0.85rem', color: 'var(--text-tertiary)' }}>Loading…</p>
    </div>
  )
}

export function Empty({ title, subtitle, message }) {
  // Support 'message' for backwards compatibility
  const displayTitle = title || 'Nothing here'
  const displaySubtitle = subtitle || message || 'No items to display'
  
  return (
    <div className="empty">
      <div className="empty-icon">📭</div>
      <h3>{displayTitle}</h3>
      <p>{displaySubtitle}</p>
    </div>
  )
}

export function Error({ msg, onRetry }) {
  return (
    <div className="error-state">
      <div className="empty-icon">⚠️</div>
      <h3>Something went wrong</h3>
      <p>{msg}</p>
      {onRetry && <button onClick={onRetry} style={{ marginTop: 12 }}>Retry</button>}
    </div>
  )
}

export function PageHeader({ title, children }) {
  return (
    <div className="page-header">
      <h1>{title}</h1>
      {children && <div className="actions">{children}</div>}
    </div>
  )
}

export function StatCard({ label, value, variant }) {
  return (
    <div className={clsx('stat-card', variant && `stat-${variant}`)}>
      <div className="stat-label">{label}</div>
      <div className="stat-value">{value ?? 0}</div>
    </div>
  )
}
