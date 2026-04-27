import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './context/AuthContext'
import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'
import NotFoundPage from './pages/NotFoundPage'
import ResourceListPage from './pages/ResourceListPage'
import ResourceDetailPage from './pages/ResourceDetailPage'
import ResourceAdminPage from './pages/ResourceAdminPage'
import BookingCreatePage from './pages/BookingCreatePage'
import MyBookingsPage from './pages/MyBookingsPage'
import BookingDetailPage from './pages/BookingDetailPage'
import BookingAdminPage from './pages/BookingAdminPage'
import TicketCreatePage from './pages/TicketCreatePage'
import TicketListPage from './pages/TicketListPage'
import TicketDetailPage from './pages/TicketDetailPage'
import TicketAdminPage from './pages/TicketAdminPage'
import NotificationsPage from './pages/NotificationsPage'
import UserAdminPage from './pages/UserAdminPage'
import ProfilePage from './pages/ProfilePage'
import AnalyticsDashboardPage from './pages/AnalyticsDashboardPage'
import Layout from './components/Layout'
import ProtectedRoute from './components/ProtectedRoute'

function App() {
  const { isAuthenticated } = useAuth()

  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      
      <Route element={<ProtectedRoute />}>
        <Route element={<Layout />}>
          <Route path="/dashboard" element={<DashboardPage />} />
          
          <Route path="/resources" element={<ResourceListPage />} />
          <Route path="/resources/:id" element={<ResourceDetailPage />} />
          
          <Route path="/bookings/new" element={<BookingCreatePage />} />
          <Route path="/bookings/my" element={<MyBookingsPage />} />
          <Route path="/bookings/:id" element={<BookingDetailPage />} />
          
          <Route path="/tickets/new" element={<TicketCreatePage />} />
          <Route path="/tickets" element={<TicketListPage />} />
          <Route path="/tickets/:id" element={<TicketDetailPage />} />
          
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/notifications" element={<NotificationsPage />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute roles={['ADMIN']} />}>
        <Route element={<Layout />}>
          <Route path="/analytics" element={<AnalyticsDashboardPage />} />
          <Route path="/resources/manage" element={<ResourceAdminPage />} />
          <Route path="/bookings/manage" element={<BookingAdminPage />} />
          <Route path="/tickets/manage" element={<TicketAdminPage />} />
          <Route path="/users" element={<UserAdminPage />} />
        </Route>
      </Route>

      <Route path="/" element={<Navigate to={isAuthenticated ? '/dashboard' : '/login'} replace />} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
}

export default App
