export function formatDate(value) {
  if (!value) return '-'
  return new Date(value).toLocaleDateString('en-GB')
}

export function formatDateTime(value) {
  if (!value) return '-'
  return new Date(value).toLocaleString('en-GB')
}

export function titleCase(str) {
  if (!str) return '-'
  return str.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase())
}
