# Smart Campus PAF - System Division by Member

## Overview
The Smart Campus system is divided into 3 main functional modules, each assigned to a team member. This document outlines the responsibilities, code components, and deliverables for each member.

---

## 📌 MEMBER 1: BOOKING SYSTEM
**Responsibility:** Create, manage, and track resource bookings

### Backend Components

#### Controllers
- **BookingController.java** (`smart-campus/src/main/java/com/smartcampus/controller/`)
  - `POST /api/bookings` - Create new booking
  - `GET /api/bookings` - List all bookings (admin)
  - `GET /api/bookings/my` - Get user's bookings
  - `GET /api/bookings/{id}` - Get booking details
  - `PATCH /api/bookings/{id}/approve` - Approve booking
  - `PATCH /api/bookings/{id}/reject` - Reject booking
  - `PATCH /api/bookings/{id}/cancel` - Cancel booking

#### Services
- **BookingService.java** (`smart-campus/src/main/java/com/smartcampus/service/`)
  - Business logic for booking creation, approval, rejection, cancellation
  - Availability validation
  - Booking conflict checking

#### Models/Entities
- **Booking.java** (`smart-campus/src/main/java/com/smartcampus/model/`)
  - Fields: id, resource, user, startTime, endTime, status, notes, etc.
  - Relationships: ManyToOne with Resource and User

#### Repositories
- **BookingRepository.java** (`smart-campus/src/main/java/com/smartcampus/repository/`)
  - Database queries for bookings

#### DTOs
- **BookingRequest.java** - Input validation for booking creation
- **BookingResponse.java** - API response formatting

### Frontend Components

#### Pages
- **BookingCreatePage.jsx** (`smart-campus-frontend/src/pages/`)
  - Form to create new booking
  - Resource selection, date/time picker
  - Submit booking

- **BookingDetailPage.jsx**
  - View booking details
  - Show booking status
  - Cancel booking button

- **MyBookingsPage.jsx**
  - List user's bookings
  - Filter by status (pending, approved, rejected, cancelled)
  - Quick actions (cancel, view details)

- **BookingAdminPage.jsx**
  - Admin panel for all bookings
  - Approve/reject pending bookings
  - View booking analytics

#### Services
- **bookingService** in `api.js` (`smart-campus-frontend/src/services/`)
  ```javascript
  - create(payload)
  - my(params)
  - all(params)
  - get(id)
  - approve(id)
  - reject(id, reason)
  - cancel(id, reason)
  ```

#### Components
- **BookingForm.jsx** - Reusable booking creation form
- **BookingList.jsx** - Reusable booking list display
- **BookingStatusBadge.jsx** - Status indicator component

### Database Tables
- **bookings** table with columns:
  - id, resource_id, user_id, start_time, end_time, status, notes, rejection_reason, cancellation_reason, created_at, updated_at

### API Endpoints (6 total)
```
POST   /api/bookings              - Create booking
GET    /api/bookings/my           - My bookings
GET    /api/bookings              - All bookings (admin)
GET    /api/bookings/{id}         - Booking details
PATCH  /api/bookings/{id}/approve - Approve
PATCH  /api/bookings/{id}/reject  - Reject
PATCH  /api/bookings/{id}/cancel  - Cancel
```

### Documentation
- **BOOKING_SYSTEM_GUIDE.md** (to be created)
- API reference section in `documentation/03_API_REFERENCE.md`

---

## 📌 MEMBER 2: RESOURCE MANAGEMENT
**Responsibility:** Create, maintain, and manage facility resources

### Backend Components

#### Controllers
- **ResourceController.java** (`smart-campus/src/main/java/com/smartcampus/controller/`)
  - `POST /api/resources` - Create resource
  - `GET /api/resources` - List resources with filtering
  - `GET /api/resources/{id}` - Get resource details
  - `PUT /api/resources/{id}` - Update resource
  - `PATCH /api/resources/{id}/status` - Update status
  - `DELETE /api/resources/{id}` - Delete resource
  - `POST /api/resources/{id}/availability` - Add availability window

#### Services
- **ResourceService.java** (`smart-campus/src/main/java/com/smartcampus/service/`)
  - Resource CRUD operations
  - Availability window management
  - Resource type and status management
  - Capacity validation

#### Models/Entities
- **Resource.java** (`smart-campus/src/main/java/com/smartcampus/model/`)
  - Fields: id, name, type, capacity, location, description, status, etc.
  - Relationships: OneToMany with Bookings and AvailabilityWindows

- **ResourceAvailabilityWindow.java**
  - Fields: id, resource, dayOfWeek, startTime, endTime
  - Represents recurring availability slots

#### Repositories
- **ResourceRepository.java** (`smart-campus/src/main/java/com/smartcampus/repository/`)
- **AvailabilityWindowRepository.java**

#### DTOs
- **ResourceRequest.java** - Input validation for resource creation/update
- **ResourceResponse.java** - API response formatting
- **AvailabilityWindowRequest.java**
- **AvailabilityWindowResponse.java**

#### Enums
- **ResourceType.java** (10 types: STUDY_ROOM, AUDITORIUM, CAMERA, LECTURE_HALL, LIBRARY, PROJECTOR, LAB, OTHER_EQUIPMENT, SPORTS_FACILITY, MEETING_ROOM)
- **ResourceStatus.java** (ACTIVE, OUT_OF_SERVICE)

### Frontend Components

#### Pages
- **ResourceListPage.jsx** (`smart-campus-frontend/src/pages/`)
  - Browse all resources
  - Search and filter (type, capacity, location)
  - Pagination
  - Link to booking

- **ResourceDetailPage.jsx**
  - Detailed resource information
  - Availability schedule display
  - Booking button
  - Photos/images gallery

- **ResourceAdminPage.jsx**
  - Admin resource management
  - Create new resource form
  - Edit resource details
  - Update availability windows
  - Delete resources

#### Services
- **resourceService** in `api.js`
  ```javascript
  - list(params)
  - get(id)
  - create(payload)
  - update(id, payload)
  - updateStatus(id, status)
  - delete(id)
  - addWindow(id, payload)
  ```

#### Components
- **ResourceCard.jsx** - Display resource summary
- **ResourceForm.jsx** - Resource CRUD form
- **ResourceFilters.jsx** - Filter component
- **AvailabilitySchedule.jsx** - Show availability windows
- **CapacityIndicator.jsx** - Visual capacity display

### Database Tables
- **resources** table:
  - id, name, type, capacity, location, description, status, created_at, updated_at

- **availability_windows** table:
  - id, resource_id, day_of_week, start_time, end_time

### API Endpoints (7 total)
```
POST   /api/resources                    - Create
GET    /api/resources                    - List (with filters: type, capacity, location)
GET    /api/resources/{id}               - Details
PUT    /api/resources/{id}               - Update
PATCH  /api/resources/{id}/status        - Update status
DELETE /api/resources/{id}               - Delete
POST   /api/resources/{id}/availability  - Add availability window
```

### Documentation
- **RESOURCE_MANAGEMENT_GUIDE.md** (to be created)
- Constants file: `src/utils/constants.js` (RESOURCE_TYPES, RESOURCE_STATUSES)
- API reference section in `documentation/03_API_REFERENCE.md`

---

## 📌 MEMBER 3: SUPPORT TICKET SYSTEM
**Responsibility:** Handle support requests, issues, and comments

### Backend Components

#### Controllers
- **TicketController.java** (`smart-campus/src/main/java/com/smartcampus/controller/`)
  - `POST /api/tickets` - Create support ticket
  - `GET /api/tickets` - List tickets (admin/technician)
  - `GET /api/tickets/my` - Get user's tickets
  - `GET /api/tickets/{id}` - Get ticket details
  - `PUT /api/tickets/{id}` - Update ticket
  - `PATCH /api/tickets/{id}/status` - Update status
  - `POST /api/tickets/{id}/comments` - Add comment
  - `GET /api/tickets/{id}/comments` - Get comments

#### Services
- **TicketService.java** (`smart-campus/src/main/java/com/smartcampus/service/`)
  - Ticket CRUD operations
  - Comment management
  - Status workflow (OPEN → IN_PROGRESS → RESOLVED/CLOSED)
  - Priority escalation

- **NotificationService.java** (`smart-campus/src/main/java/com/smartcampus/service/`)
  - Send notifications when:
    - Ticket created
    - Status changed
    - Comment added
    - Ticket resolved

#### Models/Entities
- **Ticket.java** (`smart-campus/src/main/java/com/smartcampus/model/`)
  - Fields: id, title, description, priority, status, assignedTo, resource, createdBy, etc.
  - Relationships: OneToMany with Comments, ManyToOne with Resource and User

- **Comment.java**
  - Fields: id, content, author, ticket, createdAt
  - Support for ticket discussions

- **Notification.java**
  - Fields: id, type, message, recipient, status, createdAt
  - System notifications about tickets and bookings

#### Repositories
- **TicketRepository.java** (`smart-campus/src/main/java/com/smartcampus/repository/`)
- **CommentRepository.java**
- **NotificationRepository.java**

#### DTOs
- **TicketRequest.java** - Input validation
- **TicketResponse.java** - API response
- **CommentRequest.java**
- **CommentResponse.java**
- **NotificationResponse.java**

#### Enums
- **TicketStatus.java** (OPEN, IN_PROGRESS, RESOLVED, CLOSED, REJECTED)
- **TicketPriority.java** (LOW, MEDIUM, HIGH, CRITICAL)

### Frontend Components

#### Pages
- **TicketCreatePage.jsx** (`smart-campus-frontend/src/pages/`)
  - Form to report new issue
  - Resource selection
  - Priority selection
  - Description field with formatting

- **TicketDetailPage.jsx**
  - View full ticket details
  - Comments thread with reply
  - Status update (if authorized)
  - Resource link
  - Priority indicator

- **TicketListPage.jsx**
  - User's support tickets
  - Filter by status/priority
  - Search by title
  - Quick status view

- **TicketAdminPage.jsx**
  - Admin/Technician panel
  - Assign tickets to technicians
  - Update ticket status
  - View all system tickets
  - Analytics dashboard

- **NotificationsPage.jsx**
  - System notifications feed
  - Ticket updates
  - Booking updates
  - Mark as read/unread

#### Services
- **ticketService** in `api.js`
  ```javascript
  - create(payload)
  - my(params)
  - all(params)
  - get(id)
  - update(id, payload)
  - updateStatus(id, status)
  - addComment(id, comment)
  - getComments(id)
  ```

- **notificationService** in `api.js`
  ```javascript
  - list(params)
  - getUnreadCount()
  - markAsRead(id)
  - markAllAsRead()
  ```

#### Components
- **TicketForm.jsx** - Ticket creation/edit form
- **TicketList.jsx** - Reusable ticket listing
- **CommentThread.jsx** - Comments display and reply
- **PriorityBadge.jsx** - Priority visual indicator
- **StatusBadge.jsx** - Status indicator
- **NotificationFeed.jsx** - Notifications list
- **NotificationItem.jsx** - Individual notification

### Database Tables
- **tickets** table:
  - id, title, description, priority, status, assigned_to, resource_id, created_by, created_at, updated_at

- **comments** table:
  - id, ticket_id, author_id, content, created_at, updated_at

- **notifications** table:
  - id, type, message, recipient_id, is_read, resource_type, resource_id, created_at

### API Endpoints (8 total)
```
POST   /api/tickets                  - Create ticket
GET    /api/tickets/my               - My tickets
GET    /api/tickets                  - All tickets (admin)
GET    /api/tickets/{id}             - Ticket details
PUT    /api/tickets/{id}             - Update ticket
PATCH  /api/tickets/{id}/status      - Update status
POST   /api/tickets/{id}/comments    - Add comment
GET    /api/tickets/{id}/comments    - Get comments
GET    /api/notifications            - Get notifications
GET    /api/notifications/unread     - Count unread
```

### Documentation
- **SUPPORT_TICKET_GUIDE.md** (to be created)
- API reference section in `documentation/03_API_REFERENCE.md`

---

## 🔄 SHARED / COMMON COMPONENTS

### Authentication & Authorization
- **AuthController.java** / **AuthService.java** - Shared by all
- **SecurityConfig.java** - Shared security configuration
- **JwtAuthenticationFilter.java** - Shared JWT token handling
- **LoginPage.jsx** / **AuthContext.jsx** - Shared auth frontend

- **Members:** All 3 members should understand the auth flow

### User Management
- **UserController.java** / **UserService.java** - Shared admin functions
- **UserAdminPage.jsx** - User management
- **ProfilePage.jsx** - User profile editing

- **Assigned to:** Coordinate with all members

### Dashboard & Analytics
- **DashboardPage.jsx** - Overview dashboard
- **AnalyticsDashboardPage.jsx** - Analytics with Recharts charts
- **Displays metrics for:** Bookings, Resources, Tickets

- **Assigned to:** All members should contribute metrics

### Documentation & CI/CD
- **documentation/** folder - Shared documentation
- **.github/workflows/** - Shared CI/CD pipelines
- **README.md** files

- **Assigned to:** Coordinate documentation updates

---

## 📋 Development Workflow

### For Each Member:

1. **Create Feature Branch**
   ```bash
   git checkout -b member{1-3}-feature/{feature-name}
   ```

2. **Backend Development**
   - Create/modify controller, service, entity, repository
   - Write unit tests
   - Add error handling

3. **Frontend Development**
   - Create pages/components
   - Integrate with API services
   - Add form validation

4. **Documentation**
   - Update relevant guide (BOOKING_SYSTEM_GUIDE.md, etc.)
   - Document API endpoints
   - Add code comments

5. **Testing**
   - Test API endpoints in Postman
   - Test frontend pages in browser
   - Verify error handling

6. **Pull Request**
   - Submit PR to main branch
   - Request code review from other members
   - After approval, merge

---

## 📊 File Structure Summary

### Backend Files by Member

**Member 1 (Booking System):**
```
src/main/java/com/smartcampus/
  ├── controller/BookingController.java
  ├── service/BookingService.java
  ├── model/Booking.java
  ├── repository/BookingRepository.java
  ├── dto/request/BookingRequest.java
  └── dto/response/BookingResponse.java
```

**Member 2 (Resource Management):**
```
src/main/java/com/smartcampus/
  ├── controller/ResourceController.java
  ├── service/ResourceService.java
  ├── model/Resource.java
  ├── model/ResourceAvailabilityWindow.java
  ├── repository/ResourceRepository.java
  ├── repository/AvailabilityWindowRepository.java
  ├── enums/ResourceType.java
  ├── enums/ResourceStatus.java
  ├── dto/request/ResourceRequest.java
  └── dto/response/ResourceResponse.java
```

**Member 3 (Support Ticket System):**
```
src/main/java/com/smartcampus/
  ├── controller/TicketController.java
  ├── service/TicketService.java
  ├── service/NotificationService.java
  ├── model/Ticket.java
  ├── model/Comment.java
  ├── model/Notification.java
  ├── repository/TicketRepository.java
  ├── repository/CommentRepository.java
  ├── repository/NotificationRepository.java
  ├── enums/TicketStatus.java
  ├── enums/TicketPriority.java
  ├── dto/request/TicketRequest.java
  └── dto/response/TicketResponse.java
```

### Frontend Files by Member

**Member 1 (Booking System):**
```
src/pages/
  ├── BookingCreatePage.jsx
  ├── BookingDetailPage.jsx
  ├── MyBookingsPage.jsx
  └── BookingAdminPage.jsx

src/components/
  ├── BookingForm.jsx
  ├── BookingList.jsx
  └── BookingStatusBadge.jsx
```

**Member 2 (Resource Management):**
```
src/pages/
  ├── ResourceListPage.jsx
  ├── ResourceDetailPage.jsx
  └── ResourceAdminPage.jsx

src/components/
  ├── ResourceCard.jsx
  ├── ResourceForm.jsx
  ├── ResourceFilters.jsx
  ├── AvailabilitySchedule.jsx
  └── CapacityIndicator.jsx
```

**Member 3 (Support Ticket System):**
```
src/pages/
  ├── TicketCreatePage.jsx
  ├── TicketDetailPage.jsx
  ├── TicketListPage.jsx
  ├── TicketAdminPage.jsx
  └── NotificationsPage.jsx

src/components/
  ├── TicketForm.jsx
  ├── TicketList.jsx
  ├── CommentThread.jsx
  ├── PriorityBadge.jsx
  ├── StatusBadge.jsx
  ├── NotificationFeed.jsx
  └── NotificationItem.jsx
```

---

## 🎯 Success Criteria for Each Member

### Member 1: Booking System
- ✅ All 7 API endpoints working
- ✅ 4 frontend pages fully functional
- ✅ Booking validation (no overlaps)
- ✅ Status workflow working correctly
- ✅ Tests passing
- ✅ Documentation complete

### Member 2: Resource Management
- ✅ All 7 API endpoints working
- ✅ 3 frontend pages fully functional
- ✅ Availability window management
- ✅ Resource filtering and search
- ✅ Tests passing
- ✅ Documentation complete

### Member 3: Support Ticket System
- ✅ All 8 API endpoints working
- ✅ 5 frontend pages fully functional
- ✅ Comments and notifications working
- ✅ Ticket status workflow
- ✅ Tests passing
- ✅ Documentation complete

---

## 📝 Total Deliverables

**Backend:** 21 Java files (controllers, services, models, DTOs, enums, repositories)
**Frontend:** 20+ JSX components and pages
**Documentation:** 3 member-specific guides + existing 6 files
**API Endpoints:** 22 total (7 + 7 + 8)
**Database Tables:** 7 tables (+ shared: users, auth)
**Tests:** Unit tests for each module

---

## 🔗 Communication Points

- **Weekly Sync:** Review progress on each module
- **API Contracts:** Ensure API response formats are consistent
- **Database Schema:** Coordinate on entity relationships
- **UI/UX Consistency:** Maintain consistent styling across modules
- **Bug Fixes:** Coordinate when fixes affect shared components
- **Deployment:** Coordinate final testing before production

---

**Last Updated:** April 27, 2026
**Version:** 1.0
**Status:** Ready for Development
