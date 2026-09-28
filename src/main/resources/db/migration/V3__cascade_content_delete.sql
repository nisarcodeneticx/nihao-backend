-- Make content deletes permanent: child rows (units, lessons, exercises, topics, progress)
-- are removed when their parent is deleted.

SET @fk := (
  SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'units' AND COLUMN_NAME = 'course_id'
    AND REFERENCED_TABLE_NAME = 'courses' LIMIT 1
);
SET @sql := IF(@fk IS NULL, 'SELECT 1', CONCAT('ALTER TABLE units DROP FOREIGN KEY `', @fk, '`'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
ALTER TABLE units ADD CONSTRAINT fk_units_course_id FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE;

SET @fk := (
  SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'lessons' AND COLUMN_NAME = 'unit_id'
    AND REFERENCED_TABLE_NAME = 'units' LIMIT 1
);
SET @sql := IF(@fk IS NULL, 'SELECT 1', CONCAT('ALTER TABLE lessons DROP FOREIGN KEY `', @fk, '`'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
ALTER TABLE lessons ADD CONSTRAINT fk_lessons_unit_id FOREIGN KEY (unit_id) REFERENCES units(id) ON DELETE CASCADE;

SET @fk := (
  SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'exercises' AND COLUMN_NAME = 'lesson_id'
    AND REFERENCED_TABLE_NAME = 'lessons' LIMIT 1
);
SET @sql := IF(@fk IS NULL, 'SELECT 1', CONCAT('ALTER TABLE exercises DROP FOREIGN KEY `', @fk, '`'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
ALTER TABLE exercises ADD CONSTRAINT fk_exercises_lesson_id FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE;

SET @fk := (
  SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'unit_topics' AND COLUMN_NAME = 'unit_id'
    AND REFERENCED_TABLE_NAME = 'units' LIMIT 1
);
SET @sql := IF(@fk IS NULL, 'SELECT 1', CONCAT('ALTER TABLE unit_topics DROP FOREIGN KEY `', @fk, '`'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
ALTER TABLE unit_topics ADD CONSTRAINT fk_unit_topics_unit_id FOREIGN KEY (unit_id) REFERENCES units(id) ON DELETE CASCADE;

SET @fk := (
  SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'unit_objectives' AND COLUMN_NAME = 'unit_id'
    AND REFERENCED_TABLE_NAME = 'units' LIMIT 1
);
SET @sql := IF(@fk IS NULL, 'SELECT 1', CONCAT('ALTER TABLE unit_objectives DROP FOREIGN KEY `', @fk, '`'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
ALTER TABLE unit_objectives ADD CONSTRAINT fk_unit_objectives_unit_id FOREIGN KEY (unit_id) REFERENCES units(id) ON DELETE CASCADE;

SET @fk := (
  SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_course_progress' AND COLUMN_NAME = 'course_id'
    AND REFERENCED_TABLE_NAME = 'courses' LIMIT 1
);
SET @sql := IF(@fk IS NULL, 'SELECT 1', CONCAT('ALTER TABLE user_course_progress DROP FOREIGN KEY `', @fk, '`'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
ALTER TABLE user_course_progress ADD CONSTRAINT fk_user_course_progress_course_id FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE;

SET @fk := (
  SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_unit_progress' AND COLUMN_NAME = 'unit_id'
    AND REFERENCED_TABLE_NAME = 'units' LIMIT 1
);
SET @sql := IF(@fk IS NULL, 'SELECT 1', CONCAT('ALTER TABLE user_unit_progress DROP FOREIGN KEY `', @fk, '`'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
ALTER TABLE user_unit_progress ADD CONSTRAINT fk_user_unit_progress_unit_id FOREIGN KEY (unit_id) REFERENCES units(id) ON DELETE CASCADE;

SET @fk := (
  SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_lesson_progress' AND COLUMN_NAME = 'lesson_id'
    AND REFERENCED_TABLE_NAME = 'lessons' LIMIT 1
);
SET @sql := IF(@fk IS NULL, 'SELECT 1', CONCAT('ALTER TABLE user_lesson_progress DROP FOREIGN KEY `', @fk, '`'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
ALTER TABLE user_lesson_progress ADD CONSTRAINT fk_user_lesson_progress_lesson_id FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE;

SET @fk := (
  SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'example_sentences' AND COLUMN_NAME = 'vocabulary_id'
    AND REFERENCED_TABLE_NAME = 'vocabulary' LIMIT 1
);
SET @sql := IF(@fk IS NULL, 'SELECT 1', CONCAT('ALTER TABLE example_sentences DROP FOREIGN KEY `', @fk, '`'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
ALTER TABLE example_sentences ADD CONSTRAINT fk_example_sentences_vocabulary_id FOREIGN KEY (vocabulary_id) REFERENCES vocabulary(id) ON DELETE CASCADE;

SET @fk := (
  SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'vocabulary_topics' AND COLUMN_NAME = 'vocabulary_id'
    AND REFERENCED_TABLE_NAME = 'vocabulary' LIMIT 1
);
SET @sql := IF(@fk IS NULL, 'SELECT 1', CONCAT('ALTER TABLE vocabulary_topics DROP FOREIGN KEY `', @fk, '`'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
ALTER TABLE vocabulary_topics ADD CONSTRAINT fk_vocabulary_topics_vocabulary_id FOREIGN KEY (vocabulary_id) REFERENCES vocabulary(id) ON DELETE CASCADE;
