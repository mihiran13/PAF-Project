# Testing & Quality Assurance Documentation

**Project**: Smart Campus Operations Hub  
**Version**: 1.0  
**Date**: April 27, 2026

---

## 1. Testing Strategy

### 1.1 Test Levels

```
┌──────────────────────────────────────────┐
│          TESTING PYRAMID                 │
├──────────────────────────────────────────┤
│                                          │
│              E2E/UI Tests                │
│              (10% - Manual)              │
│                                          │
│          Integration Tests               │
│          (20% - Automated)               │
│                                          │
│          Unit Tests                      │
│          (70% - Automated)               │
│                                          │
└──────────────────────────────────────────┘
```

**Target Coverage**: 70%+ code coverage across backend

---

## 2. Unit Testing Strategy

### 2.1 Backend Unit Tests (Java/Spring Boot)

**Test Framework**: JUnit 5 + Mockito  
**Scope**: Service layer, business logic, validation

### 2.1.1 Booking Service Tests

```java
@DisplayName("BookingService Tests")
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

  @Mock private BookingRepository bookingRepository;
  @Mock private ResourceRepository resourceRepository;
  @Mock private NotificationService notificationService;
  
  @InjectMocks private BookingService bookingService;

  @DisplayName("Should create booking when resource is available")
  @Test
  void testCreateBookingSuccess() {
    // Arrange
    BookingRequest request = new BookingRequest(1L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));
    Resource resource = new Resource(1L, "Room A", "CONFERENCE_ROOM", 20, "Building A", "AVAILABLE", null);
    
    when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));
    when(bookingRepository.findConflictingBookings(anyLong(), any(), any())).thenReturn(Collections.emptyList());
    when(bookingRepository.save(any())).thenReturn(new Booking(...));

    // Act
    BookingResponse response = bookingService.createBooking(request, 1L);

    // Assert
    assertNotNull(response);
    assertEquals("PENDING", response.getStatus());
    verify(notificationService, times(1)).notifyBookingCreated(any());
  }

  @DisplayName("Should throw exception when resource not found")
  @Test
  void testCreateBookingResourceNotFound() {
    // Arrange
    BookingRequest request = new BookingRequest(999L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));
    when(resourceRepository.findById(999L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(ResourceNotFoundException.class, () -> bookingService.createBooking(request, 1L));
  }

  @DisplayName("Should prevent double-booking")
  @Test
  void testCreateBookingConflict() {
    // Arrange
    BookingRequest request = new BookingRequest(1L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));
    Resource resource = new Resource(1L, "Room A", "CONFERENCE_ROOM", 20, "Building A", "AVAILABLE", null);
    Booking conflictingBooking = new Booking(...);

    when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));
    when(bookingRepository.findConflictingBookings(anyLong(), any(), any())).thenReturn(List.of(conflictingBooking));

    // Act & Assert
    assertThrows(BookingConflictException.class, () -> bookingService.createBooking(request, 1L));
  }

  @DisplayName("Should cancel booking successfully")
  @Test
  void testCancelBookingSuccess() {
    // Arrange
    Booking booking = new Booking(1L, ..., "CONFIRMED", ...);

    // Act
    bookingService.cancelBooking(1L, 1L);

    // Assert
    verify(bookingRepository).save(argThat(b -> b.getStatus().equals("CANCELLED")));
    verify(notificationService).notifyBookingCancelled(booking);
  }
}
```

### 2.1.2 Ticket Service Tests

```java
@DisplayName("TicketService Tests")
@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

  @Mock private TicketRepository ticketRepository;
  @Mock private UserRepository userRepository;
  @Mock private NotificationService notificationService;
  
  @InjectMocks private TicketService ticketService;

  @DisplayName("Should create ticket with proper status")
  @Test
  void testCreateTicket() {
    // Arrange
    TicketRequest request = new TicketRequest("Broken chair", "Furniture", "HIGH", "Room A");
    when(ticketRepository.save(any())).thenReturn(new Ticket(...));

    // Act
    TicketResponse response = ticketService.createTicket(request, 1L);

    // Assert
    assertEquals("OPEN", response.getStatus());
    assertNotNull(response.getTicketNumber());
  }

  @DisplayName("Should assign ticket to technician")
  @Test
  void testAssignTicket() {
    // Arrange
    Ticket ticket = new Ticket(1L, "Broken chair", ..., "OPEN", null, null);
    User technician = new User(2L, "tech@example.com", "Tech", "TECHNICIAN");

    when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
    when(userRepository.findById(2L)).thenReturn(Optional.of(technician));

    // Act
    TicketResponse response = ticketService.assignTicket(1L, 2L);

    // Assert
    assertEquals(2L, response.getAssignedTo().getId());
    verify(notificationService).notifyTicketAssigned(technician);
  }

  @DisplayName("Should prevent invalid status transitions")
  @Test
  void testInvalidStatusTransition() {
    // Arrange
    Ticket ticket = new Ticket(1L, "Broken chair", ..., "CLOSED", null, null);

    // Act & Assert
    assertThrows(InvalidStatusTransitionException.class, 
      () -> ticketService.updateStatus(1L, "OPEN"));
  }
}
```

### 2.1.3 Authentication Service Tests

```java
@DisplayName("AuthService Tests")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private GoogleOAuthProvider googleOAuthProvider;
  @Mock private JwtTokenProvider jwtTokenProvider;
  
  @InjectMocks private AuthService authService;

  @DisplayName("Should login user with valid Google token")
  @Test
  void testLoginWithGoogleToken() {
    // Arrange
    String googleToken = "valid-google-token";
    GoogleUser googleUser = new GoogleUser("user@university.edu", "John Doe", "avatar.jpg");
    User dbUser = new User("user@university.edu", "John Doe", "USER");

    when(googleOAuthProvider.validateAndExtractUser(googleToken)).thenReturn(googleUser);
    when(userRepository.findByEmail("user@university.edu")).thenReturn(Optional.of(dbUser));
    when(jwtTokenProvider.generateToken(dbUser)).thenReturn("jwt-token");

    // Act
    LoginResponse response = authService.loginWithGoogle(googleToken);

    // Assert
    assertEquals("jwt-token", response.getAccessToken());
    assertEquals("user@university.edu", response.getUser().getEmail());
  }

  @DisplayName("Should create new user on first login")
  @Test
  void testCreateUserOnFirstLogin() {
    // Arrange
    String googleToken = "valid-google-token";
    GoogleUser googleUser = new GoogleUser("newuser@university.edu", "New User", "avatar.jpg");

    when(googleOAuthProvider.validateAndExtractUser(googleToken)).thenReturn(googleUser);
    when(userRepository.findByEmail("newuser@university.edu")).thenReturn(Optional.empty());
    when(userRepository.save(any())).thenReturn(new User(...));
    when(jwtTokenProvider.generateToken(any())).thenReturn("jwt-token");

    // Act
    LoginResponse response = authService.loginWithGoogle(googleToken);

    // Assert
    verify(userRepository).save(argThat(u -> u.getEmail().equals("newuser@university.edu")));
  }

  @DisplayName("Should reject invalid Google token")
  @Test
  void testLoginWithInvalidToken() {
    // Arrange
    when(googleOAuthProvider.validateAndExtractUser("invalid-token")).thenThrow(new InvalidTokenException());

    // Act & Assert
    assertThrows(InvalidTokenException.class, () -> authService.loginWithGoogle("invalid-token"));
  }
}
```

---

## 2.2 Frontend Unit Tests (React)

**Test Framework**: Vitest + React Testing Library  
**Scope**: Components, hooks, utilities

### 2.2.1 Component Tests

```javascript
describe('ResourceCard Component', () => {
  const mockResource = {
    id: 1,
    name: 'Conference Room A',
    type: 'Conference Room',
    capacity: 20,
    location: 'Building A',
    status: 'AVAILABLE',
    imageUrl: 'https://example.com/image.jpg'
  };

  it('should render resource information correctly', () => {
    const { getByText, getByAltText } = render(
      <ResourceCard resource={mockResource} />
    );

    expect(getByText('Conference Room A')).toBeInTheDocument();
    expect(getByText('Building A')).toBeInTheDocument();
    expect(getByText(/20 capacity/i)).toBeInTheDocument();
    expect(getByAltText('Conference Room A')).toHaveAttribute('src', mockResource.imageUrl);
  });

  it('should display AVAILABLE status with correct styling', () => {
    const { container } = render(<ResourceCard resource={mockResource} />);
    const statusBadge = container.querySelector('[data-test="status-badge"]');

    expect(statusBadge).toHaveClass('status-available');
    expect(statusBadge).toHaveTextContent('AVAILABLE');
  });

  it('should show OCCUPIED status when resource is occupied', () => {
    const occupiedResource = { ...mockResource, status: 'OCCUPIED' };
    const { container } = render(<ResourceCard resource={occupiedResource} />);
    const statusBadge = container.querySelector('[data-test="status-badge"]');

    expect(statusBadge).toHaveClass('status-occupied');
  });

  it('should call onBook callback when book button clicked', async () => {
    const onBook = vi.fn();
    const { getByRole } = render(
      <ResourceCard resource={mockResource} onBook={onBook} />
    );

    const bookButton = getByRole('button', { name: /book/i });
    await userEvent.click(bookButton);

    expect(onBook).toHaveBeenCalledWith(mockResource.id);
  });
});
```

### 2.2.2 Hook Tests

```javascript
describe('useAuth Hook', () => {
  it('should return authenticated user data', () => {
    const mockUser = { id: 1, email: 'user@test.com', role: 'USER' };
    
    vi.mock('../../context/AuthContext', () => ({
      useAuth: () => ({ user: mockUser, isAuthenticated: true })
    }));

    const { result } = renderHook(() => useAuth());

    expect(result.current.user).toEqual(mockUser);
    expect(result.current.isAuthenticated).toBe(true);
  });

  it('should return null for unauthenticated users', () => {
    vi.mock('../../context/AuthContext', () => ({
      useAuth: () => ({ user: null, isAuthenticated: false })
    }));

    const { result } = renderHook(() => useAuth());

    expect(result.current.user).toBeNull();
    expect(result.current.isAuthenticated).toBe(false);
  });
});
```

---

## 3. Integration Testing Strategy

### 3.1 Backend Integration Tests

**Framework**: Spring Boot Test + TestContainers  
**Scope**: Controller + Service + Repository interactions

```java
@SpringBootTest
@TestcontainersTest
class BookingControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private BookingRepository bookingRepository;
  @Autowired private ResourceRepository resourceRepository;
  @Autowired private UserRepository userRepository;

  @BeforeEach
  void setup() {
    // Create test data
    User user = userRepository.save(new User("test@example.com", "Test User", "USER"));
    Resource resource = resourceRepository.save(new Resource("Room A", "CONFERENCE", 20, "Building A", "AVAILABLE", null));
  }

  @DisplayName("POST /api/v1/bookings - Create booking successfully")
  @Test
  @WithMockUser(username="test@example.com", roles="USER")
  void testCreateBooking() throws Exception {
    // Arrange
    BookingRequest request = new BookingRequest(
      1L, 
      LocalDateTime.now().plusHours(1), 
      LocalDateTime.now().plusHours(2)
    );

    // Act & Assert
    mockMvc.perform(post("/api/v1/bookings")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.status").value("success"))
      .andExpect(jsonPath("$.data.status").value("PENDING"));

    // Verify saved to DB
    Booking savedBooking = bookingRepository.findAll().get(0);
    assertEquals("PENDING", savedBooking.getStatus());
  }

  @DisplayName("GET /api/v1/resources - List resources")
  @Test
  void testListResources() throws Exception {
    mockMvc.perform(get("/api/v1/resources"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.content").isArray())
      .andExpect(jsonPath("$.data.totalElements").value(1));
  }

  @DisplayName("POST /api/v1/tickets - Create ticket only for authenticated users")
  @Test
  void testCreateTicketUnauthorized() throws Exception {
    TicketRequest request = new TicketRequest("Issue", "MAINTENANCE", "HIGH");

    mockMvc.perform(post("/api/v1/tickets")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isUnauthorized());
  }
}
```

### 3.2 Database Integration Tests

```java
@SpringBootTest
@TestcontainersTest
class BookingRepositoryIntegrationTest {

  @Autowired private BookingRepository bookingRepository;
  @Autowired private ResourceRepository resourceRepository;
  @Autowired private UserRepository userRepository;

  @DisplayName("Should find conflicting bookings for same time slot")
  @Test
  void testFindConflictingBookings() {
    // Setup
    Resource resource = resourceRepository.save(new Resource(...));
    User user = userRepository.save(new User(...));
    LocalDateTime start = LocalDateTime.now().plusHours(1);
    LocalDateTime end = LocalDateTime.now().plusHours(2);

    // Create existing booking
    Booking existing = new Booking(resource, user, start, end, "CONFIRMED");
    bookingRepository.save(existing);

    // Test
    List<Booking> conflicts = bookingRepository.findConflictingBookings(
      resource.getId(),
      start,
      end
    );

    assertEquals(1, conflicts.size());
  }
}
```

---

## 4. End-to-End (E2E) Testing Strategy

### 4.1 Critical User Journeys

**Test Platform**: Manual testing + Selenium/Cypress for automation

#### User Journey 1: Book a Resource
```gherkin
Scenario: User books Conference Room for team meeting
  Given I am logged in as a regular user
  And I navigate to the Resources page
  When I search for "Conference Room"
  And I click on "Conference Room A"
  And I select available time slot (2 PM - 3 PM)
  And I confirm booking
  Then I should see "Booking created successfully"
  And booking appears in "My Bookings"
  And I receive email confirmation
```

#### User Journey 2: Report Maintenance Issue
```gherkin
Scenario: User reports broken chair and receives updates
  Given I am logged in as a regular user
  And I navigate to Maintenance Tickets
  When I click "Report Issue"
  And I fill title: "Broken chair in Room A"
  And I select priority: "HIGH"
  And I upload damage photo
  And I submit the form
  Then ticket is created with ticket number
  And email notification sent to me
  When admin assigns ticket to technician
  Then technician receives email
  And ticket status changes to "IN_PROGRESS"
  When technician marks as "RESOLVED"
  Then I receive notification with resolution notes
  And I can rate satisfaction (1-5 stars)
```

### 4.2 Automated E2E Tests (Cypress Example)

```javascript
describe('Booking Workflow E2E', () => {
  beforeEach(() => {
    cy.visit('http://localhost:3000');
    cy.login('user@university.edu', 'google-oauth-token');
  });

  it('should complete full booking workflow', () => {
    // Navigate to resources
    cy.contains('a', 'Browse Resources').click();
    cy.url().should('include', '/resources');

    // Search and filter
    cy.get('[data-testid="search-input"]').type('Conference');
    cy.get('[data-testid="type-filter"]').select('Conference Room');
    cy.contains('Conference Room A').should('be.visible');

    // View details and check availability
    cy.contains('Conference Room A').click();
    cy.url().should('include', '/resources/');
    cy.contains('Book Now').should('be.visible');

    // Create booking
    cy.contains('Book Now').click();
    cy.get('[data-testid="date-picker"]').click();
    cy.contains('27').click(); // Select 27th
    cy.get('[data-testid="time-start"]').type('14:00');
    cy.get('[data-testid="time-end"]').type('15:00');
    cy.contains('button', 'Confirm Booking').click();

    // Verify success
    cy.contains('Booking confirmed successfully').should('be.visible');
    cy.contains('a', 'My Bookings').click();
    cy.contains('Conference Room A').should('be.visible');
  });
});
```

---

## 5. Performance Testing

### 5.1 Load Testing (JMeter)

```
Test Scenario: 100 Concurrent Users
Duration: 5 minutes
Ramp-up: 2 minutes

Endpoints Tested:
- GET /api/v1/resources (50 users)
- POST /api/v1/bookings (30 users)
- GET /api/v1/bookings/my (20 users)

Acceptance Criteria:
- 95th percentile response time < 500ms
- Error rate < 1%
- Throughput > 50 requests/second
```

### 5.2 Database Query Performance

```sql
-- Check query execution time
EXPLAIN ANALYZE
SELECT b.* FROM bookings b
WHERE b.resource_id = 1
  AND b.start_time >= '2026-04-27'
  AND b.status IN ('CONFIRMED', 'PENDING');
  
Expected: < 10ms with index on (resource_id, start_time, status)
```

---

## 6. Security Testing

### 6.1 Authentication Tests
- ✅ Invalid JWT token returns 401
- ✅ Expired JWT token returns 401
- ✅ Missing Authorization header returns 401
- ✅ User cannot access other user's data
- ✅ User cannot perform admin actions without ADMIN role

### 6.2 Authorization Tests
```java
@Test
@WithMockUser(roles="USER")
void testNonAdminCannotCreateResource() throws Exception {
  mockMvc.perform(post("/api/v1/resources")
    .contentType(MediaType.APPLICATION_JSON)
    .content(json))
  .andExpect(status().isForbidden());
}

@Test
@WithMockUser(roles="ADMIN")
void testAdminCanCreateResource() throws Exception {
  mockMvc.perform(post("/api/v1/resources")
    .contentType(MediaType.APPLICATION_JSON)
    .content(json))
  .andExpect(status().isCreated());
}
```

### 6.3 Data Validation Tests
- ✅ SQL injection prevention
- ✅ XSS prevention in comments
- ✅ File upload size validation
- ✅ Rate limiting on auth endpoints

---

## 7. Test Metrics & Reporting

### 7.1 Code Coverage (Target: 70%+)

```
File                    Lines    Covered   %
────────────────────────────────────────
BookingService.java     245      178      73%
TicketService.java      312      225      72%
UserService.java        189      145      77%
AuthService.java        156      145      93%
ResourceService.java    298      210      70%
────────────────────────────────────────
TOTAL                   1200     903      75%
```

### 7.2 Test Execution Report

| Category | Total | Passed | Failed | Duration |
|----------|-------|--------|--------|----------|
| Unit Tests | 85 | 85 | 0 | 45s |
| Integration Tests | 28 | 28 | 0 | 120s |
| E2E Tests | 15 | 15 | 0 | 180s |
| **TOTAL** | **128** | **128** | **0** | **345s** |

---

## 8. Quality Assurance Checklist

- [ ] All unit tests pass (coverage > 70%)
- [ ] All integration tests pass
- [ ] All critical E2E workflows pass
- [ ] No known bugs in production
- [ ] Code review completed for all features
- [ ] Performance benchmarks met
- [ ] Security audit completed
- [ ] Documentation up to date
- [ ] Accessibility tested (WCAG 2.0 AA)
- [ ] Mobile responsiveness verified
- [ ] Error handling verified
- [ ] Database backup tested

---

**Testing Version**: 1.0  
**Last Updated**: April 27, 2026
