# 🎟️ MEMBER 3: SUPPORT TICKET SYSTEM - Quick Reference

## Overview
You are responsible for **Support Ticket System** - managing help requests and support communications.

**Total API Endpoints:** 8  
**Frontend Pages:** 5  
**Database Tables:** 3 (tickets, comments, notifications)  
**Est. Lines of Code:** ~3,000 (backend + frontend)

---

## 🎯 Core Responsibilities

### What You Build
- Support ticket creation for reporting issues
- Ticket categorization (priority, status)
- Comment/discussion threads on tickets
- Real-time notifications for ticket updates
- Ticket assignment to technicians
- Status workflow tracking

### Who Uses It
- **Students:** Create tickets to report issues
- **Technicians:** Assign and resolve tickets
- **Admins:** Manage all tickets system-wide
- **System:** Send notifications

---

## 📂 Backend Files You Own

### Controllers
📄 `smart-campus/src/main/java/com/smartcampus/controller/TicketController.java`

**Endpoints to Implement:**
```java
@PostMapping("/tickets")                  // Create ticket
@GetMapping("/tickets/my")                // Get user's tickets
@GetMapping("/tickets")                   // Get all tickets (admin/technician)
@GetMapping("/tickets/{id}")              // Get ticket details
@PutMapping("/tickets/{id}")              // Update ticket
@PatchMapping("/tickets/{id}/status")     // Update status
@PostMapping("/tickets/{id}/comments")    // Add comment
@GetMapping("/tickets/{id}/comments")     // Get comments

@GetMapping("/notifications")             // Get notifications
@GetMapping("/notifications/unread")      // Get unread count
@PatchMapping("/notifications/{id}/read") // Mark as read
```

### Service Classes
📄 `smart-campus/src/main/java/com/smartcampus/service/TicketService.java`

**Key Methods:**
```java
public TicketResponse createTicket(TicketRequest request)
public TicketResponse updateTicket(Long id, TicketRequest request)
public TicketResponse getTicket(Long id)
public List<TicketResponse> getUserTickets(Long userId, Pageable pageable)
public List<TicketResponse> getAllTickets(Pageable pageable)
public TicketResponse updateStatus(Long id, TicketStatus status)
public CommentResponse addComment(Long ticketId, CommentRequest request)
public List<CommentResponse> getComments(Long ticketId)
```

📄 `smart-campus/src/main/java/com/smartcampus/service/NotificationService.java`

**Key Methods:**
```java
public NotificationResponse createNotification(NotificationRequest request)
public List<NotificationResponse> getUserNotifications(Long userId, Pageable pageable)
public Integer getUnreadCount(Long userId)
public void markAsRead(Long notificationId)
public void markAllAsRead(Long userId)
public void sendTicketNotification(Long ticketId, String type) // type: CREATED, ASSIGNED, STATUS_CHANGED
```

### Entity Models
📄 `smart-campus/src/main/java/com/smartcampus/model/Ticket.java`

**Fields:**
```java
@Id Long id
String title                  // "Projector not working in Room 101"
String description            // Detailed problem description
TicketPriority priority       // LOW, MEDIUM, HIGH, CRITICAL
TicketStatus status           // OPEN, IN_PROGRESS, RESOLVED, CLOSED, REJECTED
@ManyToOne User createdBy    // Who created the ticket
@ManyToOne User assignedTo   // Technician assigned
@ManyToOne Resource resource // Related resource (optional)
@OneToMany List<Comment> comments
LocalDateTime createdAt
LocalDateTime updatedAt
LocalDateTime resolvedAt
String resolutionNotes
```

📄 `smart-campus/src/main/java/com/smartcampus/model/Comment.java`

**Fields:**
```java
@Id Long id
@ManyToOne Ticket ticket
@ManyToOne User author
String content
LocalDateTime createdAt
LocalDateTime updatedAt
```

📄 `smart-campus/src/main/java/com/smartcampus/model/Notification.java`

**Fields:**
```java
@Id Long id
String type                // TICKET_CREATED, TICKET_ASSIGNED, TICKET_RESOLVED, etc.
String message            // "Your ticket #5 was resolved"
@ManyToOne User recipient
String resourceType       // TICKET, BOOKING (for generic notifications)
Long resourceId           // ID of the ticket/booking
Boolean isRead
LocalDateTime createdAt
LocalDateTime readAt
```

### Enums
📄 `smart-campus/src/main/java/com/smartcampus/enums/TicketStatus.java`

```java
public enum TicketStatus {
    OPEN,         // Newly created
    IN_PROGRESS,  // Technician is working
    RESOLVED,     // Issue fixed
    CLOSED,       // Confirmed closed
    REJECTED      // Cannot be resolved
}
```

📄 `smart-campus/src/main/java/com/smartcampus/enums/TicketPriority.java`

```java
public enum TicketPriority {
    LOW,          // Can wait
    MEDIUM,       // Should fix soon
    HIGH,         // Urgent
    CRITICAL      // System down or major impact
}
```

### Repositories
📄 `smart-campus/src/main/java/com/smartcampus/repository/TicketRepository.java`

**Queries You Need:**
```java
List<Ticket> findByCreatedById(Long userId)
List<Ticket> findByAssignedToId(Long technicianId)
List<Ticket> findByStatus(TicketStatus status)
List<Ticket> findByPriority(TicketPriority priority)
Page<Ticket> findAll(Specification<Ticket> spec, Pageable pageable)
List<Ticket> findByStatusOrderByCreatedAtDesc(TicketStatus status)
```

📄 `smart-campus/src/main/java/com/smartcampus/repository/CommentRepository.java`

**Queries:**
```java
List<Comment> findByTicketId(Long ticketId)
```

📄 `smart-campus/src/main/java/com/smartcampus/repository/NotificationRepository.java`

**Queries:**
```java
List<Notification> findByRecipientId(Long userId)
Long countByRecipientIdAndIsReadFalse(Long userId)
List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long userId)
```

### DTOs
📄 `smart-campus/src/main/java/com/smartcampus/dto/request/TicketRequest.java`

```java
String title
String description
TicketPriority priority
Long resourceId  // optional
```

📄 `smart-campus/src/main/java/com/smartcampus/dto/response/TicketResponse.java`

```java
Long id
String title
String description
TicketPriority priority
TicketStatus status
UserResponse createdBy
UserResponse assignedTo
ResourceResponse resource
List<CommentResponse> comments
LocalDateTime createdAt
LocalDateTime resolvedAt
```

---

## 🎨 Frontend Files You Own

### Pages

**1. TicketCreatePage.jsx**
```jsx
// Path: smart-campus-frontend/src/pages/TicketCreatePage.jsx
// Features:
// - Form to create new support ticket
// - Title field (required)
// - Description text area (required)
// - Priority selector: LOW, MEDIUM, HIGH, CRITICAL
// - Optional resource selector
// - Submit button
// - Success message and redirect to ticket detail
```

**2. TicketDetailPage.jsx**
```jsx
// Path: smart-campus-frontend/src/pages/TicketDetailPage.jsx
// Features:
// - Display ticket details
// - Show title, description, priority, status
// - Show created by, assigned to
// - Show resource link (if applicable)
// - Comments section with thread display
// - Input field to add new comment
// - Update status button (if technician)
// - Assign button (if admin)
// - Resolution notes field (if resolved)
```

**3. TicketListPage.jsx**
```jsx
// Path: smart-campus-frontend/src/pages/TicketListPage.jsx
// Features:
// - List of user's tickets
// - Filter by status: All, Open, In Progress, Resolved, Closed
// - Filter by priority
// - Search by title
// - Click to view details
// - Quick status badge
// - Priority color coding
// - Pagination
```

**4. TicketAdminPage.jsx**
```jsx
// Path: smart-campus-frontend/src/pages/TicketAdminPage.jsx
// Features:
// - Admin/Technician only page
// - List all system tickets
// - Filter by status, priority, created date, assigned to
// - Assign ticket to technician (dropdown)
// - Update status button
// - View ticket details modal/drawer
// - Analytics: tickets by status, priority breakdown, avg resolution time
```

**5. NotificationsPage.jsx**
```jsx
// Path: smart-campus-frontend/src/pages/NotificationsPage.jsx
// Features:
// - Notifications feed, newest first
// - Show: avatar, message, timestamp, resource link
// - Mark as read/unread toggles
// - Filter: All, Unread only
// - Delete notification button
// - Click notification to go to resource (ticket/booking)
// - Auto-load new notifications (polling or WebSocket)
```

### API Service
📄 `smart-campus-frontend/src/services/api.js`

**Add ticket service:**
```javascript
export const ticketService = {
  create: (payload) => http.post('/api/tickets', payload),
  my: (params) => http.get('/api/tickets/my', { params }),
  all: (params) => http.get('/api/tickets', { params }),
  get: (id) => http.get(`/api/tickets/${id}`),
  update: (id, payload) => http.put(`/api/tickets/${id}`, payload),
  updateStatus: (id, status) => http.patch(`/api/tickets/${id}/status`, { status }),
  addComment: (id, content) => http.post(`/api/tickets/${id}/comments`, { content }),
  getComments: (id) => http.get(`/api/tickets/${id}/comments`)
}

export const notificationService = {
  list: (params) => http.get('/api/notifications', { params }),
  getUnreadCount: () => http.get('/api/notifications/unread'),
  markAsRead: (id) => http.patch(`/api/notifications/${id}/read`)
}
```

### Components (Reusable)
- **TicketForm.jsx** - Form for creating/editing tickets
- **TicketList.jsx** - Table/list component for displaying tickets
- **CommentThread.jsx** - Display comments with reply functionality
- **PriorityBadge.jsx** - Priority display with color coding
- **StatusBadge.jsx** - Status display component
- **NotificationFeed.jsx** - Notifications list component
- **NotificationItem.jsx** - Individual notification component
- **CommentInput.jsx** - Input component to add comments

### Constants
📄 `smart-campus-frontend/src/utils/constants.js`

**Add these constants:**
```javascript
export const TICKET_STATUSES = ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'REJECTED']
export const TICKET_PRIORITIES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']

// For color coding UI
export const STATUS_COLORS = {
  OPEN: '#3498db',        // Blue
  IN_PROGRESS: '#f39c12', // Orange
  RESOLVED: '#27ae60',    // Green
  CLOSED: '#95a5a6',      // Gray
  REJECTED: '#e74c3c'     // Red
}

export const PRIORITY_COLORS = {
  LOW: '#2ecc71',      // Green
  MEDIUM: '#f39c12',   // Orange
  HIGH: '#e74c3c',     // Red
  CRITICAL: '#c0392b'  // Dark Red
}
```

---

## 📊 Database Schema

### Tickets Table
```sql
CREATE TABLE tickets (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  description TEXT NOT NULL,
  priority VARCHAR(20) NOT NULL,  -- LOW, MEDIUM, HIGH, CRITICAL
  status VARCHAR(20) NOT NULL,    -- OPEN, IN_PROGRESS, RESOLVED, CLOSED, REJECTED
  created_by BIGINT NOT NULL,
  assigned_to BIGINT,             -- NULL if not assigned
  resource_id BIGINT,             -- Optional
  resolution_notes TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  resolved_at TIMESTAMP,
  FOREIGN KEY (created_by) REFERENCES users(id),
  FOREIGN KEY (assigned_to) REFERENCES users(id),
  FOREIGN KEY (resource_id) REFERENCES resources(id),
  INDEX idx_status (status),
  INDEX idx_priority (priority),
  INDEX idx_created_by (created_by),
  INDEX idx_assigned_to (assigned_to)
);
```

### Comments Table
```sql
CREATE TABLE comments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  ticket_id BIGINT NOT NULL,
  author_id BIGINT NOT NULL,
  content TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
  FOREIGN KEY (author_id) REFERENCES users(id),
  INDEX idx_ticket_id (ticket_id)
);
```

### Notifications Table
```sql
CREATE TABLE notifications (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  type VARCHAR(50) NOT NULL,         -- TICKET_CREATED, BOOKING_APPROVED, etc.
  message TEXT NOT NULL,
  recipient_id BIGINT NOT NULL,
  resource_type VARCHAR(50),         -- TICKET, BOOKING
  resource_id BIGINT,
  is_read BOOLEAN DEFAULT FALSE,
  read_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (recipient_id) REFERENCES users(id),
  INDEX idx_recipient_id (recipient_id),
  INDEX idx_is_read (is_read),
  INDEX idx_created_at (created_at)
);
```

---

## ✅ Development Checklist

- [ ] Create TicketStatus enum
- [ ] Create TicketPriority enum
- [ ] Create Ticket JPA entity
- [ ] Create Comment entity
- [ ] Create Notification entity
- [ ] Create TicketRepository with queries
- [ ] Create CommentRepository
- [ ] Create NotificationRepository
- [ ] Create TicketRequest/Response DTOs
- [ ] Create CommentRequest/Response DTOs
- [ ] Create NotificationResponse DTO
- [ ] Create TicketService with business logic
- [ ] Create NotificationService
- [ ] Create TicketController with 8 endpoints
- [ ] Implement notification trigger on ticket changes
- [ ] Write unit tests for TicketService
- [ ] Create TICKET_STATUSES and TICKET_PRIORITIES constants
- [ ] Create TicketCreatePage.jsx
- [ ] Create TicketDetailPage.jsx
- [ ] Create TicketListPage.jsx
- [ ] Create TicketAdminPage.jsx
- [ ] Create NotificationsPage.jsx
- [ ] Implement ticketService in api.js
- [ ] Implement notificationService in api.js
- [ ] Create reusable components
- [ ] Test all endpoints in Postman
- [ ] Test all pages in browser
- [ ] Write SUPPORT_TICKET_GUIDE.md
- [ ] Update documentation/03_API_REFERENCE.md

---

## 🧪 Testing Scenarios

### Backend Testing (Postman)
1. Create a ticket with title, description, priority MEDIUM
2. Verify ticket created with status OPEN
3. Get ticket details
4. Add comment to ticket
5. Get all comments for ticket
6. Update ticket status to IN_PROGRESS
7. Verify notification was sent
8. Get user notifications
9. Get unread notification count
10. Mark notification as read
11. Update ticket status to RESOLVED
12. Verify resolution_notes saved

### Frontend Testing
1. Navigate to "Create Ticket" page
2. Fill form (title, description, priority)
3. Submit form
4. Redirect to ticket detail page
5. Verify ticket appears in "My Tickets"
6. Add comment and verify it appears
7. (Admin) Assign ticket to technician
8. (Technician) Update ticket status
9. View NotificationsPage
10. Verify ticket update notifications appear

---

## 📈 Example Data

```json
{
  "tickets": [
    {
      "id": 1,
      "title": "Projector not working in Auditorium",
      "description": "The projector in the main auditorium is not turning on",
      "priority": "HIGH",
      "status": "OPEN",
      "createdBy": { "id": 2, "name": "John Student" },
      "assignedTo": null,
      "resourceId": 5
    },
    {
      "id": 2,
      "title": "WiFi down in Building B",
      "description": "WiFi network is not working on floor 3",
      "priority": "CRITICAL",
      "status": "IN_PROGRESS",
      "createdBy": { "id": 3, "name": "Jane Student" },
      "assignedTo": { "id": 8, "name": "Tech Support" },
      "resourceId": null
    }
  ],
  "comments": [
    {
      "id": 1,
      "ticketId": 2,
      "author": { "id": 8, "name": "Tech Support" },
      "content": "We're looking into the WiFi issue. Initial diagnosis shows router problem.",
      "createdAt": "2026-04-27T10:30:00"
    },
    {
      "id": 2,
      "ticketId": 2,
      "author": { "id": 8, "name": "Tech Support" },
      "content": "WiFi is now back online. Please test and confirm.",
      "createdAt": "2026-04-27T11:15:00"
    }
  ]
}
```

---

## 🔗 Integration Points

### Depends On
- User authentication (login, JWT token)
- User model (for created_by, assigned_to)
- Resource model (optional resource link)

### Used By
- Notifications system (sends updates)
- Dashboard (shows recent tickets)
- Analytics (ticket statistics)

### Shares Data With
- Booking System (can reference resource)
- Notification System (triggers it)

---

## 💡 Key Considerations

1. **Ticket Status Workflow**: OPEN → IN_PROGRESS → RESOLVED/CLOSED (or REJECTED)
2. **Priority Escalation**: CRITICAL tickets should be highlighted
3. **Comment Threading**: Comments are ordered by time
4. **Notifications**: Send on ticket creation, status change, comment added
5. **Assignment**: Only admins/technicians can assign
6. **User Permissions**: Users only see their own tickets (unless admin)
7. **Resolution Notes**: Only filled when status is RESOLVED
8. **Notification Types**: Different types for different events

---

## 🚀 Local Development

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
curl -H "Authorization: Bearer TOKEN" http://localhost:8081/api/tickets/my
```

---

## 📋 API Request/Response Examples

### CREATE TICKET
```http
POST /api/tickets
Authorization: Bearer eyJhbGc...

{
  "title": "Projector not working",
  "description": "The projector in room 101 won't turn on",
  "priority": "HIGH",
  "resourceId": 5
}

Response:
{
  "status": "success",
  "data": {
    "id": 3,
    "title": "Projector not working",
    "priority": "HIGH",
    "status": "OPEN",
    "createdBy": { "id": 1, "name": "John" },
    "createdAt": "2026-04-27T14:00:00"
  }
}
```

### ADD COMMENT
```http
POST /api/tickets/3/comments
Authorization: Bearer eyJhbGc...

{
  "content": "I've checked the power cable. It seems okay. Testing projector now."
}

Response:
{
  "status": "success",
  "data": {
    "id": 5,
    "ticketId": 3,
    "author": { "id": 8, "name": "Tech Support" },
    "content": "I've checked the power cable...",
    "createdAt": "2026-04-27T14:05:00"
  }
}
```

### UPDATE TICKET STATUS
```http
PATCH /api/tickets/3/status
Authorization: Bearer eyJhbGc...

{
  "status": "RESOLVED",
  "resolutionNotes": "Replaced faulty bulb in projector"
}

Response:
{
  "status": "success",
  "data": {
    "id": 3,
    "status": "RESOLVED",
    "resolvedAt": "2026-04-27T14:10:00"
  }
}
```

### GET NOTIFICATIONS
```http
GET /api/notifications?page=0&size=20
Authorization: Bearer eyJhbGc...

Response:
{
  "status": "success",
  "data": [
    {
      "id": 1,
      "type": "TICKET_CREATED",
      "message": "Your ticket #3 was created",
      "resourceType": "TICKET",
      "resourceId": 3,
      "isRead": false,
      "createdAt": "2026-04-27T14:00:00"
    },
    {
      "id": 2,
      "type": "TICKET_ASSIGNED",
      "message": "Ticket #3 was assigned to you",
      "resourceType": "TICKET",
      "resourceId": 3,
      "isRead": true,
      "createdAt": "2026-04-27T14:05:00"
    }
  ]
}
```

---

**Created:** April 27, 2026  
**Status:** Ready for Development  
**Questions?** Contact team members or check GitHub discussions
