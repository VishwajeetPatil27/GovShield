-- GovShield Database - Sample Data
-- Sample data for testing and demonstration
-- Use: mysql -u root -p govshield < data.sql

USE govshield;

-- Sample Government Employees
INSERT INTO gov_employees (employee_id, email, first_name, last_name, department, designation, phone_number, password_hash, role, is_active) VALUES
('EMP001', 'admin@govshield.gov.in', 'Rajesh', 'Kumar', 'Administration', 'System Administrator', '9876543210', '$2a$10$m5YxeycqXkhcn8fsdbLm.uJ46/ohueHloeXlYkNQPrvOHeX6rQEqq', 'ADMIN', TRUE),
('EMP002', 'officer1@govshield.gov.in', 'Priya', 'Sharma', 'Welfare Department', 'Scheme Officer', '9876543211', '$2a$10$ATAHeVXEVCR2mqnj96IR..69wa8Fzs6LBPjth8s0ez1j66wYwqln.', 'OFFICER', TRUE),
('EMP003', 'officer2@govshield.gov.in', 'Amit', 'Patel', 'Welfare Department', 'Senior Officer', '9876543212', '$2a$10$ATAHeVXEVCR2mqnj96IR..69wa8Fzs6LBPjth8s0ez1j66wYwqln.', 'OFFICER', TRUE),
('EMP004', 'auditor@govshield.gov.in', 'Neha', 'Gupta', 'Internal Audit', 'Audit Officer', '9876543213', '$2a$10$AqX4JaewBZB9knP5fnS7BeqkaFMT5bEheSOzgemTFEdaJONLrgY8u', 'AUDITOR', TRUE);

-- Sample Schemes
INSERT INTO schemes (scheme_code, scheme_name, description, sector, scheme_type, benefit_amount, max_annual_income, max_age, min_age, is_government_employee_eligible, launch_date, is_active) VALUES
('PM-JAY-001', 'Pradhan Mantri Jan Arogya Yojana', 'Health insurance scheme for vulnerable families', 'HEALTH', 'FINANCIAL', 500000, 500000, 80, 0, FALSE, '2018-09-23', TRUE),
('NREGA-001', 'Mahatma Gandhi National Rural Employment Guarantee Act', 'Employment guarantee scheme for rural areas', 'EMPLOYMENT', 'EMPLOYMENT', 250000, 1000000, 70, 18, FALSE, '2005-02-02', TRUE),
('PMAY-001', 'Pradhan Mantri Awas Yojana', 'Housing scheme for economically weaker sections', 'HOUSING', 'HOUSING', 800000, 600000, 75, 18, FALSE, '2015-06-25', TRUE),
('MGNREGS-001', 'Skill Development and Livelihood Scheme', 'Vocational training and livelihood support', 'SKILL_DEVELOPMENT', 'TRAINING', 100000, 300000, 60, 18, FALSE, '2019-05-01', TRUE),
('PM-KISAN-001', 'Pradhan Mantri Kisan Samman Nidhi', 'Direct income support for farmers', 'AGRICULTURE', 'FINANCIAL', 60000, 2000000, 65, 18, TRUE, '2019-01-01', TRUE);

-- Sample Scheme Conflict Rules (Sector Conflict Rule Engine)
INSERT INTO scheme_conflict_rules (sector_a, sector_b, action, message, is_active) VALUES
('HOUSING', 'HOUSING', 'REJECT', 'Citizen already has an active/previous housing benefit. Only one housing scheme is allowed.', TRUE),
('AGRICULTURE', 'AGRICULTURE', 'FLAG', 'Multiple agriculture scheme applications detected. Requires officer review.', TRUE),
('EDUCATION', 'EDUCATION', 'FLAG', 'Multiple education scheme applications detected. Requires officer review.', TRUE);

-- Sample Citizens
INSERT INTO citizens (ugid, aadhaar, pan, first_name, last_name, email, phone_number, date_of_birth, gender, address, state, district, pincode, annual_income, employment_status, is_government_employee, is_below_poverty_line, verification_status, is_active) VALUES
('UGID-1707293400000-a1b2c3d4', '123456789012', 'ABCDE1234F', 'Ramesh', 'Patel', 'ramesh.patel@email.com', '9876543210', '1985-05-15', 'MALE', '123 Main Street', 'Gujarat', 'Ahmedabad', '380001', 250000, 'UNEMPLOYED', FALSE, TRUE, 'VERIFIED', TRUE),
('UGID-1707293400001-b2c3d4e5', '234567890123', 'BCDEF2345G', 'Priya', 'Singh', 'priya.singh@email.com', '9876543211', '1990-08-22', 'FEMALE', '456 Oak Avenue', 'Maharashtra', 'Mumbai', '400001', 350000, 'SELF_EMPLOYED', FALSE, FALSE, 'UNDER_REVIEW', TRUE),
('UGID-1707293400002-c3d4e5f6', '345678901234', 'CDEFG3456H', 'Suresh', 'Verma', 'suresh.verma@email.com', '9876543212', '1978-03-10', 'MALE', '789 Pine Road', 'Rajasthan', 'Jaipur', '302001', 180000, 'UNEMPLOYED', FALSE, TRUE, 'PENDING', TRUE),
('UGID-1707293400003-d4e5f6g7', '456789012345', 'DEFGH4567I', 'Anjali', 'Deshmukh', 'anjali.deshmukh@email.com', '9876543213', '1988-11-30', 'FEMALE', '321 Elm Street', 'Karnataka', 'Bangalore', '560001', 420000, 'EMPLOYED', FALSE, FALSE, 'VERIFIED', TRUE),
('UGID-1707293400004-e5f6g7h8', '567890123456', 'EFGHI5678J', 'Vikram', 'Rao', 'vikram.rao@email.com', '9876543214', '1982-07-18', 'MALE', '654 Birch Lane', 'Telangana', 'Hyderabad', '500001', 290000, 'UNEMPLOYED', FALSE, TRUE, 'UNDER_REVIEW', TRUE);

-- Additional Citizens
INSERT INTO citizens (ugid, aadhaar, pan, first_name, last_name, email, phone_number, date_of_birth, gender, address, state, district, pincode, annual_income, employment_status, is_government_employee, is_below_poverty_line, verification_status, is_active) VALUES
('UGID-1707293400005-f6g7h8i9', '678901234567', 'FGHIJ6789K', 'Kiran', 'Mehta', 'kiran.mehta@email.com', '9876543215', '1992-01-12', 'FEMALE', '12 River View', 'Gujarat', 'Surat', '395001', 210000, 'SELF_EMPLOYED', FALSE, TRUE, 'PENDING', TRUE),
('UGID-1707293400006-g7h8i9j0', '789012345678', 'GHIJK7890L', 'Arun', 'Nair', 'arun.nair@email.com', '9876543216', '1987-09-03', 'MALE', '44 Lake Road', 'Kerala', 'Kochi', '682001', 460000, 'EMPLOYED', FALSE, FALSE, 'VERIFIED', TRUE);

-- Sample CEPS Profiles (demo)
INSERT INTO citizen_economic_profiles (citizen_id, income_score, asset_score, land_score, employment_score, utility_score, ceps_score, ceps_category, factors_json) VALUES
(1, 10, 5, 8, 4, 6, 33, 'MODERATE_SUPPORT', '{\"vehicles\":1,\"landAcres\":0.5,\"electricityUnitsMonthly\":120}'),
(2, 18, 10, 14, 8, 9, 59, 'MODERATE_SUPPORT', '{\"vehicles\":2,\"landAcres\":1.5,\"electricityUnitsMonthly\":220}'),
(3, 6, 4, 2, 2, 4, 18, 'POOR_ELIGIBLE', '{\"vehicles\":0,\"landAcres\":0.0,\"electricityUnitsMonthly\":80}'),
(4, 22, 16, 18, 10, 14, 80, 'NOT_ELIGIBLE', '{\"vehicles\":2,\"landAcres\":3.0,\"electricityUnitsMonthly\":380}');

-- Sample Citizen Documents (base64 content is demo text)
INSERT INTO citizen_documents (citizen_id, document_type, document_number, file_name, file_content_base64, verification_status, verification_remarks, verified_by_role, uploaded_at, verified_at) VALUES
(1, 'AADHAAR', '123456789012', 'aadhaar-ramesh.txt', 'U2FtcGxlIEFhZGhhYXIgRG9jdW1lbnQ=', 'VERIFIED', 'Cross-verified from UIDAI feed', 'OFFICER', '2025-12-21 09:30:00', '2025-12-21 12:00:00'),
(1, 'PAN', 'ABCDE1234F', 'pan-ramesh.txt', 'U2FtcGxlIFBBTiBEb2N1bWVudA==', 'VERIFIED', 'PAN name and number matched', 'ADMIN', '2025-12-21 09:32:00', '2025-12-21 12:10:00'),
(2, 'AADHAAR', '234567890123', 'aadhaar-priya.txt', 'UHJpeWEgQWFkaGFhciBEb2M=', 'PENDING', NULL, NULL, '2026-01-02 11:15:00', NULL),
(5, 'AADHAAR', '567890123456', 'aadhaar-vikram.txt', 'VmlrcmFtIEFhZGhhYXIgRG9j', 'REJECTED', 'Image quality unclear, re-upload required', 'OFFICER', '2026-01-08 10:05:00', '2026-01-08 13:25:00');

-- Sample Enrollments
INSERT INTO enrollments (enrollment_number, citizen_id, scheme_id, status, eligibility_status, fraud_risk_level, is_fraud_detected, fraud_risk_score, applied_at, approved_at) VALUES
('ENR-001-2024-UUID001', 1, 1, 'APPROVED', 'ELIGIBLE', 'LOW', FALSE, 5, '2024-01-15 10:30:00', '2024-01-16 14:20:00'),
('ENR-002-2024-UUID002', 2, 3, 'APPROVED', 'ELIGIBLE', 'LOW', FALSE, 8, '2024-01-20 09:15:00', '2024-01-21 11:45:00'),
('ENR-003-2024-UUID003', 3, 2, 'APPLIED', 'ELIGIBLE', 'MEDIUM', FALSE, 35, '2024-02-01 13:45:00', NULL),
('ENR-004-2024-UUID004', 4, 4, 'APPROVED', 'ELIGIBLE', 'LOW', FALSE, 12, '2024-02-05 08:30:00', '2024-02-06 16:00:00'),
('ENR-005-2024-UUID005', 5, 5, 'FLAGGED', 'ELIGIBLE', 'HIGH', TRUE, 65, '2024-02-10 11:20:00', NULL);

-- Additional Enrollments
INSERT INTO enrollments (enrollment_number, citizen_id, scheme_id, status, eligibility_status, fraud_risk_level, is_fraud_detected, fraud_risk_score, applied_at, approved_at) VALUES
('ENR-006-2024-UUID006', 6, 1, 'APPLIED', 'ELIGIBLE', 'MEDIUM', FALSE, 30, '2024-03-11 10:00:00', NULL),
('ENR-007-2024-UUID007', 7, 3, 'APPLIED', 'ELIGIBLE', 'LOW', FALSE, 12, '2024-03-15 15:30:00', NULL);

-- Sample Projects
INSERT INTO projects (project_code, project_name, description, category, state, district, allocated_mla, allocated_mp, total_budget, released_amount, spent_amount, progress_percentage, quality_status, status, start_date, end_date) VALUES
('PROJ-001', 'NH44 Highway Expansion Phase 2', 'Expansion and widening of National Highway 44 in Gujarat', 'ROAD', 'Gujarat', 'Ahmedabad', 'Ashok Gehlot', 'Amit Shah', 500000000, 200000000, 150000000, 65, 'GOOD', 'ONGOING', '2023-01-15', '2025-12-31'),
('PROJ-002', 'Rural Water Supply Project', 'Installation of piped water supply in 50 villages', 'WATER', 'Rajasthan', 'Jaipur', 'Vasundhara Raje', 'Gumansingh Lodha', 250000000, 100000000, 80000000, 45, 'GOOD', 'ONGOING', '2023-06-01', '2026-03-31'),
('PROJ-003', 'Smart City Development', 'Smart city infrastructure development in Mumbai', 'INFRASTRUCTURE', 'Maharashtra', 'Mumbai', 'Raj Thackeray', 'Sharad Pawar', 1000000000, 350000000, 250000000, 55, 'SATISFACTORY', 'ONGOING', '2022-12-01', '2026-11-30'),
('PROJ-004', 'School Building Construction', 'Construction of 100 new school buildings in rural areas', 'EDUCATION', 'Telangana', 'Hyderabad', 'K. Chandrasekhar Rao', 'Rajinikanth', 400000000, 0, 0, 0, 'PENDING', 'PENDING', '2024-03-01', '2026-02-28');

-- Additional Projects
INSERT INTO projects (project_code, project_name, description, category, state, district, allocated_mla, allocated_mp, total_budget, released_amount, spent_amount, progress_percentage, quality_status, status, start_date, end_date) VALUES
('PROJ-005', 'District Hospital Upgrade', 'Modernization of district hospital equipment and wards', 'HEALTH', 'Maharashtra', 'Pune', 'Ajit Pawar', 'Supriya Sule', 300000000, 70000000, 45000000, 28, 'GOOD', 'ONGOING', '2024-01-01', '2026-06-30'),
('PROJ-006', 'Village Solar Lighting', 'Solar street lighting in 120 villages', 'ENERGY', 'Rajasthan', 'Udaipur', 'Gulab Chand', 'CP Joshi', 120000000, 20000000, 10000000, 18, 'SATISFACTORY', 'ONGOING', '2024-02-01', '2025-12-31');

-- Sample Audit Logs
INSERT INTO audit_logs (action, entity_type, entity_id, performed_by, details, status, ip_address) VALUES
('CREATE', 'CITIZEN', 1, 'admin@govshield.gov.in', 'New citizen registered: Ramesh Patel (UGID: UGID-1707293400000-a1b2c3d4)', 'SUCCESS', '192.168.1.100'),
('APPLY', 'ENROLLMENT', 1, '9876543210', 'Citizen applied for scheme: PM-JAY-001', 'SUCCESS', '192.168.1.101'),
('APPROVE', 'ENROLLMENT', 1, 'officer1@govshield.gov.in', 'Enrollment approved after eligibility verification', 'SUCCESS', '192.168.1.102'),
('UPDATE', 'PROJECT', 1, 'officer1@govshield.gov.in', 'Project progress updated to 65%', 'SUCCESS', '192.168.1.103'),
('RELEASE_FUNDS', 'PROJECT', 1, 'officer2@govshield.gov.in', 'Released Rs. 50,000,000 for project PROJ-001', 'SUCCESS', '192.168.1.104'),
('FRAUD_FLAG', 'ENROLLMENT', 5, 'auditor@govshield.gov.in', 'Enrollment flagged for fraud investigation - High risk score', 'SUCCESS', '192.168.1.105'),
('GENERATE_REPORT', 'AUDIT', NULL, 'auditor@govshield.gov.in', 'Monthly fraud report generated', 'SUCCESS', '192.168.1.106'),
('LOGIN', 'EMPLOYEE', 2, 'officer1@govshield.gov.in', 'Officer logged in successfully', 'SUCCESS', '192.168.1.107');

-- Create views for common queries
CREATE VIEW v_citizen_enrollments AS
SELECT 
  c.id as citizen_id,
  c.ugid,
  c.first_name,
  c.last_name,
  c.email,
  e.id as enrollment_id,
  e.enrollment_number,
  s.scheme_code,
  s.scheme_name,
  e.status,
  e.eligibility_status,
  e.fraud_risk_level,
  e.is_fraud_detected,
  e.applied_at
FROM citizens c
LEFT JOIN enrollments e ON c.id = e.citizen_id
LEFT JOIN schemes s ON e.scheme_id = s.id
WHERE c.is_active = TRUE;

CREATE VIEW v_project_summary AS
SELECT 
  p.id,
  p.project_code,
  p.project_name,
  p.category,
  p.state,
  p.district,
  p.allocated_mla,
  p.allocated_mp,
  p.total_budget,
  p.released_amount,
  p.spent_amount,
  (p.spent_amount / p.released_amount * 100) as spend_percentage,
  p.progress_percentage,
  p.quality_status,
  p.status,
  p.start_date,
  p.end_date,
  DATEDIFF(p.end_date, CURDATE()) as days_remaining
FROM projects
WHERE p.status IN ('PENDING', 'ONGOING');

CREATE VIEW v_fraud_summary AS
SELECT 
  e.id,
  e.enrollment_number,
  c.ugid,
  c.first_name,
  c.last_name,
  s.scheme_code,
  s.scheme_name,
  e.fraud_risk_level,
  e.fraud_risk_score,
  e.is_fraud_detected,
  e.applied_at,
  COUNT(*) OVER (PARTITION BY e.citizen_id) as total_enrollments
FROM enrollments e
JOIN citizens c ON e.citizen_id = c.id
JOIN schemes s ON e.scheme_id = s.id
WHERE e.fraud_risk_level IN ('HIGH', 'MEDIUM') OR e.is_fraud_detected = TRUE;

-- End of data.sql
