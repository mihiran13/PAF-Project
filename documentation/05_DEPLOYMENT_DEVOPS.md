# Deployment & DevOps Guide

**Project**: Smart Campus Operations Hub  
**Version**: 1.0  
**Date**: April 27, 2026

---

## 1. Deployment Architecture

### 1.1 Environment Tiers

```
┌─────────────────────────────────────────────────────────────┐
│                    PRODUCTION (AWS)                         │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Frontend: CloudFront > S3 (React SPA)                     │
│  Backend:  ALB > ECS > Spring Boot Container               │
│  Database: RDS MySQL (Multi-AZ, automated backup)          │
│  Cache:    ElastiCache Redis                               │
│  Storage:  S3 (file uploads)                               │
│  Logging:  CloudWatch                                      │
│  CDN:      CloudFront                                      │
│                                                             │
└──────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                   STAGING (AWS)                             │
├─────────────────────────────────────────────────────────────┤
│ Mirrors production setup for pre-release testing            │
└──────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                DEVELOPMENT (Local)                          │
├─────────────────────────────────────────────────────────────┤
│ Docker Compose: Backend + Frontend + MySQL                  │
└──────────────────────────────────────────────────────────────┘
```

---

## 2. Local Development Setup

### 2.1 Prerequisites

- Docker Desktop (or Docker Engine + Docker Compose)
- Java 17+ (for local backend development)
- Node.js 18.x+ (for local frontend development)
- Git
- MySQL 8.0+ (if running outside Docker)

### 2.2 Quick Start with Docker Compose

```bash
# Clone repository
git clone https://github.com/your-org/smart-campus.git
cd smart-campus

# Start all services
docker-compose up -d

# Verify services
docker-compose ps

# View logs
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f mysql
```

### 2.3 Docker Compose Configuration

**File**: `docker-compose.yml`

```yaml
version: '3.8'

services:
  # MySQL Database
  mysql:
    image: mysql:8.0.35
    container_name: smart-campus-db
    environment:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: smart_campus_db
      MYSQL_USER: campus_user
      MYSQL_PASSWORD: campus_password
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./scripts/init.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - smart-campus-network

  # Spring Boot Backend
  backend:
    build:
      context: ./smart-campus
      dockerfile: Dockerfile
    container_name: smart-campus-backend
    ports:
      - "8081:8081"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/smart_campus_db
      SPRING_DATASOURCE_USERNAME: campus_user
      SPRING_DATASOURCE_PASSWORD: campus_password
      SPRING_JPA_HIBERNATE_DDL_AUTO: update
      GOOGLE_CLIENT_ID: ${GOOGLE_CLIENT_ID}
      GOOGLE_CLIENT_SECRET: ${GOOGLE_CLIENT_SECRET}
      JWT_SECRET: ${JWT_SECRET}
    depends_on:
      mysql:
        condition: service_healthy
    networks:
      - smart-campus-network

  # React Frontend
  frontend:
    build:
      context: ./smart-campus-frontend
      dockerfile: Dockerfile
    container_name: smart-campus-frontend
    ports:
      - "3000:3000"
    environment:
      VITE_API_BASE_URL: http://backend:8081/api/v1
      VITE_GOOGLE_CLIENT_ID: ${GOOGLE_CLIENT_ID}
    depends_on:
      - backend
    networks:
      - smart-campus-network

volumes:
  mysql_data:

networks:
  smart-campus-network:
    driver: bridge
```

### 2.4 Environment Variables (.env.local)

```bash
# Google OAuth
GOOGLE_CLIENT_ID=your_google_client_id_here
GOOGLE_CLIENT_SECRET=your_google_client_secret_here

# JWT Secret (minimum 32 characters)
JWT_SECRET=your-256-bit-secret-key-here-must-be-at-least-32-characters

# Database
MYSQL_ROOT_PASSWORD=root_password
MYSQL_DATABASE=smart_campus_db
MYSQL_USER=campus_user
MYSQL_PASSWORD=campus_password

# API Configuration
BACKEND_PORT=8081
FRONTEND_PORT=3000
API_BASE_URL=http://localhost:8081/api/v1
```

---

## 3. Backend Deployment

### 3.1 Building Docker Image

**Backend Dockerfile**:

```dockerfile
# Stage 1: Build
FROM maven:3.9.2-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src .
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar application.jar

EXPOSE 8081
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8081/api/v1/health || exit 1

ENTRYPOINT ["java", "-jar", "application.jar"]
```

### 3.2 Building & Pushing to Registry

```bash
# Build image
docker build -t smart-campus-backend:1.0.0 ./smart-campus

# Tag for registry
docker tag smart-campus-backend:1.0.0 \
  your-registry.azurecr.io/smart-campus-backend:1.0.0

# Push to registry
docker push your-registry.azurecr.io/smart-campus-backend:1.0.0

# Verify
docker images | grep smart-campus-backend
```

### 3.3 AWS ECS Deployment

**Task Definition** (smart-campus-backend-task.json):

```json
{
  "family": "smart-campus-backend",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "containerDefinitions": [
    {
      "name": "backend",
      "image": "YOUR_REGISTRY/smart-campus-backend:1.0.0",
      "portMappings": [
        {
          "containerPort": 8081,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_DATASOURCE_URL",
          "value": "jdbc:mysql://rds-endpoint:3306/smart_campus_db"
        },
        {
          "name": "SPRING_DATASOURCE_USERNAME",
          "value": "campus_user"
        }
      ],
      "secrets": [
        {
          "name": "SPRING_DATASOURCE_PASSWORD",
          "valueFrom": "arn:aws:secretsmanager:region:account:secret:rds-password"
        },
        {
          "name": "JWT_SECRET",
          "valueFrom": "arn:aws:secretsmanager:region:account:secret:jwt-secret"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/smart-campus-backend",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      },
      "healthCheck": {
        "command": ["CMD-SHELL", "curl -f http://localhost:8081/api/v1/health || exit 1"],
        "interval": 30,
        "timeout": 5,
        "retries": 3,
        "startPeriod": 10
      }
    }
  ]
}
```

**Deploy to ECS**:

```bash
# Register task definition
aws ecs register-task-definition \
  --cli-input-json file://smart-campus-backend-task.json

# Update service
aws ecs update-service \
  --cluster smart-campus \
  --service smart-campus-backend \
  --force-new-deployment
```

### 3.4 Database Migration (Flyway)

**Migration Scripts** (src/main/resources/db/migration/):

```sql
-- V1__Initial_Schema.sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(255) UNIQUE NOT NULL,
  name VARCHAR(255) NOT NULL,
  role ENUM('USER', 'ADMIN', 'TECHNICIAN') DEFAULT 'USER',
  avatar_url TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ... (other tables)
```

**Automatic Migration**:

```yaml
# application.yml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
```

---

## 4. Frontend Deployment

### 4.1 Building React Application

```bash
# Install dependencies
npm install

# Build for production
npm run build

# Output: dist/ folder with optimized build
ls -la dist/
```

### 4.2 Frontend Dockerfile

```dockerfile
# Stage 1: Build
FROM node:18-alpine as builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# Stage 2: Runtime (Nginx)
FROM nginx:latest
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### 4.3 Nginx Configuration

**File**: `nginx.conf`

```nginx
worker_processes auto;

events {
  worker_connections 1024;
}

http {
  mime_types {
    text/html html htm;
    text/css css;
    application/javascript js;
    image/svg+xml svg;
    application/json json;
  }

  server {
    listen 80;
    server_name _;

    gzip on;
    gzip_types text/html text/css application/javascript;

    root /usr/share/nginx/html;
    index index.html;

    location / {
      try_files $uri /index.html;
    }

    location ~* \.(js|css|png|jpg|gif|ico|svg)$ {
      expires 1y;
      add_header Cache-Control "public, immutable";
    }

    # API proxy
    location /api/ {
      proxy_pass http://backend:8081;
      proxy_set_header Host $host;
      proxy_set_header X-Real-IP $remote_addr;
      proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
      proxy_set_header X-Forwarded-Proto $scheme;
    }
  }
}
```

### 4.4 Deploy to S3 + CloudFront

```bash
# Build
npm run build

# Upload to S3
aws s3 sync dist/ s3://smart-campus-frontend/ \
  --delete \
  --cache-control "public, max-age=3600"

# Invalidate CloudFront cache
aws cloudfront create-invalidation \
  --distribution-id E27EXAMPLE51Z \
  --paths '/*'
```

---

## 5. Database Deployment

### 5.1 AWS RDS MySQL Setup

```bash
# Create RDS instance
aws rds create-db-instance \
  --db-instance-identifier smart-campus-db \
  --db-instance-class db.t3.small \
  --engine mysql \
  --engine-version 8.0.35 \
  --master-username campus_admin \
  --master-user-password "YourSecurePassword123!" \
  --allocated-storage 100 \
  --storage-type gp3 \
  --backup-retention-period 30 \
  --enable-multi-az \
  --enable-iam-database-authentication
```

### 5.2 Connection from ECS

```bash
# Install mysql-client locally
brew install mysql-client

# Connect to RDS
mysql -h smart-campus-db.xxx.us-east-1.rds.amazonaws.com \
  -u campus_admin -p

# Verify connection
mysql> SELECT VERSION();
```

### 5.3 Backup Strategy

```bash
# Automated backup via AWS
# Retention: 30 days
# Backup window: 03:00-04:00 UTC

# Manual backup
aws rds create-db-snapshot \
  --db-instance-identifier smart-campus-db \
  --db-snapshot-identifier smart-campus-backup-$(date +%Y%m%d)

# Restore from backup
aws rds restore-db-instance-from-db-snapshot \
  --db-instance-identifier smart-campus-db-restored \
  --db-snapshot-identifier smart-campus-backup-20260427
```

---

## 6. CI/CD Pipeline (GitHub Actions)

### 6.1 Workflow Files

**Backend CI Workflow**:

```yaml
# .github/workflows/backend-ci.yml
name: Backend CI
on:
  push:
    branches: [main, develop]
    paths: ['smart-campus/**']
  pull_request:
    branches: [main, develop]

jobs:
  build:
    runs-on: ubuntu-latest
    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: root
        options: >-
          --health-cmd="mysqladmin ping"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=3

    steps:
      - uses: actions/checkout@v3
      - name: Set up Java
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven

      - name: Build with Maven
        run: mvn -B clean package
        working-directory: smart-campus

      - name: Run tests
        run: mvn test
        working-directory: smart-campus

      - name: Upload coverage
        uses: codecov/codecov-action@v3
        with:
          files: smart-campus/target/site/jacoco/jacoco.xml

      - name: Build Docker image
        run: docker build -t smart-campus-backend:${{ github.sha }} smart-campus

      - name: Push to registry
        run: |
          echo ${{ secrets.REGISTRY_PASSWORD }} | docker login -u ${{ secrets.REGISTRY_USERNAME }} --password-stdin
          docker tag smart-campus-backend:${{ github.sha }} ${{ secrets.REGISTRY }}/smart-campus-backend:latest
          docker push ${{ secrets.REGISTRY }}/smart-campus-backend:latest
```

### 6.2 Deployment Workflow

```yaml
# .github/workflows/deploy.yml
name: Deploy to Production
on:
  push:
    tags: ['v*']

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Deploy to AWS ECS
        run: |
          aws ecs update-service \
            --cluster smart-campus \
            --service smart-campus-backend \
            --force-new-deployment
        env:
          AWS_ACCESS_KEY_ID: ${{ secrets.AWS_ACCESS_KEY_ID }}
          AWS_SECRET_ACCESS_KEY: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          AWS_DEFAULT_REGION: us-east-1

      - name: Verify deployment
        run: |
          sleep 30
          curl -f http://smart-campus-api.example.com/api/v1/health || exit 1
```

---

## 7. Monitoring & Logging

### 7.1 CloudWatch Monitoring

```bash
# Create log group
aws logs create-log-group --log-group-name /smart-campus/backend

# Create CloudWatch alarm
aws cloudwatch put-metric-alarm \
  --alarm-name smart-campus-error-rate \
  --alarm-description "Alert if error rate > 5%" \
  --metric-name ErrorCount \
  --namespace AWS/ECS \
  --statistic Sum \
  --period 300 \
  --evaluation-periods 1 \
  --threshold 50 \
  --comparison-operator GreaterThanThreshold \
  --alarm-actions arn:aws:sns:us-east-1:ACCOUNT:alerts
```

### 7.2 Application Logging

```java
@Slf4j
@RestController
public class BookingController {
  
  @PostMapping("/bookings")
  public ResponseEntity<?> createBooking(@RequestBody BookingRequest req) {
    log.info("Creating booking for resource: {}", req.getResourceId());
    try {
      BookingResponse response = bookingService.createBooking(req);
      log.info("Booking created successfully: {}", response.getId());
      return ResponseEntity.status(201).body(response);
    } catch (Exception e) {
      log.error("Error creating booking", e);
      throw e;
    }
  }
}
```

### 7.3 CloudWatch Dashboard

```bash
# Create dashboard
aws cloudwatch put-dashboard \
  --dashboard-name SmartCampusDashboard \
  --dashboard-body file://dashboard.json
```

---

## 8. Scaling & Performance Tuning

### 8.1 Auto-Scaling Configuration

```bash
# Register scalable target
aws application-autoscaling register-scalable-target \
  --service-namespace ecs \
  --resource-id service/smart-campus/smart-campus-backend \
  --scalable-dimension ecs:service:DesiredCount \
  --min-capacity 2 \
  --max-capacity 10

# Create scaling policy
aws application-autoscaling put-scaling-policy \
  --policy-name smart-campus-scaling \
  --service-namespace ecs \
  --resource-id service/smart-campus/smart-campus-backend \
  --scalable-dimension ecs:service:DesiredCount \
  --policy-type TargetTrackingScaling \
  --target-tracking-scaling-policy-configuration file://scaling-policy.json
```

### 8.2 Database Optimization

```sql
-- Add indexes for common queries
CREATE INDEX idx_booking_resource_status ON bookings(resource_id, status);
CREATE INDEX idx_booking_user_status ON bookings(user_id, status);
CREATE INDEX idx_ticket_status ON tickets(status);
CREATE INDEX idx_ticket_assigned_to ON tickets(assigned_to);

-- Enable query cache
SET GLOBAL query_cache_type = 1;
SET GLOBAL query_cache_size = 268435456; -- 256MB
```

---

## 9. Rollback Procedures

### 9.1 ECS Service Rollback

```bash
# Rollback to previous task definition
aws ecs update-service \
  --cluster smart-campus \
  --service smart-campus-backend \
  --task-definition smart-campus-backend:5 \
  --force-new-deployment
```

### 9.2 Database Rollback

```bash
# Restore from snapshot
aws rds restore-db-instance-from-db-snapshot \
  --db-instance-identifier smart-campus-db-restored \
  --db-snapshot-identifier smart-campus-backup-20260426

# Promote read replica (if configured)
aws rds promote-read-replica \
  --db-instance-identifier smart-campus-read-replica
```

---

## 10. Disaster Recovery

### 10.1 RTO/RPO Targets

- **RTO** (Recovery Time Objective): 4 hours
- **RPO** (Recovery Point Objective): 1 hour

### 10.2 Backup Verification

```bash
# Monthly restore test
aws rds restore-db-instance-from-db-snapshot \
  --db-instance-identifier smart-campus-dr-test \
  --db-snapshot-identifier smart-campus-backup-latest

# Verify data integrity
mysql -h smart-campus-dr-test.xxx.rds.amazonaws.com -u campus_admin -p -e \
  "SELECT COUNT(*) FROM users; SELECT COUNT(*) FROM bookings; SELECT COUNT(*) FROM tickets;"

# Clean up test instance
aws rds delete-db-instance \
  --db-instance-identifier smart-campus-dr-test \
  --skip-final-snapshot
```

---

## 11. Security Best Practices

### 11.1 Secrets Management

```bash
# Store secrets in AWS Secrets Manager
aws secretsmanager create-secret \
  --name smart-campus/jwt-secret \
  --secret-string "your-secret-key-here"

# Retrieve in application
aws secretsmanager get-secret-value \
  --secret-id smart-campus/jwt-secret
```

### 11.2 Network Security

- VPC with private subnets for RDS
- Security groups restricting inbound traffic
- WAF (Web Application Firewall) for DDoS protection
- HTTPS/TLS for all communications

### 11.3 SSL/TLS Certificate

```bash
# Request certificate from ACM
aws acm request-certificate \
  --domain-name smart-campus.example.com \
  --validation-method DNS \
  --subject-alternative-names "*.smart-campus.example.com"
```

---

## 12. Troubleshooting & Support

### 12.1 Common Deployment Issues

| Issue | Cause | Solution |
|-------|-------|----------|
| ECS service won't start | Out of memory | Increase task memory in task definition |
| Database connection timeout | RDS not in same VPC | Update security group rules |
| Frontend fails to load | API CORS issue | Update CORS in Spring Security |
| High latency | Unoptimized queries | Add database indexes |

### 12.2 Support Contacts

- **AWS Support**: AWS Console > Support
- **GitHub Actions**: Debug logs in workflow runs
- **Application Issues**: Check CloudWatch logs

---

**Deployment Version**: 1.0  
**Last Updated**: April 27, 2026
