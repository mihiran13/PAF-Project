# API Reference & Endpoint Documentation

**Project**: Smart Campus Operations Hub  
**API Version**: 1.0  
**Base URL**: `http://localhost:8081/api/v1`  
**Authentication**: Bearer Token (JWT)

---

## 1. Authentication Endpoints

### 1.1 OAuth 2.0 Login
**Endpoint**: `POST /api/v1/auth/google`  
**Description**: Exchange Google OAuth token for JWT access token  
**Authentication**: None (public)

**Request Body**:
```json
{
  "token": "google_oauth_token_here"
}
```

**Response** (201 Created):
```json
{
  "status": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "email": "user@university.edu",
      "name": "John Doe",
      "role": "USER",
      "avatarUrl": "https://example.com/avatar.jpg"
    },
    "expiresIn": 86400
  },
  "timestamp": "2026-04-27T10:30:00Z"
}
```

**Error Response** (401 Unauthorized):
```json
{
  "status": "error",
  "error": {
    "code": "INVALID_TOKEN",
    "message": "Invalid or expired Google token"
  }
}
```

---

### 1.2 Logout
**Endpoint**: `POST /api/v1/auth/logout`  
**Description**: Invalidate current JWT token  
**Authentication**: Required (Bearer token)

**Response** (200 OK):
```json
{
  "status": "success",
  "data": {
    "message": "Successfully logged out"
  }
}
```

---

### 1.3 Verify Token
**Endpoint**: `GET /api/v1/auth/verify`  
**Description**: Verify JWT token validity  
**Authentication**: Required

**Response** (200 OK):
```json
{
  "status": "success",
  "data": {
    "valid": true,
    "userId": 1,
    "email": "user@university.edu",
    "role": "USER"
  }
}
```

---

## 2. User Endpoints

### 2.1 Get Current User Profile
**Endpoint**: `GET /api/v1/users/me`  
**Description**: Retrieve authenticated user's profile  
**Authentication**: Required

**Response** (200 OK):
```json
{
  "status": "success",
  "data": {
    "id": 1,
    "email": "user@university.edu",
    "name": "John Doe",
    "role": "USER",
    "avatarUrl": "https://example.com/avatar.jpg",
    "createdAt": "2026-03-15T08:00:00Z"
  }
}
```

---

### 2.2 Update User Profile
**Endpoint**: `PUT /api/v1/users/{id}`  
**Description**: Update user profile information  
**Authentication**: Required (Owner or ADMIN)

**Request Body**:
```json
{
  "name": "John Doe Updated",
  "avatarUrl": "https://example.com/new-avatar.jpg"
}
```

**Response** (200 OK): Updated user object

---

### 2.3 List All Users (Admin Only)
**Endpoint**: `GET /api/v1/users`  
**Description**: Retrieve list of all users with pagination  
**Authentication**: Required (ADMIN)

**Query Parameters**:
- `page` (int, default: 0): Page number
- `size` (int, default: 20): Items per page
- `role` (string, optional): Filter by role (USER, ADMIN, TECHNICIAN)

**Response** (200 OK):
```json
{
  "status": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "email": "user1@university.edu",
        "name": "User 1",
        "role": "USER",
        "createdAt": "2026-03-15T08:00:00Z"
      },
      {
        "id": 2,
        "email": "admin@university.edu",
        "name": "Admin User",
        "role": "ADMIN",
        "createdAt": "2026-02-01T10:00:00Z"
      }
    ],
    "totalElements": 150,
    "totalPages": 8,
    "currentPage": 0
  }
}
```

---

### 2.4 Update User Role (Admin Only)
**Endpoint**: `PATCH /api/v1/users/{id}/role`  
**Description**: Change user role  
**Authentication**: Required (ADMIN)

**Request Body**:
```json
{
  "role": "TECHNICIAN"
}
```

**Response** (200 OK): Updated user object

---

## 3. Resource Endpoints

### 3.1 List All Resources
**Endpoint**: `GET /api/v1/resources`  
**Description**: Retrieve paginated list of all resources  
**Authentication**: None (public)

**Query Parameters**:
- `page` (int, default: 0): Page number
- `size` (int, default: 20): Items per page
- `type` (string, optional): Filter by resource type
- `status` (string, optional): Filter by status (AVAILABLE, OCCUPIED, etc.)
- `location` (string, optional): Filter by location
- `search` (string, optional): Search by name or location

**Response** (200 OK):
```json
{
  "status": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Conference Hall A",
        "type": "Conference Room",
        "capacity": 50,
        "location": "Building A, Floor 2",
        "status": "AVAILABLE",
        "imageUrl": "https://example.com/resource-1.jpg",
        "createdAt": "2026-01-01T00:00:00Z"
      }
    ],
    "totalElements": 45,
    "totalPages": 3,
    "currentPage": 0
  }
}
```

---

### 3.2 Get Resource Details
**Endpoint**: `GET /api/v1/resources/{id}`  
**Description**: Retrieve detailed information about a specific resource  
**Authentication**: None (public)

**Response** (200 OK):
```json
{
  "status": "success",
  "data": {
    "id": 1,
    "name": "Conference Hall A",
    "type": "Conference Room",
    "capacity": 50,
    "location": "Building A, Floor 2",
    "status": "AVAILABLE",
    "imageUrl": "https://example.com/resource-1.jpg",
    "amenities": ["Projector", "WiFi", "Air Conditioning"],
    "upcomingBookings": [
      {
        "id": 10,
        "startTime": "2026-04-27T14:00:00Z",
        "endTime": "2026-04-27T15:00:00Z",
        "userName": "Jane Smith"
      }
    ],
    "createdAt": "2026-01-01T00:00:00Z"
  }
}
```

---

### 3.3 Create Resource (Admin Only)
**Endpoint**: `POST /api/v1/resources`  
**Description**: Create a new resource  
**Authentication**: Required (ADMIN)

**Request Body**:
```json
{
  "name": "New Meeting Room",
  "type": "Conference Room",
  "capacity": 20,
  "location": "Building B, Floor 1",
  "imageUrl": "https://example.com/image.jpg",
  "amenities": ["Whiteboard", "WiFi"]
}
```

**Response** (201 Created): Created resource object

---

### 3.4 Update Resource (Admin Only)
**Endpoint**: `PUT /api/v1/resources/{id}`  
**Description**: Update resource information  
**Authentication**: Required (ADMIN)

**Request Body**: Same as create

**Response** (200 OK): Updated resource object

---

### 3.5 Update Resource Status (Admin Only)
**Endpoint**: `PATCH /api/v1/resources/{id}/status`  
**Description**: Change resource status  
**Authentication**: Required (ADMIN)

**Request Body**:
```json
{
  "status": "MAINTENANCE"
}
```

**Response** (200 OK): Updated resource object

---

### 3.6 Delete Resource (Admin Only)
**Endpoint**: `DELETE /api/v1/resources/{id}`  
**Description**: Delete a resource permanently  
**Authentication**: Required (ADMIN)

**Response** (204 No Content)

---

### 3.7 Get Resource Availability
**Endpoint**: `GET /api/v1/resources/{id}/availability`  
**Description**: Check resource availability for specific date range  
**Authentication**: None

**Query Parameters**:
- `startDate` (ISO 8601): Start date
- `endDate` (ISO 8601): End date

**Response** (200 OK):
```json
{
  "status": "success",
  "data": {
    "resourceId": 1,
    "availableSlots": [
      {
        "date": "2026-04-27",
        "timeSlots": [
          { "startTime": "09:00", "endTime": "12:00" },
          { "startTime": "13:00", "endTime": "17:00" }
        ]
      }
    ]
  }
}
```

---

## 4. Booking Endpoints

### 4.1 Create Booking
**Endpoint**: `POST /api/v1/bookings`  
**Description**: Create a new booking request  
**Authentication**: Required (USER, ADMIN, TECHNICIAN)

**Request Body**:
```json
{
  "resourceId": 1,
  "startTime": "2026-04-27T14:00:00Z",
  "endTime": "2026-04-27T15:00:00Z",
  "notes": "Team meeting discussion"
}
```

**Response** (201 Created):
```json
{
  "status": "success",
  "data": {
    "id": 1,
    "resourceId": 1,
    "userId": 1,
    "resourceName": "Conference Hall A",
    "userName": "John Doe",
    "startTime": "2026-04-27T14:00:00Z",
    "endTime": "2026-04-27T15:00:00Z",
    "status": "PENDING",
    "notes": "Team meeting discussion",
    "createdAt": "2026-04-27T10:30:00Z"
  }
}
```

**Error Response** (409 Conflict):
```json
{
  "status": "error",
  "error": {
    "code": "BOOKING_CONFLICT",
    "message": "Resource is already booked for this time slot"
  }
}
```

---

### 4.2 List My Bookings
**Endpoint**: `GET /api/v1/bookings/my`  
**Description**: Retrieve current user's bookings  
**Authentication**: Required

**Query Parameters**:
- `status` (string, optional): Filter by status
- `page` (int, default: 0): Page number

**Response** (200 OK): List of user's bookings

---

### 4.3 List All Bookings (Admin)
**Endpoint**: `GET /api/v1/bookings`  
**Description**: Retrieve all bookings with filters  
**Authentication**: Required (ADMIN)

**Query Parameters**:
- `resourceId` (long, optional): Filter by resource
- `userId` (long, optional): Filter by user
- `status` (string, optional): Filter by status
- `startDate` (ISO 8601, optional): Filter by start date
- `page` (int, default: 0): Page number

**Response** (200 OK): Paginated list of bookings

---

### 4.4 Get Booking Details
**Endpoint**: `GET /api/v1/bookings/{id}`  
**Description**: Retrieve specific booking information  
**Authentication**: Required (Owner or ADMIN)

**Response** (200 OK): Booking object

---

### 4.5 Approve Booking (Admin Only)
**Endpoint**: `PATCH /api/v1/bookings/{id}/approve`  
**Description**: Approve pending booking  
**Authentication**: Required (ADMIN)

**Response** (200 OK):
```json
{
  "status": "success",
  "data": {
    "id": 1,
    "status": "APPROVED",
    "message": "Booking approved successfully"
  }
}
```

---

### 4.6 Reject Booking (Admin Only)
**Endpoint**: `PATCH /api/v1/bookings/{id}/reject`  
**Description**: Reject pending booking  
**Authentication**: Required (ADMIN)

**Request Body**:
```json
{
  "reason": "Resource under maintenance"
}
```

**Response** (200 OK): Updated booking object

---

### 4.7 Cancel Booking
**Endpoint**: `PATCH /api/v1/bookings/{id}/cancel`  
**Description**: Cancel a booking  
**Authentication**: Required (Owner or ADMIN)

**Request Body**:
```json
{
  "reason": "Schedule conflict"
}
```

**Response** (200 OK): Updated booking object

---

### 4.8 Update Booking
**Endpoint**: `PUT /api/v1/bookings/{id}`  
**Description**: Modify booking time or notes  
**Authentication**: Required (Owner or ADMIN)

**Request Body**:
```json
{
  "startTime": "2026-04-27T15:00:00Z",
  "endTime": "2026-04-27T16:00:00Z",
  "notes": "Updated notes"
}
```

**Response** (200 OK): Updated booking object

---

## 5. Ticket Endpoints

### 5.1 Create Ticket
**Endpoint**: `POST /api/v1/tickets`  
**Description**: Create new maintenance ticket  
**Authentication**: Required

**Request Body**:
```json
{
  "title": "Broken chair in Conference Room A",
  "description": "Office chair is broken and unsafe",
  "category": "Furniture",
  "priority": "HIGH",
  "resourceId": 1
}
```

**Response** (201 Created):
```json
{
  "status": "success",
  "data": {
    "id": 1,
    "ticketNumber": "TKT-2026-001",
    "title": "Broken chair in Conference Room A",
    "description": "Office chair is broken and unsafe",
    "category": "Furniture",
    "priority": "HIGH",
    "status": "OPEN",
    "createdBy": { "id": 1, "name": "John Doe" },
    "assignedTo": null,
    "createdAt": "2026-04-27T10:30:00Z"
  }
}
```

---

### 5.2 List My Tickets
**Endpoint**: `GET /api/v1/tickets/my`  
**Description**: Retrieve current user's tickets  
**Authentication**: Required

**Response** (200 OK): List of user's tickets

---

### 5.3 List All Tickets
**Endpoint**: `GET /api/v1/tickets`  
**Description**: Retrieve all tickets with filters  
**Authentication**: Required

**Query Parameters**:
- `status` (string, optional): OPEN, IN_PROGRESS, RESOLVED, CLOSED, REOPENED
- `priority` (string, optional): LOW, MEDIUM, HIGH, URGENT
- `category` (string, optional): Filter by category
- `assignedTo` (long, optional): Filter by technician
- `page` (int): Page number
- `size` (int): Items per page

**Response** (200 OK): Paginated list

---

### 5.4 Get Ticket Details
**Endpoint**: `GET /api/v1/tickets/{id}`  
**Description**: Retrieve full ticket information with comments  
**Authentication**: Required

**Response** (200 OK):
```json
{
  "status": "success",
  "data": {
    "id": 1,
    "ticketNumber": "TKT-2026-001",
    "title": "Broken chair",
    "category": "Furniture",
    "priority": "HIGH",
    "status": "IN_PROGRESS",
    "description": "...",
    "createdBy": { "id": 1, "name": "John Doe" },
    "assignedTo": { "id": 2, "name": "Tech Smith" },
    "createdAt": "2026-04-27T10:30:00Z",
    "updatedAt": "2026-04-27T11:15:00Z",
    "comments": [
      {
        "id": 1,
        "author": { "id": 2, "name": "Tech Smith" },
        "text": "Working on repair",
        "createdAt": "2026-04-27T11:15:00Z"
      }
    ],
    "attachments": [
      {
        "id": 1,
        "fileName": "damage.jpg",
        "fileUrl": "https://example.com/uploads/damage.jpg",
        "fileSize": "245KB"
      }
    ]
  }
}
```

---

### 5.5 Assign Ticket (Admin/Tech Only)
**Endpoint**: `PATCH /api/v1/tickets/{id}/assign`  
**Description**: Assign ticket to technician  
**Authentication**: Required (ADMIN, TECHNICIAN)

**Request Body**:
```json
{
  "technicianId": 2
}
```

**Response** (200 OK): Updated ticket

---

### 5.6 Update Ticket Status
**Endpoint**: `PATCH /api/v1/tickets/{id}/status`  
**Description**: Change ticket status  
**Authentication**: Required (ADMIN, TECHNICIAN, CREATOR)

**Request Body**:
```json
{
  "status": "IN_PROGRESS"
}
```

**Response** (200 OK): Updated ticket

---

### 5.7 Resolve Ticket
**Endpoint**: `PATCH /api/v1/tickets/{id}/resolve`  
**Description**: Mark ticket as resolved  
**Authentication**: Required (ADMIN, TECHNICIAN, CREATOR)

**Request Body**:
```json
{
  "resolutionNotes": "Chair repaired successfully"
}
```

**Response** (200 OK): Ticket marked resolved

---

### 5.8 Reopen Ticket
**Endpoint**: `PATCH /api/v1/tickets/{id}/reopen`  
**Description**: Reopen a resolved ticket  
**Authentication**: Required (CREATOR, ADMIN)

**Request Body**:
```json
{
  "reason": "Issue persists"
}
```

**Response** (200 OK): Ticket reopened

---

### 5.9 Delete Ticket (Admin Only)
**Endpoint**: `DELETE /api/v1/tickets/{id}`  
**Description**: Delete a ticket permanently  
**Authentication**: Required (ADMIN)

**Response** (204 No Content)

---

## 6. Comment Endpoints

### 6.1 Add Comment to Ticket
**Endpoint**: `POST /api/v1/tickets/{ticketId}/comments`  
**Description**: Add comment to ticket  
**Authentication**: Required

**Request Body**:
```json
{
  "text": "Update on repair progress"
}
```

**Response** (201 Created):
```json
{
  "status": "success",
  "data": {
    "id": 1,
    "ticketId": 1,
    "author": { "id": 2, "name": "Tech Smith" },
    "text": "Update on repair progress",
    "createdAt": "2026-04-27T11:30:00Z"
  }
}
```

---

### 6.2 List Ticket Comments
**Endpoint**: `GET /api/v1/tickets/{ticketId}/comments`  
**Description**: Retrieve all comments for ticket  
**Authentication**: Required

**Response** (200 OK): List of comments

---

### 6.3 Update Comment
**Endpoint**: `PUT /api/v1/comments/{id}`  
**Description**: Edit own comment  
**Authentication**: Required (Author, ADMIN)

**Request Body**:
```json
{
  "text": "Updated comment text"
}
```

**Response** (200 OK): Updated comment

---

### 6.4 Delete Comment
**Endpoint**: `DELETE /api/v1/comments/{id}`  
**Description**: Delete comment  
**Authentication**: Required (Author, ADMIN)

**Response** (204 No Content)

---

## 7. Notification Endpoints

### 7.1 Get My Notifications
**Endpoint**: `GET /api/v1/notifications`  
**Description**: Retrieve user's notifications with pagination  
**Authentication**: Required

**Query Parameters**:
- `unreadOnly` (boolean, default: false): Show only unread
- `page` (int, default: 0): Page number
- `size` (int, default: 20): Items per page

**Response** (200 OK):
```json
{
  "status": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "type": "BOOKING_APPROVED",
        "message": "Your booking for Conference Hall A has been approved",
        "relatedEntityId": 10,
        "relatedEntityType": "BOOKING",
        "isRead": false,
        "createdAt": "2026-04-27T11:00:00Z"
      }
    ],
    "totalElements": 50,
    "totalPages": 3,
    "currentPage": 0
  }
}
```

---

### 7.2 Mark Notification as Read
**Endpoint**: `PATCH /api/v1/notifications/{id}/read`  
**Description**: Mark single notification as read  
**Authentication**: Required

**Response** (200 OK): Updated notification

---

### 7.3 Mark All Notifications as Read
**Endpoint**: `PATCH /api/v1/notifications/mark-all-read`  
**Description**: Mark all notifications as read  
**Authentication**: Required

**Response** (200 OK): Count of updated notifications

---

### 7.4 Delete Notification
**Endpoint**: `DELETE /api/v1/notifications/{id}`  
**Description**: Delete notification  
**Authentication**: Required

**Response** (204 No Content)

---

## 8. Attachment Endpoints

### 8.1 Upload Attachment
**Endpoint**: `POST /api/v1/attachments`  
**Description**: Upload file (for tickets, etc.)  
**Authentication**: Required

**Form Data**:
- `file` (multipart/form-data): File to upload (Max 5MB)
- `entityType` (string): TICKET, BOOKING, etc.
- `entityId` (long): ID of related entity

**Response** (201 Created):
```json
{
  "status": "success",
  "data": {
    "id": 1,
    "fileName": "damage.jpg",
    "fileUrl": "https://example.com/uploads/damage.jpg",
    "fileSize": "245KB",
    "uploadedAt": "2026-04-27T11:30:00Z"
  }
}
```

---

### 8.2 Delete Attachment
**Endpoint**: `DELETE /api/v1/attachments/{id}`  
**Description**: Remove attachment  
**Authentication**: Required (ADMIN, CREATOR)

**Response** (204 No Content)

---

## 9. Dashboard/Analytics Endpoints

### 9.1 Get Dashboard Statistics
**Endpoint**: `GET /api/v1/dashboard/stats`  
**Description**: Retrieve system overview statistics  
**Authentication**: Required (ADMIN)

**Response** (200 OK):
```json
{
  "status": "success",
  "data": {
    "totalUsers": 150,
    "totalResources": 45,
    "totalBookings": 1200,
    "totalTickets": 380,
    "openTickets": 25,
    "averageBookingDuration": 95,
    "resourceUtilization": 78.5,
    "userGrowthThisMonth": 12
  }
}
```

---

### 9.2 Get Ticket Statistics
**Endpoint**: `GET /api/v1/dashboard/tickets/stats`  
**Description**: Ticket-related analytics  
**Authentication**: Required (ADMIN)

**Response** (200 OK):
```json
{
  "status": "success",
  "data": {
    "byPriority": { "LOW": 100, "MEDIUM": 150, "HIGH": 100, "URGENT": 30 },
    "byCategory": { "Plumbing": 50, "Electrical": 80, "Furniture": 150 },
    "Average_Resolution_Days": 3.2,
    "Ticket_SLA_Compliance": "94.5%"
  }
}
```

---

## 10. Health Check Endpoint

### 10.1 System Health
**Endpoint**: `GET /api/v1/health`  
**Description**: Check API and database health  
**Authentication**: None

**Response** (200 OK):
```json
{
  "status": "UP",
  "components": {
    "database": { "status": "UP" },
    "api": { "status": "UP" },
    "authentication": { "status": "UP" }
  },
  "timestamp": "2026-04-27T11:30:00Z"
}
```

---

## 11. Error Response Format

All endpoints return standardized error responses:

### 400 Bad Request
```json
{
  "status": "error",
  "error": {
    "code": "INVALID_PARAMETER",
    "message": "Invalid input provided",
    "details": {
      "startTime": "Must be a valid ISO 8601 datetime",
      "endTime": "Must be after startTime"
    }
  }
}
```

### 401 Unauthorized
```json
{
  "status": "error",
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Authentication token is missing or invalid"
  }
}
```

### 403 Forbidden
```json
{
  "status": "error",
  "error": {
    "code": "FORBIDDEN",
    "message": "You do not have permission to access this resource"
  }
}
```

### 404 Not Found
```json
{
  "status": "error",
  "error": {
    "code": "NOT_FOUND",
    "message": "Resource with ID 999 not found"
  }
}
```

### 409 Conflict
```json
{
  "status": "error",
  "error": {
    "code": "CONFLICT",
    "message": "Resource is already booked for this time slot"
  }
}
```

### 500 Internal Server Error
```json
{
  "status": "error",
  "error": {
    "code": "SERVER_ERROR",
    "message": "An internal server error occurred. Please try again later."
  }
}
```

---

## 12. Rate Limiting

**Default Limits**:
- 100 requests per minute per API key (global)
- 5 failed authentication attempts per 15 minutes (locks account temporarily)

**Response Headers**:
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1682099400
```

---

**API Version**: 1.0  
**Last Updated**: April 27, 2026  
**Status**: PRODUCTION READY
