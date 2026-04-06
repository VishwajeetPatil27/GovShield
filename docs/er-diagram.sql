-- GovShield - Entity Relationship Diagram with Documentation
-- This file illustrates the database schema and relationships

/*
ENTITY RELATIONSHIP DIAGRAM - GovShield

┌──────────────────────────────┐
│      gov_employees           │
├──────────────────────────────┤
│ PK id (INT)                  │
│ employee_id (VARCHAR, UNIQUE)│
│ email (VARCHAR, UNIQUE)      │
│ department (VARCHAR)         │
│ designation (VARCHAR)        │
│ role (ENUM)                  │
│ password_hash (VARCHAR)      │
│ is_active (BOOLEAN)          │
│ created_at (TIMESTAMP)       │
│ updated_at (TIMESTAMP)       │
└──────────────┬───────────────┘
               │ (authenticates)
               │
┌──────────────▼────────────────────────┐
│        citizens                       │
├───────────────────────────────────────┤
│ PK id (INT)                           │
│ ugid (VARCHAR, UNIQUE)                │ ◄─── Unique Government Shield ID
│ aadhaar (VARCHAR, UNIQUE)             │
│ pan (VARCHAR, UNIQUE)                 │
│ first_name (VARCHAR)                  │
│ last_name (VARCHAR)                   │
│ email (VARCHAR, UNIQUE)               │
│ phone_number (VARCHAR)                │
│ date_of_birth (DATE)                  │
│ gender (VARCHAR)                      │
│ address (VARCHAR)                     │
│ state (VARCHAR)                       │
│ district (VARCHAR)                    │
│ pincode (VARCHAR)                     │
│ annual_income (DECIMAL)               │ ◄─── Used for eligibility
│ employment_status (VARCHAR)           │
│ is_government_employee (BOOLEAN)      │
│ is_below_poverty_line (BOOLEAN)       │
│ is_active (BOOLEAN)                   │
│ created_at (TIMESTAMP)                │
│ updated_at (TIMESTAMP)                │
└───────────────────────────────────────┘
       │           │                │
       │ (applies) │ (beneficiary)  │
       │           │                │
       │    ┌──────▼───────┐        │
       │    │ enrollments  │        │
       │    │ (ManyToMany) │        │
       │    └──────┬───────┘        │
       │           │                │
       │    ┌──────▼───────────────────┐
       │    │     schemes              │
       │    ├─────────────────────────┤
       │    │ PK id (INT)             │
       │    │ scheme_code (VARCHAR)   │
       │    │ scheme_name (VARCHAR)   │
       │    │ sector (VARCHAR)        │
       │    │ benefit_amount (DECIMAL)│
       │    │ max_annual_income (INT) │
       │    │ max_age (INT)           │
       │    │ min_age (INT)           │
       │    │ is_govt_emp_eligible    │
       │    │ is_active (BOOLEAN)     │
       │    │ created_at (TIMESTAMP)  │
       │    │ updated_at (TIMESTAMP)  │
       └────┴─────────────────────────┘

       
┌──────────────────────────────────────────┐
│   enrollments (Application Records)      │
├──────────────────────────────────────────┤
│ PK id (INT)                              │
│ FK citizen_id (INT) ──────────┐          │
│ FK scheme_id (INT) ─────────┐ │          │
│ enrollment_number (VARCHAR)  │ │          │
│ status (ENUM: APPLIED/...)   │ │          │
│ eligibility_status (VARCHAR) │ │          │
│ is_fraud_detected (BOOLEAN)  │ │          │
│ fraud_risk_level (VARCHAR)   │ │          │
│ created_at (TIMESTAMP)       │ │          │
│ updated_at (TIMESTAMP)       │ │          │
└──────────────────────────────┼─┼──────────┘
                               │ │
                    ┌──────────┘ │
                    │            │
            ┌───────▼────────────▼────┐
            │   enrollments links to   │
            │   - citizen by FK        │
            │   - scheme by FK         │
            └────────────────────────┘


┌────────────────────────────────────────┐
│         projects                       │
├────────────────────────────────────────┤
│ PK id (INT)                            │
│ project_code (VARCHAR, UNIQUE)         │
│ project_name (VARCHAR)                 │
│ description (VARCHAR)                  │
│ category (VARCHAR)                     │
│ state (VARCHAR)                        │
│ district (VARCHAR)                     │
│ allocated_mla (VARCHAR)                │ ◄─ Government allocation
│ allocated_mp (VARCHAR)                 │
│ total_budget (DECIMAL)                 │
│ released_amount (DECIMAL)              │
│ spent_amount (DECIMAL)                 │
│ progress_percentage (DECIMAL)          │
│ quality_status (VARCHAR)               │
│ status (ENUM: ONGOING/COMPLETED)       │
│ start_date (DATE)                      │
│ end_date (DATE)                        │
│ created_at (TIMESTAMP)                 │
│ updated_at (TIMESTAMP)                 │
└────────────────────────────────────────┘
       │
       │ (logged in)
       │
       ▼
┌──────────────────────────────────────────┐
│      audit_logs (Activity Trail)        │
├──────────────────────────────────────────┤
│ PK id (INT)                              │
│ action (VARCHAR)                         │
│ entity_type (VARCHAR)                    │
│ entity_id (INT)                          │
│ performed_by (VARCHAR) ─┐                │
│ details (VARCHAR)       │                │
│ status (VARCHAR)        │ (links to)     │
│ ip_address (VARCHAR)    │ employee       │
│ created_at (TIMESTAMP)  │ email          │
└──────────────────────────────────────────┘


RELATIONSHIPS SUMMARY:
======================

1. citizens (1) ──── (N) enrollments
   One citizen can apply for multiple schemes

2. schemes (1) ──── (N) enrollments
   One scheme has many enrollments

3. gov_employees (1) ──── (N) audit_logs
   One officer/auditor performs many actions (tracked in audit logs)

4. projects (independent)
   Projects are tracked independently with ALMAs/MPs allocation

5. audit_logs captures ALL operations
   - Enrollment approvals/rejections
   - Fund releases
   - Project updates
   - Any other government operation


KEY INDEXES FOR PERFORMANCE:
=============================

citizens:
  - ugid (UNIQUE, frequently searched)
  - aadhaar (UNIQUE, verification)
  - email (UNIQUE)
  - phone_number (frequently queried)
  - pan (UNIQUE, verification)
  
schemes:
  - scheme_code (UNIQUE, frequently searched)
  - sector (filtered queries)
  - is_active (status filtering)

enrollments:
  - enrollment_number (UNIQUE, tracking)
  - citizen_id (FK, finding citizen enrollments)
  - scheme_id (FK, finding scheme enrollments)
  - status (filtering by status)
  - is_fraud_detected (finding flagged cases)

projects:
  - project_code (UNIQUE, frequently searched)
  - allocated_mla (filtering)
  - allocated_mp (filtering)
  - status (filtering)
  - state (geographic filtering)

audit_logs:
  - entity_type (filtering by entity)
  - action (filtering by action)
  - performed_by (employee accountability)
  - created_at (timeline queries)


DATA INTEGRITY CONSTRAINTS:
============================

1. Citizens Table:
   - UGID automatically generated, never changes
   - Aadhaar/PAN must be unique (prevent duplicates)
   - Email/Phone must be unique
   - Annual income ≥ 0
   - Age calculated from date_of_birth

2. Enrollments Table:
   - Foreign key constraint to citizens.id
   - Foreign key constraint to schemes.id
   - Enrollment number auto-generated, unique
   - Status must be one of predefined values
   - Can't have duplicate active enrollments in same sector

3. Projects Table:
   - Project code unique
   - Budget values ≥ 0
   - Progress percentage 0-100
   - Released amount ≤ total budget
   - Spent amount ≤ released amount

4. Audit Logs Table:
   - Immutable (only insert, never update)
   - entity_id references entity being tracked
   - Timestamp auto-set on creation
   - IP address captured for security audit


FRAUD DETECTION LOGIC:
======================

When enrollment created:
1. Check citizen's existing enrollments
2. Count enrollments in same sector → if >1 = HIGH RISK
3. Check application dates → if <30 days apart = MEDIUM/HIGH RISK
4. Verify income vs. scheme max_annual_income
5. Verify employment status vs. is_govt_emp_eligible
6. Generate risk score:
   - Multiple sector enrollments: +30 points
   - Rapid applications: +25 points
   - Income verification: +20 points
   - Score ≥50 = HIGH RISK (auto-flag for audit)


ELIGIBILITY VERIFICATION:
==========================

When citizen applies for scheme:
1. Check scheme active status
2. Verify citizen eligibility:
   a) Annual income ≤ scheme.max_annual_income
   b) Age ≥ scheme.min_age AND Age ≤ scheme.max_age
   c) If is_govt_emp_eligible = false AND citizen.is_government_employee = true → REJECT
   d) No existing active enrollment in same sector scheme
3. If all pass → ELIGIBLE with LOW/MEDIUM risk
4. If flagged → ELIGIBLE but FLAGGED for officer verification
5. Create enrollment record with status = APPLIED
6. Officer reviews and APPROVEs or REJECTs


PROJECT MONITORING:
===================

For each project:
1. Track total budget allocation
2. Monitor fund release in phases:
   - Release funds based on progress milestone
   - Prevent releasing full budget upfront
3. Record expenditures as work progresses:
   - Spent amount ≤ released amount (constraint)
4. Update progress percentage (0-100)
5. Assess quality (POOR/FAIR/GOOD/EXCELLENT)
6. Auto-mark COMPLETED when progress = 100%
7. Track MLA/MP allocation for transparency


AUDIT TRAIL:
=============

Every significant operation logged:
- Enrollment created/updated/approved/rejected
- Funds released
- Expenditure recorded
- Project progress updated
- User login attempts
- Data modifications

Each audit log contains:
- WHO (performed_by)
- WHAT (action)
- WHICH (entity_type, entity_id)
- WHEN (created_at)
- SUCCESS/FAILURE status
- SOURCE (ip_address)

Used for:
- Accountability & compliance
- Fraud investigation
- System audit
- Regulatory reporting

*/

-- This conceptual diagram is implemented in schema.sql
-- See schema.sql for actual CREATE TABLE statements
-- See data.sql for sample data insertions
