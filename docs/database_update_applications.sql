-- StudentBridge application flow update
-- Run this on an existing MySQL database if these columns/tables are missing.

USE studentbridge;

-- Students save one shareable CV / resume link for one-click applications.
SET @cv_link_exists = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'student_profiles'
    AND COLUMN_NAME = 'cv_link'
);

SET @add_cv_link_sql = IF(
  @cv_link_exists = 0,
  'ALTER TABLE student_profiles ADD COLUMN cv_link VARCHAR(500)',
  'SELECT ''cv_link already exists'' AS message'
);

PREPARE add_cv_link_statement FROM @add_cv_link_sql;
EXECUTE add_cv_link_statement;
DEALLOCATE PREPARE add_cv_link_statement;

CREATE TABLE IF NOT EXISTS applications (
  id INT AUTO_INCREMENT PRIMARY KEY,
  job_id INT NOT NULL,
  student_email VARCHAR(255) NOT NULL,
  cv_link VARCHAR(500) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY unique_application (job_id, student_email),
  INDEX idx_applications_job_id (job_id),
  INDEX idx_applications_student_email (student_email)
);
