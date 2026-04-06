# GovShield - API Documentation

## Base URL
```
http://localhost:8080/api
```

## Authentication

All private endpoints require a JWT token in the Authorization header:
```
Authorization: Bearer {token}
```

## Response Format

### Success Response
```json
{
  "id": 1,
  "data": {...}
}
```

### Error Response
```json
{
  "status": 400,
  "code": "ERROR_CODE",
  "message": "Error description",
  "timestamp": "2024-02-07T10:30:00"
}
```

---

## Endpoints

### Authentication Endpoints

#### 1. Login
```
POST /auth/login
Content-Type: application/json

{
  "email": "user@govshield.gov.in",
  "password": "admin@2727"
}

Response 200:
{
  "token": "jwt-token-string",
  "email": "user@govshield.gov.in",
  "role": "OFFICER",
  "expiresIn": 86400000
}
```

#### 2. Validate Token
```
POST /auth/validate
Authorization: Bearer {token}

Response 200:
"Token valid for: user@govshield.gov.in"
```

---

### Citizen Endpoints

#### 1. Register Citizen
```
POST /citizens/register
Content-Type: application/json

{
  "aadhaar": "123456789012",
  "pan": "ABCDE1234F",
  "firstName": "Ramesh",
  "lastName": "Patel",
  "email": "ramesh.patel@email.com",
  "phoneNumber": "9876543210",
  "dateOfBirth": "1985-05-15",
  "gender": "MALE",
  "address": "123 Main Street",
  "state": "Gujarat",
  "district": "Ahmedabad",
  "pincode": "380001",
  "annualIncome": 250000,
  "employmentStatus": "UNEMPLOYED",
  "isGovernmentEmployee": false,
  "isBelowPovertyLine": true
}

Response 201:
{
  "id": 1,
  "ugid": "UGID-1707293400000-...",
  "aadhaar": "123456789012",
  "firstName": "Ramesh",
  "lastName": "Patel",
  "email": "ramesh.patel@email.com",
  ...
}
```

#### 2. Get Citizen by UGID
```
GET /citizens/ugid/{ugid}
Authorization: Bearer {token}

Response 200:
{
  "id": 1,
  "ugid": "UGID-...",
  "firstName": "Ramesh",
  ...
}
```

#### 3. Get Citizen by ID
```
GET /citizens/{id}
Authorization: Bearer {token}

Response 200:
{
  "id": 1,
  ...
}
```

#### 4. Update Citizen
```
PUT /citizens/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "firstName": "Ramesh",
  "lastName": "Patel",
  "address": "New Address",
  "annualIncome": 300000,
  "employmentStatus": "SELF_EMPLOYED"
}

Response 200:
{...updated citizen...}
```

#### 5. Get All Citizens
```
GET /citizens
Authorization: Bearer {token}

Response 200:
[{...}, {...}]
```

#### 6. Deactivate Citizen
```
DELETE /citizens/{id}
Authorization: Bearer {token}

Response 200:
"Citizen deactivated successfully"
```

---

### Citizen Economic Profile Score (CEPS)

#### 1. Get CEPS by UGID
```
GET /ceps/{ugid}
Authorization: Bearer {token}

Response 200:
{
  "ugid": "UGID-...",
  "cepsScore": 33,
  "cepsCategory": "MODERATE_SUPPORT",
  "incomeScore": 10,
  "assetScore": 5,
  "landScore": 8,
  "employmentScore": 4,
  "utilityScore": 6,
  "calculatedAt": "2026-04-06T10:00:00"
}
```

#### 2. Calculate/Update CEPS (store factors)
```
POST /ceps/{ugid}/calculate
Authorization: Bearer {token}
Content-Type: application/json

{
  "vehiclesCount": 1,
  "landAcres": 0.5,
  "electricityUnitsMonthly": 120,
  "declaredAssetsValue": 200000
}

Response 200: { ...CEPS response... }
```

---

### Scheme Endpoints

#### 1. Create Scheme (Admin/Officer)
```
POST /schemes
Authorization: Bearer {token}
Content-Type: application/json

{
  "schemeCode": "PM-JAY",
  "schemeName": "Pradhan Mantri Jan Arogya Yojana",
  "description": "Health insurance scheme",
  "sector": "HEALTH",
  "schemeType": "FINANCIAL",
  "benefitAmount": 500000,
  "maxAnnualIncome": 500000,
  "maxAge": 70,
  "minAge": 0,
  "isGovernmentEmployeeEligible": false,
  "launchDate": "2018-09-23"
}

Response 201:
{...scheme...}
```

#### 2. Get Scheme by ID
```
GET /schemes/{id}
Authorization: Bearer {token}

Response 200:
{...scheme...}
```

#### 3. Get Scheme by Code
```
GET /schemes/code/{schemeCode}
Authorization: Bearer {token}

Response 200:
{...scheme...}
```

#### 4. Get All Active Schemes
```
GET /schemes/active/all
Authorization: Bearer {token}

Response 200:
[{...}, {...}]
```

#### 5. Get Schemes by Sector
```
GET /schemes/sector/{sector}
Authorization: Bearer {token}

Response 200:
[{...}, {...}]
```

#### 6. Update Scheme
```
PUT /schemes/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "schemeName": "Updated Name",
  "benefitAmount": 600000,
  ...
}

Response 200:
{...updated scheme...}
```

#### 7. Deactivate Scheme
```
DELETE /schemes/{id}
Authorization: Bearer {token}

Response 200:
"Scheme deactivated successfully"
```

---

### Eligibility & Enrollment Endpoints

#### 1. Apply for Scheme
```
POST /eligibility/apply
Authorization: Bearer {token}
Content-Type: application/json

{
  "schemeId": 1,
  "ugid": "UGID-..."
}

Response 201:
{
  "enrollmentId": 5,
  "enrollmentNumber": "ENR-uuid-string",
  "eligibilityStatus": "ELIGIBLE",
  "fraudRiskLevel": "LOW",
  "message": "Application submitted successfully",
  "eligible": true
}
```

#### 2. Check Eligibility (without applying)
```
POST /eligibility/check?ugid=UGID-...&schemeId=1
Authorization: Bearer {token}

Response 200:
{
  "enrollmentId": null,
  "enrollmentNumber": null,
  "eligibilityStatus": "ELIGIBLE",
  "fraudRiskLevel": "LOW",
  "message": "Citizen is eligible",
  "eligible": true
}
```

#### 2b. Real-Time Eligibility Checker (demo + profile mode)
```
POST /eligibility/realtime-check
Authorization: Bearer {token}
Content-Type: application/json

// Option A: Use stored citizen profile by UGID (conflicts + CEPS policy included)
{ "ugid": "UGID-..." }

// Option B: Simulation mode (no UGID)
{
  "age": 25,
  "annualIncome": 250000,
  "isGovernmentEmployee": false,
  "vehiclesCount": 1,
  "landAcres": 0.5,
  "electricityUnitsMonthly": 120,
  "declaredAssetsValue": 200000
}

Response 200:
{
  "cepsScore": 33,
  "cepsCategory": "MODERATE_SUPPORT",
  "results": [
    {
      "schemeId": 1,
      "schemeCode": "PM-JAY-001",
      "schemeName": "Pradhan Mantri Jan Arogya Yojana",
      "sector": "HEALTH",
      "eligible": true,
      "reason": "Eligible"
    }
  ]
}
```

#### 3. Get Enrollment by ID
```
GET /eligibility/enrollment/{id}
Authorization: Bearer {token}

Response 200:
{
  "id": 5,
  "enrollmentNumber": "ENR-...",
  "status": "APPLIED",
  "eligibilityStatus": "ELIGIBLE",
  "fraudRiskLevel": "LOW",
  ...
}
```

#### 4. Get Citizen Enrollments
```
GET /eligibility/citizen/{citizenId}
Authorization: Bearer {token}

Response 200:
[{...}, {...}]
```

#### 5. Approve Enrollment (Officer)
```
POST /eligibility/approve/{enrollmentId}
Authorization: Bearer {token}

Response 200:
{
  "id": 5,
  "status": "APPROVED",
  ...
}
```

#### 6. Reject Enrollment (Officer)
```
POST /eligibility/reject/{enrollmentId}?reason=Does%20not%20meet%20criteria
Authorization: Bearer {token}

Response 200:
{
  "id": 5,
  "status": "REJECTED",
  "rejectionReason": "Does not meet criteria",
  ...
}
```

---

### Fraud Detection Endpoints

#### 1. Detect Fraud Patterns
```
GET /fraud/detect/{citizenId}
Authorization: Bearer {token}

Response 200:
[
  {
    "id": 1,
    "enrollmentNumber": "ENR-...",
    "status": "FLAGGED",
    "fraudRiskLevel": "HIGH"
  }
]
```

#### 2. Flag as Fraud (Officer/Auditor)
```
POST /fraud/flag/{enrollmentId}?reason=Suspicious%20pattern
Authorization: Bearer {token}

Response 200:
{
  "id": 1,
  "isFraudDetected": true,
  "fraudRiskLevel": "HIGH",
  "status": "FLAGGED"
}
```

#### 3. Detect All Frauds
```
GET /fraud/detect-all
Authorization: Bearer {token}

Response 200:
[
  {...enrollment with fraud detected...},
  {}
]
```

#### 4. Get Fraud Alerts
```
GET /fraud/alerts
Authorization: Bearer {token}

Response 200:
[
  {
    "id": 1,
    "enrollmentNumber": "ENR-...",
    "fraudRiskLevel": "HIGH",
    "status": "FLAGGED"
  }
]
```

---

### Project Monitoring Endpoints

#### 1. Create Project (Officer/Admin)
```
POST /projects
Authorization: Bearer {token}
Content-Type: application/json

{
  "projectCode": "PROJ-001",
  "projectName": "NH44 Road Expansion",
  "description": "National Highway expansion",
  "category": "ROAD",
  "state": "Gujarat",
  "district": "Ahmedabad",
  "allocatedMla": "Ashok Gehlot",
  "allocatedMp": "Amit Shah",
  "totalBudget": 50000000,
  "startDate": "2023-01-15",
  "endDate": "2025-12-31"
}

Response 201:
{...project...}
```

#### 2. Get Project by ID
```
GET /projects/{id}
Authorization: Bearer {token}

Response 200:
{...project...}
```

#### 3. Get Project by Code
```
GET /projects/code/{projectCode}
Authorization: Bearer {token}

Response 200:
{...project...}
```

#### 4. Get All Projects
```
GET /projects
Authorization: Bearer {token}

Response 200:
[{...}, {...}]
```

#### 5. Get Projects by MLA
```
GET /projects/mla/{mlaName}
Authorization: Bearer {token}

Response 200:
[{...}, {...}]
```

#### 6. Get Projects by MP
```
GET /projects/mp/{mpName}
Authorization: Bearer {token}

Response 200:
[{...}, {...}]
```

#### 7. Get Projects by Status
```
GET /projects/status/{status}
Authorization: Bearer {token}

Response 200:
[{...}, {...}]
```

#### 8. Update Progress
```
PUT /projects/{id}/progress?progressPercentage=45
Authorization: Bearer {token}

Response 200:
{
  "id": 1,
  "progressPercentage": 45
}
```

#### 9. Update Quality Status
```
PUT /projects/{id}/quality?qualityStatus=GOOD
Authorization: Bearer {token}

Response 200:
{
  "id": 1,
  "qualityStatus": "GOOD"
}
```

#### 10. Release Funds
```
POST /projects/{id}/release-funds?amount=5000000
Authorization: Bearer {token}

Response 200:
{
  "id": 1,
  "releasedAmount": 20000000
}
```

#### 11. Record Expenditure
```
POST /projects/{id}/expenditure?amount=2000000
Authorization: Bearer {token}

Response 200:
{
  "id": 1,
  "spentAmount": 14000000
}
```

#### 12. Get Project Status
```
GET /projects/{id}/status
Authorization: Bearer {token}

Response 200:
{
  "projectId": 1,
  "projectCode": "PROJ-001",
  "projectName": "NH44 Road Expansion",
  "status": "ONGOING",
  "progressPercentage": 45,
  "totalBudget": 50000000,
  "releasedAmount": 20000000,
  "spentAmount": 14000000,
  "qualityStatus": "GOOD"
}
```

#### 13. Official Project Update (Officer/Admin)
```
POST /projects/{id}/updates
Authorization: Bearer {token}
Content-Type: application/json

{
  "reportedProgress": 65,
  "message": "Phase-2 asphalt laying completed",
  "photoBase64": "base64...",
  "geoLat": 23.0225,
  "geoLng": 72.5714
}

Response 201: { ...project update... }
```

#### 14. Citizen Evidence Submission (Public Corruption Evidence System)
```
POST /projects/{id}/evidence
Authorization: Bearer {token}
Content-Type: application/json

{
  "ugid": "UGID-...",
  "evidenceType": "PHOTO",
  "message": "Work seems stalled; materials not present",
  "progressEstimate": 35,
  "contractorRating": 2,
  "photoBase64": "base64...",
  "geoLat": 23.0225,
  "geoLng": 72.5714
}

Response 201: { ...evidence... }
```

#### 15. List Evidence for a Project
```
GET /projects/{id}/evidence
Authorization: Bearer {token}

Response 200: [ { ...evidence... } ]
```

#### 16. List Active Integrity Alerts (Officer/Auditor/Admin)
```
GET /projects/alerts
Authorization: Bearer {token}

Response 200: [ { ...alert... } ]
```

#### 17. Resolve an Alert (Officer/Admin)
```
POST /projects/alerts/{alertId}/resolve
Authorization: Bearer {token}
Content-Type: application/json

{ "remarks": "Site inspection completed; corrected progress report." }
```

---

### Audit Log Endpoints

#### 1. Create Audit Log
```
POST /audit
Authorization: Bearer {token}
Content-Type: application/json

{
  "action": "APPROVE",
  "entityType": "ENROLLMENT",
  "entityId": 1,
  "performedBy": "officer1@govshield.gov.in",
  "details": "Approved enrollment ENR-123",
  "status": "SUCCESS",
  "ipAddress": "192.168.1.1"
}

Response 200:
{...audit log...}
```

#### 2. Get All Audit Logs
```
GET /audit
Authorization: Bearer {token}

Response 200:
[{...}, {...}]
```

#### 3. Get Logs by Entity Type
```
GET /audit/entity/{entityType}
Authorization: Bearer {token}

Response 200:
[{...}, {...}]
```

#### 4. Get Logs by Action
```
GET /audit/action/{action}
Authorization: Bearer {token}

Response 200:
[{...}, {...}]
```

#### 5. Get Logs by Performer
```
GET /audit/performer/{performedBy}
Authorization: Bearer {token}

Response 200:
[{...}, {...}]
```

---

## Status Codes

| Code | Meaning |
|------|---------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 409 | Conflict |
| 500 | Internal Server Error |

## Error Codes

| Code | Description |
|------|-------------|
| INVALID_CREDENTIALS | Login failed |
| INVALID_TOKEN | Token validation failed |
| CITIZEN_NOT_FOUND | Citizen record not found |
| SCHEME_NOT_FOUND | Scheme not found |
| ENROLLMENT_NOT_FOUND | Enrollment not found |
| PROJECT_NOT_FOUND | Project not found |
| DUPLICATE_AADHAAR | Aadhaar already registered |
| DUPLICATE_EMAIL | Email already registered |
| INELIGIBLE_ENROLLMENT | Cannot approve ineligible enrollment |

## Rate Limiting

- **API Endpoints**: 100 requests/minute per IP
- **Login Endpoint**: 20 requests/minute per IP

---

**Document Version**: 1.0  
**Created**: February 2024  
**Last Updated**: February 2024
