# GovShield - System Architecture

## Architecture Overview

GovShield follows a layered, microservices-oriented architecture designed for scalability, security, and maintainability.

## System Components

### 1. Frontend Layer
- **Technology**: HTML5, CSS3, Vanilla JavaScript
- **Components**:
  - Landing page and authentication UI
  - Citizen dashboard
  - Officer management dashboard
  - Auditor analytics dashboard
  - Responsive design for desktop and tablet

### 2. API Layer (REST)
- **Technology**: Spring Boot REST APIs
- **Features**:
  - JWT authentication
  - CORS support
  - Request validation
  - Error handling
  - API versioning (future)

### 3. Business Logic Layer
- **Services**:
  - `AuthService`: Authentication and JWT token management
  - `CitizenService`: Citizen registration and profile management
  - `SchemeService`: Scheme CRUD operations
  - `EligibilityService`: Eligibility verification engine
  - `FraudDetectionService`: Pattern recognition and risk analysis
  - `ProjectMonitoringService`: Project tracking and fund management
  - `AuditService`: Audit logging and compliance

### 4. Data Access Layer
- **Technology**: Spring Data JPA with Hibernate
- **Pattern**: Repository pattern
- **Repositories**:
  - CitizenRepository
  - SchemeRepository
  - EnrollmentRepository
  - ProjectRepository
  - GovEmployeeRepository
  - AuditLogRepository

### 5. Database Layer
- **Technology**: MySQL 8.0
- **Design**: Relational database with proper indexing
- **Key Tables**:
  - citizens
  - gov_employees
  - schemes
  - enrollments
  - projects
  - audit_logs

### 6. Security Layer
- **Authentication**: JWT tokens
- **Authorization**: Role-based access control (RBAC)
- **Encryption**: BCrypt for password hashing
- **Transport Security**: HTTPS/TLS

### 7. Infrastructure Layer
- **Containerization**: Docker
- **Orchestration**: Docker Compose
- **Reverse Proxy**: Nginx
- **Web Server**: Embedded Tomcat in Spring Boot
- **CI/CD**: Jenkins Pipeline

## Architecture Diagram

```
┌─────────────────────────────────────────────────────┐
│                   Client Layer                      │
│  ┌────────────┬──────────────┬──────────────┐       │
│  │  Citizen   │   Officer    │   Auditor    │       │
│  │ Dashboard  │  Dashboard   │  Dashboard   │       │
│  └────────────┴──────────────┴──────────────┘       │
└────────────────────┬────────────────────────────────┘
                     │
         ┌───────────┴───────────┐
         │                       │
┌────────▼────────┐    ┌─────────▼──────────┐
│   Nginx         │    │  Static Assets     │
│ (Reverse Proxy) │    │  (HTML, CSS, JS)   │
└────────┬────────┘    └────────┬───────────┘
         │                      │
         └──────────┬───────────┘
                    │
┌───────────────────▼─────────────────────┐
│       REST API Layer (Spring Boot)      │
├─────────────────────────────────────────┤
│  Auth │ Citizen │ Scheme │ Eligibility  │
│ Fraud │ Project │ Audit Controllers     │
└───────────────────┬─────────────────────┘
                    │
┌───────────────────▼─────────────────────┐
│    Business Logic Layer (Services)      │
├─────────────────────────────────────────┤
│  AuthService      │    CitizenService   │
│  SchemeService    │ EligibilityService  │
│ FraudDetection    │ ProjectMonitoring   │
└───────────────────┬─────────────────────┘
                    │
┌───────────────────▼─────────────────────┐
│      Utility & Helper Layer             │
├─────────────────────────────────────────┤
│ UGIDGenerator │ EligibilityRules        │
│ RiskCalculator                          │
└───────────────────┬─────────────────────┘
                    │
┌───────────────────▼─────────────────────┐
│   Data Access Layer (Repositories)      │
├─────────────────────────────────────────┤
│   JPA Repositories with Hibernate ORM   │
└───────────────────┬─────────────────────┘
                    │
┌───────────────────▼─────────────────────┐
│      Database Layer (MySQL)             │
├─────────────────────────────────────────┤
│      Citizens │ Schemes │ Enrollments   │
│    Projects   │ Employees │ Audit Logs  │
└─────────────────────────────────────────┘
```

## Data Flow

### Scheme Application Process

```
1. Citizen Login
   ↓
2. Browse Available Schemes
   ↓
3. Submit Application (POST /api/eligibility/apply)
   ↓
4. EligibilityService.applyForScheme()
   ├─ Fetch citizen & scheme from DB
   ├─ Apply EligibilityRules
   ├─ Calculate FraudRisk
   ├─ Create Enrollment record
   └─ Return status (ELIGIBLE/INELIGIBLE/FLAGGED)
   ↓
5. Store Enrollment in Database
   ↓
6. Return Response to Client
   ↓
7. Log Action in AuditLog
```

### Fraud Detection Process

```
1. New Enrollment Created
   ↓
2. FraudDetectionService.calculateFraudRisk()
   ├─ Analyze citizen's enrollment history
   ├─ Check for duplicate sector enrollments
   ├─ Check for rapid successive applications
   ├─ Evaluate income/employment consistency
   └─ Calculate risk score
   ↓
3. Risk Level Assignment
   ├─ HIGH (≥50): Automatic flag for audit
   ├─ MEDIUM (30-49): Require verification
   └─ LOW (<30): Auto-approve if eligible
   ↓
4. Alert Dashboard
   └─ Update fraud alerts in real-time
```

## Security Architecture

### Authentication Flow

```
Login Request
    ↓
AuthService.authenticate()
    ├─ Validate credentials against gov_employees
    ├─ Verify BCrypt password hash
    ├─ Check account active status
    └─ Generate JWT token
    ↓
Return token with role & expiration
    ↓
Client stores token in localStorage
    ↓
Subsequent requests include: Authorization: Bearer {token}
    ↓
Security filter validates & decodes JWT
    ├─ Check signature validity
    ├─ Check expiration
    └─ Extract claims (email, role)
    ↓
Allow request → Route to controller
```

### Authorization Flow

```
API Request with JWT
    ↓
Extract role from token claims
    ↓
Check endpoint access permissions
    ├─ PUBLIC: auth/login
    ├─ CITIZEN: scheme application, profile
    ├─ OFFICER: enrollment management, project updates
    ├─ AUDITOR: all read operations, reports
    └─ ADMIN: all operations
    ↓
Allow/Deny request
```

## Database Schema Overview

### Citizens Table
- Stores citizen information with UGID
- Linked to scheme enrollments
- Tracks employment and income status

### Schemes Table
- Master data for available schemes
- Eligibility criteria (income, age limits)
- Sector classification

### Enrollments Table
- Application records with status tracking
- Links citizen to scheme
- Stores eligibility and fraud risk assessment
- Audit trail via created_at/updated_at

### Projects Table
- Government project information
- Budget and expenditure tracking
- Progress and quality metrics
- MLA/MP allocation

### Audit Logs Table
- Complete record of all operations
- Action, entity type, performer
- Timestamp and status
- Used for compliance and accountability

## Technology Stack Details

### Backend
- **Framework**: Spring Boot 3.1.5
- **Database**: MySQL 8.0 with InnoDB
- **Data Access**: Spring Data JPA + Hibernate
- **Security**: Spring Security + JWT
- **Validation**: Hibernate Validator
- **API Documentation**: Springdoc OpenAPI (Swagger)
- **Build**: Maven 3.8+
- **Java**: OpenJDK 17

### Frontend
- **HTML5**: Semantic markup
- **CSS3**: Responsive design, Flexbox/Grid
- **JavaScript**: Vanilla JS (no frameworks) for simplicity
- **HTTP Client**: Fetch API

### DevOps & Infrastructure
- **Containerization**: Docker
- **Container Orchestration**: Docker Compose
- **Web Server**: Nginx (reverse proxy, load balancing)
- **Application Server**: Apache Tomcat (embedded in Spring Boot)
- **CI/CD**: Jenkins with declarative pipelines
- **Version Control**: Git

## Scalability Considerations

1. **Database Scaling**
   - Master-slave replication
   - Query optimization with proper indexing
   - Connection pooling (HikariCP)

2. **Application Scaling**
   - Stateless REST APIs (horizontal scaling)
   - Load balancing via Nginx
   - Caching strategies (in-memory + Redis future)

3. **API Rate Limiting**
   - Per-IP rate limiting in Nginx
   - Stricter limits for login endpoint

## Performance Optimization

1. **Database Indexes** on:
   - ugid, aadhaar, email (citizens)
   - scheme_code (schemes)
   - citizen_id, scheme_id (enrollments)
   - enrollment_number
   - project_code

2. **Caching**
   - HTTP caching headers for static assets
   - API response caching (future implementation)

3. **Compression**
   - Gzip compression for responses > 1KB
   - Minified JavaScript and CSS

4. **Connection Management**
   - Database connection pooling
   - Keep-alive connections between Nginx and backend

## Disaster Recovery

1. **Data Backup**
   - Daily MySQL backups
   - Version control for code

2. **High Availability**
   - Docker Compose health checks
   - Automatic container restart policies

3. **Monitoring**
   - Nginx health endpoint
   - Application logs
   - Database query logging

## Future Architecture Enhancements

1. **Microservices Migration**
   - Separate services for different domains
   - Service discovery and mesh

2. **Message Queues**
   - Asynchronous processing with RabbitMQ/Kafka
   - Event-driven architecture

3. **Blockchain Integration**
   - Immutable audit logs
   - Smart contracts for fund release

4. **Machine Learning**
   - Advanced fraud detection
   - Predictive analytics

5. **Cloud Deployment**
   - Kubernetes orchestration
   - Cloud provider managed services (AWS, GCP, Azure)

---

**Document Version**: 1.0  
**Created**: February 2024  
**Last Updated**: February 2024
