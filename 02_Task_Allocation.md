# Smart Campus Operations Hub — 3-Member Task Allocation

> **Course:** IT3030 – Programming Applications and Frameworks (Semester 1, 2026)  
> **Group Size:** 3 Members  
> **Minimum per member:** 4 REST API endpoints using different HTTP methods (GET, POST, PUT/PATCH, DELETE)  
> **Individual Assessment:** Each member is assessed individually during viva

---

## Team Overview

| Member   | Primary Modules                                              | Secondary Responsibilities                      |
| -------- | ------------------------------------------------------------ | ----------------------------------------------- |
| Member 1 | Module A — Facilities & Assets Catalogue                     | Resource administration, availability management |
| Member 2 | Module B — Booking Management                                | Conflict detection, admin booking review         |
| Member 3 | Module C — Ticketing + Module D — Notifications + Module E — Auth/Security | Comments, attachments, OAuth, RBAC              |

---

## Member 1 — Facilities & Assets Catalogue + Resource Administration

### Primary Ownership
- **Module A:** Facilities & Assets Catalogue (full ownership)

### Backend Responsibilities
- Resource entity, repository, service, controller (full layered architecture)
- Resource CRUD operations (create, read, update, delete)
- Resource search and filtering (by type, capacity, location)
- Resource status management (ACTIVE / OUT_OF_SERVICE)
- Resource availability window handling (defining when a resource is bookable)
- Input validation for all resource fields (type, capacity > 0, location required, etc.)
- Error handling for resource operations (not found, duplicate, invalid data)
- Pagination support for resource listing

### Backend Endpoints (minimum 4, different HTTP methods)

| #  | Method | Endpoint Path                             | Purpose                                      |
| -- | ------ | ----------------------------------------- | -------------------------------------------- |
| 1  | POST   | `/api/resources`                          | Create a new bookable resource               |
| 2  | GET    | `/api/resources`                          | List all resources with search/filter params |
| 3  | GET    | `/api/resources/{id}`                     | Get a single resource by ID                  |
| 4  | PUT    | `/api/resources/{id}`                     | Update resource details                      |
| 5  | PATCH  | `/api/resources/{id}/status`              | Change resource status (ACTIVE ↔ OUT_OF_SERVICE) |
| 6  | DELETE | `/api/resources/{id}`                     | Delete/deactivate a resource                 |
| 7  | GET    | `/api/resources/{id}/availability`        | Get availability windows for a resource      |
| 8  | PUT    | `/api/resources/{id}/availability`        | Update availability windows for a resource   |

**HTTP Method Coverage:** ✅ GET, POST, PUT, PATCH, DELETE — all different methods used.

### Frontend Responsibilities
- Resource catalogue list page (card/table view with all resources)
- Resource search and filter UI (dropdowns and inputs for type, capacity, location)
- Resource detail view page (view full metadata for a single resource)
- Admin resource management page (create, edit, delete resources)
- Resource creation/edit form with client-side validation
- Resource status badges (green for ACTIVE, red for OUT_OF_SERVICE)
- Resource availability calendar/window display
- Responsive layout for resource pages

### Database Responsibilities
- `Resource` entity/table design with fields:
  - `id`, `name`, `type` (ENUM: LECTURE_HALL, LAB, MEETING_ROOM, EQUIPMENT), `capacity`, `location`, `description`, `status` (ENUM: ACTIVE, OUT_OF_SERVICE), `created_at`, `updated_at`
- `AvailabilityWindow` entity/table (or embedded):
  - `id`, `resource_id` (FK), `day_of_week`, `start_time`, `end_time`

### Testing Responsibilities
- Resource CRUD endpoint tests (Postman collection or JUnit)
- Resource validation tests (invalid/missing fields)
- Resource search/filter tests (by type, capacity range, location)
- Resource status transition tests
- Resource not found error handling tests

### Viva Preparation Topics
- Resource entity design decisions and field choices
- Search and filter implementation approach (query params, specifications/criteria)
- Status management logic and business rules
- Validation rules for resource creation and updates
- How resource availability windows are modeled and used
- How resources relate to bookings (Member 2) and tickets (Member 3)
- Frontend component structure and state management for resources
- RESTful API design decisions for resource endpoints

---

## Member 2 — Booking Management

### Primary Ownership
- **Module B:** Booking Management (full ownership)

### Backend Responsibilities
- Booking entity, repository, service, controller (full layered architecture)
- Booking request creation (with resource reference, date, time range, purpose, attendees)
- Booking workflow state management (PENDING → APPROVED / REJECTED; APPROVED → CANCELLED)
- Scheduling conflict detection and prevention (overlapping time ranges for the same resource)
- Admin booking review — approve with no conflicts, reject with reason
- User own bookings retrieval (filtered by current authenticated user)
- Admin all bookings retrieval with filters (by status, resource, date range, user)
- Booking cancellation logic (only approved bookings can be cancelled)
- Input validation for booking requests (date in future, valid time range, resource exists, etc.)
- Error handling for booking operations (resource not found, conflict detected, invalid state transition)
- Integration with Member 1's Resource module (validate resource exists and is ACTIVE)
- Integration with Member 3's Notification module (trigger notifications on approval/rejection)

### Backend Endpoints (minimum 4, different HTTP methods)

| #  | Method | Endpoint Path                              | Purpose                                           |
| -- | ------ | ------------------------------------------ | ------------------------------------------------- |
| 1  | POST   | `/api/bookings`                            | Create a new booking request (status = PENDING)   |
| 2  | GET    | `/api/bookings/my`                         | Get current user's own bookings                   |
| 3  | GET    | `/api/bookings`                            | Admin: get all bookings with filter query params   |
| 4  | GET    | `/api/bookings/{id}`                       | Get a single booking by ID                        |
| 5  | PATCH  | `/api/bookings/{id}/approve`               | Admin: approve a pending booking                  |
| 6  | PATCH  | `/api/bookings/{id}/reject`                | Admin: reject a pending booking (with reason)     |
| 7  | PATCH  | `/api/bookings/{id}/cancel`                | Cancel an approved booking                        |
| 8  | DELETE | `/api/bookings/{id}`                       | Delete a booking record (admin only)              |

**HTTP Method Coverage:** ✅ GET, POST, PATCH, DELETE — all different methods used.

### Frontend Responsibilities
- Booking request form (resource selection dropdown, date picker, time range inputs, purpose text, attendees count)
- Resource availability viewer (show available slots before booking)
- "My Bookings" page (user's own bookings with status badges)
- Admin booking review page (all bookings with filter controls: status, resource, date)
- Approve dialog (confirmation)
- Reject dialog (with reason text input, required)
- Booking status badges (PENDING = yellow, APPROVED = green, REJECTED = red, CANCELLED = grey)
- Booking conflict error display (show clear error when time overlaps)
- Booking cancellation confirmation modal
- Booking detail view page

### Database Responsibilities
- `Booking` entity/table design with fields:
  - `id`, `user_id` (FK), `resource_id` (FK), `date`, `start_time`, `end_time`, `purpose`, `expected_attendees`, `status` (ENUM: PENDING, APPROVED, REJECTED, CANCELLED), `rejection_reason`, `created_at`, `updated_at`
- Conflict checking query design (find overlapping bookings for the same resource on the same date where status is APPROVED or PENDING)

### Testing Responsibilities
- Booking creation tests (valid and invalid inputs)
- Booking conflict detection tests (overlapping time ranges)
- Booking workflow transition tests (valid and invalid state changes)
- Booking validation tests (past date, invalid time range, nonexistent resource)
- Admin approval/rejection tests (with and without reason)
- Booking cancellation tests (only approved bookings)
- User bookings retrieval tests (only own bookings)
- Admin bookings retrieval tests (all bookings with filters)

### Viva Preparation Topics
- Booking entity design and field choices
- How time overlap / conflict prevention works (query logic, edge cases)
- Workflow state transition rules and enforcement
- Who can approve, reject, cancel, and why (role-based rules)
- How booking filters work (query params → database query)
- How booking relates to resource availability (Member 1's module)
- How booking approval/rejection triggers notifications (Member 3's module)
- Frontend booking request flow explanation
- How the conflict error is communicated to the user

---

## Member 3 — Ticketing + Comments + Notifications + Authentication/Security

### Primary Ownership
- **Module C:** Maintenance & Incident Ticketing (full ownership)
- **Module D:** Notifications (full ownership)
- **Module E:** Authentication & Authorization (full ownership)
- **Comment system** within ticketing (full ownership)

### Why Member 3 Has More Modules

Member 3 owns four modules, but this allocation is **balanced** for the following reasons:

1. **Authentication & Authorization** is a cross-cutting concern used by all modules. It requires setup and configuration work (OAuth 2.0, security filters, role management) but does not involve as many CRUD endpoints as a domain module.
2. **Notifications** are event-driven and relatively lightweight — mainly triggered by actions in other modules (booking approval, ticket status changes, comments). The logic is straightforward: listen for events → create notification → serve to user.
3. **Comments** are a sub-feature of ticketing that naturally fits with Module C.
4. **Members 1 and 2** each own a single deep module with significant complexity:
   - Member 1: search/filter logic, availability windows, resource administration
   - Member 2: conflict detection algorithm, workflow state machine, admin review logic

This allocation ensures every member has substantial, individually assessable work while avoiding workload imbalance.

### Backend Responsibilities

#### Authentication & Authorization
- OAuth 2.0 integration with Google sign-in (Spring Security OAuth2 Client)
- User entity and role management (USER, ADMIN, optionally TECHNICIAN)
- Spring Security configuration (SecurityFilterChain, OAuth2 login, CORS, CSRF)
- Role-based endpoint protection (`@PreAuthorize`, `@Secured`, or method security)
- JWT token handling after OAuth authentication (or session-based)
- Current user info endpoint
- User registration/profile completion after first OAuth login

#### Ticketing
- Ticket entity, repository, service, controller
- Ticket creation with category, description, priority, preferred contact details
- Ticket linked to a specific resource/location (reference to Member 1's resources)
- Ticket status workflow enforcement (OPEN → IN_PROGRESS → RESOLVED → CLOSED; REJECTED with reason)
- Technician assignment to a ticket
- Resolution notes added by technician
- Ticket listing with filters (by status, priority, category, assigned technician)
- Ticket detail retrieval

#### Attachments
- File upload handling (multipart form data, up to 3 images per ticket)
- File type validation (allow only image types: JPEG, PNG, etc.)
- File size validation (enforce reasonable max file size)
- Safe file storage (local filesystem or cloud, not in database BLOBs)
- Attachment retrieval/download endpoint
- Attachment deletion

#### Comments
- Comment entity, repository, service, controller
- Add comment on a ticket (by user or staff)
- Edit comment (owner only — verify authenticated user is comment author)
- Delete comment (owner or admin only)
- Comment ownership enforcement in service layer
- List comments for a ticket (ordered by creation time)

#### Notifications
- Notification entity, repository, service, controller
- Notification creation triggered by:
  - Booking approval (from Member 2's approve action)
  - Booking rejection (from Member 2's reject action)
  - Ticket status changes (from ticket workflow updates)
  - New comments on a user's ticket
- Notification retrieval for current authenticated user
- Mark notification as read
- Mark all notifications as read
- Unread notification count

### Backend Endpoints (minimum 4, different HTTP methods)

| #  | Method | Endpoint Path                                     | Purpose                                        |
| -- | ------ | ------------------------------------------------- | ---------------------------------------------- |
|    | **Authentication & User**                          |                                                 |                                               |
| 1  | GET    | `/api/auth/me`                                    | Get current authenticated user's profile/info  |
| 2  | PUT    | `/api/auth/profile`                               | Update user profile (after first OAuth login)  |
|    | **Ticketing**                                      |                                                 |                                               |
| 3  | POST   | `/api/tickets`                                    | Create a new incident ticket                   |
| 4  | GET    | `/api/tickets`                                    | List tickets (with filters: status, priority)  |
| 5  | GET    | `/api/tickets/{id}`                               | Get a single ticket by ID                      |
| 6  | PATCH  | `/api/tickets/{id}/status`                        | Update ticket status (workflow transition)     |
| 7  | PATCH  | `/api/tickets/{id}/assign`                        | Assign technician to a ticket                  |
| 8  | PATCH  | `/api/tickets/{id}/resolve`                       | Add resolution notes and mark resolved         |
| 9  | DELETE | `/api/tickets/{id}`                               | Delete a ticket (admin only)                   |
|    | **Attachments**                                    |                                                 |                                               |
| 10 | POST   | `/api/tickets/{id}/attachments`                   | Upload image attachment(s) to a ticket         |
| 11 | GET    | `/api/tickets/{id}/attachments`                   | List attachments for a ticket                  |
| 12 | GET    | `/api/tickets/{id}/attachments/{attachmentId}`    | Download a specific attachment                 |
| 13 | DELETE | `/api/tickets/{id}/attachments/{attachmentId}`    | Delete an attachment                           |
|    | **Comments**                                       |                                                 |                                               |
| 14 | POST   | `/api/tickets/{id}/comments`                      | Add a comment to a ticket                      |
