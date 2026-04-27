import axios from 'axios'
import { API_BASE_URL } from '../utils/constants'
import { clearAuth, loadAuth } from '../utils/storage'

const http = axios.create({ baseURL: API_BASE_URL })

http.interceptors.request.use(config => {
  const auth = loadAuth()
  if (auth?.accessToken) {
    config.headers.Authorization = `Bearer ${auth.accessToken}`
  }
  return config
})

http.interceptors.response.use(
  res => res,
  err => {
    if (err?.response?.status === 401) {
      clearAuth()
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export const authService = {
  login: (payload) => http.post('/api/auth/login', payload).then(r => r.data.data),
  googleLogin: (token) => http.post('/api/auth/google-login', { token }).then(r => r.data.data),
  me: () => http.get('/api/auth/me').then(r => r.data.data),
  logout: () => http.post('/api/auth/logout')
}

export const resourceService = {
  list: (params) => http.get('/api/resources', { params }).then(r => r.data.data),
  get: (id) => http.get(`/api/resources/${id}`).then(r => r.data.data),
  create: (payload) => http.post('/api/resources', payload).then(r => r.data.data),
  update: (id, payload) => http.put(`/api/resources/${id}`, payload).then(r => r.data.data),
  updateStatus: (id, status) => http.patch(`/api/resources/${id}/status`, { status }).then(r => r.data.data),
  delete: (id) => http.delete(`/api/resources/${id}`),
  addWindow: (id, payload) => http.post(`/api/resources/${id}/availability`, payload).then(r => r.data.data)
}

export const bookingService = {
  create: (payload) => http.post('/api/bookings', payload).then(r => r.data.data),
  my: (params) => http.get('/api/bookings/my', { params }).then(r => r.data.data),
  all: (params) => http.get('/api/bookings', { params }).then(r => r.data.data),
  get: (id) => http.get(`/api/bookings/${id}`).then(r => r.data.data),
  approve: (id) => http.patch(`/api/bookings/${id}/approve`).then(r => r.data.data),
  reject: (id, reason) => http.patch(`/api/bookings/${id}/reject`, { rejectionReason: reason }).then(r => r.data.data),
  cancel: (id, reason) => http.patch(`/api/bookings/${id}/cancel`, { cancellationReason: reason }).then(r => r.data.data)
}

export const ticketService = {
  create: (ticket, files = []) => {
    const form = new FormData()
    form.append('ticket', new Blob([JSON.stringify(ticket)], { type: 'application/json' }))
    files.forEach(f => form.append('files', f))
    return http.post('/api/tickets', form, { headers: { 'Content-Type': 'multipart/form-data' } }).then(r => r.data.data)
  },
  my: (params) => http.get('/api/tickets/my', { params }).then(r => r.data.data),
  all: (params) => http.get('/api/tickets', { params }).then(r => r.data.data),
  assigned: (params) => http.get('/api/tickets/assigned', { params }).then(r => r.data.data),
  get: (id) => http.get(`/api/tickets/${id}`).then(r => r.data.data),
  assign: (id, techId) => http.patch(`/api/tickets/${id}/assign`, { technicianUserId: techId }).then(r => r.data.data),
  updateStatus: (id, status) => http.patch(`/api/tickets/${id}/status`, { status }).then(r => r.data.data),
  resolve: (id, notes) => http.patch(`/api/tickets/${id}/resolve`, { resolutionNotes: notes }).then(r => r.data.data),
  reject: (id, reason) => http.patch(`/api/tickets/${id}/reject`, { rejectionReason: reason }).then(r => r.data.data),
  delete: (id) => http.delete(`/api/tickets/${id}`).then(r => r.data.data)
}

export const commentService = {
  create: (ticketId, content) => http.post(`/api/tickets/${ticketId}/comments`, { content }).then(r => r.data.data),
  update: (id, content) => http.put(`/api/comments/${id}`, { content }).then(r => r.data.data),
  delete: (id) => http.delete(`/api/comments/${id}`)
}

export const notificationService = {
  list: (params) => http.get('/api/notifications', { params }).then(r => r.data.data),
  unreadCount: () => http.get('/api/notifications/unread-count').then(r => r.data.data.count),
  markRead: (id) => http.patch(`/api/notifications/${id}/read`).then(r => r.data.data),
  markAllRead: () => http.patch('/api/notifications/read-all')
}

export const userService = {
  list: (params) => http.get('/api/users', { params }).then(r => r.data.data),
  updateRole: (id, role) => http.patch(`/api/users/${id}/role`, { role }).then(r => r.data.data),
  deactivate: (id) => http.patch(`/api/users/${id}/deactivate`).then(r => r.data.data),
  updateMyProfile: (payload) => http.patch('/api/users/me/profile', payload).then(r => r.data.data)
}
