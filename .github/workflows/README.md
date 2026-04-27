# GitHub Actions CI/CD Workflows - Smart Campus

## Overview

This directory contains GitHub Actions workflows for continuous integration and deployment (CI/CD) of the Smart Campus project. These workflows are triggered automatically on push, pull requests, and tags.

---

## 📋 Workflows Included

### 1. **Backend CI** (`backend-ci.yml`)
- **Triggered**: On push/PR affecting `smart-campus/` directory
- **What it does**:
  - Sets up Java 17
  - Builds backend with Maven
  - Runs unit/integration tests
  - Uploads test reports and JAR artifacts
  - Reports build status

**Key jobs**:
- Build with Maven
- Run Tests
- Generate Test Report
- Upload Artifacts

---

### 2. **Frontend CI** (`frontend-ci.yml`)
- **Triggered**: On push/PR affecting `smart-campus-frontend/` directory
- **What it does**:
  - Sets up Node.js (tests on multiple versions: 18.x, 20.x)
  - Installs dependencies
  - Runs ESLint
  - Builds with Vite
  - Runs tests
  - Uploads build artifacts

**Key jobs**:
- Install dependencies
- Lint code
- Build frontend
- Run tests
- Upload build artifacts

---

### 3. **Full Stack CI** (`ci.yml`)
- **Triggered**: On all push/PR to main branches
- **What it does**:
  - Runs backend checks (build + test)
  - Runs frontend checks (build + test)
  - Code quality checks (secret detection, commit format)
  - Aggregates status report

**Key jobs**:
- Backend Build & Test
- Frontend Build & Test
- Code Quality & Security Check
- Overall Status Report

---

### 4. **Docker Build & Deploy** (`docker-build.yml`)
- **Triggered**: On push to main/develop, tags, and PR
- **What it does**:
  - Builds Docker images for backend and frontend
  - Uses Docker layer caching for efficiency
  - Creates GitHub releases on version tags

**Key jobs**:
- Build Backend Docker image
- Build Frontend Docker image
- Create GitHub Release (on tags)

---

## 🚀 How Workflows Work

### Trigger Events

| Workflow | Push | PR | Tags |
|----------|------|----|----|
| backend-ci.yml | ✅ | ✅ | ✅ |
| frontend-ci.yml | ✅ | ✅ | ✅ |
| ci.yml | ✅ | ✅ | ✅ |
| docker-build.yml | ✅ | ✅ | ✅ |

### Branch Filtering

Workflows only run on:
- `main` - Production branch
- `develop` - Development branch

### Path Filtering

Workflows are path-aware:
- `backend-ci.yml` only runs when `smart-campus/` changes
- `frontend-ci.yml` only runs when `smart-campus-frontend/` changes
- `ci.yml` runs on all changes
- `docker-build.yml` runs on all changes

---

## ✅ Artifacts Generated

### Backend
- Test reports (JUnit format)
- JAR application file
- Build logs

**Location**: `smart-campus/target/`

### Frontend
- Optimized build output
- Test results
- Build logs

**Location**: `smart-campus-frontend/dist/`

### All Workflows
- Stored in GitHub Actions for 90 days
- Can be downloaded for offline review
- Automatically cleaned up

---

## 🔍 Monitoring Workflows

### In GitHub UI
1. Go to repository
2. Click **Actions** tab
3. See all workflow runs
4. Click on any run to see details

### Status Badges

Add to README.md:

```markdown
![Backend CI](https://github.com/YOUR_REPO/actions/workflows/backend-ci.yml/badge.svg)
![Frontend CI](https://github.com/YOUR_REPO/actions/workflows/frontend-ci.yml/badge.svg)
![Full Stack CI](https://github.com/YOUR_REPO/actions/workflows/ci.yml/badge.svg)
```

---

## 📝 Workflow Configuration Details

### Java Setup
- Version: 17
- Distribution: Adopt (Eclipse Foundation)
- Cache: Maven repository cache for faster builds

### Node.js Setup
- Versions tested: 18.x, 20.x
- Cache: npm packages
- Package manager: npm (npm ci for CI)

### Actions Used
- `actions/checkout@v3` - Get source code
- `actions/setup-java@v3` - Setup Java
- `actions/setup-node@v3` - Setup Node.js
- `docker/setup-buildx-action@v2` - Setup Docker Buildx
- `docker/build-push-action@v4` - Build Docker images
- `softprops/action-gh-release@v1` - Create releases

---

## 🔐 Security Considerations

### Secrets Detection
- Workflow scans code for exposed secrets
- Uses TruffleHog for secret detection
- Fails if secrets found (configurable)

### Permissions
- Workflows have minimal required permissions
- `contents: read` for code access
- `packages: write` for package registry (optional)

### Environment Variables
- No sensitive data in workflow files
- Use GitHub Secrets for sensitive config
- Can be added via repository settings

---

## 🛠️ Troubleshooting

### Backend Build Fails
1. Check Java version compatibility (need Java 17+)
2. Verify `pom.xml` is valid
3. Check Maven dependencies
4. Review test output in artifacts

**Common fixes**:
```bash
# Local validation
cd smart-campus
mvn clean package
mvn test
```

### Frontend Build Fails
1. Check Node.js version (18+ required)
2. Verify `package.json` and `package-lock.json`
3. Check for missing dependencies
4. Review build logs

**Common fixes**:
```bash
# Local validation
cd smart-campus-frontend
npm ci
npm run build
npm run lint
```

### Workflow Not Triggering
1. Check branch is `main` or `develop`
2. Verify path filters match changed files
3. Ensure `.github/workflows/` files are on the branch
4. Check workflow syntax with `yamllint`

---

## 📊 Performance Optimization

### Caching
- **Maven**: `~/.m2/repository` cached
- **npm**: `node_modules/` cached
- **GitHub Actions**: Layer caching for Docker builds

### Parallel Jobs
- Backend and Frontend build in parallel
- Saves ~5-10 minutes per workflow run

### Conditional Steps
- Some steps only run on success
- Some steps continue on error
- Artifact upload only on success

---

## 🚀 CI/CD Best Practices Implemented

✅ **Automated Testing** - Tests run on every change
✅ **Build Verification** - Both backend and frontend build
✅ **Artifact Caching** - Faster builds with caching
✅ **Path Filtering** - Workflows don't run unnecessarily
✅ **Multi-version Testing** - Tests on multiple Node.js versions
✅ **Quality Checks** - Linting and secret detection
✅ **Comprehensive Logging** - Full logs for debugging
✅ **Status Aggregation** - Overall status report
✅ **Docker Builds** - Container images generated
✅ **Release Management** - Automated releases on tags

---

## 📚 References

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Maven Build Tool](https://maven.apache.org/)
- [Node.js Best Practices](https://nodejs.org/)
- [Docker Workflows](https://docs.docker.com/ci-cd/github-actions/)

---

## 🎯 Next Steps

1. **Push to GitHub**
   ```bash
   git add .github/workflows/
   git commit -m "ci: add GitHub Actions workflows"
   git push origin main
   ```

2. **Monitor First Run**
   - Go to Actions tab
   - Watch workflows execute
   - Check for any failures

3. **Add Status Badges**
   - Add badge markdown to README.md
   - Shows build status at a glance

4. **Configure Secrets** (if needed)
   - Go to Settings → Secrets
   - Add any required secrets
   - Reference in workflows with `${{ secrets.SECRET_NAME }}`

---

**Last Updated**: April 2026
**Status**: Production Ready ✅

