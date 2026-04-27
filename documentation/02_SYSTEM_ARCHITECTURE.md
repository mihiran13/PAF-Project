# System Architecture & Design Document

**Project**: Smart Campus Operations Hub  
**Version**: 1.0  
**Date**: April 27, 2026

---

## 1. System Overview

The Smart Campus Operations Hub follows a **three-tier architecture** with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────┐
│                    CLIENT TIER                          │
│        React 18+ Frontend (Vite, React Router)         │
│          - UI Components                                │
│          - State Management (React Query, Context)     │
│          - Form Validation (React Hook Form, Zod)      │
└────────────────────┬────────────────────────────────────┘
                     │ REST API / HTTPS
┌────────────────────▼────────────────────────────────────┐
│               BUSINESS TIER                             │
│        Spring Boot 3.x REST API Server                 │
│          - Controllers (Request Handling)              │
│          - Services (Business Logic)                   │
│          - Security (JWT, OAuth 2.0)                  │
│          - Middleware (CORS, Exception Handling)       │
└────────────────────┬────────────────────────────────────┘
                     │ JDBC / SQL
┌────────────────────▼────────────────────────────────────┐
│                DATA TIER                                │
│        MySQL 8.0 Database                              │
│          - Relational Tables                            │
│          - Indexes for Performance                      │
│          - Transactions & ACID Compliance              │
│          - Backup & Replication                        │
└─────────────────────────────────────────────────────────┘
```

---

## 2. High-Level Architecture

### 2.1 Components

```
┌────────────────────────────────────────────────────────────────┐
│                       FRONTEND (React)                         │
├──────────────┬──────────────┬──────────────┬──────────────────┤
│              │              │              │                  │
│  Pages       │  Components  │  Services    │  Utilities       │
│  ─────────   │  ──────────  │  ────────    │  ──────────      │
│  - Login     │  - Layout    │  - API       │  - Constants     │
│  - Dashboard │  - Forms     │  (Axios)     │  - Validators    │
│  - Resources │  - Cards     │              │  - Formatters    │
│  - Bookings  │  - Modals    │  Context     │  - Storage       │
│  - Tickets   │  - Navigation│  ────────    │  - Error Mapper  │
│  - Admin     │              │  - Auth      │                  │
│              │              │  - Toast     │                  │
└──────────────┴──────────────┴──────────────┴──────────────────┘
                              │
                    REST API (HTTP/HTTPS)
                              │
┌────────────────────────────────────────────────────────────────┐
│                    BACKEND (Spring Boot)                       │
├────────────────────────────────────────────────────────────────┤
│                                                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │ Controllers  │  │ Services     │  │ Repositories │         │
│  │ ──────────   │  │ ────────     │  │ ────────────│         │
│  │ - Auth       │  │ - Resource   │  │ - JPARepository       │
│  │ - Resource   │  │ - Booking    │  │ - SQL Queries         │
│  │ - Booking    │  │ - Ticket     │  │ - Transactions        │
│  │ - Ticket     │  │ - User       │  │                      │
│  │ - Notification│ │ - Notification│         │                      │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
│                                                                │
│  ┌──────────────────────────────────────────────────────┐    │
│  │ Security Layer (Spring Security, JWT, OAuth 2.0)    │    │
│  │ - Authentication Filter                             │    │
│  │ - Authorization Check                              │    │
│  │ - Role-Based Access Control                        │    │
│  └──────────────────────────────────────────────────────┘    │
│                                                                │
│  ┌──────────────────────────────────────────────────────┐    │
│  │ Cross-Cutting Concerns                              │    │
│  │ - Exception Handling                                │    │
│  │ - Logging & Audit Trail                             │    │
│  │ - CORS Configuration                               │    │
│  │ - Request/Response Interceptors                    │    │
│  └──────────────────────────────────────────────────────┘    │
└────────────────────────────────────────────────────────────────┘
                              │
                    JDBC Connection Pool
                              │
┌────────────────────────────────────────────────────────────────┐
│              DATABASE (MySQL 8.0)                              │
├──────────────┬──────────────┬──────────────┬──────────────────┤
│              │              │              │                  │
│ Users        │ Bookings     │ Tickets      │ Notifications    │
│ ────────     │ ────────     │ ──────       │ ─────────────    │
│ - id(PK)     │ - id(PK)     │ - id(PK)     │ - id(PK)         │
│ - email(UK)  │ - resourceId │ - title      │ - userId (FK)    │
│ - name       │ - userId(FK) │ - category   │ - type           │
│ - role       │ - status     │ - priority   │ - message        │
│ - avatar_url │ - startTime  │ - status     │ - createdAt      │
│ - createdAt  │ - endTime    │ - createdAt  │ - read           │
│              │ - createdAt  │ - updatedAt  │                  │
│              │              │              │                  │
│ Resources    │ Comments     │              │                  │
│ ────────     │ ────────     │              │                  │
│ - id(PK)     │ - id(PK)     │              │                  │
│ - name       │ - ticketId   │              │                  │
│ - type       │ - userId     │              │                  │
│ - capacity   │ - text       │              │                  │
│ - location   │ - createdAt  │              │                  │
│ - status     │              │              │                  │
│ - imageUrl   │              │              │                  │
│ - createdAt  │              │              │                  │
└──────────────┴──────────────┴──────────────┴──────────────────┘
```

---

## 3. Database Schema

### 3.1 Entity-Relationship Diagram (ERD)

```
┌─────────────────────┐         ┌──────────────────┐
│      USERS          │1      *│   BOOKINGS       │
├─────────────────────┤◄───────┤──────────────────┤
│ id (PK)             │         │ id (PK)          │
│ email (UNIQUE)      │         │ resource_id (FK) │
│ name (VARCHAR)      │         │ user_id (FK)─────┼──┐
│ role (ENUM)         │         │ start_time       │  │
│ avatar_url          │         │ end_time         │  │
│ created_at          │         │ status (ENUM)    │  │
└─────────────────────┘         │ created_at       │  │
         │                       └──────────────────┘  │
         │                                             │
         │ 1          ┌─────────────────────┐        │
         └───────────*│     RESOURCES       │        │
                      ├─────────────────────┤        │
                      │ id (PK)             │        │
                      │ name (VARCHAR)      │        │
                      │ type (VARCHAR)      │        │
                      │ capacity (INT)      │        │
                      │ location (VARCHAR)  │        │
                      │ status (ENUM)       │        │
                      │ image_url           │        │
                      │ created_at          │        │
                      └─────────────────────┘        │
         │                                             │
         │ 1                                          │
         └────┐                                       │
         ┌────▼──────────────────┐                    │
         │     TICKETS           │                    │
         ├───────────────────────┤                    │
         │ id (PK)               │ 1           *      │
         │ title (VARCHAR)       │◄────────────┼──────┘
         │ description (TEXT)    │             |
         │ category (VARCHAR)    │   COMMENTS  │
         │ priority (ENUM)       │ ┌──────────────────────┐
         │ status (ENUM)         │ │ id (PK)              │
         │ created_by (FK)───────┼─┤ ticket_id (FK)       │
         │ assigned_to (FK)─┐    │ │ user_id (FK)         │
         │ created_at       │    │ │ text (TEXT)          │
         │ updated_at       │    │ │ created_at           │
         └──────────────────┘    │ └──────────────────────┘
                                 │
                                 └─ USERS

         ┌──────────────────────┐
         │  NOTIFICATIONS       │
         ├──────────────────────┤
         │ id (PK)              │
         │ user_id (FK)─────────┼──► USERS
         │ type (VARCHAR)       │
         │ message (TEXT)       │
         │ related_entity_id    │
         │ is_read (BOOL)       │
         │ created_at           │
         └──────────────────────┘
```

### 3.2 Schema Definition

```sql
-- USERS Table
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(255) UNIQUE NOT NULL,
  name VARCHAR(255) NOT NULL,
  role ENUM('USER', 'ADMIN', 'TECHNICIAN') DEFAULT 'USER',
  avatar_url TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- RESOURCES Table
CREATE TABLE resources (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  type VARCHAR(100) NOT NULL,
  capacity INT,
  location VARCHAR(255),
  status ENUM('AVAILABLE', 'OCCUPIED', 'MAINTENANCE', 'BLOCKED') DEFAULT 'AVAILABLE',
  image_url TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_type (type),
  INDEX idx_status (status),
  INDEX idx_location (location)
);

-- BOOKINGS Table
CREATE TABLE bookings (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  resource_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  start_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL,
  status ENUM('PENDING', 'APPROVED', 'REJECTED', 'CONFIRMED', 'CANCELLED', 'COMPLETED') DEFAULT 'PENDING',
  notes TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (resource_id) REFERENCES resources(id),
  FOREIGN KEY (user_id) REFERENCES users(id),
  INDEX idx_resource_status (resource_id, status),
  INDEX idx_user_id (user_id),
  INDEX idx_start_time (start_time),
  UNIQUE KEY uk_no_overlap (resource_id, start_time, end_time)
);

-- TICKETS Table
CREATE TABLE tickets (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  category VARCHAR(50) NOT NULL,
  priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM',
  status ENUM('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'REOPENED') DEFAULT 'OPEN',
  created_by BIGINT NOT NULL,
  assigned_to BIGINT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (created_by) REFERENCES users(id),
  FOREIGN KEY (assigned_to) REFERENCES users(id),
  INDEX idx_status (status),
  INDEX idx_priority (priority),
  INDEX idx_assigned_to (assigned_to),
  INDEX idx_created_by (created_by)
);

-- COMMENTS Table
CREATE TABLE comments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  ticket_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  text TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id),
  INDEX idx_ticket_id (ticket_id)
);

-- NOTIFICATIONS Table
CREATE TABLE notifications (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  type VARCHAR(50) NOT NULL,
  message TEXT NOT NULL,
  related_entity_id BIGINT,
  related_entity_type VARCHAR(50),
  is_read BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  INDEX idx_user_read (user_id, is_read),
  INDEX idx_created_at (created_at)
);
```

---

## 4. Technology Stack

### 4.1 Backend Stack
| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| Framework | Spring Boot | 3.2.4 | REST API & Business Logic |
| Language | Java | 17 | Type-safe backend development |
| ORM | Spring Data JPA | 3.2.4 | Database persistence layer |
| Security | Spring Security | 6.2 | Authentication & Authorization |
| OAuth | Spring Security OAuth2 | 6.2 | Third-party login (Google) |
| JWT | jjwt | 0.12.3 | Token-based authentication |
| Logging | SLF4J + Logback | - | Structured logging |
| Testing | JUnit 5 + Mockito | 5.9.2 | Unit & integration tests |

### 4.2 Frontend Stack
| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| Framework | React | 18.3.1 | UI components & state |
| Build Tool | Vite | 5.0.7 | Fast build & dev server |
| Routing | React Router | 6.20.0 | Client-side navigation |
| HTTP Client | Axios | 1.6.2 | API communication |
| State (Server) | React Query | 5.28.0 | Server state management |
| State (Global) | Context API | - | Auth & Toast notifications |
| Forms | React Hook Form | 7.48.0 | Form state & validation |
| Validation | Zod | 3.22.4 | Schema validation |
| CSS | CSS3 + PostCSS | - | Styling & responsive design |
| Testing | Vitest + Testing Library | - | Component testing |
| OAuth | React Google OAuth | 0.12.1 | Google login integration |

### 4.3 Database Stack
| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| Database | MySQL | 8.0 | Relational data storage |
| Connection Pool | HikariCP | - | Efficient DB connections |
| Migration | Flyway | - | Version control for schema |
| Backup | mysqldump | - | Automated backups |

### 4.4 DevOps & Deployment
| Component | Technology | Purpose |
|-----------|-----------|---------|
| Version Control | Git | Source code management |
| Repository | GitHub | Centralized repository |
| CI/CD | GitHub Actions | Automated build & deploy |
| Containerization | Docker | Container images & deployment |
| Orchestration | Docker Compose | Local multi-container setup |
| Hosting | AWS / Azure | Cloud deployment |
| Monitoring | GitHub Actions Logs | CI/CD monitoring |

---

## 5. API Architecture

### 5.1 REST API Design Principles

**Endpoint Structure**: `/api/v1/{resource}/{id}/{sub-resource}`

**HTTP Methods**:
- `GET` - Retrieve resource(s)
- `POST` - Create new resource
- `PUT/PATCH` - Update resource (PUT is full replace, PATCH is partial)
- `DELETE` - Delete resource

**Response Format**:
```json
{
  "status": "success|error",
  "data": { /* actual data or null */ },
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable message"
  },
  "timestamp": "2026-04-27T10:30:00Z"
}
```

**Status Codes**:
- `200 OK` - Successful GET/PUT/PATCH
- `201 Created` - Successful POST
- `204 No Content` - Successful DELETE
- `400 Bad Request` - Invalid input
- `401 Unauthorized` - Missing/invalid authentication
- `403 Forbidden` - Lacks permission
- `404 Not Found` - Resource not found
- `409 Conflict` - Cannot complete due to conflict
- `500 Internal Server Error` - Server error

### 5.2 Authentication Flow

```
┌─────────────┐
│   User      │
└──────┬──────┘
       │ 1. Login & Consent
       ▼
┌──────────────────┐
│  Google OAuth    │
└──────┬───────────┘
       │ 2. Auth Code
       ▼
┌──────────────────┐     ┌─────────────────┐
│  Backend Server  │────▶│ Google OAuth API│
│  (/login)        │     │ (Token Exchange)│
└──────┬───────────┘     └─────────────────┘
       │ 3. JWT Token
       ▼
┌──────────────────┐
│  Frontend Store  │
│  (LocalStorage)  │
└──────────────────┘
       │ 4. Include JWT in requests
       ▼
┌──────────────────────────────────┐
│  Protected API Endpoints         │
│  (Authorization: Bearer <token>) │
└──────────────────────────────────┘
```

### 5.3 Authorization Filters

Endpoints are protected using Spring Security annotations:

```java
@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'TECHNICIAN')")
public ResponseEntity<?> getResources() { }

@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> updateResourceStatus() { }

@PreAuthorize("hasRole('TECHNICIAN')")
public ResponseEntity<?> assignTicket() { }
```

---

## 6. Data Flow Diagrams

### 6.1 Booking Creation Flow

```
┌─────────────────────────────────────────────────────────┐
│ 1. User selects resource & time slot (Frontend)        │
└────────────┬────────────────────────────────────────────┘
             │
             ▼
 ┌──────────────────────────────────────────┐
 │ 2. POST /api/v1/bookings (with JWT)      │
 │    - resourceId, startTime, endTime      │
 └────────┬─────────────────────────────────┘
          │
          ▼
 ┌──────────────────────────────────────────┐
 │ 3. Backend Validation                    │
 │    - User authentication check           │
 │    - Resource exists & available?        │
 │    - Conflict detection (overlaps?)      │
 │    - Duration constraints?               │
 └────────┬─────────────────────────────────┘
          │ Valid
          ▼
 ┌──────────────────────────────────────────┐
 │ 4. Create Booking Record                 │
 │    - status = PENDING (or Auto-approved) │
 │    - Save to database                    │
 └────────┬─────────────────────────────────┘
          │
          ▼
 ┌──────────────────────────────────────────┐
 │ 5. Send Notifications                    │
 │    - Create notification for admin       │
 │    - Send email confirmation             │
 └────────┬─────────────────────────────────┘
          │
          ▼
 ┌──────────────────────────────────────────┐
 │ 6. Return 201 Created with booking       │
 │    - Send booking details to frontend    │
 │    - Frontend updates UI               │
 └──────────────────────────────────────────┘
```

### 6.2 Ticket Assignment Flow

```
User creates Ticket
       ▼
Backend receives POST /api/v1/tickets
       ▼
Validate & save (status = OPEN)
       ▼
Create Notification (type = NEW_TICKET)
       ▼
For HIGH/URGENT: Auto-escalate or email
       ▼
Technician receives notification
       ▼
PATCH /api/v1/tickets/{id}/assign (with technician_id)
       ▼
Update ticket status (assigned_to, IN_PROGRESS)
       ▼
Send notification to assigned technician
       ▼
Technician resolves & marks RESOLVED
       ▼
System sends completion notification to reporter
       ▼
User rates satisfaction (optional)
```

---

## 7. Security Architecture

### 7.1 Authentication Layer

```
┌─────────────────────────────────────┐
│ Request with Authorization Header   │
│ Authorization: Bearer eyJhbGc...    │
└────────────────┬────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────┐
│ JwtAuthenticationFilter              │
│ - Extract token from header         │
│ - Validate signature & expiration   │
│ - Decode JWT claims                 │
└────────────────┬────────────────────┘
                 │ Valid
                 ▼
┌─────────────────────────────────────┐
│ SecurityContextHolder               │
│ - Populate with authenticated user  │
│ - Store user principal              │
└────────────────┬────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────┐
│ Request proceeds to controller      │
│ - User principal available via      │
│   getCurrentUser() or annotations   │
└─────────────────────────────────────┘
```

### 7.2 CORS Configuration

Frontend and Backend communicate with CORS enabled for:
- `Origin: http://localhost:3000` (Development)
- `Origin: https://smartcampus.example.com` (Production)
- `Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS`
- `Headers: Authorization, Content-Type`

---

## 8. Error Handling Strategy

```
Try-Catch Hierarchy:

Global @ControllerAdvice Handler
  │
  ├─ ValidationException → 400 Bad Request
  ├─ AuthenticationException → 401 Unauthorized
  ├─ AccessDeniedException → 403 Forbidden
  ├─ ResourceNotFoundException → 404 Not Found
  ├─ ConflictException → 409 Conflict
  ├─ EntityExistsException → 409 Conflict
  ├─ DataIntegrityViolationException → 400/409
  └─ General Exception → 500 Internal Server Error
```

---

## 9. Performance Optimization

### 9.1 Caching Strategy
- **Page Caching**: Resource lists cached for 5 minutes
- **Query Optimization**: N+1 problem solved via eager loading
- **Connection Pooling**: HikariCP with 10 min connections, 5 max idle

### 9.2 Indexing Strategy
- Primary keys on all tables
- Foreign keys indexed for joins
- Unique constraints on email, booking conflicts
- Composite indexes on frequent WHERE clauses

### 9.3 Frontend Performance
- Code splitting by route
- Lazy loading of components
- Image optimization (WebP, responsive sizes)
- CSS minification & bundling
- JavaScript minification & tree-shaking

---

## 10. Deployment Architecture

### 10.1 Docker Compose (Local)
```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: smart_campus_db
    volumes:
      - mysql_data:/var/lib/mysql
    ports:
      - "3306:3306"

  backend:
    build: ./smart-campus
    ports:
      - "8081:8081"
    depends_on:
      - mysql
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/smart_campus_db

  frontend:
    build: ./smart-campus-frontend
    ports:
      - "3000:3000"
    depends_on:
      - backend
```

### 10.2 Cloud Deployment (AWS/Azure)
- Backend: Spring Boot JAR on EC2/App Service
- Frontend: React build on S3 + CloudFront / Blob Storage + CDN
- Database: RDS MySQL / Azure Database for MySQL
- CI/CD: GitHub Actions → Docker → ECR → ECS

---

## 11. Monitoring & Logging

### 11.1 Logging Framework
```java
@Slf4j
public class BookingService {
  public void createBooking(BookingRequest req) {
    log.info("Creating booking for resource: {}", req.getResourceId());
    try {
      // logic
      log.info("Booking created successfully: {}", bookingId);
    } catch (Exception e) {
      log.error("Error creating booking", e);
    }
  }
}
```

### 11.2 Audit Trail
- All permission-critical actions logged to audit table
- User, timestamp, action, resource, result stored
- Retention: 1 year

---

## 12. Scalability Considerations

1. **Stateless API**: All state in database or JWT tokens
2. **Load Balancing**: Multiple API instances behind load balancer
3. **Horizontal Scaling**: Docker containers with auto-scaling policies
4. **Database Replication**: Master-slave MySQL for read scaling
5. **Caching Layer**: Redis for session & query caching
6. **Queue System**: For async operations (emails, notifications)

---

**Document Version**: 1.0  
**Last Updated**: April 27, 2026
