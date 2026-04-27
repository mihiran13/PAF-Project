export function mapApiError(error) {
  const status = error?.response?.status
  const apiData = error?.response?.data
  const message = apiData?.message || error?.message || 'An error occurred'
  const details = apiData?.details
  
  if (details && typeof details === 'object' && !Array.isArray(details)) {
    const firstDetail = Object.values(details).find(Boolean)
    if (typeof firstDetail === 'string' && firstDetail.trim().length > 0) {
      return firstDetail
    }
  }
  
  if (status === 400) return message || 'Invalid input. Please check your data.'
  if (status === 401) return 'Session expired. Please sign in again.'
  if (status === 403) return 'You do not have permission.'
  if (status === 404) return 'Resource not found.'
  if (status >= 500) return 'Server error. Please try again later.'
  
  return message
}
