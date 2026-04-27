# Smart Campus Operations Hub - Requirements Document

**Project**: IT3030 PAF Assignment 2026  
**Version**: 1.0  
**Date**: April 27, 2026  
**Status**: Complete

---

## 1. Executive Summary

The Smart Campus Operations Hub is a comprehensive web-based system designed for efficient management of campus resources, bookings, maintenance tickets, and user notifications. The system integrates modern authentication mechanisms, role-based access control, and real-time operational capabilities to enhance campus operations.

---

## 2. Functional Requirements

### 2.1 Module A: Resource & Facilities Management

**Requirement**: Campus administrators and users must be able to search, view, filter, and manage campus resources (facilities, equipment, spaces).

**Specifications**:
- **FR-A1**: Display comprehensive catalog of all campus resources with details (name, type, capacity, location, status, image)
- **FR-A2**: Filter resources by type (Conference Room, Auditorium, Lab, Sports Field, Parking Lot, etc.)
- **FR-A3**: Search resources by name or location with autocomplete
- **FR-A4**: Display real-time availability status (AVAILABLE, OCCUPIED, MAINTENANCE)
- **FR-A5**: Show resource details including capacity, amenities, contact information, and booking history
- **FR-A6**: Admin capability to create, update, and delete resources
- **FR-A7**: Admin capability to update resource status (AVAILABLE, OCCUPIED, MAINTENANCE, BLOCKED)
- **FR-A8**: Bulk import/export resource data functionality
- **FR-A9**: Resource categorization and tagging for efficient filtering
- **FR-A10**: Availability calendar showing bookings and maintenance windows

**Acceptance Criteria**:
- Users can view complete resource list with filters applied within 2 seconds
- Customers can filter by at least 5 resource categories
- Resource images load properly and are responsive
- Availability data updates in real-time (within 5 seconds of booking change)

---

### 2.2 Module B: Booking Management

**Requirement**: Users must be able to book campus resources with conflict prevention, approval workflows, and modification capabilities.

**Specifications**:
- **FR-B1**: Users can create new booking requests for available resources
- **FR-B2**: System prevents double-booking (conflict detection before confirmation)
- **FR-B3**: Booking workflow states: PENDING → APPROVED/REJECTED → CONFIRMED/CANCELLED → COMPLETED
- **FR-B4**: Admin approval required for certain resource types
- **FR-B5**: Booking duration constraints (minimum 30 min, maximum 8 hours per booking)
- **FR-B6**: Automatic cancellation of pending approvals after 7 days without action
- **FR-B7**: Users can view their booking history (past and upcoming)
- **FR-B8**: Users can modify or cancel their own bookings (with constraints)
- **FR-B9**: Admins can override, modify, or cancel any booking
- **FR-B10**: Booking confirmation via email/SMS notifications
- **FR-B11**: Calendar view of all bookings for resource administrators
- **FR-B12**: Recurring booking support (weekly, monthly, semester-long)
- **FR-B13**: Booking cancellation with 24-hour notice policy
- **FR-B14**: Overbooking prevention through real-time availability checking

**Acceptance Criteria**:
- Conflict detection prevents 100% of double-booking attempts
- Booking confirmation received within 1 second of submission
- Users can view their bookings with filters (upcoming, past, cancelled)
- Admins receive email notification for approval-required bookings
- Workflow transitions are properly enforced (no invalid state changes)

---

### 2.3 Module C: Maintenance & Incident Ticketing

**Requirement**: Users must be able to report maintenance issues and facility problems; technicians must manage resolution workflow.

**Specifications**:
- **FR-C1**: Users can create new maintenance tickets with issue description and category
- **FR-C2**: Ticket categories: Plumbing, Electrical, Structural, Furniture, Equipment, Cleanliness, Safety, Other
- **FR-C3**: Priority levels: Low, Medium, High, Urgent
- **FR-C4**: Ticket workflow: OPEN → IN_PROGRESS → RESOLVED → CLOSED/REOPENED
- **FR-C5**: Users can attach images/documents to tickets (up to 5MB per file)
- **FR-C6**: Automatic ticket numbering and tracking ID generation
- **FR-C7**: Escalation mechanism for overdue tickets
- **FR-C8**: SLA tracking (target resolution times by priority)
- **FR-C9**: Technician assignment capability
- **FR-C10**: Comments/updates on tickets with timestamp and author tracking
- **FR-C11**: Ticket history and audit trail
- **FR-C12**: Assignment to specific technician with notifications
- **FR-C13**: Ticket analytics dashboard showing metrics and trends
- **FR-C14**: Email notifications on status changes
- **FR-C15**: User can rate satisfaction after ticket closure (1-5 stars)

**Acceptance Criteria**:
- Tickets created and trackable within 1 second
- Attachment upload works for images and PDFs
- Technician can update status and add comments
- Workflow prevents invalid state transitions
- Email notifications sent correctly to involved parties
- Ticket search returns results within 2 seconds

---

### 2.4 Module D: Notifications System

**Requirement**: Users must receive real-time notifications for bookings, tickets, and system events.

**Specifications**:
- **FR-D1**: Email notifications for booking confirmations/rejections
- **FR-D2**: Email notifications for ticket assignment
- **FR-D3**: Email notifications when ticket status changes
- **FR-D4**: Email notifications for upcoming bookings (24-hour reminder)
- **FR-D5**: In-app notification bell with unread count
- **FR-D6**: Notification history accessible to users (stored for 30 days)
- **FR-D7**: Notification preferences (email frequency, notification types to receive)
- **FR-D8**: System broadcasts for campus-wide announcements
- **FR-D9**: Real-time push notifications (browser-based WebSocket)
- **FR-D10**: Notification pagination and filtering

**Acceptance Criteria**:
- Email sent within 5 seconds of event trigger
- Notifications visible in in-app bell within 2 seconds
- Users can mark notifications as read/unread
- Notification settings are personalized and persistent
- Email format is professional and includes all relevant links

---

### 2.5 Module E: Authentication & Authorization

**Requirement**: System must securely authenticate users and enforce role-based access control.

**Specifications**:
- **FR-E1**: OAuth 2.0 authentication with Google (OIDC compliant)
- **FR-E2**: JWT token-based session management
- **FR-E3**: Token expiration and refresh mechanism (24-hour expiry)
- **FR-E4**: Role-based access control (RBAC) with three roles:
  - **USER**: Standard campus member (view resources, create bookings, report tickets)
  - **ADMIN**: Resource and system administrator (manage resources, approve bookings, assign technicians)
  - **TECHNICIAN**: Maintenance staff (manage tickets, provide updates)
- **FR-E5**: Activity logging and audit trails
- **FR-E6**: Logout functionality that invalidates tokens
- **FR-E7**: Session timeout after 30 minutes of inactivity
- **FR-E8**: Secure password handling (OAuth only, no password storage)
- **FR-E9**: CORS configuration for frontend-backend communication
- **FR-E10**: Spring Security integration with method-level security annotations

**Acceptance Criteria**:
- OAuth login completes in < 3 seconds
- Token validation succeeds for valid users
- Invalid/expired tokens return 401 Unauthorized
- User roles properly restrict endpoint access
- Logout clears authentication state
- Audit logs record all permission-critical actions

---

## 3. Non-Functional Requirements

### 3.1 Performance
- **NFR-P1**: API endpoints respond within 500ms for database queries
- **NFR-P2**: Frontend pages load and render within 2 seconds
- **NFR-P3**: Search operations complete within 1 second for up to 1000 resources
- **NFR-P4**: Support concurrent users: minimum 100 simultaneous sessions
- **NFR-P5**: Database query optimization using indexes on frequently accessed columns

### 3.2 Security
- **NFR-S1**: HTTPS/TLS for all client-server communication
- **NFR-S2**: SQL injection prevention through parameterized queries (JPA)
- **NFR-S3**: Cross-site scripting (XSS) prevention through proper output encoding
- **NFR-S4**: Cross-site request forgery (CSRF) protection
- **NFR-S5**: Rate limiting on authentication endpoints (max 5 failed attempts per 15 minutes)
- **NFR-S6**: Sensitive data encryption (passwords, personal information)
- **NFR-S7**: Secure file upload handling with virus scanning
- **NFR-S8**: Regular security audits and code reviews

### 3.3 Scalability
- **NFR-SC1**: Horizontal scalability through stateless API design
- **NFR-SC2**: Database connection pooling
- **NFR-SC3**: Caching strategy for frequently accessed data (Redis)
- **NFR-SC4**: Application deployable in containerized environment (Docker)

### 3.4 Availability & Reliability
- **NFR-A1**: System availability target: 99.5% uptime
- **NFR-A2**: Backup strategy: daily automated backups retained for 30 days
- **NFR-A3**: Disaster recovery plan with RTO ≤ 4 hours, RPO ≤ 1 hour
- **NFR-A4**: Graceful error handling with user-friendly error messages
- **NFR-A5**: Health check endpoints for monitoring

### 3.5 Maintainability
- **NFR-M1**: Code follows MVC architectural pattern
- **NFR-M2**: Comprehensive API documentation (OpenAPI/Swagger)
- **NFR-M3**: Code comments for complex business logic
- **NFR-M4**: Unit test coverage target: 70%+
- **NFR-M5**: Standardized logging framework

### 3.6 Usability
- **NFR-U1**: Responsive UI supporting desktop, tablet, and mobile devices
- **NFR-U2**: Accessibility compliance (WCAG 2.0 AA standards)
- **NFR-U3**: Intuitive navigation and user workflows
- **NFR-U4**: Consistent UI design language and component library

---

## 4. Business Rules

### 4.1 Booking Rules
1. A user cannot book overlapping time slots for the same resource
2. Unconfirmed bookings automatically expire after 24 hours
3. Cancellations within 24 hours require admin approval
4. Peak hours (9 AM - 5 PM) may require advance booking (48 hours)

### 4.2 Ticket Rules
1. Urgent tickets must be assigned within 4 hours of creation
2. High priority tickets must be resolved within 48 hours
3. All tickets must be responded to within 24 hours
4. Technicians cannot mark tickets resolved without documentation

### 4.3 User Rules
1. Users can have maximum 5 simultaneous active bookings
2. Users with overdue tickets cannot create new bookings (configurable)
3. Repeated cancellations may flag user for review (>3 in a week)

---

## 5. System Constraints

- **Technology Stack**: Spring Boot 3.x (Java 17), React 18+, MySQL 8.0, Docker
- **Deployment Platforms**: AWS, Azure, On-premises Linux servers
- **Development Timeline**: 4 weeks (Phase 1: Core features, Phase 2: Enhancement)
- **Budget**: $15,000 (approximate for infrastructure setup)
- **Compliance**: GDPR for EU users, local privacy regulations

---

## 6. Acceptance Criteria Summary

| Module | Functional | Non-Functional | Tests Required |
|--------|-----------|-----------------|-----------------|
| A. Resources | 10 features | All | 15 test cases |
| B. Bookings | 14 features | All | 20 test cases |
| C. Tickets | 15 features | All | 18 test cases |
| D. Notifications | 10 features | P, S | 12 test cases |
| E. Authentication | 10 features | S, A | 10 test cases |

---

## 7. Dependencies & Assumptions

**Assumptions**:
- Users have valid Google accounts for OAuth
- Database server is available and performant
- Email service provider (SMTP) is configured
- Frontend and backend deployed on same network or with CORS enabled

**Dependencies**:
- Spring Security modules
- Google OAuth 2.0 provider
- MySQL database service
- Email service (SendGrid or equivalent)
- Frontend web browser with modern ECMAScript support

---

## 8. Version History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | Apr 27, 2026 | Development Team | Initial requirements document |

---

**Document Status**: APPROVED  
**Sign-off**: Project Lead & Academic Supervisor
