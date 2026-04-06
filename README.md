# GovShield - Unified Beneficiary & Corruption Monitoring Platform

Welcome to GovShield, a comprehensive full-stack Java-based platform designed to help the Indian government identify and prevent misuse of welfare schemes and corruption in public-funded projects.

## Overview

GovShield introduces a **Unique GovShield ID (UGID)** linked logically to Aadhaar, PAN, employment status, and family records without replacing Aadhaar. The system features:

- **Unified Beneficiary Database**: All government schemes virtually connected through a unified database layer
- **Rule-Based Eligibility Engine**: Verifies applicant eligibility and prevents duplicate benefits
- **Advanced Fraud Detection**: Identifies suspicious patterns and flags potential fraud cases
- **Public Project Monitoring**: Tracks government-funded projects with transparency in budgets and progress
- **Real-Time Dashboards**: Separate interfaces for citizens, government officers, and auditors
- **Comprehensive Audit Trails**: Complete accountability and transparent reporting

## Technology Stack

- **Backend**: Spring Boot 3.1.5, Spring Data JPA, Spring Security
- **Authentication**: JWT (JSON Web Tokens)
- **Database**: MySQL 8.0
- **Frontend**: HTML5, CSS3, Vanilla JavaScript
- **API Documentation**: Swagger/OpenAPI 3.0
- **DevOps**: Docker, Docker Compose, Jenkins, Nginx
- **Build Tool**: Maven
- **Java Version**: 17

## Project Structure

```
GovShield/
├── backend/                 # Spring Boot application
├── frontend/               # Web application (HTML, CSS, JS)
├── database/              # Database scripts
└── docs/                  # Project documentation
```

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.8+
- MySQL 8.0+
- Node.js (optional, for frontend only)
- Docker & Docker Compose (for containerized deployment)

### Local Development

#### 1. Database Setup

```bash
# Create MySQL database
mysql -u root -p < database/schema.sql
mysql -u root -p govshield < database/data.sql
```

#### 2. Backend Setup

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The backend will run on `http://localhost:8080/api`

#### 3. Frontend Setup

Open `frontend/index.html` in your browser or serve using a simple HTTP server:

```bash
cd frontend
python -m http.server 8000
# Visit http://localhost:8000
```

### Deployment (Netlify + Render + Railway MySQL)
See:
- `docs/DEPLOY_FROM_SCRATCH.md`
- `docs/ROTATE_PASSWORDS_API_KEYS_AND_URLS.md`

## API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/validate` - Token validation

### Citizens
- `POST /api/citizens/register` - Register new citizen
- `GET /api/citizens/{id}` - Get citizen details
- `GET /api/citizens/ugid/{ugid}` - Get citizen by UGID
- `PUT /api/citizens/{id}` - Update citizen information

### Schemes
- `GET /api/schemes` - Get all schemes
- `GET /api/schemes/{id}` - Get scheme details
- `GET /api/schemes/active/all` - Get active schemes
- `POST /api/schemes` - Create new scheme (Admin only)

### Eligibility & Enrollments
- `POST /api/eligibility/apply` - Apply for scheme
- `POST /api/eligibility/check` - Check eligibility
- `GET /api/eligibility/enrollment/{id}` - Get enrollment details
- `POST /api/eligibility/approve/{id}` - Approve enrollment (Officer)
- `POST /api/eligibility/reject/{id}` - Reject enrollment (Officer)

### Fraud Detection
- `GET /api/fraud/detect/{citizenId}` - Detect fraud patterns
- `GET /api/fraud/alerts` - Get fraud alerts
- `POST /api/fraud/flag/{enrollmentId}` - Flag enrollment as fraud

### Project Monitoring
- `GET /api/projects` - Get all projects
- `POST /api/projects` - Create project
- `PUT /api/projects/{id}/progress` - Update progress
- `POST /api/projects/{id}/release-funds` - Release funds
- `POST /api/projects/{id}/expenditure` - Record expenditure

### Audit Logs
- `GET /api/audit` - Get audit logs
- `GET /api/audit/entity/{type}` - Get logs by entity type

## Demo Credentials

### Admin User
- **Email**: admin@govshield.gov.in
- **Password**: admin@2727
- **Role**: ADMIN

### Officer
- **Email**: officer1@govshield.gov.in
- **Password**: officer@2727
- **Role**: OFFICER

### Auditor
- **Email**: auditor@govshield.gov.in
- **Password**: auditor@2727
- **Role**: AUDITOR

## Key Features

### 1. UGID System
- Unique identifier for each citizen
- Links to Aadhaar, PAN, and employment records
- Prevents duplicate enrollment

### 2. Eligibility Engine
- Rule-based verification system
- Checks income, age, employment status
- Prevents duplicate benefits from same sector
- Flags suspicious applications

### 3. Fraud Detection
- Analyzes enrollment patterns
- Identifies rapid successive applications
- Cross-references sector enrollments
- Detects income discrepancies
- Risk-based flagging (HIGH, MEDIUM, LOW)

### 4. Project Monitoring
- Budget tracking and release management
- Progress monitoring
- Quality assessment
- Transparency in fund allocation
- Audit trails for all transactions

### 5. Multi-Role Dashboards
- **Citizen Dashboard**: View schemes, submit applications, track status
- **Officer Dashboard**: Manage enrollments, verify eligibility, update projects
- **Auditor Dashboard**: Analyze fraud patterns, generate reports, track compliance

## Configuration

### Database Configuration
Edit `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/govshield
    username: root
    password: root
```

### JWT Configuration
```yaml
jwt:
  secret: your-secret-key-here
  expiration: 86400000  # 24 hours
```

## API Documentation

Once the backend is running, access Swagger UI at:
`http://localhost:8080/api/swagger-ui.html`

## Security Features

- JWT-based authentication
- Role-based access control (RBAC)
- CORS configuration
- Password encryption using BCrypt
- SQL injection prevention
- Rate limiting (Nginx)
- HTTPS/TLS support

## Database Schema

### Key Tables
- **citizens** - Citizen records with UGID
- **gov_employees** - Government employee accounts
- **schemes** - Government benefit schemes
- **enrollments** - Scheme applications and enrollment status
- **projects** - Government-funded projects
- **audit_logs** - Audit trail for all operations

## Monitoring & Maintenance

### Health Checks
```bash
# Backend health
curl http://localhost:8080/api/health

```

## Troubleshooting

### Database Connection Issues
- Ensure MySQL is running
- Check credentials in `application.yml`
- Verify database name and schema

### Port Already in Use
```bash
# Kill process using port
lsof -ti:8080 | xargs kill -9    # Backend
lsof -ti:3306 | xargs kill -9    # MySQL
lsof -ti:80 | xargs kill -9      # Nginx
```

## Contributing

1. Create a feature branch
2. Make your changes
3. Submit a pull request
4. Ensure tests pass

## Testing

```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify
```

## Performance Optimization

- Database indexing on frequently queried columns
- Query caching with application-level caching
- Connection pooling (HikariCP)
- Compression (GZIP) for API responses
- CDN for static assets

## Future Enhancements

- Mobile app (iOS/Android)
- Blockchain integration for immutable audit trails
- AI-powered fraud detection
- Real-time notifications
- Advanced analytics and reporting
- Biometric integration

## Support & Documentation

For detailed documentation, see:
- [Problem Statement](docs/problem-statement.md)
- [System Architecture](docs/system-architecture.md)
- [API Documentation](docs/api-docs.md)
- [Entity-Relationship Diagram](docs/er-diagram.png)
- [Rotate Passwords, API Keys, and URLs](docs/ROTATE_PASSWORDS_API_KEYS_AND_URLS.md)
- [Deploy From Scratch](docs/DEPLOY_FROM_SCRATCH.md)

## License

This project is developed for the Government of India and follows applicable government policies and regulations.

## Contact

For inquiries and support, contact the GovShield development team.

---

**Version**: 1.0.0  
**Last Updated**: February 2024  
**Status**: Production Ready
