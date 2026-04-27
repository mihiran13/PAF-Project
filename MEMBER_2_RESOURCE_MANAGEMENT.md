# 📚 MEMBER 2: RESOURCE MANAGEMENT - Quick Reference

## Overview
You are responsible for **Resource Management** - managing all campus facilities and equipment.

**Total API Endpoints:** 7  
**Frontend Pages:** 3  
**Database Tables:** 2 (resources + availability windows)  
**Est. Lines of Code:** ~2,500 (backend + frontend)

---

## 🎯 Core Responsibilities

### What You Build
- Catalog of all campus resources (rooms, equipment, facilities)
- Resource categorization and attributes
- Availability scheduling (which hours rooms are bookable)
- Resource status management (active, maintenance)
- Resource search and filtering

### Who Uses It
- **Students:** Browse and view available resources
- **Admins:** Create and maintain resource catalog
- **Booking System:** Check resource availability for bookings
- **System:** Filter and search resources

---

## 📂 Backend Files You Own

### Controllers
📄 `smart-campus/src/main/java/com/smartcampus/controller/ResourceController.java`

**Endpoints to Implement:**
```java
@PostMapping("/resources")                    // Create resource (admin)
@GetMapping("/resources")                     // List with filters (search, type, capacity)
@GetMapping("/resources/{id}")                // Get resource details
@PutMapping("/resources/{id}")                // Update resource (admin)
@PatchMapping("/resources/{id}/status")       // Update status ACTIVE/OUT_OF_SERVICE
@DeleteMapping("/resources/{id}")             // Delete resource (admin)
@PostMapping("/resources/{id}/availability")  // Add availability window
```

### Service Class
📄 `smart-campus/src/main/java/com/smartcampus/service/ResourceService.java`

**Key Methods:**
```java
public ResourceResponse createResource(ResourceRequest request)
public ResourceResponse updateResource(Long id, ResourceRequest request)
public ResourceResponse getResource(Long id)
public List<ResourceResponse> listResources(ResourceFilter filter, Pageable pageable)
public ResourceResponse updateStatus(Long id, ResourceStatus status)
public void deleteResource(Long id)
public AvailabilityWindowResponse addAvailabilityWindow(Long resourceId, AvailabilityWindowRequest req)
public boolean isAvailable(Long resourceId, LocalDateTime start, LocalDateTime end)
```

### Entity Models
📄 `smart-campus/src/main/java/com/smartcampus/model/Resource.java`

**Fields:**
```java
@Id Long id
String name                    // "Study Room 101"
ResourceType type             // STUDY_ROOM, LAB, MEETING_ROOM, etc.
Integer capacity              // Max people allowed
String location               // "Building A, Floor 2"
String description            // Detailed description
ResourceStatus status         // ACTIVE, OUT_OF_SERVICE
@OneToMany List<Booking> bookings
@OneToMany List<AvailabilityWindow> availabilityWindows
LocalDateTime createdAt
LocalDateTime updatedAt
```

📄 `smart-campus/src/main/java/com/smartcampus/model/ResourceAvailabilityWindow.java`

**Fields:**
```java
@Id Long id
@ManyToOne Resource resource
DayOfWeek dayOfWeek           // MONDAY, TUESDAY, etc.
LocalTime startTime           // 08:00
LocalTime endTime             // 18:00
LocalDateTime createdAt
```

### Enums
📄 `smart-campus/src/main/java/com/smartcampus/enums/ResourceType.java`

```java
public enum ResourceType {
    STUDY_ROOM,        // Study rooms
    AUDITORIUM,        // Lecture halls with seats
    CAMERA,            // Video equipment
    LECTURE_HALL,      // Large class rooms
    LIBRARY,           // Library areas
    PROJECTOR,         // Projector equipment
    LAB,               // Laboratory
    OTHER_EQUIPMENT,   // Other devices
    SPORTS_FACILITY,   // Gym, courts
    MEETING_ROOM       // Conference rooms
}
```

📄 `smart-campus/src/main/java/com/smartcampus/enums/ResourceStatus.java`

```java
public enum ResourceStatus {
    ACTIVE,           // Available for booking
    OUT_OF_SERVICE    // Under maintenance or unavailable
}
```

### Repository
📄 `smart-campus/src/main/java/com/smartcampus/repository/ResourceRepository.java`

**Queries You Need:**
```java
List<Resource> findByType(ResourceType type)
List<Resource> findByStatus(ResourceStatus status)
List<Resource> findByCapacityGreaterThanEqual(Integer capacity)
List<Resource> findByNameContainingIgnoreCase(String name)
List<Resource> findByLocationContaining(String location)
Page<Resource> findAll(Specification<Resource> spec, Pageable pageable)

// For search/filter
List<Resource> findByTypeAndStatusAndCapacityGreaterThanEqual(...)
```

📄 `smart-campus/src/main/java/com/smartcampus/repository/AvailabilityWindowRepository.java`

**Queries:**
```java
List<AvailabilityWindow> findByResourceId(Long resourceId)
List<AvailabilityWindow> findByResourceIdAndDayOfWeek(Long resourceId, DayOfWeek day)
```

### DTOs
📄 `smart-campus/src/main/java/com/smartcampus/dto/request/ResourceRequest.java`

```java
String name
ResourceType type
Integer capacity
String location
String description
ResourceStatus status
```

📄 `smart-campus/src/main/java/com/smartcampus/dto/response/ResourceResponse.java`

```java
Long id
String name
ResourceType type
Integer capacity
String location
String description
ResourceStatus status
List<AvailabilityWindowResponse> availabilityWindows
LocalDateTime createdAt
```

---

## 🎨 Frontend Files You Own

### Pages

**1. ResourceListPage.jsx**
```jsx
// Path: smart-campus-frontend/src/pages/ResourceListPage.jsx
// Features:
// - Display all resources in cards or table
// - Search by name
// - Filter by type (dropdown)
// - Filter by capacity (range slider)
// - Filter by location (search)
// - Filter by status
// - Pagination
// - Click resource to view details
// - Book button (redirects to booking page)
```

**2. ResourceDetailPage.jsx**
```jsx
// Path: smart-campus-frontend/src/pages/ResourceDetailPage.jsx
// Features:
// - Full resource information
// - Resource type with icon
// - Capacity indicator
// - Location with map link
// - Description and amenities
// - Availability schedule (show hours for each day)
// - Photo gallery (if available)
// - "Book Now" button
// - Booking calendar (show occupied dates)
// - Related resources (same type)
```

**3. ResourceAdminPage.jsx**
```jsx
// Path: smart-campus-frontend/src/pages/ResourceAdminPage.jsx
// Features:
// - Admin only page
// - List of all resources in table
// - Create new resource button
// - Edit button for each resource
// - Delete button with confirmation
// - Edit form opens modal/drawer:
//   - Name, Type, Capacity, Location, Description, Status
//   - Submit/Cancel buttons
// - Add availability window button
// - Availability window list per resource
```

### API Service
📄 `smart-campus-frontend/src/services/api.js`

**Add resource service:**
```javascript
export const resourceService = {
  list: (params) => http.get('/api/resources', { params }), // params: type, capacity, location, search
  get: (id) => http.get(`/api/resources/${id}`),
  create: (payload) => http.post('/api/resources', payload),
  update: (id, payload) => http.put(`/api/resources/${id}`, payload),
  updateStatus: (id, status) => http.patch(`/api/resources/${id}/status`, { status }),
  delete: (id) => http.delete(`/api/resources/${id}`),
  addWindow: (id, payload) => http.post(`/api/resources/${id}/availability`, payload)
}
```

### Components (Reusable)
- **ResourceCard.jsx** - Card showing resource summary with image/icon
- **ResourceForm.jsx** - Reusable form for create/edit
- **ResourceFilters.jsx** - Search and filter sidebar component
- **AvailabilitySchedule.jsx** - Display availability by day
- **CapacityIndicator.jsx** - Visual capacity bar

### Constants
📄 `smart-campus-frontend/src/utils/constants.js`

**Add these constants:**
```javascript
export const RESOURCE_TYPES = {
  STUDY_ROOM: 'STUDY_ROOM',
  AUDITORIUM: 'AUDITORIUM',
  CAMERA: 'CAMERA',
  LECTURE_HALL: 'LECTURE_HALL',
  LIBRARY: 'LIBRARY',
  PROJECTOR: 'PROJECTOR',
  LAB: 'LAB',
  OTHER_EQUIPMENT: 'OTHER_EQUIPMENT',
  SPORTS_FACILITY: 'SPORTS_FACILITY',
  MEETING_ROOM: 'MEETING_ROOM'
}

export const RESOURCE_STATUSES = {
  ACTIVE: 'ACTIVE',
  OUT_OF_SERVICE: 'OUT_OF_SERVICE'
}
```

---

## 📊 Database Schema

### Resources Table
```sql
CREATE TABLE resources (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL UNIQUE,
  type VARCHAR(50) NOT NULL,  -- STUDY_ROOM, LAB, etc.
  capacity INT NOT NULL,
  location VARCHAR(255) NOT NULL,
  description TEXT,
  status VARCHAR(20) NOT NULL,  -- ACTIVE, OUT_OF_SERVICE
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_type (type),
  INDEX idx_status (status),
  INDEX idx_location (location)
);
```

### Availability Windows Table
```sql
CREATE TABLE availability_windows (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  resource_id BIGINT NOT NULL,
  day_of_week VARCHAR(10) NOT NULL,  -- MONDAY, TUESDAY, etc.
  start_time TIME NOT NULL,  -- 08:00:00
  end_time TIME NOT NULL,    -- 18:00:00
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (resource_id) REFERENCES resources(id),
  UNIQUE KEY uk_resource_day (resource_id, day_of_week),
  INDEX idx_resource (resource_id)
);
```

---

## ✅ Development Checklist

- [ ] Create ResourceType enum with 10 types
- [ ] Create ResourceStatus enum (ACTIVE, OUT_OF_SERVICE)
- [ ] Create Resource JPA entity
- [ ] Create ResourceAvailabilityWindow entity
- [ ] Create ResourceRepository with all queries
- [ ] Create AvailabilityWindowRepository
- [ ] Create ResourceRequest DTO
- [ ] Create ResourceResponse DTO
- [ ] Create ResourceService with business logic
- [ ] Create ResourceController with 7 endpoints
- [ ] Add resource filtering logic
- [ ] Write unit tests for ResourceService
- [ ] Add RESOURCE_TYPES and RESOURCE_STATUSES to constants.js
- [ ] Create ResourceListPage.jsx
- [ ] Create ResourceDetailPage.jsx
- [ ] Create ResourceAdminPage.jsx
- [ ] Implement resourceService in api.js
- [ ] Create reusable components (Card, Form, Filters, etc.)
- [ ] Test all endpoints in Postman
- [ ] Test all pages in browser
- [ ] Write RESOURCE_MANAGEMENT_GUIDE.md
- [ ] Update documentation/03_API_REFERENCE.md

---

## 🧪 Testing Scenarios

### Backend Testing (Postman)
1. Create a resource (STUDY_ROOM, capacity 30, location "Bldg A")
2. Verify resource created with status ACTIVE
3. Add availability window (Monday 8-18, Tuesday 8-18)
4. List resources with filter by type
5. Get resource details including availability
6. Update resource name/description
7. Update resource status to OUT_OF_SERVICE
8. Try to list resources, filter by status ACTIVE (should not show updated one)
9. Delete resource
10. Verify resource is deleted

### Frontend Testing
1. Navigate to Resources page
2. See all resources listed
3. Search by name - verify filtering
4. Filter by type - verify only selected type shown
5. Filter by capacity - verify correct results
6. Click on resource - view details page
7. See availability schedule
8. (Admin) Create new resource via form
9. (Admin) Edit existing resource
10. (Admin) Delete resource with confirmation

---

## 📈 Example Data for Testing

```json
{
  "resources": [
    {
      "id": 1,
      "name": "Study Room 101",
      "type": "STUDY_ROOM",
      "capacity": 30,
      "location": "Building A, Floor 2",
      "description": "Quiet study room with Wi-Fi and power outlets",
      "status": "ACTIVE"
    },
    {
      "id": 2,
      "name": "Main Auditorium",
      "type": "AUDITORIUM",
      "capacity": 500,
      "location": "Building C",
      "description": "Large auditorium with projector and sound system",
      "status": "ACTIVE"
    },
    {
      "id": 3,
      "name": "Computer Lab 5",
      "type": "LAB",
      "capacity": 40,
      "location": "Building A, Floor 3",
      "description": "Computer lab with 40 workstations",
      "status": "ACTIVE"
    }
  ],
  "availabilityWindows": [
    {
      "resourceId": 1,
      "dayOfWeek": "MONDAY",
      "startTime": "08:00",
      "endTime": "18:00"
    },
    {
      "resourceId": 1,
      "dayOfWeek": "TUESDAY",
      "startTime": "08:00",
      "endTime": "18:00"
    }
  ]
}
```

---

## 🔗 Integration Points

### Depends On
- User authentication (login, JWT token)
- Authorization middleware

### Used By
- Booking System (to select resources)
- Dashboard (to show resource statistics)
- Analytics (resource usage statistics)

### Shares Data With
- Booking System (resource availability)
- Notification System (resource updates)

---

## 💡 Key Considerations

1. **Resource Uniqueness**: Resource names should be unique
2. **Availability Windows**: Represent recurring hours (Mon-Fri 8-18, etc.)
3. **Status Management**: Status affects whether resource can be booked
4. **Capacity**: Used for filtering by number of people needed
5. **Search/Filter**: Must support multiple filter criteria simultaneously
6. **Pagination**: Use pagination for large resource lists
7. **Validations**: Ensure capacity > 0, end_time > start_time, etc.

---

## 📚 SQL Queries Reference

```sql
-- Get all active study rooms with capacity >= 20
SELECT * FROM resources 
WHERE status = 'ACTIVE' 
AND type = 'STUDY_ROOM' 
AND capacity >= 20;

-- Get availability for a specific resource
SELECT * FROM availability_windows 
WHERE resource_id = 1 
ORDER BY day_of_week;

-- Get resources available on a specific day
SELECT r.* FROM resources r
INNER JOIN availability_windows w ON r.id = w.resource_id
WHERE w.day_of_week = 'MONDAY'
AND r.status = 'ACTIVE';
```

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
curl http://localhost:8081/api/resources
```

---

## 📋 API Request/Response Examples

### CREATE RESOURCE
```http
POST /api/resources
Authorization: Bearer eyJhbGc...
Content-Type: application/json

{
  "name": "Study Room 101",
  "type": "STUDY_ROOM",
  "capacity": 30,
  "location": "Building A, Floor 2",
  "description": "Quiet study room with Wi-Fi",
  "status": "ACTIVE"
}

Response:
{
  "status": "success",
  "data": {
    "id": 1,
    "name": "Study Room 101",
    "type": "STUDY_ROOM",
    "capacity": 30,
    "location": "Building A, Floor 2",
    "description": "Quiet study room with Wi-Fi",
    "status": "ACTIVE"
  }
}
```

### LIST RESOURCES WITH FILTERS
```http
GET /api/resources?type=STUDY_ROOM&capacity=20&location=Building%20A
Authorization: Bearer eyJhbGc...

Response:
{
  "status": "success",
  "data": [
    { "id": 1, "name": "Study Room 101", ... },
    { "id": 2, "name": "Study Room 102", ... }
  ],
  "pagination": {
    "total": 2,
    "page": 0,
    "size": 20
  }
}
```

### ADD AVAILABILITY WINDOW
```http
POST /api/resources/1/availability
Authorization: Bearer eyJhbGc...

{
  "dayOfWeek": "MONDAY",
  "startTime": "08:00:00",
  "endTime": "18:00:00"
}

Response:
{
  "status": "success",
  "data": {
    "id": 1,
    "resourceId": 1,
    "dayOfWeek": "MONDAY",
    "startTime": "08:00:00",
    "endTime": "18:00:00"
  }
}
```

---

**Created:** April 27, 2026  
**Status:** Ready for Development  
**Questions?** Contact team members or check GitHub discussions
