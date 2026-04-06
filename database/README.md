-- GovShield Database Setup Instructions
-- Complete guide for database initialization

-- ==================== QUICK START ====================

-- Manual Setup

-- Step 1: Create database
CREATE DATABASE IF NOT EXISTS govshield;
USE govshield;

-- Step 2: Create tables by running schema.sql
-- In MySQL CLI:
-- mysql -u root -p < schema.sql
-- OR
-- source schema.sql;

-- Step 3: Import sample data by running data.sql
-- In MySQL CLI:
-- mysql -u root -p govshield < data.sql
-- OR
-- source data.sql;

-- =================================================

-- ==================== VERIFICATION ====================

-- Verify database exists
SHOW DATABASES LIKE 'govshield';

-- Verify all tables created
USE govshield;
SHOW TABLES;

-- Verify record counts
SELECT 'citizens' as table_name, COUNT(*) as record_count FROM citizens
UNION ALL
SELECT 'gov_employees', COUNT(*) FROM gov_employees
UNION ALL
SELECT 'schemes', COUNT(*) FROM schemes
UNION ALL
SELECT 'enrollments', COUNT(*) FROM enrollments
UNION ALL
SELECT 'projects', COUNT(*) FROM projects
UNION ALL
SELECT 'audit_logs', COUNT(*) FROM audit_logs;

-- Table Details
DESCRIBE citizens;
DESCRIBE gov_employees;
DESCRIBE schemes;
DESCRIBE enrollments;
DESCRIBE projects;
DESCRIBE audit_logs;

-- =================================================

-- ==================== CONNECTION TESTING ====================

-- Test connection with correct credentials
-- mysql -h localhost -u root -p -e "SELECT VERSION();"

-- Expected output: MySQL version number (e.g., 8.0.x)

-- =================================================

-- ==================== USEFUL QUERIES ====================

-- List all citizens
SELECT ugid, first_name, last_name, email, annual_income, employment_status 
FROM citizens;

-- List all government employees
SELECT employee_id, email, first_name, last_name, department, role 
FROM gov_employees;

-- List active schemes
SELECT scheme_code, scheme_name, sector, benefit_amount, max_annual_income 
FROM schemes 
WHERE is_active = TRUE;

-- View citizen enrollments
SELECT 
  c.ugid,
  c.first_name,
  s.scheme_name,
  e.status,
  e.eligibility_status,
  e.fraud_risk_level
FROM enrollments e
JOIN citizens c ON e.citizen_id = c.id
JOIN schemes s ON e.scheme_id = s.id;

-- View project details
SELECT 
  project_code,
  project_name,
  total_budget,
  released_amount,
  spent_amount,
  progress_percentage,
  status
FROM projects;

-- View fraud alerts
SELECT 
  e.enrollment_number,
  c.first_name,
  c.last_name,
  s.scheme_name,
  e.fraud_risk_level,
  e.fraud_risk_score,
  e.is_fraud_detected
FROM enrollments e
JOIN citizens c ON e.citizen_id = c.id
JOIN schemes s ON e.scheme_id = s.id
WHERE e.fraud_risk_level IN ('HIGH', 'MEDIUM')
   OR e.is_fraud_detected = TRUE;

-- View audit trail
SELECT 
  action,
  entity_type,
  performed_by,
  details,
  status,
  created_at
FROM audit_logs
ORDER BY created_at DESC
LIMIT 20;

-- =================================================

-- ==================== BACKUP & RESTORE ====================

-- Backup entire database
-- mysqldump -u root -p govshield > govshield_backup.sql

-- Backup with timestamp
-- mysqldump -u root -p govshield > govshield_backup_$(date +%Y%m%d_%H%M%S).sql

-- Restore from backup
-- mysql -u root -p govshield < govshield_backup.sql

-- Backup all tables with structure and data
-- mysqldump -u root -p --all-databases > all_databases_backup.sql

-- =================================================

-- ==================== MAINTENANCE ====================

-- Check database size
SELECT 
  table_name,
  ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size in MB'
FROM information_schema.TABLES
WHERE table_schema = 'govshield'
ORDER BY (data_length + index_length) DESC;

-- Analyze tables for optimization
ANALYZE TABLE citizens, gov_employees, schemes, enrollments, projects, audit_logs;

-- Optimize tables
OPTIMIZE TABLE citizens, gov_employees, schemes, enrollments, projects, audit_logs;

-- Check table integrity
CHECK TABLE citizens, gov_employees, schemes, enrollments, projects, audit_logs;

-- Repair corrupted tables (if needed)
REPAIR TABLE citizens, gov_employees, schemes, enrollments, projects, audit_logs;

-- =================================================

-- ==================== USER MANAGEMENT ====================

-- Create GovShield application user (recommended for production)
CREATE USER 'govshield_user'@'localhost' IDENTIFIED BY 'GovShield@2727';

-- Grant privileges
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX 
ON govshield.* 
TO 'govshield_user'@'localhost';

-- Apply privileges
FLUSH PRIVILEGES;

-- Test new user connection
-- mysql -u govshield_user -p govshield

-- Remove test user
-- DROP USER 'test_user'@'localhost';

-- =================================================

-- ==================== TROUBLESHOOTING ====================

-- If you get "Access Denied" error:
-- Make sure MySQL server is running
-- Check username and password
-- Verify host (localhost vs 127.0.0.1)

-- If tables are not created:
-- Check schema.sql syntax
-- Verify CHARACTER SET is compatible
-- Check for duplicate table definitions

-- If sample data import fails:
-- Verify foreign key constraints are satisfied
-- Check for unique constraint violations
-- Ensure all referenced parent records exist

-- Common MySQL Commands:
-- mysql -u root -p                    -- Login to MySQL
-- SHOW DATABASES;                     -- List all databases
-- USE govshield;                      -- Switch to database
-- SHOW TABLES;                        -- List all tables
-- DESC table_name;                    -- Show table structure
-- SELECT * FROM table_name;           -- View table data
-- EXIT;                               -- Exit MySQL CLI

-- =================================================

-- End of setup instructions
