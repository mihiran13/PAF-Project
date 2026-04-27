# Smart Campus - Developer Quick Reference Guide

## 📚 Table of Contents
1. Project Structure
2. Key Technologies
3. Getting Started
4. Development Workflow
5. Common Tasks
6. API Reference
7. Troubleshooting
8. Best Practices

---

## 📁 Project Structure

```
PAF Project/
├── smart-campus/                 # Backend (Spring Boot)
│   ├── src/
│   │   ├── main/java/
│   │   │   └── com/smartcampus/
│   │   │       ├── config/       # Configuration classes
│   │   │       ├── controller/   # REST endpoints
│   │   │       ├── dto/          # Data transfer objects
│   │   │       ├── model/        # JPA entities
│   │   │       ├── repository/   # Data access layer
│   │   │       ├── security/     # Security configs
│   │   │       ├── service/      # Business logic
│   │   │       └── util/         # Utility classes
│   │   └── resources/
│   │       └── application.yml   # Configuration
│   ├── pom.xml                   # Maven dependencies
│   └── README.md
│
├── smart-campus-frontend/        # Frontend (React + Vite)
│   ├── src/
│   │   ├── components/           # React components
│   │   ├── context/              # Context API
│   │   ├── pages/                # Page components
│   │   ├── services/             # API services
│   │   ├── utils/                # Utilities
│   │   ├── validation/           # Form validations
│   │   ├── App.jsx               # Main app
│   │   └── main.jsx              # Entry point
│   ├── package.json
│   ├── vite.config.js
│   └── eslint.config.js
│
├── IMPLEMENTATION_COMPLETE.md    # Feature summary
├── INNOVATION_FEATURES.md        # Advanced features
├── DEPLOYMENT_GUIDE.md           # Deployment instructions
└── README.md                      # Project overview
```

---

## 🛠️ Key Technologies

### Backend
| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Java | 17+ |
| Framework | Spring Boot | 3.x |
| Database | PostgreSQL | 12+ |
| ORM | Spring Data JPA | - |
| Authentication | JWT + OAuth 2.0 | - |
| Build Tool | Maven | 3.8+ |

### Frontend
| Component | Technology | Version |
|-----------|-----------|---------|
| Framework | React | 18+ |
| Build Tool | Vite | 4.x |
| Routing | React Router | 6.x |
| State | React Query + Context | - |
| Forms | React Hook Form | 7.x |
| Validation | Zod | - |
| Styling | CSS | - |

---

## 🚀 Getting Started

### Prerequisites
```bash
# Backend
java -version      # Java 17+
mvn -version       # Maven 3.8+
psql -version      # PostgreSQL 12+

# Frontend
node -version      # Node.js 18+
npm -version       # npm 9+
```

### Backend Setup
```bash
cd smart-campus

# 1. Configure database
# Edit src/main/resources/application.yml with your DB credentials

# 2. Build project
mvn clean install

# 3. Run application
mvn spring-boot:run

# Application will start at http://localhost:8080
```

### Frontend Setup
```bash
cd smart-campus-frontend

# 1. Install dependencies
npm install

# 2. Configure environment
# Create .env.local with API_BASE_URL

# 3. Start development server
npm run dev

# Application will start at http://localhost:5173
```

---

## 💻 Development Workflow

### Daily Workflow
```bash
# 1. Pull latest changes
git pull origin main

# 2. Frontend development
cd smart-campus-frontend
npm run dev      # Hot reload enabled

# 3. Backend development (in another terminal)
cd smart-campus
mvn spring-boot:run

# 4. Make changes and test

# 5. Commit and push
git add .
git commit -m "feat: your feature description"
git push origin feature-branch
```

### Build Artifacts
```bash
# Frontend
npm run build    # Builds to dist/

# Backend
mvn clean package    # Builds to target/spring-campus-application.jar
```

---

## 🎯 Common Tasks

### 1. Create New API Endpoint (Backend)

**Step 1: Create Entity**
```java
@Entity
@Table(name = "resources")
@Data
@NoArgsConstructor
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String name;
    
    // ... other fields
}
```

**Step 2: Create Repository**
```java
@Repository
public interface ResourceRepository extends JpaRepository<Resource, String> {
    List<Resource> findByType(String type);
    List<Resource> findByStatusAndCapacityGreaterThan(String status, int capacity);
}
```

**Step 3: Create Service**
```java
@Service
@RequiredArgsConstructor
public class ResourceService {
    private final ResourceRepository repository;
    
    public List<Resource> getAvailable() {
        return repository.findByStatusAndCapacityGreaterThan("ACTIVE", 0);
    }
}
```

**Step 4: Create Controller**
```java
@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {
    private final ResourceService service;
    
    @GetMapping
    public ResponseEntity<List<Resource>> getAll() {
        return ResponseEntity.ok(service.getAvailable());
    }
}
```

### 2. Create New Frontend Page

**Step 1: Create Component**
```jsx
import { useQuery } from '@tanstack/react-query'
import { resourceService } from '../services/api'

export default function ResourcePage() {
  const { data, isLoading } = useQuery({
    queryKey: ['resources'],
    queryFn: () => resourceService.list()
  })
  
  if (isLoading) return <Loader />
  
  return <div>{/* JSX here */}</div>
}
```

**Step 2: Add Route**
```jsx
// In App.jsx
import ResourcePage from './pages/ResourcePage'

<Route path="/resources" element={<ResourcePage />} />
```

**Step 3: Add Navigation Link**
```jsx
// In Layout.jsx
{ to: '/resources', label: 'Resources', icon: '🏢' }
```

### 3. Add Form Validation

**Step 1: Define Schema**
```javascript
import { z } from 'zod'

const schema = z.object({
  name: z.string().min(2).max(255),
  capacity: z.number().int().positive(),
  type: z.string().refine(v => TYPES.includes(v))
})
```

**Step 2: Use in Form**
```jsx
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'

const { register, handleSubmit, formState: { errors } } = useForm({
  resolver: zodResolver(schema)
})
```

**Step 3: Display Errors**
```jsx
{errors.name && <p className="error">{errors.name.message}</p>}
```

### 4. Add New Query/Mutation

**Backend (Spring)**
```java
@GetMapping("/search")
public ResponseEntity<PagedResponse<Resource>> search(
    @RequestParam String keyword,
    @RequestParam int page,
    Pageable pageable
) {
    Page<Resource> results = service.search(keyword, PageRequest.of(page, 10));
    return ResponseEntity.ok(new PagedResponse<>(results));
}
```

**Frontend (React)**
```javascript
export const resourceService = {
    search: (keyword, page) => 
        http.get('/api/resources/search', { params: { keyword, page } })
            .then(r => r.data.data)
}
```

### 5. Add Authentication to Endpoint

**Backend**
```java
@GetMapping
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> getAll() {
    // Only admins can access
}
```

**Frontend (Route Protection)**
```jsx
<Route element={<ProtectedRoute roles={['ADMIN']} />}>
    <Route path="/admin" element={<AdminPage />} />
</Route>
```

---

## 📡 API Reference

### Base URL
- Development: `http://localhost:8080`
- Production: `https://api.yourdomain.com`

### Response Format
```json
{
  "status": "success|error",
  "data": {...},
  "message": "Optional message",
  "timestamp": "2026-01-01T00:00:00Z"
}
```

### Authentication
```bash
Authorization: Bearer <JWT_TOKEN>
```

### Common Endpoints

#### Resources
```
GET    /api/resources              # List resources
GET    /api/resources?keyword=lab  # Search
GET    /api/resources/{id}         # Get detail
POST   /api/resources              # Create
PUT    /api/resources/{id}         # Update
DELETE /api/resources/{id}         # Delete
PATCH  /api/resources/{id}/status  # Update status
```

#### Bookings
```
GET    /api/bookings               # Admin - all bookings
GET    /api/bookings/my            # User - own bookings
POST   /api/bookings               # Create booking
PATCH  /api/bookings/{id}/approve  # Approve booking
PATCH  /api/bookings/{id}/cancel   # Cancel booking
```

#### Tickets
```
GET    /api/tickets                # All tickets
POST   /api/tickets                # Create ticket
PATCH  /api/tickets/{id}/status    # Update status
POST   /api/tickets/{id}/comments  # Add comment
```

---

## 🐛 Troubleshooting

### Backend Issues

#### Database Connection Error
```
Solution: Verify credentials in application.yml
Check PostgreSQL is running: psql -U postgres
```

#### Port 8080 Already in Use
```bash
# Kill process on port 8080
lsof -i :8080
kill -9 <PID>
```

#### Build Failure
```bash
# Clear Maven cache
mvn clean
rm -rf ~/.m2/repository
mvn install
```

### Frontend Issues

#### Port 5173 Already in Use
```bash
# Use different port
npm run dev -- --port 3000
```

#### Module Not Found
```bash
# Reinstall dependencies
rm -rf node_modules package-lock.json
npm install
```

#### API Not Responding
```
Check: Backend is running on correct port
Check: CORS is configured correctly
Check: API_BASE_URL is correct in .env
```

### Common Errors & Solutions

| Error | Cause | Solution |
|-------|-------|----------|
| 401 Unauthorized | Missing/invalid token | Login again, check token expiry |
| 403 Forbidden | Insufficient permissions | Check user role assignment |
| 404 Not Found | Endpoint doesn't exist | Verify endpoint path |
| 500 Internal Server Error | Backend error | Check backend logs |
| CORS Error | Cross-origin issue | Configure CORS in backend |

---

## ✅ Best Practices

### Backend

1. **Always Use DTOs**
   ```java
   // Good
   return new ResourceResponse(resource);
   
   // Avoid
   return resource; // Exposes JPA entity
   ```

2. **Handle Exceptions Properly**
   ```java
   try {
       // code
   } catch (Exception e) {
       logger.error("Error: ", e);
       throw new CustomException(msg);
   }
   ```

3. **Use Service Layer**
   ```java
   // Controllers call services, not repositories directly
   @Autowired private ResourceService service;
   ```

4. **Validate Input**
   ```java
   @Valid @RequestBody ResourceRequest request
   // Use @NotNull, @NotBlank, @Positive, etc.
   ```

### Frontend

1. **Use React Query for Data**
   ```javascript
   // Good
   const { data } = useQuery({ queryKey: [...], queryFn: ... })
   
   // Avoid
   const [data, setData] = useState(null);
   ```

2. **Separate Concerns**
   ```javascript
   // Components handle UI
   // Services handle API calls
   // Utils handle helpers
   ```

3. **Use React Hook Form**
   ```javascript
   // Good - form state management
   const { register, handleSubmit } = useForm()
   
   // Avoid - manual state for every field
   ```

4. **Lazy Load Routes**
   ```javascript
   const AdminPage = lazy(() => import('./pages/AdminPage'))
   ```

5. **Add Loading/Error States**
   ```javascript
   if (isLoading) return <Loader />
   if (isError) return <Error />
   return <Content />
   ```

### General

1. **Follow Naming Conventions**
   - Backend: `camelCase` for variables, `PascalCase` for classes
   - Frontend: `camelCase` for files/components (unless folder)

2. **Add Comments for Complex Logic**
   ```java
   // Calculates resource availability based on recent bookings
   public int calculateAvailability(Resource resource) { ... }
   ```

3. **Use Environment Variables**
   ```java
   @Value("${app.api.url:http://localhost:8080}")
   private String apiUrl;
   ```

4. **Test Your Code**
   ```bash
   # Backend
   mvn test
   
   # Frontend
   npm run test
   ```

5. **Use Version Control Properly**
   ```bash
   git commit -m "feat: add resource recommendation"  # Good
   git commit -m "update"                              # Avoid
   ```

---

## 📖 Useful Resources

### Documentation
- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [React Documentation](https://react.dev)
- [React Query Docs](https://tanstack.com/query/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

### Tools
- VS Code with Java Extension Pack
- Postman for API testing
- pgAdmin for database management
- Chrome DevTools for frontend debugging

### Commands Cheat Sheet
```bash
# Backend
mvn clean install       # Build
mvn spring-boot:run     # Run
mvn test                # Test
mvn package             # Package

# Frontend
npm install             # Install dependencies
npm run dev             # Development
npm run build           # Production build
npm run preview         # Preview build
npm run lint            # Lint code

# Git
git clone <repo>        # Clone
git checkout -b feat    # New branch
git add .               # Stage changes
git commit -m "msg"     # Commit
git push origin feat    # Push
git pull                # Pull changes
```

---

## 🚨 Important Notes

1. **Always test locally before pushing**
2. **Never commit environment variables or secrets**
3. **Keep dependencies updated regularly**
4. **Document API changes in comments**
5. **Follow the existing code style**
6. **Test with different screen sizes**
7. **Test all user roles and permissions**
8. **Check CORS configuration for new endpoints**

---

## 📞 Support

- Check documentation files: `IMPLEMENTATION_COMPLETE.md`, `INNOVATION_FEATURES.md`
- Review code comments and JSDoc
- Check backend logs: `backend_log.txt`
- Use browser DevTools for frontend debugging

---

**Last Updated**: 2026
**Version**: 1.0

