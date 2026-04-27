# 📅 MEMBER 1: BOOKING SYSTEM - Quick Reference

## Overview
You are responsible for the **Booking System** - managing how students and staff can book campus resources.

**Total API Endpoints:** 7  
**Frontend Pages:** 4  
**Database Tables:** 1 main table  
**Est. Lines of Code:** ~2,000 (backend + frontend)

---

## 🎯 Core Responsibilities

### What You Build
- System to create and manage booking requests for resources
- Track booking status: PENDING → APPROVED/REJECTED → CANCELLED
- Prevent booking conflicts
- Cancel bookings with reason tracking

### Who Uses It
- **Students:** Create bookings for resources
- **Admins:** Approve/reject bookings
- **Systems:** Check availability

---

## 📂 Backend Files You Own

### Controllers
📄 `smart-campus/src/main/java/com/smartcampus/controller/BookingController.java`

**Endpoints to Implement:**
```java
@PostMapping("/bookings")           // Create new booking
@GetMapping("/bookings/my")         // Get my bookings
@GetMapping("/bookings")            // Get all (admin only)
@GetMapping("/bookings/{id}")       // Get booking details
@PatchMapping("/bookings/{id}/approve")  // Approve booking
@PatchMapping("/bookings/{id}/reject")   // Reject and provide reason
@PatchMapping("/bookings/{id}/cancel")   // Cancel and provide reason
```

### Service Class
📄 `smart-campus/src/main/java/com/smartcampus/service/BookingService.java`

**Key Methods:**
```java
public BookingResponse createBooking(BookingRequest request)
public BookingResponse approveBooking(Long id)
public BookingResponse rejectBooking(Long id, String reason)
public BookingResponse cancelBooking(Long id, String reason)
public List<BookingResponse> getUserBookings(Long userId)
public List<BookingResponse> getAllBookings(Pageable pageable)
public boolean isResourceAvailable(Long resourceId, LocalDateTime start, LocalDateTime end)
```

### Entity Model
📄 `smart-campus/src/main/java/com/smartcampus/model/Booking.java`

**Fields:**
```java
@Id Long id
@ManyToOne Resource resource
@ManyToOne User user
LocalDateTime startTime
LocalDateTime endTime
BookingStatus status  // PENDING, APPROVED, REJECTED, CANCELLED
String notes
String rejectionReason
String cancellationReason
LocalDateTime createdAt
LocalDateTime updatedAt
```

### Repository
📄 `smart-campus/src/main/java/com/smartcampus/repository/BookingRepository.java`

**Queries You Need:**
```java
List<Booking> findByUserId(Long userId)
List<Booking> findByStatus(BookingStatus status)
List<Booking> findByResourceId(Long resourceId)
List<Booking> findByResourceIdAndStatusAndTimeRange(...)
```

### DTOs
📄 `smart-campus/src/main/java/com/smartcampus/dto/request/BookingRequest.java`
📄 `smart-campus/src/main/java/com/smartcampus/dto/response/BookingResponse.java`

---

## 🎨 Frontend Files You Own

### Pages

**1. BookingCreatePage.jsx**
```jsx
// Path: smart-campus-frontend/src/pages/BookingCreatePage.jsx
// Features:
// - Form to create new booking
// - Resource selector dropdown
// - Date and time pickers (start, end)
// - Notes text area
// - Submit button
```

**2. BookingDetailPage.jsx**
```jsx
// Path: smart-campus-frontend/src/pages/BookingDetailPage.jsx
// Features:
// - Display booking details
// - Show status (PENDING, APPROVED, etc.)
// - Show resource info
// - Show dates and times
// - Cancel button (if applicable)
// - Show rejection reason (if rejected)
```

**3. MyBookingsPage.jsx**
```jsx
// Path: smart-campus-frontend/src/pages/MyBookingsPage.jsx
// Features:
// - List of user's bookings
// - Filter by status: All, Pending, Approved, Rejected, Cancelled
// - Search by resource name
// - Clickable row to view details
// - Quick cancel button
// - Pagination
```

**4. BookingAdminPage.jsx**
```jsx
// Path: smart-campus-frontend/src/pages/BookingAdminPage.jsx
// Features:
// - Admin only page
// - List all system bookings
// - Filter by status, date range, resource
// - Approve button (changes status to APPROVED)
// - Reject button with reason input (changes to REJECTED)
// - View booking details modal
```

### API Service
📄 `smart-campus-frontend/src/services/api.js`

**Add booking service:**
```javascript
export const bookingService = {
  create: (payload) => http.post('/api/bookings', payload),
  my: (params) => http.get('/api/bookings/my', { params }),
  all: (params) => http.get('/api/bookings', { params }),
  get: (id) => http.get(`/api/bookings/${id}`),
  approve: (id) => http.patch(`/api/bookings/${id}/approve`),
  reject: (id, reason) => http.patch(`/api/bookings/${id}/reject`, { rejectionReason: reason }),
  cancel: (id, reason) => http.patch(`/api/bookings/${id}/cancel`, { cancellationReason: reason })
}
```

### Components (Reusable)
- **BookingForm.jsx** - Form component for creating/editing bookings
- **BookingList.jsx** - Table component for displaying bookings
- **BookingStatusBadge.jsx** - Status display component (colors for different statuses)

---

## 📊 Database Schema

### Bookings Table
```sql
CREATE TABLE bookings (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  resource_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  start_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL,
  status VARCHAR(20) NOT NULL,  -- PENDING, APPROVED, REJECTED, CANCELLED
  notes TEXT,
  rejection_reason TEXT,
  cancellation_reason TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (resource_id) REFERENCES resources(id),
  FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

## ✅ Development Checklist

- [ ] Create BookingController with 7 endpoints
- [ ] Implement BookingService with business logic
- [ ] Create Booking JPA entity
- [ ] Create BookingRepository
- [ ] Create BookingRequest DTO
- [ ] Create BookingResponse DTO
- [ ] Implement validation (date range, no overlaps)
- [ ] Write unit tests for BookingService
- [ ] Create BookingCreatePage.jsx
- [ ] Create BookingDetailPage.jsx
- [ ] Create MyBookingsPage.jsx
- [ ] Create BookingAdminPage.jsx
- [ ] Implement bookingService in api.js
- [ ] Create reusable components
- [ ] Test all endpoints in Postman
- [ ] Test all pages in browser
- [ ] Write BOOKING_SYSTEM_GUIDE.md
- [ ] Update documentation/03_API_REFERENCE.md

---

## 🧪 Testing Scenarios

### Backend Testing (Postman)
1. Register a new user
2. Login to get token
3. Create a booking for a resource
4. Verify status is PENDING
5. Approve the booking (as admin)
6. Verify status changed to APPROVED
7. Create another booking that overlaps - verify error
8. Reject a booking - verify rejection_reason stored
9. Cancel a booking - verify cancellation_reason stored

### Frontend Testing
1. Navigate to "New Booking" page
2. Fill form with resource, dates, notes
3. Submit form
4. Should redirect to booking detail
5. Verify booking shows in "My Bookings"
6. Try to cancel it
7. Verify status updated
8. (Admin) Approve/reject pending bookings

---

## 📚 Integration Points

### Depends On
- User authentication (login, JWT token)
- Resource Management (resource data)
- Database schema

### Used By
- Dashboard (shows recent bookings)
- Notifications (sends booking status updates)
- Analytics (booking statistics)

---

## 🚀 Local Development Setup

```bash
# Backend
cd smart-campus
mvn clean install
mvn spring-boot:run

# Frontend (in another terminal)
cd smart-campus-frontend
npm install
npm run dev

# Test endpoint
curl -H "Authorization: Bearer TOKEN" http://localhost:8081/api/bookings/my
```

---

## 📋 API Request/Response Examples

### CREATE BOOKING
```http
POST /api/bookings
Authorization: Bearer eyJhbGc...

{
  "resourceId": 1,
  "startTime": "2026-04-28T09:00:00",
  "endTime": "2026-04-28T11:00:00",
  "notes": "Need projector for presentation"
}

Response:
{
  "data": {
    "id": 5,
    "resource": { "id": 1, "name": "Study Room 101" },
    "user": { "id": 1, "name": "John" },
    "startTime": "2026-04-28T09:00:00",
    "endTime": "2026-04-28T11:00:00",
    "status": "PENDING",
    "notes": "Need projector for presentation"
  }
}
```

### APPROVE BOOKING
```http
PATCH /api/bookings/5/approve
Authorization: Bearer eyJhbGc...

Response:
{
  "data": {
    "id": 5,
    "status": "APPROVED"
  }
}
```

---

## 💡 Key Considerations

1. **Availability Conflict Prevention**: Don't allow overlapping bookings for same resource
2. **Time Validation**: Start time must be before end time
3. **Status Workflow**: Can only approve PENDING bookings, can only cancel APPROVED ones
4. **User Permissions**: Only admins can approve/reject, users can only manage their own
5. **Notification**: Send notification when booking status changes
6. **Resource Availability**: Check resource availability windows

---

## 📞 Questions, Issues, Communication

- **Questions about API?** Check Member 2 & 3 for their APIs
- **Database schema changes?** Coordinate with team
- **Frontend styling?** Check existing components for consistency
- **Deployment issues?** Check CI/CD workflow in `.github/workflows/`

---

**Created:** April 27, 2026  
**Status:** Ready for Development  
**Questions?** Contact team members or check GitHub discussions
