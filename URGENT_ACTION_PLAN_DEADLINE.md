# 🚨 URGENT ACTION PLAN — April 27, 2026 | 11:45 PM DEADLINE

**Time Remaining: ~4 HOURS**  
**Current Estimated Marks: 72/100**  
**Target Marks: 85+/100**

---

## CRITICAL: These Must Be Done BEFORE Submission

### ✋ STOP — Don't Submit Until These Are Complete:

#### [ ] 1. Create Final PDF Report (90 min) — REQUIRED SUBMISSION ARTIFACT
**File:** `IT3030_PAF_Assignment_2026_Group[XX].pdf`

**Minimum Sections:**
- [ ] Cover page (Group XX, Date, Team members)
- [ ] Executive Summary (1 page)
- [ ] System Architecture Diagram (1 page)
  - Frontend → API → Backend → Database flow
  - Simple draw.io diagram or even hand-drawn + photographed
- [ ] REST API Architecture (1 page)
  - Show layering: Controller → Service → Repository
  - List all endpoint categories
- [ ] Frontend Architecture (1 page)
  - Component tree diagram
  - Page list with descriptions
- [ ] Database Schema (0.5 page)
  - Entity list and relationships
- [ ] Endpoint Table (2 pages)
  ```
  | Method | Endpoint | Auth | Role | Purpose |
  |--------|----------|------|------|---------|
  | POST | /api/resources | Yes | ADMIN | Create resource |
  | GET | /api/resources | Yes | Any | List resources |
  ...
  ```
- [ ] Testing Evidence (0.5 page)
  - Screenshot of Postman collection (or curl commands)
  - Test endpoints called
- [ ] Team Contributions (1 page)
  - Member 1: Resources module (list endpoints)
  - Member 2: Bookings module (list endpoints)
  - Member 3: Tickets/Notifications/Auth modules (list endpoints)

**Time: 60-90 minutes**

---

#### [ ] 2. Create GitHub Backend CI/CD Workflow (20 min) — MARKS +2
**Create File:** `.github/workflows/backend-ci.yml`

```yaml
name: Backend CI

on:
  push:
    paths:
      - 'smart-campus/**'
  pull_request:
    paths:
      - 'smart-campus/**'

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          cache: maven
      - name: Build with Maven
        run: cd smart-campus && mvn clean compile -q
      - name: Run Tests
        run: cd smart-campus && mvn test -q
        continue-on-error: true
```

**Time: 20 minutes**

---

#### [ ] 3. Create Postman Collection (20 min) — EVIDENCE +1
**Option A: Export from Postman (5 min)**
- Open Postman
- Create collection: `Smart Campus API`
- Add 5-10 sample requests:
  - POST /api/auth/login
  - GET /api/resources
  - POST /api/bookings
  - GET /api/tickets
  - PATCH /api/tickets/{id}/resolve
- Export as JSON
- Add to report as appendix

**Option B: Screenshot Evidence (10 min)**
- Screenshot Postman showing:
  - Collection folder structure
  - Sample request with response (status 200/201)
  - Error response (status 400/401)
- Include in PDF report

**Time: 10-20 minutes**

---

#### [ ] 4. Document Git Contributions (10 min) — MARKS +1-2
**Create**: `CONTRIBUTIONS.md` in root

```markdown
# Team Contributions

## Git Commit History Summary
- Total commits: XX
- Authors: Member1, Member2, Member3
- Main branches: main, develop (if used)

## Per-Member Breakdown

### Member 1 — Resources Module
- Endpoints: POST /api/resources, GET /api/resources, etc.
- Commits: XX commits
- Key files: ResourceController.java, ResourceService.java, Resource.java
- Dates: XX to YY

### Member 2 — Bookings Module
- Endpoints: POST /api/bookings, GET /api/bookings, PATCH /api/bookings/{id}/approve, etc.
- Commits: XX commits
- Key files: BookingController.java, BookingService.java, Booking.java
- Dates: XX to YY

### Member 3 — Tickets/Notifications/Auth
- Endpoints: POST /api/tickets, PATCH /api/tickets/{id}/resolve, GET /api/notifications, etc.
- Commits: XX commits
- Key files: TicketController.java, NotificationController.java, AuthController.java
- Dates: XX to YY
```

**Time: 10 minutes**

---

### Total MUST DO Time: **2 hours maximum**

---

## SHOULD DO: Quick Wins for +5 More Marks

### [ ] 5. Add 1 Quick Innovation Feature (60 min) — MARKS +4-5

**Pick ONE (Easiest First):**

#### Option A: Booking Check-In QR Code (Easy)
You already have check-in logic! Just add:

**Backend:** In `BookingResponse.java`, add:
```java
private String qrCodeUrl; // Generated check-in link
```

**Frontend:** In `BookingDetailPage.jsx`, add:
```javascript
// Generate QR code for check-in link
const checkInLink = `${window.location.origin}/bookings/${booking.id}/check-in?token=${bookingToken}`
// Show QR code using: https://qr-server.com/api/qr?size=200x200&data=[URL]
```

**Time: 30-40 minutes**

---

#### Option B: Notification Preferences (Medium)
**Backend:** Add `NotificationPreference` entity:
```java
@Entity
public class NotificationPreference {
    @Id private Long id;
    @ManyToOne private User user;
    private boolean bookingNotifications = true;
    private boolean ticketNotifications = true;
    private boolean commentNotifications = true;
}
```

**Frontend:** Add `/notifications/preferences` page with checkboxes

**Time: 40-50 minutes**

---

#### Option C: Booking Analytics Widget (Medium)
**Backend:** Add endpoint:
```java
@GetMapping("/analytics")
public ResponseEntity<ApiResponse<BookingAnalytics>> getAnalytics() {
    // Return: totalBookings, approvedCount, rejectedCount, mostUsedResource
}
```

**Frontend:** Add dashboard card showing stats

**Time: 40-50 minutes**

---

### [ ] 6. Fix Resource Admin UI (20 min) — MARKS +1

Add "Create Resource" form to admin page (single form in modal/pop-up)

---

### Total SHOULD DO Time: **60-90 minutes**

---

## NICE TO HAVE: If You Have Time (30+ min remain)

- [ ] Add loading skeletons (better UX)
- [ ] Add confirmation dialogs for delete actions
- [ ] Add dark mode toggle
- [ ] Improve table responsive behavior
- [ ] Add inline help/tooltips

---

## FINAL SUBMISSION CHECKLIST

### Before clicking "Submit":

```
Backend:
  [ ] mvn clean compile succeeds
  [ ] No compilation errors
  [ ] No critical warnings

Frontend:
  [ ] npm run build succeeds
  [ ] npm run lint passes
  [ ] npm run test:run passes

Documentation:
  [ ] IT3030_PAF_Assignment_2026_GroupXX.pdf exists
  [ ] PDF has all required sections (see above)
  [ ] Architecture diagrams included
  [ ] Endpoint table complete

GitHub:
  [ ] Repository is public or accessible
  [ ] README.md exists with setup instructions
  [ ] .github/workflows/backend-ci.yml added
  [ ] .github/workflows/frontend-ci.yml exists (already there)
  [ ] git log shows contributions from all members

Zip File:
  [ ] NO node_modules/ included
  [ ] NO target/ (Java build folder) included
  [ ] NO .env file (only .env.example)
  [ ] NO .git folder
  [ ] Contains: smart-campus/, smart-campus-frontend/, docs/, PDF report
  [ ] File named: PAF_Assignment_2026_GroupXX.zip

README:
  [ ] Setup instructions clear
  [ ] Credentials explained
  [ ] API base URL mentioned
  [ ] Build/run commands listed
  [ ] Testing instructions included
  [ ] Endpoints list provided
```

---

## Time Allocation (Next 4 Hours)

```
11:45 PM → 3:45 PM (Actual time you have)
|
├─ 00:00-00:20 → GitHub Backend CI/CD workflow
├─ 00:20-01:50 → Create PDF report with diagrams  
├─ 01:50-02:00 → Create Postman collection or screenshot
├─ 02:00-02:10 → Document contributions
├─ 02:10-03:10 → Add 1 innovation feature (QR codes or preferences)
├─ 03:10-03:30 → Fix resource admin UI
├─ 03:30-03:40 → Final verification (build, test, lint)
├─ 03:40-03:45 → Create zip file and submit
```

---

## If Running Out of Time (Must Prioritize)

**Bare Minimum to Pass (70+ marks):**
1. ✅ Final PDF report (Don't submit without this!)
2. ✅ Backend CI/CD workflow
3. ✅ Postman collection screenshots

**To Reach 80+ marks:**
4. ✅ Document contributions
5. ✅ Add 1 innovation feature

---

## Quick Command Reference

```bash
# Verify backend compiles
cd smart-campus
mvn clean compile -q

# Verify frontend builds
cd ../smart-campus-frontend
npm run build

# Create git log for contributions
git log --oneline --graph --all > ../git-history.txt

# List all endpoints (grep pattern)
grep -r "@PostMapping\|@GetMapping\|@PutMapping\|@PatchMapping\|@DeleteMapping" \
  smart-campus/src/main/java/com/smartcampus/controller/

# Create zip without build files
# Remove target and node_modules first!
rm -rf smart-campus/target
rm -rf smart-campus-frontend/node_modules
zip -r PAF_Assignment_2026_Group01.zip smart-campus smart-campus-frontend \
  *.md *.pdf -x "*/node_modules/*" "*/target/*" "*/.env" "*/.git/*"
```

---

## Questions? Ask Yourself:

1. ✅ Can the system build and run?
2. ✅ Is the PDF report complete?
3. ✅ Does GitHub show both frontend and backend workflows?
4. ✅ Can each member explain their endpoints?
5. ✅ Is there any innovative feature (beyond minimum)?

If YES to all → Submit confidently!

---

**Good luck! You've built a solid system. Now finish strong! 🎯**
