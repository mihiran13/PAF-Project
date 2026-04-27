# Smart Campus - Deployment Guide

## Prerequisites

### System Requirements
- Java 17+ (for backend)
- Node.js 18+ (for frontend)
- PostgreSQL 12+ (for database)
- Maven 3.8+ (for building backend)
- npm or yarn (for frontend dependencies)

### Environment Setup

#### Database Setup (PostgreSQL)
```sql
CREATE DATABASE smart_campus;
CREATE USER smart_campus_user WITH PASSWORD 'secure_password';
ALTER ROLE smart_campus_user SET client_encoding TO 'utf8';
ALTER ROLE smart_campus_user SET default_transaction_isolation TO 'read committed';
ALTER ROLE smart_campus_user SET default_transaction_deferrable TO ON;
ALTER ROLE smart_campus_user SET timezone TO 'UTC';
GRANT ALL PRIVILEGES ON DATABASE smart_campus TO smart_campus_user;
```

---

## Backend Deployment

### 1. Build the Backend
```bash
cd smart-campus
mvn clean package -DskipTests
```

### 2. Configure Application Properties
Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/smart_campus
    username: smart_campus_user
    password: secure_password
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://accounts.google.com
          jwk-set-uri: https://www.googleapis.com/oauth2/v3/certs

server:
  port: 8080
  servlet:
    context-path: /

app:
  cors:
    allowed-origins: 
      - http://localhost:5173
      - http://localhost:3000
      - https://yourdomain.com
  google:
    client-id: YOUR_GOOGLE_CLIENT_ID
    client-secret: YOUR_GOOGLE_CLIENT_SECRET
```

### 3. Run the Backend
```bash
# Development
mvn spring-boot:run

# Production (using JAR)
java -jar target/smart-campus-application.jar
```

### 4. Verify Backend
```bash
curl http://localhost:8080/api/auth/me
# Should return 401 Unauthorized (expected without token)
```

---

## Frontend Deployment

### 1. Install Dependencies
```bash
cd smart-campus-frontend
npm install
```

### 2. Configure Environment
Create `.env.local`:

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_GOOGLE_CLIENT_ID=YOUR_GOOGLE_CLIENT_ID
VITE_APP_NAME=Smart Campus
```

For production `.env.production`:
```env
VITE_API_BASE_URL=https://api.yourdomain.com
VITE_GOOGLE_CLIENT_ID=YOUR_GOOGLE_CLIENT_ID
VITE_APP_NAME=Smart Campus
```

### 3. Build the Frontend
```bash
npm run build
```

### 4. Run Locally (Development)
```bash
npm run dev
```

### 5. Preview Production Build
```bash
npm run preview
```

---

## Docker Deployment

### Backend Dockerfile
```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/smart-campus-application.jar .

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "smart-campus-application.jar"]
```

### Frontend Dockerfile
```dockerfile
FROM node:18-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM nginx:alpine

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

### Docker Compose
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: smart_campus
      POSTGRES_USER: smart_campus_user
      POSTGRES_PASSWORD: secure_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  backend:
    build: ./smart-campus
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/smart_campus
      SPRING_DATASOURCE_USERNAME: smart_campus_user
      SPRING_DATASOURCE_PASSWORD: secure_password
    ports:
      - "8080:8080"
    depends_on:
      - postgres

  frontend:
    build: ./smart-campus-frontend
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  postgres_data:
```

---

## Azure Deployment

### Backend - Azure App Service
```bash
# 1. Create resource group
az group create --name smart-campus-rg --location eastus

# 2. Create PostgreSQL server
az postgres flexible-server create \
  --resource-group smart-campus-rg \
  --name smart-campus-db \
  --admin-user smart_campus_user \
  --admin-password secure_password

# 3. Create App Service Plan
az appservice plan create \
  --name smart-campus-plan \
  --resource-group smart-campus-rg \
  --sku B2 --is-linux

# 4. Create App Service
az webapp create \
  --resource-group smart-campus-rg \
  --plan smart-campus-plan \
  --name smart-campus-backend \
  --runtime "JAVA|17-java17"

# 5. Deploy JAR
az webapp deploy \
  --resource-group smart-campus-rg \
  --name smart-campus-backend \
  --src-path target/smart-campus-application.jar
```

### Frontend - Azure Static Web Apps
```bash
# 1. Create Static Web App
az staticwebapp create \
  --name smart-campus-frontend \
  --resource-group smart-campus-rg \
  --source https://github.com/yourusername/smart-campus-frontend \
  --branch main \
  --login-with-github

# Configure build settings in staticwebapp.config.json
```

---

## Cloud Deployment - AWS

### Backend - EC2
```bash
# 1. Launch EC2 instance (Ubuntu 22.04 LTS)
# 2. SSH into instance
ssh -i key.pem ubuntu@instance-ip

# 3. Install Java
sudo apt update
sudo apt install openjdk-17-jre-headless

# 4. Install PostgreSQL
sudo apt install postgresql postgresql-contrib

# 5. Upload and run JAR
scp -i key.pem target/smart-campus-application.jar ubuntu@instance-ip:~/
ssh -i key.pem ubuntu@instance-ip
java -jar smart-campus-application.jar
```

### Frontend - S3 + CloudFront
```bash
# Build frontend
npm run build

# Upload to S3
aws s3 sync dist/ s3://smart-campus-frontend/

# Create CloudFront distribution
aws cloudfront create-distribution --origin-domain-name smart-campus-frontend.s3.amazonaws.com
```

---

## Environment Variables

### Backend
| Variable | Required | Description |
|----------|----------|-------------|
| `SPRING_DATASOURCE_URL` | Yes | Database connection URL |
| `SPRING_DATASOURCE_USERNAME` | Yes | Database username |
| `SPRING_DATASOURCE_PASSWORD` | Yes | Database password |
| `APP_GOOGLE_CLIENT_ID` | Yes | Google OAuth client ID |
| `APP_GOOGLE_CLIENT_SECRET` | Yes | Google OAuth client secret |
| `APP_CORS_ALLOWED_ORIGINS` | Yes | Allowed CORS origins |
| `SERVER_PORT` | No | Server port (default: 8080) |

### Frontend
| Variable | Required | Description |
|----------|----------|-------------|
| `VITE_API_BASE_URL` | Yes | Backend API URL |
| `VITE_GOOGLE_CLIENT_ID` | Yes | Google OAuth client ID |
| `VITE_APP_NAME` | No | Application name |

---

## Health Checks

### Backend Health
```bash
curl http://localhost:8080/actuator/health
```

### Frontend Health
Check if application loads at configured URL.

---

## Monitoring & Logging

### Backend Logs
```bash
# Development
tail -f backend_log.txt

# Production
docker logs -f smart-campus-backend
```

### Database Monitoring
```bash
# Connect to PostgreSQL
psql -U smart_campus_user -d smart_campus

# Check database size
\l+

# Monitor active connections
SELECT * FROM pg_stat_activity;
```

---

## Security Considerations

1. **API Security**
   - Use HTTPS in production
   - Implement rate limiting
   - Set up firewall rules

2. **Database Security**
   - Use strong passwords
   - Enable SSL connections
   - Regular backups

3. **OAuth Configuration**
   - Use environment variables for secrets
   - Regular key rotation
   - Monitor unauthorized access attempts

4. **CORS Configuration**
   - Only allow trusted origins
   - Avoid using '*' wildcard in production

---

## Troubleshooting

### Database Connection Issues
```bash
# Test PostgreSQL connection
psql -h localhost -U smart_campus_user -d smart_campus

# Check connection string format
springdjdbc:postgresql://host:port/database
```

### CORS Errors
1. Verify allowed origins in backend configuration
2. Check browser console for specific origin URL
3. Ensure request headers are correct

### Google OAuth Issues
1. Verify client ID and secret are correct
2. Check authorized redirect URIs in Google Console
3. Ensure cookies are enabled in browser

### Build Issues
```bash
# Clear Maven cache
mvn clean

# Rebuild with verbose output
mvn clean package -X

# Clear Node cache
rm -rf node_modules package-lock.json
npm install
```

---

## Performance Optimization

### Backend
1. Enable query pagination
2. Use connection pooling
3. Add database indexes
4. Cache frequently accessed data
5. Monitor slow queries

### Frontend
1. Enable code splitting
2. Lazy load components
3. Compress assets
4. Use CDN for static files
5. Implement service workers

---

## Backup & Recovery

### Database Backup
```bash
# Backup PostgreSQL
pg_dump -U smart_campus_user smart_campus > backup.sql

# Restore from backup
psql -U smart_campus_user smart_campus < backup.sql
```

### Application Backup
- Version control all source code
- Store environment files securely
- Document configuration changes

---

## Support & Resources

- Backend Repository: `smart-campus/`
- Frontend Repository: `smart-campus-frontend/`
- Documentation: `/smart-campus/README.md`
- API Documentation: `/smart-campus/target/classes/openapi.yaml`

---

**Deployment Status**: Ready for Production

**Last Updated**: 2026

