import { describe, it, expect } from 'vitest'
import { mapApiError } from '../utils/errorMapper'

describe('mapApiError', () => {
  it('maps 401 status', () => {
    const err = { response: { status: 401 } }
    expect(mapApiError(err)).toContain('Session')
  })

  it('maps 403 status', () => {
    const err = { response: { status: 403 } }
    expect(mapApiError(err)).toContain('permission')
  })

  it('returns fallback message', () => {
    const err = new Error('Test error')
    expect(mapApiError(err)).toBe('Test error')
  })
})
