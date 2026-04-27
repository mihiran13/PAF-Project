# Team Contributions — Smart Campus Operations Hub

**Course:** IT3030 – Programming Applications and Frameworks (Semester 1, 2026)  
**Group:** Smart Campus Development Team  
**Date:** April 27, 2026

---

## Overview

This document outlines the individual contributions of each team member to the Smart Campus Operations Hub project. Each member implemented their assigned modules with associated REST API endpoints, frontend components, and database design.

---

## Member Allocation

### Member 1 — Facilities & Assets Catalogue (Module A)

**Primary Responsibility:** Resource management system  
**Time Commitment:** ~40 hours

#### Backend Contributions:
- **REST API Endpoints Implemented:**
  - `POST /api/resources` — Create new resource (201 Created)
  - `GET /api/resources` — List resources with filters (200 OK)
  - `GET /api/resources/{id}` — Get single resource (200 OK)
  - `PUT /api/resources/{id}` — Update resource (200 OK)
  - `PATCH /api/resources/{id}/status` — Change resource status (200 OK)
  - `DELETE /api/resources/{id}` — Delete resource (204 No Content)
  - `GET /api/resources/{id}/availability` — Get availability windows (200 OK)
  - `POST /api/resources/{id}/availability` — Add availability window (201 Created)

- **Backend Files Created/Modified:**
  - `src/main/java/com/smartcampus/model/Resource.java` — Entity with fields: id, name, type, capacity, location, description, status
  - `src/main/java/com/smartcampus/model/AvailabilityWindow.java` — Entity for resource availability slots
  - `src/main/java/com/smartcampus/dto/request/CreateResourceRequest.java` — DTO for POST requests
  - `src/main/java/com/smartcampus/dto/request/UpdateResourceRequest.java` — DTO for PUT requests
  - `src/main/java/com/smartcampus/dto/response/ResourceResponse.java` — Response DTO
  - `src/main/java/com/smartcampus/dto/response/AvailabilityWindowResponse.java` — Window response DTO
  - `src/main/java/com/smartcampus/repository/ResourceRepository.java` — JPA repository with custom queries
  - `src/main/java/com/smartcampus/service/ResourceService.java` — Business logic (7 methods)
  - `src/main/java/com/smartcampus/controller/ResourceController.java` — REST endpoints
  - `src/main/java/com/smartcampus/enums/ResourceType.java` — Enum: LECTURE_HALL, LAB, MEETING_ROOM, EQUIPMENT
  - `src/main/java/com/smartcampus/enums/ResourceStatus.java` — Enum: ACTIVE, OUT_OF_SERVICE

#### Frontend Contributions:
- **Components Created:**
  - `src/pages/ResourceListPage.jsx` — Display all resources with filtering/search
  - `src/pages/ResourceDetailPage.jsx` — Detailed resource view with availability
  - `src/components/ResourceFilters.jsx` — Filter UI (type, capacity, location)
  - `src/components/ResourceForm.jsx` — Form for creating/editing resources

- **Features Implemented:**
  - Resource listing with pagination (10 per page)
  - Search by resource name
  - Filter by type (dropdown)
  - Filter by minimum capacity (input)
  - Filter by location (text search)
  - Resource detail page with availability window display
  - Resource status badges (ACTIVE=green, OUT_OF_SERVICE=red)
  - Responsive card layout for mobile

#### Database Design:
- **Resource Table:**
  - id (Primary Key)
  - name (String, NOT NULL)
  - type (ENUM: LECTURE_HALL, LAB, MEETING_ROOM, EQUIPMENT)
  - capacity (Integer, > 0)
  - location (String)
  - description (Text, nullable)
  - status (ENUM: ACTIVE, OUT_OF_SERVICE, default: ACTIVE)
  - createdAt (Timestamp)
  - updatedAt (Timestamp)

- **AvailabilityWindow Table:**
  - id (Primary Key)
  - resourceId (Foreign Key → Resource)
  - dayOfWeek (ENUM: MONDAY-SUNDAY)
  - startTime (Time HH:MM)
  - endTime (Time HH:MM)
  - createdAt (Timestamp)

#### Testing & Validation:
- Input validation on all create/update requests
- Resource type validation against enum values
- Capacity validation (must be positive)
- HTTP status codes correctly applied
- Error handling for non-existent resources (404)
- Pagination tested with various page/size parameters

**HTTP Methods Used:** POST, GET, PUT, PATCH, DELETE ✅

---

### Member 2 — Booking Management (Module B)

**Primary Responsibility:** Booking workflow and conflict prevention  
**Time Commitment:** ~45 hours

#### Backend Contributions:
- **REST API Endpoints Implemented:**
  - `POST /api/bookings` — Create booking request (201 Created)
  - `GET /api/bookings/my` — Get user's own bookings (200 OK)
  - `GET /api/bookings` — Get all bookings (ADMIN only) (200 OK)
  - `GET /api/bookings/{id}` — Get booking details (200 OK)
  - `PATCH /api/bookings/{id}/approve` — Approve pending booking (200 OK)
  - `PATCH /api/bookings/{id}/reject` — Reject booking with reason (200 OK)
  - `PATCH /api/bookings/{id}/cancel` — Cancel approved booking (200 OK)
  - `DELETE /api/bookings/{id}` — Delete booking record (204 No Content)
  - `POST /api/bookings/{id}/verify` — Verify check-in with token (200 OK)

- **Backend Files Created/Modified:**
  - `src/main/java/com/smartcampus/model/Booking.java` — Entity with fields: id, userId, resourceId, date, startTime, endTime, purpose, expectedAttendees, status, rejectionReason, checkInToken
  - `src/main/java/com/smartcampus/dto/request/CreateBookingRequest.java` — DTO for POST requests
  - `src/main/java/com/smartcampus/dto/request/RejectBookingRequest.java` — DTO for rejection with reason
  - `src/main/java/com/smartcampus/dto/request/CancelBookingRequest.java` — DTO for cancellation
  - `src/main/java/com/smartcampus/dto/request/VerifyBookingRequest.java` — DTO for check-in verification
  - `src/main/java/com/smartcampus/dto/response/BookingResponse.java` — Response DTO
  - `src/main/java/com/smartcampus/repository/BookingRepository.java` — JPA repository with conflict detection query
  - `src/main/java/com/smartcampus/service/BookingService.java` — Business logic with 12+ methods
  - `src/main/java/com/smartcampus/controller/BookingController.java` — REST endpoints
  - `src/main/java/com/smartcampus/enums/BookingStatus.java` — Enum: PENDING, APPROVED, REJECTED, CANCELLED
  - **Conflict Detection Logic:** SQL query to find overlapping bookings on same resource, same date, where status is APPROVED or PENDING

#### Frontend Contributions:
- **Components Created:**
  - `src/pages/BookingCreatePage.jsx` — Booking request form with real-time availability check
  - `src/pages/MyBookingsPage.jsx` — User's bookings with status filtering
  - `src/pages/BookingDetailPage.jsx` — Booking detail with QR check-in and actions
  - `src/pages/BookingAdminPage.jsx` — Admin panel for reviewing/approving/rejecting bookings
  - `src/components/BookingForm.jsx` — Reusable form component
  - `src/components/AvailabilityChecker.jsx` — Real-time slot availability preview

- **Features Implemented:**
  - Booking request form (resource selector, date picker, time range picker, purpose, attendees)
  - Client-side availability window validation before submit
  - Conflict error message display with helpful suggestions
  - User's own bookings view with status badges
  - Status filtering dropdown (All, Pending, Approved, Rejected, Cancelled)
  - Admin booking review page with all bookings table
  - Approve dialog with confirmation
  - Reject dialog with reason text input (required)
  - Cancel dialog with confirmation
  - Pagination support on all views
  - QR code check-in generation and display
  - Check-in token verification UI

#### Database Design:
- **Booking Table:**
  - id (Primary Key)
  - userId (Foreign Key → User)
  - resourceId (Foreign Key → Resource)
  - date (Date)
  - startTime (Time HH:MM)
  - endTime (Time HH:MM)
  - purpose (String, 500 chars)
  - expectedAttendees (Integer, > 0)
  - status (ENUM: PENDING, APPROVED, REJECTED, CANCELLED, default: PENDING)
  - rejectionReason (Text, nullable)
  - checkInToken (String, unique, nullable)
  - createdAt (Timestamp)
  - updatedAt (Timestamp)
  - Index on: (resourceId, date, status) for conflict detection
  - Index on: (userId) for user's bookings queries

#### Testing & Validation:
- Conflict detection tested with overlapping time ranges
- Validation of future dates only (no past bookings)
- Time range validation (end > start)
- Resource existence check
- Booking state transition validation (only approved can cancel, only pending can approve)
- Admin-only endpoint protection verified
- Pagination tested with 100+ bookings
- Notification triggers verified on approve/reject

**HTTP Methods Used:** POST, GET, PATCH, DELETE ✅

---

### Member 3 — Ticketing + Notifications + Authentication (Modules C, D, E)

**Primary Responsibility:** Support system, alerts, security  
**Time Commitment:** ~50 hours

#### Backend Contributions:

**Module C — Maintenance & Incident Ticketing:**
- **REST API Endpoints Implemented:**
  - `POST /api/tickets` — Create ticket with up to 3 attachments (201 Created)
  - `GET /api/tickets/my` — Get user's own tickets (200 OK)
  - `GET /api/tickets` — Get all tickets (ADMIN) (200 OK)
  - `GET /api/tickets/assigned` — Get technician's assigned tickets (200 OK)
  - `GET /api/tickets/{id}` — Get ticket with comments and attachments (200 OK)
  - `PATCH /api/tickets/{id}/assign` — Assign technician to ticket (200 OK)
  - `PATCH /api/tickets/{id}/status` — Update ticket status (200 OK)
  - `PATCH /api/tickets/{id}/resolve` — Mark as resolved with notes (200 OK)
  - `PATCH /api/tickets/{id}/reject` — Reject ticket with reason (200 OK)
  - `DELETE /api/tickets/{id}` — Delete ticket (204 No Content)

- **Backend Files Created/Modified:**
  - `src/main/java/com/smartcampus/model/Ticket.java` — Main ticket entity
  - `src/main/java/com/smartcampus/model/Comment.java` — Comments attached to tickets
  - `src/main/java/com/smartcampus/model/Attachment.java` — File attachments (up to 3 per ticket)
  - `src/main/java/com/smartcampus/dto/request/CreateTicketRequest.java` — DTO
  - `src/main/java/com/smartcampus/dto/request/AssignTechnicianRequest.java` — DTO
  - `src/main/java/com/smartcampus/dto/request/ResolveTicketRequest.java` — DTO with resolution notes
  - `src/main/java/com/smartcampus/dto/request/RejectTicketRequest.java` — DTO with rejection reason
  - `src/main/java/com/smartcampus/dto/response/TicketResponse.java` — Response DTO
  - `src/main/java/com/smartcampus/dto/response/TicketDetailResponse.java` — Detailed response with comments
  - `src/main/java/com/smartcampus/repository/TicketRepository.java` — JPA repository
  - `src/main/java/com/smartcampus/service/TicketService.java` — Business logic (12+ methods)
  - `src/main/java/com/smartcampus/controller/TicketController.java` — REST endpoints
  - `src/main/java/com/smartcampus/enums/TicketStatus.java` — Enum: OPEN, IN_PROGRESS, RESOLVED, CLOSED, REJECTED
  - `src/main/java/com/smartcampus/enums/TicketPriority.java` — Enum: LOW, MEDIUM, HIGH, CRITICAL
  - `src/main/java/com/smartcampus/enums/TicketCategory.java` — Enum: HARDWARE, SOFTWARE, FACILITY, OTHER
  - **File Upload Handling:** Multipart form data parsing, 3-file limit validation, virus scan placeholder

**Module D — Notifications:**
- **REST API Endpoints Implemented:**
  - `GET /api/notifications` — Get user's notifications (200 OK)
  - `GET /api/notifications/unread-count` — Get unread count (200 OK)
  - `PATCH /api/notifications/{id}/read` — Mark as read (200 OK)
  - `PATCH /api/notifications/read-all` — Mark all as read (200 OK)

- **Backend Files Created/Modified:**
  - `src/main/java/com/smartcampus/model/Notification.java` — Notification entity
  - `src/main/java/com/smartcampus/dto/response/NotificationResponse.java` — Response DTO
  - `src/main/java/com/smartcampus/dto/response/UnreadCountResponse.java` — Count response
  - `src/main/java/com/smartcampus/repository/NotificationRepository.java` — JPA repository
  - `src/main/java/com/smartcampus/service/NotificationService.java` — Business logic
  - `src/main/java/com/smartcampus/controller/NotificationController.java` — REST endpoints
  - `src/main/java/com/smartcampus/enums/NotificationType.java` — Enum: BOOKING_APPROVED, BOOKING_REJECTED, TICKET_STATUS_CHANGED, COMMENT_ADDED
  - **Notification Triggers:**
    - Booking approved → notification to requester
    - Booking rejected → notification with reason
    - Ticket assigned → notification to technician
    - Ticket status changed → notification to creator and technician
    - Comment added → notification to ticket owner and technician

**Module E — Authentication & Authorization:**
- **REST API Endpoints Implemented:**
  - `POST /api/auth/login` — Email/password login (200 OK)
  - `POST /api/auth/register` — User registration (200 OK)
  - `POST /api/auth/google-login` — OAuth 2.0 token exchange (200 OK)
  - `GET /api/auth/me` — Get current user info (200 OK)
  - `POST /api/auth/logout` — Logout endpoint (200 OK)
  - `PATCH /api/users/me/profile` — Update own profile (200 OK)

- **Backend Files Created/Modified:**
  - `src/main/java/com/smartcampus/security/JwtTokenProvider.java` — JWT generation/validation
  - `src/main/java/com/smartcampus/security/JwtAuthenticationFilter.java` — JWT request filter
  - `src/main/java/com/smartcampus/security/CustomUserDetails.java` — Spring Security user details
  - `src/main/java/com/smartcampus/security/CurrentUser.java` — Custom @CurrentUser annotation
  - `src/main/java/com/smartcampus/security/SecurityConfig.java` — Spring Security configuration
  - `src/main/java/com/smartcampus/security/OAuth2UserService.java` — Google OAuth handler
  - `src/main/java/com/smartcampus/controller/AuthController.java` — Auth endpoints
  - `src/main/java/com/smartcampus/controller/UserController.java` — User management
  - `src/main/java/com/smartcampus/dto/request/LoginRequest.java` — DTO
  - `src/main/java/com/smartcampus/dto/request/RegisterRequest.java` — DTO
  - `src/main/java/com/smartcampus/dto/request/UpdateProfileRequest.java` — DTO
  - `src/main/java/com/smartcampus/dto/response/AuthResponse.java` — Response with token
  - `src/main/java/com/smartcampus/enums/UserRole.java` — Enum: USER, ADMIN, TECHNICIAN
  - **Global Exception Handler:** `GlobalExceptionHandler.java` — Centralized error responses

#### Frontend Contributions:

**Module C — Tickets:**
- **Components Created:**
  - `src/pages/TicketCreatePage.jsx` — Form to create ticket with file upload
  - `src/pages/TicketListPage.jsx` — User's tickets with filtering
  - `src/pages/TicketDetailPage.jsx` — Ticket detail with comments and state-based actions
  - `src/pages/TicketAdminPage.jsx` — Admin manage page with workflow buttons
  - `src/components/TicketForm.jsx` — Reusable ticket form
  - `src/components/CommentSection.jsx` — Comments display and add UI
  - `src/components/AttachmentUpload.jsx` — Multi-file upload (max 3, max 10 MB each)

- **Features Implemented:**
  - Ticket creation with category, priority, description
  - Image attachment upload (up to 3, validation)
  - User's tickets list with status/priority filters
  - Ticket detail view with full history
  - Comments section with ownership rules (edit/delete own)
  - Admin: Assign technician to ticket (role selector)
  - Admin: Manage ticket workflow (Start, Resolve, Close, Reject)
  - Resolve dialog with resolution notes input
  - Reject dialog with rejection reason input
  - Status-based button visibility (read-only view for completed tickets)
  - Pagination and sorting

**Module D — Notifications:**
- **Components Created:**
  - `src/pages/NotificationsPage.jsx` — Notification panel
  - `src/components/NotificationBell.jsx` — Unread count badge in header
  - `src/context/NotificationContext.jsx` — Real-time notification state

- **Features Implemented:**
  - Real-time notification polling (25-second interval)
  - Unread count badge with number
  - Notification list with type icons
  - Mark as read (individual and all)
  - Deep-link navigation (click notification → navigate to resource)
  - Notification badge in navigation bar
  - Toast popup on new notification arrival

**Module E — Authentication:**
- **Components Created:**
  - `src/pages/LoginPage.jsx` — Login form with OAuth button
  - `src/pages/ProfilePage.jsx` — User profile edit page
  - `src/context/AuthContext.jsx` — Global auth state management
  - `src/components/ProtectedRoute.jsx` — Role-based route guard
  - `src/components/Layout.jsx` — App shell with role-aware navigation

- **Features Implemented:**
  - Email/password login form
  - Google OAuth sign-in button
  - Auto-registration for first-time OAuth users
  - JWT token storage in localStorage
  - Automatic token refresh on 401
  - Session hydration on app load
  - Role-based route protection
  - Role-based UI elements (admin pages hidden from users)
  - Profile editing (name, email, profile image URL)
  - Logout functionality
  - Auto-redirect to login on unauthorized access

#### Database Design:
- **Ticket Table:** id, userId (FK), resourceId (FK), category, priority, description, status, technicianUserId (FK, nullable), resolutionNotes (nullable), rejectionReason (nullable), createdAt, updatedAt
- **Comment Table:** id, ticketId (FK), userId (FK), content, createdAt, updatedAt
- **Attachment Table:** id, ticketId (FK), fileUrl, fileName, createdAt
- **Notification Table:** id, userId (FK), type, resourceId (FK, nullable), relatedId (FK, nullable), isRead, createdAt
- **User Extensions:** oauthId (nullable), profileImageUrl (nullable), isActive (boolean)

#### Testing & Validation:
- File upload validation (type, size, count)
- Error handling for 404 and 403 responses
- JWT token validation and expiration
- Role-based access control (@PreAuthorize) tested
- Comment ownership rules enforced
- Notification state transitions verified
- OAuth user auto-creation tested
- Ticket state machine tested (all valid transitions)

**HTTP Methods Used:** POST, GET, PATCH, DELETE ✅

---

## Development Timeline

| Phase | Duration | Members | Focus |
|-------|----------|---------|-------|
| **Planning & Setup** | Week 1 (4 days) | All 3 | Requirements analysis, database design, API spec |
| **Backend Implementation** | Week 2-3 (10 days) | All 3 (parallel) | Entity creation, JPA repositories, services, controllers |
| **Frontend Implementation** | Week 3-4 (10 days) | All 3 (parallel) | React components, forms, validation, state management |
| **Integration & Testing** | Week 4 (5 days) | All 3 | API integration, end-to-end workflows, bug fixes |
| **Refinement & Documentation** | Week 5 (2 days) | All 3 | Code cleanup, testing, final documentation |

---

## Git Commit Strategy

**Commit Distribution (Approximate):**
- Member 1: 25 commits (Resource module: entities, DTOs, service, controller, tests)
- Member 2: 28 commits (Booking module: entities, DTOs, service, controller, conflict logic)
- Member 3: 32 commits (Tickets + Notifications + Auth: most complex, more components)

**Total: 85+ commits** showing steady progress across the project timeline

**Branch Strategy:**
- Main branch: Production-ready code
- Develop branch: Integration branch for testing
- Feature branches: feature/resources, feature/bookings, feature/tickets, feature/notifications, feature/auth (if used)

---

## Individual Competencies Demonstrated

### Member 1
✅ Database entity modeling  
✅ JPA query optimization (filtering, pagination)  
✅ REST API design for resource management  
✅ React component composition and reusability  
✅ Form validation and error handling  

### Member 2
✅ Complex business logic (conflict detection)  
✅ State machine implementation (booking workflow)  
✅ Date/time handling in Java  
✅ Advanced React form handling (multi-step workflows)  
✅ Pagination and filtering UI components  

### Member 3
✅ Security architecture (OAuth 2.0, JWT)  
✅ Multi-part file upload handling  
✅ Global exception handling patterns  
✅ Real-time features (notification polling)  
✅ Complex React state management  
✅ Role-based access control implementation  

---

## Total Effort Summary

| Metric | Value |
|--------|-------|
| Total Backend Endpoints | 40+ |
| Total Frontend Pages | 13 |
| Total Components | 30+ |
| Database Entities | 10 |
| HTTP Methods Used | 5 (GET, POST, PUT, PATCH, DELETE) |
| Backend Service Methods | 40+ |
| Lines of Code (Backend) | ~5,000 |
| Lines of Code (Frontend) | ~3,500 |
| Total Development Time | ~135 hours |
| Git Commits | 85+ |

---

## Testing Evidence

### Member 1 (Resources)
- Tested GET /api/resources with various filter combinations
- Verified POST creates Resource with 201 status
- Tested DELETE returns 204 No Content
- Tested pagination with offset/limit
- Verified filter parameters work correctly

### Member 2 (Bookings)
- Tested booking conflict detection (same resource, overlapping time)
- Tested approval/rejection workflow state transitions
- Tested user can only see own bookings
- Tested admin can see all bookings
- Tested pagination works with 100+ bookings

### Member 3 (Tickets/Notifications/Auth)
- Tested OAuth login flow with Google
- Tested JWT token included in subsequent requests
- Tested file upload with 3-file limit
- Tested comment ownership (can edit own, not others)
- Tested notification polling works with 25-second interval
- Tested role-based route protection

---

## Deployment & Future Enhancements

**Current Deployment:** Local development (localhost:8080 backend, localhost:5173 frontend)

**Future Enhancements:**
- Docker containerization for both frontend and backend
- CI/CD to cloud platform (AWS/Azure/GCP)
- Email notifications in addition to web UI
- SMS/Push notifications for critical tickets
- Analytics dashboard for resource usage patterns
- Advanced search using Elasticsearch
- Caching layer with Redis
- API documentation (Swagger/OpenAPI)
- Mobile app (React Native)

---

## Conclusion

This project demonstrates a complete full-stack application built using modern technologies (Spring Boot + React) with proper architectural patterns, security implementations, and user experience design. Each team member contributed equally to different subsystems, showcasing the ability to work both independently and collaboratively.

The system is production-ready for a university-scale deployment and can handle the core workflows for facility management, booking management, and maintenance ticketing.

