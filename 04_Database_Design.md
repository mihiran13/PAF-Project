# Smart Campus Operations Hub — Database & Entity Design

---

## 1. Database Technology Decision

- **Database:** MySQL
- **ORM:** Spring Data JPA with Hibernate

**Why a relational database is suitable for this system:**
1. **Structured Data:** Resources, bookings, and tickets have clear, well-defined schemas that map perfectly to relational tables.
2. **Relationships:** The system heavily relies on relationships (e.g., a Booking belongs to a User and a Resource; a Ticket has Comments and Attachments). Relational databases excel at enforcing foreign key constraints for data integrity.
3. **Workflow Integrity:** Booking conflict detection and ticket state transitions require ACID properties to prevent race conditions (e.g., two users booking the same room at the exact same millisecond).
4. **Auditability:** Relational tables make it easy to implement robust audit trails (who created what and when) using consistent foreign keys and timestamp columns.

---

## 2. Entity Overview

| # | Entity Name | Purpose | Owner (Member) |
|---|-------------|---------|----------------|
| 1 | `User` | Stores authenticated users and roles | Member 3 |
| 2 | `Resource` | Bookable facilities and assets | Member 1 |
| 3 | `ResourceAvailabilityWindow` | Availability time slots per resource | Member 1 |
| 4 | `Booking` | Booking requests, status, and workflow history | Member 2 |
| 5 | `Ticket` | Maintenance/incident reports and workflow | Member 3 |
| 6 | `TicketAttachment` | Image attachments for tickets | Member 3 |
| 7 | `TicketComment` | Comments on tickets with ownership tracking | Member 3 |
| 8 | `Notification` | User notifications for system events | Member 3 |

---

## 3. Detailed Entity Definitions

### 3.1 User Entity

**Table name:** `users`

**Purpose:** Stores all authenticated users who log in via OAuth 2.0.

| Field Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| `id` | Long (BIGINT) | PK, Auto-increment | Unique identifier |
| `oauth_id` | String (VARCHAR 255) | NOT NULL, UNIQUE | Google OAuth subject ID |
| `email` | String (VARCHAR 255) | NOT NULL, UNIQUE | User email from Google |
| `name` | String (VARCHAR 255) | NOT NULL | Display name from Google |
| `profile_image_url` | String (VARCHAR 500) | NULLABLE | Profile picture URL from Google |
| `role` | Enum (VARCHAR 20) | NOT NULL, DEFAULT 'USER' | USER, ADMIN, TECHNICIAN |
| `is_active` | Boolean | NOT NULL, DEFAULT true | Whether account is active |
| `created_at` | LocalDateTime | NOT NULL, Auto-set | When user first registered |
| `updated_at` | LocalDateTime | NOT NULL, Auto-update | Last profile update timestamp |

**Indexes:**
- UNIQUE index on `oauth_id`
- UNIQUE index on `email`
- Index on `role` (for filtering by role in admin views)

**Notes:**
- The User is created automatically on the first OAuth login.
- Default role is `USER`.
- Admins can change a user's role to `ADMIN` or `TECHNICIAN`.

---

### 3.2 Resource Entity

**Table name:** `resources`

**Purpose:** Stores all bookable campus facilities and assets.

| Field Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| `id` | Long (BIGINT) | PK, Auto-increment | Unique identifier |
| `name` | String (VARCHAR 255) | NOT NULL | Resource name (e.g., "Lecture Hall A") |
| `type` | Enum (VARCHAR 50) | NOT NULL | LECTURE_HALL, LAB, MEETING_ROOM, EQUIPMENT |
| `description` | String (TEXT) | NULLABLE | Detailed description |
| `capacity` | Integer | NULLABLE | Maximum capacity (for rooms) |
| `location` | String (VARCHAR 255) | NOT NULL | Building/floor/room or storage info |
| `status` | Enum (VARCHAR 20) | NOT NULL, DEFAULT 'ACTIVE' | ACTIVE, OUT_OF_SERVICE |
| `image_url` | String (VARCHAR 500) | NULLABLE | Resource image URL |
| `created_by` | Long (BIGINT) | FK → users.id, NOT NULL | Admin who created this resource |
| `created_at` | LocalDateTime | NOT NULL, Auto-set | Creation timestamp |
| `updated_at` | LocalDateTime | NOT NULL, Auto-update | Last update timestamp |

**Indexes:**
- Index on `type`
- Index on `status`
- Index on `location`
- Composite index on `type, status` (for filtered searches)

**Enum values for type (`ResourceType`):**
- `LECTURE_HALL`, `LAB`, `MEETING_ROOM`, `PROJECTOR`, `CAMERA`, `OTHER_EQUIPMENT`

**Enum values for status (`ResourceStatus`):**
- `ACTIVE`, `OUT_OF_SERVICE`

---

### 3.3 ResourceAvailabilityWindow Entity

**Table name:** `resource_availability_windows`

**Purpose:** Defines when a resource is available for booking (day of the week + time range).

| Field Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| `id` | Long (BIGINT) | PK, Auto-increment | Unique identifier |
| `resource_id` | Long (BIGINT) | FK → resources.id, NOT NULL | Which resource this window applies to |
| `day_of_week` | Enum (VARCHAR 15) | NOT NULL | MONDAY, TUESDAY, etc. |
| `start_time` | LocalTime | NOT NULL | Available from |
| `end_time` | LocalTime | NOT NULL | Available until |

**Indexes:**
- Index on `resource_id`
- Composite index on `resource_id, day_of_week`

**Notes:**
- A resource can have multiple availability windows (e.g., Monday morning and Wednesday afternoon).
- A booking must fall entirely within one of these defined windows.
- If no windows are defined, the backend can treat the resource as available all day (or completely unavailable, based on business logic choice).

---

### 3.4 Booking Entity

**Table name:** `bookings`

**Purpose:** Stores booking requests, their current status, and workflow audit history.

| Field Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| `id` | Long (BIGINT) | PK, Auto-increment | Unique identifier |
| `resource_id` | Long (BIGINT) | FK → resources.id, NOT NULL | Which resource is booked |
| `user_id` | Long (BIGINT) | FK → users.id, NOT NULL | Who requested the booking |
| `booking_date` | LocalDate | NOT NULL | Date of the booking |
| `start_time` | LocalTime | NOT NULL | Start time |
| `end_time` | LocalTime | NOT NULL | End time |
| `purpose` | String (TEXT) | NOT NULL | Purpose of the booking |
| `expected_attendees` | Integer | NULLABLE | Number of expected attendees |
| `status` | Enum (VARCHAR 20) | NOT NULL, DEFAULT 'PENDING' | PENDING, APPROVED, REJECTED, CANCELLED |
| `rejection_reason` | String (TEXT) | NULLABLE | Reason if rejected by admin |
| `cancellation_reason` | String (TEXT) | NULLABLE | Reason if cancelled |
| `reviewed_by` | Long (BIGINT) | FK → users.id, NULLABLE | Admin who approved/rejected |
| `reviewed_at` | LocalDateTime | NULLABLE | When the review occurred |
| `cancelled_by` | Long (BIGINT) | FK → users.id, NULLABLE | Who cancelled (user or admin) |
| `cancelled_at` | LocalDateTime | NULLABLE | When the cancellation occurred |
| `created_at` | LocalDateTime | NOT NULL, Auto-set | When booking was requested |
| `updated_at` | LocalDateTime | NOT NULL, Auto-update | Last update timestamp |

**Indexes:**
- Index on `resource_id`
- Index on `user_id`
- Index on `status`
- Index on `booking_date`
- Composite index on `resource_id, booking_date, status` (critical for fast conflict checking)

**Enum values for status (`BookingStatus`):**
- `PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`

**Critical Constraint:**
- No two bookings for the same resource with overlapping time ranges on the same date where both have status `PENDING` or `APPROVED`.

---

### 3.5 Ticket Entity

**Table name:** `tickets`

**Purpose:** Stores maintenance/incident reports and their resolution workflow.

| Field Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| `id` | Long (BIGINT) | PK, Auto-increment | Unique identifier |
| `reporter_id` | Long (BIGINT) | FK → users.id, NOT NULL | Who reported the issue |
| `resource_id` | Long (BIGINT) | FK → resources.id, NULLABLE | Related resource (if applicable) |
| `location_description` | String (VARCHAR 500) | NULLABLE | Location if not tied to a specific resource |
| `category` | Enum (VARCHAR 50) | NOT NULL | ELECTRICAL, PLUMBING, IT_EQUIPMENT, etc. |
| `title` | String (VARCHAR 255) | NOT NULL | Short title/summary |
| `description` | String (TEXT) | NOT NULL | Detailed description of the issue |
| `priority` | Enum (VARCHAR 20) | NOT NULL | LOW, MEDIUM, HIGH, CRITICAL |
| `preferred_contact` | String (VARCHAR 255) | NOT NULL | How to contact the reporter |
| `status` | Enum (VARCHAR 20) | NOT NULL, DEFAULT 'OPEN' | OPEN, IN_PROGRESS, RESOLVED, CLOSED, REJECTED |
| `assigned_technician_id`| Long (BIGINT) | FK → users.id, NULLABLE | Assigned technician |
| `assigned_at` | LocalDateTime | NULLABLE | When technician was assigned |
| `assigned_by` | Long (BIGINT) | FK → users.id, NULLABLE | Admin who assigned the ticket |
| `resolution_notes` | String (TEXT) | NULLABLE | How the issue was resolved |
| `resolved_at` | LocalDateTime | NULLABLE | When the ticket was resolved |
| `rejection_reason` | String (TEXT) | NULLABLE | Reason if rejected |
| `rejected_by` | Long (BIGINT) | FK → users.id, NULLABLE | Admin who rejected the ticket |
| `closed_at` | LocalDateTime | NULLABLE | When the ticket was finally closed |
| `created_at` | LocalDateTime | NOT NULL, Auto-set | When ticket was created |
| `updated_at` | LocalDateTime | NOT NULL, Auto-update | Last update timestamp |

**Indexes:**
- Index on `reporter_id`
- Index on `assigned_technician_id`
- Index on `status`
- Index on `priority`
- Index on `category`
- Composite index on `status, priority` (useful for admin dashboards)

**Enum values for category (`TicketCategory`):**
- `ELECTRICAL`, `PLUMBING`, `IT_EQUIPMENT`, `FURNITURE`, `HVAC`, `CLEANING`, `SAFETY`, `OTHER`

**Enum values for priority (`TicketPriority`):**
- `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`

**Enum values for status (`TicketStatus`):**
- `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `REJECTED`

---

### 3.6 TicketAttachment Entity

**Table name:** `ticket_attachments`

**Purpose:** Stores metadata for image attachments added to tickets as evidence.

| Field Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| `id` | Long (BIGINT) | PK, Auto-increment | Unique identifier |
| `ticket_id` | Long (BIGINT) | FK → tickets.id, NOT NULL | Which ticket this belongs to |
| `original_filename` | String (VARCHAR 255) | NOT NULL | Original uploaded filename |
| `stored_filename` | String (VARCHAR 255) | NOT NULL, UNIQUE | Generated safe filename (e.g., UUID.jpg) |
| `file_path` | String (VARCHAR 500) | NOT NULL | Storage path on disk/cloud |
| `content_type` | String (VARCHAR 100) | NOT NULL | MIME type (e.g., image/jpeg) |
| `file_size` | Long | NOT NULL | File size in bytes |
| `uploaded_by` | Long (BIGINT) | FK → users.id, NOT NULL | Who uploaded the file |
| `created_at` | LocalDateTime | NOT NULL, Auto-set | Upload timestamp |

**Indexes:**
- Index on `ticket_id`

**Constraints (Enforced in Service Layer):**
- Maximum 3 attachments per ticket.
- Only image MIME types allowed.
- Max file size (e.g., 5MB).

---

### 3.7 TicketComment Entity

**Table name:** `ticket_comments`

**Purpose:** Stores comments on tickets, with ownership tracking for edit/delete rules.

| Field Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| `id` | Long (BIGINT) | PK, Auto-increment | Unique identifier |
| `ticket_id` | Long (BIGINT) | FK → tickets.id, NOT NULL | Which ticket this belongs to |
| `author_id` | Long (BIGINT) | FK → users.id, NOT NULL | Who wrote the comment |
| `content` | String (TEXT) | NOT NULL | The text content of the comment |
| `is_edited` | Boolean | NOT NULL, DEFAULT false | Flag to show if comment was modified |
| `created_at` | LocalDateTime | NOT NULL, Auto-set | When comment was posted |
| `updated_at` | LocalDateTime | NOT NULL, Auto-update | Last edit timestamp |

**Indexes:**
- Index on `ticket_id`
- Index on `author_id`
- Composite index on `ticket_id, created_at` (to retrieve comments in chronological order)

---

### 3.8 Notification Entity

**Table name:** `notifications`

**Purpose:** Stores system-generated notifications for users.

| Field Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| `id` | Long (BIGINT) | PK, Auto-increment | Unique identifier |
| `user_id` | Long (BIGINT) | FK → users.id, NOT NULL | Who receives this notification |
| `type` | Enum (VARCHAR 50) | NOT NULL | Event type trigger |
| `title` | String (VARCHAR 255) | NOT NULL | Short notification title |
| `message` | String (TEXT) | NOT NULL | Detailed notification message |
| `reference_type`| Enum (VARCHAR 20) | NULLABLE | BOOKING or TICKET |
| `reference_id` | Long | NULLABLE | ID of the related booking or ticket |
| `is_read` | Boolean | NOT NULL, DEFAULT false | Whether user has read it |
| `created_at` | LocalDateTime | NOT NULL, Auto-set | When notification was created |

**Indexes:**
- Index on `user_id`
- Composite index on `user_id, is_read` (for calculating unread badge count)
- Composite index on `user_id, created_at` (for ordered retrieval)

**Enum values for type (`NotificationType`):**
- `BOOKING_APPROVED`, `BOOKING_REJECTED`, `BOOKING_CANCELLED`
- `TICKET_ASSIGNED`, `TICKET_IN_PROGRESS`, `TICKET_RESOLVED`, `TICKET_CLOSED`, `TICKET_REJECTED`
- `NEW_COMMENT`

**Enum values for reference_type (`ReferenceType`):**
- `BOOKING`, `TICKET`

---

## 4. Entity Relationship Diagram Description

### One-to-Many Relationships

| Parent Entity | Child Entity | Relationship Description | Foreign Key in Child |
|---------------|--------------|--------------------------|---------------------|
| `User` | `Booking` | A user can request many bookings. | `bookings.user_id` |
| `User` | `Ticket` | A user can report many tickets. | `tickets.reporter_id` |
| `User` | `TicketComment`| A user can write many comments. | `ticket_comments.author_id` |
| `User` | `Notification` | A user can receive many notifications. | `notifications.user_id` |
| `Resource` | `Booking` | A resource can have many bookings. | `bookings.resource_id` |
| `Resource` | `ResourceAvailabilityWindow` | A resource has multiple availability slots. | `resource_availability_windows.resource_id` |
| `Resource` | `Ticket` | A resource can have many maintenance tickets. | `tickets.resource_id` |
| `Ticket` | `TicketAttachment` | A ticket can have up to 3 attachments. | `ticket_attachments.ticket_id` |
| `Ticket` | `TicketComment`| A ticket can have many comments. | `ticket_comments.ticket_id` |

### Many-to-One References (Audit & Workflow Logic)

These are crucial for the "strong auditability" requirement:

| Entity | References | Purpose |
|--------|------------|---------|
| `Booking` | `User` (reviewed_by) | Tracks which ADMIN approved or rejected the booking. |
| `Booking` | `User` (cancelled_by) | Tracks who cancelled the booking (the owner or an ADMIN). |
| `Ticket` | `User` (assigned_technician_id) | The TECHNICIAN assigned to handle the ticket. |
| `Ticket` | `User` (assigned_by) | The ADMIN who assigned the technician. |
| `Ticket` | `User` (rejected_by) | The ADMIN who rejected the ticket. |
| `Resource` | `User` (created_by) | The ADMIN who registered the resource in the catalogue. |
| `TicketAttachment`| `User` (uploaded_by) | Tracks who uploaded the file. |

---

## 5. Enum Definitions Summary

*For use in Java code as `enum` types.*

- **UserRole:** `USER`, `ADMIN`, `TECHNICIAN`
- **ResourceType:** `LECTURE_HALL`, `LAB`, `MEETING_ROOM`, `PROJECTOR`, `CAMERA`, `OTHER_EQUIPMENT`
- **ResourceStatus:** `ACTIVE`, `OUT_OF_SERVICE`
- **BookingStatus:** `PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`
- **TicketCategory:** `ELECTRICAL`, `PLUMBING`, `IT_EQUIPMENT`, `FURNITURE`, `HVAC`, `CLEANING`, `SAFETY`, `OTHER`
- **TicketPriority:** `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`
- **TicketStatus:** `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `REJECTED`
- **NotificationType:** `BOOKING_APPROVED`, `BOOKING_REJECTED`, `BOOKING_CANCELLED`, `TICKET_ASSIGNED`, `TICKET_IN_PROGRESS`, `TICKET_RESOLVED`, `TICKET_CLOSED`, `TICKET_REJECTED`, `NEW_COMMENT`
- **ReferenceType:** `BOOKING`, `TICKET`
- **DayOfWeek:** `MONDAY`, `TUESDAY`, `WEDNESDAY`, `THURSDAY`, `FRIDAY`, `SATURDAY`, `SUNDAY`

---

## 6. Audit Fields Strategy

To meet the assignment's explicit requirement for "strong auditability":
1. Every entity includes `created_at` (auto-set) and `updated_at` (auto-updated).
2. Key entities track **WHO** performed critical actions using explicit foreign keys (e.g., `reviewed_by`, `assigned_by`, `resolved_at`, `closed_at`).

**Implementation Recommendation:**
Create a JPA `@MappedSuperclass` named `BaseEntity` to hold the ID and timestamp fields to reduce boilerplate:
```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Getters / Setters
}
```

---

## 7. Database Constraints Summary

| Constraint Type | Entity | Rule Description |
|-----------------|--------|------------------|
| **Unique** | `User` | Enforce unique `email` and unique `oauth_id`. |
| **Unique** | `TicketAttachment` | Enforce unique `stored_filename` to prevent file overwrites. |
| **Integrity** | *All Entities* | Maintain foreign key integrity (e.g., ticket cannot reference non-existent resource). |
| **Business Logic** | `Booking` | Prevent overlapping `APPROVED` or `PENDING` bookings for same resource (enforced in Service Layer DB query). |
| **Business Logic** | `Resource` | Bookings can only be created for `ACTIVE` resources (enforced in Service Layer). |
| **Validation** | `Booking`, `ResourceAvailabilityWindow` | `start_time` must be strictly before `end_time` (enforced via Java validation/Service layer). |
| **Validation** | `TicketAttachment` | Enforce maximum 3 image attachments per ticket and file type restrictions. (Service Layer). |

---

## 8. Cascade and Delete Strategy

| Scenario (Parent Deleted/Modified) | Child Action / Strategy | Best Practice Used |
|------------------------------------|-------------------------|--------------------|
| Resource deactivated (`status` = OUT_OF_SERVICE) | Existing Bookings | **Keep.** Do not allow new bookings. Treat soft-delete as the standard. Admin handles future APPROVED bookings manually. |
| User deactivated (`is_active` = false) | User's Bookings | **Keep.** Maintain historical audit trail. |
| User deactivated | User's Tickets / Comments | **Keep.** Maintain historical audit trail. |
| Ticket genuinely deleted (Admin action) | `TicketAttachment` | **CASCADE DELETE.** Remove DB record AND the actual file from disk. |
| Ticket genuinely deleted | `TicketComment` | **CASCADE DELETE.** |

**Recommendation:** Rely entirely on **Soft Deletes** (status changes like `OUT_OF_SERVICE`, `is_active = false`, `CANCELLED`) for core entities (`Resource`, `User`, `Booking`) to preserve the audit trail. Hard deletes should be reserved only for cascade-deleting child elements like comments or attachments if the parent ticket is purged by an admin.

---

## 9. Sample Data for Testing

Suggesting seed data for initial setup and UI testing:

### Sample Users
| Name | Email | Role |
|------|-------|------|
| Alice Ops | alice.admin@campus.edu | `ADMIN` |
| Bob Tech | bob.tech@campus.edu | `TECHNICIAN` |
| Charlie Student | charlie.s@campus.edu | `USER` |

### Sample Resources
| Name | Type | Capacity | Location | Status |
|------|------|----------|----------|--------|
| Main Hall | `LECTURE_HALL` | 300 | North Wing | `ACTIVE` |
| Physics Lab | `LAB` | 30 | Science Block, Rm 101 | `ACTIVE` |
| Sony Projector | `PROJECTOR`| null | AV Store | `OUT_OF_SERVICE` |

---

## 10. Spring Boot JPA Mapping Notes

*Guidelines for the backend developers mapping these entities:*
- **Entity Creation:** Use `@Entity` and `@Table(name = "plural_name")`.
- **Enums:** Always map enums using `@Enumerated(EnumType.STRING)` so the text value is stored, not the ordinal integer.
- **Relationships:**
    - Use `@ManyToOne(fetch = FetchType.LAZY)` for foreign keys (e.g., `user` in `Booking`). Lazy loading improves performance.
    - Use `@OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)` for strong parent-child relationships like a `Ticket` and its `TicketAttachment`s.
- **Dates/Times:** Use modern Java classes (`LocalDateTime`, `LocalDate`, `LocalTime`). Do not use `java.util.Date`.
- **Text:** Use `@Column(columnDefinition = "TEXT")` for long string fields like descriptions, notes, and reasons.
- **Audit Logging:** Enable strict auditing using `@EnableJpaAuditing` in the main app configuration class.

---
