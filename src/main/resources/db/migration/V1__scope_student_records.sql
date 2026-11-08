-- Add tenant ownership to databases created by older app releases.
-- Existing rows stay unassigned because the app cannot safely infer owners.
-- Unassigned rows are intentionally hidden from authenticated users.

SET @ddl = IF(
  EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'assignment')
  AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'assignment' AND column_name = 'student_id'),
  'ALTER TABLE assignment ADD COLUMN student_id BIGINT NULL', 'SELECT 1');
PREPARE migration_stmt FROM @ddl; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

SET @ddl = IF(
  EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'ai_notes')
  AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'ai_notes' AND column_name = 'student_id'),
  'ALTER TABLE ai_notes ADD COLUMN student_id BIGINT NULL', 'SELECT 1');
PREPARE migration_stmt FROM @ddl; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

SET @ddl = IF(
  EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'attendance')
  AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'attendance' AND column_name = 'student_id'),
  'ALTER TABLE attendance ADD COLUMN student_id BIGINT NULL', 'SELECT 1');
PREPARE migration_stmt FROM @ddl; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

SET @ddl = IF(
  EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'gpa')
  AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'gpa' AND column_name = 'student_id'),
  'ALTER TABLE gpa ADD COLUMN student_id BIGINT NULL', 'SELECT 1');
PREPARE migration_stmt FROM @ddl; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

SET @ddl = IF(
  EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'notes')
  AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'notes' AND column_name = 'student_id'),
  'ALTER TABLE notes ADD COLUMN student_id BIGINT NULL', 'SELECT 1');
PREPARE migration_stmt FROM @ddl; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

SET @ddl = IF(
  EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'reminder')
  AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'reminder' AND column_name = 'student_id'),
  'ALTER TABLE reminder ADD COLUMN student_id BIGINT NULL', 'SELECT 1');
PREPARE migration_stmt FROM @ddl; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

SET @ddl = IF(
  EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'flashcards')
  AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'flashcards' AND column_name = 'student_id'),
  'ALTER TABLE flashcards ADD COLUMN student_id BIGINT NULL', 'SELECT 1');
PREPARE migration_stmt FROM @ddl; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

-- Replace the legacy global one-row-per-semester constraint with one that
-- permits each account to track its own GPA for the same semester.
SET @has_gpa = EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'gpa');
SET @has_legacy_unique = EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'gpa' AND index_name = 'uk_gpa_semester');
SET @ddl = IF(@has_gpa AND @has_legacy_unique, 'ALTER TABLE gpa DROP INDEX uk_gpa_semester', 'SELECT 1');
PREPARE migration_stmt FROM @ddl; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @has_scoped_unique = EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'gpa' AND index_name = 'uk_gpa_student_semester');
SET @ddl = IF(@has_gpa AND NOT @has_scoped_unique, 'ALTER TABLE gpa ADD CONSTRAINT uk_gpa_student_semester UNIQUE (student_id, semester)', 'SELECT 1');
PREPARE migration_stmt FROM @ddl; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
