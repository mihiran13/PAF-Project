import { useQuery } from '@tanstack/react-query'
import { resourceService } from '../services/api'
import { Link } from 'react-router-dom'
import { Loader, Empty, Badge, PageHeader } from '../components/UI'
import { useState, useMemo } from 'react'

export default function ResourceListPage() {
  const [filters, setFilters] = useState({ keyword: '', type: '', status: '' })
  
  const params = useMemo(() => ({
    ...filters, size: 50,
    keyword: filters.keyword || undefined,
    type: filters.type || undefined,
    status: filters.status || undefined
  }), [filters])

  const query = useQuery({
    queryKey: ['resources', params],
    queryFn: () => resourceService.list(params)
  })

  if (query.isLoading) return <Loader />
  const items = query.data?.content || []

  return (
    <div>
      <PageHeader title="Facilities & Assets" />

      <div className="filters">
        <input placeholder="🔍 Search resources..." value={filters.keyword} onChange={e => setFilters(f => ({ ...f, keyword: e.target.value }))} />
        <input placeholder="Type (e.g. Lab, Hall)" value={filters.type} onChange={e => setFilters(f => ({ ...f, type: e.target.value }))} />
        <select value={filters.status} onChange={e => setFilters(f => ({ ...f, status: e.target.value }))}>
          <option value="">All Statuses</option>
          <option value="ACTIVE">Active</option>
          <option value="OUT_OF_SERVICE">Out of Service</option>
        </select>
      </div>

      {!items.length ? (
        <Empty title="No resources found" subtitle="Try adjusting your search or filters" />
      ) : (
        <div className="card-grid">
          {items.map(r => (
            <Link key={r.id} to={`/resources/${r.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
              <div className="card card-clickable">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 12 }}>
                  <h3 style={{ margin: 0, fontSize: '1rem' }}>{r.name}</h3>
                  <Badge value={r.status} />
                </div>
                <div style={{ display: 'flex', gap: 20, fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                  <span>🏷️ {r.type}</span>
                  <span>👥 {r.capacity}</span>
                  <span>📍 {r.location}</span>
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}
