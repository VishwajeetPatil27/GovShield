-- GovShield Database Schema
-- MySQL 8.0+
-- Create database and tables for the GovShield platform

-- Create Database
CREATE DATABASE IF NOT EXISTS govshield;
USE govshield;

-- Table: Citizens
CREATE TABLE IF NOT EXISTS citizens (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  ugid VARCHAR(255) UNIQUE NOT NULL,
  aadhaar VARCHAR(12) UNIQUE NOT NULL,
  pan VARCHAR(10) UNIQUE,
  first_name VARCHAR(100) NOT NULL,
  last_name VARCHAR(100) NOT NULL,
  email VARCHAR(100) UNIQUE NOT NULL,
  phone_number VARCHAR(20) UNIQUE NOT NULL,
  date_of_birth DATE NOT NULL,
  gender VARCHAR(20) NOT NULL,
  address VARCHAR(255) NOT NULL,
  state VARCHAR(50) NOT NULL,
  district VARCHAR(50) NOT NULL,
  pincode VARCHAR(6) NOT NULL,
  annual_income BIGINT NOT NULL,
  employment_status VARCHAR(50) NOT NULL,
  is_government_employee BOOLEAN DEFAULT FALSE,
  is_below_poverty_line BOOLEAN DEFAULT FALSE,
  verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  KEY idx_ugid (ugid),
  KEY idx_aadhaar (aadhaar),
  KEY idx_email (email),
  KEY idx_phone_number (phone_number),
  KEY idx_employment_status (employment_status),
  CONSTRAINT check_annual_income CHECK (annual_income >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: Citizen Economic Profiles (CEPS)
CREATE TABLE IF NOT EXISTS citizen_economic_profiles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  citizen_id BIGINT NOT NULL,
  income_score INT NOT NULL DEFAULT 0,
  asset_score INT NOT NULL DEFAULT 0,
  land_score INT NOT NULL DEFAULT 0,
  employment_score INT NOT NULL DEFAULT 0,
  utility_score INT NOT NULL DEFAULT 0,
  ceps_score INT NOT NULL DEFAULT 0,
  ceps_category VARCHAR(30) NOT NULL DEFAULT 'UNKNOWN',
  factors_json TEXT,
  calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (citizen_id) REFERENCES citizens(id) ON DELETE CASCADE,
  UNIQUE KEY uq_ceps_citizen_id (citizen_id),
  KEY idx_ceps_score (ceps_score),
  KEY idx_ceps_category (ceps_category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: Citizen Documents
CREATE TABLE IF NOT EXISTS citizen_documents (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  citizen_id BIGINT NOT NULL,
  document_type VARCHAR(100) NOT NULL,
  document_number VARCHAR(100),
  file_name VARCHAR(255) NOT NULL,
  file_content_base64 LONGTEXT NOT NULL,
  verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  verification_remarks VARCHAR(500),
  verified_by_role VARCHAR(20),
  uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  verified_at TIMESTAMP NULL,

  FOREIGN KEY (citizen_id) REFERENCES citizens(id) ON DELETE CASCADE,
  KEY idx_citizen_documents_citizen_id (citizen_id),
  KEY idx_citizen_documents_status (verification_status),
  KEY idx_citizen_documents_uploaded_at (uploaded_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: Government Employees
CREATE TABLE IF NOT EXISTS gov_employees (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  employee_id VARCHAR(50) UNIQUE NOT NULL,
  email VARCHAR(100) UNIQUE NOT NULL,
  first_name VARCHAR(100) NOT NULL,
  last_name VARCHAR(100) NOT NULL,
  department VARCHAR(100) NOT NULL,
  designation VARCHAR(100) NOT NULL,
  phone_number VARCHAR(20),
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(50) NOT NULL,
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  KEY idx_email (email),
  KEY idx_employee_id (employee_id),
  KEY idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: Schemes
CREATE TABLE IF NOT EXISTS schemes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  scheme_code VARCHAR(50) UNIQUE NOT NULL,
  scheme_name VARCHAR(255) NOT NULL,
  description TEXT,
  sector VARCHAR(50) NOT NULL,
  scheme_type VARCHAR(50) NOT NULL,
  benefit_amount BIGINT NOT NULL,
  max_annual_income BIGINT NOT NULL,
  max_age INT NOT NULL,
  min_age INT NOT NULL,
  is_government_employee_eligible BOOLEAN DEFAULT FALSE,
  uses_ceps BOOLEAN NOT NULL DEFAULT FALSE,
  min_ceps_score INT NOT NULL DEFAULT 0,
  max_ceps_score INT NOT NULL DEFAULT 100,
  launch_date DATE NOT NULL,
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  KEY idx_scheme_code (scheme_code),
  KEY idx_sector (sector),
  KEY idx_is_active (is_active),
  CONSTRAINT check_benefit_amount CHECK (benefit_amount > 0),
  CONSTRAINT check_max_income CHECK (max_annual_income >= 0),
  CONSTRAINT check_age_range CHECK (min_age >= 0 AND max_age >= min_age)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: Scheme Conflict Rules (Rule Engine)
-- Example: (HOUSING, HOUSING) => REJECT or FLAG
CREATE TABLE IF NOT EXISTS scheme_conflict_rules (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  sector_a VARCHAR(50) NOT NULL,
  sector_b VARCHAR(50) NOT NULL,
  action VARCHAR(10) NOT NULL DEFAULT 'REJECT', -- REJECT|FLAG
  message VARCHAR(500) NOT NULL,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  UNIQUE KEY uq_conflict_rule (sector_a, sector_b),
  KEY idx_conflict_active (is_active),
  KEY idx_conflict_sector_a (sector_a),
  KEY idx_conflict_sector_b (sector_b)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: Enrollments (Scheme Applications)
CREATE TABLE IF NOT EXISTS enrollments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  enrollment_number VARCHAR(100) UNIQUE NOT NULL,
  citizen_id BIGINT NOT NULL,
  scheme_id BIGINT NOT NULL,
  status VARCHAR(50) NOT NULL DEFAULT 'APPLIED',
  current_stage VARCHAR(50) DEFAULT 'OFFICER_REVIEW',
  auditor_decision VARCHAR(20),
  officer_decision VARCHAR(20),
  admin_decision VARCHAR(20),
  eligibility_status VARCHAR(50),
  fraud_risk_level VARCHAR(50),
  is_fraud_detected BOOLEAN DEFAULT FALSE,
  fraud_risk_score INT DEFAULT 0,
  conflict_flag BOOLEAN NOT NULL DEFAULT FALSE,
  conflict_action VARCHAR(10),
  conflict_message VARCHAR(500),
  conflict_rule_id BIGINT NULL,
  rejection_reason TEXT,
  applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  approved_at TIMESTAMP NULL,
  rejected_at TIMESTAMP NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  FOREIGN KEY (citizen_id) REFERENCES citizens(id) ON DELETE RESTRICT,
  FOREIGN KEY (scheme_id) REFERENCES schemes(id) ON DELETE RESTRICT,
  KEY idx_enrollment_number (enrollment_number),
  KEY idx_citizen_id (citizen_id),
  KEY idx_scheme_id (scheme_id),
  KEY idx_status (status),
  KEY idx_is_fraud_detected (is_fraud_detected),
  KEY idx_conflict_flag (conflict_flag),
  CONSTRAINT unique_citizen_scheme UNIQUE (citizen_id, scheme_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: Projects (Government-Funded Projects)
CREATE TABLE IF NOT EXISTS projects (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_code VARCHAR(50) UNIQUE NOT NULL,
  project_name VARCHAR(255) NOT NULL,
  description TEXT,
  category VARCHAR(50) NOT NULL,
  state VARCHAR(50) NOT NULL,
  district VARCHAR(50) NOT NULL,
  allocated_mla VARCHAR(100),
  allocated_mp VARCHAR(100),
  total_budget BIGINT NOT NULL,
  released_amount BIGINT DEFAULT 0,
  spent_amount BIGINT DEFAULT 0,
  progress_percentage INT DEFAULT 0,
  quality_status VARCHAR(50) DEFAULT 'PENDING',
  status VARCHAR(50) DEFAULT 'PENDING',
  current_stage VARCHAR(50) DEFAULT 'AUDITOR_REVIEW',
  auditor_decision VARCHAR(20),
  officer_decision VARCHAR(20),
  admin_decision VARCHAR(20),
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  KEY idx_project_code (project_code),
  KEY idx_status (status),
  KEY idx_state (state),
  KEY idx_allocated_mla (allocated_mla),
  KEY idx_allocated_mp (allocated_mp),
  CONSTRAINT check_budget CHECK (total_budget > 0),
  CONSTRAINT check_progress CHECK (progress_percentage >= 0 AND progress_percentage <= 100),
CONSTRAINT check_amounts CHECK (released_amount >= 0 AND spent_amount >= 0 AND spent_amount <= released_amount)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: Project Updates (Official / Contractor progress uploads)
CREATE TABLE IF NOT EXISTS project_updates (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  submitted_by_role VARCHAR(20) NOT NULL, -- OFFICER|ADMIN|CONTRACTOR (demo)
  submitted_by_identifier VARCHAR(100),
  reported_progress INT NOT NULL DEFAULT 0,
  message VARCHAR(1000),
  photo_base64 LONGTEXT,
  geo_lat DECIMAL(10,7),
  geo_lng DECIMAL(10,7),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
  KEY idx_project_updates_project_id (project_id),
  KEY idx_project_updates_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: Public Corruption Evidence Submissions (Citizen input)
CREATE TABLE IF NOT EXISTS project_evidence (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  citizen_id BIGINT NULL,
  evidence_type VARCHAR(20) NOT NULL, -- PHOTO|COMPLAINT|REVIEW
  message VARCHAR(2000),
  photo_base64 LONGTEXT,
  geo_lat DECIMAL(10,7),
  geo_lng DECIMAL(10,7),
  progress_estimate INT,
  contractor_rating INT,
  status VARCHAR(20) NOT NULL DEFAULT 'NEW', -- NEW|VERIFIED|DISMISSED
  reviewer_role VARCHAR(20),
  reviewer_identifier VARCHAR(100),
  reviewer_remarks VARCHAR(500),
  reviewed_at TIMESTAMP NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
  FOREIGN KEY (citizen_id) REFERENCES citizens(id) ON DELETE SET NULL,
  KEY idx_project_evidence_project_id (project_id),
  KEY idx_project_evidence_status (status),
  KEY idx_project_evidence_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: Project Fraud / Quality Alerts (generated by rules)
CREATE TABLE IF NOT EXISTS project_alerts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  evidence_id BIGINT NULL,
  update_id BIGINT NULL,
  severity VARCHAR(10) NOT NULL DEFAULT 'LOW', -- LOW|MEDIUM|HIGH
  reason VARCHAR(500) NOT NULL,
  resolved BOOLEAN NOT NULL DEFAULT FALSE,
  resolved_by_role VARCHAR(20),
  resolved_by_identifier VARCHAR(100),
  resolved_at TIMESTAMP NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
  FOREIGN KEY (evidence_id) REFERENCES project_evidence(id) ON DELETE SET NULL,
  FOREIGN KEY (update_id) REFERENCES project_updates(id) ON DELETE SET NULL,
  KEY idx_project_alerts_project_id (project_id),
  KEY idx_project_alerts_resolved (resolved),
  KEY idx_project_alerts_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: Audit Logs
CREATE TABLE IF NOT EXISTS audit_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  action VARCHAR(50) NOT NULL,
  entity_type VARCHAR(50) NOT NULL,
  entity_id BIGINT,
  performed_by VARCHAR(100),
  details TEXT,
  status VARCHAR(50),
  ip_address VARCHAR(45),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  KEY idx_entity_type (entity_type),
  KEY idx_action (action),
  KEY idx_performed_by (performed_by),
  KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Indexes for frequent queries
CREATE INDEX idx_citizens_active ON citizens(is_active);
CREATE INDEX idx_enrollments_citizen_status ON enrollments(citizen_id, status);
CREATE INDEX idx_projects_state_status ON projects(state, status);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(created_at DESC);
CREATE INDEX idx_project_evidence_project_status ON project_evidence(project_id, status);
CREATE INDEX idx_project_alerts_project_resolved ON project_alerts(project_id, resolved);

-- Set session variables for data import
SET SESSION sql_mode='ALLOW_INVALID_DATES';
SET SESSION max_allowed_packet=16777216;

-- End of schema.sql
