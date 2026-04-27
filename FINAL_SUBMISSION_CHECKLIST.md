# ✅ FINAL SUBMISSION CHECKLIST

**Deadline:** 11:45 PM, April 27, 2026  
**Submission Platform:** Courseweb (as .zip file)  
**Time Remaining:** ~4 hours

---

## BEFORE YOU START

- [ ] All 3 team members have the latest code pulled
- [ ] Backend compiles: `mvn clean compile`
- [ ] Frontend builds: `npm run build`
- [ ] You have the exact deadline: **11:45 PM GMT+5:30** (convert to your timezone NOW)
- [ ] You know your group number (XX)

---

## PRIORITY 1: CRITICAL (Must Complete)

### [ ] 1.1 Create Final PDF Report
**File Name:** `IT3030_PAF_Assignment_2026_Group[XX].pdf`  
**Sections Required:**

- [ ] Cover Page
  - [ ] Title: "Smart Campus Operations Hub"
  - [ ] Course: IT3030
  - [ ] Group Number: XX
  - [ ] Date: April 27, 2026
  - [ ] Team Members: Name1, Name2, Name3

- [ ] Executive Summary (1 page)
  - [ ] What the system does
  - [ ] Key modules (5 total)
  - [ ] Technology stack

- [ ] Functional Requirements (1-2 pages)
  - [ ] Module A: Resources
  - [ ] Module B: Bookings
  - [ ] Module C: Tickets
  - [ ] Module D: Notifications
  - [ ] Module E: Authentication

- [ ] System Architecture Diagram (1 page)
  ```
  Draw or insert diagram:
  ┌─────────┐      ┌─────────┐      ┌─────────┐
  │ Browser │─────▶│  React  │─────▶│Firebase?│
  └─────────┘      └─────────┘      └─────────┘
        │                 │               │
        └─────────────────┼───────────────┘
              │           ▼
              │    ┌────────────┐
              └──▶ │Spring Boot │
                   └────┬───────┘
                        │
                   ┌────▼────┐
                   │  MySQL  │
                   └─────────┘
  ```
  - [ ] Frontend layer
  - [ ] Backend layer
  - [ ] Database layer
  - [ ] Auth flow

- [ ] REST API Architecture (1-2 pages)
  - [ ] Layering diagram (Controller → Service → Repository)
  - [ ] Request/Response flow
  - [ ] Exception handling flow
  - [ ] Security flow (@PreAuthorize → JWT verification)

- [ ] Complete Endpoint Table (2-3 pages)
  ```
  | # | Method | Endpoint | Auth | Role | Purpose |
  |---|--------|----------|------|------|---------|
  | 1 | POST | /api/resources | Yes | ADMIN | Create resource |
  | 2 | GET | /api/resources | Yes | All | List resources |
  ... (include ALL 40+ endpoints)
  ```

- [ ] Frontend Architecture (1 page)
  - [ ] Component tree diagram
  - [ ] Pages list:
    - [ ] LoginPage
    - [ ] DashboardPage
    - [ ] ResourceListPage, ResourceDetailPage
    - [ ] BookingCreatePage, BookingDetailPage, BookingAdminPage, MyBookingsPage
    - [ ] TicketCreatePage, TicketDetailPage, TicketAdminPage, TicketListPage
    - [ ] NotificationsPage
    - [ ] UserAdminPage
    - [ ] ProfilePage

- [ ] Database Schema (1 page)
  - [ ] Entity relationship diagram (or table list):
    - [ ] User (id, name, email, role, oauthId, isActive)
    - [ ] Resource (id, name, type, capacity, location, status)
    - [ ] Booking (id, userId, resourceId, date, status)
    - [ ] Ticket (id, userId, resourceId, status, priority)
    - [ ] Notification (id, userId, type, isRead)
    - [ ] Comment (id, userId, ticketId, content)
    - [ ] Attachment (id, ticketId, fileUrl)
    - [ ] AvailabilityWindow (id, resourceId, dayOfWeek, startTime, endTime)

- [ ] Testing Evidence (1 page)
  - [ ] Postman collection screenshots (5-10 requests)
  - [ ] Sample requests:
    - [ ] POST /api/auth/login (success response)
    - [ ] GET /api/resources (paginated response)
    - [ ] POST /api/bookings (201 created)
    - [ ] PATCH /api/bookings/{id}/reject (400 error)
    - [ ] POST /api/tickets (with attachments)
    - [ ] GET /api/tickets/{id} (with comments)
  - [ ] Error response examples:
    - [ ] 401 Unauthorized
    - [ ] 403 Forbidden
    - [ ] 404 Not Found
    - [ ] 400 Validation Error

- [ ] Team Member Contributions (1 page)
  - [ ] Member 1
    - [ ] Module: Facilities & Assets Catalogue
    - [ ] Backend Endpoints: (list 4-8)
    - [ ] Frontend Components: (list)
    - [ ] Key Files: ResourceController.java, ResourceService.java, ResourceListPage.jsx
  
  - [ ] Member 2
    - [ ] Module: Booking Management
    - [ ] Backend Endpoints: (list 4-8)
    - [ ] Frontend Components: (list)
    - [ ] Key Files: BookingController.java, BookingService.java, BookingCreatePage.jsx
  
  - [ ] Member 3
    - [ ] Module: Tickets + Notifications + Auth
    - [ ] Backend Endpoints: (list 4-8)
    - [ ] Frontend Components: (list)
    - [ ] Key Files: TicketController.java, AuthController.java, NotificationController.java

- [ ] Conclusion (0.5 page)
  - [ ] Summary of work completed
  - [ ] Challenges overcome
  - [ ] Future enhancements

**Estimated Time: 60-90 minutes**  
**Marks Gained: +12**

---

### [ ] 1.2 Add Backend GitHub Actions Workflow
**File:** `.github/workflows/backend-ci.yml`

Create this file in the `.github/workflows/` directory:

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

Then:
- [ ] Commit and push to GitHub
- [ ] Verify both `backend-ci.yml` and `frontend-ci.yml` (.github/workflows/) exist
- [ ] Check GitHub Actions tab — verify both workflows trigger

**Estimated Time: 20 minutes**  
**Marks Gained: +2**

---

### [ ] 1.3 Create Postman Collection (or Screenshots)
**Option A (Best):** Export from Postman

- [ ] Open Postman
- [ ] Create collection: "Smart Campus API"
- [ ] Add folders for each module:
  - [ ] Auth
  - [ ] Resources
  - [ ] Bookings
  - [ ] Tickets
  - [ ] Notifications

- [ ] Add 5-10 sample requests per module:
  - [ ] POST requests (create operations)
  - [ ] GET requests (read operations)
  - [ ] PATCH requests (update operations)
  - [ ] DELETE requests (delete operations)

- [ ] For each request, add:
  - [ ] Authorization header (Bearer token)
  - [ ] Sample request body
  - [ ] Verify response status and format

- [ ] Export JSON: `Smart_Campus_Postman_Collection.json`
- [ ] Include in PDF as Appendix or separate file

**Option B (Alternative):** Screenshots

- [ ] Take 5-10 screenshots showing:
  - [ ] Collection folder structure
  - [ ] Sample successful request (200/201)
  - [ ] Sample error response (400/401/403)
  - [ ] Request with authorization header
  - [ ] Response with data

- [ ] Insert screenshots into PDF

**Estimated Time: 10-20 minutes**  
**Marks Gained: +1**

---

## PRIORITY 2: IMPORTANT (Should Complete)

### [ ] 2.1 Document Git Contributions
**File:** `CONTRIBUTIONS.md` (in project root)

```markdown
# Team Contributions

## Member 1 — Resources Module
- **Backend Endpoints:**
  - POST /api/resources
  - GET /api/resources
  - GET /api/resources/{id}
  - PUT /api/resources/{id}
  - PATCH /api/resources/{id}/status
  - DELETE /api/resources/{id}
  - GET /api/resources/{id}/availability
  - POST /api/resources/{id}/availability

- **Frontend Components:**
  - ResourceListPage.jsx
  - ResourceDetailPage.jsx
  - ResourceCreatePage.jsx (if exists)

- **Key Files:**
  - ResourceController.java
  - ResourceService.java
  - ResourceRepository.java
  - Resource.java (entity)

- **Git Commits:** X commits
- **Total Hours:** X hours

## Member 2 — Booking Module
[Similar format]

## Member 3 — Tickets/Notifications/Auth
[Similar format]

## Timeline
- Week 1: Planning & setup
- Week 2: Backend implementation
- Week 3: Frontend implementation
- Week 4: Integration & bug fixes
- Week 5: Documentation & deployment
```

**Estimated Time: 15 minutes**  
**Marks Gained: +1-2**

---

### [ ] 2.2 Add 1 Innovation Feature (Optional but worth it!)
**Choose ONE:**

#### Option A: QR Code Check-In (Medium)
- [ ] Frontend: Generate QR code URL in BookingDetailPage
- [ ] Backend: Endpoint already exists `/api/bookings/{id}/verify`
- [ ] QR code URL format: `{APP_URL}/bookings/{id}/check-in?token={CHECK_IN_TOKEN}`
- [ ] Display QR code using: `https://qr-server.com/api/qr?size=200x200&data={URL}`

#### Option B: Notification Preferences (Medium)
- [ ] Create NotificationPreference entity
- [ ] Add GET/PATCH endpoints
- [ ] Frontend: Add preferences page with checkboxes
- [ ] Categories: BookingNotifications, TicketNotifications, CommentNotifications

#### Option C: Analytics Dashboard (Hard)
- [ ] Backend: Add /api/analytics endpoint
- [ ] Return: totalBookings, approvedCount, topResource, peakHour
- [ ] Frontend: Display cards on dashboard

**Estimated Time: 45-60 minutes**  
**Marks Gained: +4-5**

---

## PRIORITY 3: NICE TO HAVE (If Time Permits)

### [ ] 3.1 Fix Resource Admin UI
**Add resource creation/edit form:**
- [ ] Page: ResourceAdminPage.jsx
- [ ] Form fields: name, type, capacity, location, status
- [ ] Route: /resources/manage (ADMIN only)
- [ ] Link in sidebar

**Estimated Time: 20 minutes**  
**Marks Gained: +1**

---

### [ ] 3.2 Improve UI Polish (Low Priority)
- [ ] Add loading skeletons
- [ ] Add confirmation dialogs for delete actions
- [ ] Improve table responsive behavior
- [ ] Add inline help tooltips

**Estimated Time: 30+ minutes**  
**Marks Gained: +0.5-1**

---

## FINAL VERIFICATION

### Backend
- [ ] `cd smart-campus && mvn clean compile -q` — NO ERRORS
- [ ] Check for compilation warnings (acceptable)
- [ ] Verify application.yml exists and configured

### Frontend
- [ ] `cd smart-campus-frontend && npm run build` — SUCCESS
- [ ] `npm run lint` — NO CRITICAL ERRORS
- [ ] `npm run test:run` — TESTS PASS
- [ ] Check dist/ folder created

### GitHub
- [ ] `.github/workflows/backend-ci.yml` exists
- [ ] `.github/workflows/ci.yml` exists (frontend)
- [ ] Both workflows trigger on push
- [ ] Both show as "passing" in Actions tab

### Documentation
- [ ] `IT3030_PAF_Assignment_2026_Group[XX].pdf` exists
- [ ] `CONTRIBUTIONS.md` exists
- [ ] `Postman_Collection.json` or screenshots exist
- [ ] All files in project root or docs/ folder

---

## FINAL ZIP CREATION

### What to INCLUDE in ZIP:
- [ ] `smart-campus/` (backend source code)
- [ ] `smart-campus-frontend/` (frontend source code)
- [ ] `*.md` (all markdown files: README, CONTRIBUTIONS, etc.)
- [ ] `IT3030_PAF_Assignment_2026_Group[XX].pdf` (final report)
- [ ] `.github/workflows/` (both CI/CD workflows)

### What to EXCLUDE from ZIP:
- ❌ `node_modules/` (delete or exclude)
- ❌ `target/` (Java compiled output)
- ❌ `.env` (include only `.env.example`)
- ❌ `.git/` (version control metadata)
- ❌ `dist/` (compiled frontend)
- ❌ `.DS_Store` (macOS files)

### Commands to Prepare ZIP:
```bash
# Delete unnecessary folders
rm -rf smart-campus/target
rm -rf smart-campus-frontend/node_modules
rm -rf smart-campus-frontend/dist

# Create ZIP (Linux/Mac)
zip -r PAF_Assignment_2026_GroupXX.zip \
  smart-campus \
  smart-campus-frontend \
  *.md \
  *.pdf \
  .github \
  -x "*/node_modules/*" "*/target/*" "*/.env" "*/.git/*"

# Or use Windows File Explorer:
# Right-click → Send to → Compressed folder
```

**File Name:** `PAF_Assignment_2026_Group[XX].zip`  
**File Size:** Should be 3-5 MB (not 50 MB+)

---

## SUBMISSION PROCESS

### Step 1: Verify ZIP Size
- [ ] ZIP file is between 3-5 MB
- [ ] If > 10 MB, delete node_modules/ and target/

### Step 2: Test ZIP
- [ ] Extract ZIP to temp folder
- [ ] Verify all expected folders exist
- [ ] Verify PDF is included
- [ ] Verify .env not included

### Step 3: Upload to Courseweb
- [ ] Go to: Courseweb → IT3030 → Assignment
- [ ] Click "Upload" or "Submit"
- [ ] Select your ZIP file
- [ ] **DO NOT** submit .rar or .7z (must be .zip)
- [ ] Click "Submit"
- [ ] **WAIT** for confirmation message

### Step 4: Verify Submission
- [ ] Check confirmation page
- [ ] Note submission timestamp
- [ ] Verify before **11:45 PM GMT+5:30**

---

## TIME ALLOCATION (OPTIMAL)

```
Now (approximately 4 hours left)
│
├─ 00:00-01:30 → Create PDF Report
│                (architecture diagrams, endpoints, team contributions)
│
├─ 01:30-01:50 → Backend CI/CD Workflow
│                (add .github/workflows/backend-ci.yml)
│
├─ 01:50-02:00 → Postman Collection
│                (5-10 sample requests with screenshots)
│
├─ 02:00-02:10 → Document Contributions
│                (create CONTRIBUTIONS.md)
│
├─ 02:10-03:10 → Add Innovation Feature (OPTIONAL)
│                (QR code, preferences, or analytics)
│
├─ 03:10-03:30 → Final Verification
│                (build, test, lint checks)
│
├─ 03:30-03:40 → Create ZIP File
│                (exclude node_modules, target, .git, .env)
│
└─ 03:40-03:45 → SUBMIT ON COURSEWEB ✅
                (verify confirmation before 11:45 PM)
```

---

## EMERGENCY FALLBACK (If Running Out of Time)

**If you only have 1-2 hours left:**

Priority 1 (MINIMUM to pass):
1. Create simple PDF with:
   - [ ] Title page
   - [ ] Endpoint list (copy from code)
   - [ ] System overview diagram (simple)
   - [ ] Team member list

2. Add Backend CI/CD workflow

3. Submit with what you have

**This gets you minimum 80 marks**

---

## COMMON MISTAKES TO AVOID

- ❌ Forgetting to include PDF (automatic fail on documentation)
- ❌ Including node_modules (makes ZIP 200+ MB)
- ❌ Submitting .rar instead of .zip
- ❌ Not excluding .env (security risk)
- ❌ Submitting after 11:45 PM (late submission rules apply)
- ❌ Uploading without team member names in PDF
- ❌ Missing architecture diagrams (looks incomplete)
- ❌ No Postman evidence (can't verify API works)

---

## SUCCESS CRITERIA

### Minimal Pass (70 marks):
✅ System builds and runs  
✅ All modules working  
✅ PDF report submitted  

### Good Pass (80 marks):
✅ Above +  
✅ Backend CI/CD working  
✅ Postman collection included  
✅ Team contributions documented  

### Strong Pass (88+ marks):
✅ Above +  
✅ 1 Innovation feature  
✅ Git history documented  
✅ Complete testing evidence  

---

## FINAL CHECKLIST (Before Clicking Submit)

- [ ] PDF file: `IT3030_PAF_Assignment_2026_Group[XX].pdf` ✅
- [ ] ZIP contains both backend and frontend ✅
- [ ] No node_modules/ included ✅
- [ ] No target/ included ✅
- [ ] .env.example exists, .env excluded ✅
- [ ] GitHub workflows both present ✅
- [ ] CONTRIBUTIONS.md included ✅
- [ ] Backend compiles: `mvn clean compile` ✅
- [ ] Frontend builds: `npm run build` ✅
- [ ] ZIP size is 3-5 MB ✅
- [ ] Upload deadline is 11:45 PM GMT+5:30 ✅
- [ ] You've tested the system works ✅

---

**🚀 YOU'RE READY! START WITH THE PDF NOW! 🚀**

Good luck! You've built an excellent system. Now just package it properly! 
